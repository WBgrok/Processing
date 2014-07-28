// Constants
int W = 200;
int H = 200;
float MAP_FAC = 0.15;
int CV_SIZE = 3; // convolution matrix size

// Globals
float[][] bfr ;// Image Buffer
float[][] prv ;// Previous Buffer
float[][] cvM ;// Convolution Matrix (technically a constant,  but whatevs)


void setup() {
  size(W, H);
  background(#000000);
  colorMode(HSB, 1);
  //  colorMode(RGB, 1);

  // initialise buffer
  bfr = new float[W][H];
  prv = new float[W][H];
  for (int i = 0; i < W; i ++) {
    for (int j = 0; j < H; j ++) {
      bfr[i][j] = 0;
      prv[i][j] = 0;
    }
  }

  // initialise CM
  defineCvMtrx();
}

void mousePressed() {
  bfr[mouseX][mouseY] = 1;
}

void draw() {

  if (mousePressed) {
    if (mouseButton == RIGHT) {
      bfr[mouseX][mouseY] = 0;
    } 
    else {
      bfr[mouseX][mouseY] = 1;
    }
  }

  for (int i = 0; i < W; i ++) {
    for (int j = 0; j < H; j ++) {
      stroke(colMapValue(bfr[i][j]));  
      //      stroke(bfr[i][j]);
      point(i, j);
      // Whilst we're iterating on the buffer, copy to prv
      prv[i][j] = bfr[i][j];
    }
  }
  convolution();
  saveFrame("/tmp/Diffuse/frame-####################.tif");
}

// generates a new buffer by convolution
void convolution() {
  //TODO: re-write to support conv matrix of arbitrary (odd) size
  for (int i = 1; i < W-1; i ++) {
    for (int j = 1; j < H -1; j ++) {
      // for each point, except the edges
      float sum = 0;
      for (int u = 0; u < CV_SIZE; u++) {
        for (int v = 0; v < CV_SIZE; v++) {
          // the convolution is done with i,j being the midle point
          //          print("("+i+","+j+") & ("+u+","+v+")");
          sum += prv[i-1+u][j-1+v] * cvM[u][v];
        }
      }
      // bung the new value in the buffer
      bfr[i][j] = sum;
    }
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

// define the convoluion matrix
void defineCvMtrx() {
  cvM = new float[CV_SIZE][CV_SIZE];
  cvM[0][0] = 0.05;
  cvM[0][1] = 0.125;
  cvM[0][2] = 0.05;
  cvM[1][0] = 0.125;
  cvM[1][1] = 0.30;
  cvM[1][2] = 0.125;
  cvM[2][0] = 0.05;
  cvM[2][1] = 0.125;
  cvM[2][2] = 0.05;
}

