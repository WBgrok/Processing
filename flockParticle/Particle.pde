// class for particles
class Particle {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float rad;
  color col;

  Particle(PVector l, PVector v, float r) {
    location = l.get();
    acceleration = new PVector();
    velocity = v.get();
    rad = r;
  }

  // set acceleration
  void push(PVector f) {
    acceleration = f.get();
  }

  void resetAcc() {
    acceleration = new PVector(0, 0);
  }

  void update() {
    PVector prev_loc = location.get();
    PVector friction = velocity.get();
    friction.mult(-fricFac * velocity.mag());
    acceleration.add(friction);
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
    // colour represents the energy of the ball
    col = colMapValue(sq(velocity.mag()) / 2);
  }

  void display() {
    fill(col);
    ellipse(location.x, location.y, 2 * rad, 2 * rad );
  }
}
