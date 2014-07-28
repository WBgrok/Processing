// Constants
// constants
int WIDTH = 1280;
int HEIGHT = 800;
color STROKE = 0;
color FILL = 175;
float EDGE_DAMP = 0.90; // Dampening on edge bounce
int MAX_PART = 500;
float INIT_SPEED_FACTOR = 1;
float SIZE = 5;
float FRIC_INCR = 1.05;
float ACC_INCR = 1.05;
float MAP_FAC = 0.5;

// globals
ArrayList<Particle> parts;
float fricFac = 0.01;
float accPower = 0.01;
int minRad = 100;
int maxRad = 200;
int nParts = 0;


void setup() {
  size(WIDTH, HEIGHT);
  colorMode(HSB, 1);
  background(0.2);
  smooth();
  noStroke();
  parts = new ArrayList<Particle>();
}

void keyPressed() {
  switch(key) {
  case 'q':
  case 'Q':
    chMinRad(1);
    break;
  case 'a':
  case 'A':
    chMinRad(-1);
    break;
  case 'w':
  case 'W':
    chMaxRad(1);
    break;
  case 's':
  case 'S':
    chMaxRad(-1);
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
    chAcc(ACC_INCR);
    break;
  case 'f':
  case 'F':
    chAcc(1/ACC_INCR);
    break;
  case 't':
  case 'T':
    spawnPart();
    break;
  case 'g':
  case 'G':
    popPart();
    break;
  }
}


void spawnPart() {
  if (parts.size() < MAX_PART) {
    Particle p = new Particle(new PVector(random(width), random(height)), new PVector(random(-INIT_SPEED_FACTOR, INIT_SPEED_FACTOR), random(-INIT_SPEED_FACTOR, INIT_SPEED_FACTOR)), SIZE);
    parts.add(p);
    println("Spawned " + p + " #" + parts.size());
    nParts++;
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

void popPart() {
  if (parts.size() > 0) {
    parts.remove(parts.size()-1);
  }
}

void chFric(float f) {
  fricFac *= f;
  println("fricFac = " + fricFac);
}

void chAcc(float f) {
  accPower *= f;
  println("accPower = " + accPower);
}

void chMinRad(int i) {
  minRad += i;
  println("minRad = " + minRad);
}

void chMaxRad(int i) {
  maxRad += i;
  println("maxRad = " + maxRad);
}

void updParts() {
  Iterator<Particle> it = parts.iterator();
  PVector cGrav = new PVector(0, 0);
  while (it.hasNext ()) {
    Particle p = it.next();
    cGrav.add(p.location);
  }
  cGrav.div(nParts);
  stroke(0,0,255);
  strokeWeight(1);
  noSmooth();
  point(cGrav.x, cGrav.y);
  ellipseMode(CENTER);
  noFill();
  ellipse(cGrav.x, cGrav.y, minRad * 2, minRad *2);
  ellipse(cGrav.x, cGrav.y, maxRad * 2, maxRad *2);
  noStroke();
  it = parts.iterator();
  while (it.hasNext ()) {
    Particle p = it.next();
    PVector dst = PVector.sub(p.location, cGrav);
    float d = dst.mag();
    dst.normalize();
    if ( d < minRad) {
      dst.mult(accPower);
      p.push(dst);
    } 
    else if (d > maxRad) {
      dst.mult(-accPower);
      p.push(dst);
    }
    p.update();
    p.resetAcc();
    p.display();
  }

}

void draw() {
  background(0.2);
  updParts();
}

