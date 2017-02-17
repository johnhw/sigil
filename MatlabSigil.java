import sigil.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * Interfaces with Matlab outputs (from Simulink) using native methods
 * Offers input only; cannot return results to Matlab
 *
 * @author John Williamson
 */

public class MatlabSigil extends GeneratorModel 
{
    //The current state of the inputs
    private double [] currentVector;
    private transient JLabel widthLabel;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "MatlabSigil";
    }
    public String getDescription()
    {
	return "Allows one-way communication from Matlab";
    }
    public String getDate()
    {
	return "July 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    public int getSignalWidth()
    {
	if(currentVector!=null)
	    return currentVector.length;
	else
	    return 0;
    }
    
    /**
     * Poll the inputs
     */
    public void pollMatlab()
    {
	int oldSize = getSignalWidth();
	pollMatlabNative();

	currentVector = getMatlabVector();
      
	if(oldSize!=getSignalWidth())
	    {
		propogateWidthChanges();
		if(widthLabel!=null)
		    widthLabel.setText("Current inputs: "+getSignalWidth());
	    }
    }

    /**
     * Execute a poll, and send the signal to consumers
     */
    public void tock()
    {
	pollMatlab();
	GestureSignal gs = new GestureSignal(currentVector,getID());
	setCurrentSignal(gs);
    }

 static
 {
      System.loadLibrary("MatlabSigil");
 }
    
 public native void initMatlab();
 public native void pollMatlabNative();
 public native double [] getMatlabVector();
       
    /**
     * Create and initialize
     */
    public MatlabSigil()
    {
	initMatlab();
    }
    
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}

	initMatlab();
    }

    public void showInterface()
    {
	JFrame jf = new JFrame(getName());
	jf.setSize(200,60);
	widthLabel = new JLabel("Current inputs: "+getSignalWidth());
	jf.getContentPane().add(widthLabel);
	jf.show();
    }
}
