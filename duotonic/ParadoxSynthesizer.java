package duotonic;
import java.io.*;

public class ParadoxSynthesizer
{
    static byte [] [] paradoxNoteCache;
    private static final String notePath = "paradox";
    
    static
    {
	paradoxNoteCache = new byte[12] [];
	for(int i=0;i<12;i++)
	    {
		paradoxNoteCache[i] = Speech.cacheSample(notePath+"/"+
                                                         "N"+i+".WAV");
		
		if(paradoxNoteCache[i]==null)
                    System.err.println("ParadoxSynthesizer: Note N"+i+" not found");
	
	    }
    }


    public static void playNote(int orgNote, int duration, boolean wait)
    {
	int note;
	if(orgNote>0)
	    note=orgNote%12;
	else
	    note = 11-Math.abs(orgNote)%12;

	double fadeLength = 800;
	if(note>=0)
	    {
		byte [] noteSample = paradoxNoteCache[note];
		if(noteSample!=null)
			Speech.loopCachedSample(noteSample, duration, wait);

	    }
    }

    public static void main(String args[])
    {
	int steps=48, increment=1;
	if(args.length>2)
	    {
		System.err.println("Usage: ParadoxSynthezier [nSteps] [increment]");
		System.exit(-1);
	    }
	if(args.length>0)
	    try{
		steps = Integer.parseInt(args[0]);
	    } catch(NumberFormatException nfe)
		{
		    System.err.println("Usage: ParadoxSynthezier <nSteps> <increment>");
		    System.exit(-1);
		}
	if(args.length>1)
	     try{
		 increment = Integer.parseInt(args[1]);
	     } catch(NumberFormatException nfe)
		 {
		     System.err.println("Usage: ParadoxSynthezier <nSteps> <increment>");
		     System.exit(-1);
		 }

	for(int i=0;i<steps;i++)
	    {
		playNote(i*increment, 300, true);
		try{Thread.sleep(50);} catch(InterruptedException ie){}
		playNote((i+1)*increment, 300, true);
		try{Thread.sleep(300);} catch(InterruptedException ie){}
		
	    }

    }

}
