import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * SignalDevice: Writes out the string value of incoming heterogenous objects to a text window
 *
 * @author John Williamson
 */

public class HetWatcher extends SProcessorModel
{
    
    //The area in which objects are added
    private transient JTextArea jAreaForward;
    
    //The area in which reverse objects are added
    private transient JTextArea jAreaBackward;

    
    
    //Properties...
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "HetWatcher";
    }
    public String getDescription()
    {
	return "Writes out the string value of incoming heterogenous objects to a text window";
    }
    public String getDate()
    {
	return "Janurary 2002";
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
     * Show the interface window, with the text pane
     */
    public void showInterface()
    {
	JFrame jf;
	jAreaForward = new JTextArea();
	jAreaBackward = new JTextArea();
	jAreaForward.setEditable(false);
	jAreaBackward.setEditable(false);
	JScrollPane jPaneForward = new JScrollPane(jAreaForward);
	JScrollPane jPaneBackward = new JScrollPane(jAreaBackward);
	jf = new JFrame();
	jf.setSize(400,500);
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
					      jPaneForward,
					      jPaneBackward);
	
	jf.getContentPane().add(splitPane);
	jf.setTitle(getName());
	jf.show();
	splitPane.setDividerLocation(0.5);
    }



    public void deleted()
    {
    }
    
    public void processSignal()
    {
	setCurSig(getLastSig());
    }
    
    /**
     * Add the object to the area
     */
    public void processHetObject(Object o)
    {
	if(jAreaForward!=null)
	    jAreaForward.append(o.toString()+"\n");
    }


    /**
     * Add the reverse object to the area
     */
    public void processReverseHetObject(Object o)
    {
	if(jAreaBackward!=null)
	    jAreaBackward.append(o.toString()+"\n");
    }

}
