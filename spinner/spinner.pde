// constants

float ACC_RAND = 0.005;
float SPEED_INCR = 0.01; // rad/s
float STROKE_INCR = 0.1;
int WIDTH = 720;
int HEIGHT = 720;
float SCLE = 255;
float RAD_INCR = 0.5; // pixels
float CX = WIDTH / 2;
float CY = HEIGHT / 2;
float FRIC_FAC = 0.1;

// globals

float speed = 0.0051; // rad/s
float strk = 2;
float rad = CX;
Colourer col;

float dx;
float dy;
float ang;


void setup() {
  colorMode(HSB,SCLE);
  size(WIDTH,HEIGHT);
  col = new Colourer(color(100,252, 100,252),SCLE);
//  col.push(0, 0.1, 0, 0);
  noFill();
  ang = 0;
  frameRate(100);
}

void draw() {
  dx = CX + rad * cos(ang);
  dy = CY + rad * sin(ang);
  ang += speed;
  col.push(random(-ACC_RAND,ACC_RAND),random(-ACC_RAND,ACC_RAND),random(-ACC_RAND,ACC_RAND),random(-ACC_RAND,ACC_RAND));
//  col.push(random(-ACC_RAND,ACC_RAND), 0, 0, 0);

  color c = col.update();
  strokeWeight(strk);
  stroke(hue(c),saturation(c),brightness(c),alpha(c));
//    stroke(red(c),blue(c),green(c),alpha(c));
//stroke(c);
  line(CX,CY,dx,dy);
}
