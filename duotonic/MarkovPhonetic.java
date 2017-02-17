package duotonic;
import sigil.Library;
import java.io.*;
import java.util.*;


public class MarkovPhonetic
{
   private static int phoneSize = 3;
   private Hashtable transitions;
   private static final Hashtable phonemeTranslation;
   private Random rnd = new Random();
    private String blanks;
    private int length;


   private static String [] phonemeFromTable =
   {
      "[", "]", ":", ">", "<", "{", "}",
      "$", "%", "*", "i", "I", "e", 
      "&", "A", "0", "O", "U", "u", "V",
      "3", "@", "R", "N", "T", "D", "S",
      "Z", "p", "t", "k", "b", "d", "g",
      "m", "n", "f", "v", "s", "z", "r",
      "l", "w", "h", "j", "_", "-", "a", "o"
   };


   private static String [] phonemeToTable =
   {
     "ch", "jh", "ey", "ow", "ay", "aw", "oy",
     "iy", "ay", "uw1", "iy", "ih", "eh",
     "ae", "ae", "ao", "ao", "uh", "uw2", "ax",
     "ih", "ae", "xr", "ng", "th", "dh1", "sh", "zh",
     "pp", "tt1", "kk1", "bb1", "dd1", "gg1",
     "mm", "nn", "ff", "vv", "ss", "zz", "rr1",
     "ll", "ww", "hh", "jh", "pa4", "pa2", "ae","ao"
   };


   static
   {
    phonemeTranslation = new Hashtable();
    for(int i=0;i<phonemeFromTable.length;i++)
     phonemeTranslation.put(phonemeFromTable[i], phonemeToTable[i]);
   }

   public static String translatePhoneme(String phone)
   {
    String retVal = (String)(phonemeTranslation.get(phone));
    if(retVal==null)
      retVal="?"+phone+"?";
    return retVal;
   }
    
    private static void analyzeSequence(String phonetic, Accumulator phoneTable)
    {
	if(phonetic.length()<phoneSize)
	    return;
	String phone;
	phonetic = phonetic.toLowerCase();
	
	for(int i=1;i<phoneSize;i++)
	    {
		phone="";
		for(int j=0;j<i;j++)
		    phone+="_";
		for(int j=i;j<phoneSize;j++)
		    {
			phone +=phonetic.charAt(j-i);
		    }
		
		phoneTable.add(phone);
	    }
	int i;

	for(i=phoneSize-1;i<phonetic.length();i++)
	    {
		phone = phonetic.substring((i-phoneSize)+1, i+1);
	
		phoneTable.add(phone);
	    }
	int len = phonetic.length();
	phone = phonetic.substring(len-phoneSize+1)+"_";
	phoneTable.add(phone);

    }
    
    
    private static Hashtable constructTransitionProbabilities(Vector sortedList)
    {
        Hashtable prephoneMappings = new Hashtable();
	
	for(int i=0;i<sortedList.size();i++)
	    {
		AccEntry ae = (AccEntry)(sortedList.get(i));
		String phonephone = (String)(ae.val);
		
                String prephone = phonephone.substring(0,phoneSize-1);
		String monophone = phonephone.substring(phoneSize-1,phoneSize);
		
                Vector preVec;
                if(prephoneMappings.containsKey(prephone))
                    preVec = (Vector)(prephoneMappings.get(prephone));
		else
                    preVec = new Vector();
                preVec.add(new AccEntry(ae.freq, monophone));
                prephoneMappings.put(prephone, preVec);
	    }

        Enumeration preEnum = prephoneMappings.keys();
        while(preEnum.hasMoreElements())
	    {
                String prephone = (String)(preEnum.nextElement());
                Vector aeVec = (Vector)(prephoneMappings.get(prephone));

		//Normalize to 0..1
		double totalOccurence = 0;
		for(int i=0;i<aeVec.size();i++)
		    {
			AccEntry ae = (AccEntry)(aeVec.get(i));
			totalOccurence+=ae.freq;
		    }
		for(int i=0;i<aeVec.size();i++)
		    {
			AccEntry ae = (AccEntry)(aeVec.get(i));
			ae.freq/=totalOccurence;
		    }
	    }
        return prephoneMappings;
    }

    public String markovSimulate(String currentState)
    {
	Vector avec = (Vector)(transitions.get(currentState));
	if(avec==null)
	    {
		System.err.println("Null with "+currentState);
                return "?";
	    }
	double newRand = rnd.nextDouble();
	double currentProb = 0;
	String newChar=null;
	for(int j=0;j<avec.size();j++)
	    {
		AccEntry ae = (AccEntry)(avec.get(j));
		currentProb+=ae.freq;
		if(newRand<currentProb)
		    {
			newChar=(String)(ae.val);
			break;
		    }
	    }
	if(newChar==null)
	    {
		System.err.println("Probability calculation error!");
                return "?";
	    }
	
	return newChar;
    }

    public String getInitialState()
    {
	return blanks;
    }
    

    public int getLength()
    {
	return length;
    }

    private void markovTest()
    {

	String currentState = blanks;
        String phoneString="";

        for(int i=0;i<50;i++)
	    {

                String newChar = markovSimulate(currentState);
                phoneString+=(translatePhoneme(newChar)+"/");

                if(!newChar.equals("?"))
                {
                 if(!newChar.equals("_"))
                     currentState = currentState.substring(1,length-1) + newChar;
                 else
                         currentState = blanks; //Set word boundaries properly
                }

	    }
       Speech speaker = new Speech();
       speaker.say(phoneString);

    }

    public MarkovPhonetic(String inName)
    {
     transitions = deserializeTable(inName);
     blanks="";
     for(int i=0;i<length-1;i++)
	 blanks+="_";
    }

    private static void processDictionary(String filename, Accumulator phoneTable)
    {
	try{
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String inLine = reader.readLine();   
	    while(inLine!=null)
		{
		  
		    analyzeSequence(inLine, phoneTable);
		    inLine = reader.readLine();
		}
	    reader.close();
	} catch(IOException ioe) { ioe.printStackTrace(); }
    }

    private Hashtable deserializeTable(String inName)
    {
     try{
      ObjectInputStream objIn = new ObjectInputStream(
						      new BufferedInputStream(Library.getInputStream(inName)));
       length = objIn.readInt();
       Hashtable retVal = (Hashtable)(objIn.readObject());
       objIn.close();
       return retVal;
      }
      catch(IOException ioe) { ioe.printStackTrace(); return null; }
      catch(ClassNotFoundException cnfe) { cnfe.printStackTrace(); return null; }
    }

    private static void serializeTable(Hashtable toSer, String outName)
    {
     try{
      ObjectOutputStream objOut = new ObjectOutputStream(
                                  new BufferedOutputStream(new FileOutputStream(
                                  outName)));
       objOut.writeInt(phoneSize);
       objOut.writeObject(toSer);
       objOut.flush();
       objOut.close();
      } catch(IOException ioe) { ioe.printStackTrace(); }
    }

    public static void createMarkovModel(String filename, int phoneSz, String outName)
    {
        phoneSize = phoneSz;
	Accumulator phoneTable = new Accumulator();
	processDictionary(filename, phoneTable);
	Vector sortedList = phoneTable.getSortedList();
	Hashtable transitions = constructTransitionProbabilities(sortedList);
        serializeTable(transitions, outName);
 }


 public static void main(String args[])
 {
     try{
     if(args.length==3)
             createMarkovModel(args[0], Integer.parseInt(args[1]), args[2]);
     else if(args.length==1)
                {
                    MarkovPhonetic mp = new MarkovPhonetic(args[0]);
                    mp.markovTest();
                }
     else
      System.err.println("Usage: MarkovPhonetic [<compfile>] | [<dictfile> <length> <outfile>]");

     } catch(Exception E)
     {
      System.err.println("Usage: MarkovPhonetic [compfile] |[<dictfile> <length> <outfile>]");
     }
 }


}





