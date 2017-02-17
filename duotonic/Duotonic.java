package duotonic;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 * Speech synthesizer interface
 *
 * @author John Williamson
 */

public class Duotonic extends JDialog implements ActionListener
{

    
    //Sliders for each of the controls
    private JSlider pitchSlider;
    private  JSlider volumeSlider;
    private  JSlider filterSlider;
    private  JSlider panSlider;

    //The textfield for the text to speak
    private JTextField text;
    
    private  JLabel efx;

    //Special effects sliders
    private  JSlider delaySlider;
    private  JSlider delayMixSlider;
    private  JSlider flangeSlider;
    private  JSlider delayFilterSlider;
    

    //Colors used in the interface
    private static final Color darkFore = new Color(100,100,100);
    private static final Color lightFore = new Color(170,170,170); 

    private Speech finalSpeaker;
   
    /**
     * Make the panel that contains all of the main controls
     */
    private JPanel makeControlPanel()
    {

	
        JPanel controlPanel = new JPanel(new BorderLayout());
        setColors(controlPanel, true);
        controlPanel.setBorder(new LineBorder(new Color(80,80,80)));
        JPanel newPanel = addFillers(controlPanel, 10);
        JPanel sliderPanel = new JPanel(new GridLayout(4, 1));
        setColors(sliderPanel, true);
        newPanel.add(sliderPanel, BorderLayout.CENTER);

        pitchSlider = new JSlider(0, 100, 50);
        setColors(pitchSlider, true);
        JPanel pitchPan = nameSlider(pitchSlider, "Pitch", true);
        sliderPanel.add(pitchPan);

        volumeSlider = new JSlider(0, 100, 100);
        setColors(volumeSlider, true);
        JPanel volumePan = nameSlider(volumeSlider, "Volume", true);
        sliderPanel.add(volumePan);


        filterSlider = new JSlider(0, 100, 0);
        setColors(filterSlider, true);
        JPanel filterPan = nameSlider(filterSlider, "Filter", true);
        sliderPanel.add(filterPan);

	panSlider = new JSlider(0, 100, 50);
        setColors(panSlider, true);
        JPanel panPan = nameSlider(panSlider, "Pan", true);
        sliderPanel.add(panPan);

        return controlPanel;

  }


    /**
     * Speak the current text, using the parameters
     * from the sliders
     */
    public void actionPerformed(ActionEvent ae)
    {
	String textToSay = text.getText();
	Speech speaker = getCurrentSpeaker();
	speaker.textToSpeech(textToSay, false);

    }

    /**
     * Make the panel containing the text field
     */
    private JPanel makeSayPanel()
    {
	JPanel sayPanel = new JPanel(new BorderLayout());
	setColors(sayPanel, true);
	JPanel cenPanel = addFillers(sayPanel, 0, 10);
	setColors(cenPanel, true);
	
	JButton say = new JButton("Say");
	say.addActionListener(this);
	setColors(say, false);
	text = new JTextField(40);
        text.addActionListener(this);
	text.setBorder(new LineBorder(darkFore));
	setColors(text, false);
	cenPanel.add(say, BorderLayout.WEST);
	cenPanel.add(text, BorderLayout.EAST);
	return sayPanel;
    }
    
    /**
     * Make the panel containing the effects controls
     */
    private JPanel makeEFXPanel()
    {
        JPanel efxPanel = new JPanel(new BorderLayout());
        setColors(efxPanel, true);
        efxPanel.setBorder(new LineBorder(new Color(80,80,80)));
        JPanel newPanel = addFillers(efxPanel, 10);
        JPanel sliderPanel = new JPanel(new GridLayout(1, 4));
        setColors(sliderPanel, true);
        newPanel.add(sliderPanel, BorderLayout.CENTER);

        delaySlider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        setColors(delaySlider, true);
        JPanel delayPan = nameSlider(delaySlider, "Delay", false);
        sliderPanel.add(delayPan);

        delayMixSlider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        setColors(delayMixSlider, true);                   
        JPanel delayMixPan = nameSlider(delayMixSlider, "Delay mix", false);
        sliderPanel.add(delayMixPan);

        flangeSlider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        setColors(flangeSlider, true);
        JPanel flangePan = nameSlider(flangeSlider, "Flange", false);
        sliderPanel.add(flangePan);

        delayFilterSlider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        setColors(delayFilterSlider, true);
        JPanel delayFilterPan = nameSlider(delayFilterSlider, "Dampening", false);
        sliderPanel.add(delayFilterPan);

        efx = new JLabel("EFX");
        setColors(efx, false);
        newPanel.add(efx, BorderLayout.NORTH);

        return efxPanel;
  }

    /**
     * Take a slider and add a name label, returning a panel with
     * the two of them in it. Top should be true to add the label 
     * above the slider
     */
    private JPanel nameSlider(JSlider slider, String name, boolean top)
    {
	JPanel newPanel = new JPanel(new BorderLayout());
	Dimension dFill = new Dimension(5, 5);
	Box.Filler left = new Box.Filler(dFill, dFill, dFill);
	Box.Filler right = new Box.Filler(dFill, dFill, dFill);
	
	Box.Filler leftT = new Box.Filler(dFill, dFill, dFill);
	Box.Filler rightT = new Box.Filler(dFill, dFill, dFill);
	
	setColors(newPanel, true);
	slider.setBorder(null);
	setColors(slider, true);
	newPanel.add(slider, BorderLayout.CENTER);
	JPanel labelPanel = new JPanel(new BorderLayout());
	JLabel nameLabel = new JLabel(name);
	nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	nameLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
	
	setColors(nameLabel, false);
	setColors(labelPanel, true);
	labelPanel.add(nameLabel, BorderLayout.CENTER);
	labelPanel.add(left, BorderLayout.WEST);
	labelPanel.add(right, BorderLayout.EAST);
	
	newPanel.add(leftT, BorderLayout.WEST);
	newPanel.add(rightT, BorderLayout.EAST);
	
	newPanel.add(slider, BorderLayout.CENTER);
	if(top)
	    newPanel.add(labelPanel, BorderLayout.NORTH);
	else
	    newPanel.add(labelPanel, BorderLayout.SOUTH);
	return newPanel;
    }
    

    /** 
     * Set the colors for a component, use dark foreground if dark is true
     */
    private void setColors(JComponent jc, boolean dark)
    {
	jc.setBackground(Color.black);
	if(dark)
	    jc.setForeground(darkFore);
	else
	    jc.setForeground(lightFore);
    }
    
    /** 
     * Add four fillers to a container, of size size, and return
     * a new panel representing the center
     */
    private JPanel addFillers(Container con, int size)
    {
	return addFillers(con, size, size);
    }

    /** 
     * Add four fillers to a container, of size sizeX by sizeY, and return
     * a new panel representing the center
     */
    private JPanel addFillers(Container con, int sizeX, int sizeY)
    {
	Dimension dFill = new Dimension(sizeX, sizeY);
	Box.Filler north = new Box.Filler(dFill, dFill, dFill);
	Box.Filler south = new Box.Filler(dFill, dFill, dFill);
	Box.Filler east = new Box.Filler(dFill, dFill, dFill);
	Box.Filler west = new Box.Filler(dFill, dFill, dFill);
	con.add(north, BorderLayout.NORTH);
	con.add(south, BorderLayout.SOUTH);
	con.add(east, BorderLayout.EAST);
	con.add(west, BorderLayout.WEST);
	JPanel newPanel = new JPanel(new BorderLayout());
	setColors(newPanel, true);
	con.add(newPanel, BorderLayout.CENTER);
	return newPanel;
    }

    private Speech getCurrentSpeaker()
    {
	double pitch = 1.0+((double)(pitchSlider.getValue()-50)/100.0);
	double volume = ((double)(volumeSlider.getValue()/100.0));
	int delay = delaySlider.getValue()*100;
	double delayMix = ((double)(delayMixSlider.getValue())/100.0);
	double delayFilter = ((double)(delayFilterSlider.getValue())/100.0);
	double filter = ((double)(filterSlider.getValue())/100.0);
	
	double flange = ((double)(flangeSlider.getValue())/100.0);
	double pan = ((double)(panSlider.getValue())/100.0);
	Speech speaker = new Speech(pitch, volume, delay, delayMix, flange, delayFilter, filter, pan);
	return speaker;
    }


    public Speech getFinalSpeaker()
    {
	return finalSpeaker;
    }

    private class CloseListener extends WindowAdapter
    {
	public void windowClosing(WindowEvent we)
	{
	    finalSpeaker = getCurrentSpeaker();
	}

    }
    
    /**
     * Construct the user interface
     */
    public Duotonic()
    {
	super();
	setModal(true);
	setTitle("Duotonic");
	setSize(700, 400);
	setBackground(Color.black);	
	//Add the controls
	Container gc = getContentPane();
	gc.setLayout(new BorderLayout());
	JPanel efx = makeEFXPanel();
	JPanel controlPanel = makeControlPanel();
	JPanel mainPanel = new JPanel(new BorderLayout());
	setColors(mainPanel, true);
	JPanel borderPanel = addFillers(mainPanel, 30);
	borderPanel.setBorder(new LineBorder(new Color(80,80,80)));
	JPanel cenPanel = addFillers(borderPanel, 30);
	
	JPanel sayPanel = makeSayPanel();
	
	cenPanel.add(efx, BorderLayout.EAST);
	cenPanel.add(controlPanel, BorderLayout.WEST);
	cenPanel.add(sayPanel, BorderLayout.SOUTH);
	addWindowListener(new CloseListener());

	gc.add(mainPanel);
	
	show();

    }

    public static void main(String args[])
    {
	Duotonic duo = new Duotonic();
	
    }

}
