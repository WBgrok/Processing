import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Diffuse extends PApplet {

// Constants
int W = 200;
int H = 200;
float MAP_FAC = 0.15f;
int CV_SIZE = 3; // convolution matrix size

// Globals
float[][] bfr ;// Image Buffer
float[][] prv ;// Previous Buffer
float[][] cvM ;// Convolution Matrix (technically a constant,  but whatevs)


public void setup() {
  size(W, H);
  background(0xff000000);
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

public void mousePressed() {
  bfr[mouseX][mouseY] = 1;
}

public void draw() {

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
public void convolution() {
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
public int colMapValue(float in) {
  in = pow(in, MAP_FAC);
  float h = (0.75f - (0.85f * in) - (0.0625f* 0.85f) * sin(2* TWO_PI * in)) % 1;
  float s = 1 - pow(10, (in - 1) * 4.5f);
  float v = 1 - pow(10, -in * 4.5f);
  return color(h, s, v);
}

// define the convoluion matrix
public void defineCvMtrx() {
  cvM = new float[CV_SIZE][CV_SIZE];
  cvM[0][0] = 0.05f;
  cvM[0][1] = 0.125f;
  cvM[0][2] = 0.05f;
  cvM[1][0] = 0.125f;
  cvM[1][1] = 0.30f;
  cvM[1][2] = 0.125f;
  cvM[2][0] = 0.05f;
  cvM[2][1] = 0.125f;
  cvM[2][2] = 0.05f;
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Diffuse" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
