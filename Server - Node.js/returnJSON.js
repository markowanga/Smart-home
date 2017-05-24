/**
 * Created by Marcin on 09.04.2017.
 */
var url = require('url');
var db = require('./serverDataBaseMySQL');
var gS = require('./gateService');
var qs = require('querystring');

module.exports = {

    pageNotFound: function (response) {
        response.writeHead(
            200,
            {"content-type": "application/json"}
        );
        response.write(JSON.stringify({message: 'page not found'}, null, 4));
        response.end();
    },

    returnWebSocketPort: function (response, port) {
        response.writeHead(
            200,
            {"content-type": "application/json"}
        );
        response.write(JSON.stringify({port: port}, null, 4));
        response.end();
    },

    historyForPhone: function (response, dbConnection) {
        response.writeHead(
            200,
            {"content-type": "application/json"}
        );
        dbConnection.query('SELECT RequestSmartphoneHistory.Id, RequestSmartphoneHistory.Date, ' +
            'RequestSmartphoneHistory.OpenGate, RequestSmartphoneHistory.OpenWicket, ' +
            'SmartphonesList.Phone, SmartphonesList.Description ' +
            'FROM RequestSmartphoneHistory INNER JOIN SmartphonesList ' +
            'ON RequestSmartphoneHistory.SmartphoneId = SmartphonesList.Id ' +
            'ORDER BY RequestSmartphoneHistory.Id DESC LIMIT 50', function (err, result) {
            if (err)
                throw err;

            response.write(JSON.stringify(result, null, 4));
            response.end();
        });
    },

    mqBreaks: function (response, dbConnection) {
        response.writeHead(
            200,
            {"content-type": "application/json"}
        );
        dbConnection.query('SELECT * FROM MqBreaks ORDER BY Id DESC', function (err, result) {
            if (err)
                throw err;

            response.write(JSON.stringify(result, null, 4));
            response.end();
        });
    },

    mobileControllerResponce: function (request, response, dbConnection, mqWebSocket) {
        var url_parts = url.parse(request.url, true);
        var query = url_parts.query;
        console.log('brama: ', query.gate);
        console.log('furtka: ', query.wicket);
        console.log('IMEI: ', query.IMEI);
        console.log('mq: ', mqWebSocket != null ? 'enable' : 'disable');

        response.writeHead(
            200,
            {"Content-Type": "application/json; charset=utf-8"}
        );

        dbConnection.query('SELECT * FROM SmartphonesList', function (err, result) {
            if (err)
                throw err;
            var founded = false;

            for (var i = 0; i < result.length; i++) {
                if (query.IMEI == (result[i].IMEI) && result[i].IsActive == 1) {
                    founded = true;
                    if (mqWebSocket !== null) {
                        mqWebSocket.send(JSON.stringify({gate: query.gate, wicket: query.wicket}));
                        db.addEvetToHistory(dbConnection, result[i].Id, query.gate, query.wicket);
                    }

                    response.write(JSON.stringify({
                        gate: query.gate,
                        wicket: query.wicket,
                        mq: mqWebSocket != null ? 'enable' : 'disable'
                    }, null, 4));
                    break;
                }
            }
            if (founded == false) {
                response.write(JSON.stringify({
                    message: 'authorization phone failed'
                }, null, 4));
            }
            response.end();
        });
    },

    showConnection: function (response, mqWebSocket) {
        response.writeHead(
            200,
            {"Content-Type": "application/json; charset=utf-8"}
        );
        response.write(JSON.stringify({
            message: mqWebSocket==null?"disable":"enable"
        }, null, 4));
        response.end();
    },

    sendErrorLog: function (request, response) {
        response.writeHead(
            200,
            {"Content-Type": "application/json; charset=utf-8"}
        );
        if (request.method == 'POST') {
            var body = '';

            request.on('data', function (data) {
                body += data;

                // Too much POST data, kill the connection!
                // 1e6 === 1 * Math.pow(10, 6) === 1 * 1000000 ~~~ 1MB
                if (body.length > 1e6)
                    request.connection.destroy();
            });

            request.on('end', function () {
                var post = qs.parse(body);
                console.log(post);
                gS.sendErrorEmail(post.logSimple, post.logHtml);
                response.end();
            });
        }
    }
};