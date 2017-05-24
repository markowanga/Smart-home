/**
 * Created by Marcin on 11.04.2017.
 */
var db = require('./serverDataBaseMySQL');
var nodeMailer = require('nodemailer');

module.exports = {
    isDeviceAuthorizated: function (connection, numIMEI) {
        return db.getListOfAuthorizedPhones(connection, numIMEI);
    },

    sendErrorEmail: function (errorSimple, errorHtml) {
        // create reusable transporter object using the default SMTP transport
        var transporter = nodeMailer.createTransport({
            service: 'gmail',
            auth: {
                user: 'magda.powiadomienia.pokoj@gmail.com',
                pass: 'Markowanga1'
            }
        });

        // setup email data with unicode symbols
        var mailOptions = {
            from: '"Smarthome manager" <magda.powiadomienia.pokoj@gmail.com>', // sender address
            to: 'markowanga@gmail.com', // list of receivers
            subject: 'Error in application', // Subject line
            text: errorSimple, // plain text body
            html: errorHtml // html body
        };

        // send mail with defined transport object
        transporter.sendMail(mailOptions, function (error, info) {
            if (error) {
                return console.log(error);
            }
            console.log('Message %s sent: %s', info.messageId, info.response);
        });
    }
};