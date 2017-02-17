/** 
 * SignalDevice: Combines multiple signals
 *
 * @author John Williamson
 */

import javax.swing.*;
import java.awt.*;
import sigil.*;

public class Combiner extends MProcessorModel
{

  
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "Combiner";
    }
    public String getDescription()
    {
	return "Combines multiple signal inputs into a single wide output";
    }
    public String getDate()
    {
	return "Janurary 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }


    public void connectionChange()
    {
	
    }
    
    /**
     * Merge the buffers together
     */
    public void processSignalBuffer()
    {
	//Merge buffers
        GestureSignal gSig = (GestureSignal)(SignalUtils.mergeBuffer(getSignalBuffer()));   
	gSig.lastID = getID();
	setCurSig(gSig); //Send signal
    }

    public Combiner()
    {
	super();
    }
    
    public void showInterface()
    {
	
    }


}
