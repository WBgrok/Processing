// constants
int WIDTH = 1280;
int HEIGHT = 960;
float EDGE_DAMP = 0.9;

// eater parameters
PRED_EFFICIENCY = 1.00;


//globals
ArrayList<Predator> predators;
ArrayList<Prey> preys;

void setup() {
  size(WIDTH,HEIGHt);
  background(#000000);
  predators = new ArrayList<Predator>;
  prey = new ArrayList<Prey>;
  noStroke();
}
