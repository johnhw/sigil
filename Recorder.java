import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class Recorder extends SProcessorModel
{
 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Recorder";
 }
 public String getDescription()
 {
  return "Records incoming signals to a file";
 }
 public String getDate()
 {
  return "Janurary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }


    private static Color recColor = new Color(88,48,18);
    public Color getColor()
    {
	if(!recording)
	    return procColor;
	else
	    return recColor;
    }


 public void connectionChange()
 {
 }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
     stopRecording();
     out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException
    {
    try{
     in.defaultReadObject();
     }catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
    getCurrentNum();
    
    }

  
 private String currentName = "defGest";
 private PrintStream outStream;
 private int currentNum;
 transient JLabel numLab;
 transient JCheckBox recordBox;
 private boolean recording = false;
 private long startTime = 0;

 public void getCurrentNum()
 {
     File thisDir = new File(".");
     File [] files = thisDir.listFiles();
     int maxInt = 0;
     for(int i=0;i<files.length;i++)
	 {
	     String fname = files[i].getName();
	     if(fname.endsWith(".dat") && fname.indexOf(currentName)>=0)
		 {
		     int startChop = fname.indexOf(currentName)+currentName.length();
		     int endChop = fname.indexOf(".dat");
		     String num = fname.substring(startChop, endChop);
		     int thisNum = maxInt;
		     try
			 {
			     thisNum = Integer.parseInt(num);
			     
			 } catch(NumberFormatException nfe) {}
		     if(thisNum>maxInt)
			 maxInt = thisNum;
		 }
	     
	 }
     currentNum = maxInt;
 }


    private class SetFileListener implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    theLabel.setText(theField.getText());
	    currentName = theField.getText();
	    getCurrentNum();
	    numLab.setText(""+currentNum);

	}

	private JTextField theField;
	private JLabel theLabel;

	public SetFileListener(JTextField field, JLabel label)
	{
	    theField = field;
	    theLabel = label;
	}


    }

 public void showInterface()
 {
  getCurrentNum();
  JFrame jf;
  JPanel namePanel = new JPanel(new FlowLayout());
  JPanel setPanel = new JPanel(new BorderLayout());
  JTextField tField = new JTextField(14);
  JLabel fileName = new JLabel(currentName);
  JButton recButton = new JButton("Start/Stop");
  tField.setText(currentName);

  recordBox = new JCheckBox("Recording ");
  
  recordBox.setEnabled(false);
  JButton setButton = new JButton("Set file");
  setButton.addActionListener(new SetFileListener(tField, fileName));
  
  setPanel.add(recordBox, BorderLayout.CENTER);
  setPanel.add(recButton, BorderLayout.EAST);
  
  recButton.addActionListener(new ActionListener()
      {
	  public void actionPerformed(ActionEvent ae)
	  {
	      if(recording)
		  stopRecording();
	      else
		  startRecording();
	  }
      });
  recordBox.setSelected(recording);
  numLab = new JLabel(""+currentNum);
  namePanel.add(tField);
  namePanel.add(setButton);
  namePanel.add(fileName);
  namePanel.add(numLab);
  jf = new JFrame();
  jf.setSize(400,90);
  jf.getContentPane().add(namePanel, BorderLayout.CENTER);
  jf.getContentPane().add(setPanel, BorderLayout.SOUTH);
  jf.setTitle(getName());
  jf.show();
 }



 public void deleted()
 {
 }

 private String getFileName()
 {
  String retVal = currentName+"_"+currentNum+".dat";
  currentNum++;
  if(numLab!=null)
   numLab.setText(""+currentNum);
  return retVal;
 }

 public void startRecording()
 {

	     startTime = System.currentTimeMillis();
	     try
		 {
		     outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(getFileName())));
		 }catch(IOException ioe) { ioe.printStackTrace(); }
	     
	     if(recordBox!=null)
		 recordBox.setSelected(true);
	     recording = true;
 }

 public void stopRecording()
 {
  recording = false;
  if(recordBox!=null)
   recordBox.setSelected(false);
  if(outStream!=null)
      {
	  outStream.flush();
	  outStream.close();
      }
 }


 public void processSignal()
 {
  if(recording)
  {
   outStream.print(lastSig.time-startTime+" ");
   for(int i=0;i<lastSig.vals.length;i++)
    outStream.print(lastSig.vals[i]+" ");
   outStream.println("");

  }
 }

 public void processHetObject(Object o)
 {
 if(o instanceof String)
 {
  String s = (String)o;
  if(s.endsWith("SEGMENT"))
   if(recording)
     stopRecording();
   else
     startRecording();
 }
 }

}
