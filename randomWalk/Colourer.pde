class Colourer {
  // in spite of the name this works (in fact probably better, hue-wise) in HSV
  float r, g, b, a;
  float rv, gv, bv, av;
  float ra, ga, ba, aa;
  float scle // max of colorMode used
  
  Colourer(color c, float s) {
    r = hue(c);
    g = saturation(c);
    b = brightness(c);
    a = alpha(c);
    rv = 0;
    gv = 0;
    bv = 0;
    av = 0;
    scle = s;
    initAcc();
  }
  
  void initAcc() {
    ra = 0;
    ga = 0;
    ba = 0;
    aa = 0;
  }
  
  void push(float rf, float gf, float bf, float af) {
    ra += rf;
    ga += gf;
    ba += bf;
    aa += af;
  }
  
  color update() {
    rv += ra;
    bv += ba;
    gv += ga;
    av += aa;
    
    r = (r + rv) % scle;
    g = (g + gv) % scle;
    b = (b + bv) % scle;
    a = (a + av) % scle;
    
    return color(r,g,b,a);
  }
}
  
