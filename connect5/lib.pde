// Prints a list of positions
void printPos(ArrayList<int[]>  moves) {
  print("Positions:");
  Iterator<int[]> it = moves.iterator();
  while (it.hasNext ()) {
    int[] pos = it.next();
    print("(" + pos[0] + "," + pos[1] + ") ");
  }
  println();
}

// Returns true if a position is in a list
boolean posInList(ArrayList<int[]> lst, int[] pos) {
  Iterator<int[]> it = lst.iterator();
  while (it.hasNext ()) {
    int[] item = it.next();
    if (item[0] == pos[0] && item[1] == pos[1]) {
      return true;
    }
  }
  return false;
}



// Returns an ArrayList of free neighbouring spaces when given a list of spaces)
ArrayList<int[]> neighbourSpaces(ArrayList<int[]> occupiedSpaces, int size) {

  ArrayList<int[]> neighbours = new ArrayList<int[]>();

  // For each of the occupied spaces
  Iterator<int[]> main = occupiedSpaces.iterator();
  while (main.hasNext ()) {
    int[] occPos = main.next();
    // North
    if (occPos[1] > 0) {
      // NW 
      if (occPos[0] > 0) {
        int[] NW = {
          occPos[0] - 1, occPos[1] -1
        };
        // check the candidate isn't occupied or already listed
        if (!posInList(occupiedSpaces, NW) && !posInList(neighbours, NW)) {
          neighbours.add(NW);
        }
      }
      // N
      int[] N = {
        occPos[0], occPos[1] -1
      };
      // check the candidate isn't occupied or already listed
      if (!posInList(occupiedSpaces, N) && !posInList(neighbours, N)) {
        neighbours.add(N);
      }
      // NE
      if (occPos[1] < size - 1) {
        int[] NE = {
          occPos[0] + 1, occPos[1] -1
        };
        // check the candidate isn't occupied or already listed
        if (!posInList(occupiedSpaces, NE) && !posInList(neighbours, NE)) {
          neighbours.add(NE);
        }
      }
    }
    // W
    if (occPos[0] > 0) {
      int[] W  = {
        occPos[0] - 1, occPos[1]
      };
      // check the candidate isn't occupied or already listed
      if (!posInList(occupiedSpaces, W) && !posInList(neighbours, W)) {
        neighbours.add(W);
      }
    }
    // E
    if (occPos[0] < size - 1) {
      int[] E = {
        occPos[0] + 1, occPos[1]
      };
      // check the candidate isn't occupied or already listed
      if (!posInList(occupiedSpaces, E) && !posInList(neighbours, E)) {
        neighbours.add(E);
      }
    }
    // South
    if (occPos[1] < size -1) {
      // SW
      if (occPos[0] > 0) {
        int[] SW = {
          occPos[0] - 1, occPos[1] + 1
        };
        // check the candidate isn't occupied or already listed
        if (!posInList(occupiedSpaces, SW) && !posInList(neighbours, SW)) {
          neighbours.add(SW);
        }
      }
      // S
      int[] S = {
        occPos[0], occPos[1] + 1
      };
      // check the candidate isn't occupied or already listed
      if (!posInList(occupiedSpaces, S) && !posInList(neighbours, S)) {
        neighbours.add(S);
      }
      // SE
      if (occPos[1] < size - 1) {
        int[] SE = {
          occPos[0] + 1, occPos[1] + 1
        };
        // check the candidate isn't occupied or already listed
        if (!posInList(occupiedSpaces, SE) && !posInList(neighbours, SE)) {
          neighbours.add(SE);
        }
      }
    }
  }
  return neighbours;
}

//Get all chunks a given position belongs to for a particular player
ArrayList<Chunk> getChunks(int[] pos, int player, int[][]board, int sze) {

  ArrayList<Chunk> chunks = new ArrayList<Chunk>(4);
  Chunk chunk;

  int[] look = {
    pos[0], pos[1]
  };
  int[] st = new int[2];
  int[] en = new int[2];
  int cnt = 1;
  boolean fS = false;
  boolean fE = false;
  // West
  while ( (look[0] > 0) && (board[look[0]-1][look[1]] == player)) {
    // there's a piece belonging to the player to the east
    cnt++;
    look[0]--;
  }
  st[0]=look[0];
  st[1]=look[1];
  // we've reached either a free space or an opposite piece
  if ((look[0] > 0) && (board[look[0]-1][look[1]] == 0)) {
    fS = true;
  }
  // now move east
  look[0] = pos[0];
  look[1] = pos[1];
  while ( (look[0] < sze - 1) && (board[look[0]+1][look[1] == player)) {
    cnt++;
    look[0]++;
  }
  // we've reached either a free space or an opposite piece
  if ((look[0] < sze - 1) && (board[look[0]+1][look[1]] == 0)) {
    fE = true;
  }
  if (cnt > 1) { 
    chunk = new Chunk(player, 1, st, en, cnt, fS, fE);
    chunks.add(chunk);
  }
  //NW
  cnt = 1;
  fS = false;
  fE = false;
  look[0] = pos[0];
  look[1] = pos[1];
  while ( (look[0]) > 0) && (look[1] > 0) && (board[look[0]-1][look[1]-1] == player) ) {
    cnt++;
    look[0]--;
    look[1]--;
  }
  // reached NW-most edge
  if ( (look[0]) > 0) && (look[1] > 0) && (board[look[0]-1][look[1]-1] == 0) ) {
    fS = true;
  }
  look[0] = pos[0];
  look[1] = pos[1];
  while ( (look[0] < sze -1) && (look[1] < sze -1) && (board[look[0]+1][look[1]+1] == player) ) {
    cnt++;
    look[0]++;
    look[1]++;
  }
  //check free end
  // add chunk
  //North
  // North-East
}

