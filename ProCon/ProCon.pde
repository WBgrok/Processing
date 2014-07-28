import processing.net.*; 
Client myClient; 
byte header[]={0,0,0,0};
int waitingfor=-1;

float xrot=0;
float xang=0;
float gravity=9.8;
float upaccel=0;
float groundPos=1.5;
float upPos=1.5;
float xoffset=0;
float yoffset=100;
float gotox=0;
float gotoy=200;
float assembled=1;
float lightlevel=255;
float fps=30;
float zoom=30;

void setup()
{ 
  size(600,400,P3D);
  noStroke();
  fill(204, 204);
  myClient=new Client(this,"192.168.1.141",2222); 
} 
 
void draw()
{ 
  String msg;
  int a,b,c,d,bytecount;
  byte[] bytebuffer=new byte[100];
  if(waitingfor==-1)
  {
    if(myClient.available()>3)
    { 
      a=myClient.read();
      b=myClient.read();
      c=myClient.read();
      d=myClient.read();
      waitingfor=a+(b*256)+(c*256*256)+(d*256*256*256);
    }
  } 
  else
  {
    if(myClient.available()>=waitingfor)
    { 
      bytecount=myClient.readBytesUntil(10,bytebuffer);
      msg=new String(bytebuffer,0,bytecount-1);
      domessage(msg);
      waitingfor=-1;
    }
  }
  background(255);
  ambientLight(lightlevel,lightlevel,lightlevel);
  if(xang>PI*2) xang=xang-PI*2;
  if(xang<0) xang=PI*2-xang;
  upPos=upPos-(upaccel/fps)+(gravity/fps);
  if(upPos>groundPos) upPos=groundPos;
  upaccel=upaccel-(gravity/fps);
  if(upaccel<0.1) upaccel=0;
  if(xoffset>gotox) xoffset-=1;
  if(xoffset<gotox) xoffset+=1;
  if(yoffset>gotoy) yoffset-=1;
  if(yoffset<gotoy) yoffset+=1;
  translate((width/2)+xoffset,height/2,yoffset);
  rotateY(xang);
  scale(zoom);
  puppet();
}

void puppet()
{
  pushMatrix();
  translate(0*assembled,(-1.594*assembled)+upPos,-0.050);
  fill(255,200,100);
  scale(0.0744,0.1063,0.0744);
  sphere(1);
  popMatrix();
  pushMatrix();
  translate(0*assembled,(-1.456*assembled)+upPos,-0.050);
  fill(255,200,100);
  scale(2,2,2);
  box(0.0638,0.0319,0.0414);
  popMatrix();
  pushMatrix();
  translate(-0.159*assembled,(-1.349*assembled)+upPos,-0.064);
  fill(255,0,0);
  scale(2,2,2);
  box(0.0638,0.0744,0.0638);
  popMatrix();
  pushMatrix();
  translate(0.159*assembled,(-1.349*assembled)+upPos,-0.064);
  fill(255,0,0);
  scale(2,2,2);
  box(0.0638,0.0744,0.0638);
  popMatrix();
  pushMatrix();
  translate(0*assembled,(-1.243*assembled)+upPos,-0.050);
  fill(255,0,0);
  scale(2,2,2);
  box(0.1275,0.1806,0.0829);
  popMatrix();
  pushMatrix();
  translate(0*assembled,(-0.956*assembled)+upPos,-0.050);
  fill(0,0,255);
  scale(2,2,2);
  box(0.1275,0.1063,0.0829);
  popMatrix();
  pushMatrix();
  translate(-0.181*assembled,(-1.169*assembled)+upPos,-0.064);
  fill(255,0,0);
  scale(2,2,2);
  box(0.0425,0.1063,0.0425);
  popMatrix();
  pushMatrix();
  translate(0.181*assembled,(-1.169*assembled)+upPos,-0.064);
  fill(255,0,0);
  scale(2,2,2);
  box(0.0425,0.1063,0.0425);
  popMatrix();
  pushMatrix();
  translate(-0.191*assembled,(-0.956*assembled)+upPos,-0.064);
  fill(255,200,100);
  scale(2,2,2);
  box(0.0319,0.1063,0.0319);
  popMatrix();
  pushMatrix();
  translate(0.191*assembled,(-0.956*assembled)+upPos,-0.064);
  fill(255,200,100);
  scale(2,2,2);
  box(0.0319,0.1063,0.0319);
  popMatrix();
  pushMatrix();
  translate(-0.064*assembled,(-0.637*assembled)+upPos,-0.064);
  fill(0,0,255);
  scale(2,2,2);
  box(0.0531,0.2125,0.0531);
  popMatrix();
  pushMatrix();
  translate(0.064*assembled,(-0.637*assembled)+upPos,-0.064);
  fill(0,0,255);
  scale(2,2,2);
  box(0.0531,0.2125,0.0531);
  popMatrix();
  pushMatrix();
  translate(-0.064*assembled,(-0.234*assembled)+upPos,-0.064);
  fill(255,200,100);
  scale(2,2,2);
  box(0.0425,0.1913,0.0425);
  popMatrix();
  pushMatrix();
  translate(0.064*assembled,(-0.234*assembled)+upPos,-0.064);
  fill(255,200,100);
  scale(2,2,2);
  box(0.0425,0.1913,0.0425);
  popMatrix();
  pushMatrix();
  translate(-0.064*assembled,(-0.021*assembled)+upPos,0);
  fill(255,0,0);
  scale(2,2,2);
  box(0.0531,0.0531,0.1063);
  popMatrix();
  pushMatrix();
  translate(0.064*assembled,(-0.021*assembled)+upPos,0);
  fill(255,0,0);
  scale(2,2,2);
  box(0.0531,0.0531,0.1063);
  popMatrix();
  pushMatrix();
  translate(-0.191*assembled,(-0.744*assembled)+upPos,-0.064);
  fill(255,200,100);
  scale(0.0106,0.1063,0.0319);
  sphere(1);
  popMatrix();
  pushMatrix();
  translate(0.191*assembled,(-0.744*assembled)+upPos,-0.064);
  fill(255,200,100);
  scale(0.0106,0.1063,0.0319);
  sphere(1);
  popMatrix();
}

void keyPressed()
{
  if(key == 'h')
  {
    header[0]=5;
    header[1]=0;
    header[2]=0;
    header[3]=0;
    myClient.write(header); 
    myClient.write("Hello"); 
  }
}

void domessage(String message)
{
  float X,Y,Z,A,S;
  if(message!=null)
  {
    message=trim(message);
    float[] sensor=float(split(message,','));
    switch(int(sensor[0]))
    {
      case 1:    // ACCEL
        X=sensor[1];
        Y=sensor[2];
        Z=sensor[3];
        break;
      case 2:    // COMPASS
        A=sensor[1];
        xang=(A/180)*PI;
        break;
      case 3:    // MOUSE
        X=sensor[1];
        Y=sensor[2];
        A=sensor[3];
        gotox=map(X,0,100,-300,300);
        gotoy=map(Y,0,100,-300,300);
        break;
      case 4:    // SLIDER
        S=sensor[1];
        assembled=map(S,0,100,0,3);
        break;
      case 5:    // BA
        upaccel=15;
        break;
      case 6:    // BB
        break;
      case 7:    // BC
        break;
      case 8:    // BD
        xrot=0;
        xang=0;
        gravity=9.8;
        upaccel=0;
        upPos=1.5;
        xoffset=0;
        yoffset=0;
        assembled=1;
        zoom=30;
        break;
    }
  }
}


