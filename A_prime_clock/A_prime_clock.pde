// Concentric arcs spins at different speeds (slower for outward arcs), each using
// a prime factor to base speed. As the counter moves through number, arcs move in and out of sync.
// eg at 30 = 2 * 3 * 5, the inmost three arcs line up, again at 60, etc.
// sync of larger numbers of arcs are more rare...


// CONSTANTS
int SZE = 15; // arc thickness
int period = 40; // framecount of one base rotation

// globals
int i = 0;
ArrayList<Arc> arcs;

void setup() {
  size(400, 400);
  background(#000000);
  arcs = new ArrayList<Arc>();
  // populate this by hand with primes from 2 to 31
   arcs.add(new Arc(2, SZE, new PVector(200, 200), 0, 2));
  arcs.add(new Arc(3, SZE, new PVector(200, 200), 0, 3));
  arcs.add(new Arc(5, SZE, new PVector(200, 200), 0, 4));
  arcs.add(new Arc(7, SZE, new PVector(200, 200), 0, 5));
  arcs.add(new Arc(11, SZE, new PVector(200, 200), 0, 6));
  arcs.add(new Arc(13, SZE, new PVector(200, 200), 0, 7));
  arcs.add(new Arc(17, SZE, new PVector(200, 200), 0, 8));
  arcs.add(new Arc(19, SZE, new PVector(200, 200), 0, 9));
  arcs.add(new Arc(23, SZE, new PVector(200, 200), 0, 10));
  arcs.add(new Arc(29, SZE, new PVector(200, 200), 0, 11));
  arcs.add(new Arc(31, SZE, new PVector(200, 200), 0, 12));
  
  // this can work perfectly well with non primes, they will in fact line up more often - substitue the below to see
  
//  arcs.add(new Arc(2, SZE, new PVector(200, 200), 0, 2));
//  arcs.add(new Arc(3, SZE, new PVector(200, 200), 0, 3));
//  arcs.add(new Arc(4, SZE, new PVector(200, 200), 0, 4));
//  arcs.add(new Arc(5, SZE, new PVector(200, 200), 0, 5));
//  arcs.add(new Arc(6, SZE, new PVector(200, 200), 0, 6));
//  arcs.add(new Arc(7, SZE, new PVector(200, 200), 0, 7));
//  arcs.add(new Arc(8, SZE, new PVector(200, 200), 0, 8));
//  arcs.add(new Arc(9, SZE, new PVector(200, 200), 0, 9));
//  arcs.add(new Arc(10, SZE, new PVector(200, 200), 0, 10));
//  arcs.add(new Arc(11, SZE, new PVector(200, 200), 0, 11));
//  arcs.add(new Arc(12, SZE, new PVector(200, 200), 0, 12));
}

void draw() {
  background(#000000);
  float angle = (i  * TWO_PI / period);
  // that for syntax allows us to iterate within an ArrayList (here arcs)
  // using an iterator variable of the class the ArrayList contains (here, variable a of class Arc)
  // This is the way to do that, instead of using the Java Iterator class, deprecated since Processing 2.0 :D
  for (Arc a : arcs) {
    a.render(angle);
  }
  int cnt = i / period;
  fill(TWO_PI, 0, TWO_PI);
  textSize(10);
  text(cnt, 0, 10);
  i++;
}

