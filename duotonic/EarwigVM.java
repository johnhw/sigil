package duotonic;
import java.util.*;

public class EarwigVM extends Thread
{
    public static final int VM_PATCH = 0;
    public static final int VM_NOTE = 1;
    public static final int VM_DURATION = 2;
    public static final int VM_VELOCITY = 3;
    public static final int VM_TEMPO = 4;
    public static final int VM_START_CHORD = 5;
    public static final int VM_END_CHORD = 6;
    public static final int VM_REST = 7;
    public static final int VM_PAN = 8;

    private int patch = 0, note = 60, duration = 8, velocity = 127, tempo = 100, pan = 64;
    private boolean inChord = false;
    private int currentPos = 0;
    private int actualDuration, preChordDuration;
    protected Vector instructions;
    private int channel;

    private void recalculateNoteLengths()
    {
	actualDuration = (60000*duration)/(tempo*32);
    }

    public class VMInstruction
    {
	int type, value;

	public VMInstruction(int type, int value)
	{
	    this.type = type;
	    this.value = value;
	}

    }
    
    public void playInstruction(VMInstruction vmi)
    {
	
	switch(vmi.type) 
	    {
	    case VM_REST:
		MIDIUtils.safeSleep(actualDuration);
		break;
	    case VM_NOTE: 
		if(inChord)
		    MIDIUtils.playNote(channel, vmi.value, velocity, 
				       actualDuration, patch, pan);
		else
		    MIDIUtils.playNoteBlocking(channel, vmi.value, velocity, 
					       actualDuration, patch, pan);
		break;
		
	    case VM_VELOCITY: 
		velocity = vmi.value;
		break;
	    case VM_PAN:
		pan = vmi.value;
		break;
	    case VM_PATCH: 
		patch = vmi.value;
		break;
		
	    case VM_DURATION: 
		duration = vmi.value;
		recalculateNoteLengths();
		break;
		
	    case VM_TEMPO:
		tempo = vmi.value;
		recalculateNoteLengths();
		break;
		
	    case VM_START_CHORD:
		inChord = true;
		preChordDuration = actualDuration;
		break;
		
	    case VM_END_CHORD:
		inChord = false;
		MIDIUtils.safeSleep(preChordDuration);
		break;
	    }
    }

    public void playStepNoBlock()
    {
	VMInstruction vmi;
	int oldPos = currentPos;
	
	vmi = (VMInstruction)(instructions.get(currentPos++));
	while(vmi.type!=VM_NOTE && currentPos!=oldPos)
	    {
		if(currentPos>=instructions.size())
		    currentPos = 0;
		playInstruction(vmi);
		vmi = (VMInstruction)(instructions.get(currentPos++));
	    }

	if(vmi.type==VM_NOTE)
	    {
		MIDIUtils.playNote(channel, vmi.value, velocity, 
				   actualDuration, patch, pan);
	    }
    }


    public void playStep()
    {
	
	VMInstruction vmi = (VMInstruction)(instructions.get(currentPos++));
	if(currentPos>=instructions.size())
	    currentPos = 0;
	playInstruction(vmi);
    }

    public void reset()
    {
	currentPos = 0;
    }

    public void run()
    {
	currentPos = 0;
	for(int i=0;i<instructions.size();i++)
	    playStep();
    }

    public EarwigVM(int channel)
    {
	instructions = new Vector();
	this.channel = channel;
	recalculateNoteLengths();
    }

    public void addInstruction(int type, int value)
    {
	instructions.add(new VMInstruction(type, value));
    }





}
