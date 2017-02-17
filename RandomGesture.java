import java.util.*;
import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 *
 * @author John Williamson
 */
public class RandomGesture extends GeneratorModel
{
    
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
        return "RandomGesture";
    }
    public String getDescription()
    {
        return "Generates random gestures";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }

    private int states;

    private transient Vector resynthStates, resynthSymbols;
    private HMM hmm;
    int resynthPos =0;
    private boolean bounce = false;
    
    public int getSignalWidth()
    {
	return 0;
    }
    
    private class SliderListener implements ChangeListener
    {
	private String command;

	public SliderListener(String command)
	{
	    this.command = command;
	}

	public void stateChanged(ChangeEvent ce)
	{
	    JSlider slider = (JSlider)(ce.getSource());
	    if(command.equals("States"))
		{
		    if(!slider.getValueIsAdjusting())
			{
			    int confirm = JOptionPane.showConfirmDialog(null, "This will invalidate current recognition. Continue?", "Invalidate recognition", JOptionPane.YES_NO_OPTION);
			    if(confirm == JOptionPane.YES_OPTION)
				{
				    states = slider.getValue();		    
                                    createModel(27, 3, states);
				}
			}
		}
	}
  }


    
    private void resynthesizeGesture()
    {
	resynthPos = 0;
	resynthStates = new Vector();
	resynthSymbols = new Vector();
	hmm.simulateSequence(500, 
			     resynthStates, resynthSymbols);
    }


    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.setSize(460,460);
	jf.setTitle(getName());

	Box sliderPanel = Box.createVerticalBox();
	
	
	JSlider stateSlider = new JSlider(2, 100, states);
	JPanel statePanel = UIUtils.nameSliderLabelled(stateSlider, 
						       "States per model", true);
	sliderPanel.add(statePanel);
	stateSlider.addChangeListener(new SliderListener("States"));
	Container gc = jf.getContentPane();
        gc.add(sliderPanel, BorderLayout.SOUTH);
	UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
	jf.show();


    }

  

    public void tock()
    {

        if(resynthSymbols!=null && resynthPos<resynthSymbols.size())
	    {
		int symbol = ((Integer)(resynthSymbols.get(resynthPos))).intValue();
		StateInformation sInfo = new StateInformation();
		sInfo.setTransition(symbol, 3, 27);
	
		distributeHetObject(sInfo);
		resynthPos++;
	    }
	else
	    resynthesizeGesture();
    }

	
    private void createModel(int alphabet, int divisions, int states)
    {
        hmm = new HMM(states, alphabet);
        this.states = states;
    }
	
        public RandomGesture()
	{
	    states = 14;
	    createModel(27,3,states);
	}
	
    
}
