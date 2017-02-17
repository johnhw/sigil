import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
/**
 * SignalDevice: Uses FM synthesis to sonify 1d signals
 *
 *
 * @author John Williamson
 */

public class FMSynth extends SProcessorModel
{
    static final long serialVersionUID = 213L;
    private transient JPanel targetPanel;
    private transient JLabel harmLab1, harmLab2;
    private int harm1 = 1, harm2 = 2;
    private boolean harmLock;
    private double target, signal, deviation;
    private double centerPt;
    private boolean manualTarget;
    private static boolean swap;

    public String getGenName()
    {
	return "FMSynth";
    }

    public String getDescription()
    {
	return "Sonifies 1d signals";
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
	if(getInputWidth()==0)
	    setVolume(0);
	else
	    setVolume(0.5);
    }

    public void showInterface()
    {
	JFrame jf = new JFrame(getName());
	jf.setSize(600,400);
	Container gc = jf.getContentPane();
	gc.setLayout(new BorderLayout());
	
	JPanel manualPanel = new JPanel();
	JCheckBox targetBox = new JCheckBox("Manual target", manualTarget);
	targetBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    manualTarget = ((JCheckBox)(ae.getSource())).isSelected();
		}

	    });
	manualPanel.add(targetBox);
	gc.add(manualPanel, BorderLayout.SOUTH);

	JPanel harmonicsPanel = new JPanel(new BorderLayout());
	harmonicsPanel.add(new JLabel("Harmonics"), BorderLayout.NORTH);


	JPanel sliderPanel = new JPanel(new GridLayout(1,2));
	JSlider harm1Slider = new JSlider(1,30, harm1);
	JSlider harm2Slider = new JSlider(1,30, harm2);
	harm1Slider.setOrientation(JSlider.VERTICAL);
	harm2Slider.setOrientation(JSlider.VERTICAL);
	
	sliderPanel.add(UIUtils.nameSliderLabelled(harm1Slider, "Numerator", false, 0, 1));
	sliderPanel.add(UIUtils.nameSliderLabelled(harm2Slider, "Denominator", false, 0, 1));

	harm1Slider.addChangeListener(new ChangeListener()
	    {
		public void stateChanged(ChangeEvent ce)
		{
		    int val = ((JSlider)(ce.getSource())).getValue();
		    harm1 = val;
		    updateRatio();
		}

	    });

	harm2Slider.addChangeListener(new ChangeListener()
	    {
		public void stateChanged(ChangeEvent ce)
		{
		    int val = ((JSlider)(ce.getSource())).getValue();
		    harm2 = val;
		    updateRatio();
		}

	    });

	harmonicsPanel.add(sliderPanel, BorderLayout.CENTER);
   	gc.add(harmonicsPanel, BorderLayout.EAST);

	jf.show();
    }

    public void deleted()
    {
	setVolume(0);
	
    }



    private void updateRatio()
    {
	if(swap)
	    setRatio((double)harm2/(double)harm1);
	else
	    setRatio((double)harm1/(double)harm2);
    }

    static
    {
	System.loadLibrary("FMSynth");
	
    }

    public FMSynth()
    {
	setTerminating(true);
	openSynth();
	setPitch(20);
	setVolume(0);
	
    }

    public void processSignal()
    {
	GestureSignal sig = lastSig;
	if(sig.vals.length==sigWidth && sigWidth>0)
	    {
		double dev;
		if(!manualTarget && sigWidth>1)
		    dev = sig.vals[0] - sig.vals[1];
		else
		    dev = sig.vals[0];

		setModulation(dev);
		swap = (dev<0);
		updateRatio();
	    }
    }
    
    private static boolean synthInit = false;                              
    private static native void setVolume(double vol);
    private static native void openSynth();
    private static native void start();
    private static native void stop();
    public static native void setPitch(double pitch);
    public static native void setModulation(double mod);
    public static native void setRatio(double ratio);
    

    
    public static void startSynth()
    {
	if(!synthInit)
	    openSynth();
	else
	    start();
    }
    
    public static void stopSynth()
    {
	start();
    }    
}
