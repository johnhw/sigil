import java.util.*;
import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

public class Resynthesizer extends SProcessorModel
{
    
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "Resynthesizer";
    }
    public String getDescription()
    {
	return "Uses data from a StateRecognizer to resynthesize the original signal"+
	    "as best as possible";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    private double [] synthSig;
    private int divs = 3;
    private int scale = 20;
    private int dims = 3;
    private int alphabet = 27;

        public int getSignalWidth()
        {
         return 3;
        }


    
    public void connectionChange()
    {
    }
    
    public void clearSynthSig()
    {
	for(int i=0;i<synthSig.length;i++)
	    synthSig[i] = 0.0;
    }
    
    public void setSynthSig(int transition)
    {
	double [] coords = getSpace(transition, scale, divs, dims);
	synthSig[0] = (int)(coords[0]);
	synthSig[1] = (int)(coords[1]);
	synthSig[2] = (int)(coords[2]);
    }

    public static double [] getSpace(int transition, double scale, int divs, int dims)
    {
	double [] coords = new double[3];
	double x = (transition/(divs*divs))*scale;
	double y = ((transition/divs)%divs)*scale;
        double z = (transition % divs)*scale;
	x -= ((divs/2)*scale);
	y -= ((divs/2)*scale);
	z -= ((divs/2)*scale);
	if(dims==3)
	    {
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	    }
	else
	    {
		coords[0] = y;
		coords[1] = z;
		coords[2] = 0;
	    }
	return coords;
    }

    public void showInterface()
    {
    }

	public void processHetObject(Object o)
	{
	if(o instanceof StateInformation)
	    {
		StateInformation sInfo = (StateInformation)o;
		if(sInfo.getBoolean("Transition"))
		    {
			if(divs!=sInfo.getDivisions() || alphabet!=sInfo.getAlphabet())
			    {
				divs = sInfo.getDivisions();
				alphabet = sInfo.getAlphabet();
				dims = (alphabet == divs*divs) ? 2 : 3;

			    }
			setSynthSig(sInfo.getTransition());
		    }
		if(sInfo.getBoolean("Quiescent"))
		    clearSynthSig();
	    }
	}

    public Resynthesizer()
    {
	super();
	synthSig = new double[3];
    }

    public void tock()
    {
	distributeSignal(new GestureSignal(synthSig, getID()));
    }



}
