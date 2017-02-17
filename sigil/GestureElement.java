package sigil;
import java.awt.Color;

/**
 * Abstracts from GestureGenerators and GestureProcessors
 * and provides methods to access their various properties
 *
 * @author John Williamson
 */
public class GestureElement implements java.io.Serializable
{
    
    static final long serialVersionUID = 213L;

    //The attached device
    private GestureGenerator thisGen;

    //True if the device is a generator and not a processor
    private boolean isGen = false;

    //Name of the device
    private String name;

    /** 
     * Return device properties
     */
    public String getDevName()
    {
	return thisGen.getGenName();
    }
    
    /** 
     * Return device properties
     */
    public String getDescription()
    {
	return thisGen.getDescription();
    }


    
    /** 
     * Return device properties
     */
    public String getDate()
    {
	return thisGen.getDate();
    }
    
    /** 
     * Return device properties
     */
    public String getAuthor()
    {
	return thisGen.getAuthor();
    }

    /**
     * Pass a tick onto an attached device
     * Don't tick if there is currently an unhandled exception.
     */
    public void tick()
    {
	thisGen.tick();
    }

    /**
     * Handle a device being removed
     */
    public void deleted()
    {
	thisGen.deleted();
	MasterClock.removeElement(this);
    }


    /**
     * Returns the synchronous state of this device
     */
    public boolean synchronous()
    {
	return thisGen.synchronous();
    }
    
    /**
     * Set the current synchronous state
     * of this device
     */
    public void setSynchronous(boolean sync)
    {
	thisGen.setSynchronous(sync);
    }

    /**
     * Returns devices asynchronizable status
     * 
     */
    public boolean canAsync()
    {
	return thisGen.canAsync();
    }

    /** 
     * return true if this device was active last tick
     */
    public boolean getActive()
    {
	return thisGen.wasActive();
    }


    /**
     * Return the color of this device
     */
    public Color getColor()
    {
	return thisGen.getColor();
    }

    /**
     * Bring up a buffer display if one is available
     */
    public void modifyBuffer()
    {
	if(!isTerminating())
	    thisGen.modifyBuffer();
    }

    /**
     * Return true if this device is a processor
     * and it is attached to something
     */
    public boolean isConnected()
    {
	if(isGen)
	    return false;
	else
	    return ((GestureConsumer)(thisGen)).isConnected();
    }

    /**
     * Make a connection from this element to the passed element
     *
     * @param ge The element to connect to
     */

    public boolean connectTo(GestureElement ge)
    {
	if(!isTerminating() && !ge.isGen && !(ge.isSingleInput() && ge.isConnected()))
	    {
		thisGen.addConsumer((GestureConsumer)(ge.thisGen));
		((GestureConsumer)(ge.thisGen)).connect(thisGen);
		return true;
	    }
	return false;
    }

    /**
     * Show the device's interface
     */
    public void showInterface()
    {
	thisGen.showInterface();
    }
    
    /**
     * Disconnect this device from all it's parents
     */
    public void disconnectAll()
    {
	((GestureConsumer)(thisGen)).disconnectAll();    
    }
    
    /**
     * Disconnect this device from the given element
     *
     */
    public boolean disconnect(GestureElement ge)
    {
	if(!ge.isGen)
	    {
		thisGen.removeConsumer((GestureConsumer)(ge.thisGen));
		((GestureConsumer)(ge.thisGen)).disconnect(thisGen);
		return true;
	    }
	return false;
    }


    /**
     * Get the actual device for this element
     */
    public GestureGenerator getElement()
    {
	return thisGen;
    }

    /**
     * Set the actual device for this element
     */
    public void setElement(Object o)
    {
       if(o instanceof GestureGenerator)
         thisGen = (GestureGenerator)o;
    }

    
    /**
     * Construct a new gesture element
     *
     * @param genClass The class of device to create
     * @param isGenerator Should be true if this is a generator and not a processor
     * @param name The name for this device
     */
    public GestureElement(Class genClass, boolean isGenerator, String name)
    {
	this.name = name;
	isGen = isGenerator;
	try{
	    thisGen = (GestureGenerator)(genClass.newInstance());
	    
	} catch(Exception E) {
	    System.err.println("Could not create an instance of "+name+"!");
	    E.printStackTrace();
	}
	thisGen.setName(name);
    }


    /**
     * Return true if this device does not propogate signals
     */
    public boolean isTerminating()
    {
	if(thisGen instanceof ProcessorModel)
	    {
		return ((ProcessorModel)thisGen).isTerminating();
	    }
	else
	    return false;
    }
    

    /**
     * Return true if this device can only take a single connection at a time
     */
    public boolean isSingleInput()
    {
	if(thisGen instanceof ProcessorModel)
	    {
		return ((ProcessorModel)thisGen).isSingleInput();
	    }
	else
	    return true;
    }
    
    /**
     * Return the output width of this device
     */
    public int getSignalWidth()
    {
	return thisGen.getSignalWidth();
    }
    
    /** 
     * Get the name for this device
     */
    public String getName()
    {
	return name;
    }
    
    /**
     * rename this device
     */
    public void setName(String name)
    {
	this.name = name;
	thisGen.setName(name);
    }

    /**
     * Return true if this device is a generator
     */
    public boolean getGenStat()
    {
	return isGen;
    }



}
