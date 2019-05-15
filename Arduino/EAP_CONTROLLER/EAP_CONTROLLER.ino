// **************
// WIFI EAP control
// June 2018
// Author: Luke Franzke
// **************
#include <WiFi101.h>
#include <MQTT.h>

//const char ssid[] = "BRIDGE";
//const char pass[] = "internet";
//const char IP[] = "192.168.1.26";

const char ssid[] = "TEI_ZHDK_2019";
const char pass[] = "Nc3MjGbJU6DxdDMM"; // Nc3MjGbJU6DxdDMM
const char IP[] = "10.0.1.3";

WiFiClient net;
MQTTClient client;

unsigned long lastMillis = 0;

int NoEAPS = 8;
int EAPpins[] = {11, A2, A3, 10, 12, A1, A4, 9};
//int EAPpins[] = {9, A4, A1, 12, 10, A3, A2, 11}; 
bool EAPS[] = {false, false, false, false, false, false, false, false};
bool EAP_OLD_VALUE[] = {false, false, false, false, false, false, false, false};
int EAPtimout = 8000; // how long before EAP shut off for safety
int sequenceTime;
int noSignalCount;
unsigned long lastActivation = 0;

void connect() {
  Serial.print("checking wifi...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(1000);
  }

  Serial.print("\nconnecting...");
  while (!client.connect("arduino", "try", "try")) {
    Serial.print(".");
    delay(1000);
  }

    Serial.println("\nconnected!");

  // client.subscribe("#");
  client.subscribe("/EAPS_IAD");
  // client.unsubscribe("/EAPS_IAD");
}

void messageReceived(String &topic, String &payload) {

  //Serial.println("incoming: " + topic + " - " + payload);
  char payloadChar[8];
  boolean EAPBufer[8];
  payload.toCharArray(payloadChar, 9);
  for (int i = 0; i < 8; i++) {
    // Serial.println(payloadChar[i]);
    if (payloadChar[i] == '1') {
      EAPBufer[i] = true;
    } else {
      EAPBufer[i] = false;
    }
  }
  activeEAPS(EAPBufer);
}

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  for (int i = 0; i < NoEAPS; i++) {
    pinMode(EAPpins[i], OUTPUT);
  }
  disableEAPS();
  //Serial.begin(115200);
  WiFi.begin(ssid, pass);
  // Note: Local domain names (e.g. "Computer.local" on OSX) are not supported by Arduino.
  // You need to set the IP address directly.

    client.begin(IP, 1883, net);
  client.onMessage(messageReceived);
  delay(1000);
  connect();
  disableEAPS();
}

void loop() {

  //
  client.loop();

  if (!client.connected()) {
    connect();
  }

  // publish a message roughly every 2 seconds.
  if (millis() - lastMillis > 2000) {
    lastMillis = millis();
    client.publish("/EAPS", "Connected");
  }

  // led for safety
  bool EAPSOn = false;
  for (int i = 0; i < NoEAPS; i++) {
    if (EAPS[i]) {
      EAPSOn = true;
    }
  }
  if (EAPSOn) {
    digitalWrite(LED_BUILTIN, HIGH);
  } else {
    digitalWrite(LED_BUILTIN, LOW);
  }
  // turn off eaps for safety
  if ( millis() - lastActivation > EAPtimout) {
    disableEAPS();
  }

}





void disableEAPS() {
  // turn all EAPS off
  for (int i = 0; i < NoEAPS; i++) {
    digitalWrite(EAPpins[i], LOW);
    EAPS[i] = false;
    EAP_OLD_VALUE[i] = false;
  }

};

void activeEAPS(boolean commmandBufer[]) {
  lastActivation = millis();
  //Serial.println("activeEAPS");
  for (int i = 0; i < NoEAPS; i++) {
    EAPS[i] = commmandBufer[i];
    // only call digitalWrite if there is a change in value
    if (EAPS[i] !=  EAP_OLD_VALUE[i]) {
      //Serial.println(i + " is " + EAPS[i]);
      digitalWrite(EAPpins[i], EAPS[i]);
      EAP_OLD_VALUE[i] =  EAPS[i];
    }
  }
}
