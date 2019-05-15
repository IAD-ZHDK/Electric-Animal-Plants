// global osc functions
void OSCSendSensorValue(String OSCid, int EAPno, int SensorAverage) {
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

 void OSCSend(String OSCid, int EAPno, String State) {
  //OscMessage myMessage = new OscMessage(OSCid);
  OscMessage myMessage = new OscMessage("/EAP");
  myMessage.add(OSCid);
  myMessage.add(EAPno);
  myMessage.add(State);
  oscP5.send(myMessage, myRemoteLocation);
  String message = myMessage.addrPattern()+" "+myMessage.get(0).stringValue()+" "+myMessage.get(1).intValue()+" "+myMessage.get(2).stringValue();
  oscConsol.addText(message);
}
