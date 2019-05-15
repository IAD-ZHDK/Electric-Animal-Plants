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

  void update() {
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

  int getLowThreshold() {
    return lowThreshold;
  };
  int getUpperThreshold() {
    return upperThreshold;
  };
  void setLowThreshold(int val) {
    lowThreshold = val;
  };
  void setUpperThreshold(int val) {
    upperThreshold = val;
  };
  int getSensitivity() {
    return sensitivity;
  };
  void setSensitivity(int val) {
    sensitivity = val;
  };
  int getAverage() {
    return average;
  };
  int getLastAverage() {
    return lastAverage;
  };

  void setAverage(int val) {
    this.average = val;
  };
  void setLastAverage(int val) {
    this.lastAverage = val;
  };
  int getOutPutCH() {
    return outPutCH;
  };
  void setOutPutCH(int val) {
    outPutCH = val;
  };
}
