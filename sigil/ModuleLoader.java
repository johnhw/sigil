package sigil;
import java.io.*;
import java.util.*;

/**
 * Handles the dynamic loading of devices.
 * Can add and update modules during runtime.
 *
 * @author John Williamson
 */

public class ModuleLoader
{
    //The hashtable mapping generator names to the corresponding Class object
    private static Hashtable genHash;

    //The hashtable mapping processor names to the corresponding Class object
    private static Hashtable procHash;

    //The Class objects for the interfaces for the generators and processors
    private static Class procClass, genClass;

    //Hashtable mapping names of devices to groups
    private static Hashtable groupHash;
    
    /**
     *  Returns the vector of names of devices associated
     *  with a group
     */
    public static Vector getGroupDevices(String groupName)
    {
	return (Vector)(groupHash.get(groupName));
    }

    /**
     * Returns the names of all the currently loaded groups
     */
    public static Enumeration getGroupNames()
    {
	return groupHash.keys();
    }

    /**
     * Returns the names of all the currently load generators
     */
    public static Enumeration getGenNames()
    {
	return genHash.keys();   
	
    }

    /**
     * Returns the names of all of the currently loaded processors
     */
    public static Enumeration getProcNames()
    {
    	return procHash.keys();	
    }
    
    /**
     * Creates a new instance of the element with the given name
     * and wraps it in a GestureElement, then registers it with the
     * master clock
     */
    public static GestureElement createElement(String name)
    {
	GestureElement gElt = null;
	try{
	    if(genHash.containsKey(name))
		{
		    gElt= new GestureElement(((Class)genHash.get(name)), true, name);
		}
	    else if(procHash.containsKey(name))
		{
		    gElt= new GestureElement(((Class)procHash.get(name)), false, name);
		}
	} catch(Exception E)
	    {
		System.err.println("Could not create an instance of "+name+"; "+E.getMessage());		

		E.printStackTrace();
	    }
	MasterClock.registerElement(gElt);
	return gElt;
	
    }


    static 
    {
	genHash = new Hashtable();
	procHash = new Hashtable();
	groupHash = new Hashtable();

        //Loads the interfaces necessary to check the type
        //of loaded modules
	try{
	    procClass = Class.forName("sigil.GestureConsumer");
	    genClass = Class.forName("sigil.GestureGenerator");   
	}catch(ClassNotFoundException cnfe)
	    {
                System.err.println("Cannot find the GestureProcessor/Generator interface! Is the classpath set?");     
	    }
    }

    /**
     * Loads each of the modules listed in the given text file
     * One class per line, groups are denoted by an asterisk followed by a name
     * and all following devices will be inserted in that group until the next group name
     */
    public static void loadModules(String filename)
    {		
	BufferedReader modRead;
	String currentGroup = "Ungrouped";
        //Check that the interfaces have been initialized
	if(procClass==null || genClass==null)
	    return;
        //Open the file
	try{
	    modRead = new BufferedReader(new FileReader(filename));
	}catch(IOException ioe) {
	    System.err.println("Cannot open the module file ["+filename+"]!");
	    return;
	}
	try{
	    String modName = modRead.readLine();
	    
            //Read each line
	    while(modName!=null)
		{
		    if(modName.charAt(0)!='*')
			//Add the next name
			addModule(modName, currentGroup);		    
		    else
			currentGroup = modName.substring(1);
		      modName = modRead.readLine();
		}
	}catch(IOException ioe) {
	    System.err.println("Error while reading "+filename);
	    System.exit(-1);
	}
    }

    /**
     * Loads each of the modules listed in the text file modules.txt.
     * One class per line, groups are denoted by an asterisk followed by a name
     * and all following devices will be inserted in that group until the next group name
     */
    public static void loadModules()
    {		
	BufferedReader modRead;
	String currentGroup = "Ungrouped";
        //Check that the interfaces have been initialized
	if(procClass==null || genClass==null)
	    return;

        //Open the file
	try{
	    modRead = Library.getReader("modules.txt");
	}catch(IOException ioe) {
	    System.err.println("Cannot open the module file [modules.txt]!");
	    return;
	}
	try{
	    String modName = modRead.readLine();
	    
            //Read each line
	    while(modName!=null)
		{
		    if(modName.length()>0)
			{
			    if(modName.charAt(0)!='*')
				//Add the next name
				addModule(modName, currentGroup);		    
			    else
				currentGroup = modName.substring(1);
			}
		    modName = modRead.readLine();
		}

	}catch(IOException ioe) {
	    System.err.println("Error while reading modules.txt");
	    System.exit(-1);
	}
    }

    /**
     * Load the module with the specified name and put it in the group "Ungrouped"
     * so that it can then be instantiated with
     * createElement()
     */
    public static void addModule(String modName)
    {
	addModule(modName, "Ungrouped");
    }

    /**
     * Load the module with the specified name and put it in the specified group
     * so that it can then be instantiated with
     * createElement()
     */
    public static void addModule(String modName, String group)
    {
	boolean generator;

        //Check interfaces exist
	if(procClass==null || genClass==null)
	    return;

        //Check name is valid
        if(modName!=null && modName.length()>0)
	    {
		try{

                    //Load the class
                    Class thisModule = Class.forName(modName);

                    //Check if is a gesture module
		    if(genClass.isAssignableFrom(thisModule))
			{
			    generator = !procClass.isAssignableFrom(thisModule);

                            //Check whether the class is a generator or processor
			    if(generator)
				genHash.put(modName, thisModule);
			    else
				procHash.put(modName, thisModule);

			    Vector nameVec;
			    if(groupHash.containsKey(group))
				nameVec = (Vector)(groupHash.get(group));
			    else
				nameVec = new Vector();

			    nameVec.add(modName);
			    Collections.sort(nameVec);
			    groupHash.put(group, nameVec);
			}
		    else
			{
			    System.err.println("Warning: class "+modName+" is not a gesture class, skipping...");
			}
		    
		} catch(ClassNotFoundException cnfe)
		    {
			System.err.println("Warning: class file for "+modName+" not found...");
		    }
	    }
    }

}
