import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.sound.midi.*;
import java.io.*;

public class RhythmLock extends SProcessorModel
{
    private static final int maxSamples = 8;
    private static final double durThreshold = 50.0;
    private double [] lastVals;
    private double [] maxVals;
    private long [] lastTimes;
    private long [] lastDurations = new long[maxSamples];
    private long [] lastDeviations = new long[maxSamples];
    private double [] currTempo;
    private double cTempo, meanDev;
    private long [] nextTime;
    private int threshold = 10;
    static final long serialVersionUID = 213L;

    public String getGenName()
    {
	return "RhythmLock";
    }

    public String getDescription()
    {
	return "Extracts the tempo from a (periodic) input signal";
    }

    public String getDate()
    {
	return "August 2002";
    }

    public String getAuthor()
    {
	return "John Williamson";
    }

    public int getSignalWidth()
    {
	return 2;
    }
   
    private long calculateDuration(double tempo)
    {
	double dur = 1000.0/(tempo/60.0);
	return (long) dur;
    }
    
    public void connectionChange()
    {
      int width = getInputWidth();
    
      lastVals = new double[width];
      maxVals = new double[width];
      lastTimes = new long[width];
      nextTime = new long[width];
      currTempo = new double[width];
      for(int i=0;i<width;i++)
	  {
	      lastTimes[i] = System.currentTimeMillis();
	      currTempo[i] = 60.0;
	      nextTime[i] = lastTimes[i] + calculateDuration(currTempo[i]);
	  }
    }

    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, re-creating timer
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
    }

    private class SliderListener implements ChangeListener
    {
	private String command;
	
	public void stateChanged(ChangeEvent ce)
	{
	    JSlider slider = (JSlider)(ce.getSource());
	    if(command.equals("Threshold"))
		threshold = slider.getValue();
	}
	public SliderListener(String cmd)
	{
	    command = cmd;
	}
    }

    public void showInterface()
    {
	if(getInputWidth()>0)
	    {
		JFrame jf = new JFrame();
		jf.setTitle(getName());
		jf.setSize(400,300);
		
		Box sliderBox = Box.createVerticalBox();
		JSlider thresholdSlider = new JSlider(0,200,threshold);
		JPanel threshPanel = UIUtils.nameSliderLabelled(thresholdSlider, "Threshold", true);

		thresholdSlider.addChangeListener(new SliderListener("Threshold"));
		sliderBox.add(threshPanel);
		jf.getContentPane().add(sliderBox, BorderLayout.CENTER);
		UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
		jf.show();
	    }
    }

    public RhythmLock()
    {
	super();
    }

    public void deleted()
    {
	
    }
    
    private double calculateTempo(long duration)
    {
	return 60.0/(duration/1000.0);
    }



    private void addNewDuration(long dur, long dev)
    {
	for(int i=0;i<maxSamples-1;i++)
	    {
		lastDurations[i] = lastDurations[i+1];
		lastDeviations[i] = lastDeviations[i+1];
	    }
	lastDurations[maxSamples-1] = dur;
	lastDeviations[maxSamples-1] = dev;
    }

    private long getAverageDuration()
    {
	double accum=0;
	for(int i=0;i<maxSamples;i++)
	    accum+=lastDurations[i];
	accum/=(double)maxSamples;
	return (long)(accum);
    }


    private long getAverageDeviation()
    {
	double accum=0;
	for(int i=0;i<maxSamples;i++)
	    accum+=(lastDeviations[i]*lastDeviations[i]);
	accum/=(double)maxSamples;
	return (long)(Math.sqrt(accum));
    }

    private void zeroCrossing(int axis, int direction)
    {
        int mag =(int)(maxVals[axis]);

       if(mag>threshold)
       {
	   long currTime = System.currentTimeMillis();
	   long deviation = currTime - nextTime[axis];
	   long duration = currTime - lastTimes[axis];
	   if(duration>durThreshold)
	       {
		   addNewDuration(currTime - lastTimes[axis], deviation);
		   cTempo = calculateTempo(getAverageDuration());
		   meanDev = getAverageDeviation();
		   nextTime[0] = currTime + calculateDuration(cTempo);
		   lastTimes[0] = currTime;
	       }
       }
    }

    private void calcZeroCrossings(double [] vals)
    {
        int width = getInputWidth();
	
	if(lastVals[0]<0 && vals[0]>=0)
	    {
		zeroCrossing(0, 0);
		maxVals[0]=0.0;
	    }
	
        else if (lastVals[0]>=0 && vals[0]<0)
	    {
		zeroCrossing(0, 1);
		maxVals[0]=0.0;
	    }
	if(Math.abs(vals[0])>maxVals[0])
	    maxVals[0]=Math.abs(vals[0]);

	lastVals[0] = vals[0];
    }

    private void clearAverages(long dur)
    {
	for(int i=0;i<maxSamples;i++)
	    {
		lastDeviations[i] = 0;
		lastDurations[i] = dur;
	    }
    }

    public void processSignal()
    {
	GestureSignal sig = lastSig;      
	long currTime = System.currentTimeMillis();
	
	if(getInputWidth()>0)
	    {
		long dev = currTime - nextTime[0];
		if(dev > (calculateDuration(cTempo))*2)
		    {
			cTempo=calculateTempo(dev);
			clearAverages(dev);
		    }
		
		double [] outVals = new double[2];
		outVals[0] = cTempo;
		outVals[1] = meanDev;
		active = true;
		calcZeroCrossings(sig.vals);
		
		setCurSig(new GestureSignal(outVals, getID()));
	    }
    }

}


