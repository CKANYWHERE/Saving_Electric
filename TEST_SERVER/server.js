var express        = require('express');
var app            = express();
var bodyParser     = require('body-parser');
var methodOverride = require('method-override');
var logger         = require('morgan');
const request      = require('request');
const cheerio      = require('cheerio');
const date_utils   = require('date-utils');

var port = process.env.PORT || 3000; // set PORT

// get all data/stuff of the body (POST) parameters
app.use(logger('tiny'));
app.use(bodyParser.json());
app.use(bodyParser.json({ type: 'application/vnd.api+json' })); // parse application/vnd.api+json as json
app.use(bodyParser.urlencoded({ extended: true })); // parse application/x-www-frorm-urlencoded
//app.use(timeout(12000));
app.use(methodOverride('X-HTTP-Method-Override')); // override with the X-HTTP-Method-Override header in the request. simulate DELETE/PUT
var server = app.listen(port);
server.timeout = 5000;
var io = require('socket.io')(server);

app.get('/',function(req,res){
    res.send([{used:60.0,notused:30.0}]);
}),

app.get('/croll',function(req,res){
    let url = "http://www.kpx.or.kr/www/contents.do?key=217";
    let power, percent, date
    request(url,function(error,response,body){
       
     
        const $ = cheerio.load(body);
        $('#contents > div.content > div > div > div.conTable_type05.mb40 > table > tbody > tr:nth-child(3) > td').each(function(){
            let power_info = $(this);
            power = power_info.text();
        });
        $('#contents > div.content > div > div > div.conTable_type05.mb40 > table > tbody > tr:nth-child(4) > td').each(function(){
            let percent_info = $(this);
            percent = percent_info.text();
        });
        
        let newDate = new Date();
        date = newDate.toFormat('YYYY-MM-DD HH24:MI:SS');
        console.log({power:power,percent:percent,date:date});
        res.json([{power:power,percent:percent,date:date}]);
    });
});

app.set('socketio',io);

//app.set('classifier', classifier);

console.log('server is on port ' + port);
exports = module.exports = app;
