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
        int a = int(row.getString("centerX"));
        int b = int(row.getString("centerY"));
        int c = int(row.getString("cornerX"));
        int d = int(row.getString("cornerY"));
        int e = int(row.getString("upperThreshold"));
        int f = int(row.getString("lowThreshold"));
        int g = int(row.getString("zone"));
        int h = int(row.getString("sensitivity"));
        int i = int(row.getString("outPutCH"));
        int j = int(row.getString("blocked"));
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
