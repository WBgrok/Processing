// constants
//String url = "http://www.whitegadget.com/attachments/pc-wallpapers/16950d1224057972-landscape-wallpaper-blue-river-scenery-wallpapers.jpg";
//String url = "http://www.ibiblio.org/wm/paint/auth/kandinsky/kandinsky.comp-7.jpg";
//String url = "http://www.deshow.net/d/file/travel/2009-06/germany-landscape-605-2.jpg";
//String url = "http://deathvalleyhikerasso.homestead.com/files/the_desert_landscape.jpg";
//String url = "https://lh5.googleusercontent.com/-DYIX_2aFpCs/UI7m7rSVD3I/AAAAAAAAF-g/RQRaWnWB5G0/s720/Red+by+Hakan+G%C3%BCl.JPG";
String url = "http://www.deshow.net/d/file/travel/2009-02/landscape-photo-manipulation-415-2.jpg";
PImage file = null;
boolean doubleRes = false;
float MOUSE_MASS = 1;
float VEL_FAC = 2.13;
float MAN_SPEED_FAC = 1.1;
float STROKE_FAC = 1;
float INIT_SPEED = 0.01;

// globals
color[][] data;
Spiro root;
int curLay=0;
int maxLay=0;
ArrayList<Spiro> spiros;



// wrapper around bezier() taking in vectors
void spiroBezier(PVector s, PVector vs, PVector e, PVector ve) {
  bezier(s.x, s.y, (s.x+vs.x), (s.y+vs.y), e.x, e.y, (e.x+ve.x), (e.y+ve.y));
}

void arcBezier(cX,cY,d,a_stop,a_start) {
  float sx, sy, ex, ey;
  sx = cX + (d * cos(a_start));
  sy = cY + (d * sin(a_start));
}

// average two colours
color avg(color c1, color c2) {
  float r = (red(c1)+red(c2)) / 2;
  float g = (green(c1)+green(c2)) / 2;
  float b = (blue(c1)+blue(c2)) / 2;
  return color(r, g, b);
}

class Spiro {
  float cX, cY; //center locations
  Spiro parent;
  ArrayList<Spiro> children;
  int layer;
  boolean selected;
  float diameter;
  float angle; // current drawing angle
  float speed;

  Spiro(Spiro p) {
    parent = p;
    cX = parent.cX + (cos(parent.angle) * (parent.diameter / 2));
    cY = parent.cY + (sin(parent.angle) * (parent.diameter / 2));
    layer = p.layer + 1;
    children = new ArrayList<Spiro>();
    angle = parent.angle;
    speed = parent.speed * VEL_FAC;
    diameter =  parent.diameter / 2;
    selected = false;
    spiros.add(this);
  }

  //center spiro
  Spiro() {
    parent = null;
    cX = width / 2;
    cY = height / 2;
    layer = 0;
    children = new ArrayList<Spiro>();
    angle = 0;
    speed = INIT_SPEED;
    diameter = min(width/4, height/4);
    selected = true;
    spiros.add(this);
  }

  void spawn() {
    Spiro c = new Spiro(this);
    children.add(c);
    spiros.add(c);
    println(this + " spawned " + c);
  }

  void draw() {
    if (parent != null) {
      cX = parent.cX + (cos(parent.angle) * (parent.diameter / 2));
      cY = parent.cY + (sin(parent.angle) * (parent.diameter / 2));
    }
    float newAngle = (angle + speed) % TWO_PI;
    //    color c1 = data[floor(cX + (cos(angle) * diameter))][floor(cY + (sin(angle) * diameter)];
    //    color c1 = data[floor(cX + (cos(newAngle) * diameter))][floor(cY + (sin(newAngle) * diameter)];
    //    color c1 = (255,200,100);
    //    color c2 = (100,200,243);
    //strokeWeight(max(STROKE_FAC / speed, 1));
    if (selected) {
      stroke(255);
    } 
    else {
      stroke(layer * (255 / (maxLay +1)));
      //      stroke(avg(c1, c2));
    }
    //ellipse(cX,cY,diameter,diameter);
    arc(cX, cY, diameter, diameter, angle, newAngle);
    angle = newAngle;

    //recursively update children
    Iterator<Spiro> it = children.iterator();
    while (it.hasNext ()) {
      Spiro l = it.next();
      l.draw();
    }
  }
}


void keyPressed() {
  switch(key) {
  case 'a':
  case 'A':
    downLayer();
    println(curLay);
    break;
  case 'q':
  case 'Q':
    upLayer(); 
    println(curLay);
    break;
  case 'w':
  case 'W':
    chSpeed(MAN_SPEED_FAC);
    break;
  case 's':
  case 'S':
    chSpeed(1 / MAN_SPEED_FAC);
    break;
  case 'e':
  case 'E':
    addLayer();
    println("spawning at " + curLay);
    break;
  case 'd':
  case 'D':
    addSpiro();
    break;
  case 'z':
  case 'Z':
    background(0);
    break;
  }
}

void upLayer() {
  if (curLay == maxLay) {
    return;
  }
  curLay++;
  updSelected();
}

void updSelected() {
  ArrayList<Spiro> lin = (ArrayList<Spiro>)spiros.clone();
  Iterator<Spiro> it = lin.iterator();
  while (it.hasNext ()) {
    Spiro l = it.next();
    if (l.layer == curLay) {
      l.selected = true;
    }
    else {
      l.selected = false;
    }
  }
}

void downLayer() {
  if (curLay == 0) {
    return;
  }
  curLay--;
  updSelected();
}

void addLayer() {
  curLay = maxLay;
  updSelected();
  ArrayList<Spiro> lin = (ArrayList<Spiro>)spiros.clone();
  Iterator<Spiro> it = lin.iterator();
  while (it.hasNext ()) {
    Spiro l = it.next();
    if (l.selected) {
      l.spawn();
    }
  }
  maxLay++;
  upLayer();
}

void addSpiro() {
  if (curLay == 0) {
    return;
  }
  ArrayList<Spiro> lin = (ArrayList<Spiro>)spiros.clone();
  Iterator<Spiro> it = lin.iterator();
  while (it.hasNext ()) {
    Spiro l = it.next();
    if (l.layer == curLay - 1) {
      l.spawn();
    }
  }
}

void chSpeed(float fac) {
  Iterator<Spiro> it = spiros.iterator();
  while (it.hasNext ()) {
    Spiro l = it.next();
    if (l.selected) {
      l.speed *= fac;
    }
  }
}



void loadImg(File f) {
  if (f == null) {
    file =loadImage(url, "bmp");
    return;
  }
  file = loadImage(f.getAbsolutePath());
}

void setup() {
  //  selectInput("Select a file to process:", "loadImg");
  //  while (file == null) {
  //  }
  file =loadImage(url, "bmp");
  println("Start set-up");
  PImage img = file;
  if (doubleRes) {
    size(img.width * 2, img.height * 2);
    data = new color[width][height];
    img.loadPixels();
    for (int i = 0; i < img.width; i++) {
      for (int j = 0; j < img.height; j++) {
        data[2*i][2*j] = img.pixels[(j * img.width) +i];
        data[2*i][2*j+1] = img.pixels[(j * img.width) +i];
        data[2*i+1][2*j] = img.pixels[(j * img.width) +i];
        data[2*i+1][2*j+1] = img.pixels[(j * img.width) +i];
      }
    }
  } 
  else {
    size(img.width, img.height);
    data = new color[width][height];
    img.loadPixels();
    for (int i = 0; i < img.width; i++) {
      for (int j = 0; j < img.height; j++) {
        data[i][j] = img.pixels[(j * img.width) +i];
      }
    }
  }
  background(0);
  noFill();
  strokeCap(ROUND);
  spiros = new ArrayList<Spiro>();
  root = new Spiro();
  spiros.add(root);
  updSelected();
  println("aaand done");
  //  addLayer();
}


void draw() {
  root.draw();
}

