package sigil;
import java.io.Serializable;
import java.awt.Color;

/**
 * Interface that must be implemented for all signal devices
 *
 * @author John Williamson
 */
public interface GestureGenerator extends Serializable
{

     /**
     * Returns the generic name of the device
     */
    public String getGenName();

   /**
     * Returns the description of the device
     */
    public String getDescription();

    /**
     * Returns the creation date of the device
     */
    public String getDate();

    /**
     * Called when the device is deleted
     */
    public void deleted();

    /**
     * Returns the author of the device
     */
    public String getAuthor();

    /**
     * Connect a processor to this device
     * @param gc The processor to attach
     */
    public void addConsumer(GestureConsumer gc);

    
    /**
     * Returns true if the device 
     * is currently operating synchronously
     */
    public boolean synchronous();
    

    /**
     * Set the current synchronous state
     * of this device
     */
    public void setSynchronous(boolean sync);


    /**
     * Returns true if can operate asynchronously
     */
    public boolean canAsync();

    /**
     * Return the  output width of this device
     */
    public int getSignalWidth();

    /** 
     * Disconnect a processor from this device
     * @param gc The processor to remove
     */
    public void removeConsumer(GestureConsumer gc);

    /**
     * Return true if this device was active
     * on the last tick
     */
    public boolean wasActive();

    /**
     * Show the interface for this device
     */
    public void showInterface();

    /**
     * Set the name of this device
     */
    public void setName(String name);

    
    /**
     * Bring up the interface for changing the buffer settings
     */
    public void modifyBuffer();

    /**
     * Return the device name (not the generic name)
     */
    public String getName();

    /**
     * called on every clock tick
     */
    public void tick();
    
    /**
     * return the unique id of this device 
     */
    public int getID();

    /**
     * Get the color of this device
     */
    public Color getColor();

    /**
     *  Recieve a back propogated heterogenous object
     */
    public void recieveReverseHetObject(Object o);

}
