  package rita;

import java.io.IOException;
import java.io.InputStream;

import java.util.*;
import java.util.regex.Pattern;

import processing.core.*;

import rita.render.*;
import rita.support.*;

import static rita.support.Constants.EventType.*;

/**
 * RiTa's text display object. Wraps an instance of RiString to provide utility
 * methods for typography and animation.
 *
 * @example RiTextMoveTo
 */
public class RiText implements RiTextIF
{
  static { RiTa.init(); }
  
  public static final Defaults defaults = new Defaults();
  
  public static final RiText[] EMPTY_ARRAY = new RiText[0];
 
  // Statics ============================================================

  protected static boolean DBUG_INFO = false;

  public static final List<RiText> instances = new ArrayList<RiText>();
  protected static boolean behaviorWarningsDisabled;
  public static boolean callbacksDisabled = false;
  
  protected static Boolean rendering3D;
  public static Map fonts;

  // Members ============================================================

  public int motionType;
    
  public float scaleX = 1, scaleY = 1, scaleZ = 1;

  public float rotateX, rotateY, rotateZ;

  public List behaviors;

  /* Current delegate for this text */
  protected RiString text;

  /**
   * Current x-position of this text
   */
  public float x;

  /**
   * Current y-position of this text
   */
  public float y;

  /**
   * Current z-position of this text
   */
  public float z;

  protected float fillR = defaults.fill[0], fillG = defaults.fill[1];
  protected float fillB = defaults.fill[2], fillA = defaults.fill[3];
  protected float bbStrokeR, bbStrokeG, bbStrokeB, bbStrokeA = 255;
  protected float bbFillR, bbFillG, bbFillB, bbFillA = 0;
  protected Rect boundingBox, screenBoundingBox;
  
  protected float fontSize, bbStrokeWeight;
  protected boolean boundingBoxVisible;
  protected int alignment;
  
  public PApplet pApplet;
    
  protected PFont font;
  protected RiText textToCopy;
  protected boolean hidden, autodraw;
  
  public RiText(PApplet pApplet)
  {
    this(pApplet, E);
  }

  public RiText(PApplet pApplet, String text)
  {
    this(pApplet, text, Float.MIN_VALUE, Float.MIN_VALUE);
  }

  public RiText(PApplet pApplet, char character)
  {
    this(pApplet, Character.toString(character), Float.MIN_VALUE, Float.MIN_VALUE, defaults.alignment);
  }

  public RiText(PApplet pApplet, float startXPos, float startYPos)
  {
    this(pApplet, E, startXPos, startYPos, defaults.alignment);
  }

  public RiText(PApplet pApplet, char character, float startXPos, float startYPos)
  {
    this(pApplet, Character.toString(character), startXPos, startYPos, defaults.alignment);
  }

  public RiText(PApplet pApplet, String text, float startXPos, float startYPos)
  {
    this(pApplet, text, startXPos, startYPos, defaults.alignment);
  }

  public RiText(PApplet pApplet, String text, float startXPos, float startYPos, PFont font)
  {
    this(pApplet, text, startXPos, startYPos, defaults.alignment, font);
  }

  public RiText(PApplet pApplet, String text, float xPos, float yPos, int alignment)
  {
    this(pApplet, text, xPos, yPos, alignment, null);
  }

  /**
   * Creates a new RiText object base-aligned at x='xPos', y='yPos', with
   * 'alignment' from one of (LEFT, CENTER, RIGHT), using font specified by
   * 'theFont'.
   */
  public RiText(PApplet pApplet, String text, float xPos, float yPos, int alignment, PFont theFont)
  {
    this.pApplet = pApplet;
    this.setDefaults();
    if (theFont != null) {
      this.font = theFont;
      this.fontSize = theFont.getSize();
    }
    this.text(text);
    this.registerInstance(pApplet);
    this.textMode(alignment);
    this.verifyFont();
    this.x = (xPos == Float.MIN_VALUE) ? screenCenterX() : xPos;
    this.y = (yPos == Float.MIN_VALUE) ? screenCenterY() : yPos;    
    
    //System.out.println("RiText.boundingBoxVisible="+boundingBoxVisible);
  }

  protected void setDefaults()
  {
    this.alignment = defaults.alignment;
    this.motionType = defaults.motionType;
    this.boundingBoxVisible = defaults.showBounds;
    this.bbStrokeWeight = defaults.boundingStrokeWeight;
    this.boundingBoxStroke(defaults.boundingStroke);
    this.fontSize = defaults.fontSize;
  }

  static boolean msgNullPAppletRegisterInstance;

  protected void registerInstance(PApplet p)
  {
    instances.add(this);
    
    if (p == null)
    {
      if (!msgNullPAppletRegisterInstance)
      {
        System.err.println("[WARN] Null PApplet passed to RiText(PApplet...)");
        msgNullPAppletRegisterInstance = true;
      }
      return;
    }

    p.smooth(); // for clean fonts
  }
  
  protected void verifyFont()
  {
    if (pApplet == null) return;
    
    if (this.font == null) {
      this.font = defaultFont(pApplet);
      this.fontSize = this.font.getSize();
    }

    //System.out.println("RiText.verifyFont() -> "+font+"/"+fontSize);
    pApplet.textFont(font);
    if (this.fontSize > 0)
      pApplet.textSize(fontSize);
  }

  /**
   * Returns the point representing the center of the RiText
   */
  public float[] center()
  {
    float[] bb = this.boundingBox();
    return new float[] { bb[0] + bb[2]/2f, bb[1] - bb[3]/2f };
    
    //return new float[] { x + textWidth() / 2f, y - textHeight() / 2f};
  }
  
  public static final PFont defaultFont(PApplet p)
  {
    //System.out.println("_defaultFont("+CREATE_FONT+")");
/*    PFont pf = checkFontCache(defaults.fontFamily, defaults.fontSize); 
    if (pf == null)
    {*/
      PFont pf = defaults.font;
      if (pf == null) {
        
        if (defaults.fontFamily.endsWith(".vlw")) {
          defaults.fontSize = -1;
          pf = _loadFont(p, defaults.fontFamily, defaults.fontSize);
        }
        else
          pf = _createFont(p, defaults.fontFamily, defaults.fontSize);
        
        if (pf != null) 
        {
          RiText.defaults.font = pf;
          RiText.defaults.fontSize = pf.getSize();
        }
        else {
          String msg = "Unable to find font " + "with name='" + defaults.font + "'";
          if (defaults.fontSize > -1)
            msg += " and size=" + defaults.fontSize;
          throw new RiTaException(msg);
        }
      }           
    //}
    return pf;
  }
  
  public static final void defaultFont(PFont font) {  
    defaults.fontFamily = font.getName();
    defaults.fontSize = font.getSize();
    defaults.font = font; 
  } 
  
  public static final void defaultFont(String name) { 
    defaults.fontFamily = name;
    defaults.font = null; 
  }
  
  public static final void defaultFont(String name, int size) { 
    defaults.fontFamily = name;
    defaults.fontSize = size;
    defaults.font = null;
  }
  
  public static float[] defaultFill()
  {
    return defaults.fill;
  }
  
  public static void defaultFill(float r, float g, float b, float alpha)
  {
    defaults.fill[0] = r;
    defaults.fill[1] = g;
    defaults.fill[2] = b;
    defaults.fill[3] = alpha;
  }

  public static void defaultFill(float gray)
  {
    defaultFill(gray, gray, gray, 255);
  }

  public static void defaultFill(float gray, float alpha)
  {
    defaultFill(gray, gray, gray, alpha);
  }

  public static void defaultFill(float r, float g, float b)
  {
    defaultFill(r, g, b, 255);
  }

  private float screenCenterX()
  {
    return (pApplet != null) ? screenCenterX(pApplet.g) : -1;
  }

  private float screenCenterX(PGraphics p)
  {
    if (p == null) return -1;
    float cx = p.width / 2;
    if (alignment == LEFT)
      cx -= (textWidth() / 2f);
    else if (alignment == RIGHT)
      cx += (textWidth() / 2f);
    return cx;
  }

  private float screenCenterY()
  {
    return (pApplet != null) ? pApplet.height / 2 : -1;
  }

  public float boundingBoxStrokeWeight()
  {
    return bbStrokeWeight;
  }

  // ------------------------- Colors -----------------------------
  
  /**
   * Sets the text fill color according to a single hex number.
   */
  public RiTextIF fillHex(int hexColor)
  {
    this.fill(unhex(hexColor));
    return this;

  }

  protected static final float[] unhex(int hexColor){
    // note: not handling alphas...
    int r = hexColor >> 16;
    int temp = hexColor ^ r << 16;
    int g = temp >> 8;
    int b = temp ^ g << 8;
    return new float[]{r,g,b,255};
  }
  

  /**
   * Set the text color for this object
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param a
   *          alpha component (0-255)
   */
  public RiTextIF fill(float r, float g, float b, float alpha)
  {
    this.fillR = r;
    this.fillG = g;
    this.fillB = b;
    this.fillA = alpha;
    
    return this;
  }

  public RiTextIF fill(float g)
  {
    this.fillR = g;
    this.fillG = g;
    this.fillB = g;
    
    return this;
  }
  
  public RiTextIF fill(float g, float a)
  {
    return this.fill(g, g, g, a);
  }

  public RiTextIF fill(float r, float g, float b)
  {
    this.fillR = r;
    this.fillG = g;
    this.fillB = b;
    return this;
  }

  /**
   * Set the text color for this object (r,g,b,a) from 0-255
   */
  public RiTextIF fill(float[] color)
  {
    float r = color[0], g = 0, b = 0, a = fillA;
    switch (color.length)
    {
      case 4:
        g = color[1];
        b = color[2];
        a = color[3];
        break;
      case 3:
        g = color[1];
        b = color[2];
        break;
      case 2:
        g = color[0];
        b = color[0];
        a = color[1];
        break;
    }
    return this.fill(r, g, b, a);
  }

  /**
   * Set the bounding-box (or background) color for this object
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param alpha
   *          transparency (0-255)

   */
  public RiTextIF boundingBoxFill(float r, float g, float b, float alpha)
  {
    this.bbFillR = r;
    this.bbFillG = g;
    this.bbFillB = b;
    this.bbFillA = alpha;
    return this;

  }

  /**
   * Set the current boundingBoxFill color for this object, applicable only when
   * <code>showBoundingBox(true)</code> has been called.
   */
  public RiTextIF boundingBoxFill(float[] color)
  {
    bbFillR = color[0];
    bbFillG = color[1];
    bbFillB = color[2];
    bbFillA = 255;
    if (color.length > 3)
      this.bbFillA = color[3];
    return this;

  }

  public RiTextIF boundingBoxFill(float gray)
  {
    return this.boundingBoxFill(gray, gray, gray, 255);
  }

  public RiTextIF boundingBoxFill(float gray, float alpha)
  {
    return this.boundingBoxFill(gray, gray, gray, alpha);
  }

  public RiTextIF boundingBoxFill(float r, float g, float b)
  {
    return this.boundingBoxFill(r, g, b, 255);
  }

  /**
   * Set the stroke color for the bounding-box of this object, assuming it has
   * been set to visible.
   * 
   * @param r
   *          red component (0-255)
   * @param g
   *          green component (0-255)
   * @param b
   *          blue component (0-255)
   * @param alpha
   *          transparency (0-255)
   */
  public RiTextIF boundingBoxStroke(float r, float g, float b, float alpha)
  {
    this.bbStrokeR = r;
    this.bbStrokeG = g;
    this.bbStrokeB = b;
    this.bbStrokeA = alpha;
    return this;
  }

  public RiTextIF boundingBoxStroke(float gray)
  {
    return this.boundingBoxStroke(gray, gray, gray, 255);
  }

  public RiTextIF boundingBoxStroke(float gray, float alpha)
  {
    return this.boundingBoxStroke(gray, gray, gray, alpha);
  }

  public RiTextIF boundingBoxStroke(float r, float g, float b)
  {
    return this.boundingBoxStroke(r, g, b, 255);
  }

  /**
   * Returns the current text color for this object
   */
  public float[] fill()
  { // yuck
    return new float[] { fillR, fillG, fillB, fillA };
  }

  /**
   * Returns the current bounding box fill color for this object
   */
  public float[] boundingBoxFill()
  { // yuck
    return new float[] { bbFillR, bbFillG, bbFillB, bbFillA };
  }

  /**
   * Returns the current bounding box stroke color for this object
   * 

   */
  public float[] boundingBoxStroke()
  { // yuck
    return new float[] { bbStrokeR, bbStrokeG, bbStrokeB, bbStrokeA };
  }

  /**
   * Set the current boundingBoxStroke color for this object, applicable only
   * when <code>showBoundingBox(true)</code> has been called.
   */
  public RiTextIF boundingBoxStroke(float[] color)
  {
    bbStrokeR = color[0];
    bbStrokeG = color[1];
    bbStrokeB = color[2];
    bbStrokeA = 255;
    if (color.length == 4)
      this.bbStrokeA = color[3];
    return this;
  }

  /**
   * Set the current alpha trasnparency for this object (0-255))
   * @param alpha
   */
  public RiTextIF alpha(float alpha)
  {
    this.fillA = alpha;
    return this;
  }

  /**
   * Returns the fill alpha value (transparency)
   */
  public float alpha()
  {
    return fillA;
  }

  // -------------------- end colors ----------------------

  /**
   * Checks if the input point is inside the bounding box
   */
  public boolean contains(float mx, float my)
  {
    // System.out.println("Testing: ("+mx+","+my+") vs ("+x1+","+y1+")");
    this.updateBoundingBox(pApplet.g);
    return (boundingBox.contains(mx - x, my - y));
  }

  /**
   * Draw the RiText object at current x,y,color,font,alignment, etc.
   */
  public RiTextIF draw()
  {
    this.update(pApplet.g);
    this.render(pApplet.g);
    return this;
  }

  /**
   * Draw the RiText object at current x,y,color,font,alignment, etc. on the
   * specified PGraphics object
   */
  public RiTextIF draw(PGraphics p)
  {
    PGraphics pg = p != null ? p : pApplet.g;
    this.update(pg);
    this.render(pg);
    return this;
  }

  /**
   * Override in subclasses to do custom rendering
   * <p>
   * Note: It is generably preferable to override this method rather than the draw()
   * method in subclasses to ensure proper maintenance of contained objects.
   */
  protected void render()
  {
    render(pApplet.g);
  }

  /**
   * Override in subclasses to do custom rendering
   * <p>
   * Note: It is generably preferable to override this method rather than the draw()
   * method in subclasses to ensure proper maintenance of contained objects.
   */
  protected void render(PGraphics p)
  {
    if (this.hidden || text == null)
      return;

    if (text == null || text.length() == 0)
      return;
    
    if (textToCopy != null)
      textToCopy.draw();

    // translate & draw at 0,0
    p.pushStyle();
    p.pushMatrix(); // --------------

    doAffineTransforms(p);

    if (boundingBoxVisible)
      this.drawBoundingBox(p);

    p.fill(fillR, fillG, fillB, fillA);

    if (font != null)
      p.textFont(font);

    p.textAlign(alignment);

    if (this.fontSize > 0)
      p.textSize(fontSize);

    p.text(text.text(), 0, 0);

    p.popMatrix(); // --------------
    p.popStyle();
  }

  protected void doAffineTransforms(PGraphics p)
  {      
    float[] bb = boundingBox();
    float centerX = bb[2]/2f;
    float centerY = bb[3]/2f;
    
    p.translate(x, y);
    p.translate(centerX, -centerY); 
    p.rotate(rotateZ);
    p.translate(-centerX, +centerY);
    p.scale(scaleX, scaleY);
  }

  /**
   * Returns true if we are rendering in 3D, else false
   */
  static boolean is3D(PGraphics p)
  {
    if (p instanceof PGraphicsJava2D) // for PDF renderer
      return false;
    
    if (rendering3D == null)
    {
      if (p == null)
        rendering3D = Boolean.FALSE;
      else
        rendering3D = new Boolean(!(p instanceof PGraphicsJava2D));
    }
    return rendering3D.booleanValue();
  }

  protected void drawBoundingBox(PGraphics p)
  {
    if (bbFillA <= 0) 
      p.noFill();
    else
      p.fill(bbFillR, bbFillG, bbFillB, bbFillA);
    p.stroke(bbStrokeR, bbStrokeG, bbStrokeB, bbStrokeA);
    if (bbStrokeWeight > 0)
      p.strokeWeight(bbStrokeWeight);
    else
      p.noStroke();
    p.rectMode(PApplet.CORNER);
    p.rect( boundingBox.x,  boundingBox.y,  boundingBox.w,  boundingBox.h);
  }
  
  /**
   * Returns a field for field copy of this object
   */
  public RiTextIF copy()
  {
    return copy(this);
  }


  /**
  public RiTextIF mouseEvent(MouseEvent e) 
  {
    float mx = e.getX();
    float my = e.getY();

    switch (e.getAction())
    {
      case MouseEvent.PRESS:
        if (mouseDraggable && !hidden && contains(mx, my))
        {
          isDragging = true;
          this.mouseXOff = mx - x;
          this.mouseYOff = my - y;
        }
        break;
      case MouseEvent.RELEASE:
        
        if (mouseDraggable && contains(mx, my))
        {
          isDragging = false;
          pauseBehaviors(false);
        }
        break;
      case MouseEvent.CLICK:
        break;
        
      case MouseEvent.DRAG:
        if (isDragging && contains(mx, my))
        {
          x = mx - mouseXOff;
          y = my - mouseYOff;
        }
        break;
      case MouseEvent.MOVE:
        break;
    }
    return this;
  }*/
     

  /**
   * Returns the current text width in pixels
   */
  public float textWidth()
  {
    float result = 0;
    
    String txt = text != null ? text.text() : null; 
    
    if (txt == null)
    {
      if (!printedTextWidthWarning)
      {
        System.err.println("[WARN] textWidth() called for null text!");
        printedTextWidthWarning = true;
      }
      return result; // hmm?
    }
    
    this.verifyFont();
    
    if (this.pApplet != null)
      result = pApplet.textWidth(txt);// * scaleX;
    
    return result;
  }

  static boolean printedTextWidthWarning;

  /**
   * Returns the height for the current font in pixels 
   * (including ascenders and descenders)
   */
  public float textHeight()
  {
    return (textAscent() + textDescent()) * scaleY;
    //return (_pApplet.textAscent() + _pApplet.textDescent()) * scaleY;
  }

  protected  void update()
  {
    update(pApplet.g);
  }

  protected void update(PGraphics p)
  {
    if (x == Float.MIN_VALUE && text.text() != null)
      x = screenCenterX();

    this.updateBehaviors();

    if (boundingBoxVisible && text.text() != null)
      this.updateBoundingBox(p);
  }

  /**
   * Sets the animation <code>motionType</code> for this for moveTo() or
   * moveBy() methods on this object, set via one of the following constants: <br>
   * <ul>
   * <li>RiText.LINEAR
   * <li>RiText.EASE_IN
   * <li>RiText.EASE_OUT
   * <li>RiText.EASE_IN_OUT
   * <li>RiText.EASE_IN_OUT_CUBIC
   * <li>RiText.EASE_IN_CUBIC
   * <li>RiText.EASE_OUT_CUBIC;
   * <li>RiText.EASE_IN_OUT_QUARTIC
   * <li>RiText.EASE_IN_QUARTIC
   * <li>RiText.EASE_OUT_QUARTIC;
   * <li>RiText.EASE_IN_OUT_SINE
   * <li>RiText.EASE_IN_SINE
   * <li>RiText.EASE_OUT_SINE
   * </ul>
   * 
   * @param motionType
   */
  public RiTextIF motionType(int motionType)
  {
    this.motionType = motionType;
    return this;
  }

  /**
   * Returns the <code>motionType</code> for this object,
   */
  public int motionType()
  {
    return this.motionType;
  }

  /**
   * Move to new absolute x,y (or x,y,z) position over 'time' seconds
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @return the unique id for this behavior
   */
  public int moveTo(float newX, float newY, float seconds)
  {
    return this.moveTo(new float[] { newX, newY }, seconds, 0);
  }

  /**
   * Move to new absolute x,y (or x,y,z) position over 'time' seconds
   * <p>
   * Note: uses the current <code>motionType</code> for this object, starting at
   * 'startTime' seconds in the future
   * 
   * @return the unique id for this behavior
   */
  public int moveTo(float newX, float newY, float seconds, float startTime)
  {
    return this.moveTo(new float[] { newX, newY }, seconds, startTime);
  }

  /**
   * Move to new absolute x,y (or x,y,z) position over 'time' seconds
   * <p>
   * Note: uses the current <code>motionType</code> for this object, starting at
   * 'startTime' seconds in the future
   * 
   * @return the unique id for this behavior
   */
  public int moveTo(final float[] newPosition, final float seconds, final float startTime)
  {
    String err3d = "Invalid newPosition.length for moveTo(),"
        + " expected 2 (or 3 in 3d mode), but found: " + newPosition.length;

    InterpolatingBehavior moveTo = null;
    if (!is3D(pApplet.g) || newPosition.length == 2) // 2d
    {
      if (newPosition.length != 2)
        throw new RiTaException(err3d);
      moveTo = new TextMotion2D(this, newPosition, startTime, seconds);
    }
    else
    // 3d
    {
      if (newPosition.length != 3)
        throw new RiTaException(err3d + "\nPerhaps you wanted moveTo3D()?");
      moveTo = new TextMotion3D(this, newPosition, startTime, seconds);
      // moveTo.resetTarget(new float[] {x,y,z}, newPosition, startTime,
      // seconds);
    }
    moveTo.setMotionType(motionType);

    addBehavior(moveTo);

    return moveTo.getId();
  }

  /**
   * Move to new position by x,y offset over the duration specified by
   * 'seconds', starting at 'startTime' seconds in the future
   * <p>
   * 
   * @return the unique id for this behavior
   */
  public int moveBy(float xOffset, float yOffset, float seconds, float startTime)
  { 
    return (is3D(pApplet.g)) ?
      this.moveBy(new float[] { xOffset, yOffset, 0 }, seconds, startTime) :
      this.moveBy(new float[] { xOffset, yOffset }, seconds, startTime);
  }

  /**
   * Move to new position by x,y offset over the duration specified by
   * 'seconds'.
   * <p>
   * 
   * @return the unique id for this behavior
   */
  public int moveBy(float xOffset, float yOffset, float seconds)
  {
    return this.moveBy(xOffset, yOffset,  seconds, 0);
  }

  /**
   * Move to new position by x,y offset over the duration specified by
   * 'seconds'.
   * <p>
   * Note: uses the current <code>motionType</code> for this object.
   * 
   * @return the unique id for this behavior
   */
  public int moveBy(float[] posOffset, float seconds, float startTime)
  {

    boolean is3d = is3D(pApplet.g);
    float[] newPos =  is3d ? new float[3] : new float[2];
    
    if (posOffset.length != newPos.length) {
      throw new RiTaException("Expecting a 2d array(or 3 in 3d) "
          + "for the 1st argument, but found: " + RiTa.asList(posOffset));
    }
    newPos[0] = x + posOffset[0];
    newPos[1] = y + posOffset[1];
    if (newPos.length > 2)
      newPos[2] = posOffset.length > 2 ? z += posOffset[2] : z;
    return this.moveTo(newPos, seconds, startTime);
  }

  /**
   * Returns true if the object is offscreen
   */
  public boolean isOffscreen()
  {
    return isOffscreen(pApplet.g);
  }

  /**
   * Returns true if the object is offscreen
   * 

   */
  public boolean isOffscreen(PGraphics p)
  {
    // System.err.println(text+" - offscreen? ("+x+","+y+")");
    return (x < 0 || x >= p.width) || (y < 0 || y >= p.height);
  }

  // Scale methods ----------------------------------------

  /**
   * Scales object to 'newScale' over 'time' seconds, starting at 'startTime'
   * seconds in the future
   * <p>
   * Note: uses linear interpolation unless otherwise specified. Returns the Id
   * of the RiTextBehavior object used for the scale.
   */
  public int scaleTo(float newScale,  float seconds, float startTime)
  {
    return scaleTo(newScale, newScale, newScale, seconds, startTime);
  }

  /**
   * Scales object to 'newScale' over 'time' seconds, starting immediately.
   * <p>
   * Note: uses linear interpolation unless otherwise specified. Returns the Id
   * of the RiTextBehavior object used for the scale.
   */
  public int scaleTo(float newScale, float seconds)
  {
    return scaleTo(newScale, newScale, newScale, seconds);
  }

  /**
   * Scales object to {scaleX, scaleY, scaleZ} over 'time' seconds. Note: uses
   * linear interpolation unless otherwise specified. Returns the Id of the
   * RiTextBehavior object used for the scale.
   */
  public int scaleTo(float scaleX, float scaleY, float scaleZ, float seconds)
  {
    return scaleTo(scaleX, scaleY, scaleZ, seconds, 0);
  }

  /**
   * Scales object to {scaleX, scaleY, scaleZ} over 'time' seconds, starting at
   * 'startTime' seconds in the future.
   * <p>
   * Returns the Id of the RiTextBehavior object used for the scale. Note: uses
   * linear interpolation unless otherwise specified.
   */
  public int scaleTo(final float newScaleX, final float newScaleY, final float newScaleZ, final float seconds, final float delay)
  {
    ScaleBehavior scaleTo = new ScaleBehavior(this, new float[] { newScaleX, newScaleY,
        newScaleZ }, delay, seconds);
    scaleTo.setMotionType(LINEAR);
    addBehavior(scaleTo);
    return scaleTo.getId();
  }
  
  public int rotateTo(float angleInRadians, float seconds)
  {
    return rotateTo(angleInRadians, seconds, 0);
  }
  
  // TODO: add axis for 3D ??
  public int rotateTo(float angleInRadians, float seconds, float delay)
  {
    RotateZBehavior rotateTo = new RotateZBehavior(this, angleInRadians, delay, seconds);
    rotateTo.setMotionType(LINEAR);
    addBehavior(rotateTo);
    return rotateTo.getId();
  }


  // Fade methods -----------------------------------------

  /**
   * Fades in current text over <code>seconds</code> starting at
   * <code>startTime</code>. Interpolates from the current color {r,g,b,a} to
   * {r,g,b,255}.
   * 
   * @param startTime
   *          time in future to start
   * @param seconds
   *          time for fade
   * @return a unique id for this behavior
   */
  public int fadeIn(float seconds, float startTime)
  {
    float[] col = { fillR, fillG, fillB, 255 };
    return _colorTo(col, seconds, startTime, FadeIn, false);
  }

  public int fadeIn(float seconds)
  {
    return this.fadeIn(seconds, 0);
  }

  /**
   * Fades out current text over <code>seconds</code> starting at
   * <code>startTime</code>. Interpolates from the current color {r,g,b,a} to
   * {r,g,b,0}.
   * 
   * @param seconds
   *          time for fade
   * @param startTime
   *          time in future to start
   * @param removeOnComplete
   *          destroys the object when the behavior completes
   * @return the unique id for this behavior
   */
  public int fadeOut(float seconds, float startTime, boolean removeOnComplete)
  {
    return this._fadeOut(seconds, startTime, removeOnComplete, FadeOut);
  }
  
  protected int _fadeOut(float seconds, float startTime, boolean removeOnComplete, EventType type)
  {
    float[] col = { fillR, fillG, fillB, 0 };
    // if (isBoundingBoxVisible()) // fade bounding box too
    // addBehavior(new BoundingBoxAlphaFade(this, 0, startTime, seconds));
    return _colorTo(col, seconds, startTime, type, removeOnComplete);
  }

  public int fadeOut(float seconds, float startTime)
  {
    return this.fadeOut(seconds, startTime, false);
  }

  public int fadeOut(float seconds, boolean removeOnComplete)
  {
    return this.fadeOut(seconds, 0, removeOnComplete);
  }

  public int fadeOut(float seconds)
  {
    return this.fadeOut(seconds, false);
  }

  protected synchronized int _colorTo(final float[] color, final float seconds, final float startTime, final EventType type, final boolean disposeWhenFinished)
  {
    //System.out.println(this+"._colorTo("+RiTa.asList(color)+")");

    if (boundingBoxVisible && (type == EventType.FadeIn || type == EventType.FadeOut))
    {
      if (color[3] >= 255 || color[3] < 1) // hack to fade bounding box too
        addBehavior(new BoundingBoxAlphaFade(this, color[3], startTime, seconds));
    }

    TextColorFade colorFade = new TextColorFade(this, color, startTime, seconds);
    colorFade.setType(type);

    if (disposeWhenFinished) {
      colorFade.addListener(new BehaviorListener()
      {
        public void behaviorCompleted(RiTextBehavior behavior)
        {
          dispose(behavior.getParent());
        }
      }); // disposes the RiText after fadeOut
    }
    
    addBehavior(colorFade);

    return colorFade.getId();
  }

  public int colorTo(float[] colors, float seconds, float delay, EventType type)
  {
    return this._colorTo(colors, seconds, delay, type, false);
  }

  /**
   * Transitions to 'color' (rgba) over 'seconds' starting at 'startTime'
   * seconds in the future
   * 
   * @param seconds
   *          time for fade
   * @return a unique id for this behavior
   */
  public int colorTo(float r, float g, float b, float a, float seconds)
  {
    return this.colorTo(new float[] { r, g, b, a }, seconds);
  }
  public int colorTo(float[] color, float seconds)
  {
    return this.colorTo(color, seconds, 0);
  }
  public int colorTo(float gray, float seconds)
  {
    return this.colorTo(new float[] { gray, gray, gray, this.fillA }, seconds);
  }
  public int colorTo(float[] color, float seconds, float startTime)
  {
    return this._colorTo(color, seconds, startTime, ColorTo, false);
  }

  /**
   * Fades out the current text and fades in the <code>newText</code> over
   * <code>seconds</code> starting immediately
   * 
   * @return the unique id for this behavior
   */
  public int textTo(final String newText, final float seconds)
  {
    return textTo(newText, seconds, 0);
  }

  /**
   * Fades out the current text and fades in the <code>newText</code> over
   * <code>seconds</code> starting at 'startTime' seconds in the future
   * 
   * @param newText
   *          to be faded in
   * @param startTime
   *          # of seconds in the future that the fade will start
   * @param seconds
   *          time for fade
   * @return the unique id for this behavior
   */
  public int textTo(final String newText, final float seconds, final float startTime)
  {
    // grab the alpha if needed
    float startAlpha = 0;
    if (textToCopy != null)
    {
      startAlpha = textToCopy.alpha();
      dispose(textToCopy); // stop any currents
    }

    // use the copy to fade out
    textToCopy = RiText.copy(this);
    //textToCopy.fadeOut(seconds);
    textToCopy._fadeOut(seconds, 0, false, TextToCopy);

    // and use 'this' to fade in
    this.text(newText);
    this.alpha(startAlpha);
    float[] col = { fillR, fillG, fillB, 255 }; // fadeIn
    return _colorTo(col, seconds * .95f, startTime, TextTo, false);
  }

  /**
   * Call to remove a RiText from the current sketch (and from existence),
   * cleaning up whatever resources it may have held
   */
  public static synchronized void dispose(RiTextIF rt)
  {
    if (rt != null)
    {
      PApplet p = (PApplet) rt.getPApplet();
      if (p != null && rt.autodraw())
      {
          try
          {
            p.unregisterMethod("draw", rt);
          }
          catch (Throwable e)
          {
            System.err.println("[WARN] Error unregistering draw() for "+rt.text());
          }
      }
        /*try {
          //p.unregisterMethod("dispose", rt);
          p.unregisterMethod("mouseEvent", rt);
        }
        catch (Throwable e)
        {
          System.err.println("[WARN] Error unregistering: "+rt.text());
        }*/
      
      ((RiText)rt)._dispose();
    }
  }
  
  protected void _dispose()
  {
    visible(false);

    if (text != null)
      RiString.dispose(text);

    if (behaviors != null)
    {
      for (int i = 0; i < behaviors.size(); i++)
      {
        RiTextBehavior rtb = (RiTextBehavior) behaviors.get(i);
        rtb.delete();
      }
      behaviors.clear();
      behaviors = null;
    }
    boundingBox = null;

    instances.remove(this);
  }
  
  public static synchronized void disposeAll()
  {
    dispose(instances);
  }
  
  public static synchronized void dispose(RiTextIF[] c)
  {
    if (c == null) return;
    for (int i = 0; i < c.length; i++)
    {
      if (c[i] != null)
      {
        dispose(c[i]);
        c[i] = null;
      }
    }
    c = null;
  }
  
  public static synchronized void dispose(List l)
  {
    if (l == null) return;
    
    while (l.size() > 0)
    {
      RiText p = (RiText) l.remove(0);
      dispose(p);
    }
  }
  
  /////////////////////////////////////////////////
  
  /**
   * Returns all existing instances of RiText objects in an array
   */
  protected static RiText[] getInstances()
  {
    return (RiText[]) instances.toArray(new RiText[instances.size()]);
  }
  
  /**
   * Returns all RiTexts that contain the point x,y or null if none do.
   * <p>
   * Note: this will return an array even if only one item is picked, therefore,
   * you should generally use it as follows:
   * 
   * <pre>
   *   RiText picked = null;
   *   RiText[] rts = RiText.getPicked(mx, my);
   *   if (rts != null)
   * picked = rts[0];
   * 
   * <pre>
   * @return RiText[] 1 or more RiTexts containing
   * the point, or null if none do.
   */
  public static final RiText[] picked(float x, float y)
  {
    List pts = null;
    for (int i = 0; i < instances.size(); i++)
    {
      RiText rt = (RiText) instances.get(i);
      if (rt.contains(x, y))
      {
        if (pts == null)
          pts = new ArrayList();
        pts.add(rt);
      }
    }
    if (pts == null || pts.size() == 0)
      return EMPTY_ARRAY;

    return (RiText[]) pts.toArray(new RiText[pts.size()]);
  }

  // end statics ----------------------------------------------

  /**
   * Fades all visible RiText objects.
   */
  public static final void fadeAllOut(float seconds)
  {
    for (Iterator i = instances.iterator(); i.hasNext();)
    {
      RiText p = (RiText) i.next();
      p.fadeOut(seconds);
    }
  }

  /**
   * Fades in all RiText objects over the specified duration
   */
  public static final void fadeAllIn(float seconds)
  {
    for (Iterator i = instances.iterator(); i.hasNext();)
    {
      RiText p = (RiText) i.next();
      p.fadeIn(seconds);
    }
  }

  // getters / setters ----------------------------------------------



  protected static PFont checkFontCache(String fontFileName, float sz)
  {
    if (fonts == null)
      fonts = new HashMap();
    PFont pf = (PFont) fonts.get(fontFileName + sz);
    // System.out.println("CacheCheck: "+fontFileName+sz+" -> "+(pf!=null));
    return pf;
  }

  /**
   * Returns the font specified after loading it and setting it as the
   * current font.
   */
  public PFont loadFont(String fontFileName)
  {
    return loadFont(fontFileName, -1);
  }

  protected static PFont fontFromStream(InputStream is, String name)
  {
    // System.out.println("fontFromStream("+name+")");
    try
    {
      PFont pFont = new PFont(is);
      return pFont;
    }
    catch (IOException e)
    {
      throw new RiTaException("creating font from stream: " + is + " with name=" + name);
    }
  }

  /**
   * Returns the font specified after loading it and setting the current font
   * size.
   */
  public PFont loadFont(String fontFileName, float size)
  {
    PFont pf = _loadFont(getPApplet(), fontFileName, size);
    this.font(pf, size);
    return pf;
  }

  protected static PFont _loadFont(PApplet p, String fontFileName, float size)
  {
    PFont pf = checkFontCache(fontFileName, size);
    if (pf == null)
    {
      // try the filesystem...
      try
      {
        // System.out.println("looking for font: "+fontFileName);
        InputStream is = RiTa.openStream(fontFileName);
        pf = fontFromStream(is, fontFileName);
      }
      catch (Throwable e)
      {
        String errStr = "Could not load font '"+ fontFileName + "'. Make "
            + "sure that the font\nhas been copied to the data folder"+
            " of your sketch\nError="+ e.getMessage();
        throw new RiTaException(errStr);
      }
      cacheFont(fontFileName, size, pf); // add to cache
    }
    return pf;
  }

  protected static void cacheFont(String fontFileName, float fontSz, PFont pf)
  {
    // System.out.println("caching: "+fontFileName+fontSz+"->"+pf);
    if (fonts == null)
      fonts = new HashMap();
    fonts.put(fontFileName + fontSz, pf);
  }

  /**
   * Creates (and caches) the font for this object from a System font (via
   * PApplet.createFont()). Note: this is not a good idea for web-applets as the
   * user's machine may not have the specified font. 
   */
  public RiTextIF createFont(String fontName, float sz)
  {
    // System.out.println("RiText.createFont("+fontName+","+sz+")");
    this.font = _createFont(pApplet, fontName, sz);
    return font(font, sz);
  }

  protected static PFont _createFont(PApplet p, String fontName, float size)
  {
    PFont pf = checkFontCache(fontName, size);
    //System.out.println("Checking cache: "+fontName+"-"+sz);
    if (pf == null)
    {
      //System.out.println("Creating font: "+fontName+"-"+sz);
      pf = p.createFont(fontName, size); 
      cacheFont(fontName, size, pf);
    }
    return pf;
  }

  /**
   * Set the current boundingBox stroke-weight for this object
   */
  public RiTextIF boundingBoxStrokeWeight(float r)
  {
    this.bbStrokeWeight = r;
    return this;
  }

  /**
   * Gets the current font size
   */
  public float textSize()
  {
    return fontSize;
  }

  /**
   * Sets the font size for this object
   */
  public RiTextIF textSize(float textSize)
  {
    this.fontSize = textSize;
    return this;
  }

  /**
   * Returns the current text
   */
  public String text()
  {
    return (text == null) ? null : text.text();
  }

  /**
   * Sets the current text to this String
   */
  public RiTextIF text(String _text)
  {
    if (this.text == null)
      this.text = new RiString(_text);
    else
      this.text.text(_text);
    return this;
  }

  /**
   * Sets boolean flag to show or hide the object
   */
  public RiTextIF visible(boolean visible)
  {
    this.hidden = !visible;
    return this;
  }

  /**
   * Sets the current text to the character
   */
  public RiTextIF text(char ch)
  {
    return this.text(Character.toString(ch));
  }

  public String toString()
  {
    return "RiText['" + this.text() + "']";
  }

  /**
   * Returns true if the objects is not hidden
   */
  public boolean isVisible()
  {
    return !this.hidden;
  }
  
  /** @exclude */
  protected synchronized void updateBehaviors()
  {
    for (int i = 0; behaviors != null && i < behaviors.size(); i++)
    {
      RiTextBehavior rtb = (RiTextBehavior) behaviors.get(i);
      // System.out.println("RiText.updateBehaviors("+rtb+")");
      if (rtb == null)
      {
        behaviors.remove(rtb);
        continue;
      }
      rtb.update();
    }
  }

  /**
   * Add a new behavior to this RiText's run queue
   */
  protected synchronized RiTextBehavior addBehavior(RiTextBehavior behavior)
  {
    if (behaviors == null)
      this.behaviors = new ArrayList();
    if (!behaviors.contains(behavior))
      this.behaviors.add(behavior);
    return behavior;
  }

  /**
   * Remove a Behavior from the RiText's run queue

   */
  protected RiText removeBehavior(RiTextBehavior behavior)
  {
    // System.out.println("REMOVED behavior: " + behavior);
    if (behaviors != null) {
      behaviors.remove(behavior);
      behavior.delete();
    }
    return this;
  }

  /**
   * Immediately marks all Behaviors in the RiText's run queue as
   * complete and causes them to fire their<code>behaviorCompleted()</code> methods.

   */
  protected void completeBehaviors()
  {
    if (behaviors == null)
      return;
    for (int i = 0; i < behaviors.size(); i++)
      ((RiTextBehavior) behaviors.get(i)).finish();
  }// NEEDs MORE TESTING!

  /**
   * Pauses (or unpauses) all Behaviors in the RiText's run queue

   */
  protected synchronized void pauseBehaviors(boolean paused)
  {
    if (behaviors == null)
      return;
    for (int i = 0; i < behaviors.size(); i++)
    {
      RiTextBehavior tb = (RiTextBehavior) behaviors.get(i);
      tb.setPaused(paused);
    }
  }

  /**
   * Remove all Behaviors from the RiText's run queue
   * 

   */
  protected synchronized void removeBehaviors()
  {
    if (behaviors == null)
      return;
    for (int i = 0; i < behaviors.size(); i++)
    {
      RiTextBehavior tb = (RiTextBehavior) behaviors.get(i);
      this.removeBehavior(tb);
    }
  }

  /**
   * Sets the position for the current RiText
   */
  public RiTextIF position(float x, float y)
  {
    this.x = x;
    this.y = y;
    return this;
  }

  /**
   * Sets the 3d position for the current RiText

   */
  public RiTextIF position(float x, float y, float z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
    return this;
  }

  /**
   * Returns a list of behaviors for the object.
   * 

   */
  public List behaviors()
  {
    return this.behaviors;
  }

  /**
   * Returns a list of behaviors of the specified type for this object, where
   * type is generally one of (MOVE, FADE_IN, FADE_OUT, FADE_TO_TEXT, SCALE_TO,
   * etc.)
   */
  public RiTextBehavior[] behaviorsByType(int type)
  {
    List l = RiTextBehavior.selectByType(behaviors, type);
    return (RiTextBehavior[]) l.toArray(new RiTextBehavior[l.size()]);
  }

  /**
   * Returns the behavior corresponding to the specified 'id'.
   * 

   */
  public static final RiTextBehavior behaviorById(int id)
  {
    return RiTextBehavior.getBehaviorById(id);
  }

  /**
   * Returns true if the object has been set to be draggable by the mouse
   * 

  public boolean mouseDraggable()
  {
    return this.mouseDraggable;
  }
   */

  /**
   * Sets the object's draggable state (default=false)
   
  public RiTextIF mouseDraggable(boolean mouseDraggable)
  {
    this.mouseDraggable = mouseDraggable;
    return this;
  }*/

  /**
   * Returns the current alignment (default=LEFT)
   * 

   */
  public int textAlign()
  {
    return this.alignment;
  }

  /**
   * Sets text alignment mode for the object.
   * 
   * @param align (CENTER, RIGHT, LEFT[default])
   */
  public RiTextIF textAlign(int align)
  {
    this.textMode(align);
    return this;
  }

  /**
   * Sets text alignment mode for the object.
   * 
   * @param align (CENTER, RIGHT, LEFT[default])
   */
  public void textMode(int align)
  {
    switch (align)
    {
      case LEFT:
      case CENTER:
      case RIGHT:
        break;
      default:
        throw new RiTaException("Illegal alignment: use LEFT, CENTER, or RIGHT");
    }
    this.alignment = align;
  }

  /**
   * Returns a rectangle representing the current screen position of the
   * bounding box
   */
  public float[] boundingBox()
  {
    updateBoundingBox(pApplet.g);
    
    if (screenBoundingBox == null)
      screenBoundingBox = new Rect();

    screenBoundingBox.set((x + boundingBox.x),  (y + boundingBox.y),  (boundingBox.w*scaleX),  (boundingBox.h*scaleY));

    return screenBoundingBox.asArray();
  }

  /**
   * Converts and returns a list of RiTexts as a RiText[]

   */
  protected static RiText[] toArray(List result)
  {
    return (RiText[]) result.toArray(EMPTY_ARRAY);
  }

  /**
   * Returns number of characters in the contained String
   */
  public int length()
  {
    return text.length();
  }
  
  public float wordOffset(int wordIdx)
  {
    return wordOffsetWith(this.font, words(), wordIdx);
  }

  protected float wordOffsetWith(PFont pfont, int wordIdx, String delim)
  {
    String[] words = text.text().split(delim);
    return this.wordOffsetWith(pfont, words, wordIdx);
  }

  protected float wordOffsetWith(Object pfont, String[] words, int wordIdx)
  {
    if (wordIdx >= words.length) {
      throw new IllegalArgumentException("\nBad wordIdx=" 
          + wordIdx + " for " +  RiTa.asList(words));
    }

    if (pfont == null)
      verifyFont();
    else
      pApplet.textFont((PFont)pfont);

    float xPos = this.x;
    if (wordIdx > 0)
    {
      String[] pre = new String[wordIdx];
      System.arraycopy(words, 0, pre, 0, pre.length);
      String preStr = RiTa.join(pre, SP) + SP;
      float tw = -1;
      if (pApplet != null)
        tw = pApplet.textWidth(preStr);
      switch (alignment)
      {
        case LEFT:
          xPos = this.x + tw;
          break;
        case RIGHT:
          xPos = this.x - tw;
          break;
        default:
          throw new RiTaException("wordOffset() only supported for LEFT & RIGHT alignments");
      }
    }
    return xPos;
  }

  
  /**
   * Returns the x-position (in pixels) for the character at 'charIdx'.
   */
  public float charOffset(int charIdx)
  {
    return positionForChar(defaultFont(pApplet), charIdx);
  }
  
  /**
   * Returns the x-position (in pixels) for the character at 'charIdx'.
   * @param pf 
   */
  protected float positionForChar(PFont pf, int charIdx)
  {
    if (charIdx <= 0) return x;
    if (charIdx > length()) // -1?
      charIdx = length();
    String sub = text().substring(0, charIdx);
    pApplet.textFont(pf);
    return x + pApplet.textWidth(sub);
  }

  /** @exclude */
  public PApplet getPApplet()
  {
    if (pApplet == null && !msgNullRootApplet)
    {
      System.err.println("[WARN] getPApplet() returned null");
      msgNullRootApplet = true;
    }
    return pApplet;
  }

  static boolean msgNullRootApplet;

  // ========================= STATICS =============================
  /**
   * Immediately pauses all Behaviors in the RiText's run queue
   * 

   */
  protected static synchronized void pauseAllBehaviors(boolean paused)
  {
    RiText[] cts = RiText.getInstances();
    for (int i = 0; i < cts.length; i++)
      cts[i].pauseBehaviors(paused);
  }

  /**
   * Creates an array of RiText from a file, by delegating to
   * PApplet.loadStrings(), then creating one array element per line (including
   * blanks).
   * 
   * @param fileName
   *          located in data directory
   */
  protected static RiText[] loadStrings(PApplet p, String fileName)
  {
    String[] lines = RiTa.loadStrings(/*p, */fileName);
    RiText[] rts = new RiText[lines.length];
    for (int i = 0; i < lines.length; i++)
      rts[i] = new RiText(p, lines[i]);
    return rts;
  }

  /**
   * Pops the last value off the array, disposes it, and returns the new array
   * (shortened by one element).
   * <p>
   * If there are no elements in the array, the original array is returned
   * unchanged.
   */
  protected static RiText[] popArray(RiText[] rts)
  {
    if (rts == null || rts.length < 1)
      return rts;
    RiText[] tmp = new RiText[rts.length - 1];
    System.arraycopy(rts, 0, tmp, 0, tmp.length);
    RiText.dispose(rts[rts.length - 1]);
    return tmp;
  }

  /**
   * Shifts the first value off the array, disposes it, and returns the new array
   * (shortened by one element).
   * <p>
   * If there are no elements in the array, the original array is returned
   * unchanged.
   */
  protected static RiText[] shiftArray(RiText[] rts)
  {
    if (rts == null || rts.length < 1)
      return rts;
    RiText[] tmp = new RiText[rts.length - 1];
    System.arraycopy(rts, 1, tmp, 0, tmp.length);
    return tmp;
  }

  protected static void constrainLines(List<RiTextIF> ritexts, float y, float h, float leading)
  {
    leading = (int) leading; // same as JS
    float ascent = ritexts.get(0).textAscent();
    float descent = ritexts.get(0).textDescent();
    //System.out.println("RiText.constrainLines().ascent="+ascent+" descent="+descent+" leading="+leading);
    float maxY = y + h, currentY = y + ascent;
    
    RiTextIF next = null;
    Iterator<RiTextIF> it = ritexts.iterator();
    
    // set y-pos for those that fit
    while (it.hasNext())
    {
      next = it.next();
      next.position(next.x(), currentY);
      if (!_withinBoundsY(currentY, leading, maxY, descent))
        break;
      currentY += leading;
    }   
    
    // then remove/delete the rest
    while (it.hasNext()) {
      next = it.next();
      RiText.dispose(next);
      it.remove();
    }
  }
  
  public static boolean _withinBoundsY(float currentY, float leading, float maxY, float descent)
  {
    return currentY + leading <= maxY - descent;
  }

  /**
   * Utility method to do regex replacement on a String
   * 
   * @param patternStr
   *          regex
   * @param fullStr
   *          String to check
   * @param replaceStr
   *          String to insert
   * @see Pattern
   */
  protected static String regexReplace(String patternStr, String fullStr, String replaceStr)
  {
    return Regex.getInstance().replace(patternStr, fullStr, replaceStr);
  }

  /**
   * Utility method to test whether a String partially matches a regex pattern.
   * 
   * @param patternStr
   *          regex String
   * @param fullStr
   *          String to check
   * @see Pattern

   */
  protected static boolean regexMatch(String patternStr, String fullStr)
  {
    return Regex.getInstance().test(patternStr, fullStr);
  }

  /**
   * Return the current font for this object
   */
  public Object font()
  {
    return font;
  }

  /**
   * Sets the font and size for the object
   */
  public RiTextIF font(PFont pf, float size)
  {
    this.fontSize = size; // even -1 ? yes...
    this.font = pf;
    return this;
  }

  public RiTextIF font(PFont pf)
  {
    return this.font(pf, -1); // -1 ignored in renderer
  } 
  
  public RiTextIF font(String name, float size)
  {
    return this.font(_createFont(pApplet, name, size), size);
  }

  /**
   * Returns a 2 or 3-dimensional array with the objects x,y, or x,y,z position
   * (depending on the renderer)
   */
  public float[] position()
  {
    if (is3D(pApplet.g))
      return new float[] { x, y, z };
    else
      return new float[] { x, y, };
  }

  /**
   * Returns a 3-dimensional array with the objects x,y,z scale (1=100% or
   * unscaled)
   */
  public float[] scale()
  {
    return new float[] { scaleX, scaleY, scaleZ };
  }

  /**
   * Draws all (visible) RiText objects
   */
  protected static void drawAll(PGraphics p)
  {
    for (int i = 0; i < instances.size(); i++)
    {
      RiText rt = (RiText) instances.get(i);
      rt.draw(p);
    }
  }
  
  /**
   * Draws all (visible) RiText objects
   */
  public static final void drawAll() { drawAll(null); }

  /**
   * Returns a Map of all the features (key-value pairs) exists for this RiText
   */
  public Map features()
  {
    return text.features();
  }

  /**
   * Returns the character at the specified index
   */
  public String charAt(int index)
  {
    return text.charAt(index);
  }

  /**
   * Returns a new character sequence that is a subsequence of this sequence.
   */
  protected CharSequence subSequence(int start, int end)
  {
    return text.subSequence(start, end);
  }

  // rotate and scale  =======================

  /** 
   * Rotate the object via affine transform. This is same as rotateZ, but for 2D
   */
  public RiTextIF rotate(float rotate)
  {
    this.rotateZ = rotate;
    return this;
  }
  
  /**
   * Gets the x-rotation for the object
   */
  public float rotateX()
  {
    return this.rotateX;
  }
  
  /**
   * Gets the y-rotation for the object
   */
  public float rotateY()
  {
    return this.rotateY;
  }
  
  /**
   * Gets the z-rotation for the object
   */
  public float rotateZ()
  {
    return this.rotateZ;
  }
  
  /**
   * Sets the x-rotation for the object
   */
  public RiTextIF rotateX(float rotate)
  {
    this.rotateX = rotate;
    return this;
  }

  /** 
   * Sets the y-rotation for the object 
   */
  public RiTextIF rotateY(float rotate)
  {
    this.rotateY = rotate;
    return this;
  }

  /**
   * Sets the z-rotation for the object
   * 
   */
  public RiTextIF rotateZ(float rotate)
  {
    this.rotateZ = rotate;
    return this;
  }
  
  /** 
   * Sets the x-scale for the object
   */
  public RiTextIF scaleX(float scale)
  {
    this.scaleX = scale;
    return this;
  }

  /** 
   * Sets the y-scale for the object 
   */
  public RiTextIF scaleY(float scale)
  {
    this.scaleY = scale;
    return this;
  }

  /** 
   * Sets the z-scale for the object 
   */
  public RiTextIF scaleZ(float scale)
  {
    this.scaleZ = scale;
    return this;
  }

  /** 
   * Uniformly scales the object on all dimensions (x,y,z) 
   */
  public RiTextIF scale(float scale)
  {
    scaleX = scaleY = scaleZ = scale;
    return this;
  }

  /**
   *  Scales the object on all dimensions (x,y,z)
   *  @exclude
   */
  public RiTextIF scale(float sX, float sY, float sZ)
  {
    scale(new float[] { sX, sY, sZ });
    return this;
  }

  /** 
   * Scales the object on either 2 or 3 dimensions (x,y,[z])
   */
  public RiTextIF scale(float[] scales)
  {
    if (scales.length < 2)
      throw new RiTaException("scale(float[]) requires at least 2 values!");

    if (scales.length > 1)
    {
      scaleX = scales[0];
      scaleY = scales[1];
    }

    if (scales.length > 2)
      scaleZ = scales[2];
    
    return this;
  }

  /**
   * Returns the distance between the center points of the two RiTexts.
   */
  public float distanceTo(RiText rt)
  {
    float[] p1 = center();
    float[] p2 = rt.center();
    return distance(p1[0], p1[1], p2[0], p2[1]);
  }

  /**
   * Deletes the character at at the specified character index ('idx'). If the
   * specified 'idx' is less than xero, or beyond the length of the current
   * text, there will be no effect.
   */
  public RiTextIF removeChar(int idx)
  {
    text.removeChar(idx);
    return this;
  }

  /**
   * Replaces the character at the specified character index ('idx') with the
   * 'replaceWith' character.
   * <p>
   * If the specified 'idx' is less than zero, or beyond the length of the
   * current text, there will be no effect. 
   */
  public RiTextIF replaceChar(int idx, char replaceWith)
  {
    text.replaceChar(idx, replaceWith);
    return this;
  }
  
  /**
   * Replaces the character at 'idx' with 'replaceWith'. If the specified 'idx'
   * is less than xero, or beyond the length of the current text, there will be
   * no effect. 
   */
  public RiTextIF replaceChar(int idx, String replaceWith)
  {
    text.replaceChar(idx, replaceWith);
    return this;
  }

  /**
   * Inserts the character at the specified character index ('idx'). If the
   * specified 'idx' is less than zero, or beyond the length of the current
   * text, there will be no effect. 
   */
  public RiTextIF insertChar(int idx, char toInsert)
  {
    text.insertChar(idx, toInsert);
    return this;
  }

  /*
   * TODO: add lazy(er) updates
   */
  protected void updateBoundingBox(PGraphics pg)
  {
    verifyFont(); // need this here (really!)

    if (boundingBox == null)
      boundingBox = new Rect();
    
    float ascent = font.ascent() * font.getSize(); // fix for P5 bug
    float descent = font.descent() * font.getSize();
    float bbw = textWidth();
    float bbh = ascent + descent;
    
    // offsets from RiText.x/y
    float bbx = -bbw / 2f;
    float bby = -ascent;

    switch (alignment)
    {
      case LEFT:
        bbx += bbw / 2f;// + bbPadding / 2f;
        break;
      case CENTER: // ok as is
        break;
      case RIGHT:
        bbx -= bbw / 2f;// + bbPadding / 2f;
        break;
    }

    if (boundingBox != null)
      boundingBox.set(bbx, bby, bbw, bbh);
  }

  // ughh, need to rethink, maybe reflection?
  /**
   * Returns a field for field copy of <code>toCopy</code>
   */
  protected static RiText copy(RiText toCopy)
  {
    RiText rt = new RiText(toCopy.getPApplet());
    rt.font = toCopy.font;

    rt.behaviors = toCopy.behaviors; // deep or shallow?
    rt.text = new RiString(toCopy.text.text());
    rt.autodraw = toCopy.autodraw;
    
    rt.x = toCopy.x;
    rt.y = toCopy.y;
    rt.z = toCopy.z;
    
    
    
    rt.fillR = toCopy.fillR;
    rt.fillG = toCopy.fillG;
    rt.fillB = toCopy.fillB;
    rt.fillA = toCopy.fillA;
    
    rt.bbFillR = toCopy.bbFillR;
    rt.bbFillG = toCopy.bbFillG;
    rt.bbFillB = toCopy.bbFillB;
    rt.bbFillA = toCopy.bbFillA;
    
    rt.bbStrokeR = toCopy.bbStrokeR;
    rt.bbStrokeG = toCopy.bbStrokeG;
    rt.bbStrokeB = toCopy.bbStrokeB;
    rt.bbStrokeA = toCopy.bbStrokeA;
   /* rt.bbsStrokeR = toCopy.bbsStrokeR;
    rt.bbsStrokeG = toCopy.bbsStrokeG;
    rt.bbsStrokeB = toCopy.bbsStrokeB;
    rt.bbsStrokeA = toCopy.bbsStrokeA;*/
    rt.bbStrokeWeight = toCopy.bbStrokeWeight;
    rt.alignment = toCopy.alignment;
    rt.fontSize = toCopy.fontSize;

    //rt.mouseDraggable = toCopy.mouseDraggable;
    rt.boundingBoxVisible = toCopy.boundingBoxVisible;
    rt.motionType = toCopy.motionType;
    rt.hidden = toCopy.hidden;

    rt.scaleX = toCopy.scaleX;
    rt.scaleY = toCopy.scaleY;
    rt.scaleZ = toCopy.scaleZ;

    rt.rotateX = toCopy.rotateX;
    rt.rotateY = toCopy.rotateY;
    rt.rotateZ = toCopy.rotateZ;

    // add the features
    Map m = toCopy.features();
    Map features = rt.features();
    for (Iterator it = m.keySet().iterator(); it.hasNext();)
    {
      CharSequence key = (CharSequence) it.next();
      features.put(key, m.get(key));
    }
    return rt;
  }  
  

  public RiTextIF showBounds(boolean b)
  {
    this.boundingBoxVisible = b;
    return this;
  }

  public boolean showBounds()
  {
    return boundingBoxVisible;
  } 
  
  public int align()
  {
    return this.alignment;
  }
  
  public static final float distance(float x1, float y1, float x2, float y2)
  {
    float dx = x1 - x2, dy = y1 - y2;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }
  
  public static int timer(float period) { // for better error msg
    throw new RiTaException("Missing parent object -- did you mean: RiText.timer(this, "+period+");"); 
  }
  
  public static final int timer(Object parent, float period) {  return RiTa.timer(parent, period); } 
  public static final void stopTimer(int idx) { RiTa.stopTimer(idx); } 
  public static final void pauseTimer(int idx, boolean b) { RiTa.pauseTimer(idx, b); }
  public static final void pauseTimer(int idx, float pauseFor) { RiTa.pauseTimer(idx, pauseFor); }

  public static final float[] randomColor() { 
    return new float[] { random(256f), random(256f), random(256f) }; 
  } 

  public static final int random(int f) {  return (int)random((float)f); } // round or floor ?
  public static final int random(int f, int g) { return (int)random((float)f, (float)g); } // round or floor ?
  static Random internalRandom;
  public static final float random(float high) {

    if (high == 0) return 0;

    // internal random number object
    if (internalRandom == null) internalRandom = new Random();

    float value = 0;
    do {
      //value = (float)Math.random() * howbig;
      value = internalRandom.nextFloat() * high;
    } while (value == high);
    
    return value;
  }

  public static final float random(float low, float high) {
    if (low >= high) return low;
    float diff = high - low;
    return random(diff) + low;
  }

  public RiTextIF stopBehavior(int id)
  {
    RiTextBehavior.getBehaviorById(id).stop();
    return this;
  }

  public RiTextIF stopBehaviors()
  {
    if (behaviors != null) {
      for (int i = 0; i < behaviors.size(); i++)
      {
        RiTextBehavior tb = (RiTextBehavior) behaviors.get(i);
        this.removeBehavior(tb);
      }
    }
    return this;
  }
  
  public boolean contains(String s)
  {
    return this.text.text().contains(s); // delegate?
  }

  public float distanceTo(float px, float py)
  {
    return pApplet.dist(this.x, this.y, px, py);
  }

  public float textAscent()
  {
    if (this.font == null) this.font = defaultFont(pApplet);
    return font.ascent() * font.getSize();     // fix for bug in PApplet.textAscent
  }

  public float textDescent()
  {
    if (this.font == null) this.font = defaultFont(pApplet);
    return font.descent() * font.getSize();
  }

  public RiTextIF fontSize(float f)
  {
    this.fontSize = f;
    return this;
  }

  public float fontSize()
  {
    return this.fontSize;
  }

  public RiTextIF align(int i)
  {
    this.alignment = i;
    return this;
  }

  public RiTextIF rotate(float rx, float ry, float rz)
  {
    this.rotateX = rx;
    this.rotateY = ry;
    this.rotateZ = rz;
    return this;
  }

  public float[] rotate()
  {
    return new float[] {this.rotateZ,this.rotateY,this.rotateZ};
  }

  public RiTextIF analyze()
  {
    text.analyze();
    return this;
  }

  public RiTextIF concat(String cs)
  {
    text.concat(cs);
    return this;
  }
  
  public RiTextIF concat(RiString cs)
  {
    text.concat(cs);
    return this;
  }

  public boolean endsWith(String suffix)
  {
    return text.endsWith(suffix);
  }

  /**
   * Returns true iff the supplied String matches the text for this RiText 
   * ignoring case
   */
  public boolean equalsIgnoreCase(String cs)
  {
    return cs != null && text.equalsIgnoreCase(cs);
  }
  
  /**
   * Returns true iff the supplied String matches the text for this RiText 
   */
  public boolean equals(String cs)
  {
    return cs != null && text.equals(cs);
  }

  public String get(String featureName)
  {
    return text.get(featureName);
  }

  public int indexOf(String s)
  {
    return text.indexOf(s);
  }

  public int lastIndexOf(String s)
  {
    return text.lastIndexOf(s);
  }

  public RiTextIF insertWord(int wordIdx, String newWord)
  {
    text.insertWord(wordIdx, newWord);
    return this;
  }

  public String[] pos()
  {
    return text.pos();
  }

  public String[] pos(boolean useWordNetTags)
  {
    return text.pos(useWordNetTags);
  }

  public String posAt(int wordIdx)
  {
    return text.posAt(wordIdx);
  }

  public String posAt(int wordIdx, boolean useWordNetTags)
  {
    return text.posAt(wordIdx, useWordNetTags);
  }

  public RiTextIF replaceWord(int wordIdx, String newWord)
  {
    text.replaceWordAt(wordIdx, newWord);
    return this;
  }

  public RiTextIF replaceFirst(String regex, String replacement)
  {
    text.replaceFirst(regex, replacement);
    return this;
  }

  public RiTextIF replaceLast(String regex, String replacement)
  {
    text.replaceLast(regex, replacement);
    return this;
  }

  public RiTextIF replaceAll(String regex, String replacement)
  {
    text.replaceAll(regex, replacement);
    return this;
  }

  public String slice(int i, int j)
  {
    return text.slice(i, j);
  }

/*  public RiText[] split()
  {
    return this.split(SP);
  }

  public RiText[] split(String regex)
  {
    if (regex == null) regex = SP;
    return (regex.length()<1) ? splitLetters() : splitWords(text().split(regex));
  }*/

  /**
   * Splits the current object into an array of RiTexts, one per word,
   * maintaining the x and y position of each. Note: In most cases the original
   * RiText should be disposed manually to avoid text a doubling effect (via
   * RiText.dispose(originalRiText)).
   */
  public RiText[] splitWords()
  {
    Object pf = font();
    List result = new ArrayList();
    String[] txts = text().split(SP);
    for (int i = 0; i < txts.length; i++)
    {
      if (txts[i] != null && txts[i].length() > 0) {
        float xPos = wordOffsetWith(pf, txts, i);
        result.add(new RiText(pApplet, txts[i], xPos, this.y));
      }
    }
    return toArray(result);
  }
  
  
  /**
   * Splits the current object into an array of RiTexts, one per letter,
   * maintaining the x and y position of each. Note: In most cases the original
   * RiText should be disposed manually to avoid text a doubling effect (via
   * RiText.dispose(originalRiText)).
   */
  public RiText[] splitLetters()
  {
    Object pf = font();
    if (pApplet != null)
      pApplet.textFont((PFont) pf);
    
    String measure = E;
    List result = new ArrayList();
    char[] chars = text.toCharArray();
    for (int i = 0; i < chars.length; i++)
    {
      if (chars[i] != ' ') {
        float tw = pApplet != null ? pApplet.textWidth(measure) : 0;
        result.add(new RiText(pApplet, chars[i], this.x + tw, this.y));
      }
      measure += chars[i];
    }
    return toArray(result);
  }
  
/*    List l = new ArrayList();
    String[] chars = splitChars();
    for (int i = 0; i < chars.length; i++)
    {
      float mx = positionForChar(pf, i);
      l.add(new RiText(_pApplet, chars[i], mx, y, pf));
    }
    return (RiText[]) l.toArray(new RiText[l.size()]);*/


  public boolean startsWith(String cs)
  {
    return text.startsWith(cs);
  }

  public String substring(int start, int end)
  {
    return text.substring(start, end);
  }
  
  public String substring(int start)
  {
    return text.substring(start);
  }

  public String substr(int i, int j)
  {
    return text.substr(i, j);
  }

/*  public char[] toCharArray()
  {
    return text.toCharArray();
  }*/

  public RiTextIF toLowerCase()
  {
    text.toLowerCase();
    return this;
  }

  public RiTextIF toUpperCase()
  {
    text.toUpperCase();
    return this;
  }

  public RiTextIF trim()
  {
    text.trim();
    return this;
  }

  public String wordAt(int wordIdx)
  {
    return text.wordAt(wordIdx);
  }

  public int wordCount()
  {
    return text.wordCount();
  }

  public String[] words()
  {
    return text.words();
  }

  public String[] match(String s)
  {
    return text.match(s);
  }
  
  public String[] match(String s, int flags)
  {
    return text.match(s, flags);
  }

  public RiTextIF removeWord(int idx)
  {
     text.removeWord(idx);
     return this;
  }

  public RiTextIF concat(RiText cs)
  {
    text.concat(cs.text); 
    return this;
  }

  // only for interface
  public RiTextIF concat(RiTextIF cs)
  {
    text.concat(cs.text()); 
    return this;
  }
  
  
  ////// 12 methods for each

  public static RiText[] createLines(PApplet p, String txt, float x, float y)
  {
    return createLines(p, txt, x, y, p.width-x, Float.MAX_VALUE);
  }

  public static RiText[] createLines(PApplet p, String txt, float x, float y, float w)
  {
    return createLines(p, txt, x, y, w, Float.MAX_VALUE);
  }
  
  public static final RiText[] createLines(PApplet p, String txt, float x, float y, float w, float h)
  {
    return createLines(p, txt, x, y, w, h, defaultFont(p));
  }
  
  public static final RiText[] createLines(PApplet p, String txt, float x, float y, float w, float h, float lead)
  {
    return createLines(p, txt, x, y, w, h, defaultFont(p), lead);
  }
  
  public static final RiText[] createLines(PApplet p,  String txt, float x, float y, float w, float h, PFont pf)
  {
    return createLines(p, txt, x, y, w, h, pf, computeLeading(pf));
  }
  
  public static final RiText[] createLines(PApplet p, String txt, float x, float y, float w, float h, PFont pf, float lead)
  {
    if (txt == null || txt.length() == 0) return EMPTY_ARRAY;
    rita.render.PageLayout rp = new PageLayout(p, new Rect(x, y, w, h), p.width, p.height);
    rp.paragraphIndent = defaults.paragraphIndent;
    RiTextIF[] rxts = rp.layout(pf, txt, lead);
    //RiText[] result = new RiText[rxts.length];
    //for (int i = 0; i < result.length; i++)
      //result[i] = (RiText) rxts[i];
    return (RiText[])rxts;
  }
  
  // arrays

  public static final RiText[] createLines(PApplet p, String[] lines, float x, float y)
  {    
    return createLines(p, lines, x, y, Float.MAX_VALUE);
  }
  
  public static final RiText[] createLines(PApplet p, String[] lines, float x, float y, float w)
  {    
    return createLines(p, lines, x, y, w, Float.MAX_VALUE, defaultFont(p));
  }
  
  public static final RiText[] createLines(PApplet p, String[] lines, float x, float y, float w, float h)
  {    
    return createLines(p, lines, x, y, w, h, defaultFont(p));
  }
  
  public static final RiText[] createLines(PApplet p, String[] lines, float x, float y, float w, float h, PFont pf)
  {    
    return createLines(p, lines, x, y, w, h, pf, computeLeading(pf));
  }
  
  public static final RiText[] createLines(PApplet p, String[] lines, float x, float y, float w, float h, float lead)
  {    
    return createLines(p, lines, x, y, w, h, defaultFont(p), lead);
  }
  
  public static final RiText[] createLines(PApplet p, String[] lines, float x, float y, float w, float h, PFont pf, float lead) {
      return layoutArray(p, pf, lines, x, y, h, lead); // width is ignored here
  }
  
  //////
  
  public static RiText[] createWords(PApplet p, String txt, float x, float y)
  {
    return createWords(p, txt, x, y, p.width-x, Float.MAX_VALUE);
  }
  
  public static RiText[] createWords(PApplet p, String txt, float x, float y, float w)
  {
    return createWords(p, txt, x, y, w, Float.MAX_VALUE);
  }
  
  public static final RiText[] createWords(PApplet p, String txt, float x, float y, float w, float h)
  {
    return createWords(p, txt, x, y, w, h, defaultFont(p));
  }
  
  public static final RiText[] createWords(PApplet p, String txt, float x, float y, float w, float h, float lead)
  {
    return createWords(p, txt, x, y, w, h, defaultFont(p), lead);
  }
  
  public static final RiText[] createWords(PApplet p, String txt, float x, float y, float w, float h, PFont pf)
  {
    return createWords(p, txt, x, y, w, h, pf, computeLeading(pf));
  }
  
  public static final RiText[] createWords(PApplet p, String txt, float x, float y, float w, float h, PFont pf, float lead)
  {
    return linesToWords(createLines(p, txt, x, y, w, h, pf, lead)).toArray(EMPTY_ARRAY);
  }
  
  //

  public static final RiText[] createWords(PApplet p, String[] lines, float x, float y)
  {
    return createWords(p, lines, x, y, Float.MAX_VALUE);
  }
  
  public static final RiText[] createWords(PApplet p, String[] lines, float x, float y, float w)
  {
    return createWords(p, lines, x, y, w, Float.MAX_VALUE, defaultFont(p));
  }
  
  public static final RiText[] createWords(PApplet p, String[] lines, float x, float y, float w, float h)
  {    
    return createWords(p, lines, x, y, w, h, defaultFont(p));
  }
  
  public static final RiText[] createWords(PApplet p, String[] lines, float x, float y, float w, float h, PFont pf)
  {    
    return createWords(p, lines, x, y, w, h, pf, computeLeading(pf));
  }
  
  public static final RiText[] createWords(PApplet p, String[] lines, float x, float y, float w, float h, float lead)
  {    
    return createWords(p, lines, x, y, w, h,  defaultFont(p), lead);
  }
  
  public static final RiText[] createWords(PApplet p, String[] lines, float x, float y, float w, float h, PFont pf, float lead) {
    
    return linesToWords(createLines(p, lines, x, y, w, h, pf, lead)).toArray(EMPTY_ARRAY);
  }  

  //////
  
  public static RiText[] createLetters(PApplet p, String txt, float x, float y)
  {
    return createLetters(p, txt, x, y, p.width-x, Float.MAX_VALUE);
  }
  
  public static RiText[] createLetters(PApplet p, String txt, float x, float y, float w)
  {
    return createLetters(p, txt, x, y, w, Float.MAX_VALUE);
  }
  
  public static final RiText[] createLetters(PApplet p, String txt, float x, float y, float w, float h)
  {
    return createLetters(p, txt, x, y, w, h, defaultFont(p));
  }
  
  public static final RiText[] createLetters(PApplet p, String txt, float x, float y, float w, float h, float lead)
  {
    return createLetters(p, txt, x, y, w, h, defaultFont(p), lead);
  }
  
  public static final RiText[] createLetters(PApplet p, String txt, float x, float y, float w, float h, PFont pf)
  {
    return createLetters(p, txt, x, y, w, h, pf, computeLeading(pf));
  }
  
  public static final RiText[] createLetters(PApplet p, String txt, float x, float y, float w, float h, PFont pf, float lead)
  {
    return linesToLetters(createLines(p, txt, x, y, w, h, pf, lead)).toArray(EMPTY_ARRAY);
  }
  
  ///
  
  public static final RiText[] createLetters(PApplet p, String[] lines, float x, float y)
  {    
    return createLetters(p,  lines, x, y, Float.MAX_VALUE);
  }
  
  public static final RiText[] createLetters(PApplet p, String[] lines, float x, float y, float w)
  {    
    return createLetters(p,  lines, x, y, w, Float.MAX_VALUE, defaultFont(p));
  }
  
  public static final RiText[] createLetters(PApplet p, String[] lines, float x, float y, float w, float h)
  {    
    return createLetters(p,  lines, x, y, w, h, defaultFont(p));
  }
  
  public static final RiText[] createLetters(PApplet p, String[] lines, float x, float y, float w, float h, PFont pf)
  {    
    return createLetters(p, lines, x, y, w, h, pf, computeLeading(pf));
  }
  
  public static final RiText[] createLetters(PApplet p, String[] lines, float x, float y, float w, float h, float lead)
  {    
    return createLetters(p, lines, x, y, w, h, defaultFont(p), lead);
  }
  
  public static final RiText[] createLetters(PApplet p, String[] lines, float x, float y,  float w, float h, PFont pf, float lead)
  {
    return linesToLetters(createLines(p, lines, x, y, w, h, pf, lead)).toArray(EMPTY_ARRAY);
  }
  
  //////
  
  /*
   * Ignores any HTML markup and layouts the lines exactly as they are...
   */
  private static RiText[] layoutArray(PApplet pApplet, PFont font, String[] lines, float x, float y, float h, float lead)
  {
    // System.out.println("createLinesByCount("+RiTa.asList(lines)+","+startX+","+startY+","+leading+")");

    if (lines == null || lines.length < 1) return EMPTY_ARRAY;

    List<RiTextIF> ritexts = new LinkedList<RiTextIF>();
    
    for (int i = 0; i < lines.length; i++)
      ritexts.add(new RiText(pApplet, lines[i], x+1, y).font(font));

    constrainLines(ritexts, y, h, lead);

    return ritexts.toArray(EMPTY_ARRAY);
  }

  
  private static List<RiTextIF> linesToWords(RiTextIF[] rlines)
  {
    List<RiTextIF> result = new ArrayList();
    for (int i = 0; rlines != null && i < rlines.length; i++)
    {
      RiTextIF[] rts = rlines[i].splitWords();
      PFont pf = (PFont) rts[0].font();
      for (int j = 0; j < rts.length; j++)
        result.add(rts[j].font(pf));
      RiText.dispose(rlines[i]);
    }
    return result;
  }


  private static List<RiTextIF> linesToLetters(RiTextIF[] rlines)
  {
    List<RiTextIF> result = new ArrayList();
    for (int i = 0; rlines != null && i < rlines.length; i++)
    {
      RiTextIF[] rts = rlines[i].splitLetters();
      PFont pf = (PFont) rts[0].font();
      for (int j = 0; j < rts.length; j++)
        result.add(rts[j].font(pf));
      RiText.dispose(rlines[i]);
    }
    return result;
  }
   
  private static float computeLeading(PFont pf)
  {
    return pf.getSize() * defaults.leadingFactor;
  }

  public boolean autodraw()
  {
    return autodraw;
  }

  public float x()
  {
    return x;
  }

  public float y()
  {
    return y;
  }

  public RiTextIF font(Object pf)
  {
    if (pf instanceof String) {
      
      String fname = (String)pf;
      if (fname.endsWith(".vlw")) { // P5 fonts
        pf = _loadFont(pApplet, fname, -1);
        this.fontSize = ((PFont)pf).getSize();
      }
      else {
        return this.font(fname, DEFAULT_FONT_SIZE);
      }
    }
    if (!(pf instanceof PFont))
      throw new RiTaException("Expected PFont, but got: "+pf.getClass());
    
    this.font = (PFont) pf;
    return this;
  }

}// end

  