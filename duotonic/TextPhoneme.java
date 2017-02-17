package duotonic;
import sigil.Library;
import java.util.*;
import java.io.*;

public class TextPhoneme
{

 private static final String [] vowels = {"a", "e", "i", "o", "u", "y"};
 private static final String [] startVowels = {"e", "i", "y"};
 private static final String [] voicedConsonants = {"b", "d", "v", "g", "j", "l", "m", "n", "r", "w", "z"};
 private static final String [] endings = {"er", "e", "es", "ed", "ing", "ely"};
 private static final String [] consonants = {"b", "c", "d", "f", "g", "h", "k", "l", "m", "n", "p", 
					      "q", "r", "s", "t", "v", "x", "z"};

 private static String [] startEndTable = {
     "t", "tt2", "b", "bb1", "d", "dd2", "k", "kk1",
     "y", "yy2", "g", "gg2", "dh", "dh2", "s", "ss", "r", "rr1"};
    
 private static String phonemeTable [] = {
     "b2", "bb2", "ax", "ao", "k1", "kk1",
     "yy", "yy1", "ah", "ae2", "p", "pp", "t", "tt1",
     "r1", "rr2", "k", "kk2", "b", "bb2",
     "d", "dd1", "g", "gg3", "f", "ff",
     "d1", "dd1", "v", "vv", "er", "yr",
     "yy", "yy1", "th", "th",   "uw", "uw1",
     "dh", "dh2", "s","ss", "z", "zz",
     "h", "hh1", "m", "mm", "n", "nn1",
     "l", "ll", "w", "ww", "y", "yy1",
     "r", "rr2", "j", "jh", "p1", "pa1", "t1", "tt1",
     "p2", "pa1", "p3", "pa1",   "p4", "pa4",
     "p5", "pa5", "g3", "gg3", "n2", "nn2", "k2",
     "kk2", "g2", "gg2", "dt", "dd2", "xx","pa2", "u2", "uw2",
     "ard1", "ar/dd1", "b2", "bb2", "eh", "eh2", "ih", "ih2"
 };


    private static class RuleEntry
    {
        String before, after, result;
        public RuleEntry(String before,  String after, String result)
	{
	    this.before = before;
	    this.after = after;
	    this.result = result;
	}
    }
    
    private static Hashtable ruleBase;
    
    private static void loadRules()
    {
	try{  
	    BufferedReader reader = Library.getReader("english.phn");
	    String inLine;
	    do
		{
		    inLine = reader.readLine();
		    if(inLine!=null)
			{
			    StringTokenizer commaTok = new StringTokenizer(inLine, "|", false);
                            String before = commaTok.nextToken().toLowerCase();
                            String match = commaTok.nextToken().toLowerCase();
                            String after = commaTok.nextToken().toLowerCase();
                            String result = commaTok.nextToken().toLowerCase();
                            RuleEntry re = new RuleEntry(before,
                                                         after,
                                                         result);
                            Vector ruleVec;
                            if(ruleBase.containsKey(match))
                              ruleVec = (Vector)(ruleBase.get(match));
                             else
                               ruleVec = new Vector();
                            ruleVec.add(re);
                            ruleBase.put(match, ruleVec);
			}
		} while(inLine!=null);
	    	    
	} catch(IOException ie) { ie.printStackTrace(); }
    }
    
    
    private static boolean inTable(char c, String [] table)
    {
	for(int i=0;i<table.length;i++)
	    if(table[i].length()==1 && table[i].charAt(0)==c)
		return true;
	return false;
    }
    
    private static boolean matches(String test, String pattern, boolean suffix, boolean reverse, boolean useWild)
    {
	int pos = (reverse) ? test.length()-1 : 0;
	int patPos = (reverse) ? pattern.length()-1 : 0;
	int patDir = (reverse) ? -1 : 1;
	boolean vowelMatch = false;
	boolean conMatch = false;
	boolean finished = false;
	if(suffix && pattern.charAt(0)=='*') return true;
	while(!finished)
	    {
		if(patPos>=pattern.length() ||  patPos<0 || pos<0 || pos>=test.length())
		    return false;
		char t = test.charAt(pos);
		char p = pattern.charAt(patPos);
		
		if(t!=p && !useWild)
		    return false;
		if(t==p || p=='*')
		    patPos+=patDir;
		else if(p==' ' && (t=='.' || t==',' || t==':' || t=='!' || t==';' || t=='?'))
		    patPos+=patDir;
		else if(p=='^' && inTable(t, consonants))
		    patPos+=patDir;
		else if(p=='.' && inTable(t, voicedConsonants))
		    patPos+=patDir;
		else if(p=='+' && inTable(t, startVowels))
		    patPos+=patDir;
		else if(p=='#' && inTable(t, vowels))
		    vowelMatch = true;
		else if(p==':' && inTable(t, consonants))
		    conMatch = true;
		else
		    {
			if(vowelMatch)
			    {
				vowelMatch = false;
				patPos+=patDir;
				pos-=patDir;
			    }
			else
			    if(conMatch || p==':')
				{
				    conMatch = false;
				    patPos+=patDir;
				    pos-=patDir;
				}
			    else
				return false;
		    }
		pos+=patDir;
		if(!suffix)
		    finished = (reverse) ? pos<0 : (pos==test.length() && patPos == pattern.length());
		else
		    finished = (reverse) ? patPos<0 : patPos==pattern.length();
		
	    }
	return true;
    }
    
    private static boolean endingMatches(String suffix)
    {
	for(int i=0;i<endings.length;i++)
	    if(matches(suffix, endings[i], true, false, true))
		return true;
	return false;
    }
    
    private static String findMatch(String word, int start, int end)
    {
	String matchSegment = word.substring(start, end);
	String prefix = word.substring(0, start);
	String suffix = word.substring(end, word.length());
	String retVal = null;
        Vector reVec = (Vector)(ruleBase.get(matchSegment));
        if(reVec!=null)
        {
           for(int i=0;i<reVec.size();i++)
           {
                RuleEntry re = (RuleEntry)(reVec.get(i));
                String prematch = re.before;
                String sufmatch = re.after;		
			if(matches(prefix, prematch, true, true, true))
			    {
				if(matches(suffix, sufmatch, true, false, true)
                                   || (suffix=="%" && endingMatches(suffix)))
				    {
					return re.result;
				    }
			    }
                  }
                }
	return null;
    }
    
    private static String matchWord(String word)
    {
	word = " "+word.toLowerCase()+" ";
	int matchPos = 1;
	int endMatchPos = word.length();
	String phonemeString = "";
	while(matchPos<word.length()-1)
	    {
		endMatchPos--;
		String match = findMatch(word, matchPos, endMatchPos);
		if(match!=null)
		    {
			if(!match.equals("@"))
			    {
				if(!match.endsWith("/"))
				    match=match+"/";
				
				phonemeString = phonemeString+match;
			    }
			matchPos = endMatchPos;      
			endMatchPos = word.length();
		    }
		
	    }
	return phonemeString;
    }
    
    static
    {
        ruleBase = new Hashtable();  
	loadRules();
    }
    
    private static String translate(String phoneme, boolean startEnd)
    {
	phoneme = phoneme.toLowerCase();
	if(startEnd)
	    for(int i=0;i<startEndTable.length;i+=2)
		if(startEndTable[i].equals(phoneme))
		    return startEndTable[i+1];
	
	for(int i=0;i<phonemeTable.length;i+=2)
	    {
		if(phonemeTable[i].equals(phoneme))
		    return phonemeTable[i+1];
	    }
	String newPhoneme = new String(phoneme);
	return newPhoneme;
    }
    
    private static String translateWord(String word, char punc)
    {
	StringTokenizer tok = new StringTokenizer(word,"/",false);
	int pos = 0;
	int maxPos = tok.countTokens();
	String retVal = "";
	String lastPhoneme = "";
	while(tok.hasMoreTokens())
	    {
		String newPhoneme = tok.nextToken();
		if(!newPhoneme.equals(lastPhoneme))
		  {
			retVal+=translate(newPhoneme, ((pos==0)||(!tok.hasMoreTokens())));
			if(pos==maxPos-1)
			    retVal+="@=";
			else if(punc=='.' && pos>maxPos-7)
			    retVal+='<';
			else if(punc=='?' && pos>maxPos-7)
			    retVal+='>';
			retVal+='/';
			   }
		lastPhoneme = newPhoneme;
		pos++;
	    }
	return retVal;
	
    }
    
    
    public static String textToPhonemes(String text)
    {
	text+=" ";
	
	StringTokenizer tok = new StringTokenizer(text," ",false);
	String phonemeString = "";
	int tokPos = 0;
	while(tok.hasMoreTokens())
	    {
		String word = tok.nextToken();
		String phoneticWord = word;
		if(word.indexOf("/")==-1)
		    phoneticWord = matchWord(word);
		String newWord = translateWord(phoneticWord, word.charAt(word.length()-1));
		phonemeString += newWord+"pa4/";
		tokPos++;
	    }
	phonemeString+="pa5/pa5";
	return phonemeString;
    }

}
