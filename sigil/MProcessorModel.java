package sigil;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Class from which all devices that can take multiple signal paths
 * inherit.
 */
public abstract class MProcessorModel extends ProcessorModel
{
     
    static final long serialVersionUID = 213L;

    //The devices sending input to this device
    protected Vector generators;

    //The list of devices from which this device has not
    //yet recieved the input for this tick
    protected Vector awaitedInputs;

    //The buffer for signals; maps from a reference to the device
    //that sent the signal to the actual signal
    protected Hashtable signalBuffer;

    //True if all devices feedng into this device have
    //sent their signals
    protected boolean inputReady = false;

    //The color for multi-input devices 
    public static Color mprocColor = new Color(38,94,54);

    /**
     * Pass a back propogated heterogenous object
     * to all of this device's parents
     */
    public void propogateReverseHetObject(Object o)
    {
      for(int i=0;i<generators.size();i++)
      {
       GestureGenerator gGen = (GestureGenerator)(generators.get(i));
       gGen.recieveReverseHetObject(o);
      }

    }


    /**
     * Take the current signal buffer and do something with it.
     * Subclasses must override this to do something useful
     */
    public abstract void processSignalBuffer();    

    /**
     * Return the color of this device
     */
    public Color getColor()
    {
	return mprocColor;
    }

    /**
     * Returns the current signal buffer hashtable
     */
    public Hashtable getSignalBuffer()
    {
	return signalBuffer;
    }

    /**
     * Returns true if one or more devices are connected to
     * the inputs of this device
     */
    public boolean isConnected()
    {
	return generators.size()>0;
    }

   /**
    * Calculate the total input width  for this device by summing
    * the output width of all devices feeding into it.
    * Propogates any changes in output width to its children, and
    * calls connectionChange();
    */
   public void recalculateWidth()
   {
    sigWidth = 0;

    for(int i=0;i<generators.size();i++)
      sigWidth += ((GestureGenerator)(generators.get(i))).getSignalWidth();

    propogateWidthChanges();
    connectionChange();
   } 

   /**
    * Removes the specified device from the list of
    * input devices. Forces a width recalculation.
    */
   public final void disconnect(GestureGenerator gs)
   { 
    if(generators.contains(gs))
    { 
     generators.remove(gs);
     recalculateWidth();
    }
  }

 /**
  *  Disconnects all input devices from this device and
  *  updates the current width
  */
 public final void disconnectAll()
 {
  generators = new Vector();
   recalculateWidth();
 }
 /**
  * Called every clock tick. 
  */
 public void tock()
 {


 }


 /**
   * Called every clock tick. Distributes the currently buffered signal
   * and buffers a new output if all generators have sent their signals
   * and then calls tock() to execute the bottom half
   */
 public final void tick()
 {
    if(!terminating)
    {
      distributeSignal(fBuf.unBuffer());
      if(inputReady)
      {
            awaitedInputs = (Vector)(generators.clone());
            signalBuffer = new Hashtable();
            inputReady = false;
	    for(int i=0;i<consumers.size();i++)
		{
		    ProcessorModel nextToTick = (ProcessorModel)(consumers.get(i));
		    if(!nextToTick.synchronous())
			nextToTick.tick();
		}
	
      }
    }
 }


 public void connect(GestureGenerator gs)
 {
   generators.add(gs);
   recalculateWidth();
 }

 /**
  * Takes a new input from the given device and puts it in the
  * current input buffer. If all connected devices have passed their
  * signal, calls processSignalBuffer()
  */
 public final void newSignal(GestureSignal sig, GestureGenerator gGen)
 {
  if(!inputReady)
  {
          awaitedInputs.remove(gGen);
          signalBuffer.put(gGen, sig);
  }

  if(awaitedInputs.size()==0)
  {
          processSignalBuffer();
          inputReady = true;
	 
  }

 }

 /**
  * Returns false; all MProcessorModels take multiple inputs
  */
 public boolean isSingleInput()
 {
  return false;
 }

 /**
  * Creates a new model. Note that it is IMPERATIVE that
  * subclasses call super() in their own constructor!
  */
 public MProcessorModel()
 {
   super();
   generators = new Vector();
   awaitedInputs = new Vector();
   signalBuffer = new Hashtable();
 }

}

