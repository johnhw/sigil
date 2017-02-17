import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Shows incoming probabilistic gestures, using
 * a histogram for the various gesture probabilities
 *
 * @author John Williamson
 */
public class GestureHistogram extends SProcessorModel
{
    private transient DisplayPanel displayPanel;
    
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "GestureDisplay";
    }
    public String getDescription()
    {
	return "Shows incoming probabilistic gestures, using a histogram for the various gesture probabilities";
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
		if(pGest!=null)
		    displayPanel.newGesture(pGest);
	    }
    }

    private class DisplayPanel extends JPanel
    {
	private double magnitude;
	private int curFade;
	private int fadeScale = 3;
	private double [] probabilities;
	private String [] names;
	private int nGests;
	
	private class FadeThread extends Thread
	{
	    public void run()
	    {
		while(curFade>0)
		    {
			try{Thread.sleep(25);}catch(InterruptedException ie){}
			curFade--;
			repaint();
		    }
	    }

	}

	public void newGesture(ProbabilisticGesture pGest)
	{
	    Vector gestureList = pGest.getGestureList();
	    nGests = gestureList.size();
	    probabilities = new double[nGests];
	    names = new String[nGests];
	    
	    for(int i=0;i<nGests;i++)
		{
		    ParameterizedGesture parmGest = (ParameterizedGesture)(gestureList.get(i));
		    names[i] = parmGest.getName();
		    probabilities[i] = parmGest.getProbability();
		    System.out.println(names[i]+" p="+probabilities[i]);
		}

	    curFade = 80;
	    new FadeThread().start();
	}

	public void paint(Graphics g)
	{
	    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					     RenderingHints.VALUE_ANTIALIAS_ON);
	    Dimension dSize = getSize();
	   
	    int colorVal = curFade*fadeScale;
	   

	    g.setColor(Color.black);
	    g.fillRect(0,0, dSize.width, dSize.height);	    
	    
	    if(nGests>0)
		{
		    int divSize = dSize.width/nGests;
		    g.setFont(new Font("SansSerif", Font.BOLD, 40));
		    FontMetrics fMet = g.getFontMetrics();
		    int height = fMet.getHeight()+fMet.getAscent()+fMet.getDescent();
		    int currentX = 0;
		    for(int i=0;i<nGests;i++)
			{
			    int width = fMet.stringWidth(names[i]);
			    
			    int barHeight = (int)(dSize.height*probabilities[i]);
			    int xOff = (divSize-width)/2;
			    int yOff = (barHeight-height)/2;

			    int cVal = (int)(colorVal*probabilities[i]);
			    if(cVal>255)
				cVal=255;
			    if(cVal<0)
				cVal =0;
			    g.setColor(new Color(cVal, cVal, cVal));
			    g.fillRect(currentX, dSize.height-barHeight, divSize, barHeight);
			    g.setColor(Color.black);
			    g.drawString(names[i], currentX+xOff, dSize.height-40);
			    currentX+=divSize;
			}
		}
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

    
    
    public GestureHistogram()
    {
	super();
    }

}





