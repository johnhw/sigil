package sigil;
/**
 * Interface for components that want to recieve
 * gesture events
 *
 * @author John Williamson
 */

public interface GestureListener
{
  /**
   * Called when a gesture is generated.
   * The event parameter gives the specific gesture
   */
  public void gestureRecieved(GestureEvent ge);

}
