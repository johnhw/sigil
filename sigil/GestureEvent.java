package sigil;
import java.awt.AWTEvent;

/**
 * Class encapsulating gesture event information
 * for use with GestureListeners
 *
 * @author John Williamson
 */

 public class GestureEvent extends AWTEvent
 {
    /**
     * ID for gesture events
     */
    public static final int GESTURE_EVENT = 1018025;

    //The name of gesture for this event
    private String gestureName;

    /**
     * Construct a new event, given a source object
     * and a gesture name
     */
    public GestureEvent(Object src, String gestureName)
    {
      super(src, GESTURE_EVENT);
      this.gestureName = gestureName;
    }

    /**
     * Return the name of the gesture
     */
    public String getName()
    {
       return gestureName;
    }


 }
