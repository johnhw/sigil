import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Multiplies incoming signals by -1
 *
 * @author John Williamson
 */
public class Inverter extends SProcessorModel
{

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Inverter";
 }
 public String getDescription()
 {
  return "Multiplies incoming signals by -1";
 }
 public String getDate()
 {
  return "Feburary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }
 
 public void connectionChange()
 {
 }

 public Inverter()
 {
  super();
 }
    
    /**
     * Inverts the signal
     */
    public void processSignal()
    {
	GestureSignal sig = lastSig;
	
	if(sig.vals.length==sigWidth)
	    {
		active = true;
		double [] newVals = new double[sig.vals.length];
		for(int i=0;i<sig.vals.length;i++)
		    newVals[i] = -1.0*sig.vals[i];      
		setCurSig(new GestureSignal(newVals,getID()));
	    }   
    }

}





