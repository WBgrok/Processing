// constants 
int WIDTH = 500;
int HEIGHT = 500;
float ANGULAR_VEL = 0.10;
float SM_RAD = 13;
float BG_RAD = 160;
float P_DIST = 50;
color BG_COL = 0;
color OUTLINE= 64;
color PEN = 255;

float theta = 0;
float r_ratio = SM_RAD / BG_RAD;

PVector centre = new PVector(WIDTH/2, HEIGHT/2); 
// cente of the small circle, relative to main centre
PVector smCentre = new PVector(BG_RAD-SM_RAD, 0);
// tip of the pen, relative to the centre of the small circle
PVector pen = new PVector(P_DIST, 0);
// tip of the pen, absolute;
PVector tip = new PVector(0, 0);


void setup () {
  size(WIDTH, HEIGHT);
  ellipseMode(CENTER);
  noFill();
  frameRate(120);
  background(BG_COL);
}

// updates smCentre, pen, based on angle
void moveto(float angle) {
  // pen relative to the small circle centre:
  pen.set(new PVector(P_DIST * cos(theta), P_DIST * sin(theta)));

  // the small circle's edge moves against the inner edge of the large circle
  // for theta angular movement, the circle rolls theta * small r - this is an angle
  // on the big circle of (theta * small_r)/ big_r
  smCentre.set(new PVector((BG_RAD - SM_RAD) * cos(theta * r_ratio), (BG_RAD - SM_RAD) * sin(theta * r_ratio)));

  // sum up to get the ocation of the tip
  tip = centre.get();
  tip.add(smCentre);
  tip.add(pen);
}

//draws the apparatus
void drawSpiro() {
  
  stroke(OUTLINE);
  //big circle
  ellipse(centre.x,centre.y, BG_RAD * 2, BG_RAD * 2);
  ellipse(centre.x + smCentre.x, centre.y + smCentre.y, SM_RAD * 2 , SM_RAD * 2);
  line(centre.x + smCentre.x, centre.y + smCentre.y, tip.x, tip.y);
}

void draw() {
  
  theta += ANGULAR_VEL;
  moveto(theta);
//  drawSpiro();
  stroke(theta);
//  point(tip.x, tip.y);
  
  rectMode(CORNERS);
  rect(centre.x, centre.y, tip.x, tip.y);
}

void keyPressed() {
  println("smCentre = " + smCentre);
} 

