import processing.serial.*;


import pt.citar.diablu.processing.mindset.*;



MindSet r;
int attention;
int strength;

int numSamples = 60;
ArrayList attSamples;

void setup() {
  size(512, 512);
  attSamples = new ArrayList();
  r = new MindSet(this, "/dev/cu.BrainBand-DevB");
}

void draw() {
}


public void eegEvent(int delta, int theta, int low_alpha, int high_alpha, int low_beta, int high_beta, int low_gamma, int mid_gamma) {
  background(0);
  print(delta);
}

   
