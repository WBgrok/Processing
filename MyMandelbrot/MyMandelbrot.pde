/**
 * The Mandelbrot Set
 * by Daniel Shiffman.  
 * 
 * Simple rendering of the Mandelbrot set.
 */
 
// Establish a range of values on the complex plane
// A different range will allow us to "zoom" in or out on the fractal
// float xmin = -1.5; float ymin = -.1; float wh = 0.15;
float xmin = -2.5; 
float ymin = -2; 
float wh = 4;
float zoomFactor = 0.001;
float zFacIncr = 0.001;
float moveFactor = 0.01;

void setup() {
  size(500, 500, P2D);
  background(255);
  frameRate(20);
  // Make sure we can write to the pixels[] array. 
  // Only need to do this once since we don't do any other drawing.
  drawmb();
}

void reset() {
  float xmin = -2.5; 
  float ymin = -2; 
  float wh = 4;
}

void move(float x,float y) {
  xmin+=(x*wh*moveFactor);
  ymin+=(y*wh*moveFactor);
  
}

void zoomIn() {
  println("Z-IN wh= " + wh);
  wh-=zoomFactor;
  xmin+=(zoomFactor/2);
  ymin+=(zoomFactor/2);
  zoomFactor+=zFacIncr;
}

void zoomOut() {
  println("Z-OUT wh= " + wh);
  wh+=zoomFactor;
  xmin-=(zoomFactor/2);
  ymin-=(zoomFactor/2);
  zoomFactor+=zFacIncr;
}

void draw() {
  if(mousePressed) {
    handleMouse();
    drawmb();
  } else {
    zoomFactor = 0.001;
  }
}

void handleMouse() {
  float dmx = float(mouseX)/float(width)-0.5 ;
  float dmy = float(mouseY)/float(height)-0.5;
  println("dmx = " + dmx + " dmy = " + dmy);
  if (mouseButton == LEFT) {
    zoomIn();
    move(dmx,dmy);
  } else if (mouseButton == RIGHT) {
    zoomOut();
    move(dmx,dmy);
  } else {
    reset();
    
  }
}


void drawmb() {
  loadPixels();
  // Maximum number of iterations for each point on the complex plane
  int maxiterations = 200;

  // x goes from xmin to xmax
  float xmax = xmin + wh;
  // y goes from ymin to ymax
  float ymax = ymin + wh;
  
  // Calculate amount we increment x,y for each pixel
  float dx = (xmax - xmin) / (width);
  float dy = (ymax - ymin) / (height);

  // Start y
  float y = ymin;
  for (int j = 0; j < height; j++) {
    // Start x
    float x = xmin;
    for (int i = 0;  i < width; i++) {
      
      // Now we test, as we iterate z = z^2 + cm does z tend towards infinity?
      float a = x;
      float b = y;
      int n = 0;
      while (n < maxiterations) {
        float aa = a * a;
        float bb = b * b;
        float twoab = 2.0 * a * b;
        a = aa - bb + x;
        b = twoab + y;
        // Infinty in our finite world is simple, let's just consider it 16
        if(aa + bb > 16.0) {
          break;  // Bail
        }
        n++;
      }
      
      // We color each pixel based on how long it takes to get to infinity
      // If we never got there, let's pick the color black
      if (n == maxiterations) {
        pixels[i+j*width] = 0;
      } else {
        // Gosh, we could make fancy colors here if we wanted
        pixels[i+j*width] = color(n*16 % 255);  
      }
      x += dx;
    }
    y += dy;
  }
  updatePixels();
}

