package sigil;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public abstract class SProcessorModel extends ProcessorModel
{
    protected GestureSignal lastSig;
    protected GestureGenerator conn;
     
    static final long serialVersionUID = 213L;
    
    public void processSignal()
    {
	setCurSig(lastSig);
    }
    
     /**
     * Returns true if the device 
     * is currently operating synchronously
     */
    public boolean synchronous()
    {
	return synchronous;
    }
    

    /**
     * Set the current synchronous state
     * of this device
     */
    public void setSynchronous(boolean sync)
    {
	synchronous = sync;
    }


    /**
     * Returns true if can operate asynchronously
     */
    public boolean canAsync()
    {
	return true;
    }


    public GestureSignal getLastSig()
    {
	return lastSig;
    }


    public void propogateReverseHetObject(Object o)
    {
     conn.recieveReverseHetObject(o);
    }


 public void recalculateWidth()
 {
  if(conn!=null)
          sigWidth = conn.getSignalWidth();
  else
        sigWidth = 0;
  propogateWidthChanges();
  connectionChange();
 }

 public boolean isConnected()
 {
  return conn!=null;
 }


 public void disconnect(GestureGenerator gs)
 {
  conn = null;
  recalculateWidth();
 }

 public void connect(GestureGenerator gs)
 {
  if(conn==null)
  {
   conn=gs;
   recalculateWidth();
  }
 }

 public void disconnectAll()
 {
   conn = null;
   recalculateWidth();
 }

  public final void tick()
  {
    if(!terminating)
	distributeSignal(fBuf.unBuffer());
    
    for(int i=0;i<consumers.size();i++)
	{
	    ProcessorModel nextToTick = (ProcessorModel)(consumers.get(i));
	    if(!nextToTick.synchronous())
		nextToTick.tick();
	}
   
	
    tock();

  }

  public void tock()
  {

  }

 public final void newSignal(GestureSignal sig, GestureGenerator gGen)
 {
     lastSig = sig;
     processSignal();     
 }

 public boolean isSingleInput()
 {
  return true;
 }


}
