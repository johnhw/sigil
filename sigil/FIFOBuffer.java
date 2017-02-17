package sigil;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;



/**
 * FIFO buffer for signal devices to smooth signal timings
 *
 * @author John Williamson
 */
public class FIFOBuffer implements java.io.Serializable
{
    
    //The buffer
    private LinkedList list;
    private int listLength;

    /**
     * Create a new buffer with a particular initial size
     *
     * @param initLength The initial length
     */
    public FIFOBuffer(int initLength)
    {
	listLength = initLength;
	list = new LinkedList();
    }

    /**
     * Show the interface for the buffer, allowing the size 
     * to be changed
     */
  public void show()
    {
	//Create a frame
	JFrame jf = new JFrame("FIFO Buffer");
	Container gc = jf.getContentPane();
	jf.setSize(250, 80);
	gc.setLayout(new BorderLayout());
	
	//Add the size slider
	final JLabel lab = new JLabel("Buffer size "+listLength);
	gc.add("North", lab);
	JSlider slider = new JSlider(1,512,listLength);

	//Listen to size changes
	slider.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent ce)
		{
		    listLength = ((JSlider)(ce.getSource())).getValue();
		    lab.setText("Buffer size "+listLength);
		    while(list.size()>listLength)
			list.removeLast();
		}});

	gc.add("Center", slider);
	jf.show();

  }

    /**
     * Stick another signal onto the buffer
     */
    public void buffer(GestureSignal toBuffer)
    {
	list.addFirst(toBuffer);
	if(list.size()>listLength)
	    {
		list.removeLast();
		System.err.println("Buffer overrun...");
	    }
    }
    
    /**
     * Return the next signal from the buffer
     * 
     * @return The next signal, or null if the buffer is empty
     */
    public GestureSignal unBuffer()
    {
	if(list.size()>1)
	    return (GestureSignal)(list.removeLast());
	else if(list.size()==1)
	    return (GestureSignal)(list.getLast());
        else
	    return null;
    }


}
