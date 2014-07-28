class Colourer {
  // in spite of the name this works (in fact probably better, hue-wise) in HSV
  float r, g, b, a;
  float rv, gv, bv, av;
  float ra, ga, ba, aa;
  float scle; // max of colorMode used
  
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
    println ("r = "+r+" g = "+g+" b = "+b+" a = "+a);
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
    rv += ra - rv * (abs(rv) * FRIC_FAC);
    gv += ga - gv * (abs(gv) * FRIC_FAC);
    bv += ba - bv * (abs(bv) * FRIC_FAC);
    av += aa - av * (abs(av) * FRIC_FAC);
    
    r = (r + rv + scle) % scle;
    g = (g + gv) % (scle*2);
    b = (b + bv) % (scle*2);
    a = (a + av) % (scle*2);
//    println ("rv = "+rv+" gv = "+gv+" bv = "+bv+" av = "+av);
//    println ("r = "+r+" g = "+g+" b = "+b+" a = "+a);
    return color(r,min(g,2*scle-g),min(b,2*scle-b),min(a,2*scle-a));
  }
}
  
