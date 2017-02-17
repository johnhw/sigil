import sigil.*;
import java.io.*;
public class P5XYZ extends P5Glove
{
    static final long serialVersionUID = 213L;
    private boolean recording = false;
    public int getSignalWidth()
    {
	return 3;
    }

    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
	
	initTracker();
    }
	
    
    
    /**
     * Execute a poll, and send the signal to consumers
     */
    public void tock()
    {
	double [] sig = new double[3];
	pollTracker();
    	sig[0]=x;
        sig[1]=y;
        sig[2]=z;
	GestureSignal gs = new GestureSignal(sig,getID());
	setCurrentSignal(gs);
	
	if(buttonState[0]==0 && oldButtonState[0]!=0)
	    if(recording)
		{
		    StateInformation sInfo = new StateInformation();
		    sInfo.setBoolean("Start");
		    distributeHetObject(sInfo);
		    recording = true;
		}
	    else
		{
		    StateInformation sInfo = new StateInformation();
		    sInfo.setBoolean("End");
		    distributeHetObject(sInfo);
		    recording = false;
		}

	if(buttonState[1]==0 && oldButtonState[1]!=0)
	    distributeHetObject("MousePad:SEGMENT");

    }

}
