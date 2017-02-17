package sigil;
import java.awt.event.*;
/**
 * Common mouse manipulation utilities
 *
 * @author John Williamson
 */
public class MouseUtils
{
  /**
   * True if the specified event involved the left mouse button
   */
  public static boolean left(MouseEvent me)
  {
   return (me.getModifiers() & MouseEvent.BUTTON1_MASK)!=0;
  }

  /**
   * True if the specified event involved the middle mouse button
   */
  public static boolean middle(MouseEvent me)
  {
   return (me.getModifiers() & MouseEvent.BUTTON2_MASK)!=0;
  }

  /**
   * True if the specified event involved the right mouse button
   */
  public static boolean right(MouseEvent me)
  {
   return (me.getModifiers() & MouseEvent.BUTTON3_MASK)!=0;
  }

  /**
   * True if the ctrl was down during the specified event 
   */
  public static boolean ctrl(MouseEvent me)
  {
   return (me.getModifiers() & MouseEvent.CTRL_MASK)!=0;
  }

  /**
   * True if the shift was down during the specified event 
   */
  public static boolean shift(MouseEvent me)
  {
   return (me.getModifiers() & MouseEvent.SHIFT_MASK)!=0;
  }

  /**
   * True if the alt was down during the specified event 
   */
  public static boolean alt(MouseEvent me)
  {
   return (me.getModifiers() & MouseEvent.ALT_MASK)!=0;
  }

  /**
   * True if the specified event was a double left click
   */
  public static boolean dblClick(MouseEvent me)
  {
   return left(me) && me.getClickCount()==2;
  }





}
