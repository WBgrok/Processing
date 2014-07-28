// For each pixel, which object contains it? 
ImgBubble[][] pix;
// image to display
//String url = "http://www.hack4fun.org/h4f/sites/default/files/bindump/lena.bmp";
//String url = "http://www.whitegadget.com/attachments/pc-wallpapers/16950d1224057972-landscape-wallpaper-blue-river-scenery-wallpapers.jpg";
//String url = "http://2.bp.blogspot.com/-dcgYv-SFEu0/TZVuveCkq1I/AAAAAAAAI2A/vK0PXUJ8aKw/s1600/tiger_wallpapers_hd_Bengal_Tiger_hd_wallpaper.jpg";
String url = "http://www.deshow.net/d/file/travel/2009-06/germany-landscape-605-2.jpg";



// Actual pixels of the image
color[][] data;


void setup() {
  PImage img;
  img = loadImage(url,"bmp");
//  size(1024, 602);
  size(img.width, img.height);
  data = new color[width][height];
  pix = new ImgBubble[width][height];

  
  img.loadPixels();
  for (int i = 0; i < img.width; i++) {
    for (int j = 0; j < img.height; j++) {
      data[i][j] = img.pixels[(j * img.width) +i];
    }
  }
  background(0);
  new ImgBubble(0,0,img.width,img.height);
  
    noStroke();
}

void draw() {
//  background(0);
while(pix[floor(random(width))][floor(random(height))].split()) {
}
  
}

void mousePressed() {
  
  
}

class ImgBubble {
  
  // coord of top left corner, and size
  int xPos;
  int yPos;
  int wdth;
  int hght;
  
  color col;
  boolean hasChild;
  ImgBubble child;
  
  ImgBubble(int xp, int yp, int w, int h) {
    hasChild = false;
    xPos = xp;
    yPos = yp;
    wdth = w;
    hght = h;
    
    float r = 0;
    float g = 0;
    float b = 0;
    
    // updates ref array:
     for (int i = xPos; i < xPos + wdth; i++) {
      for (int j = yPos; j < yPos + hght; j++) {
        pix[i][j] = this;
        r += red(data[i][j]);
        //println("summed " + data[i][j] + "onto " + r);
        g += green(data[i][j]);
        b += blue(data[i][j]);
        
      }
     }
     
     r = round(r / (wdth * hght));
     g = round(g / (wdth * hght));
     b = round(b / (wdth * hght));
     col = color(r,g,b);
     //println ("created " + this + " at " + xp +"," + yp);
    // println("rgb=" + r + ","+g+","+b);
    // println ("colour " + red(col)+","+green(col)+","+blue(col));
     this.draw();
    
    
  }
  
  void draw() {
    fill(col);
    ellipse((xPos + wdth/2), (yPos + hght/2), wdth, hght);
  }
  
  boolean split() {
    

    if(max(wdth,hght) <=1){
      println("minimum resolution - skipping");
      return true;
    }
    // if we're not at the lowest generation, split the child
    if (hasChild) {
      child.split();
      //println("skippo");
      return false;
    }
    
    
    // generates 4 new sub-images and put them in the global array
    int newX = floor(xPos+wdth/2);
    int newY = floor(yPos+hght/2);
    
    hasChild = true;
    
    ImgBubble topLeft = new ImgBubble(xPos,yPos,floor(wdth/2),floor(hght/2));
    ImgBubble topRight = new ImgBubble(newX,yPos,ceil(wdth/2),floor(hght/2));
    ImgBubble botLeft = new ImgBubble(xPos,newY,floor(wdth/2),ceil(hght/2));
    ImgBubble botRight = new ImgBubble(newX,newY,ceil(wdth/2),ceil(hght/2));
    
    child = topLeft;
    return false;
    
//    for (int i = xPos; i < floor(wdth/2); i++) {
//      for (int j = yPos; j < floor(hght/2); j++) {
//        ImgBubble[i][j] = topLeft;
//      }
//      for {int j = newY; j < ceil(hght/2); j++) {
//        ImgBubble[i][j] = botLeft;
//      }
//    }
//    for (int i = newX; i < ceil(wdth/2); i++) {
//      for (int j = yPos; j < floor(hght/2); j++) {
//        ImgBubble[i][j] = topRight;
//      }
//      for {int j = newY; j < ceil(hght/2); j++) {
//        ImgBubble[i][j] = botRight;
//      }
//    }
  }
  
}
