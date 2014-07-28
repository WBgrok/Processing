class Liner {
  PVector location;
  PVector velocity;
  PVector acceleration;
  color col;

  Liner(PVector l, PVector v, color c) {
    location = l.get();
    velocity = v.get();
    col = c;
    acceleration = new PVector(0, 0);
  }

  void push(PVector f) {
    acceleration.add(f);
  }

  boolean update() {
    float c;
    if (acceleration.mag() > ACC_MAX) {
      return true;
    }
    PVector newLoc = location.get();
    PVector newVel = velocity.get();
    newVel.add(acceleration);
    newLoc.add(velocity);

    if (newLoc.x >= width || newLoc.x < 0 || newLoc.y >= height || newLoc.y < 0) {
      return true;
    }
    
    PVector dir = velocity.get();
    dir.normalize();
    if (dir.y >= 0) {
      c = acos(dir.x);
    } else {
      c = TWO_PI-acos(dir.x);
    }
    strokeWeight(max(8- (2*velocity.mag()), 1));
    stroke(color(c,1,1));
    linerBezier(location, velocity, newLoc, newVel);
    location = newLoc;
    velocity = newVel;
    acceleration.x = 0;
    acceleration.y = 0;
    return false;
  }
}

