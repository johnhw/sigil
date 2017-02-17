package sigil;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

/**
 * Model for all generator devices (those that take no input)
 *
 * @author John Williamson
 */

public abstract class GeneratorModel implements GestureGenerator
{
    
    static final long serialVersionUID = 213L;

    //The current signal to be sent
    protected GestureSignal curSig;
    
    //The buffer for this device
    protected FIFOBuffer fBuf = new FIFOBuffer(200);

    //Color for generators
    public static Color genColor = new Color(71,82,133);

    
    //Device name (specific device name, not generic)
    protected String name;

    //The processors that this generator is connected to
    protected Vector consumers = new Vector();

    //True if this device sent a signal on the last tick
    protected boolean active = false;

    //The unique id for this particular device
    protected  int id = ElementID.getID();

    /**
     * Default implentation; does nothing
     */
    public void deleted()
    {

    }

     /**
     * Returns the generic name of the device
     */
    public String getGenName()
    {
	return "Unknown generator";
    }

    /**
     * Returns the description of the device
     */
    public String getDescription()
    {
	return "None";
    }

    /**
     * Returns the creation date of the device
     */
    public String getDate()
    {
	return "Not available";
    }

    /**
     * Returns the author of the device
     */
    public String getAuthor()
    {
	return "Unknown";
    }
    
     
    /**
     * Returns true - generators are always synchronous
     */
    public boolean synchronous()
    {
	return true;
    }
    
    /**
     * Set the current synchronous state
     * Does nothing for generators
     */
    public void setSynchronous(boolean sync)
    {
    }

    /**
     * Returns false; all generators are synchronous by
     * nature
     */
    public boolean canAsync()
    {
	return false;
    }


    /**
     * Return this device's color
     * can be overriden for special effects (devices going red for recording, or whatever)
     */
    public Color getColor()
    {
	return genColor;
    }

    /**
     * Bring up the interface for changing the buffer settings
     */
    public void modifyBuffer()
    {
	fBuf.show();
    }

    /**
     * Tell all attached consumers of this device that the output
     * width of this device has changed, and they should update
     */
    protected void propogateWidthChanges()
    {
	for(int j=0;j<consumers.size();j++)
	    {
		((GestureConsumer)(consumers.get(j))).recalculateWidth();
	    }
    }

    /**
     * This method is always called every system tick
     * can override this to do something like poll devices
     */
    public void tock()
    {
	
    }

    /**
     * Recieve a back propogated heterogenous object
     * In this case, just calls processReverseHetObject(o);
     */
    public void recieveReverseHetObject(Object o)
    {
     processReverseHetObject(o);
    }

    /**
     * Process a back propogated heterogenous object
     */
    public void processReverseHetObject(Object o)
    {

    }

    /**
     * Send a heterogenous object to all the attached consumers
     */
    public void distributeHetObject(final Object o)
    {
	new Thread()
	{
	    public void run()
	    {
		for(int i=0;i<consumers.size();i++)
		    ((GestureConsumer)(consumers.get(i))).recieveHetObject(o);
	    }
	}.start();
    }

    /** 
     * Distributes the next signal from the buffer to
     * each of the attached children, then calls the bottom half (tock)
     */
    public final void tick()
    {
	distributeSignal(fBuf.unBuffer());
	for(int i=0;i<consumers.size();i++)
	    {
		ProcessorModel nextToTick = (ProcessorModel)(consumers.get(i));
		if(!nextToTick.synchronous())
		    nextToTick.tick();
	    }
	tock();
    }

    /**
     * Add the passed signal to the current outgoing buffer
     */
    public void setCurrentSignal(GestureSignal gs)
    {
	fBuf.buffer(gs);
    }

    /**
     * Set the name of this device
     */
    public void setName(String name)
    {
	this.name = name;
    }

    /**
     * Return the device name (not the generic name) along with it's
     * unique id
     */
    public String getName()
    {
	return name+" [ID:"+getID()+"]";
    }



    /**
     * Return the devices unique ID
     */
    public int getID()
    {
	return id;
    }

    
    /**
     * Return true if this device was active
     * on the last tick
     */
    public boolean wasActive()
    {
	if(active)
	    {
		active=false;
		return true;
	    }
	else
	    return false;
    }

    /**
     * Connect a processor to this device
     * @param gc The processor to attach
     */
    public void addConsumer(GestureConsumer gc)
    {
	consumers.add(gc);
    }

    /** 
     * Disconnect a processor from this device
     * @param gc The processor to remove
     */
    public void removeConsumer(GestureConsumer gc)
    {
	consumers.remove(gc);
    }
    
    /**
     * Return the  output width of this generator
     * This should be overriden by subclasses; by default will return 0
     */
    public int getSignalWidth()
    {
	return 0;
    }

    /**
     * Distribute the given signal to all of the connected processors
     * This should generally not be overriden except in special circumstances
     */
    public void distributeSignal(GestureSignal gs)
    {
	active=true;
	
	if(gs!=null)
	    {
		curSig=gs;
		for(int i=0;i<consumers.size();i++)
		    ((GestureConsumer)(consumers.get(i))).newSignal(gs, this);
	    }
	else if(curSig!=null)
	    {
		for(int i=0;i<consumers.size();i++)
		    ((GestureConsumer)(consumers.get(i))).newSignal(curSig, this);
	    }
    }
    
    /**
     * Show the interface for this device
     * Subclasses should override this; does nothing by default
     */
    public void showInterface()
    {
	
    }
    

}
