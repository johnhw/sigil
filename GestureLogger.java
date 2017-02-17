import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class GestureLogger extends SProcessorModel
{
 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "GestureLogger";
 }
 public String getDescription()
 {
  return "Records incoming gestures to a file";
 }
 public String getDate()
 {
  return "March 2002";
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
    
     out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException
    {
    try{
     in.defaultReadObject();
     }catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
    stopRecording();
    }

  
    private String currentName = "";
    private transient PrintStream outStream;
    private transient JToggleButton recordBox;
    private boolean recording = false;
    private long startTime = System.currentTimeMillis();
    private transient JTextField filenameField;
    private transient JLabel filenameLabel;

    
    private class SetFileListener implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    theLabel.setText(theField.getText());
	    currentName = theField.getText();
	    recordBox.setEnabled(true);
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
     JFrame jf;
     JPanel namePanel = new JPanel(new FlowLayout());
     JPanel setPanel = new JPanel(new BorderLayout());
      filenameField = new JTextField(14);
      filenameLabel = new JLabel(currentName);
     filenameField.setText(currentName);
     
     recordBox = new JToggleButton("Recording");
     
     recordBox.setEnabled(false);
     JButton setButton = new JButton("Set file");
     setButton.addActionListener(new SetFileListener(filenameField, filenameLabel));
  
     setPanel.add(recordBox, BorderLayout.CENTER);
  
     recordBox.setSelected(recording);

     recordBox.addActionListener(new ActionListener()
	 {
	     public void actionPerformed(ActionEvent ae)
	     {
		 if(recordBox.isSelected())
		     startRecording();
		 else
		     stopRecording();
	     }
	 });
     namePanel.add(filenameField);
     namePanel.add(setButton);
     namePanel.add(filenameLabel);
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
	String retVal = currentName+".ges";
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
      {
	  recordBox.setSelected(false);
	  recordBox.setEnabled(false);
	  filenameLabel.setText("");
	  filenameField.setText("");
      }
  long curTime = System.currentTimeMillis();
  if(outStream!=null)
      {
	  
	  outStream.println("Finished after "+((curTime-startTime)/1000));
	  outStream.flush();
	  outStream.close();
      }
 }

 public void processHetObject(Object o)
 {
     if(o instanceof ProbabilisticGesture && recording)
	    {
		ProbabilisticGesture pGest = (ProbabilisticGesture) o;
		
		ParameterizedGesture mostProbable = pGest.getMostProbable();
		long curTime = System.currentTimeMillis();
		if(mostProbable!=null)
		    outStream.println(mostProbable.getName()+" "+mostProbable.getProbability()+" "+
				      ((curTime-startTime)/1000));
		
	    }
 }

}
