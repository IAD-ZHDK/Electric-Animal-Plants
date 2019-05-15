import ch.bildspur.realsense.*;

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
  void displayZone(ArrayList<buttonZone> zones) {
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
      color displayColor = color(0,255,0);
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
      String displayText = "zone: "+(int(zoneNo))+"\n";
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
      int depth = floor(depthAverage[i]*.80);
      int depthNew= camera.getDepth(xpos,ypos);
      if (depthNew <= 0) {
        depthNew = 0xFFFF;
      }

      //
      depth +=  floor(depthNew*.20);
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
