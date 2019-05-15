import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import oscP5.*; 
import netP5.*; 
import java.util.*; 
import mqtt.*; 
import ch.bildspur.realsense.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TEI_MISSION_CONTROL extends PApplet {

// OSC



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
public void setup() {
  
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

public void draw() {
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
public final class ActivationZones{
  // this class takes care of creating, storing and retrieving buttonZones
  public ArrayList<buttonZone> zones;
  private Table table;
  private String file = "zoneData.csv";
  private int currentZone = 0;
  public ActivationZones() {
    println("loading table...");
    table = loadTable("data/"+file, "header");
    //
    zones = new ArrayList<buttonZone>();
    if (table == null) {
      // if there is no file yet, create a new one
      println("no zone file found");
      makeFile();
    } else {
      // sort the data
      println("zone file found");
      table.sort("zone");
      int iterator = 0;
      for (TableRow row : table.rows()) {
        int a = PApplet.parseInt(row.getString("centerX"));
        int b = PApplet.parseInt(row.getString("centerY"));
        int c = PApplet.parseInt(row.getString("cornerX"));
        int d = PApplet.parseInt(row.getString("cornerY"));
        int e = PApplet.parseInt(row.getString("upperThreshold"));
        int f = PApplet.parseInt(row.getString("lowThreshold"));
        int g = PApplet.parseInt(row.getString("zone"));
        int h = PApplet.parseInt(row.getString("sensitivity"));
        int i = PApplet.parseInt(row.getString("outPutCH"));
        int j = PApplet.parseInt(row.getString("blocked"));
        zones.add(new buttonZone(a, b, c, d, e, f, g, h,i));
        EAPcontroller.setBlock(iterator,j);
        iterator++;
      }
    }
  }
  private void makeFile() {
    table = new Table();
    table.addColumn("zone");
    table.addColumn("centerX");
    table.addColumn("centerY");
    table.addColumn("cornerX");
    table.addColumn("cornerY");
    table.addColumn("upperThreshold");
    table.addColumn("lowThreshold");
    table.addColumn("outPutCH");
    table.addColumn("blocked");
    saveTable(table, "data/"+file);
    TableRow  newRow = table.getRow(0);
    newRow.setString("zone", "0");
    newRow.setString("centerX", "0");
    newRow.setString("centerY", "0");
    newRow.setString("cornerX", "0");
    newRow.setString("cornerY", "0");
    newRow.setString("upperThreshold", "0");
    newRow.setString("lowThreshold","0");
    newRow.setString("sensitivity", "0");
    newRow.setString("outPutCH", "0");
    newRow.setString("blocked", "0");
    saveTable(table, "data/"+file);
  }
  public void saveData() {
    // save a new entry into the csv file
    int iterator = 0;
    for (TableRow row : table.rows()) {
      row.setString("zone", str(iterator));
      row.setString("centerX", str(zones.get(iterator).centerX));
      row.setString("centerY", str(zones.get(iterator).centerY));
      row.setString("cornerX", str(zones.get(iterator).cornerX));
      row.setString("cornerY", str(zones.get(iterator).cornerY));
      row.setString("upperThreshold", str(zones.get(iterator).getLowThreshold()));
      row.setString("lowThreshold", str(zones.get(iterator).getUpperThreshold()));
      row.setString("sensitivity", str(zones.get(iterator).getSensitivity()));
      row.setString("outPutCH", str(zones.get(iterator).getOutPutCH()));
      int myInt = EAPcontroller.getBlock(iterator) ? 1 : 0;
      row.setString("blocked", str(myInt));
      iterator++;
    }
    saveTable(table, "data/"+file);
    println("saved");
  }

  public buttonZone getCurrentZone() {
    return zones.get(currentZone);
  }

  public void setCurrentZone(int val) {
    if (val>=0 && val<zones.size()) {
      currentZone = val;
    }
  }

  public void setPoint1(int x, int y) {
    x-=RealSenseVisual.x;
    y-=RealSenseVisual.y;
    if(x>1 && y>1 && x<RealSenseVisual.getw()-1  && y<RealSenseVisual.geth()-1) {
      zones.get(currentZone).setPoint1(x, y);
    }
  }

  public void setPoint2(int x, int y) {
      if(x>1 && y>1 && x<RealSenseVisual.getw()-1  && y<RealSenseVisual.geth()-1) {
        x-=RealSenseVisual.x;
        y-=RealSenseVisual.y;
        zones.get(currentZone).setPoint2(x, y);
      }
  }

  public void update() {
    getRectAverage();
  }

  private void getRectAverage() {

    for (buttonZone object : zones) {
      object.setLastAverage(object.getAverage());
      object.update();
      int tlX = object.tlX;
      int trX = object.trX;
      int trY = object.trY;
      int brY = object.brY;
      int centerX = object.centerX;
      float average = 0;
      int count = 0;

      for (int x = tlX; x < trX; x++) {
        for (int y = trY; y < brY; y++) {
          int depthV = depthData[x][y];
          //int depthV = depthData[2][2];
          count++;
          if (depthV>object.getLowThreshold() && depthV<object.getUpperThreshold()) {
            average+= 1;
          } else {
            average+= 0;
          }
        }
      }
      average = (average/count)*100;
      object.setAverage(floor(average));
    }
  }

}
public class SensorVis {
  private int x;
  private int y;
  private int w;
  private int h;
  private ArrayList<Integer> sensor0_y;
  private ArrayList<Integer> sensor1_y;
  //private ArrayList<Integer> sensor2_y;
  //private ArrayList<Integer> sensor3_y;
  private int sampleRate = 4;
  //  private String OSCid = "sensor/0/";
  public String[] OSCid = { "sensor/0/",
  "sensor/1/",
  "sensor/2/",
  "sensor/3/"};
  private boolean VisOn = false;
  private boolean rectOver= true;
  private PVector buttonLoc = new PVector(10,10);
  private int count = 0;
  private int SensorLow = 35000;
  private int SensorHigh = 0;
  SensorVis(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    sensor0_y = new ArrayList<Integer>();
    sensor1_y = new ArrayList<Integer>();

    for (int i = 0; i < floor(w/sampleRate); i++) {
      sensor0_y.add(0);
      sensor1_y.add(0);
    }
  }

  public void draw() {
    pushStyle();
    fill(50);
    rect(x, y, w, h);
    stroke(255);
    rect(x+buttonLoc.y, y+buttonLoc.x, 30, 20);
    rectOver = overRect(x+buttonLoc.y, y+buttonLoc.x, 30, 20);
    fill(255);
    pushMatrix();
    translate(x, y);
    if(VisOn) {
      count++;
      text("OFF",buttonLoc.y+5, buttonLoc.x+13); // button text
      translate(0, 0);
      noFill();
      stroke(255);
      // draw curves
      beginShape();
      float Low = 35000;
      float High = 0;
      for (int i = 0; i < sensor0_y.size(); i++) {
        int value = sensor0_y.get(i);
        if (value<Low) {
          Low = value;
        }
        if (value>High) {
          High =value;
        }
        value = floor(map(value,SensorLow,SensorHigh+1,2,this.h/2));
        vertex(i*sampleRate, value);
      }
        endShape();
      //weighted moving average for scaleing graph
      SensorLow *=.99f;
      SensorLow += Low*.01f;
      SensorHigh *=.99f;
      SensorHigh += High*.01f;
      //


    } else {
      text("ON",buttonLoc.y+5, buttonLoc.x+13); // button text
    }
    popMatrix();
    popStyle();
  }
  private void OSCSend(int sensor, int value) {
    OscMessage myMessage = new OscMessage(OSCid[sensor]);
    myMessage.add(value);
    oscP5.send(myMessage, myRemoteLocation);
    String message = myMessage.addrPattern()+""+value;
    oscConsol.addText(message);
  }

  public void click() {
    println("mouse");
    if (rectOver) {
      VisOn = !VisOn;
    }
  }

  public void updateSensor(int value) {
    //value = floor(map(value,SensorLow,SensorHigh+1,0,this.h));
    sensor0_y.add(value);
    sensor0_y.remove(0);
  };

  private boolean overRect(float _x, float _y, int width, int height)  {
    if (mouseX >= _x && mouseX <= _x+width &&
      mouseY >= _y && mouseY <= _y+height) {
        cursor(HAND);
        return true;
      } else {
        cursor(ARROW);
        return false;
      }
    }
  }
class buttonZone {
  // buttonZones define a BOX in spoace that can be used like a button
  public int centerX;
  public int centerY;
  public int cornerX;
  public int cornerY;
  private int tlX;
  private int trX;
  private int trY;
  private int brY;
  private int w;
  private int h;
  private int halfW;
  private int halfH;
  private int lowThreshold = 500;
  private int upperThreshold = 600;
  private int average = 0;
  private int lastAverage = 0;
  private int counter = 0;
  private int outPutCH = 0;
  private int onCounter = 0;
  public int zone;
  private boolean activeNew = false;
  public boolean active = false;
  private boolean lastActive = true;
  private int spiralCount;
  private int sensitivity = 30;
  private int timer;
  private int StartTimer;

  public buttonZone(int centerX, int centerY, int cornerX, int cornerY, int lowThreshold, int upperThreshold, int zone, int sensitivity,int outPutCH) {
    this.zone = zone;
    this.sensitivity = sensitivity;
    this.centerX =  centerX;
    this.centerY =  centerY;
    this.cornerY =  cornerY;
    this.cornerX =  cornerX;
    this.lowThreshold = lowThreshold;
    this.upperThreshold = upperThreshold;
    this.outPutCH = outPutCH;
    updateData();
  }

  public void update() {
    if (average>sensitivity) {
      activeNew = true;
    } else {
      activeNew = false;
    }
    // debounce
    if (active != activeNew) {
      counter++;
    }

    if (counter > 10) {
      if (active != activeNew) {
        active = activeNew;
        counter = 0;
        if (active && outPutCH<EAPChannels) {
          EAPcontroller.activate(outPutCH,2000);
          StartTimer = millis();
        }
      }
    }
    //time out to allow further activation
    timer = millis() - StartTimer;
    if (timer>4500 &&  active == true) {
      active = false;
    }
  }
  public void setPoint1(int centerX, int centerY) {
    this.centerX =  centerX;
    this.centerY =  centerY;
    updateData();
  }
  public void setPoint2(int cornerX, int cornerY) {
    this.cornerY =  cornerY;
    this.cornerX =  cornerX;
    updateData();
  }
  private void updateData() {
    w = abs(centerX-cornerX)*2;
    h = abs(centerY-cornerY)*2;
    halfW = w/2;
    halfH= h/2;
    tlX = centerX-halfW;
    trX =centerX+halfW;
    trY =centerY-halfH;
    brY =centerY+halfH;

    //  int tlX = object.tlX; // top left x
    //   tlX = (tlX > 0) ? tlX : 0;
    //  int trX = object.trX; // top Right x
    //   trX = (trX< this.w) ? trX : RealSenseVisual.w;
    //int trY = object.trY; // top Right y
    // trY = (trY > 0) ?  trY : 0;
    //  int brY = object.brY; // bottom Right y
    //   brY = (brY< this.h) ? brY : RealSenseVisual.h;

  }

  public int getLowThreshold() {
    return lowThreshold;
  };
  public int getUpperThreshold() {
    return upperThreshold;
  };
  public void setLowThreshold(int val) {
    lowThreshold = val;
  };
  public void setUpperThreshold(int val) {
    upperThreshold = val;
  };
  public int getSensitivity() {
    return sensitivity;
  };
  public void setSensitivity(int val) {
    sensitivity = val;
  };
  public int getAverage() {
    return average;
  };
  public int getLastAverage() {
    return lastAverage;
  };

  public void setAverage(int val) {
    this.average = val;
  };
  public void setLastAverage(int val) {
    this.lastAverage = val;
  };
  public int getOutPutCH() {
    return outPutCH;
  };
  public void setOutPutCH(int val) {
    outPutCH = val;
  };
}
public class EAPControl {
  private int x;
  private  int y;
  private  int w;
  private int h;
  private  float mouseXLoc= 0;
  private  float mouseYLoc = 0;
  // VIS
  private EAP[] EAPS;
  private  PShape loadedSVG; // EAP vis
  //  private  PGraphics EAPOverView; // EAP vis
  private  PImage EAPOverView; // EAP vis
  private  PShape[] nodeArray;
  private  int childcount = EAPChannels;
  private  float originalW;

  public String[] oscIDS = {"B",
  "B",
  "B",
  "B",
  "T",
  "T",
  "T",
  "T"};

  EAPControl(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    // Setup visualiser
    loadedSVG = loadShape("OverViewEAP.svg");
    originalW = loadedSVG.width;
    if (childcount > loadedSVG.getChildCount()) {
      childcount = loadedSVG.getChildCount();
    }
    println(childcount);
    EAPS = new EAP[EAPChannels];
    nodeArray = new PShape[childcount];
    for (int i = 0; i < childcount; i++) {
      nodeArray[i] = loadedSVG.getChild(i);
      nodeArray[i].disableStyle();
      nodeArray[i].scale(w/originalW);
      EAPS[i] = new EAP(nodeArray[i], oscIDS[i], i);
    }
    loadedSVG = loadShape("OverView.svg");
    loadedSVG.scale(w/originalW);
    loadedSVG.disableStyle();
  //  EAPOverView = loadImage("OverView.png");
    //EAPOverView.resize(w, h);
    // buffer the svg, FX2D not supported!
    // EAPOverView = createGraphics(floor(loadedSVG.width), floor(loadedSVG.height));
    // EAPOverView.beginDraw();
    // EAPOverView.noFill();
    // EAPOverView.stroke(255);
    // EAPOverView.strokeWeight(.5);
    // EAPOverView.shape(loadedSVG, 0, 0);
    // EAPOverView.endDraw();
  }

  public void draw() {
    fill(50);
    rect(x, y, w, h);
    pushMatrix();
    pushStyle();
    translate(x, y);
    //scale((w)/loadedSVG.width);
    //  image(EAPOverView, 0, 0);
    for (int i = 0; i < getChildcount(); i++) {
      EAPS[i].draw();
      fill(255);
      pushMatrix();
      scale(w/originalW);
      //  text(EAPS[i].OSCid+"_"+EAPS[i].IDNo%4, EAPS[i].pos.x,EAPS[i].pos.y);
      popMatrix();
    }
      noFill();
      stroke(255);
      strokeWeight(.5f);
      shape(loadedSVG, 0, 0);
    //image(EAPOverView,0,0,w,h);
    // fill(255,0,0);

    popStyle();

    popMatrix();
  }

  public void activate(int channel, int duration) {
    if (channel<EAPS.length) {
      EAPS[channel].activate(duration);
    }
  }

  public void block(int channel) {
    if (channel<EAPS.length) {
      EAPS[channel].togle(true);
    }
  }
  public boolean getBlock(int channel) {
    if (channel<EAPS.length) {
      return EAPS[channel].getBlock();
    }
    return false;
  }
  public void setBlock(int channel, int blocked) {
    EAPS[channel].togle((blocked != 0));
  }
  public void blockAll() {
    for (int i = 0; i < childcount; i++) {
      EAPS[i].togle(true);
    }
  }
  public final int getChildcount() {
    return this.childcount;
  }

  public void click() {
    println("mouse");
    mouseYLoc = mouseY-y;
    mouseXLoc = mouseX-x;
    for (int i = 0; i < childcount; i++) {
      float d = dist(EAPS[i].pos.x*(w/originalW), EAPS[i].pos.y*(w/originalW),mouseXLoc, mouseYLoc);
      if (d<20) {
        if (mouseButton == LEFT) {
          EAPS[i].activate(1500);
        } else {
          EAPS[i].togle();
        }
      }
    }
  }
}
public enum EAPState {
  UP,
  DOWN,
  OFF
}
// EAP with state
public class EAP {
  PShape svg;
  private EAPState _state = EAPState.OFF;
  private int timer;
  private int StartTimer;
  private int upDuration = 0;
  private int downDuration = 400; // time it takes for EAP to discharge
  private int fillIntensity; //
  private int IDNo;
  private int EAPno;
  private boolean block = false;
  private String OSCid = "_";
  public PVector pos;

  public EAP(PShape svg, String ID, int IDNo) {
    this.IDNo = IDNo;
    this.OSCid = ID;
    this.svg = svg;
    this.EAPno = IDNo%4;
    pos = centoroid(svg);
    _state = EAPState.OFF;
  }

  private void draw() {
    switch(_state) {
      case UP:
      UP();
      break;
      case DOWN:
      DOWN();
      break;
      case OFF:
      OFF();
      break;
    }

  }

  public void activate(int duration) {
    upDuration = duration;
    StartTimer = millis();
    _state = EAPState.UP;
    onPin[IDNo] = true;
    //sendByteSingle();
    if (!block) {
      MQTTHandler.publish();
    }
    OSCSend(OSCid, EAPno, _state.toString());
  }

  public void togle(boolean setValue) {
    block = setValue;
    if (block) {
      onPin[IDNo] = false;
    }
  }

  public void togle() {
    block = !block;
    if (block) {
      onPin[IDNo] = false;
    }
  }

  public boolean getBlock() {
    return block;
  }

  private void OFF() {
    // NOTHING
    if (block) {
      fill(250, 255, 0);
      noStroke();
      shape(svg, 0, 0);
    }
  }

  private void UP() {
    timer = millis() - StartTimer;
    fillIntensity = 255;
    fill(250, 114, 104, fillIntensity);
    noStroke();
    shape(svg, 0, 0);
    // textSize(30);
    // text(OSCid, 100,100);
    if (timer>upDuration) {
      StartTimer = millis();
      _state = EAPState.DOWN;
      // The EAP gets no power at this stage
      onPin[IDNo] = false;
      if (!block) {
        MQTTHandler.publish();
      }
      OSCSend(OSCid, EAPno, _state.toString());
    }
  }

  private void DOWN() {
    timer = millis() - StartTimer;
    fillIntensity = floor(map(timer, 0, downDuration, 255, 0));
    fill(250, 114, 104, fillIntensity);
    noStroke();
    shape(svg, 0, 0);
    if (timer>downDuration) {
      _state = EAPState.OFF;
      OSCSend(OSCid, EAPno, _state.toString());
    }
  }


  //
  private PVector centoroid(PShape object) {
    //point();
    int childVertexCount = object.getVertexCount();
    int sumX = 0;
    int sumY = 0;
    int averageX;
    int averageY;
    for (int i = 0; i < childVertexCount; i++) {
      PVector tempVertice = object.getVertex(i);
      sumX += tempVertice.x;
      sumY += tempVertice.y;
    }
    averageX = sumX/childVertexCount;
    averageY = sumY/childVertexCount;
    PVector v = new PVector(averageX, averageY);
    return v;
  }
}
// Adjust the threshold with key presses
boolean safety = false;
public void keyReleased() {
  safety = false;
}
public void keyPressed() {
  buttonZone zone = myZones.getCurrentZone();
  zone.getUpperThreshold();
  zone.getLowThreshold();
  //zones.get(2)
  int a = zone.getUpperThreshold();
  int b = zone.getLowThreshold();
  int c = zone.getSensitivity();
  int d = zone.getOutPutCH();
  //toggle display
  if (keyCode == ' ') {
    lock = !lock;
    println("lock:"+lock);
  }
  if (key == 'x' || key == 'X') {
    safety = true;
    println("safety"+safety);
  }

  if (key == 'g' || key == 'G') {
    println("all EAPS off");
    EAPcontroller.blockAll();
  }

  if (key == 'r' || key == 'R') {
    randomActivation = !randomActivation;
  }


  if (!lock) {
    if (keyCode == UP) {
      a +=20;
      zone.setUpperThreshold(a);
    } else if (keyCode == DOWN) {
      a -=20;
      zone.setUpperThreshold(a);
    }
    if (keyCode == LEFT) {
      b +=20;
      zone.setLowThreshold(b);
    } else if (keyCode == RIGHT) {
      b -=20;
      zone.setLowThreshold(b);
    }
    if (key == 'a' || key == 'A') {
      c +=5;
      zone.setSensitivity(c);
    } else if (key == 'Q' || key == 'q') {
      c -=5;
      zone.setSensitivity(c);
    }
    if (key == 'w' || key == 'W') {
      d ++;
      d = d%(EAPChannels+1);
      zone.setOutPutCH(d);
    }
    int val = PApplet.parseInt(key)-48;
    if (val >=0 && val <=8) {
      myZones.setCurrentZone(val);
    }
    myZones.saveData();
  }
}
public void mousePressed() {
  if (!lock) {
    sensorVisual.click();
    EAPcontroller.click();
    if (safety) {
      myZones.setPoint1(mouseX, mouseY);
      myZones.setPoint2(mouseX, mouseY);
    }
  }
}
public void mouseDragged() {
  if (!lock && safety) {
    // need to bound to within image
    myZones.setPoint2(mouseX, mouseY);
  }
}

public void mouseReleased() {
  if (!lock) {
    myZones.saveData();
  }
}

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
public void messageReceived(String topic, byte[] payload) {
  String input = new String(payload);
  sensorVisual.updateSensor(Integer.parseInt(input));
  //println("new message: " + topic + " - " + new String(payload));
}
// global osc functions
public void OSCSendSensorValue(String OSCid, int EAPno, int SensorAverage) {
  //OscMessage myMessage = new OscMessage(OSCid);
//  myMessage.add(_state.toString());
//  oscP5.send(myMessage, myRemoteLocation);
  //OscMessage myMessage = new OscMessage(OSCid);
  OscMessage myMessage = new OscMessage("/RealSENSE");
  myMessage.add(OSCid);
  myMessage.add(EAPno);
  myMessage.add(SensorAverage);
  oscP5.send(myMessage, myRemoteLocation);
  String message = myMessage.addrPattern()+" "+myMessage.get(0).stringValue()+" "+myMessage.get(1).intValue()+" "+myMessage.get(2).intValue();
  oscConsol.addText(message);
}

 public void OSCSend(String OSCid, int EAPno, String State) {
  //OscMessage myMessage = new OscMessage(OSCid);
  OscMessage myMessage = new OscMessage("/EAP");
  myMessage.add(OSCid);
  myMessage.add(EAPno);
  myMessage.add(State);
  oscP5.send(myMessage, myRemoteLocation);
  String message = myMessage.addrPattern()+" "+myMessage.get(0).stringValue()+" "+myMessage.get(1).intValue()+" "+myMessage.get(2).stringValue();
  oscConsol.addText(message);
}


public final class RealSenseCameraVis{
  long[] depthAverage;
  private RealSenseCamera camera;
  private int depthLevelLow = 0;
  private int depthLevelHigh = 10000;
  private final PImage depthImage;
  private int x;
  private  int y;
  private  int w;
  private int h;
  private boolean LastactivityCheck = false; // used for checking if there has been changes in the system

  public RealSenseCameraVis(int x, int y, int w, int h, PApplet mainSketch) {
    camera = new RealSenseCamera(mainSketch);
    this.x = x;
    this.y = y;
    this.w = 640;
    this.h = 360;
    depthImage = new PImage(this.w, this.h, PConstants.RGB);
    depthAverage = new long[this.w*this.h];
    for (int i = 0; i < this.w*this.h; i++)
    {
      depthAverage[i] = 2;
    }
    if (camera.isCameraAvailable())
    {
      camera.start(640,360, 60, true, false);
    }
  }
  public final int getw() {
    return this.w;
  }

  public final int geth() {
    return this.h;
  }
  public void draw(ArrayList<buttonZone> zones) {
    pushMatrix();
    translate(x, y);
    if (camera.isCameraAvailable()) {
      // read streams
      camera.readFrames();
      //camera.createDepthImage(depthLevelLow, depthLevelHigh);
      readDepthImage();
      // show depth stream
      image(depthImage,0, 0);
      // show color stream
      //image(camera.getColorImage(), 0, 0);
      displayZone(zones);
      // show fps
    } else {
      fill(50);
      rect(0, 0, w, h);
      fill(255);
      // todo: need to fix overlaps
      text("No Camera Feed", 10, 10);  // Text wraps within text box
        displayZone(zones);
    }
    popMatrix();
  }
  public void displayZone(ArrayList<buttonZone> zones) {
    boolean activityCheck = false;
    for (buttonZone object : zones) {
      boolean secondKinect = false;
      int tlX = object.tlX; // top left x
      //int tlX = (object.tlX > 0) ? object.tlX : 0;
      int trX = object.trX; // top Right x
      //int trX = (object.trX < this.w) ? object.trX : this.w;
      int trY = object.trY; // top Right y
      //int trY = (object.trY > 0) ? object.trY : 0;
      int brY = object.brY; // bottom Right y
      //  int brY = (object.brY < this.h) ? object.brY : this.h;
      int zoneNo = object.zone;
      int centerX = object.centerX;
      int centerY = object.centerY;
      int lowThreshold = object.getLowThreshold();
      int upperThreshold = object.getUpperThreshold();
      int average = object.getAverage();
      int lastAverage = object.getLastAverage();
      int sensitivity = object.getSensitivity();
      int outPutCH = object.getOutPutCH();
      String outputText = "";
      if (outPutCH<EAPChannels) {
        outputText = EAPcontroller.oscIDS[outPutCH]+outPutCH%4;
      } else {
        outputText = "off";
      }
      boolean active = object.active;
      float _w = object.w;
      float _h = object.h;
      int displayColor = color(0,255,0);
      if (active) {
        displayColor  = color(255,0,0);
        activityCheck = true;
        if(lastAverage!= average && outPutCH<EAPChannels) {
          OSCSendSensorValue(EAPcontroller.oscIDS[outPutCH], outPutCH%4, average);
        }
      }
      PImage tempImage = createImage(trX-tlX, brY-trY, ARGB);
      tempImage.loadPixels();
      for (int x = 0; x < tempImage.width; x++) {
        for (int y = 0; y < tempImage.height; y++) {
          int pix = y*tempImage.width + x;
          int depthV = depthData[x+tlX][y+trY];
          //  int depthV = depthData[20][20];
          if (depthV>object.getLowThreshold() && depthV<object.getUpperThreshold()) {
            tempImage.pixels[pix] = displayColor;
          } else {
            tempImage.pixels[pix] = color(0, 0, 0,0);
          }
        }
      }
      tempImage.updatePixels();
      image(tempImage, tlX, trY);
      pushStyle();
      noFill();
      stroke(displayColor);
      beginShape();
      rectMode(CENTER);
      rect(centerX, centerY, _w, _h);
      //   float average = DataSource.getRectAverage(10, 100, 30, 50);
      fill(displayColor);
      String displayText = "zone: "+(PApplet.parseInt(zoneNo))+"\n";
      displayText += average+"\n";
      displayText += "low: "+lowThreshold+"\n";
      displayText += "high: "+upperThreshold+"\n";
      displayText += "sens: "+sensitivity+"\n";
      displayText += "CH: "+outputText+"\n";
      textSize(9);
      textLeading(8);

      int displayHeight = h+15;
      if (zoneNo>3) {
        displayHeight = - 50;
      }
      line(trX,trY, trX,displayHeight);
    //  line(trX,trY, trX,displayHeight);
      fill(0);
    //  line()
      // alternate between top and bottom of GUI

      text(displayText, (trX)+4, displayHeight);
      endShape();
      popStyle();
    }

    if(!activityCheck && LastactivityCheck != activityCheck) {
      //  no Activity so inform Andres
      OSCSendSensorValue("noActivity", 5, 0);
    }
    LastactivityCheck = activityCheck;
  }

  private void readDepthImage() {
    depthImage.loadPixels();
    for (int i = 0; i < w * h; i++)
    {
      //   int loc = x + y * width;
      int xpos = i%w;
      int ypos = floor(i/w);
      //weighted moving point average
      int depth = floor(depthAverage[i]*.80f);
      int depthNew= camera.getDepth(xpos,ypos);
      if (depthNew <= 0) {
        depthNew = 0xFFFF;
      }

      //
      depth +=  floor(depthNew*.20f);
      depthAverage[i] = depth;
      //
      depthData[xpos][ypos] = depth;
      int grayScale = this.clamp((int)map(depth, depthLevelLow, depthLevelHigh, 255, 0), 0, 255);
      if (depth > 0) {
        depthImage.pixels[i] = color(grayScale);
      } else {
        depthImage.pixels[i] = color(0);
        //depthImage.pixels[i] = int(random(0,0xFFFFFF));
      }
    }
    depthImage.updatePixels();
  }

  private final int clamp(int receiver, int min, int max) {
    return Math.max(Math.min(max, receiver), min);
  }

}
public class Console {
  private int x;
  private  int y;
  private  int w;
  private int h;
  private int MaxLines;
  private String TextBox = "";
  private  ArrayList<String> stringList;
  private  int textS = 10;
  Console(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.MaxLines = floor((h-40)/(textS*1.5f));
    stringList = new ArrayList<String>();
  }

  public void draw() {
    pushStyle();
    textSize(textS);
    fill(50);
    rect(x, y, w, h);
    stroke(0);
    fill(255);
    text(TextBox, x+10, y+10, w-10, h-10);  // Text wraps within text box
    popStyle();
  }

  public void addText(String newText) {
    stringList.add(newText);
    TextBox = "";
    for (int i = 0; i < stringList.size(); i++) {
      TextBox = TextBox+stringList.get(i)+"\n";
    }
    if (stringList.size()>MaxLines) {
      stringList.remove(0);
    }
  }
}
  public void settings() {  size(1000, 850, FX2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#000000", "--hide-stop", "TEI_MISSION_CONTROL" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
