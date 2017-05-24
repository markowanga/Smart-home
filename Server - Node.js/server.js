/**
 * Created by Marcin on 05.04.2017.
 */
var WebSocketServer = require('websocket').server;
var http = require('http');
var fs = require('fs');
var path = require('path');
var url = require('url');
var mime = require('mime');
var mysql = require('mysql');
var gateService = require('./gateService');
var returnJson = require('./returnJSON');
var db = require('./serverDataBaseMySQL');
var port = process.env.PORT || 1337;
var cache = {};

var mqWebSocket = null;
var dbConnection = db.getConnection();

var server = http.createServer(function (request, response) {
    if (request.url.substring(0, 5) == '/port')
        returnJson.returnWebSocketPort(response, port);
    else if (request.url.substring(0, 17) == '/mobileController')
        returnJson.mobileControllerResponce(request, response, dbConnection, mqWebSocket);
    else if (request.url.substring(0, 16) == '/historyForPhone')
        returnJson.historyForPhone(response, dbConnection);
    else if (request.url.substring(0, 15) == '/showConnection')
        returnJson.showConnection(response, mqWebSocket);
    else if (request.url.substring(0, 13) == '/sendErrorLog')
        returnJson.sendErrorLog(request, response);
    else if (request.url.substring(0, 13) == '/mqBreaks')
        returnJson.mqBreaks(response, dbConnection);
    else if (request.url.substring(0, 2) == '/m')
    {
        gateService.sendErrorEmail("fwefeqrv");
        returnJson.mqBreaks(response, dbConnection);
    }    else if (request.url == '' || request.url == '/')
        return sendFile(response, './index.html');
    else
        returnJson.pageNotFound(response);
});

function sendFile(response, filePath) {
    response.writeHead(
        200,
        {"content-type": mime.lookup(path.basename(filePath))}
    );
    fs.readFile('./index.html', function (err, data) {
        if (err)
            returnJson.pageNotFound(response);

        response.end(data);
    });

}


server.listen(port, function () {
    console.log("nasłuchiwanie " + port);
});

wsServer = new WebSocketServer({
    httpServer: server
});

wsServer.on('request', function (request) {
    console.log('WebSocket request');
    webSocketMq(request);

    // odczyt parametrów :D
    //console.log(request.resourceURL.query.ala);
});

function webSocketEcho(request) {
    console.log('WebSocket echo request');
    var connection = request.accept(null, request.origin);

    connection.on('message', function (message) {
        console.log(message);
        connection.send(message);
    });

    connection.on('close', function (connection) {
        console.log("Close webSocket echo connection");
        connection = null;
        mqWebSocket = null;
    });
}

function webSocketMq(request) {
    // odczyt parametrów :D
    //console.log(request.resourceURL.query.ala);

    console.log('WebSocket mq request');
    var connection = request.accept(null, request.origin);
    mqWebSocket = connection;

    connection.on('message', function (message) {
        console.log("Received mq message: ", message);
    });

    connection.on('close', function (connection) {
        console.log("Close webSocket mq connection");
        connection = null;
        mqWebSocket = null;
        db.addErrorConnectionRecord(dbConnection);
    });
}

function webSocketSmartphone(request) {
    console.log('WebSocket smartphone request');
    var connection = request.accept(null, request.origin);
    mqWebSocket = connection;

    connection.on('message', function (message) {
        console.log("Received smartphone message: ", message);
    });

    connection.on('close', function (connection) {
        console.log("Close webSocket smartphone connection");
        connection = null;
        mqWebSocket = null;
    });
}