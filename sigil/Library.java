package sigil;
import java.io.*;
import java.util.jar.*;
import java.util.zip.*;
import java.util.*;
import java.net.*;
/**
 * Handles the loading of files from the main JAR file from the system.
 * All classes that need to read initialization or data files should 
 * use this class to load them from the jar. Such files should be placed
 * within the lib/ directory of the jar file. If files exist in the current
 * directory, they will be loaded from there in preference to the jar file
 *
 * @author John Williamson
 */
public class Library
{
    //Name of the system jar file
    private static final String jarFilename = "sigil.jar";

    //The actual reference to the file
    private static JarFile sigilJar;

    //Open the system jar file
    static
    {
	try{
	sigilJar = new JarFile(jarFilename);
	} catch(IOException ioe)
	    {

		File testFile = new File("lib");
		if(!(testFile.exists() && testFile.isDirectory()))
		    {
			System.err.println("JAR file "+jarFilename+" and there is no lib/ directory! Exiting...");
			System.exit(-1);
		    }
	    }
    }
    
    /**
     * Returns a URL inside the system jar file, pointing to the
     * given file
     */
    public static URL getURL(String jarFile)
    {
	try{
	    File testFile = new File("lib"+File.separator+jarFile);
	    if(testFile.exists())
		return new URL("file:lib/"+jarFile);
	    else
		return new URL("jar:file:"+jarFilename+"!/lib/"+jarFile);
	} catch(MalformedURLException mue) { return null; }
    }

    /**
     * Return a buffered reader to read from the given filename
     */
    public static BufferedReader getReader(String filename) throws IOException
    {
	 File testFile = new File("lib"+File.separator+filename);
	 if(testFile.exists())
	     return new BufferedReader(new FileReader(testFile));
	 else
	     return new BufferedReader(new InputStreamReader(getInputStream(filename)));
    }

    /**
     * Return an input stream to read from the given filename
     */
    public static InputStream getInputStream(String filename) throws IOException
    {
	File testFile = new File("lib"+File.separator+filename);
	if(testFile.exists())
	    return new FileInputStream(testFile);
	else
	    {
		ZipEntry fileEntry = sigilJar.getEntry("lib/"+filename);
		return sigilJar.getInputStream(fileEntry);
	    }
    }

}
