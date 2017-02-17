import duotonic.*;
import sigil.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class SpaceMIDI extends SProcessorModel
{
    private int patch = 0;
    private Hashtable melodyTable;
    
    int [] mjScale = {0,2,4,5,7,9,11,12};
    int [] mjNotes = {0,4,7,12,16,19,24};
    int [] miScale = {0,3,4,5,7,9,11,12};
    int [] miNotes = {0,3,7,12,15,19,24};
    int [] penta =   {0,2,4,7,9,12,14,16};
    int [] currentMelody;


    static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "SpaceMIDI";
 }
 public String getDescription()
 {
  return "Provides MIDI audio feedback on Quanizer output (for 2D 3x3)";
 }
 public String getDate()
 {
  return "April 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }



  public void connectionChange()
  {

  }
    

   private class PatchListener implements ItemListener
    {
	

	public void itemStateChanged(ItemEvent ie)
	{
	    JComboBox src = (JComboBox)(ie.getSource());
	    patch  = src.getSelectedIndex();
	}
    }

    private class ComboListener implements ItemListener
    {
	public void itemStateChanged(ItemEvent ie)
	{
	    JComboBox jCombo = (JComboBox)(ie.getSource());
	    String selectedMelody = (String)(jCombo.getSelectedItem());
	    currentMelody = (int [])(melodyTable.get(selectedMelody));
	}
    }
	
    public void showInterface()
    {
     JPanel cen = UIUtils.addFillers(new JPanel(new BorderLayout()), 20, 20);
     Box eastPanel = Box.createVerticalBox();

     JComboBox melodyBox = new JComboBox();
     Enumeration enum = melodyTable.keys();
     while(enum.hasMoreElements())
	 {
	     String melodyName = (String)(enum.nextElement());
	     melodyBox.addItem(melodyName);
	 }

     melodyBox.addItemListener(new ComboListener());
     eastPanel.add(melodyBox);

     JLabel patchLabel = new JLabel("Patch");
     JComboBox patchBox = MIDIUtils.getPatchSelector(patch);
     
     patchLabel.setForeground(Color.black);
     patchBox.addItemListener(new PatchListener());

     eastPanel.add(patchLabel);
     eastPanel.add(patchBox);

     cen.add(eastPanel, BorderLayout.EAST);

     JFrame jf = new JFrame();
     jf.getContentPane().add(cen);
     UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
     jf.setSize(180,100);
     jf.setTitle(getName());
     jf.show();
    }


    private void createMelodies()
    {
	melodyTable = new Hashtable();
	melodyTable.put("Major scale", mjScale);
	melodyTable.put("Major chord", mjNotes);
	melodyTable.put("Minor scale", miScale);
	melodyTable.put("Minor chord", miNotes);
	melodyTable.put("Pentatonic scale", penta);
	currentMelody = penta;
    }


    public SpaceMIDI()
    {	
	createMelodies();
    }

   public void processHetObject(Object o)
   {
       if(o instanceof StateInformation)
	   {
	       StateInformation stateInfo = (StateInformation) o;
	       if(stateInfo.getBoolean("Transition"))
		   playTransitionFeedback(stateInfo.getTransition());
	   }
   }

    private void playTransitionFeedback(int spaceSegment)
    {
	if(spaceSegment>0 && spaceSegment<9)
	    MIDIUtils.playNote(0, 60+currentMelody[spaceSegment-1], 100, 100, patch);
    }

         
}
