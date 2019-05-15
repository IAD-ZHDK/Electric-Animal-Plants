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

  void draw() {
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
      SensorLow *=.99;
      SensorLow += Low*.01;
      SensorHigh *=.99;
      SensorHigh += High*.01;
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
