class Liner {
  PVector location;
  PVector velocity;
  PVector acceleration;

  Liner(PVector l, PVector v) {
    println("spawning " + this);
    location = l.get();
    velocity = v.get();
    acceleration = new PVector(0, 0);
  }

  void push(PVector f) {
    acceleration.add(f);
  }

  void update() {
    PVector newLoc = location.get();
    PVector newVel = velocity.get();
    newVel.add(acceleration);
    newLoc.add(velocity);

    if (newLoc.x >= width || newLoc.x < 0) {
      newLoc.x = location.x;
      newVel.x = -newVel.x * EDGE_DAMP;
    }
    if (newLoc.y >= height || newLoc.y < 0) {
      newLoc.y = location.y;
      newVel.y = -newVel.y * EDGE_DAMP;
    }

    color c1 = data[floor(location.x)][floor(location.y)];
    color c2 = data[floor(newLoc.x)][floor(newLoc.y)];
    strokeWeight(max(STROKE_FAC / (newVel.mag() + 1),1));   
    stroke(avg(c1, c2));
    linerBezier(location, velocity, newLoc, newVel);
    location = newLoc;
    velocity = newVel;
    acceleration.x = 0;
    acceleration.y = 0;
  }
}
