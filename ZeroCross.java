import sigil.*;
import duotonic.MIDIUtils;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.sound.midi.*;
import java.io.*;

public class ZeroCross extends SProcessorModel
{

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "ZeroCross";
 }
 public String getDescription()
 {
  return "Plays MIDI sequences when zero crossings on the input signal occur";
 }
 public String getDate()
 {
  return "Feburary 2002";
 }
    public String getAuthor()
    {
     return "John Williamson";
    }
   
    private int [] noteNumbers;
    private int [] oldNoteNumbers;
    private int duration = 200;
    private transient java.util.Timer timeOut = new java.util.Timer();

    private double [] lastVals;
    private double [] maxVals;
    private static final int baseNote = 68;
    private int [] patchMatrix = {0,0,
				  98,98,
				  106,106,
				  107,107,
				  56,56,
				  15,15,
				  38,38};
    LinkedList [] oldNotes;
    private static final int noteHistSize = 4;
    
    public void connectionChange()
    {
      int width = getInputWidth();
      noteNumbers = new int[width];
      oldNoteNumbers = new int[width];

      lastVals = new double[width];
      maxVals = new double[width];
      oldNotes = new LinkedList[width];
        for(int i=0;i<width;i++)
        {
                noteNumbers[i] = baseNote;
                oldNotes[i] = new LinkedList();
        }
    }

    private class TimerExpire extends TimerTask
    {
	public void run()
	{
	    connectionChange();
	}
    }

    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, re-creating timer
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
	timeOut = new java.util.Timer();
	
    }


    private class ComboListener implements ItemListener
    {
	private int index;

	public void itemStateChanged(ItemEvent ie)
	{
	    JComboBox src = (JComboBox)(ie.getSource());
	    patchMatrix[index] = src.getSelectedIndex();
	}

	public ComboListener(int index)
	{
	    this.index = index;
	}

    }

    private JPanel  makePatchPanel()
    {
	int nControls = getInputWidth()*2;
	JPanel retVal = new JPanel(new GridLayout(nControls, 2));
	
	for(int i=0;i<nControls;i+=2)
	    {
		JLabel pos = new JLabel("Axis "+(i/2)+" positive");
		JComboBox posBox = MIDIUtils.getPatchSelector(patchMatrix[i]);
		JLabel neg = new JLabel("Axis "+(i/2)+" negative");
		JComboBox negBox = MIDIUtils.getPatchSelector(patchMatrix[i+1]);
		neg.setForeground(Color.black);
		pos.setForeground(Color.black);
		posBox.addItemListener(new ComboListener(i));
		negBox.addItemListener(new ComboListener(i+1));
		retVal.add(pos);
		retVal.add(posBox);
		retVal.add(neg);
		retVal.add(negBox);
	    }
	return retVal;
    }

    private class SliderListener implements ChangeListener
    {
	private String command;
	
	public void stateChanged(ChangeEvent ce)
	{
	    JSlider slider = (JSlider)(ce.getSource());
	   
	    if(command.equals("Duration"))
		{
		    duration = slider.getValue();
		}
	}
	public SliderListener(String cmd)
	{
	    command = cmd;
	}
    }

    public void showInterface()
    {
	if(getInputWidth()>0)
	    {
		JFrame jf = new JFrame();
		jf.setTitle(getName());
		jf.setSize(400,300);
		JPanel patchPanel = makePatchPanel();
		jf.getContentPane().add(patchPanel, BorderLayout.NORTH);

		Box sliderBox = Box.createVerticalBox();
		JSlider durationSlider = new JSlider(50,1000,duration);
		JPanel durPanel = UIUtils.nameSliderLabelled(durationSlider, "Duration", true);
		durationSlider.addChangeListener(new SliderListener("Duration"));
		sliderBox.add(durationSlider);
		jf.getContentPane().add(sliderBox, BorderLayout.CENTER);
		UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
		jf.show();
	    }
    }

    public ZeroCross()
    {
	super();
	setTerminating(true);	
    }

    public void deleted()
    {
    }
    


    private void zeroCrossing(int axis, int direction)
    {
        timeOut.cancel();
        timeOut = new java.util.Timer();
        timeOut.schedule(new TimerExpire(), 500);
	int chan=0, vel=0, patch=0;
        chan = axis+1;
        vel = (int)(maxVals[axis]*3);
        patch = patchMatrix[axis*2+direction];
	
        if(vel>10 && vel<30)
            vel = 30;
        else if(vel<10)
	    vel = 0;

	if(vel>127)
	    vel = 127;

        int noteChange = vel/12;

       if(vel!=0)
       {
           if(direction==0)
                {
                 noteNumbers[axis]+=noteChange;
                 playSeq(chan, noteNumbers[axis], 2, 
			 noteChange, vel, duration, patch, axis);
                }

	else
	    {
		noteNumbers[axis]-=noteChange;
		playSeq(chan, noteNumbers[axis], 2, 
			-noteChange, vel, duration, patch, axis);
		
	    }
       }
    }

   
    private void pushNote(int axis, int note)
    {
      if(oldNotes[axis].size()==noteHistSize)
        oldNotes[axis].removeLast();

      oldNotes[axis].addFirst(new Integer(note));
    }


    private void playHistory(final int chan, final LinkedList history,
                         final int vel, final int dur, final int patch)
    {
	new Thread()
	    {
		public void run()
		{
                    int nNotes = history.size();

		    for(int i=0;i<nNotes;i++)
			{
                            int curNote = ((Number)(history.get(i))).intValue();
                            MIDIUtils.playNote(chan, curNote, vel, dur, patch);
                            try{Thread.sleep(dur);}catch(InterruptedException ie) {}
			}
		}
	    }.start();
    }
    
    private void playSeq(final int chan, final int start, final int nNotes, 
                         final int change, final int vel, final int dur, 
			 final int patch, final int axis)
    {
	new Thread()
	    {
		public void run()
		{
		    int curNote = start;
		    for(int i=0;i<nNotes;i++)
			{
                            MIDIUtils.playNote(chan, curNote, vel, dur, patch);
                            try{Thread.sleep(dur+10);}catch(InterruptedException ie) {}

			    curNote+=change;
			}
		}
	    }.start();
    }

    private void calcZeroCrossings(double [] vals)
    {
        int width = getInputWidth();
        for(int i=0;i<getInputWidth();i++)
        {
         if(lastVals[i]<0 && vals[i]>=0)
         {
          zeroCrossing(i, 0);
          maxVals[i]=0.0;
         }
         else if (lastVals[i]>=0 && vals[i]<0)
         {
          zeroCrossing(i, 1);
          maxVals[i]=0.0;
         }

         if(Math.abs(vals[i])>maxVals[i])
                maxVals[i]=Math.abs(vals[i]);
         lastVals[i] = vals[i];
        }
	
    }


 public void processSignal()
 {
   GestureSignal sig = lastSig;      
   if(getInputWidth()>0)
   {
       active = true;
       calcZeroCrossings(sig.vals);
   }
 }

}


