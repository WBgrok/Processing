int WIDTH = 500;
int BATCH = 20;

float SQR_W = sqrt(WIDTH);

int[] count = new int[WIDTH];


void setup() {
  size(WIDTH, WIDTH);
  stroke(0, 255, 0);
  background(0);
}

void draw() {
  for (int i = 0; i <BATCH; i++) {
    cnt(generateSqrt());
  }
  for (int i = 0; i < WIDTH; i++) {
    line(i, WIDTH-1, i, WIDTH-1-count[i]);
  }
}

void cnt(float x) {
  count[floor(x)]++;
}

float generateNorm() {
  return random(WIDTH);
}

float generateSum() {
  float x = random(WIDTH/2);
  float y = random(WIDTH/2);
  return x + y;
}

float generateSqrt() {
  return sq(random(SQR_W));
}

float generate() {
  return random(SQR_W) * random(SQR_W);
}

