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
float VEL_FAC = 1;
float MAN_SPEED_FAC = 1.1;
float EDGE_DAMP = 0.9;
float PART_MASS = 5;
float STROKE_FAC = 10;
float INIT_SPEED = 2;

// globals
color[][] data;
Liner root;
int curLay=0;
int maxLay=0;
ArrayList<Liner> liners;



// wrapper around bezier() taking in vectors
void linerBezier(PVector s, PVector vs, PVector e, PVector ve) {
  bezier(s.x, s.y, (s.x+vs.x), (s.y+vs.y), e.x, e.y, (e.x+ve.x), (e.y+ve.y));
}

// average two colours
color avg(color c1, color c2) {
  float r = (red(c1)+red(c2)) / 2;
  float g = (green(c1)+green(c2)) / 2;
  float b = (blue(c1)+blue(c2)) / 2;
  return color(r, g, b);
}

class Liner {
  PVector location;
  PVector velocity;
  PVector acceleration;
  Liner parent;
  ArrayList<Liner> children;
  int layer;
  boolean selected;

  Liner(PVector l, PVector v, Liner p, int ly) {
    parent = p;
    layer = ly;
    location = l.get();
    velocity = v.get();
    acceleration = new PVector(0, 0);
    children = new ArrayList<Liner>();
    selected = false;
    liners.add(this);
  }

  void spawn() {
    PVector l, v;
    Liner c;
    if (layer == 0) {
      l = new PVector(1, 1);
      l.mult(min(width, height)*0.25);
      v = new PVector(1, -1);
      v.mult(INIT_SPEED);
    } 
    else {
      l = PVector.sub(location, parent.location);
      v = velocity.get();
      println("P = " + parent.location.x + "," + parent.location.y );
    }
    l.mult(0.5);
    println("I'm " + location.x + "," + location.y );

    l.add(location);
    v.mult(VEL_FAC);
    c = new Liner(l, v, this, layer + 1);
    children.add(c);
    liners.add(c);
    println(this + " spawned " + c);
    println("at " + l.x + "," + l.y + " and " + v.x + "," +v.y);
  }

  void update() {
    if (parent != null) {
      acceleration = new PVector(parent.location.x - this.location.x, parent.location.y - this.location.y);
      float d = acceleration.mag();
      acceleration.mult(PART_MASS / sq(d));
      PVector newLoc = location.get();
      PVector newVel = velocity.get();
      newVel.add(acceleration);
      newLoc.add(velocity);

      if (newLoc.x >= width || newLoc.x < 0) {
        newLoc.x = location.x;
        newVel.x = -newVel.x * EDGE_DAMP;
      }
      if (newLoc.y >= height || newLoc.y < 0) {
        newLoc.y = location.y;
        newVel.y = -newVel.y * EDGE_DAMP;
      }
      color c1 = data[floor(location.x)][floor(location.y)];
      color c2 = data[floor(newLoc.x)][floor(newLoc.y)];
      strokeWeight(max(STROKE_FAC / (newVel.mag() + 1), 1));
      //    stroke(255);
      //    linerBezier(location, velocity, newLoc, newVel);
      if (selected) {
        stroke(255);
      } 
      else {
        stroke(avg(c1, c2));
      }
      linerBezier(location, velocity, newLoc, newVel);
      location = newLoc;
      velocity = newVel;
    }
    //recursively update children
    Iterator<Liner> it = children.iterator();
    while (it.hasNext ()) {
      Liner l = it.next();
      l.update();
    }
  }
}


void keyPressed() {
  switch(key) {
  case 'a':
  case 'A':
    downLayer();
    break;
  case 'q':
  case 'Q':
    upLayer();
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
    break;
  case 'd':
  case 'D':
    addLiner();
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
  ArrayList<Liner> lin = (ArrayList<Liner>)liners.clone();
  Iterator<Liner> it = lin.iterator();
  while (it.hasNext ()) {
    Liner l = it.next();
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
  ArrayList<Liner> lin = (ArrayList<Liner>)liners.clone();
  Iterator<Liner> it = lin.iterator();
  while (it.hasNext ()) {
    Liner l = it.next();
    if (l.selected) {
      l.spawn();
    }
  }
  maxLay++;
  upLayer();
}

void addLiner() {
  if (curLay == 0) {
    return;
  }
  ArrayList<Liner> lin = (ArrayList<Liner>)liners.clone();
  Iterator<Liner> it = lin.iterator();
  while (it.hasNext ()) {
    Liner l = it.next();
    if (l.layer == curLay - 1) {
      l.spawn();
    }
  }
}

void chSpeed(float fac) {
  Iterator<Liner> it = liners.iterator();
  while (it.hasNext ()) {
    Liner l = it.next();
    if (l.selected) {
      l.velocity.mult(fac);
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
  liners = new ArrayList<Liner>();
  root = new Liner(new PVector(width/2, height/2), new PVector(0, 0), null, 0);
  liners.add(root);
  updSelected();
  println("aaand done");
  //  addLayer();
}


void draw() {
  root.update();
}

