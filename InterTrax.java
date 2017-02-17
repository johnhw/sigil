import sigil.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

/**
 * Interfaces with the InterTrax II orientation sensor, using a
 * custom dll
 * 
 * @author John Williamson
 */

public class InterTrax extends GeneratorModel 
{
    //The current state of the device
    private double roll, pitch, yaw;

    //The old state of the device
    private double oldRoll=0, oldPitch=0, oldYaw=0;
    
    //The wrapping values for each of the orientations
    private double angWrapRoll=0, angWrapPitch=0, angWrapYaw=0;

    //True if sending record messages
    private boolean recordMode = true;

    //True if not currently in a gesture
    private boolean atStart = true;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "InterTrax";
    }
    public String getDescription()
    {
	return "Interfaces with the InterTrax II orientation sensor";
    }
    public String getDate()
    {
	return "Janurary 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }

    
    public int getSignalWidth()
    {
	return 3;
    }
    
    /**
     * Calculates if the wrap needs to be adjusted
     * avoid discontinuties in the signal (e.g from 180 to -180)
     */
    private void calculateWraps()
    {
        if(pitch<-120.0 && oldPitch>120.0)
                angWrapPitch+=360;
        if(pitch>120.0 && oldPitch<-120.0)
                angWrapPitch-=360;

        if(yaw<-120.0 && oldYaw>120.0)
                angWrapYaw+=360;
        if(yaw>120.0 && oldYaw<-120.0)
                angWrapYaw-=360;
        
        if(roll<-120.0 && oldRoll>120.0)
                angWrapRoll+=360;
        if(roll>120.0 && oldRoll<-120.0)
                angWrapRoll-=360;

    }

    /**
     * Checks if a calibration has occured
     * if it has, sends a segment signal to listener to all consumers
     */
    private void calibrated()
    {
     angWrapRoll = 0.0;
     angWrapYaw = 0.0;
     angWrapPitch = 0.0;
     if(!(oldRoll==0 && oldYaw==0 && oldPitch==0))
	 if(recordMode)
             distributeHetObject("InterTrax:SEGMENT");
	 else
	     {
		 StateInformation stateInfo = new StateInformation();
		 if(atStart)
		     stateInfo.setBoolean("Start");
		 else
		     stateInfo.setBoolean("End");
		 atStart = !atStart;
				 
		 distributeHetObject(stateInfo);
	     }
     
    }

    /**
     * Poll the tracker and update the state of the class
     */
    public void pollTracker()
    {
	pollTrackerNative();
	roll = getRoll();
        pitch = getPitch();
        yaw = getYaw();

        calculateWraps();
        if(roll==0.0 && pitch==0.0 && yaw==0.0)
          calibrated();

        oldRoll = roll;
        oldPitch = pitch;
        oldYaw = yaw;
    }

    /**
     * Execute a poll, and send the signal to consumers
     */
    public void tock()
    {
	double [] sig = new double[3];
	pollTracker();
        sig[0]=roll+angWrapRoll;
        sig[1]=pitch+angWrapPitch;
        sig[2]=yaw+angWrapYaw;
	GestureSignal gs = new GestureSignal(sig,getID());
	setCurrentSignal(gs);
    }

 static
 {
      System.loadLibrary("InterTrax");
 }
    
 public native void initTracker();
 public native void pollTrackerNative();
 public native double getRoll();
 public native double getPitch();
 public native double getYaw();
    
    
    /**
     * Create and initialize
     */
    public InterTrax()
    {
		initTracker();
    }
    
    public void showInterface()
    {
	JFrame jf = new JFrame(getName());
	jf.setSize(200,60);
	JCheckBox recordBox = new JCheckBox("Segment record", recordMode);
	recordBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    boolean val = ((JCheckBox)(ae.getSource())).isSelected();
		    recordMode = val;
		    atStart = true;
		}
	    });
	jf.getContentPane().add(recordBox);
	jf.show();
    }


}
