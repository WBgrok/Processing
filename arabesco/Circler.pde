class Circler {
  float period;
  float radius;
  float centreX;
  float centreY;
  int count;


  Circler(float x, float y, float p, float r) {
    centreX = x;
    centreY = y;
    period  = p;
    radius  = r;
    count = 0;
  }

  void render() {
    count++;
    curveVertex(centreX + radius * cos(TWO_PI * count/period), centreY + radius * sin(TWO_PI * count/period));
  }
}

