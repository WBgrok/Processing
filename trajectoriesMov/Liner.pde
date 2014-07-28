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
 
    stroke(col);
    linerBezier(location, velocity, newLoc, newVel);
    location = newLoc;
    velocity = newVel;
    acceleration.x = 0;
    acceleration.y = 0;
    return false;
  }
}
