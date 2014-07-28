//// Atempting a particle system
//
//// constants
//int WIDTH = 640;
//int HEIGHT = 480;
//color STROKE = 0;
//color FILL = 175;
//float PROX = 1; // Particles separated by that value or less are considered as having collided
//float EDGE_DAMP = 0.9; // Dampening on edge bounce
//int MAX_PART = 1;
//PVector ORIG;
//
////globals
//float _max_rad = 1;
//int p_count = 0;
//ParticleSystem mySystem;
//
//void setup() {
//  size(WIDTH, HEIGHT);
//  smooth();
//  ORIG = new PVector(0,0);
//  mySystem = new ParticleSystem(1,1,WIDTH,HEIGHT);
//}
//
//void draw() {
//  background(0);
//  println("system size = " + mySystem.particles.size());
//  if (mySystem.particles.size() < MAX_PART) {
//    mySystem.addParticle(new PVector(random(0,50),random(0,50)));
//    mySystem.particles.get(p_count).push(new PVector(random(-1,1),random(-1,1)));
//    println ("new particle " + p_count);
//    p_count++;
//  }
//  mySystem.run();
//}


