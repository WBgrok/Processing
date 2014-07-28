// group of particles
class ParticleSystem {
  int w;
  int h;
  int[][] map;
  float init_size;
  float prox;
  ArrayList<Particle> particles;
  //PVector origin;


  ParticleSystem(float s, float p, int wd, int ht) {
    //origin = location.get();
    particles = new ArrayList<Particle>();
    w = wd;
    h = ht;
    map = new int[w][h];
    for (int y = 0; y < h/2; y++) {
      for (int x = 0; x < w/2; x++) {
        map[x][y] = -1;
      }
    }
    init_size = s;
    prox = p;
  }

  void addParticle(PVector origin) {
    particles.add(new Particle(origin, init_size));
  }

  // detects collisions and feeds p.
  void feed(Particle p) {
    ArrayList<Particle> absorb = new ArrayList<Particle>();
    Particle q;
    int n;

    // populate the list of particles to absorb - if we have particles in range below and to the right
    // of p, add each unique particle to the list
    for (int i = floor(p.location.x); i < ceil(p.location.x + p.rad + prox); i++) {
      for (int j = floor(p.location.x); j < ceil(p.location.x + p.rad + prox); j++) {
        // println("updating i = " + i + " j = " + j);
        n = map[i][j];
        if (n != -1) {
          q = particles.get(n);
          if (absorb.indexOf(q) == -1) {
            absorb.add(q);
          }
        }
      }
    }
    // now absorb all the qs
    Iterator<Particle> it = absorb.iterator();
    while (it.hasNext ()) {
      q = it.next();
      p.absorb(q);
    }
  }



  void run() {
    Iterator<Particle> it = particles.iterator();
    // first pass: alive particles are updated, and absorb neighbours
    while (it.hasNext ()) {
      Particle p = it.next();
      if (p.alive) {
        feed(p);
        p.update();
        p.display();
      }
    }
    //second pass - remove dead particles
    it = particles.iterator();
    while (it.hasNext ()) {
      Particle p = it.next();
      if (!p.alive) {
        it.remove();
      }
    }

    //reset map
    for (int y = 0; y < h/2; y++) {
      for (int x = 0; x < w/2; x++) {
        map[x][y] = -1;
      }
    }


    //third pass - update map (the indices have changed)
    it = particles.iterator();
    while (it.hasNext ()) {
      Particle p = it.next();
      for (int i = min(floor(p.location.x - p.rad),0); i < max(ceil(p.location.x + p.rad),w); i++) {
        for (int j = min(floor(p.location.y - p.rad),0); j < max(ceil(p.location.y + p.rad),h); j++) {
          int n = particles.indexOf(p);
          // println("updating i = " + i + " j = " + j);
          map[i][j] = n;
        }
      }
    } 
  }
}

