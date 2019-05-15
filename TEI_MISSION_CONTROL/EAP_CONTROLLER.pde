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

  void draw() {
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
      strokeWeight(.5);
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
