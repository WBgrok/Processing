int SIZE = 20;
int SQUARE_SIZE = 20;
color BG = color(175);
color GRID = color(100);
color P1 = color(255);
color P2 = color(0);

Connect5Renderer renderer;
GameState state;
void setup() {
  renderer = new Connect5Renderer(SIZE, SQUARE_SIZE, P1, P2, BG, GRID);
  state = new GameState(SIZE);
//  noLoop();
}

void mousePressed() {
  int xp = floor(mouseX / (SQUARE_SIZE + 1));
  int yp = floor(mouseY / (SQUARE_SIZE + 1));
  println(xp + " - " + yp);
  state.play(xp,yp,renderer);
  
}

void keyPressed() {
  printPos(state.getMoves());
  printPos(neighbourSpaces(state.getMoves(), SIZE));
}

void test() {
//    renderer.square(1, 0, 0);
//    renderer.square(2, 2, 2);
//    renderer.square(1, 1, 1);
//    renderer.square(2, 1, 0);
//    renderer.square(1, 0, 1);
//  state.play(0,0,renderer);
//  state.play(5,5,renderer);
//  state.play(5, 6, renderer);
  
}

void draw() {
//    test();
}

