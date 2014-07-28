// class for particles
class Particle {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float rad;
  float eng;
  color col;
  boolean alive;

  Particle(PVector l, float r) {
    location = l.get();
    acceleration = new PVector();
    velocity = new PVector();
    alive = true;
    rad = r;
  }

  // update acceleration
  void push(PVector f) {
    PVector a = f.get();
    a.div(sq(rad));
    acceleration.add(a);
  }

  void resetAcc() {
    acceleration = new PVector(0, 0, 0);
  }

  void update() {
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
    eng = sq(velocity.mag()) * sq(rad);
    // colour represents the energy of the ball
    col = colMapValue(velocity.mag() * sq(rad) * 0.0511);
  }

  // merge another Particle into the current one
  void absorb(Particle p) {
    if (p.alive && p!=this) {
      rad = sqrt(sq(rad) + sq(p.rad));
      location.add(p.location);
      location.div(2);
      velocity.mult(sq(rad));
      p.velocity.mult(sq(p.rad));
      velocity.add(p.velocity);
      velocity.div(sq(rad) + sq(p.rad));
      p.alive = false;
      println("Energy="+(velocity.mag() * sq(rad)));
    }
  }

  // bounce the particle against another
  void bounce(Particle p) {
    PVector tmp = p.velocity.get();
    p.velocity = velocity.get();
    p.velocity.mult(EDGE_DAMP * sq(rad) / sq(p.rad));
    velocity = tmp;
    velocity.mult(EDGE_DAMP * sq(p.rad) / sq(rad));
    rad *= BOUNCE_SHED;
    p.rad *= BOUNCE_SHED;
  }

  void display() {
    fill(col);
    ellipse(location.x, location.y, 2 * rad, 2 * rad );
  }
}

