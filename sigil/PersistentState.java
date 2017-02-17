package sigil;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Handles the serialization and deserialization of the
 * state of the current setup.
 *
 * @author John Williamson
 */
public class PersistentState
{
    /**
     * File filter for ".sgc" (signal gesture system) files
     */
    private static class SGCFilter extends javax.swing.filechooser.FileFilter
    {
	public boolean accept(File F)
	{
	    return (F.getName().toLowerCase().endsWith(".sgc"));
	}
	
	public String getDescription()
	{
	    return "Signal Gesture System files (*.sgc)";    
	}
    }
    
    /**
     * Returns the header for the given file
     */
    public static SignalHeader skimHeader(String filename)
    {
	try{
	    ObjectInputStream objIn = new ObjectInputStream(
							       new BufferedInputStream(
										       new FileInputStream(filename)));
	       
	       //Get the header info
	       SignalHeader head = (SignalHeader)(objIn.readObject());
	       objIn.close();
	       return head;
	   } catch(Exception e) 
	       {
		   return null;
	       }

    }

  /**
   * Takes a vector of VisualElements and stores them
   * along with header information from the master clock
   * to a file chosen by the user. Returns true
   * if save was succesful
   */
  public static boolean saveState(Vector systemState)
  {
      checkSetupDir();
      //Create a file chooser
      JFileChooser jfc = new JFileChooser("setups");
      jfc.setFileFilter(new SGCFilter());

      //If user chooses save...
      if(jfc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
	  {
	      File selected = jfc.getSelectedFile();
	      String fName = selected.getName();
	      if(fName.indexOf(".")<0)
		  {
		      fName = "setups"+File.separator+fName+".sgc";
		      selected = new File(fName);
		  }
	      
	      //Update master clock information
	      MasterClock.setFilename(fName.substring(0, fName.indexOf(".")));
	      
	      //Record the current state to the selected file
	      return recordState(selected, systemState);
	  }
      else
	  return false;
      
  }
    
    //The annotation to replace the current one with
    //set by the action listener
    private static String currentAnnotation;
    
    private static class OKListener implements ActionListener
    {
	private JDialog dlg;
	private JTextArea theArea;
	private String oldAnnot;

	public OKListener(JDialog dlg, JTextArea area, String annot)
	{
	    this.dlg = dlg;
	    theArea = area;
	    oldAnnot = annot;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
	    JButton src = (JButton)(ae.getSource());
	    
	    //Check which button was pressed
	    if(ae.getActionCommand().equals("OK"))
                currentAnnotation = theArea.getText();           
	    else if(ae.getActionCommand().equals("Cancel"))
                currentAnnotation = oldAnnot;
	    dlg.dispose();
	}
	
    }
    

    /**
     * Edit the annotation information for a file
     *
     */
    private static String editAnnotation(String annotation)
    {        
        JDialog jf = new JDialog();
        jf.setTitle("Please edit annotation...");
        jf.setModal(true);
        jf.setSize(600,200);
        Container gc = jf.getContentPane();
        JPanel annotPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        JTextArea annotArea = new JTextArea(annotation);
        annotArea.setLineWrap(true);
        okButton.addActionListener(new OKListener(jf, annotArea, annotation));
        cancelButton.addActionListener(new OKListener(jf, annotArea, annotation));
        annotPanel.add(buttonPanel, BorderLayout.SOUTH);
        annotPanel.add(annotArea, BorderLayout.CENTER);
        gc.add(annotPanel);
	UIUtils.setColors(gc, Color.black, Color.white);
        jf.show();
        return currentAnnotation;
   }

  /**
   * Records the current system state to the specified file
   * Takes a vector of VisualElements, representing the
   * current device setup
   */
  private static boolean recordState(File outFile, Vector state)
  {

      //Open an output stream
      try{
	  //Check that the state can actually be serialized before overwriting the file
	  TemporarySerializer tempSer = new TemporarySerializer(state);
	  
	  if(tempSer.getBuffer()!=null)
	      {
		  ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
		  
		  
		  //Get the header
		  SignalHeader head = MasterClock.getHeader();
		  
		  //Update version number; increases by one each time file is saved
		  head.verNo++;
		  
		  //Allow user to edit annotation
		  head.annotation = editAnnotation(head.annotation);
		  
		  //Update the header date
		  head.date = MasterClock.dateForm.format(new Date());
		  
		  
		  //Write header
		  objOut.writeObject(head);
		  
		  //Write the state vector
		  objOut.writeObject(state);
		  
		  //Write the master clock vectors
		  MasterClock.writeElts(objOut);
		  
		  //Shutdown output stream
		  objOut.flush();
		  objOut.close();
	      }

      } catch(IOException ioe) {
	  ioe.printStackTrace();
	  JOptionPane.showMessageDialog(null, "Could not write file!", "Save error", JOptionPane.ERROR_MESSAGE);   
	  return false;
      }
      return true;
  }
    
    

    /**
     * Deserialize the state of the system from the specified
     * file. Returns the vector of VisualElements for SignalCanvas to use
     * and updates the MasterClock information
     */
    private static Vector restoreState(File inFile)
    {
	Vector returnVec = new Vector();
	
	//Open stream
	try{
	    ObjectInputStream objIn = new ObjectInputStream(
							    new BufferedInputStream(
										    new FileInputStream(inFile)));
	    
	    //Get the header info
	    SignalHeader head = (SignalHeader)(objIn.readObject());
	    
	    //Get the VisualElements
	    returnVec = (Vector)(objIn.readObject());
	    
	    //Update the MasterClock lists
	    MasterClock.readElts(objIn);
	    
	    //Update the master clock header info
	    MasterClock.setHeader(head);
	    
	    //Shutdown stream
	    objIn.close();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	    
	    //Warn user of errors
	    JOptionPane.showMessageDialog(null, "Could not read file!", "Load error", JOptionPane.ERROR_MESSAGE);   
	    return null;
	}
	catch(ClassNotFoundException cfne) {
	    JOptionPane.showMessageDialog(null, "Class error while loading: "+cfne.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
	    return null;
	}
	return returnVec;
    }
    
    /**
     * Checks if the directory "setups" exists, and creates it if it
     * does not
     */
    private static void checkSetupDir()
    {
	File testDir = new File("setups");
	if(!testDir.exists())
	    testDir.mkdir();
    }

    /**
     * Deserialize the system state, allowing the user to select
     * a file to load from. Returns the vector of VisualElements
     * for SignalCanvas to use. Updates MasterClock info
     */
    public static Vector loadState()
    {
	checkSetupDir();
        FileBrowser fb = new FileBrowser(".sgc", "setups");
       	String fName = fb.getFilename();
	if(fName!=null)
	    {
                int slashIndex = fName.indexOf(File.separator);
		if(slashIndex<0)
		    slashIndex = 0;
		MasterClock.setFilename(fName.substring(slashIndex, fName.lastIndexOf(".")));
		//Deserialize
		return restoreState(new File(fName));
	    }
	return null;
    }
    
}
