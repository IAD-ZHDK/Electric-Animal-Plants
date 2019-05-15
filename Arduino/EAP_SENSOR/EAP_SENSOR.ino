// **************
// WIFI EAP control
// June 2018
// Author: Luke Franzke
// **************
#include <WiFi101.h>
#include <MQTT.h>
#include <Adafruit_ADS1015.h>

const char ssid[] = "BRIDGE"; //
const char pass[] = "internet"; // 
const char IP[] = "192.168.1.26";

//const char ssid[] = "TEI_ZHDK_2019";
//const char pass[] = "Nc3MjGbJU6DxdDMM";
//const char IP[] = "10.0.1.2";


WiFiClient net;
MQTTClient client;

unsigned long lastMillis = 0;

Adafruit_ADS1115 ads;  /* Use this for the 16-bit version */

//smoothing
const int numReadings = 90; // 20 is good
const int messageDelay = 100;
int readings[numReadings];      // the readings from the analog input
int readIndex = 0;              // the index of the current reading
int total = 0;                  // the running total
int average = 0;                // the average
int lastMax  = 0;
int lastLow = 30000;


void connect() {
  Serial.print("checking wifi...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(1000);
  }

  //Serial.print("\nconnecting...");
  while (!client.connect("ard_sensor", "sensor", "sensor")) {
    //Serial.print(".");
    delay(1000);
  }

  //  Serial.println("\nconnected!");

  // client.subscribe("#");
}

void messageReceived(String &topic, String &payload) {
  //Serial.println("incoming: " + topic + " - " + payload);
}

void setup() {
  //pinMode(LED_BUILTIN, OUTPUT);
  WiFi.begin(ssid, pass);
  // Note: Local domain names (e.g. "Computer.local" on OSX) are not supported by Arduino.
  // You need to set the IP address directly.

  client.begin(IP, 1883, net);
  client.onMessage(messageReceived);
  delay(1000);
  connect();
  // initialize all the readings to 0:
  for (int thisReading = 0; thisReading < numReadings; thisReading++) {
    readings[thisReading] = 0;
  }
  ads.setGain(GAIN_TWO);        // 2x gain   +/- 2.048V  1 bit = 1mV      0.0625mV
  ads.begin();
}

void loop() {


  //
  client.loop();

  if (!client.connected()) {
    connect();
  }
  // publish a message at interval
  // map the sensor value to between the lowest and highest recording
  if (millis() - lastMillis > messageDelay) {
    lastMillis = millis();
    client.publish("/SENS", String(average));
    // Serial.println(average);
  } else {
    // don't make readings while publishing
  }
  sensor();
  delay(1);
}
// average /
void sensor() {
  int results;
  results = ads.readADC_Differential_0_1();
  // subtract the last reading:
  total = total - readings[readIndex];
  // read from the sensor:
  readings[readIndex] = results;
  // add the reading to the total:
  total = total + readings[readIndex];
  // advance to the next position in the array:
  readIndex = readIndex + 1;

  // if we're at the end of the array...
  if (readIndex >= numReadings) {
    readIndex = 0;
  }
  //  calculate the average:
  average = total / numReadings;
}
