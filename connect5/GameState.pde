class GameState {
  int s; // size of the grid
  int nxtPlayer; // 1 or 2
  ArrayList<int[]> moves; // lmove history
  ArrayList<int[]> p1; // player one's pieces
  ArrayList<int[]> p2; // player two's pieces
  int[][] board;
  
  GameState(int sze) {
    s=sze;
    nxtPlayer = 1;
    moves = new ArrayList<int[]>(s * s);
    p1 = new ArrayList<int[]>();
    p2 = new ArrayList<int[]>();
    board = new int[s][s];
    for (int i = 0; i < s; i++) {
      for (int j = 0; j <s; j++) {
        board[i][j] = 0;
      }
    }
  }
  
  ArrayList<int[]> getMoves() {
    return moves;
  }
  
  ArrayList<int[]> getP1() {
    return p1;
  }
  
  ArrayList<int[]> getP2() {
    return p2;
  }
  
  int[][] getBoard() {
    return board;
  }
  
  void printMoves() {
    print("Moves played:");
    Iterator<int[]> it = moves.iterator();
    while (it.hasNext()) {
      int[] pos = it.next();
      print("(" + pos[0] + "," + pos[1] + ") ");
    }
    println();
  }
  
  void printBoard() {
    for (int j = 0; j< s; j++) {
      for (int i = 0; i <s; i++) {
        print(board[i][j]);
      }
      println(" | " + j);
    }
  }
    
  
  // TODO: return state?
  void play(int xp, int yp, Connect5Renderer renderer) {
    
    if (xp < 0 || xp >= s || yp < 0 || yp >= s) {
      println("Outside the grid");
      return;
    }
    
    if (board[xp][yp] != 0) {
      println("Square taken");
      return;
    }
    int[] pos = {xp,yp};
    moves.add(pos);
    board[xp][yp] = nxtPlayer;
    renderer.square(nxtPlayer,xp, yp);
    
    if (nxtPlayer == 1) {
      p1.add(pos);
      nxtPlayer = 2;
    } else {
      p2.add(pos);
      nxtPlayer = 1;
    }
//    printMoves();
//    printBoard();
  }
}
