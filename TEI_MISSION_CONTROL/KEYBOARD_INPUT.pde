// Adjust the threshold with key presses
boolean safety = false;
void keyReleased() {
  safety = false;
}
void keyPressed() {
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
    int val = int(key)-48;
    if (val >=0 && val <=8) {
      myZones.setCurrentZone(val);
    }
    myZones.saveData();
  }
}
void mousePressed() {
  if (!lock) {
    sensorVisual.click();
    EAPcontroller.click();
    if (safety) {
      myZones.setPoint1(mouseX, mouseY);
      myZones.setPoint2(mouseX, mouseY);
    }
  }
}
void mouseDragged() {
  if (!lock && safety) {
    // need to bound to within image
    myZones.setPoint2(mouseX, mouseY);
  }
}

void mouseReleased() {
  if (!lock) {
    myZones.saveData();
  }
}
