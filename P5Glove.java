import sigil.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

/**
 * Interfaces with the P5 Glove sensor
 * custom dll
 * 
 * @author John Williamson
 */

public class P5Glove extends GeneratorModel 
{
    //The current state of the device
    protected double roll, pitch, yaw, x, y, z;
    
    //The state of the fingers
    private int [] fingerState = new int[5];
    private int [] oldFingerState = new int[5];

    //The state of the buttons
    protected int [] buttonState = new int[4];
    protected int [] oldButtonState = new int[5];

    //The old state of the device
    private double oldRoll=0, oldPitch=0, oldYaw=0, oldX=0, oldY=0, oldZ=0;
    
    //The wrapping values for each of the orientations
    private double angWrapRoll=0, angWrapPitch=0, angWrapYaw=0;

    //True if sending record messages
    private boolean recordMode = true;

    //True if not currently in a gesture
    private boolean atStart = true;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "P5Glove";
    }
    public String getDescription()
    {
	return "Interfaces with the P5 glove sensor";
    }
    public String getDate()
    {
	return "Decemeber 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }

    
    public int getSignalWidth()
    {
	return 11;
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
    private void buttonPressCheck()
    {
	for(int i=0;i<4;i++)
	    if(buttonState[i]!=oldButtonState[i])
		{
		    StateInformation stateInfo = new StateInformation();
		    stateInfo.setBoolean("ButtonPress");
		    stateInfo.setObject("Button", new Integer(i));
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
	x = getX();
	y = getY();
	z = getZ();
	for(int i=0;i<5;i++)
	    oldFingerState[i] = fingerState[i];
	for(int i=0;i<4;i++)
	    oldButtonState[i] = buttonState[i];
	for(int i=0;i<5;i++)
	    fingerState[i] = getFinger(i);
	for(int i=0;i<4;i++)
	    buttonState[i] = getButton(i);


        calculateWraps();
	buttonPressCheck();
        oldRoll = roll;
        oldPitch = pitch;
        oldYaw = yaw;
	oldX = x;
	oldY = y;
	oldZ = z;
	
	
    }

    /**
     * Execute a poll, and send the signal to consumers
     */
    public void tock()
    {
	double [] sig = new double[11];
	pollTracker();
        sig[0]=roll+angWrapRoll;
        sig[1]=pitch+angWrapPitch;
        sig[2]=yaw+angWrapYaw;
	sig[3]=x;
        sig[4]=y;
        sig[5]=z;
	for(int i=0;i<5;i++)
	    sig[6+i] = fingerState[i];
	GestureSignal gs = new GestureSignal(sig,getID());
	setCurrentSignal(gs);
    }

 static
 {
      System.loadLibrary("P5Glove");
 }
    
    public native void initTracker();
    public native void pollTrackerNative();
    public native double getRoll();
    public native double getPitch();
    public native double getYaw();
    public native double getX();
    public native double getY();
    public native double getZ();
    public native int getFinger(int finger);
    public native int getButton(int button);
    
    
    /**
     * Create and initialize
     */
    public P5Glove()
    {
		initTracker();
    }
    
    public void showInterface()
    {
    }


}
