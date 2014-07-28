// a chunk of lined up pieces
class Chunk {
  int owner; // player 1 or 2
  
  // direction: Horizontal is 1, downwards 2, vertical 3, upwards 4
  int dir;
  
  // start/ finish coordinates. 
  // start is always W/NW/N/NE-most
  int[] st;
  int[] fn;
  int lgth;
  
  // can the chunk be extended towards the start/end
  boolean freeStart;
  boolean freeEnd;
  
  Chunk(int o, int d, int[] s, int[] f,  int l; boolean fS, boolean fE) {
    owner = o;
    dir = d;
    st[0] = s[0]
    st[1] = s[1];
    fn[0] = f[0];
    fn[1] = f[1];
    lgth = l;
    freeStart = fs;
    freeEnd = fe;
  }

}


  
