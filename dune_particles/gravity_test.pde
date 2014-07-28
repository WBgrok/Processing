// constants
int WIDTH = 1280;
int HEIGHT = 960;
color STROKE = 0;
color FILL = 175;
float PROX = 1; // Particles separated by that value or less are considered as having collided
float EDGE_DAMP = 0.90; // Dampening on edge bounce
int MAX_PART = 100;
float SIZE = 5;
float ATTR_FACTOR = 10;
float GROWTH_RATE = 0.1;
float INIT_SPEED_FACTOR = 1;
float BOUNCE_CHANCE = 2000;  // on each collision, chance of bounce (divided by sum of rad)
float BOUNCE_SHED = 0.9; // radius reduction upon bounce
boolean RANDOM_SPAWN = true;
int RANDOM_SPAWN_MAX = 150; // probability of a random spawn happening each second - this will be divided by the number of balls in play
float GRAV_FACTOR = 0.5;

// globals
ArrayList<Particle> parts;
Particle newPart;
PVector mOrigPos;
boolean autoGen = false;
float mapMax = 10000;
boolean running = true;
float[][] field;
float maxField = 0;


void setup() {
  size(WIDTH, HEIGHT);
  background(#00AAAA);
  smooth();
  noStroke();
  parts = new ArrayList<Particle>();
  field = new float[width][height];
  for (int i = 0; i <width; i++) {
    for (int j = 0; j < height; j++) {
      field[i][j] = 0;
    }
  }
  println("f=" + field);
}

void mousePressed() {
  if (parts.size() < MAX_PART) {
    newPart = new Particle(new PVector(mouseX, mouseY), SIZE);
    mOrigPos = new PVector(mouseX, mouseY);
  }
  //  if (autoGen) {
  //    autoGen = false;
  //  } else {
  //    autoGen = true;
  //  }
}

void mouseReleased() {
  PVector mCurPos =  new PVector(mouseX, mouseY);
  mCurPos.sub(mOrigPos);
  mCurPos.mult(INIT_SPEED_FACTOR);
  newPart.push(mCurPos);
  parts.add(newPart);
}

void keyPressed() {
  if (running) {
    running = false;
    updField();
    noLoop();
    dispField();
    if (RANDOM_SPAWN) {
      RANDOM_SPAWN = false;
    } 
    else {
      RANDOM_SPAWN = true;
    }
  } 
  else {
    running = true;
    loop();
  }

  if (RANDOM_SPAWN) {
    RANDOM_SPAWN = false;
  } 
  else {
    RANDOM_SPAWN = true;
  }
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
        dst.mult(sq(p.rad) / sq(d));
        f.add(dst);
      }
      if (field[i][j] == -1) {
        field[i][j] = 0;
      } else {
      maxField = max(maxField, f.mag());
      field[i][j] = f.mag();
      }
    }
  }
  println("max force = " + maxField);
}

void dispField() {
  println("displaying Field");
  float f = 511 / log(maxField+1);
  for (int i = 0; i <width; i++) {
    for (int j = 0; j < height; j++) {
      stroke(colMapValue(log(field[i][j]+1) * f));
      point(i, j);
    }
  }
  Iterator<Particle> it = parts.iterator();
  while (it.hasNext ()) {
    Particle p = it.next();
    p.col = 0;
    p.display();
  }
}

// returns and RGB colour based on a value between 0 and 511
// Starts at #0000FF, through #FFFF00, to #FF0000
color colMapValue(float in) {
  int r = min(255, floor(in));
  int b = min(255, ceil(511 - in));
  return color(r, 0, b);
}
void aGen() {
  Particle p = new Particle(new PVector(random(width), random(height)), SIZE);
  parts.add(p);
}

void ranGen() {
  float r = random(100);
  if (r <=  (RANDOM_SPAWN_MAX / (frameRate * parts.size()))) {
    Particle p = new Particle(new PVector(random(width), random(height)), random(SIZE, 4 * SIZE));
    p.push(new PVector(random(-10, 10), random(-10, 10)));
    parts.add(p);
  }
}

void updParts() {
  Iterator<Particle> it = parts.iterator();
  ArrayList<Particle> newParts = new ArrayList<Particle>(parts);

  while (it.hasNext ()) {
    Particle p = it.next();

    //    PVector mousePos = new PVector(mouseX, mouseY);
    //    mousePos.sub(p.location);
    //    float fac = mousePos.mag();
    //    if (fac !=0) {
    //      fac = 1 / fac;
    //    }
    //    mousePos.setMag(fac * ATTR_FACTOR * sq(p.rad));
    //    p.push(mousePos);
    p.update();
    p.resetAcc();
    Iterator<Particle> check = newParts.iterator();
    while (check.hasNext ()) {
      Particle q = check.next();
      if (p == q) {
        break;
      }
    }
    while (check.hasNext ()) {
      Particle q = check.next();
      PVector dst = PVector.sub(p.location, q.location);
      float d = dst.mag();
      if (d <= PROX + p.rad + q.rad) {
        float r = random(100);
        if (r >= (BOUNCE_CHANCE / (p.rad + q.rad))) {
          p.bounce(q);
        } 
        else {
          p.absorb(q);
          check.remove();
        }
      } 
      else {
        dst.mult(GRAV_FACTOR * sq(p.rad) * sq(q.rad) / sq(d));
        q.push(dst);
        dst.mult(-1);
        p.push(dst);
      }
    }
    p.display();
  }
  parts = newParts;
}

void draw() {

  background(#00AAAA);

  if (autoGen && (parts.size() < MAX_PART)) {
    aGen();
  }

  if (RANDOM_SPAWN && (parts.size() < MAX_PART)) {
    ranGen();
  }


  if (mousePressed) {
    newPart.rad += GROWTH_RATE;
    newPart.display();
    stroke(#0000FF);
    line(mOrigPos.x, mOrigPos.y, mouseX, mouseY);
    noStroke();
  }

  if (running) {
    updParts();
  }
}

