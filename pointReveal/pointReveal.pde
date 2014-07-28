// constants
//String url = "http://www.whitegadget.com/attachments/pc-wallpapers/16950d1224057972-landscape-wallpaper-blue-river-scenery-wallpapers.jpg";
//String url = "http://www.ibiblio.org/wm/paint/auth/kandinsky/kandinsky.comp-7.jpg";
//String url = "http://www.deshow.net/d/file/travel/2009-06/germany-landscape-605-2.jpg";
//String url = "http://deathvalleyhikerasso.homestead.com/files/the_desert_landscape.jpg";
String url = "https://lh5.googleusercontent.com/-DYIX_2aFpCs/UI7m7rSVD3I/AAAAAAAAF-g/RQRaWnWB5G0/s720/Red+by+Hakan+G%C3%BCl.JPG";
PImage file = null;
boolean doubleRes = false;
float EDGE_DAMP = 0.9;
float PART_MASS = 20;
float STROKE_FAC = 10;
float VISC_FAC = 0.05;
float V = 10; //Random velocity range

//spawn points
PVector s1, s2, s3, s4, s5, s6, s7, s8 ,s9, s10, s11, s12, s13;

int spawn = 0;

// globals
ArrayList<Pointy> pointies;
ArrayList<Pointy> done;
ArrayList<Pointy> doneSwap;
Iterator<Pointy> it;
Iterator<Pointy> dn;
Boolean doingDone;

class Pointy {
  PVector location;
  PVector velocity;
  PVector acceleration;
  PVector home;
  color col;

  Pointy(PVector l, PVector v, PVector h, color c) {
    col = c;
    home = h.get();
    location = l.get();
    velocity = v.get();
    acceleration = new PVector(0, 0);
  }
  boolean update() {
    acceleration = PVector.sub(home, location);
    float d = acceleration.mag();
    acceleration.mult(PART_MASS / sq(d));
    PVector drag = velocity.get();
    drag.mult(-VISC_FAC * velocity.mag());
    acceleration.add(drag); 
    velocity.add(acceleration);
    PVector prevLoc = location.get();
    location.add(velocity);
    if (PVector.sub(location, home).mag() <=1) {
      location = home;
      doneSwap.add(this);
      return true;
    }

    if (location.x >= width || location.x < 0) {
      location.x = prevLoc.x;
      velocity.x = -velocity.x * EDGE_DAMP;
    }
    if (location.y >= height || location.y < 0) {
      location.y = prevLoc.y;
      velocity.y = -velocity.y * EDGE_DAMP;
    }
    stroke(col);
    point(location.x, location.y);
    //    fill(col);
    //ellipse(location.x, location.y, max(STROKE_FAC / (velocity.mag() + 1), 1), max(STROKE_FAC / (velocity.mag() + 1), 1));
    return false;
  }

  void finish() {
    stroke(col);
    point(home.x, home.y);
  }
}

void plop(int i, int j, color c) {
  PVector s = s1;
  switch(spawn++) {
  case 1:
    s=s1;
    break;
  case 2:
    s=s2;
    break;
  case 3:
    s=s3;
    break;
  case 4:
    s=s4;
    break;
    case 5:
    s=s5;
    break;
    case 6:
    s=s6;
    break;
    case 7:
    s=s7;
    break;
    case 8:
    s=s8;
    break;
    case 9:
    s=s9;
    break;
    case 10:
    s=s10;
    break;
    case 11:
    s=s11;
    break;
    case 12:
    s=s12;
    break;
    case 13:
    s=s13;
    spawn=0;
    break;
    
  }

  pointies.add(new Pointy(s, new PVector(random(-V, V), random(-V, V)), new PVector(i, j), c));
 // randomSeed(minute()+second()+millis()+round(random(1000)));
// randomSeed(second()+millis());
// randomSeed(round(random(10000)));
}

void setup() {
  file =loadImage(url, "bmp");
  println("Start set-up");
  PImage img = file;
  img.loadPixels();
  pointies = new ArrayList<Pointy>();
  done = new ArrayList<Pointy>();
  doneSwap = new ArrayList<Pointy>();
  if (doubleRes) {
    size(img.width * 2, img.height * 2);
  } 
  else {
    size(img.width, img.height);
  }
  s1 = new PVector(width/4, height/4);
  s2 = new PVector(3*width/4, height/4);
  s3 = new PVector(width/4, 3*height/4);
  s4 = new PVector(3*width/4, 3*height/4);
  s5 = new PVector(0, 0);
  s6 = new PVector(0, height/2);
  s7 = new PVector(0, height);
  s8 = new PVector(width/2, 0);
  s9 = new PVector(width/2, height/2);
  s10 = new PVector(width/2, height);
  s11 = new PVector(width, 0);
  s12 = new PVector(width, height/2);
  s13 = new PVector(width, height);
  
  if (doubleRes) {
    for (int i = 0; i < img.width; i++) {
      for (int j = 0; j < img.height; j++) {
        plop(2*i, 2*j, img.pixels[(j * img.width) +i]);
        plop(2*i, 2*j+1, img.pixels[(j * img.width) +i]);
        plop(2*i+1, 2*j, img.pixels[(j * img.width) +i]);
        plop(2*i+1, 2*j+1, img.pixels[(j * img.width) +i]);
      }
    }
  } 
  else {
    for (int i = 0; i < img.width; i++) {
      for (int j = 0; j < img.height; j++) {
        plop(i, j, img.pixels[(j * img.width) +i]);
      }
    }
  }
  background(0);
  noStroke();
  it = pointies.iterator();
  dn = done.iterator();
  doingDone = false;
  frameRate(60);
}

void draw() {
  Pointy p, q;

  for (int i = 0; i <60000; i++) {
    if (dn.hasNext()) {
      q = dn.next();
      q.finish();
    } 
    else {
      done = (ArrayList<Pointy>)doneSwap.clone();
      //      println(done.size());
      dn = done.iterator();
    }
    if (it.hasNext()) {
      p = it.next();
      if (p.update()) {
        it.remove();
      }
    } 
    else {
      println(pointies.size());
      it = pointies.iterator();
    }
  }
  saveFrame("/Users/will/Documents/pointReveal/frame-####################.tif");
}

