import processing.video.*;
Capture myCapture;


PImage shot1,shot2;
boolean first_shot, disp_result;

void setup() 
{
  size(640, 480);

  // The name of the capture device is dependent those
  // plugged into the computer. To get a list of the 
  // choices, uncomment the following line 
  // println(Capture.list());
  // And to specify the camera, replace "Camera Name" 
  // in the next line with one from Capture.list()
  // myCapture = new Capture(this, width, height, "Camera Name", 30);
  
  // This code will try to use the last device used
  // by a QuickTime program
  myCapture = new Capture(this, width/2, height/2, 30);
  shot1 = createImage(width/2, height/2,RGB);
  shot2 = createImage(width/2, height/2,RGB);
  first_shot = true;
  disp_result = false;
  background(0);
}

void captureEvent(Capture myCapture) {
  myCapture.read();
//  myCapture.filter(GRAY);
}

void keyPressed() {
  if(first_shot) {
    // take the first shot
    println("1!");
//      shot1 = myCapture;
    shot1.copy(myCapture, 0, 0,width/2, height/2, 0, 0,width/2, height/2);
    first_shot = false;
    return;
  } else if (!disp_result) {
    //take the second shot and display the result
    println("2!");
    shot2.copy(myCapture, 0, 0,width/2, height/2, 0, 0,width/2, height/2);
    disp_result = true;
    return;
  }
 // if we reach this point, we are after a full cycle, reset everything
  shot1 = createImage(width/2, height/2,RGB);
  shot2 = createImage(width/2, height/2,RGB);
  first_shot = true;
  disp_result = false;
  background(0);
//  image(shot1, width/2, 0);
//  image(shot2, 0, height/2);
  loop();
  }

void draw() {
  image(myCapture, 0, 0);
  if (!first_shot) {
    image(shot1, width/2, 0);
  } 
  if (disp_result) {
    image(shot2, 0, height/2);
    PImage i = blender (shot1,shot2, DIFFERENCE);
    // shot2.filter(THRESHOLD, 0.5);
    image(i, width/2, height/2);
    noLoop();
  }
  
}

PImage blender(PImage img1, PImage img2, int mode) {
  PImage c = createImage(img1.width, img1.height, RGB);
  c.copy(img1, 0, 0, img1.width, img1.height, 0, 0, img1.width, img1.height);
  c.blend(img2, 0, 0, img1.width, img1.height, 0, 0, img1.width, img1.height, mode);
  return c;
}
