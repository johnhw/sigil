import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Converts between Cartesian and polar co-ordinates
 *
 * @author John Williamson
 */
public class Polar extends SProcessorModel
{

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Polar";
 }
 public String getDescription()
 {
   return "Converts between Cartesian and polar co-ordinates";
 }
 public String getDate()
 {
  return "May 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }
 
    private boolean toPolar = true;
    private static final double angleScale = 20.0;

     
    /**
     * Show the interface, with the checkbox for
     * selecting co-ordinate mode
     */
    public void showInterface()
    {

	JFrame jf = new JFrame();
	jf.setSize(400, 80);
	jf.setTitle(getName());
	JCheckBox polarBox = new JCheckBox("To polar", toPolar);
	Container gc = jf.getContentPane();
	gc.setLayout(new BorderLayout());
	gc.add(new JLabel("Convert to/from polar"), BorderLayout.NORTH);
	polarBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    JCheckBox src = (JCheckBox)(ae.getSource());
		    toPolar = src.isSelected();
		}

	    });
	gc.add(polarBox, BorderLayout.SOUTH);
       	jf.show();

    }

 public void connectionChange()
 {
 }

 public Polar()
 {
  super();
 }
    
    /**
     * Converts co-ordinates
     */
    public void processSignal()
    {
	GestureSignal sig = lastSig;
	
	if(sig.vals.length==2)
	    {
		double [] newVals = new double[sig.vals.length];
		    if(toPolar)
			{
			    double r = Math.sqrt(sig.vals[0]*sig.vals[0]+
						 sig.vals[1]*sig.vals[1]);
			    double theta;
			    if(sig.vals[0]!=0.0 && sig.vals[1]!=0.0)
				theta = Math.atan2(sig.vals[0], sig.vals[1]);
			    else
				theta = 0.0;

			    newVals[0] = r;
			    newVals[1] = theta*angleScale;
			}
		    else
			{
			    double x = Math.sin(sig.vals[1]/angleScale)*sig.vals[0];
			    double y = Math.cos(sig.vals[1]/angleScale)*sig.vals[0];
			    newVals[0] = x;
			    newVals[1] = y;			    
			}
		setCurSig(new GestureSignal(newVals,getID()));
	    }   
    }

}





