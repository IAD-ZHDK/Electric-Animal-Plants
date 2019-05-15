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
    this.MaxLines = floor((h-40)/(textS*1.5));
    stringList = new ArrayList<String>();
  }

  void draw() {
    pushStyle();
    textSize(textS);
    fill(50);
    rect(x, y, w, h);
    stroke(0);
    fill(255);
    text(TextBox, x+10, y+10, w-10, h-10);  // Text wraps within text box
    popStyle();
  }

  void addText(String newText) {
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
