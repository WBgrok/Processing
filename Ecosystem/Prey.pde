class Predator {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float mass;
  boolean alive;
  
  Prey(PVector l, float m) {
    location = l.get;
    acceleration = new PVector();
    velocity = new PVector();
    alive = true;
    mass = m;
  }
  
  void accelerate(PVector d, float amt) {
    d.normalize();
    mass -= amt * PREY_EFFICIENCY;
    acceleration = d.get();
    acceleration.div(mass);
    acceleration.mult(amt);
  }
  
  void update() {
    decide();
    move();
    render();
  }
  
  void move() {
    PVector prev_loc = location.get();
    velocity.add(acceleration);
    location.add(velocity);
    //edge collision
    if ((location.x >= width - rad) || (location.x <= rad)) {
      location.x = prev_loc.x;
      velocity.x = -velocity.x * EDGE_DAMP;
    }
    if ((location.y >= height - rad) || (location.y <= rad)) {
      location.y = prev_loc.y;
      velocity.y = -velocity.y * EDGE_DAMP;
    }
  }
  
  void render() {
    float rad = sqrt(mass);
    fill(#0000FF);
    ellipse(location.x, location.y, 2 * rad, 2 * rad);
  }
}
