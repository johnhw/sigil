package sigil;
import java.awt.event.*;
import java.io.*;
import java.util.Date;
import javax.swing.JFrame;

/**
 * The main class. Simply creates a new frame, and adds
 * a new SignalCanvas. Modules are then loaded from "modules.txt"
 * or from the first parameter on the command line if one is given
 * and the MasterClock is started. Standard error will be recorded 
 * to the file "errlog".
 *
 * @author John Williamson
 */
public class SignalSystem
{
  /**
   * If this is true, then components will use the high-quality
   * but slow transparent drawing methods
   */
    public static boolean useAlpha = true;

    /**
     * Output stream for error messages
     */
    private static PrintStream errorLogger;
    
   public static void createGestureSystem(String args [])
  {
   //Make the new frame
   JFrame jf = new JFrame("SIGIL");
   jf.setSize(800, 700);
   SignalCanvas sigCan = new SignalCanvas();
   jf.getContentPane().add(sigCan);

   //Check for command line arguments
   if(args.length>0)
       {
	   //Check that any file given actually exists
	   File testFile = new File(args[0]);
	   if(testFile.exists())
	       //Load the devices from the given file
	       ModuleLoader.loadModules(args[0]);
	   else
	       {
		   System.err.println("File "+args[0]+" does not exist. Exiting...");
		   System.exit(-1);
	       }
       }
   else
       {
	   //Load the devices from the default modules.txt
	   ModuleLoader.loadModules(); 
       }
   //Start the clock
   MasterClock.initClock();
   MasterClock.startClock();
   
   jf.addWindowListener(new WindowAdapter()
   {
    public void windowClosing(WindowEvent we)
    {
	errorLogger.flush();
	errorLogger.close();
	System.exit(0);
    }   });
   jf.show();
   
  }

  /**
   *  Starts up the main program.
   */
  public static void main(String args[])
  {
      try{
	  
	  //Not buffered to avoid flushing problems when crashes occur
	  errorLogger = new PrintStream(new FileOutputStream("errlog", true));
	  errorLogger.println("** Execution started at "+new Date());
	  System.setErr(errorLogger);
      } catch(FileNotFoundException fnfe)
	  {
	      System.err.println("Could not redirect standard error...");
	  }
      createGestureSystem(args);
  }


}
