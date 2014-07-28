class Predator {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float mass;
  boolean alive;
  string type;
  
  Predator(PVector l, float m, string type) {
    location = l.get;
    acceleration = new PVector();
    velocity = new PVector();
    alive = true;
    mass = m;
  }
  
  void accelerate(PVector d, float amt) {
    d.normalize();
    mass -= amt * PRED_EFFICIENCY;
    acceleration = d.get();
    acceleration.div(mass);
    acceleration.mult(amt);
    fill(#FFFFFF);
     ellipse(location.x, location.y, 2 * rad, 2 * rad );
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
    acceleration = new PVector(0,0);
  }
  
  void render() {
    float rad = sqrt(mass);
    fill(#FF0000);
     ellipse(location.x, location.y, 2 * rad, 2 * rad );
  }
  
  // deciding how to move
  void decide() {
    // find the closest prey
    Iterator<Prey> it = preys.iterator();
    Prey myPrey;
    float minDist = width * height
    
    while (it.hasNext ()) {
      Prey p = it.next();
      dst = dist(location, p.location);
      if (dst < minDist) {
        minDist = dst;
        myPrey = p;
      }
    }
    if minDist < sqrt(myPrey.mass) + sqrt(mass) {
      eat(myPrey);
    } else {
      target(myPrey);
    }
  }
  
  void eat(Prey p) {
  
  void target(Prey p) {
    switch(type) {
      case 'BASIC':
        targetBasic(p);
        break;
    }
  }
  
  void targetBasic
  
}

