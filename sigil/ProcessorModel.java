package sigil;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public abstract class ProcessorModel implements GestureProcessor
{

    protected String devName; 
    protected int sigWidth;
    protected boolean terminating = false;
    protected FIFOBuffer fBuf = new FIFOBuffer(16);
    public static Color procColor = new Color(38,74,98);
    protected GestureSignal curSig = null;
    static final long serialVersionUID = 213L;
    protected boolean synchronous;
    
    /**
     * Return the current input signal
     */
    public GestureSignal getCurrentSignal()
    {
	return curSig;
    }

    /**
     * Default implentation; does nothing
     */
    public void deleted()
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
		propogateHetObject(o);
	    }
	}.start();
    }

 public String getGenName()
 {
  return "Unknown processor";
 }

 public String getDescription()
 {
  return "None";
 }

 public String getDate()
 {
  return "Not available";
 }

 public String getAuthor()
 {
  return "Unknown";
 }


 public void setName(String name)
 {
  devName = name;

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

    public Color getColor()
    {
	return procColor;

    }

    protected void setTerminating(boolean term)
    {
	terminating = term;
    }
    
    public String getName()
    {
	return devName+" [ID:"+getID()+"]";
    }
    
    protected int id = ElementID.getID();
    
    public int getID()
    {
	return id;
    }

 public void showInterface()
 {
   

 }

 ProcessorModel()
 {
   sigWidth = 0;
   synchronous = true;
 }

 protected Vector consumers = new Vector();

 protected void propogateWidthChanges()
 {
  for(int j=0;j<consumers.size();j++)
  {
   ((GestureConsumer)(consumers.get(j))).recalculateWidth();
  }
 }

 public final void addConsumer(GestureConsumer gc)
 {
  consumers.add(gc);
  connectionChange();
 }

 public final void setCurSig(GestureSignal gs)
 {
  if(!terminating)
           fBuf.buffer(gs);
 }

 public final void removeConsumer(GestureConsumer gc)
 {
  consumers.remove(gc);
  connectionChange();
 }


    public abstract void propogateReverseHetObject(Object o);

    public void recieveReverseHetObject(Object o)
    {
     propogateReverseHetObject(o);
     processReverseHetObject(o);
    }

    public void processReverseHetObject(Object o)
    {

    }


 
    public void propogateHetObject(Object o)
    {
	for(int i=0;i<consumers.size();i++)
	    ((GestureConsumer)(consumers.get(i))).recieveHetObject(o);
	    
    }

    public void distributeReverseHetObject(final Object o)
    {
	new Thread()
	{
	    public void run()
	    {
		propogateReverseHetObject(o);
	    }
	}.start();
    }

    public void processHetObject(Object o)
    {

    }

    public void recieveHetObject(Object o)
    {
	processHetObject(o);
	propogateHetObject(o);
    }


 protected boolean active = false;

 public final boolean wasActive()
 {
  if(active)
  {
   active=false;
   return true;
  }
  else
  return false;
 }

 public void modifyBuffer()
 {
  fBuf.show();
 }

 public int getSignalWidth()
 {
  return sigWidth;
 }

 public int getInputWidth()
 {
  return sigWidth;
 }




 public final void distributeSignal(GestureSignal sig)
 {

    if(sig!=null)
    {
   curSig=sig;
   for(int i=0;i<consumers.size();i++)
     ((GestureConsumer)(consumers.get(i))).newSignal(sig, this);
   }
  else if(curSig!=null && getInputWidth()>0)
   {
           for(int i=0;i<consumers.size();i++)
             ((GestureConsumer)(consumers.get(i))).newSignal(curSig, this);
   }

 }

 public boolean isTerminating()
 {
  return terminating;
 }

 public abstract boolean isSingleInput();


}
