import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Takes an FFT of the incoming signal
 *
 * @author John Williamson
 */
public class Spectral extends SProcessorModel
{

    static final long serialVersionUID = 213L;
    private static final int nSamples = 256;
    private double [] samples = new double[nSamples];
    private transient JPanel fftPanel;
    private transient FFT curFFT;

 public String getGenName()
 {
  return "Spectral";
 }
 public String getDescription()
 {
     return "Takes the FFT of the incoming signal, and outputs its fundamental";
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

 public Spectral()
 {
  super();
 }

    private class FFTPanel extends JPanel
    {
	public void paint(Graphics g)
	{
	    g.setColor(Color.black);
	    Dimension dSize = getSize();
	    g.fillRect(0,0,dSize.width, dSize.height);
	    if(curFFT!=null)
		{
		    double [] amps = curFFT.getAmplitudeSpectrum();
		    g.setColor(Color.red);
		    for(int i=0;i<amps.length;i++)
			g.drawLine(i,dSize.height,i,dSize.height-
				   (int)(amps[i]/100000.0));
		}
	}

    }
    
    public void showInterface()
    {
	JFrame jf = new JFrame(getName());
	fftPanel = new FFTPanel();
	jf.getContentPane().add(fftPanel);
	jf.setSize(300, 300);
	jf.show();
    }
       
    private double getFundamental()
    {
	curFFT = new FFT(samples);
	return curFFT.getDominant(1, 4, 100);
    }

    private void addSample(double sample)
    {
	for(int i=0;i<nSamples-1;i++)
	    samples[i] = samples[i+1];
	
	samples[nSamples-1] = sample;
    }

    public void processSignal()
    {
	GestureSignal sig = lastSig;
	
	if(sig.vals.length==sigWidth)
	    {
		addSample(sig.vals[0]);
		double [] newVals = new double[1];
		newVals[0] = (getFundamental() * 60.0) * 2.0;
		setCurSig(new GestureSignal(newVals,getID()));
		if(fftPanel!=null)
		    fftPanel.repaint();
	    }   
    }
    
}





