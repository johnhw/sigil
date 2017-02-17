import sigil.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * SignalDevice: Synchronizes incoming heterogenous objects to the master clock
 *
 * @author John Williamson
 */

public class HetSynchronizer extends SProcessorModel
{
    private boolean syncReverse = false, syncForward = false;
    private int maxSize = 100, dropSize = 50;
    private transient LinkedList forwardBuffer = new LinkedList();
    private transient LinkedList backwardBuffer = new LinkedList();

    //Properties...
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "HetSynchronizer";
    }
    public String getDescription()
    {
	return "Synchronizes incoming heterogenous objects to the master clock";
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
     * Cannot run in asynchronous mode
     */
    public boolean canAsync()
    {
	return false;
    }

    /**
     * Always returns true; cannot be desynchronized
     */
    public boolean getSynchronous()
    {
	return true;
    } 

    /**
     * Pass signals through unchanged (but synchronized)
     */
    public void processSignal()
    {
	setCurSig(getLastSig());
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
     * Deserialize
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
	forwardBuffer = new LinkedList();
	backwardBuffer = new LinkedList();
    }

    /**
     * Listener for when the
     * synchronize check boxes are changed
     */
    private class CheckListener implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    String command = ae.getActionCommand();
	    boolean val = ((JCheckBox)(ae.getSource())).isSelected();
	    if(command.equals("Synchronize forward"))
		syncForward = val;
	    else if(command.equals("Synchronize reverse"))
		syncReverse = val;
	}

    }

        
    /**
     * Show the interface window, with the text pane
     */
    public void showInterface()
    {
	Box buttonBox = Box.createVerticalBox();

	//Add checkboxes for the two directions
	JCheckBox syncForwardBox = new JCheckBox("Synchronize forward", syncForward);
	JCheckBox syncReverseBox = new JCheckBox("Synchronize reverse", syncReverse);
	syncForwardBox.addActionListener(new CheckListener());
	syncReverseBox.addActionListener(new CheckListener());
	buttonBox.add(syncForwardBox);
	buttonBox.add(syncReverseBox);	

	//Show the frame
	JFrame jf = new JFrame();
	jf.setSize(250,80);
	jf.getContentPane().add(buttonBox);
	UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
	jf.setTitle(getName());
	jf.show();

    }



    public void deleted()
    {
    }
    
    /**
     * Distribute the next object from the buffer
     * Will drop buffer entries if the maximum size is exceeded
     * In this case buffer will be reduced to dropSize
     */
    public void tock()
    {
	if(forwardBuffer.size()>0)
	    propogateHetObject(forwardBuffer.removeLast());
	if(backwardBuffer.size()>0)
	    propogateHetObject(backwardBuffer.removeLast());

	if(forwardBuffer.size()>maxSize)
	    {
		System.err.println("Forward buffer overrun in "+getName());
		while(forwardBuffer.size()>dropSize)
		    forwardBuffer.removeLast();
	    }
	
	if(backwardBuffer.size()>maxSize)
	    {
		System.err.println("Backward buffer overrun in "+getName());
		while(backwardBuffer.size()>dropSize)
		    backwardBuffer.removeLast();
	    }
    }
    
    /**
     * Synchronize incoming objects as required
     */
    public void recieveHetObject(Object o)
    {
	if(syncForward)
	    forwardBuffer.addFirst(o);
	else
	    propogateReverseHetObject(o);

    }


    /**
     * Synchronize incoming backwards objects as required
     */
    public void recieveReverseHetObject(Object o)
    {
	if(syncReverse)
	    backwardBuffer.addFirst(o);
	else
	    propogateReverseHetObject(o);
    }

}
