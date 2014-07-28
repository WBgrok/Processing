PImage a;

void setup () {
  a = loadImage("trees.jpg"); // From examples/.../Blur/data
  size(a.width, a.height);
  noLoop();
}

void draw () {
  PImage c = blender(a, a, DIFFERENCE);
  c.filter(THRESHOLD, 0.005); // Note very small threshold value.
  image(c, 0, 0);
}

PImage blender(PImage img1, PImage img2, int mode) {
  PImage c = createImage(img1.width, img1.height, RGB);
  c.copy(img1, 0, 0, img1.width, img1.height, 0, 0, img1.width, img1.height);
  c.blend(img2, 0, 0, img1.width, img1.height, 0, 0, img1.width, img1.height, mode);
  return c;
}

