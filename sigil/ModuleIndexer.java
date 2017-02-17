package sigil;
import java.io.*;
import java.util.*;

public class ModuleIndexer
{
    
    public static void main(String args[])
    {
	if(args.length!=2)
	    System.err.println("Usage: ModuleIndexer <path> <outfile>");
	else
	    {
		Class genClass = null;
		Vector deviceList = new Vector();

		//Loads the interface necessary to check the type
		//of loaded modules
		try{
		    genClass = Class.forName("sigil.GestureGenerator");   
                }catch(ClassNotFoundException cnfe)
		    {
			System.err.println("Cannot find the GestureProcessor/Generator interface! Is the classpath set?");
			System.exit(-1);
		    }    
		
		String outFilename = args[1];
		File thisDir = new File(args[0]);
		try
		    {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outFilename)));
			String [] matchNames = thisDir.list();
			for(int i=0;i<matchNames.length;i++)
			    {
				String testName = matchNames[i];
				if(testName.endsWith(".class") && testName.indexOf("$")==-1)
				    {
					String className = testName.substring(0,testName.length()-6);
					try
					    {
						Class testClass = Class.forName(className);
						if(genClass.isAssignableFrom(testClass))
						    {
						       writer.println(className);
						       deviceList.add(className);
						    }
					    }
					catch(ClassNotFoundException cfne) {
					    System.err.println("Could not read "+className+
							       "; is the classpath set correctly?");
					}
                                        catch(Throwable e) {
					    System.err.println("Cannot instantiate "+className+"; skipping.");
					}
				    }
			    }
			writer.flush();
			writer.close();
		    }
	     catch(IOException ioe){
		System.err.println("Cannot open output file!");
		System.exit(-1);
	     }
	    }
	System.exit(0);
    }



}
