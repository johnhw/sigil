import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import sigil.*;

/**
 * SignalDevice: Delays a signal by a set number of system ticks
 *
 * @author John Williamson
 */

public class Delay extends SProcessorModel
{
    private int delay = 0; //The delay to use
    private LinkedList queue; //Queue of delayed data
    private static final int maxDelay = 20; //Maximum possible delay

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Delay";
 }
 public String getDescription()
 {
  return "Delays a signal by a set number of system ticks";
 }
 public String getDate()
 {
  return "Feburary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }

    
    public void showInterface()
    {
	
	JFrame jf = new JFrame(getName());
	Container gc = jf.getContentPane();
	jf.setSize(250, 80);
	gc.setLayout(new BorderLayout());
	
	//Add labels and slider
	final JLabel lab = new JLabel("Delay "+delay);
	gc.add("North", lab);
	JSlider slider = new JSlider(0,maxDelay,delay);
	
	//Update delay on slider moves
	slider.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent ce)
		{
		    delay = ((JSlider)(ce.getSource())).getValue();
		    lab.setText("Delay "+delay);}});
	gc.add("Center", slider);
	
	jf.show();
    }

    public void connectionChange()
    {
    }
    
    public Delay()
    {
	super();
	queue = new LinkedList();
    }

    public void processSignal()
    {
	GestureSignal sig = getLastSig();

	//Check signal width
	if(sig.vals.length==sigWidth)
	    {
		active = true;
		//Queue signal
		queue.addLast(sig);
		
		//Send old signal, if buffer has filled
		if(queue.size()>delay && queue.size()>0)
		    setCurSig((GestureSignal)(queue.removeFirst()));
	    }   
    }
    
}
