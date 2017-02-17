import duotonic.*;
import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import java.io.*;

public class ZeroSpeech extends SProcessorModel
{

    private transient Speech standardVoice = new Speech();
    private double [] lastVals;
    private double [] maxVals;
    private String [] phonemeMatrix;
    private String [] defaultPhonemeTable = {"tt2/ae", "rr2/uh", "ss/iy", "mm/aw", "sh/ay", "kk1/aa"};

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "ZeroSpeech";
 }
 public String getDescription()
 {
  return "Outputs phoneme sequences at zero crossings in the input signal";
 }
 public String getDate()
 {
  return "March 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }

    
    public void connectionChange()
    {
      int width = getInputWidth();
      lastVals = new double[width];
      maxVals = new double[width];
      int minLen = (int)(Math.min(defaultPhonemeTable.length, width*2));
      phonemeMatrix = new String[width*2];
      System.arraycopy(defaultPhonemeTable, 0, phonemeMatrix, 0, minLen);
    }

    private class PhonemeListener implements DocumentListener
    {
	private int index;

	public void changedUpdate(DocumentEvent dc)
	{
	    Document src = (Document)(dc.getDocument());
	    try{
	    phonemeMatrix[index] = src.getText(0,src.getLength());
	    } catch(BadLocationException ble) {}
	}
	
	public void insertUpdate(DocumentEvent dc)
	{
	    changedUpdate(dc);
	}

	public void removeUpdate(DocumentEvent dc)
	{
	    changedUpdate(dc);
	}

	public PhonemeListener(int index)
	{
	    this.index = index;
	}

    }


    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}

	standardVoice = new Speech();
    }


   
    private JPanel  makePhonemePanel()
    {
	int nControls = getInputWidth()*2;
	JPanel retVal = new JPanel(new GridLayout(nControls, 2));
	
	for(int i=0;i<nControls;i+=2)
	    {
		JLabel pos = new JLabel("Axis "+(i/2)+" positive");
		JTextField posBox = new JTextField(12);
		JLabel neg = new JLabel("Axis "+(i/2)+" negative");
		JTextField negBox = new JTextField(12);
		posBox.setText(phonemeMatrix[i]);
		negBox.setText(phonemeMatrix[i+1]);
		neg.setForeground(Color.black);
		pos.setForeground(Color.black);
		posBox.getDocument().addDocumentListener(new PhonemeListener(i));
		negBox.getDocument().addDocumentListener(new PhonemeListener(i+1));
		retVal.add(pos);
		retVal.add(posBox);
		retVal.add(neg);
		retVal.add(negBox);
	    }
	return retVal;
    }


    public void showInterface()
    {
	if(getInputWidth()>0)
	    {
		JFrame jf = new JFrame();
		jf.setTitle(getName());
		jf.setSize(400,300);
		JPanel phonemePanel = makePhonemePanel();
		jf.getContentPane().add(phonemePanel, BorderLayout.NORTH);
		jf.show();
	    }
    
    }

    public ZeroSpeech()
    {
	super();
	setTerminating(true);	
    }

    public void deleted()
    {
    }
    


    private void zeroCrossing(int axis, int direction)
    { if(maxVals[axis]>10)
    {
      double volume = maxVals[axis]/100.0;
      if(volume>1.0) volume = 1.0;
      standardVoice = new Speech();
      standardVoice.setDistance((1.5-volume));
      if(axis*2+direction<phonemeMatrix.length)
	  standardVoice.say(phonemeMatrix[axis*2+direction],false);

     }
    }



    private void calcZeroCrossings(double [] vals)
    {
        int width = getInputWidth();
        for(int i=0;i<getInputWidth();i++)
        {
         if(lastVals[i]<0 && vals[i]>=0)
         {
          zeroCrossing(i, 0);
          maxVals[i]=0.0;
         }
         else if (lastVals[i]>=0 && vals[i]<0)
         {
          zeroCrossing(i, 1);
          maxVals[i]=0.0;
         }

         if(Math.abs(vals[i])>maxVals[i])
                maxVals[i]=Math.abs(vals[i]);
         lastVals[i] = vals[i];
        }
	
    }


 public void processSignal()
 {
   GestureSignal sig = lastSig;      
   if(getInputWidth()>0)
   {
       active = true;
       calcZeroCrossings(sig.vals);
   }
 }

}


