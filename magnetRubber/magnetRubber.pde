import java.util.Iterator;

// constants
int WIDTH = 1280;
int HEIGHT = 960;
color STROKE = 0;
color FILL = 175;
float EDGE_DAMP = 0.90; // Dampening on edge bounce
int MAX_PART = 500;
float SIZE = 5;
float INIT_SPEED_FACTOR = 1;
float MASS_INCR = 1.1;
float EDGE_INCR = 1.1;
float FRIC_INCR = 1.05;
float MAP_FAC = 0.5;
float MAX_DIST = sqrt(sq(WIDTH)+sq(HEIGHT));
float ELAST_FAC = 0.001;

// globals
ArrayList<Particle> parts;
Particle newPart;
PVector mOrigPos;
boolean autoGen = false;
float mapMax = 10000;
boolean running = true;
float[][] field;
float maxField = 0;
float edgePush = 100;
float massPush = 150;
float fricFac = 0.005;
float edge = (SIZE+1) * sqrt(2);
boolean connect = true;
boolean align = false;
boolean aMode = true;


void setup() {
  size(WIDTH, HEIGHT);
  colorMode(HSB, 1);
  background(0.2);
  smooth();
  noStroke();
  parts = new ArrayList<Particle>();
  field = new float[width][height];
  for (int i = 0; i <width; i++) {
    for (int j = 0; j < height; j++) {
      field[i][j] = 0;
    }
  }
  //  spawnPart();
}

void stopStart() {
  if (running) {
    running = false;
    updField();
    noLoop();
    dispField();
  } 
  else {
    running = true;
    loop();
  }
}

void mousePressed() {
  if (!running) {
    println("Value = " + field[mouseX][mouseY]);
  }
  if (connect) {
    connect = false;
  } 
  else {
    connect = true;
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
    chFric(FRIC_INCR);
    break;
  case 'd':
  case 'D':
    chFric(1 / FRIC_INCR);
    break;
  case 'r':
  case 'R':
    spawnPart();
    break;
  case 'f':
  case 'F':
    popPart();
    break;
  case 'z':
  case 'Z':
    stopStart();
    break;
  case 'x':
  case 'X':
    toggleAlign();
    break;
  case 'c':
  case 'C':
    toggleAMode();
    break;
  }
}


void toggleAlign() {
  if (align) {
    align = false;
    println("full mode");
  } 
  else {
    align = true;
    println("align mode");
  }
}

void toggleAMode() {
  if (align) {
    if (aMode) {
      aMode = false;
      println("simple align");
    } 
    else {
      aMode = true;
      println("complex align");
    }
  }
}

void chMass(float f) {
  massPush *= f;
  println("massPush = " + massPush);
}

void chEdge(float f) {
  edgePush *= f;
  println("edgePush = " + edgePush);
}

void chFric(float f) {
  fricFac *= f;
  println("fricFac = " + fricFac);
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
PVector massForce(PVector l, Particle p) {
  PVector dst = PVector.sub(l, p.location);
  float d = max(dst.mag(), SIZE/4);
  dst.normalize();
  dst.mult(massPush / d);
  return dst;
}

void updField() {
  println("updating field");
  maxField = 0;
  PVector v, f;
  for (int i = 0; i < width; i++) {
    for (int j = 0; j < height; j++) {
      v = new PVector(i, j);
      f = new PVector(0, 0);
      Iterator<Particle> it = parts.iterator();
      while (it.hasNext ()) {
        Particle p = it.next();
        PVector dst = PVector.sub(p.location, v);
        float d = dst.mag();
        if (d <= p.rad) {
          field[i][j] = -1;
          break;
        }
        dst.mult(massPush / sq(d));
        dst = massForce(v, p);
        f.add(dst);
      }
      f.add(edgeForce(v));
      if (field[i][j] == -1) {
        field[i][j] = 0;
      } 
      else { 
        if ((edge < i) && (i <= width - edge) && (edge <= j) && (j <= height - edge)) {
          maxField = max(maxField, f.mag());
        }
        field[i][j] = f.mag();
      }
    }
  }
  println("max force = " + maxField);
  maxField /= 4;
}

void dispField() {
  println("displaying Field");
  for (int i = 0; i <width; i++) {
    for (int j = 0; j < height; j++) {
      stroke(colMapValue(field[i][j]/maxField));
      point(i, j);
    }
  }
  Iterator<Particle> it = parts.iterator();
  noStroke();
  while (it.hasNext ()) {
    Particle p = it.next();
    p.col = 1;
    p.display();
  }
}

// returns an HSV 0-1 value from an input 0-1;
color colMapValue(float in) {
  in = pow(in, MAP_FAC);
  float h = (0.75 - (0.85 * in) - (0.0625* 0.85) * sin(2* TWO_PI * in)) % 1;
  float s = 1 - pow(10, (in - 1) * 4.5);
  float v = 1 - pow(10, -in * 4.5);
  return color(h, s, v);
}

// average two colours
color avg(color c1, color c2) {
  float r = (red(c1)+red(c2)) / 2;
  float g = (green(c1)+green(c2)) / 2;
  float b = (blue(c1)+blue(c2)) / 2;
  return color(r, g, b);
}

// wrapper around bezier() taking in vectors
void linerBezier(PVector s, PVector vs, PVector e, PVector ve) {
  bezier(s.x, s.y, (s.x+vs.x), (s.y+vs.y), e.x, e.y, (e.x+ve.x), (e.y+ve.y));
}

void spawnPart() {
  if (parts.size() < MAX_PART) {
    Particle p = new Particle(new PVector(random(width), random(height)), new PVector(random(-INIT_SPEED_FACTOR, INIT_SPEED_FACTOR), random(-INIT_SPEED_FACTOR, INIT_SPEED_FACTOR)), SIZE);
    parts.add(p);
    println("Spawned " + p + " #" + parts.size());
  }
}

void popPart() {
  if (parts.size() > 0) {
    parts.remove(parts.size()-1);
  }
}

void updParts() {
  Iterator<Particle> it = parts.iterator();
  while (it.hasNext ()) {
    Particle p = it.next();
    p.push(edgeForce(p.location));
    Iterator<Particle> check = parts.iterator();
    while (check.hasNext ()) {
      Particle q = check.next();
      if (p == q) {
        break;
      }
    }
    if (align) {
      if (check.hasNext ()) {
        Particle q = check.next();
        // println("Checking " + p + " against " + q);
        PVector dst = PVector.sub(p.location, q.location);
        dst.mult(ELAST_FAC);
        q.push(dst);
        dst.mult(-1);
        p.push(dst);
      }
    } 
    if (!align || aMode) {
      while (check.hasNext ()) {
        Particle q = check.next();
        // println("Checking " + p + " against " + q);
        PVector dst = massForce(p.location, q);
        p.push(dst);
        dst.mult(-1);
        q.push(dst);
      }
    }
    p.update();
    p.resetAcc();
    p.display();
  }
}

void draw() {
  background(0.2);
  if (running) {
    updParts();
  }
  if (connect && parts.size() > 1) {
    strokeWeight(SIZE/2);
    noFill();
    Particle p1, p2, p3, p4;
    p1 = parts.get(0);
    for (int i = 0; i + 1 < parts.size(); i++) {
      p2 = parts.get(i);
      p3 = parts.get(i + 1);
      if (i + 2 == parts.size()) {
        p4 = p3;
      } 
      else {
        p4 = parts.get(i + 2);
      }
      stroke(colMapValue(PVector.sub(p2.location, p3.location).mag()/MAX_DIST));
      curve(p1.location.x, p1.location.y, p2.location.x, p2.location.y, p3.location.x, p3.location.y, p4.location.x, p4.location.y);
      p1 = p2;
    }
    noStroke();
  }
}

