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
