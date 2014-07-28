class Connect5Renderer {
  int s; //size in squares
  int squareSize;
  color p1Col;
  color p2Col;
  color bgCol;
  color gridCol;
  
  Connect5Renderer(int sze, int sqSze, color p1, color p2, color bg, color grd) {
    s=sze;
    squareSize=sqSze;
    p1Col=p1;
    p2Col=p2;
    bgCol=bg;
    gridCol=grd;
    
    size(s * (squareSize + 1) + 1,s * (squareSize + 1) + 1);
    background(bgCol);
    noFill();
    stroke(gridCol);
    rect(0,0,width-1,height-1);
    for(int i = 1; i < s; i++) {
      line(i * (squareSize + 1), 0, i * (squareSize + 1), height - 1);
      line(0, i * (squareSize + 1),width - 1, i * (squareSize + 1));
    }
  }
  
  void square(int player, int xs, int ys) {
    if (player == 1) {
      fill(p1Col);
    } else if (player==2) {
      fill(p2Col);
    } else {
      println("Unknown player");
      return;
    }
    
    if (xs < 0 || xs >= s || ys < 0 || ys >= s) {
      println("Outside the grid");
      return;
    }
    
    stroke(gridCol);
    rect(xs * (squareSize + 1), ys * (squareSize + 1), squareSize + 1, squareSize + 1);
    
  }
    
    // TODO: render
}

