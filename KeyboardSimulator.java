import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 * SignalDevice: Simulates keystrokes based on incoming heterogenous objects
 *
 * @author John Williamson
 */

public class KeyboardSimulator extends SProcessorModel
{
    

    private transient EventQueue systemEventQueue;
    
    //Properties...
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "KeyboardSimulator";
    }
    public String getDescription()
    {
	return "Simulates keystrokes based on incoming heterogenous objects";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    /**
     * No effect when connections are changed
     */
    public void connectionChange()
    {
    }


     /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, getting the new event queue
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}

	systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    }


    /**
     * Create a new keyboard simulator
     */
    public KeyboardSimulator()
    {
	systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
	
    }
        
    /**
     * Show the interface window
     */
    public void showInterface()
    {
	JFrame jf;


	jf = new JFrame();
	jf.setSize(400,400);
	jf.setTitle(getName());
	jf.show();
    }



    public void deleted()
    {
    }
    
    public void processSignal()
    {
    }
    
    /**
     * Post key press, release and typed events to the system event queue
     * Key parameters are the alt/shift/ctrl modifiers, the key code
     * and the key character
     */
    private void simulateKeyStroke(String keyString)
    {
	KeyEvent kePress, keRelease, keType;
	long timestamp = System.currentTimeMillis();
	KeyStroke stroke = KeyStroke.getKeyStroke(keyString);
        try{
        Robot r = new Robot();
        r.keyPress(stroke.getKeyCode());
        r.keyRelease(stroke.getKeyCode());
        } catch(AWTException awtE) {System.err.println("Key events not available...");}
	
    }

    /**
     * Simulate a keystroke based on the incoming object
     */
    public void processHetObject(Object o)
    {	
        simulateKeyStroke("alt D");
    }

}
