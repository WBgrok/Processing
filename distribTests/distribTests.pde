int WIDTH = 511;
int BATCH = 20;

float SQR_W = sqrt(WIDTH);

float[] val = new float[WIDTH];
float[] show = new float[WIDTH];

int start = 0;
float mx = 0;


void setup() {
  colorMode(HSB,1);
  size(WIDTH, WIDTH);
  background(0);
  test();
  //ref();
}

void ref() {
  for (int i = 0; i < WIDTH; i++) {
    stroke(yaxMap((float)i/WIDTH));
    line(i, 0, i, WIDTH-1);
    //red
    //stroke(#000000);
   // point(i, WIDTH-1-min(255, i));
    //blue
    //stroke(#00FF00);
  //  point(i, WIDTH-1-min(255, ceil(511 - i)));
  }
}

void test() {
  for (int i = 0; i < WIDTH; i++) {
    val[i] = (1 / sq(i+1));
    if (i >= start) {
      mx = max(mx, val[i]);
    }
  }
  println("Max = " + mx);
  tweak();
  for (int i = 0; i < WIDTH; i++) {
    print(show[i] + ",");
    stroke(yaxMap(show[i]*100));
    line(i, 0, i, WIDTH-1);
//    stroke(0);
//    point(i, WIDTH-1-((val[i] / mx) * 511));
//    stroke(255);
//    point(i, (log((val[i] / mx)+1) * 511));
  }
}

void mousePressed() {
  color c = colMapValue(show[mouseX]);
  println(red(c) + "," + green(c) + "," + blue(c));
}

void draw() {
}

void tweak() {
  for (int i = 0; i <WIDTH; i++) {
    print(val[i] + ",");
    show[i] = val[i] / mx;
    //show[i] = (val[i] / mx) * 511;
  }
  println("and...");
}

// returns and RGB colour based on a value between 0 and 511
// Starts at #0000FF, through #FFFF00, to #FF0000
color colMapValue(float in) {
  int r = min(255, floor(in));
  int b = min(255, ceil(511 - in));
  return color(r, 0, b);
}
// maps 
color yaxMap(float in) {
  float h = (0.75 - (0.85 * in) - (0.0625* 0.85) * sin(2* TWO_PI * in)) % 1;
  float s = 1 - pow(10, (in - 1) * 4.5);
  float v = 1 - pow(10, -in * 4.5);
  println(in + " to " + h + "," + s + "," + v);
  return color(h,s,v);
}

