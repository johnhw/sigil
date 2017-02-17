import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Brings up a message after a cetain time has elapsed
 *
 * @author John Williamson
 */
public class Timer extends SProcessorModel
{

    private transient CountdownTimer timerThread;
    private JToggleButton startButton;

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Timer";
 }
 public String getDescription()
 {
     return "Brings up a message box after a certain time has elapsed";
 }
    public String getDate()
 {
  return "March 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }


    private class CountdownTimer extends Thread
    {
	private String message;
	private int duration;
	private boolean showMessage = true;

	public void disableMessage()
	{
	    showMessage = false;
	}

	public CountdownTimer(String message, int duration)
	{
	    this.message = message;
	    this.duration = duration;
	}
	
	public void run()
	{
	    try{sleep(duration*1000);}catch(InterruptedException ie){}
	    if(showMessage)
		{
		    JOptionPane.showMessageDialog(null, message, "Timer expired", JOptionPane.INFORMATION_MESSAGE);
		    startButton.setSelected(false);
		}
	}
    }
    
    public void showInterface()
    {
	JFrame jf = new JFrame();
	JLabel duration = new JLabel("Duration (s)");
	final JTextField durationField = new JTextField(20);
	startButton = new JToggleButton("Start");
	JPanel durationPanel = new JPanel(new FlowLayout());
	durationPanel.add(duration);
	durationPanel.add(durationField);
	Container gc = jf.getContentPane();
	gc.add(durationPanel, BorderLayout.CENTER);
	gc.add(startButton, BorderLayout.SOUTH);
	startButton.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    JToggleButton src =  (JToggleButton)(ae.getSource());
		    try{
			int duration = Integer.parseInt(durationField.getText());
			if(src.isSelected())
			    {
				timerThread = new CountdownTimer("Time up!", duration);
				timerThread.start();
			    }
			else
			    timerThread.disableMessage();
		    } catch(NumberFormatException nfe){ src.setSelected(false);}
		}
	    });
	jf.setSize(300,120);
	jf.setTitle(getName());
	jf.show();
    }
 
 public void connectionChange()
 {
 }

 public Timer()
 {
  super();
 }
    
}





