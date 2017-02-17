import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.sound.sampled.*;

public class RhythmLoop extends SProcessorModel
{

 private static Color recColor = new Color(88,48,18);
 private Clip audioClip;


 static final long serialVersionUID = 213L;
 private int beatSlice;
 private int beatPos;
 private int slices = 16;

 public void connectionChange()
 {

 }

 public String getGenName()
 {
  return "RhythmLoop";
 }
 public String getDescription()
 {
  return "Loops a sound file continously";
 }
 public String getDate()
 {
  return "March 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }


  public Color getColor()
  {
	if(!playing)
	    return super.getColor();
	else
	    return recColor;
  }



   public RhythmLoop()
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
       
    }

    private String currentName = "";
    
    private boolean playing = false;
    
    
    public Vector getBaseNames()
    {
        File thisDir = new File("loops/");
	File [] files = thisDir.listFiles();
	int maxInt = 0;
        Vector fileList = new Vector();
	for(int i=0;i<files.length;i++)
	    {
		String fname = files[i].getName();
                if(fname.endsWith(".wav"))
		    {
                           int endChop = fname.indexOf(".");
			
                                 String name = fname.substring(0, endChop);
                                fileList.add(name);
                }
	 }
	return fileList;
 }


    private class NameChangeListener implements ItemListener
    {

	public void  itemStateChanged(ItemEvent e)
	{
	    JComboBox src = (JComboBox)(e.getSource());
	    String curSel = src.getSelectedItem().toString();
            if(playing && audioClip!=null)
            {
             audioClip.stop();
             loopSample("loops/"+curSel+".wav");
            }
	}
 
    }

    public int getSignalWidth()
    {
        return 0;
    }

 JToggleButton playButton  = new JToggleButton("Play");

 public void showInterface()
 {

  if(playing && audioClip!=null)
         audioClip.stop();

  Vector baseNames = getBaseNames();
  JFrame jf;

 
  final JComboBox nameBox = new JComboBox(baseNames);
 
  nameBox.addItemListener(new NameChangeListener());

  JPanel setPanel = new JPanel(new BorderLayout());
  setPanel.add(nameBox, BorderLayout.CENTER);
  
  playButton.addActionListener(new ActionListener()
      {
	  public void actionPerformed(ActionEvent ae)
	  {
	      currentName = nameBox.getSelectedItem().toString();
	      if(playing)
                  stopPlaying();
	      else
		  startPlaying();
	  }

      });
 

  JPanel controlPanel = new JPanel(new FlowLayout());
  controlPanel.add(playButton);

  JPanel mainPanel = new JPanel(new BorderLayout());
  mainPanel.add(controlPanel, BorderLayout.SOUTH);
  mainPanel.add(setPanel, BorderLayout.NORTH);

  jf = new JFrame();
  jf.setSize(400,90);
  jf.getContentPane().add(mainPanel, BorderLayout.CENTER);
  
  jf.setTitle(getName());
  jf.show();
 }

 public void skip(int beatPoint)
 {
  if(playing && audioClip!=null)
        audioClip.setFramePosition(beatPoint*beatSlice);
 }

 private void loopSample(String name)
 {
  try{
  if(audioClip!=null)
    audioClip.stop();
  File openFile = new File(name);
  AudioFormat audioForm = AudioSystem.getAudioFileFormat(openFile).getFormat();
  DataLine.Info clipInfo = new DataLine.Info(Clip.class, audioForm);
  audioClip = (Clip)(AudioSystem.getLine(clipInfo));
  audioClip.open(AudioSystem.getAudioInputStream(openFile));
  beatSlice = audioClip.getFrameLength()/slices;
  

  audioClip.loop(Clip.LOOP_CONTINUOUSLY);
  } catch(Exception E) {E.printStackTrace();}
 }

 public void deleted()
 {
 }


 public void startPlaying()
 {
     playing = true;
     if(!currentName.equals(""))
            loopSample("loops/"+currentName+".wav");                
 }

 public void stopPlaying()
 {
     
  playing = false;
  audioClip.stop();

 }

    
}
