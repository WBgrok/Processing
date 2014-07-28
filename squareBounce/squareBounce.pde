// N squares
int N = 100;
// Size squares
int S = 5;

// Rigidity
float L = 0.03;

// Mass 
float m = 1.5;

// Dampening
float d = 0.001;

// Mouse force
float P = 0.1;

// Neighbouring effect - the value is for diagonal squares
float b = 2.0;


// Arrays - intialise at N+2 to contain borders (fixed)
float[][] squarePos = new float[N+2][N+2];
float[][] squareForce = new float[N+2][N+2];
float[][] squareSpeed = new float[N+2][N+2];

//global dis for logging
float dis = 0;

void setup() {
  colorMode(HSB,256);
  frameRate(24);
  size(500, 500,P3D);
  stroke(255);
  noStroke();
  // Initialise arrays - loop from 0 to N+1 to do edges
  for(int i = 0; i<=N+1; i++) {
    for (int j = 0; j<=N+1; j++) {
      squarePos[i][j]=0;
      squareForce[i][j]=0;
      squareSpeed[i][j]=0;
    }
  }
}

void draw() {
  int x;
  int y;

  if (mousePressed){
    x=min(N,floor(mouseX/S)+1);
    y=min(N,floor(mouseY/S)+1);
    println("x="+x+" y="+y+" P="+squarePos[x][y]+" F="+squareForce[x][y]+" v="+squareSpeed[x][y]);
    squarePos[x][y] += P;
    squareSpeed[x][y] = 0;
  }
  calcSquares();
  renderSquares();
}

void calcSquares() {
  // All loops are from 0 to N included - coordinates 0 and N+1 are for the fixed edges
  
  // Calculate force according to position
  for(int i = 1; i<=N; i++) {
    for(int j = 1; j<=N; j++) {
      //average displacement
      float dis=0;
      for(int k = -1; k <= 1; k++) {
        for(int l = -1; l <= 1; l++) {
          dis+= b * (squarePos[i][j] - (squarePos[i+k][j+l]));
        }
      }
      // println("i="+i+" j="+j+" Dis="+dis);
      squareForce[i][j] = -(L/m)*((dis/8)+squarePos[i][j]) - (d/m)*squareSpeed[i][j]; 
    }
  }
  
  // speed according to force
  for(int i = 1; i<=N; i++) {
    for(int j = 1; j<=N; j++) {
      squareSpeed[i][j] += squareForce[i][j];
    }
  }
  
  // Position according to speed
  for(int i = 1; i<=N; i++) {
    for(int j = 1; j<=N; j++) {
      squarePos[i][j]+= squareSpeed[i][j];
      if (squarePos[i][j] > 1) {
        squarePos[i][j] = 1;
      }
      if (squarePos[i][j] < -1) {
        squarePos[i][j] = -1;
      }
     // println(" Dis="+dis);
     // println("i="+i+" j="+j+" P="+squarePos[i][j]+" F="+squareForce[i][j]+" v="+squareSpeed[i][j]);
    }
  }
}

void renderSquares() {
 // noStroke();
  int x=0;
  int y=0;
  for(int i = 1; i<=N; i++) {
    x=0;
    for (int j = 1; j<=N; j++) {
      float v = squarePos[j][i];
    //  int a = floor((sin(PI*v)+1)*128);
    //  int b = 64+floor((cos(PI*v)+1)*64);
   //   int c = 64+floor((sin(PI*v)+1)*64);
      fill(128*(1+v));
      box(x,y,50*(1+v));
      //rect(x,y,S,S);
      x+=S;
    }
    y+=S;
  }
}

//color convertColour(float v) {
//  int a = floor((sin(PI*v)+1)*128);
//  int b = floor((cos(PI*v)+1)*64);
//  int c = floor((sin(PI*v)+1)*64);
//}
