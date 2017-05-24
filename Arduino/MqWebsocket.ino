/*
Name:		MqWebsocket.ino
Created:	4/30/2017 8:42:35 PM
Author:	Marcin
*/

// the setup function runs once when you press reset or power the board
#include <ESP8266WiFi.h>
#include <WebSocketsClient.h>
#include <ArduinoJson.h>

#define GATE_PIN 5 // control gate on pin 5
#define WICKET_PIN 4 // control wicket on pin 4

// wifi connection
const char* ssid = "---";
const char* password = "---";
const char* host = "---";

WebSocketsClient webSocket;

/*
This function gives the signal on gate pin
*/
void openGate() {
	digitalWrite(GATE_PIN, HIGH);
	delay(1000);
	digitalWrite(GATE_PIN, LOW);
}

/*
This function gives the signal on wicket pin
*/
void openWicket() {
	digitalWrite(WICKET_PIN, HIGH);
	delay(1000);
	digitalWrite(WICKET_PIN, LOW);
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {

	switch (type) {
	case WStype_DISCONNECTED:
		Serial.printf("[WSc] Disconnected!\n");
		break;
	case WStype_CONNECTED: 
	{
		Serial.printf("[WSc] Connected to url: %s\n", payload);

		// send message to server when Connected
		webSocket.sendTXT("Connected");
	}
	break;
	case WStype_TEXT:
	{
		Serial.printf("[WSc] get text: %s\n", payload);

		const size_t BUFFER_SIZE = JSON_OBJECT_SIZE(10);
		DynamicJsonBuffer jsonBuffer(BUFFER_SIZE);
		JsonObject& root = jsonBuffer.parseObject(payload);
		if (!root.success()) {
			Serial.println("[JSON parsing failed!]");
			return;
		}
		
		if (root["gate"] == "true")
			openGate();
		if (root["wicket"] == "true")
			openWicket();

	}
	break;
	case WStype_BIN:
		Serial.printf("[WSc] get binary length: %u\n", length);

		// send data to server
		// webSocket.sendBIN(payload, length);
		break;
	default:
		Serial.println("[WSc] Nothing");
	}
}

void setup() {
	pinMode(GATE_PIN, OUTPUT);
	digitalWrite(GATE_PIN, LOW);

	pinMode(WICKET_PIN, OUTPUT);
	digitalWrite(WICKET_PIN, LOW);

	Serial.begin(115200);
	Serial.println();

	for (int a = 5; a < 0; a--)
	{
		Serial1.println(a);
		delay(1000);
	}

	Serial.printf("Connecting to %s ", ssid);
	WiFi.begin(ssid, password);
	while (WiFi.status() != WL_CONNECTED) {
		delay(500);
		Serial.print(".");
	}
	WiFi.setAutoReconnect(true);
	WiFi.hostname("SmartHome gateController");
	Serial.println(" connected");

	// connect with websocket
	webSocket.beginSSL(host, 80);
	webSocket.onEvent(webSocketEvent);
}

// the loop function runs over and over again until power down or reset
void loop() {
	webSocket.loop();
}