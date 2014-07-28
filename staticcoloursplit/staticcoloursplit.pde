PImage img, r,g,b,rMin,gMin,bMin,rMax,gMax,bMax,rMask,gMask,bMask;
int WIDTH,HEIGHT, dim;
// scale factor for display
float SCALE_FACTOR = 0.5;

//threshold factors
float rMi = 0;
float rMa = 255;
float gMi = 0;
float gMa = 255;
float bMi = 0;
float bMa = 255;


void setup() {
  img = loadImage("test.jpg");
  img.resize(0,floor(img.height * SCALE_FACTOR));
  WIDTH = img.width;
  HEIGHT = img.height;
  size(WIDTH * 4, HEIGHT *4);
  background(0);
  // red component
  r = createImage(WIDTH,HEIGHT,RGB);
  r.copy(img,0,0,WIDTH,HEIGHT,0,0,WIDTH,HEIGHT);
  // green component
  g = createImage(WIDTH,HEIGHT,RGB);
  g.copy(img,0,0,WIDTH,HEIGHT,0,0,WIDTH,HEIGHT);
  // blue component
  b = createImage(WIDTH,HEIGHT,RGB);
  b.copy(img,0,0,WIDTH,HEIGHT,0,0,WIDTH,HEIGHT);
  // bw images of pixels above min threshold, below max, and resulting mask
  rMin = createImage(WIDTH,HEIGHT,RGB);
  rMax = createImage(WIDTH,HEIGHT,RGB);
  rMask = createImage(WIDTH,HEIGHT,RGB);
  gMin = createImage(WIDTH,HEIGHT,RGB);
  gMax = createImage(WIDTH,HEIGHT,RGB);
  gMask = createImage(WIDTH,HEIGHT,RGB);
  bMin = createImage(WIDTH,HEIGHT,RGB);
  bMax = createImage(WIDTH,HEIGHT,RGB);
  bMask = createImage(WIDTH,HEIGHT,RGB);
  
  split();
}

void draw() {
  // original image
  image(img,0,0);
  // components breakdown
  image(r,0,HEIGHT);
  image(g,0,HEIGHT * 2);
  image(b,0,HEIGHT * 3);
  // min images
  image(rMin,WIDTH,HEIGHT);
  // max images
  image(rMax,WIDTH*2,HEIGHT);
  // masks
  image(rMask,WIDTH*3,HEIGHT);
}

void keyPressed() {
  switch(key) {
    //red 
    case 'q':
      rMi--;
      thresh(0,0);
      break;
    case 'w':
      rMi++;
      thresh(0,0);
      break;      
    case 'e':
      rMa--;
      thresh(0,1);
      break;
    case 'r':
      rMa++;
      thresh(0,1);
      break;
    // green
    case 'a':
      gMi--;
      break;
    case 's':
      gMi++;
      break;
    case 'd':
      gMa--;
      break;
    case 'f':
      gMa++;
      break;
    // blue
    case 'z':
      gMi--;
      break;
    case 'x':
      gMi++;
      break;
    case 'c':
      gMa--;
      break;
    case 'v':
      gMa++;
      break; 
  }
}


// recalculate thresholds - col: 0:red, 1:green, 2:blue, bound: 0:min, 1:max
//
void thresh(int col, int bound) {
  switch(col) {
    case 0:
      // red
      if (bound == 0) {
        // recalc min
        rMin.copy(r,0,0,WIDTH,HEIGHT,0,0,WIDTH,HEIGHT);
        rMin.filter(THRESHOLD,rMi/255);
      } else {
        // max
        rMax.copy(r,0,0,WIDTH,HEIGHT,0,0,WIDTH,HEIGHT);
        rMax.filter(THRESHOLD,rMa/255);
        rMax.filter(INVERT);
      }
    sumMask(0);  
    break;
    case 1:r
      // green
      if (bound == 0) {
        // min
      } else {
        // max       
      }
    break;
    case 2:
      // blue
      if (bound == 0) {
        // min
      } else {
        // max
      }
    break;
  }
    
}

// Sum Mask image  col: 0:red, 1:green, 2:blue
// 
void sumMask(int col) {
  switch(col) {
    case 0:
      // red
      rMask.copy(rMin,0,0,WIDTH,HEIGHT,0,0,WIDTH,HEIGHT);
      rMask.blend(rMax,0,0,WIDTH,HEIGHT,0,0,WIDTH,HEIGHT,MULTIPLY);
    break;
    case 1:
      // green
     
    break;
    case 2:
      // blue
      
    break;
  }
    
  
}

void split() {
  int rd,grn,blu;
  for (int i=0; i < img.pixels.length; i++) {
    //get the red value
    rd = (int) r.pixels[i] >> 16 & 0xFF;
    r.pixels[i] = color(rd,0,0);
    //green
    grn = (int) g.pixels[i] >> 8 & 0xFF;
    g.pixels[i] = color(0,grn,0);
    //blue
    blu = (int) b.pixels[i] & 0xFF;
    b.pixels[i] = color(0,0,blu);
  }
}
