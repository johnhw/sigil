import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Shows incoming probabilistic gestures in an attractive manner
 *
 * @author John Williamson
 */
public class GestureDisplay extends SProcessorModel
{
    private DisplayPanel displayPanel;
    
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "GestureDisplay";
    }
    public String getDescription()
    {
	return "Shows incoming probabilistic gestures in an attractive manner";
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
    }
    
    public void processHetObject(Object o)
    {
	if(o instanceof ProbabilisticGesture && displayPanel!=null)
	    {
		ProbabilisticGesture pGest = (ProbabilisticGesture) o;
		
		ParameterizedGesture mostProbable = pGest.getMostProbable();
		if(mostProbable!=null)
		    {
			displayPanel.newGesture(mostProbable);
		    }
	    }
    }

    private class DisplayPanel extends JPanel
    {
	private double magnitude;
	private int curFade;
	private int fadeScale = 3;
	private double probability;
	private double duration;
	private String probName="", durName="";
	private String [] probabilityNames = {"Possibly", "Maybe", "Likely to be", 
					      "Very likely to be", "Definitely"};
	private double [] probabilityLevels = {0.0, 0.4, 0.65, 0.8, 0.95};
	private String [] durationNames = {"very fast", "fast", "medium fast", "slowly", "very slowly"};
	private double [] durationTimes = {0.8, 1.2, 1.6, 1.8, 2.5};
	private String name="";

	private class FadeThread extends Thread
	{
	    public void run()
	    {
		while(curFade>0)
		    {
			try{Thread.sleep(45);}catch(InterruptedException ie){}
			curFade--;
			repaint();
		    }
	    }

	}

	public void newGesture(ParameterizedGesture mostProbable)
	{
	    probability = mostProbable.getProbability();
	    duration = (mostProbable.getDuration()/1000.0);
	    name = mostProbable.getName();
	    int i=0;
	    while(i<probabilityNames.length && probability>probabilityLevels[i])
		i++;

	    if(i==probabilityNames.length)
		i--;
	    probName = probabilityNames[i]+" ("+probability*100+"%)";
	    i=0;
	    while(i<durationNames.length && duration>durationTimes[i])
		i++;
	    
	    if(i==durationNames.length)
		i--;
	    durName = durationNames[i]+" ("+duration+"s)";
	    
	    magnitude = mostProbable.getAverageScale();
	    
	    curFade = 80;
	    new FadeThread().start();
	}

	public void paint(Graphics g)
	{
	    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					     RenderingHints.VALUE_ANTIALIAS_ON);
	    Dimension dSize = getSize();
	    int colorVal = curFade*fadeScale;
	    if(colorVal>255)
		colorVal=255;
	    if(colorVal<0)
		colorVal = 0;
	    g.setColor(new Color(colorVal, colorVal, colorVal));
	    g.fillRect(0,0, dSize.width, dSize.height);
	    g.setColor(Color.black);
	    g.drawLine(0, 50, 200, 50);
	    g.drawLine(dSize.width-200, dSize.height-50, dSize.width, dSize.height-50);
	    g.setFont(new Font("SansSerif", Font.PLAIN, 16));
	    g.drawString(probName, 10, 44);
	    g.drawString(durName, 90, dSize.height-35);
	    if(magnitude!=0.0)
		g.drawString(""+magnitude, 90, dSize.height-65);
	    g.setFont(new Font("SansSerif", Font.BOLD, 64));
	    FontMetrics fMet = g.getFontMetrics();
	    int width = fMet.stringWidth(name);
	    int height = fMet.getHeight()+fMet.getAscent()+fMet.getDescent();
	    int xOff = (dSize.width-width)/2;
	    int yOff = (dSize.height-height)/2;
	    g.drawString(name, xOff, yOff+height/2);
	
	}


    }


    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.setSize(300,400);
	jf.setTitle(getName());
	displayPanel = new DisplayPanel();
	jf.getContentPane().add(displayPanel);
	jf.show();
    }

    
    
    public GestureDisplay()
    {
	super();
    }

}





