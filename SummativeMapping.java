import duotonic.*;
import sigil.ParameterizedGesture;
import java.io.*;
import javax.swing.JOptionPane;

public abstract class SummativeMapping implements Serializable
{
    protected String str;
    static final long serialVersionUID = 213L;
    public abstract void perform();
    public SummativeMapping(String str)
    {
	this.str = str;
    }

    public String getString()
    {
	return str;
    }
  
}

class SpeechSummativeMapping extends SummativeMapping
{
    static final long serialVersionUID = 213L;
    private Speech speaker;
    public void perform()
    {
	speaker.textToSpeech(str, false);
    }

    public Speech getSpeaker()
    {
	return speaker;
    }

    public void setSpeaker(Speech speaker)
    {
	this.speaker = speaker;
    }

    public SpeechSummativeMapping(String str)
    {
	super(str);
	speaker = new Speech();
    }

    public String toString()
    {
	return "Speech "+str;
    }
}

class MIDISummativeMapping extends SummativeMapping
{
    private EarwigParser parsed;
    static final long serialVersionUID = 213L;
    private int getScaledMagnitude(double mag)
    {
	int retVal = (int)(mag/100);
	if(mag>5)
	    mag = 5;
	return retVal;
    }

    private int getScaledProbability(double prob)
    {
	int retVal = (int)(prob*10);
	return retVal;
    }

    private int getScaledDuration(double dur)
    {
	int retVal = (int)dur;
	return retVal;
    }

    public void perform(ParameterizedGesture pGest)
    {
	if(parsed!=null)
	    {
		EarwigCompiler compiled = new EarwigCompiler();
		compiled.setBinding("mag", getScaledMagnitude(pGest.getAverageScale()));
		compiled.setBinding("prob", getScaledProbability(pGest.getProbability()));
		compiled.setBinding("time", getScaledDuration(pGest.getDuration()));
		compiled.compile(parsed);
		compiled.play();
	    }
    }

    public void perform()
    {
	if(parsed!=null)
	    {
		EarwigCompiler compiled = new EarwigCompiler();
		compiled.compile(parsed);
		compiled.play();
	    }
    }
    
    public MIDISummativeMapping(String str)
    {
	super(str);
	try{
	parsed = new EarwigParser(str);
	}  catch(EarwigParser.EarwigSyntaxException ese)
	    {
		JOptionPane.showMessageDialog(null, ese.getMessage(),
					      "Parse error", JOptionPane.ERROR_MESSAGE);
	    }
    }


    public String toString()
    {
	return "MIDI";
    }
}

class SampleSummativeMapping extends SummativeMapping
{
    private byte [] sampleData;
    private String name;
    static final long serialVersionUID = 213L;
    public String toString()
    {
	int slashIndex = str.lastIndexOf(File.separator);
	String filename;
	if(slashIndex>-1)
	    filename = str.substring(slashIndex+1);
	else
	    filename = str;
	return "Sample "+filename;
    }
    public void perform()
    {
	Speech.playSample(str, false);
    }

    public SampleSummativeMapping(String str)
    {
	super(str);	
    }


}

