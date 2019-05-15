import mqtt.*;
public class MQTT_HANDLER {

  MQTTClient client;

  MQTT_HANDLER(PApplet mainSketch) {
    client = new MQTTClient(mainSketch);
    client.connect("mqtt://0.0.0.0:1883", "MISSION_CONTROL");
    client.subscribe("/SENS");
  }

  public void publish() {
    //client.publish("/INstpoMad", ""+chanel+"");
    String Command = "";

    for (int i = 0; i < onPin.length; i++) {
      if (onPin[i]) {
        Command = Command+"1";
      }else {
        Command = Command+"0";
      }

    }
    client.publish("/EAPS_IAD", Command);
  }

  public void clientConnected() {
    println("client connected");
    // subscribe to topics

  }

  //  private void messageReceived(String topic, byte[] payload) {


  public void connectionLost() {
    println("connection lost");
  }
}
// global events
void messageReceived(String topic, byte[] payload) {
  String input = new String(payload);
  sensorVisual.updateSensor(Integer.parseInt(input));
  //println("new message: " + topic + " - " + new String(payload));
}
