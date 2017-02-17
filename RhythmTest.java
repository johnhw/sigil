import sigil.*;
import duotonic.*;
import javax.sound.sampled.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import javax.swing.event.*;

/**
 * SignalDevice: Simple rhythm generation test
 *
 * @author John Williamson
 */
public class RhythmTest extends SProcessorModel
{

    static final long serialVersionUID = 213L;
    
    public String getGenName()
    {
	return "RhythmTest";
    }

    public String getDescription()
    {
	return "Rhythm generation device";
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

    private int target = 110;
    private long targetDuration;
    private static final int maxLoops = 16;
    private int [] loopPositions = new int[maxLoops];
    private int [] loopDurations = new int[maxLoops];
    private static byte [] clap, fast, targetSound, slow;
    private int loopIndex;
    private int tolerance = 2000;
    private int duration = 300;
    private boolean targetRhythm = true;
    private static final int bufferSize = 5000;
    private static final int maxSize = 88200;
    private static final int sampleRate = 22;
    private static final int sampleRateHz = 22050;
    private static final byte [] emptySamp = new byte[maxSize];		
    
    static
    {
	fast = cacheSample("piano3.wav");
	clap = cacheSample("handclp1.wav");
	slow = cacheSample("tambo22.wav");
	targetSound = cacheSample("click22.wav");
    }

    public void showInterface()
    {
	JFrame jf = new JFrame(getName());
	jf.setSize(600,400);

	Container gc = jf.getContentPane();
	gc.setLayout(new BorderLayout());	

	JPanel sliderPanel = new JPanel(new GridLayout(1,2));
	JSlider tolSlider = new JSlider(100,4000, tolerance);
	JSlider targetSlider = new JSlider(10,300, target);
	JCheckBox targetBox = new JCheckBox("Target rhythm on", targetRhythm);

	targetBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    targetRhythm = ((JCheckBox)(ae.getSource())).isSelected();
		}
	    });

	sliderPanel.add(UIUtils.nameSliderLabelled(tolSlider,
						   "Tolerance", false, 0, 1));

	sliderPanel.add(UIUtils.nameSliderLabelled(targetSlider, 
						   "Target rhythm", 
						   false, 0, 1));

	tolSlider.addChangeListener(new ChangeListener()
	    {
		public void stateChanged(ChangeEvent ce)
		{
		    int val = ((JSlider)(ce.getSource())).getValue();
		    tolerance = val;
		}

	    });

	targetSlider.addChangeListener(new ChangeListener()
	    {
		public void stateChanged(ChangeEvent ce)
		{
		    int val = ((JSlider)(ce.getSource())).getValue();
		    target = val;
		    updateDuration();
		}
	    });

	gc.add(sliderPanel, BorderLayout.EAST);
	gc.add(targetBox, BorderLayout.SOUTH);
	jf.show();
    }

    private void updateDuration()
    {
	targetDuration = calculateDuration(target/2);
    }

    public SourceDataLine initSound()
    {
	AudioFormat audioForm = new AudioFormat(sampleRateHz, 16, 1, true, false);
	SourceDataLine line;
	
	DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioForm); 
	if (!AudioSystem.isLineSupported(info)) 
	    {
		System.out.println("Not supported...");
		return null; 
	    }
	try 
	    {
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(audioForm, bufferSize);
	    } 
	catch (LineUnavailableException ex)  { return null; }
	return line;

    }
    
    public static byte [] cacheSample(String filename)
    {
	try{
	    //Get the actual file format
	    URL openFile = Library.getURL(filename);
	    AudioFormat audioForm = AudioSystem.getAudioFileFormat(openFile).getFormat();
	    
	    //Get the audio stream
	    AudioInputStream iStream = AudioSystem.getAudioInputStream(openFile);
	    int maxLen = 2000000;
	    byte [] wavTemp = new byte[maxLen];
	    int bytesRead, curPos = 0;
	    
	    //Read the data
	    do
		{
		    bytesRead = iStream.read(wavTemp, curPos, maxLen-curPos);
		    curPos+=bytesRead;
		} while(bytesRead>0 && curPos<maxLen);
	    
	    //Copy out the required data into a new buffer of exactly the right size
	    byte [] retVal = new byte[curPos+1];
	    System.arraycopy(wavTemp, 0, retVal, 0, curPos);
	    
	    return retVal;
	} catch(Exception E) { return null; }
    }
    
    private void setVolume(byte [] buf, int len, double vol)
    {
	for(int i=0;i<len;i+=2)
	    {
		//Get the next sample
		int sampleVal = buf[i]+(buf[i+1]<<8);
		if(sampleVal>32767)  //Correct sign
		    sampleVal -= 65536;

		int fadedVal = (int)(sampleVal*vol);

		buf[i] = (byte)(fadedVal & 255);
		buf[i+1] = (byte)((fadedVal>>8) & 255);
	    }
    }

    private void phaseLock(int variable, int target, SourceDataLine line)
    {
	int phaseLockTime = loopPositions[variable]-loopPositions[target];
	phaseLockTime = Math.abs(phaseLockTime) % loopDurations[target];
	
	if(phaseLockTime>10)
	    writeData(line, emptySamp, checkDuration(phaseLockTime), variable); 
    } 

    private void writeData(SourceDataLine line, byte [] sample, int duration, int thisIndex)
    {
	loopPositions[thisIndex] += duration;
	line.write(sample, 0, duration);	
    }

    private void playRhythm(final boolean isTarget, final boolean lilt, final int lockTo)
    {
	final int thisIndex = loopIndex;

	if(thisIndex>=maxLoops)
	    return;

	new Thread()
	{
	    public void run()
	    {
		int sampleDuration;
		SourceDataLine line = initSound();
		if(line!=null)
		    {
			byte [] bufSamp = new byte[maxSize];
			int msecDuration;
			int i = 0;
			line.start();
			while (true)
			    {
				if(getInputWidth()>0)
				    {
					msecDuration = (isTarget) ? (int)targetDuration : duration;
					sampleDuration = msecDuration * sampleRate;
					sampleDuration = checkDuration(sampleDuration);

					if(lockTo>=0 && lockTo<loopIndex) 
					    if(Math.abs(sampleDuration - loopDurations[lockTo])<tolerance)
						{
						    sampleDuration = loopDurations[lockTo];
						    phaseLock(thisIndex, lockTo, line);
						    System.arraycopy(clap, 0, bufSamp, 0, 
								     Math.min(sampleDuration, clap.length));
						}
					    else
						if(sampleDuration>loopDurations[lockTo] || !targetRhythm)
						    {
							System.arraycopy(slow, 0, bufSamp, 0, 
									 Math.min(sampleDuration, slow.length)); 
						    }
						else
						    {
							System.arraycopy(fast, 0, bufSamp, 0, 
									 Math.min(sampleDuration, fast.length)); 
						    }
					else
					    if(!(isTarget && targetRhythm))
						System.arraycopy(emptySamp, 0, bufSamp, 0, 
								 Math.min(sampleDuration, emptySamp.length)); 
					    else
						System.arraycopy(targetSound, 0, bufSamp, 0, 
								 Math.min(sampleDuration, targetSound.length)); 

					loopDurations[thisIndex] = sampleDuration;
					if(lilt)
					    {
						if(i%4!=0)
						    setVolume(bufSamp, Math.min(bufSamp.length, sampleDuration), 0.7);
						if(i%2!=0)
						    setVolume(bufSamp, Math.min(bufSamp.length, sampleDuration), 0.7);
					    }
					writeData(line, bufSamp, sampleDuration, thisIndex);
					i++;
				    }
				else
				    try{Thread.sleep(100);}catch(InterruptedException ie){}
			    }
		    }
		else
		    System.out.println("Line was null");
	    }
	}.start();
	loopIndex++;
    }
        
    private long calculateDuration(double tempo)
    {
	double dur = 1000.0/(tempo/60.0);
	return (long) dur;
    }
    
    private int checkDuration(int sampleDuration)
    {
	if(sampleDuration%2!=0)
	    sampleDuration--;
	
	if(sampleDuration<100)
	    sampleDuration = 100;
	
	if(sampleDuration>maxSize-4)
	    sampleDuration=maxSize-4;
	
	return sampleDuration;
    }

    public void loopRhythm()
    {
	updateDuration();
	playRhythm(true, true, -1);
	playRhythm(false, true, 0);
    }


    public RhythmTest()
    {          
	super();
	loopRhythm();
    }
    

    public void processSignal()
    {
	GestureSignal sig = lastSig;
	if(sig.vals.length>0)
	    duration = (int)( calculateDuration(Math.abs(sig.vals[0]/4)+0.1));
    }

}





