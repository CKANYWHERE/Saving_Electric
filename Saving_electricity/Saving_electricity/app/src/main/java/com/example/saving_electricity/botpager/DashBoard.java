package com.example.saving_electricity.botpager;

 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 import android.widget.TextView;

 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.example.saving_electricity.R;
 import com.example.saving_electricity.network.NetworkUtil;
 import com.example.saving_electricity.util.Config;
 import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Text;

 import java.util.ArrayList;
import java.util.List;

public class DashBoard extends Fragment {
    PieChart pieChart;
   // LineChart realtimeChart;
   //  BarChart barChart;
    NetworkUtil networkUtil;
    TextView realTimeLog;

    String power;
    String percent;
    String date;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.dashboard, container, false);
        pieChart = (PieChart) v.findViewById(R.id.piechart);
        realTimeLog = (TextView) v.findViewById(R.id.txtRealtime);
        realTimeLog.setMovementMethod(new ScrollingMovementMethod());
        //  realtimeChart = (LineChart)v.findViewById(R.id.realtimelog);
       // barChart = (BarChart)v.findViewById(R.id.barchart);
        networkUtil = new NetworkUtil(getContext());
        new GetPieChartData().execute();
        setPieChart(100,0);
        getRealTimeData();
       // setBarChart();
       // setRealtimeChart();
        return v;
    }

    private void getRealTimeData(){
        final Handler handler = new Handler();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        requestRealTimeData();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                realTimeLog.append("시간:"+date + " " +"공급예비율" +percent+ " " + "공급예비력"+power +"\n"
                                );
                            }
                        });
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    public void requestRealTimeData(){
        networkUtil.requestServer(Config.CROLL_DATA,realTimeSuccessListener(),realTimeErrorListener());
    }
    private Response.Listener<JSONArray> realTimeSuccessListener() {
        return new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                for(int i=0;i<response.length();i++){
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                         power = jsonObject.getString("power");
                         percent = jsonObject.getString("percent");
                         date = jsonObject.getString("date");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private Response.ErrorListener realTimeErrorListener() {
        return new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.toString());
            }
        };
    }


    class GetPieChartData extends AsyncTask<Void, String, Void> {
        //백그라운드
        @Override
        protected Void doInBackground(Void... voids) {
            requestPieData();
            return null;
        }
    }

    public void requestPieData(){
        networkUtil.requestServer(Config.TEST_URL,networkSuccessListener(),networkErrorListener());
    }

    private Response.Listener<JSONArray> networkSuccessListener() {
        return new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                for(int i=0;i<response.length();i++){
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                         double used = jsonObject.getDouble("used");
                         double notused = jsonObject.getDouble("notused");
                         setPieChart(used,notused);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private Response.ErrorListener networkErrorListener() {
        return new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.toString());
            }
        };
    }

  /*  private void setBarChart() {

        ArrayList<BarEntry> bargroup1 = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            bargroup1.add(new BarEntry(i, (float) (Math.random())));
        }
        BarDataSet barDataSet1 = new BarDataSet(bargroup1, "현재까지 아낀 전력량");
        barDataSet1.setColors(Color.rgb(104, 241, 175));
        BarData data = new BarData(barDataSet1);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);

        YAxis yLAxis = barChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = barChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDescription(description);
        barChart.animateY(2000, Easing.EasingOption.EaseInCubic);
        barChart.invalidate();
    }
    private void setRealtimeChart(){

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 1));
        entries.add(new Entry(2, 2));
        entries.add(new Entry(3, 0));
        entries.add(new Entry(4, 4));
        entries.add(new Entry(5, 3));

        LineDataSet lineDataSet = new LineDataSet(entries, "전력량");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);

        LineData lineData = new LineData(lineDataSet);
        realtimeChart.setData(lineData);

        XAxis xAxis = realtimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);

        YAxis yLAxis = realtimeChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = realtimeChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        realtimeChart.setDoubleTapToZoomEnabled(false);
        realtimeChart.setDrawGridBackground(false);
        realtimeChart.setDescription(description);
        realtimeChart.animateY(2000, Easing.EasingOption.EaseInCubic);
        realtimeChart.invalidate();
    }*/

    private void setPieChart(double used, double notused){
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        ArrayList<PieEntry> yValues =  new ArrayList<PieEntry>();
        yValues.add(new PieEntry((float)used,"현재 절약량"));
        yValues.add(new PieEntry((float) notused,"절약하지 않았을 경우"));
        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        Description description = new Description();
        description.setText("최대전력 대비"); //라벨
        description.setTextSize(15);
        pieChart.setDescription(description);

        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic); //애니메이션

        PieDataSet dataSet = new PieDataSet(yValues,"동");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.YELLOW);
        pieChart.setData(data);
    }


}