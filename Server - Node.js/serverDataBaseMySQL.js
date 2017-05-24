/**
 * Created by Marcin on 10.04.2017.
 */
var mysql = require('mysql');

module.exports = {
    getConnection: function () {
        var connection = mysql.createConnection({
            host: 'serwer1689340.home.pl',
            user: '22255857_0000002',
            password: 'otwieraniebramy1',
            database: '22255857_0000002',
            dateStrings: 'date'
        });

        setInterval(function () {
            connection.query('SELECT 1', function () {

            });
        }, 5000);
        
        return connection;
    },

    getListOfAuthorizedPhones: function (connection, IMEI) {
        connection.query('SELECT * FROM SmartphonesList', function (err, result, fields) {
            if (err)
                throw err;

            for (var i = 0; i < result.length; i++) {
                if (IMEI == (result[i].IMEI) && result[i].IsActive == 1) {
                    console.log("okokok");
                    return true;
                }
            }
            return false;
        });
    },

    addEvetToHistory: function (connection, smartphoneId, openGate, openWicket) {
        connection.query("INSERT INTO `RequestSmartphoneHistory`(`SmartphoneId`, `Date`, `OpenGate`, `OpenWicket`) "
            + "VALUES (" + smartphoneId + ", NOW()," + openGate + "," + openWicket + ")");
    },

    addErrorConnectionRecord: function(dbConnection) {
        dbConnection.query("INSERT INTO `MqBreaks` (`Time`) VALUES (NOW())", function(err, result) {
            if (err)
                throw err;
        });
    }
};