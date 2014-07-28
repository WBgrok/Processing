import processing.video.*; 
//import codeanticode.gsvideo.*; 
Capture myCapture;
String[] cameras = Capture.list();

// constants
//String url = "http://www.deshow.net/d/file/travel/2009-02/landscape-photo-manipulation-415-2.jpg";
String url = "http://www.deshow.net/d/file/travel/2009-06/germany-landscape-605-2.jpg";
int batch = 15;
boolean refFrame = true;


//globals
float sze = 35; // size of point
color[][] data; //image pixel data
ImgPoint curPoint;

void setup() {
  if (cameras.length == 0) {
    println("There are no cameras available for capture.");
    exit();
  } 
  else {
    println("Available cameras:");
    for (int i = 0; i < cameras.length; i++) {
      println(cameras[i]);
    }

    // The camera can be initialized directly using an 
    // element from the array returned by list():
    myCapture = new Capture(this, cameras[2]);
    myCapture.start();     

    size(1280, 720);
    //    size(img.width, img.height);
    data = new color[width][height];


    background(255);
  }
}

void draw() {
  for (int i =0; i < batch; i++) {
    curPoint = new ImgPoint(floor(random(width-sze)), floor(random(height-sze)));
    curPoint.draw();
  }
  if (myCapture.available() && refFrame) {
    myCapture.read();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        data[i][j] = myCapture.pixels[(j * myCapture.width) +i];
      }
    }
  }
}

void mousePressed() {
  if (mouseButton == LEFT) {
    sze++;
  } 
  else {
    sze--;
  }
  println(sze);
}

void keyPressed() {
  if (refFrame) {
    refFrame = false;
  } else {
    refFrame = true;
  }
  println(refFrame);
}


class ImgPoint {

  // coord of top left corner, and size
  int xPos;
  int yPos;
  int wdth;
  int hght;

  color col;
  ;

  ImgPoint(int xp, int yp) {
    xPos = xp;
    yPos = yp;

    float r = 0;
    float g = 0;
    float b = 0;

    for (int i = xPos; i < xPos + sze; i++) {
      for (int j = yPos; j < yPos + sze; j++) {

        r += red(data[i][j]);
        g += green(data[i][j]);
        b += blue(data[i][j]);
      }
    }

    r = round(r / sq(sze));
    g = round(g / sq(sze));
    b = round(b / sq(sze));
    col = color(r, g, b);
    this.draw();
  }

  void draw() {
    fill(col);
    noStroke();
    ellipse((xPos + sze/2), (yPos + sze/2), sze, sze);
  }
}

