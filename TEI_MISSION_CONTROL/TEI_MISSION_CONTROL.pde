// OSC
import oscP5.*;
import netP5.*;
import java.util.*;
// Communication
int portNoOut = 57120; // old 45678
int portNoIn = 45678; // old 45678
//String AndresSoundAdress = "192.168.1.64"; // with bridge router
//String AndresSoundAdress = "10.0.1.4"; // with mac
String AndresSoundAdress = "127.0.0.1"; // with localhost
//String IPAdress = "127.0.0.1"; // localhost
OscP5 oscP5;
NetAddress myRemoteLocation;
final int EAPChannels = 8;
MQTT_HANDLER MQTTHandler;
// terminal
Console oscConsol;
SensorVis sensorVisual;
EAPControl EAPcontroller;
RealSenseCameraVis RealSenseVisual;
boolean display = true;
boolean randomActivation = false;
boolean lock = true;
int[][] depthData;
// settings
ActivationZones myZones; // settings for virtual buttons in 3D space
//settings for wifi
byte printByte;
ArrayList<Byte> sendByteBuffer= new ArrayList<Byte>();
boolean [] onPin = new boolean[8];
boolean LockIcon = false; // for small animation
// the order on the arduino side
void setup() {
  size(1000, 850, FX2D);
  oscP5 = new OscP5(this, portNoIn);
  MQTTHandler = new MQTT_HANDLER(this);
  //myRemoteLocation = new NetAddress("127.0.0.1", portNoOut);
  myRemoteLocation = new NetAddress(AndresSoundAdress, portNoOut);
  int border = 20;
  RealSenseVisual = new RealSenseCameraVis(10, 60, 0,0,this);
  EAPcontroller  = new EAPControl(10, 490+border, 320, 320);
  oscConsol = new Console(320+border, 490+border, 320, 320);
  sensorVisual = new SensorVis(320+border+320+10, 490+border, 320, 320);
  myZones = new ActivationZones();
  depthData = new int[RealSenseVisual.getw()][RealSenseVisual.geth()];
}

void draw() {
  myZones.update();
  background(200);
  fill(0);
  text("Active EAPS", 10, 490+13);
  EAPcontroller.draw();
  text("OSC output", 320+20, 490+13);
  oscConsol.draw();
  text("Sensor 0", 320+20+320+10, 490+13);
  sensorVisual.draw();
  //text("RealSense", 10, 10);
  RealSenseVisual.draw(myZones.zones);
  surface.setTitle("FPS: " + floor(frameRate));

  //
  if (randomActivation) {
    for (int i = 0; i < EAPcontroller.getChildcount(); i++) {
      if (floor(random(2000)) == 1) {
        EAPcontroller.activate(i,2000);
      };
    }
  }
  if (lock) {
    //fill(255,0,0, 100);
    //rect(0,0,width,height);
    fill(255,255,255,255);
    if(frameCount%10 == 0) {
      LockIcon = !LockIcon;
    };
  } else {
    LockIcon = true;
  }
  pushMatrix();
  translate(320+20+320+10, 70);

  if (LockIcon) {
    text(" Screen lock: "+lock+" ( SPACE )", 0, 0);
  }
  text(" Adjust Sensitivity: ( Q - A ) \n Adjust Zone Start: ( UP - DOWN )\n Adjust Zone End: ( LEFT - RIGHT ) \n Set Zone Output ( W ) \n Set Zone Pos ( hold X and mouse click) \n Block all EAPS ( g ) \n Ranom Activation of EAPS: "+randomActivation+" ( r ) \n", 0, 15);
  popMatrix();
  // instuction text:
}
