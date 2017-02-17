import sigil.*;
import duotonic.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Sonifies the output of a probabilistic recognizer
 * using granular synthesis
 *
 * @author John Williamson
 */
public class Granular extends SProcessorModel
{
    

    private Hashtable gestureTable = new Hashtable();
    
    public static int threadId = 0;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "Granular";
    }
    public String getDescription()
    {
	return "Sonifies output from a probabilistic recognizer using granular synthesis";
    }
    public String getDate()
    {
	return "Janurary 2003";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    public void connectionChange()
    {
    }
    
    public void processHetObject(Object o)
    {
	if(o instanceof StateInformation)
	    {
		StateInformation sInfo = (StateInformation)o;
		if(sInfo.getBoolean("Adjusted probabilities"))
		   newProbabilities((Hashtable)(sInfo.getObject("Probabilities")));
		if(sInfo.getBoolean("End") || sInfo.getBoolean("Drop") 
		   || sInfo.getBoolean("Correct"))
		    stopAll();
	    }
    }


    private void stopAll()
    {

    }


    private void adjustProbabilities(int length)
    {
	Enumeration enum = gestureTable.keys();
	int noteIndex = 0;
	while(enum.hasMoreElements())
	    {
		Double probability = (Double)(gestureTable.get(enum.nextElement()));

	    }
    }

    public void newProbabilities(Hashtable probTable)
    {
	
	Enumeration enum = probTable.keys();
	int length  = 0;
	Vector gestureNames = new Vector();
	while(enum.hasMoreElements())
	    {
		String name = (String)(enum.nextElement());
		gestureNames.add(name);
		length++;
	    }
	gestureTable = probTable;
	adjustProbabilities(length);
    }


    private class CloseListener extends WindowAdapter
    {
	public void windowClosing(WindowEvent we)
	{
	    stopAll();
	}
    }


    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.setSize(300,250);
	jf.setTitle(getName());
	jf.addWindowListener(new CloseListener());
	UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
	jf.show();
    }

    public native void setMaxGrains(int grains);
    public native void setNormalized(boolean normalized);
    public native void setAmplitudeModulation(boolean amplitude);
    public native int addGrainSource(String waveName);
    public native void setSourceProbability(int source, double prob);
    public native void setSourceDistribution(int source, double pos, double width);
    public native void initSound();
    public native void stopSound();
    public native void startSound();

    static
    {
	System.loadLibrary("Granular");
    }

    public Granular()
    {
	super();
	new Thread() 
	{
	    public void run()
	    {
		initSound();
		setNormalized(false);
		setAmplitudeModulation(false);
		addGrainSource("h:\\granular\\wavs\\take5.wav");
		addGrainSource("h:\\granular\\wavs\\bass2.wav");
		startSound();
	    }
	}.start();
	
    }

}





