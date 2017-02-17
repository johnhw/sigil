import java.util.*;
public class StateInformation 
{

    private Hashtable parameters;
    private int transition, alphabet, divisions;
    private int pathInfo;
    

    public String toString()
    {
	String retVal = "[StateInformation "+transition+" ";
	Enumeration enum = parameters.keys();
	while(enum.hasMoreElements())
	    retVal+=enum.nextElement().toString()+" ";
	retVal+="]";
	return retVal;
    }

    public Object getObject(String name)
    {
	return parameters.get(name);
    }

    public void setObject(String name, Object o)
    {
	parameters.put(name, o);
    }

    public boolean getBoolean(String name)
    {
	Object o = parameters.get(name);
	if(o instanceof Boolean)
	    {
		return ((Boolean)o).booleanValue();
	    }

	else
	    return false;
    }

    public void setBoolean(String name)
    {
	setBoolean(name, true);
    }

    public void setBoolean(String name, boolean parm)
    {
	parameters.put(name, new Boolean(parm));
    }

    public void setTransition(int transition, int divisions, int alphabet)
    {
	this.transition = transition;
	this.divisions = divisions;
	this.alphabet = alphabet;
	setBoolean("Transition");
    }

    public StateInformation()
    {
	parameters = new Hashtable();
	this.transition = transition;
    }

    public void setPathInfo(int pathInfo)
    {
	this.pathInfo = pathInfo;
    }
    
    public int getPathInfo()
    {
	return pathInfo;
    }
    
    public int getTransition()
    {
	return transition;
    }

    public int getAlphabet()
    {
	return alphabet;
    }

    public int getDivisions()
    {
	return divisions;
    }
    



}
