// The Arc class

class Arc {
  // The index of the arc determines its size and speed
  int indx;
  // thickness of the arc
  int sze;
  // radius to render the arc at
  int rad;
  PVector centre;
  // the angle to display at (will be factored by the index)
  float theta;
  
  Arc(int i, int s, PVector c, float t, int r) {
    indx = i;
    sze = s;
    centre = c.get();
    theta = t;
    rad = sze * r;
  }
  
  // draw the arc at current theta
  void render() {
    
    // start and end angles
    float start = (theta / indx) % TWO_PI;
    
    // an alternate version is to have the length of the arc being a fucntion of the index
    // (here all arcs are of equal length, a quarter of the circle)
    float end = start + (TWO_PI / 4);
    
    // prepare for drawing
    ellipseMode(RADIUS);
    strokeWeight(sze);
    colorMode(HSB, TWO_PI);
    stroke(start,TWO_PI,TWO_PI);
    noFill();
    
    // actually draw
    arc(centre.x,centre.y,rad,rad,start,end);
  }
  
  // draw the arc at given theta
  void render(float t) {
    theta = t;
    render();
  }
}


