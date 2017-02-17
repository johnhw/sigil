import sigil.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * SignalDevice: Transforms to and from asynchronous signal forms
 * Optionally allows signal to remain on signal path
 *
 * @author John Williamson
 */
public class Asynchronize extends SProcessorModel
{
    //True if converting TO async mode
    private boolean async = true;

    //Buffer of asynchronous incoming objects
    private transient FIFOBuffer asyncBuffer;

    //Properties...
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "Asynchronize";
    }
    public String getDescription()
    {
	return "Transforms to and from asynchronous signal forms";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    public void connectionChange()
    {
    }

    public Asynchronize()
    {
	super();
	asyncBuffer = new FIFOBuffer(20);
    }
    
    /**
     * Show the interface, with the checkbox for
     * selecting asynchronization or resynchronization
     */
    public void showInterface()
    {

	JFrame jf = new JFrame();
	jf.setSize(400, 80);
	jf.setTitle(getName());
	JCheckBox asyncBox = new JCheckBox("To asynchronous", async);
	Container gc = jf.getContentPane();
	gc.setLayout(new BorderLayout());
	gc.add(new JLabel("Convert to/from asynchronous mode"), BorderLayout.NORTH);
	asyncBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    JCheckBox src = (JCheckBox)(ae.getSource());
		    async = src.isSelected();
		}

	    });
	gc.add(asyncBox, BorderLayout.SOUTH);
       	jf.show();

    }

    /**
     * Takes asynchronous objects and puts them back on the synchronous stream
     */
    public void processHetObject(Object o)
    {
	if(!async)
	    {
		
		if(o instanceof GestureSignal)
		    asyncBuffer.buffer((GestureSignal)o);
	    }
    }

    
    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, re-creating buffer
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
	asyncBuffer = new FIFOBuffer(20);
    }

    /**
     * Unbuffers the current async object if one is available
     */
    public void tock()
    {
	if(!async)
	    {
		setCurSig(asyncBuffer.unBuffer());

	    }
    }

    /**
     * Converts to and from asyncronous form 
     */
    public void processSignal()
    {
	GestureSignal sig = lastSig;
	
	if(async)
	    distributeHetObject(new GestureSignal(sig.vals, getID()));
    }

}





