// constants
//String url = "http://www.whitegadget.com/attachments/pc-wallpapers/16950d1224057972-landscape-wallpaper-blue-river-scenery-wallpapers.jpg";
//String url = "http://www.ibiblio.org/wm/paint/auth/kandinsky/kandinsky.comp-7.jpg";
String url = "http://www.deshow.net/d/file/travel/2009-06/germany-landscape-605-2.jpg";
//String url = "http://deathvalleyhikerasso.homestead.com/files/the_desert_landscape.jpg";
//String url = "https://lh5.googleusercontent.com/-DYIX_2aFpCs/UI7m7rSVD3I/AAAAAAAAF-g/RQRaWnWB5G0/s720/Red+by+Hakan+G%C3%BCl.JPG";
//String url = "http://www.deshow.net/d/file/travel/2009-02/landscape-photo-manipulation-415-2.jpg";
//String url = "http://2.bp.blogspot.com/-dcgYv-SFEu0/TZVuveCkq1I/AAAAAAAAI2A/vK0PXUJ8aKw/s1600/tiger_wallpapers_hd_Bengal_Tiger_hd_wallpaper.jpg";
PImage file = null;
boolean doubleRes = false;

float EDGE_DAMP = 0.5;
float RND_INC = 1.05;
float PART_MASS = 2;
float STROKE_FAC = 10;
float VEL_FAC = 1;

// globals
color[][] data;
Liner liner;
ArrayList<Liner> liners;
int nLin = 0;
float rand_fac = 1.00;

// wrapper around bezier() taking in vectors
void linerBezier(PVector s, PVector vs, PVector e, PVector ve) {
  bezier(s.x, s.y, (s.x+vs.x), (s.y+vs.y), e.x, e.y, (e.x+ve.x), (e.y+ve.y));
}

// average two colours
color avg(color c1, color c2) {
  float r = (red(c1)+red(c2)) / 2;
  float g = (green(c1)+green(c2)) / 2;
  float b = (blue(c1)+blue(c2)) / 2;
  return color(r, g, b);
}

void loadImg(File f) {
  if (f == null) {
    file =loadImage(url, "bmp");
    return;
  }
  file = loadImage(f.getAbsolutePath());
}

void keyPressed() {
  liners.add(new Liner(new PVector(random(width), random(height)), new PVector(random(-2, 2), random(-2, 2))));
}


void setup() {
  //  selectInput("Select a file to process:", "loadImg");
  //  while (file == null) {
  //  }
  file =loadImage(url, "bmp");
  println("Start set-up");
  PImage img = file;
  if (doubleRes) {
    size(img.width * 2, img.height * 2);
    data = new color[width][height];
    img.loadPixels();
    for (int i = 0; i < img.width; i++) {
      for (int j = 0; j < img.height; j++) {
        data[2*i][2*j] = img.pixels[(j * img.width) +i];
        data[2*i][2*j+1] = img.pixels[(j * img.width) +i];
        data[2*i+1][2*j] = img.pixels[(j * img.width) +i];
        data[2*i+1][2*j+1] = img.pixels[(j * img.width) +i];
      }
    }
  } 
  else {
    size(img.width, img.height);
    data = new color[width][height];
    img.loadPixels();
    for (int i = 0; i < img.width; i++) {
      for (int j = 0; j < img.height; j++) {
        data[i][j] = img.pixels[(j * img.width) +i];
      }
    }
  }
  background(0);
  noFill();
  strokeCap(ROUND);
  liners = new ArrayList<Liner>();
  liners.add(new Liner(new PVector(random(width), random(height)), new PVector(random(-2, 2), random(-2, 2))));
  println("aaand done");
}


void draw() {
  if (mousePressed) {
    if (mouseButton == LEFT) {
      rand_fac *= RND_INC;
    }  
    else {
      rand_fac /= RND_INC;
    }
  } 
  Iterator<Liner> it = liners.iterator();
  while (it.hasNext ()) {
    Liner l = it.next();
    l.push(new PVector(random(-rand_fac, rand_fac), random(-rand_fac, rand_fac)));
    l.update();
  }
}

