import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Takes the sine of an incoming waveform
 *
 * @author John Williamson
 */
public class SineModulator extends SProcessorModel
{

    int multiplier = 1;
    
    static final long serialVersionUID = 213L;
    
    public String getGenName()
    {
	return "SineModulator";
    }
    public String getDescription()
    {
	return "Sine modulates incoming signals";
    }
    public String getDate()
    {
	return "August 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
 
    public void connectionChange()
    {
    }
    
    public SineModulator()
    {
	super();
    }
    
    private class SliderListener implements ChangeListener
    {
	public void stateChanged(ChangeEvent ce)
	{
	    multiplier = ((JSlider)(ce.getSource())).getValue();
	}
    }

    public void showInterface()
    {
	JFrame jf = new JFrame(getName());
	jf.setSize(200,150);
	JSlider multSlider = new JSlider(-10, 10, 1);
	multSlider.addChangeListener(new SliderListener());
	JPanel sliderPanel = UIUtils.nameSliderLabelled(multSlider,
							"Multiplier",
							true, 0, 1);
	jf.getContentPane().add(sliderPanel);
	jf.show();
    }


    /**
     * Processes the signal
     */
    public void processSignal()
    {
	GestureSignal sig = lastSig;
	
	if(sig.vals.length==sigWidth)
	    {
		double realMult;
		if(multiplier>=0)
		    realMult = multiplier;
		else
		    realMult = 1.0/((double)Math.abs(multiplier));
		
		active = true;
		double [] newVals = new double[sig.vals.length];
		for(int i=0;i<sig.vals.length;i++)
		    newVals[i] = Math.sin(sig.vals[i]/180.0*Math.PI*realMult)*100; 
		setCurSig(new GestureSignal(newVals,getID()));
	    }   
    }

}





