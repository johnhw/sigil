package sigil;
/**
 * Interface that devices which recieve signals must implement
 *
 * @author John Williamson
 */
public interface GestureConsumer
{
    /**
     * Take a new signal, coming from gGen
     */
    public void newSignal(GestureSignal sig, GestureGenerator gGen);

    /**
     * Connect this machine to the given generator
     */
    public void connect(GestureGenerator gGen);

    /**
     * Disconnect this machine from the given generator 
     */
    public void disconnect(GestureGenerator gGen);
    
    /**
     * Disconnect this machine from all it's parents
     */
    public void disconnectAll();

    /**
     * Called whenever the input is disconnected and reconnected
     */
    public  void connectionChange();

    /**
     * Called if a parent generator changes it's output width
     */
    public void recalculateWidth();

    /**
     * return true if this device is connected to something
     */
    public boolean isConnected();

    /**
     * Sends the given object to all of it's children (if it has any)
     */
    public void propogateHetObject(Object o);

    /**
     * Recieves an incoming object
     */
    public void recieveHetObject(Object o);

    /**
     * Called with an object to process after it has been recieved
     */
    public void processHetObject(Object o);
}
