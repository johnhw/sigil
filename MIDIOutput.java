import sigil.*;
import duotonic.MIDIUtils;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.swing.text.*;

/**
 * SignalDevice: Produces a stream of notes, parametrized by the incoming signal
 * 
 * @author John Williamson
 */

public class MIDIOutput extends SProcessorModel
{

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "MIDIOutput";
 }
 public String getDescription()
 {
  return "Produces a stream of notes, parametrized by the incoming signal";
 }
 public String getDate()
 {
  return "Janurary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
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
     makeInterface();
    }


 public void connectionChange()
 {
   if(getInputWidth()==0)
     sCon.disableSynth();
   unmodGains = new int[getInputWidth()];
   signalGains = new double[getInputWidth()];
   signalMaps = new String[getInputWidth()];
   clipped = new boolean[getInputWidth()];
   makeInterface();
 }

 private void mapParms(GestureSignal sig)
 {
  for(int i=0;i<sig.vals.length;i++)
  {
   if(signalMaps[i]!=null)
   {
    double newVal = Math.abs(sig.vals[i]*signalGains[i]);
    if(newVal>1.0)
    {
     newVal = 1.0;
     clipped[i] = true;
    }
    else
    clipped[i] = false;
    sCon.setNormalizedParameter(signalMaps[i], newVal);
   }
  }
 }

 private MIDISynth sCon;
 private boolean [] clipped;
 private int [] unmodGains;
 private double [] signalGains;
 private String [] signalMaps;

 transient JFrame jf;

 public void showInterface()
 {
  jf.setTitle(getName());
  jf.show();

 }

  private class PatchListener implements ItemListener
  {

   public void itemStateChanged(ItemEvent ie)
   {
     JComboBox src = (JComboBox)(ie.getSource());
     String value = src.getSelectedItem().toString();
     int newPatch = 0;
     try{
        newPatch=Integer.parseInt(value);
     }catch(NumberFormatException nfe){}
     sCon.setPatch(newPatch);

   }

  }

  private class ComboListener implements ItemListener
  {
   private JTextField toEnable;
   private int index;

   public void itemStateChanged(ItemEvent ie)
   {
     JComboBox jCombo = (JComboBox)(ie.getSource());
     String parmName = (String)jCombo.getSelectedItem();
     if(signalMaps[index]!=null)
        sCon.restoreDefault(signalMaps[index]);

     if(parmName.startsWith("CC"))
      toEnable.setEnabled(true);
     else
      toEnable.setEnabled(false);

     if(parmName!="")
             signalMaps[index] = parmName;
     else
             signalMaps[index] = null;

   }

   public ComboListener(int index, JTextField toEnable)
   {
    this.index = index;
    this.toEnable = toEnable;

   }

  }

  private class SliderListener implements ChangeListener
  {
   private JLabel toUpdate;
   private int index;

   public void stateChanged(ChangeEvent ce)
   {
    JSlider source = (JSlider)(ce.getSource());
    int nVal = 100-source.getValue();
    unmodGains[index] = nVal;
    double newValue = 1.0/Math.pow(10.0,(nVal/20.0));
    signalGains[index] = newValue;

    if(clipped[index])
     toUpdate.setForeground(Color.red);
    else
     toUpdate.setForeground(Color.black);

    toUpdate.setText("-"+nVal+"dB");
   }

   public SliderListener(int index, JLabel toUpdate)
   {
    this.toUpdate = toUpdate;
    this.index = index;
   }


  }

  private void addMIDIParms(JComboBox jCombo)
  {
      jCombo.addItem("");
      Enumeration names = sCon.getParameterNames();
      while(names.hasMoreElements())
	  {
	      String nextName = (String)(names.nextElement());
	      if(!nextName.startsWith("CC"))
		  jCombo.addItem(nextName);
	  }
      jCombo.addItem("CC");
  }

    private class NumListen implements DocumentListener
    {
	private int index;
	private JTextField parent;

	public NumListen(int index, JTextField parent)
	{
	    this.index = index;
	    this.parent = parent;
	}

	public void changedUpdate(DocumentEvent de)
	{

	}

	public void insertUpdate(DocumentEvent de)
	{
	    Document docIns = de.getDocument();
	    int offset = de.getOffset();
	    String text = ""; 
	    try{
	    text= docIns.getText(0, docIns.getLength());
	    } catch(BadLocationException ble) {ble.printStackTrace();}
	    int ccNo = 0;
	    
	    try
		{
		    Integer.parseInt(text);
		    
		} catch(NumberFormatException pe) {parent.setText("0");}

	    if(ccNo>63)
		ccNo=63;

	    if(ccNo<0)
		ccNo=0;
	    
	    if(signalMaps[index]!=null && signalMaps[index].startsWith("CC"))
		{
		    signalMaps[index]="CC-"+ccNo;
		}
	}
	
	public void removeUpdate(DocumentEvent de)
	{

	}


    }

  public void makeInterface()
  {
    if(jf==null)
      jf = new JFrame();
    jf.setTitle(getName());
    jf.setSize(600,400);
    Container gc = jf.getContentPane();
    gc.removeAll();
    gc.setLayout(new BorderLayout());

    Dimension spaceInset = new Dimension(30,30);
    Dimension smallInset = new Dimension(4,4);
    String patchNumbers[] = new String[128];
    for(int i=0;i<128;i++)
     patchNumbers[i]=String.valueOf(i);
    JComboBox patchList = new JComboBox(patchNumbers);
    patchList.addItemListener(new PatchListener());
  
    gc.add(new Box.Filler(spaceInset, spaceInset, spaceInset), BorderLayout.WEST);
    gc.add(new Box.Filler(spaceInset, spaceInset, spaceInset), BorderLayout.EAST);
    gc.add(patchList, BorderLayout.NORTH);
    gc.add(new Box.Filler(spaceInset, spaceInset, spaceInset), BorderLayout.SOUTH);

    int maxI = getInputWidth();
    JPanel outerPanel = new JPanel(new BorderLayout());
    JPanel mainPanel = new JPanel(new GridLayout(maxI,1));
    JScrollPane sPane = new JScrollPane(mainPanel);

    for(int i=0;i<maxI;i++)
    {
     JPanel oneControl = new JPanel(new BorderLayout());
     JComboBox parmCombo = new JComboBox();
     addMIDIParms(parmCombo);
     if(signalMaps[i]!=null)
       parmCombo.setSelectedItem(signalMaps[i]);
     else
      parmCombo.setSelectedItem("");

     JPanel combo = new JPanel(new BorderLayout());
     JPanel ccPanel = new JPanel(new FlowLayout());
     ccPanel.add(new JLabel("CC:"));
     JTextField ccField = new JTextField(3);
     ccField.getDocument().addDocumentListener(new NumListen(i, ccField));
     ccField.setText("0");
     ccField.setEnabled(false);
     parmCombo.addItemListener(new ComboListener(i, ccField));

     ccPanel.add(ccField);
     combo.add(parmCombo, BorderLayout.CENTER);
     combo.add(ccPanel, BorderLayout.SOUTH);

     JPanel sliderPanel = new JPanel(new BorderLayout());
     JLabel valIndicator = new JLabel("-50dB");
     sliderPanel.add(valIndicator,BorderLayout.NORTH);
     JSlider slider = new JSlider(0,100,50);
     slider.addChangeListener(new SliderListener(i,valIndicator));
     slider.setValue(unmodGains[i]);
     sliderPanel.add(slider, BorderLayout.CENTER);
     JLabel label = new JLabel("Signal "+i);
     oneControl.add(sliderPanel, BorderLayout.CENTER);
     oneControl.add(combo, BorderLayout.WEST);
     oneControl.add(label, BorderLayout.EAST);
     oneControl.add(new Box.Filler(smallInset, smallInset, smallInset), BorderLayout.SOUTH);

     mainPanel.add(oneControl);
    }
    outerPanel.add(sPane, BorderLayout.NORTH);
    gc.add(outerPanel, BorderLayout.CENTER);


  }

 public MIDIOutput()
 {
  super();
  setTerminating(true);
  makeInterface();

  sCon = new MIDISynth();
  sCon.enableSynth();
 }

 public void deleted()
 {
  sCon.disableSynth();
 }

 public void processSignal()
 {
   GestureSignal sig = lastSig;      
   if(getInputWidth()>0)
   {
       active = true;
       mapParms(sig);
   }

 }

}
