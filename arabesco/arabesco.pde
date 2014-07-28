

void setup() {
  size(400, 400);
  stroke(#FF0000);
  fill(#00FF00);
}

void draw() {
  beginShape(TRIANGLE_FAN);
  vertex(30, 75);
  vertex(40, 20);

  vertex(60, 20);
  vertex(70, 75);
  vertex(80, 20);
  vertex(90, 75);
    vertex(50, 75);
  endShape();
}

