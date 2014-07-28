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
float ELAST_INCR = 1.05;

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
float elastFac = 0.0005;
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


void mousePressed() {
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
    chElast(ELAST_INCR);
    break;
  case 'f':
  case 'F':
    chElast(1 / ELAST_INCR);
    break;
  case 't':
  case 'T':
    spawnPart();
    break;
  case 'g':
  case 'G':
    popPart();
    break;
  case 'z':
  case 'Z':
    //    stopStart();
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

void chElast(float f) {
  elastFac *= f;
  println("elastFac = " + elastFac);
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
  dst.mult(massPush / sq(d));
  return dst;
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

    while (check.hasNext ()) {
      Particle q = check.next();
      // println("Checking " + p + " against " + q);
      PVector dst = PVector.sub(p.location, q.location);
      dst.mult(elastFac);
      q.push(dst);
      dst.mult(-1);
      p.push(dst);
      dst = massForce(p.location, q);
      p.push(dst);
      dst.mult(-1);
      q.push(dst);
    }

    p.update();
    p.resetAcc();
//    p.display();
  }
}

void draw() {
  background(0.2);
  if (running) {
    updParts();
  }
  if (connect && parts.size() > 1) {
    strokeWeight(SIZE/4);
    noFill();
    Particle p1, p2;
    for (int i = 0; i  < parts.size(); i++) {
      p1 = parts.get(i);
      for (int j = 0; j < parts.size(); j++) {
        p2 = parts.get(j);
        stroke(colMapValue(PVector.sub(p1.location, p2.location).mag()/MAX_DIST));
        line(p1.location.x, p1.location.y, p2.location.x, p2.location.y);
      }
    }
  }
  noStroke();
}

