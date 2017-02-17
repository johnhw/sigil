package duotonic;
import sigil.Library;
import java.io.*;
import java.util.*;

public class EarwigParser implements Serializable
{

    static final long serialVersionUID = 213L;
    
    public static final int TYPE_NOTE = 0;
    public static final int TYPE_NOTE_CHANGE = 1;
    public static final int TYPE_DURATION = 2;
    public static final int TYPE_PATCH = 3;
    public static final int TYPE_TEMPO = 4;
    public static final int TYPE_VELOCITY = 5;
    public static final int TYPE_CHORD = 7;
    public static final int TYPE_OCTAVE = 8;
    public static final int TYPE_SEQUENCE = 9;
    public static final int TYPE_NUMBER = 10;
    public static final int TYPE_REST = 11;
    public static final int TYPE_DELTA_UP = 12;
    public  static final int TYPE_DELTA_DOWN = 13;
    public static final int TYPE_VARIABLE = 14;
    public static final int TYPE_ASSIGNMENT = 15;
    public static final int TYPE_PAN = 16;
    public static final int TYPE_PUSH = 17;
    public static final int TYPE_POP = 18;
    public static final int TYPE_MULTI_EXPR = 24;
    public static final int TYPE_ADD = 20;
    public static final int TYPE_SUB = 21;
    public static final int TYPE_MULT = 22;
    public static final int TYPE_DIV = 23;
   
    int [] noteNumbers = {-3, -1, 0, 2, 4, 5, 7};
    private Vector sequences;
    private static Hashtable macros = new Hashtable();
    
    private static class MacroData
    {
	String expansion;
	Vector parameterNames;

	public MacroData(String expansion, Vector parameterNames)
	{
	    this.expansion = expansion;
	    this.parameterNames = parameterNames;
	}
    }

    public static class EarwigSyntaxException extends Exception
    {
	public EarwigSyntaxException(String message)
	{
	    super(message);
	}
    }
    
    public static void readLibrary(String libName)
    {
	try{
	    BufferedReader reader = Library.getReader(libName);
	    String inLine = reader.readLine();
	    while(inLine!=null)
		{
		    defineMacros(inLine);
		    inLine = reader.readLine();
		}
	} catch(Exception E) {
	    System.err.println("Earwig: library loading error: "+E.getMessage());
	}


    }

    static
    {
	String stdLib = "stdlib.ewg";
	readLibrary(stdLib);
    }

    private boolean inTable(char c, char [] table)
    {
	for(int i=0;i<table.length;i++)
	    if(c==table[i])
		return true;
	return false;
    }

    private int getIndex(char c, char [] table)
    {
	for(int i=0;i<table.length;i++)
	    if(c==table[i])
		return i;
	return -1;
    }


    public static String expandMacro(String name, Vector parms, int reps) throws EarwigSyntaxException
    {
	String retVal= "";
	MacroData macro = getMacroExpansion(name);
	if(macro==null)
	    return retVal;
	String expansion = macro.expansion;
	Vector subsParms = macro.parameterNames;
	StringTokenizer colonTok = new StringTokenizer(expansion, ":", true);
	String substituted = "";
	boolean nextParm = false;
	while(colonTok.hasMoreTokens())
	    {
		String nextToken = colonTok.nextToken();
		if(nextToken.charAt(0)==':')
		    {
			nextParm = true;
			continue;
		    }
		int spaceIndex = nextToken.indexOf(" ");
		if(spaceIndex<0)
		    spaceIndex = nextToken.length();

		if(spaceIndex>0 && nextParm)
		{
				String nextPar = nextToken.substring(0, spaceIndex);
				if(spaceIndex<nextToken.length())
				    nextToken = nextToken.substring(spaceIndex);
				else
				    nextToken = "";
				int subsIndex = subsParms.indexOf(nextPar);
				if(subsIndex>=0 && subsIndex<parms.size())
				    substituted+=(String)(parms.get(subsIndex));
		}
		substituted+=nextToken;
		nextParm = false;
	    }
	for(int i=0;i<reps;i++)
		retVal += substituted+" ";	  
	return retVal;
    }

    public static void defineMacro(String name, MacroData expansion)
    {
	macros.put(name, expansion);
    }

    public static MacroData getMacroExpansion(String name)
    {
	MacroData retVal = (MacroData)(macros.get(name));
	return retVal;

    }

    private static Vector parseParameters(String toParse) throws EarwigSyntaxException
    {
	StringTokenizer tok = new StringTokenizer(toParse);
	Vector retVal = new Vector();
	while(tok.hasMoreTokens())
	    {
		String nextToken = tok.nextToken();
		if(nextToken.charAt(0)!=':' || nextToken.length()<2)
		    throw new EarwigSyntaxException("Bad macro parameter "+nextToken);
		else
		    retVal.add(nextToken.substring(1));
	    }
	return retVal;
    } 

    private static Vector parseExpandParameters(String toParse) throws EarwigSyntaxException
    {
	StringTokenizer tok = new StringTokenizer(toParse,"|",false);
	Vector retVal = new Vector();
	while(tok.hasMoreTokens())
	    {
		String nextToken = tok.nextToken();
		retVal.add(nextToken);
	    }
	return retVal;
    } 

    public static String fullyExpand(String toParse) throws EarwigSyntaxException
    {
	StringBuffer expandBuffer = new StringBuffer(toParse);
	String currentName="", currentRepeat="", currentParameters = "";
	Vector parms = new Vector();
	boolean searching = true, naming = false, moreExpansions = true, repeat = false,
	    inParameter = false;
	while(moreExpansions)
	    {
		StringBuffer tempBuffer = new StringBuffer();
		moreExpansions = false;
		for(int i=0;i<expandBuffer.length();i++)
		    {
			char testChar = expandBuffer.charAt(i);
			

			if(searching && testChar!='!')
			    tempBuffer.append(testChar);
			    else if(searching && testChar=='!')
			    {
				currentName = "";
				naming = true;
				searching = false;
				moreExpansions = true;
				continue;
			    }			
			if(naming && testChar!='[' && testChar!='{' 
			   && Character.isLetterOrDigit(testChar))
			    currentName+=testChar;
			else if(naming && testChar!='[' && testChar!='{')
			    {
				naming = false;
				searching = true;
				tempBuffer.append(expandMacro(currentName,parms, 1));
				tempBuffer.append(testChar);
				
				continue;
			    }
			else if(naming && testChar=='[')
			    {
				currentRepeat="";
				repeat = true;
				naming = false;
				
				continue;
			    }
			else if(naming && testChar=='{')
			    {
				currentParameters = "";
				naming = false;
				inParameter = true;
				
				continue;
			    }

			if(inParameter && testChar!='}')
			    currentParameters+=testChar;
			else if(inParameter && testChar=='}')
			    {
				parms = parseExpandParameters(currentParameters);
				inParameter = false;
				naming = true;
				
				continue;
			    }

			if(repeat && testChar!=']')
			    currentRepeat+=testChar;
			else if(repeat && testChar==']')
			    { 
				repeat = false;
				searching = true;
				
				int rep;
				try{
				    rep=Integer.parseInt(currentRepeat);
				} 
				catch(NumberFormatException nfe) { rep = 0; }
				tempBuffer.append(expandMacro(currentName, parms, rep));
			    }
		    }
		if(!searching)
		    throw new EarwigSyntaxException("Bad macro expansion");
		expandBuffer = new StringBuffer(tempBuffer.toString());
	    }
	return expandBuffer.toString();

    }

    public static String defineMacros(String toParse) throws EarwigSyntaxException
    {

	boolean searching = true, naming = false, expanding = false, inParameter = false;
	String currentName="", currentExpansion="", currentParameters = "";
	String retVal = "";
	Vector parms = new Vector();
	for(int i=0;i<toParse.length();i++)
	    {
		char testChar = toParse.charAt(i);
		
		if(searching && testChar!='@')
		    retVal+=testChar;
		else if(searching && testChar=='@')
		    {
			currentName = "";
			naming = true;
			searching = false;

		
			continue;
		    }

		if(naming && testChar!='[' && testChar!='{')
		    currentName+=testChar;
		else if(naming && testChar=='[')
		    {
			currentExpansion="";
			naming = false;
			expanding = true;
		      
		
			continue;
		    }
		else if(naming && testChar=='{')
		    {
			currentParameters = "";
			naming = false;
			inParameter = true;
		
			continue;
		    }

		if(inParameter && testChar!='}')
		    currentParameters+=testChar;
		else if(inParameter && testChar=='}')
		    {
			parms = parseParameters(currentParameters);
			inParameter = false;
			naming = true;

			continue;
		    }
		    
		if(expanding && testChar!=']')
		    currentExpansion+=testChar;
		else if(expanding && testChar==']')
		    {

			expanding = false;
			searching = true;
			MacroData newMacro = new MacroData(currentExpansion, parms);
			defineMacro(currentName, newMacro);
		    }

	    }
	if(!searching)
	    throw new EarwigSyntaxException("Bad macro definition");
	return retVal;
    }


    public class EarwigElement implements Serializable
    {
	static final long serialVersionUID = 213L;
	public boolean delta = false;
	public int val;
	public int type;
	public Vector vec;
	public String str;
	public EarwigElement elt;

	
	public String toString()
	{
	    String retVal="EE["+type+":";

	    if(vec!=null)
		retVal+=vec.toString();
	    else if(str!=null)
		retVal+=str;
	    else if(elt!=null)
		retVal+=elt;
	    else
		retVal+=val;
	    retVal+="]";
	    return retVal;

	}


	public EarwigElement(int type, int val)
	{
	    this.val = val;
	    this.type = type;
	    this.delta = false;
	}

	public EarwigElement(int type, Vector vec)
	{
	    this.type = type;
	    this.vec = vec;
	}


    	public EarwigElement(int type, String str)
	{
	    this.type = type;
	    this.str = str;
	}

	public EarwigElement(int type, String str, EarwigElement elt)
	{
	    this.type = type;
	    this.str = str;
	    this.elt = elt;
	}

	public EarwigElement(int type, String str, Vector vec)
	{
	    this.type = type;
	    this.str = str;
	    this.vec = vec;
	}

	public EarwigElement(int type, EarwigElement elt)
	{
	    this.type = type;
	    this.elt = elt;
	}

    }

    

    private Vector parse(String toParse) throws EarwigSyntaxException
    {
	Vector retVal = new Vector();
	toParse+=" ";
	toParse = defineMacros(toParse);
	toParse = fullyExpand(toParse);
	StringTokenizer tok = new StringTokenizer(toParse, ";", false);
	while(tok.hasMoreTokens())
	    retVal.add(parseSeq(tok.nextToken()));
	return retVal;
    }


    private EarwigElement parseSeq(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0)
	    {
		return parseSeqElts(toParse);
	    }
	else
	    throw new EarwigSyntaxException("Malformed sequence");
    }


    private EarwigElement parseChord(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0 && toParse.charAt(toParse.length()-1)==')')
	    {
		toParse = toParse.substring(1, toParse.length()-1);
		Vector chordVector = new Vector();
		StringTokenizer tok = new StringTokenizer(toParse);
		while(tok.hasMoreTokens())
		    chordVector.add(parseSeqElt(tok.nextToken()));
		EarwigElement newElt = new EarwigElement(TYPE_CHORD, chordVector);
		return newElt;
	    }
	else
	    throw new EarwigSyntaxException("Malformed chord: "+toParse);
    }

    private int countBrackets(String toCount, char left, char right)
    {
	int bracketCount = 0;
	for(int i=0;i<toCount.length();i++)
	    {
		if(toCount.charAt(i)==left)
		    bracketCount++;
		else if(toCount.charAt(i)==right)
		    bracketCount--;
	    }
	return bracketCount;
    }		


    private Vector tokenizeProtectBrackets(String toParse) throws EarwigSyntaxException
    {
	StringTokenizer tok = new StringTokenizer(toParse);
	Vector tokVector = new Vector();
	while(tok.hasMoreTokens())
	{
	    String nextToken = tok.nextToken();
	    if(nextToken.indexOf("(")>=0)
		{
		    int bracketCount = countBrackets(nextToken, '(', ')');
		    String nextBracketToken;
		    while(tok.hasMoreTokens() && bracketCount!=0)
			{
			    nextBracketToken = tok.nextToken();
			    nextToken += " "+nextBracketToken;
			    bracketCount += countBrackets(nextBracketToken, '(', ')');
			}
		    if(bracketCount!=0)
			throw new EarwigSyntaxException("Mismatched brackets "+toParse);
		}
	    tokVector.add(nextToken);
	}
	return tokVector;
    }

    private EarwigElement parseSeqElts(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0)
	    {
		Vector seqVector = new Vector();
		Vector tokenVector = tokenizeProtectBrackets(toParse);
		for(int i=0;i<tokenVector.size();i++)
		    {
			String nextToken = (String)(tokenVector.get(i));
			if(nextToken.indexOf("(")==0)
			    seqVector.add(parseChord(nextToken));
			else
			    seqVector.add(parseSeqElt(nextToken));
		    }
	    
		EarwigElement newElt = new EarwigElement(TYPE_SEQUENCE, seqVector);
		return newElt;
	    }
	else
	    throw new EarwigSyntaxException("Malformed sequence");
    }



    private EarwigElement parseSeqElt(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0)
	    {
		char testChar = toParse.charAt(0);
		String remainder = toParse.substring(1);
		EarwigElement retVal=null;
		switch(testChar)
		    {
		    case 'p': case 'P':
			retVal = parsePatch(remainder);
			break;
		    case 'l': case 'L':

			retVal = new EarwigElement(TYPE_DURATION, parseDExpr(remainder));
			break;
		    case 'o': case 'O':
			retVal = new EarwigElement(TYPE_OCTAVE, parseDExpr(remainder));
			break;
		    case 's': case 'S':
			retVal = new EarwigElement(TYPE_PAN, parseDExpr(remainder));
			break;
		    case '/':
			retVal = new EarwigElement(TYPE_PUSH, parseDExpr(remainder));
			break;
		    case 'n': case 'N':
			retVal = new EarwigElement(TYPE_NOTE, parseDExpr(remainder));
			break;
		    case 't': case 'T':
			retVal = new EarwigElement(TYPE_TEMPO, parseDExpr(remainder));
			break;
		    case '\'':
			retVal = parseAssignment(remainder);
			break;
		    case 'v': case 'V':
			retVal = new EarwigElement(TYPE_VELOCITY, parseDExpr(remainder));
			break;
		    case '&': 
			retVal = new EarwigElement(TYPE_REST, 0);
			break;
		    case '>':
		    case '<':
			retVal = new EarwigElement(TYPE_NOTE_CHANGE, parseDExpr(""+testChar+
										remainder));
		    }

		if((testChar>='a' && testChar<='g') || 
		   (testChar>='A' && testChar<='G'))
		    retVal = parseNote(""+testChar+remainder);
		
		if(retVal==null)
		    throw new EarwigSyntaxException("Unknown sequence element: "+testChar);
		return retVal;
	    }
	else
	    throw new EarwigSyntaxException("Missing sequence element");
    }


    private EarwigElement parseAssignment(String toParse) throws EarwigSyntaxException
    {
	int lastEquals = toParse.indexOf("=");
	if(lastEquals>-1 && lastEquals<toParse.length()-1)
	    {
		String varName = toParse.substring(0,lastEquals);
		String remainder = toParse.substring(lastEquals+1);
		return new EarwigElement(TYPE_ASSIGNMENT, varName, parseDExpr(remainder));

	    }
		else
		    throw new EarwigSyntaxException("Missing assignment "+toParse);

    }


    private EarwigElement parsePatch(String toParse)throws EarwigSyntaxException
    {
	if(toParse.length()>0)
	    {
		char testChar = toParse.charAt(0);
		if(testChar=='(')
		    {
			if(toParse.length()>2 && toParse.charAt(toParse.length()-1)==')')
			    {
				String patchName = toParse.substring(1, toParse.length()-1);
				int patchNum = MIDIUtils.getMatchingPatch(patchName);
				return new EarwigElement(TYPE_PATCH, 
							 new EarwigElement(TYPE_NUMBER, patchNum));
			    }
			else
			    throw new EarwigSyntaxException("Malformed patch name");
		    }
		else
		    return new EarwigElement(TYPE_PATCH, parseDExpr(toParse));
		
	    }
	else
	    throw new EarwigSyntaxException("Missing patch parameter");
    }

    private EarwigElement parseNote(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0 && toParse.length()<3)
	    {
		char noteChar = toParse.charAt(0);
		int note;
		if(noteChar>='a' && noteChar<='g')
		    note = noteNumbers[noteChar-'a'];
		else if(noteChar>='A' && noteChar<='G')
		    note = noteNumbers[(noteChar-'A')]+12;
		else
		    throw new EarwigSyntaxException("Unknown note data "+toParse);
		if(toParse.length()>1)
		    {
			char testChar = toParse.charAt(1);
			if(testChar=='$')
			    note--;
			else if(testChar=='#')
			    note++;
		    }
		return new EarwigElement(TYPE_NOTE, note);
	    }

	else
	    throw new EarwigSyntaxException("Unknown note data "+toParse);
    }


    private EarwigElement parseMultiExpr(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0)
	    {
		Vector tokens = tokenizeProtectBrackets(toParse);
		Vector multiExpr = new Vector();
		for(int i=0;i<tokens.size();i++)
		    {
			String nextTok = (String)(tokens.get(i));
			if(nextTok.length()==1)
			    {
				char testChar = nextTok.charAt(0);
				switch(testChar)
				    {
				    case '+': multiExpr.add(new EarwigElement(TYPE_ADD, 0)); break;
				    case '-': multiExpr.add(new EarwigElement(TYPE_SUB, 0)); break;
				    case '/': multiExpr.add(new EarwigElement(TYPE_DIV, 0)); break;
				    case '*': multiExpr.add(new EarwigElement(TYPE_MULT, 0)); break;
				    default:  multiExpr.add(parseExpr(nextTok)); 
				    }
			    }
			else
			    multiExpr.add(parseExpr(nextTok));
		    }
		return new EarwigElement(TYPE_MULTI_EXPR, multiExpr);
	    }
	else
	    throw new EarwigSyntaxException("Missing expression "+toParse);
    }

    private EarwigElement parseDExpr(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0)
	    {

		char testChar = toParse.charAt(0);
		if(toParse.length()==1)
		    {
			if(testChar=='<')
			    return new EarwigElement(TYPE_DELTA_DOWN, 
						     new EarwigElement(TYPE_NUMBER, 1));
			else if(testChar=='>')
			    return new EarwigElement(TYPE_DELTA_UP, 
						     new EarwigElement(TYPE_NUMBER, 1));
		    }
		if(testChar=='<')
		    return new EarwigElement(TYPE_DELTA_DOWN, parseExpr(toParse.substring(1)));
		else if(testChar=='>')
		    return new EarwigElement(TYPE_DELTA_UP, parseExpr(toParse.substring(1)));
		else
		    return parseExpr(toParse);
	    }
	
	else
	    throw new EarwigSyntaxException("Missing expression "+toParse);
    }

    private EarwigElement parseExpr(String toParse) throws EarwigSyntaxException
    {
	if(toParse.length()>0)
	    {
	       
		char testChar = toParse.charAt(0);
		if(testChar=='(' && toParse.charAt(toParse.length()-1)==')')
		    return parseMultiExpr(toParse.substring(1,toParse.length()-1));

		if(testChar=='.')
		    return new EarwigElement(TYPE_POP,0);
			
		if(!Character.isDigit(testChar) && testChar!='-')
		    return new EarwigElement(TYPE_VARIABLE, toParse);
		else 
		    {
			try{
			    int intVal = Integer.parseInt(toParse);
			    return new EarwigElement(TYPE_NUMBER, intVal);
			} catch(NumberFormatException nfe)
			    {
				throw new EarwigSyntaxException("Bad number format: "+toParse);
			    }

		    }
	    }
	else
	    return null;
    }

    public EarwigParser(String toParse) throws EarwigSyntaxException
    {
	sequences = parse(toParse);
    }

    public Vector getSequences()
    {
	return sequences;
    }

    
 

}
