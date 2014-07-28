// Constants
float ATTR_SIZE = 4;
int WIDTH = 960;
int HEIGHT = 960;
color ATTR_COL = 255;
float ANG_INCR = 0.01;
float VEL_FAC = 1.05;
float MASS_INCR = 1.1;
float EDGE_INCR = 1.1;
float EDGE_DAMP = 0.90;
int CYCLE = 10;
float ACC_MAX = 4;


// globals
float edgePush = 0;
float massPush = 320;
Liner liner;
ArrayList<PVector> attractors;
float ang = 1; // in radian
float vInit = 2.4; // in pix/frame
ArrayList<Liner> liners;
int count = 0;
boolean auto = false;


// wrapper around bezier() taking in vectors
void linerBezier(PVector s, PVector vs, PVector e, PVector ve) {
  bezier(s.x, s.y, (s.x+vs.x), (s.y+vs.y), e.x, e.y, (e.x+ve.x), (e.y+ve.y));
}

// work out force from edge
PVector edgeForce(PVector l) {
  //  float side = 0;
  float side = ((edgePush / sq(l.x + 1)) - (edgePush / sq(width - l.x)));
  //  float vert = 0;
  float vert = ((edgePush / sq(l.y + 1)) - (edgePush / sq(height - l.y)));
  return new PVector(side, vert);
}

// work out force from particle
PVector massForce(PVector l, PVector p) {
  PVector dst = PVector.sub(p, l);
  float d = max(dst.mag(), ATTR_SIZE);
  dst.normalize();
  dst.mult(massPush / sq(d));
  return dst;
}

void drawAttr() {
  noStroke();
  for (int i = 0; i < attractors.size(); i ++) {
    PVector v = attractors.get(i);
    fill(liner.col);
    ellipse(v.x, v.y, ATTR_SIZE, ATTR_SIZE);
  }
  noFill();
}

void setup() {
  ellipseMode(RADIUS);
  size(WIDTH, HEIGHT);
  colorMode(HSB, TWO_PI, 1, 1);
  attractors = new ArrayList<PVector>();
  //  attractors.add(new PVector(WIDTH/4, HEIGHT/4));
  //  attractors.add(new PVector(3*WIDTH/4, HEIGHT/4));
  //  attractors.add(new PVector(WIDTH/4, 3*HEIGHT/4));
  //  attractors.add(new PVector(3*WIDTH/4, 3*HEIGHT/4));
  attractors.add(new PVector(WIDTH/2, HEIGHT/4));
  attractors.add(new PVector(WIDTH/4, HEIGHT/2));
  attractors.add(new PVector(WIDTH/2, 3*HEIGHT/4));
  attractors.add(new PVector(3*WIDTH/4, HEIGHT/2));
  background(0);
  liners = new ArrayList<Liner>();
  megaSplode();
}

void spawnLiner() {
  liner = new Liner(new PVector(width/2, height/2), new PVector(vInit * cos(ang), vInit * sin(ang)), color(ang, 1, 1));
  liners.add(liner);
  println (liners.size() + " liners");
}

// spawn ALL THE LINERS
void megaSplode() {
  liners = new ArrayList<Liner>();
  auto = false;
  ang = 0;
  for (int i = 0; i < (TWO_PI / ANG_INCR); i ++) {
    liner = new Liner(new PVector(width/2, height/2), new PVector(vInit * cos(ang), vInit * sin(ang)), color(ang, 1, 1));
    liners.add(liner);
    ang += ANG_INCR;
  }
  ang =0;
}

void mousePressed() {
  if (mouseButton == LEFT) {
    attractors.add(new PVector(mouseX, mouseY));
    println ("Attractor at" + mouseX + "," + mouseY);
  } 
  else {
    if (attractors.size() > 0) {
      PVector v = attractors.get(attractors.size()-1);
      noStroke();
      fill(0);
      ellipse(v.x, v.y, ATTR_SIZE, ATTR_SIZE);
      attractors.remove(attractors.size()-1);
      noFill();
    }
  }
}

void keyPressed() {
  switch(key) {
  case 'q':
  case 'Q':
    chMass(MASS_INCR);
    break;
  case 'a':
  case 'A':
    chMass(1 / MASS_INCR);
    break;
  case 'w':
  case 'W':
    chEdge(EDGE_INCR);
    break;
  case 's':
  case 'S':
    chEdge(1 / EDGE_INCR);
    break;
  case 'e':
  case 'E':
    chAng(ANG_INCR);
    break;
  case 'd':
  case 'D':
    chAng(-ANG_INCR);
    break;
  case 'r':
  case 'R':
    chVel(VEL_FAC);
    break;
  case 'f':
  case 'F':
    chVel(1 / VEL_FAC);
    break;
  case 'z':
  case 'Z':
    background(0);
    liners = new ArrayList<Liner>();
    spawnLiner();
    break;
  case 'x':
  case 'X':
    saveFrame("ScreenCap-######.png");
    break;
  case 'c':
  case 'C':
    if (auto) {
      auto = false;
    } 
    else {
      auto = true;
    }
    break;
  case 'v':
  case 'V':
    megaSplode();
    break;
  }
  //  println(liners);
}

void chAng(float a) {
  ang = (ang + a) % TWO_PI;
  println("ang = " + ang);
  spawnLiner();
}

void chVel(float f) {
  vInit *= f;
  println("vInit = " + vInit);
  spawnLiner();
}

void chMass(float f) {
  massPush *= f;
  println("massPush = " + massPush);
  spawnLiner();
}

void chEdge(float f) {
  edgePush *= f;
  println("edgePush = " + edgePush);
  spawnLiner();
}

void draw() {
  for (int j = liners.size()-1; j >= 0; j--) {
    Liner l = liners.get(j);
    l.push(edgeForce(l.location));
    for (int i = 0; i < attractors.size(); i ++) {
      PVector v = attractors.get(i);
      l.push(massForce(l.location, v));
    }
    if (l.update()) {
      liners.remove(j);
    }
  }
  drawAttr();
  if (auto && count++ == CYCLE) {
    count = 0;
    chAng(ANG_INCR);
  }
} 

