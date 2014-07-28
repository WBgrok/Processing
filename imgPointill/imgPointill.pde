// constants
//String url = "http://www.deshow.net/d/file/travel/2009-02/landscape-photo-manipulation-415-2.jpg";
//String url = "http://www.deshow.net/d/file/travel/2009-06/germany-landscape-605-2.jpg";
String url = "http://2.bp.blogspot.com/-dcgYv-SFEu0/TZVuveCkq1I/AAAAAAAAI2A/vK0PXUJ8aKw/s1600/tiger_wallpapers_hd_Bengal_Tiger_hd_wallpaper.jpg";

int batch = 15;


//globals
float sze = 35; // size of point
color[][] data; //image pixel data
ImgPoint curPoint;

void setup() {
  PImage img;
  img = loadImage(url, "bmp");
  //  size(1024, 602);
  size(img.width, img.height);
  data = new color[width][height];


  img.loadPixels();
  for (int i = 0; i < img.width; i++) {
    for (int j = 0; j < img.height; j++) {
      data[i][j] = img.pixels[(j * img.width) +i];
    }
  }
  background(255);
}

void draw() {
  for (int i =0; i < batch; i++) {
    curPoint = new ImgPoint(floor(random(width-sze)), floor(random(height-sze)));
    curPoint.draw();
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

