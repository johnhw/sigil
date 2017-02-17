import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Displays the output of a MarkovModel, using
 * a moving histogram for the various gesture probabilities
 *
 * @author John Williamson
 */
public class ContinuousProbability extends SProcessorModel
{
    private transient DisplayPanel displayPanel;
 
    private Vector probabilities;
    private Vector names;
    private int nGests;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "ContinuousProbability";
    }
    public String getDescription()
    {
	return "Displays MarkovModel output using a moving histogram"+
	    " for the various gesture probabilities";
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

    public int getSignalWidth()
    {
	return nGests;
    }
    
    public void processHetObject(Object o)
    {
	if(o instanceof StateInformation && displayPanel!=null)
	    {
		StateInformation sInfo = (StateInformation)o;
		if(sInfo.getBoolean("Adjusted probabilities"))
		    {
			newProbabilities((Hashtable)(sInfo.getObject("Probabilities")));
			if(displayPanel!=null)
			    displayPanel.repaint();
		    }
	    }
    }
    
	public void newProbabilities(Hashtable probTable)
	{
	    probabilities = new Vector();
	    names = new Vector();	    
	    Enumeration enum = probTable.keys();
	    while(enum.hasMoreElements())
		{
		    String name = (String)(enum.nextElement());
		    Double probability = (Double)(probTable.get(name));
		    names.add(name);
		    probabilities.add(probability);
       		}
	    if(names.size()!=nGests)
		{
		    nGests = names.size();
		    propogateWidthChanges();
		}
	    double [] sigVals = new double[probabilities.size()];
	    for(int i=0;i<sigVals.length;i++)
		sigVals[i] = ((Double)(probabilities.get(i))).doubleValue()*100.0;
	    setCurSig(new GestureSignal(sigVals, getID()));
	}

    private class DisplayPanel extends JPanel
    {
	private double magnitude;
	
	private boolean use3D;

	public DisplayPanel()
	{
	    super();
	    addMouseListener(new ClickListener());
	}


	private class ClickListener extends MouseAdapter
	{
	    public void mouseClicked(MouseEvent me)
	    {
		if(MouseUtils.dblClick(me))
		    {
			use3D = !use3D;
			repaint();
		    }
	    }

	}
	

	  

	private void drawHistogram(Graphics g, Dimension dSize)
	{
	    g.setColor(Color.lightGray);
	    g.setFont(new Font("SansSerif", Font.PLAIN, 12));
	    g.drawString("Histogram view", 10, 20);
	    	    int divSize = dSize.width/nGests;
		    g.setFont(new Font("SansSerif", Font.BOLD, 32));
		    FontMetrics fMet = g.getFontMetrics();
		    int height = fMet.getHeight()+fMet.getAscent()+fMet.getDescent();
		    int currentX = 0;
		    for(int i=0;i<nGests;i++)
			{
			    String name = (String)(names.get(i));
			    double prob = ((Double)(probabilities.get(i))).doubleValue();
			    int width = fMet.stringWidth(name);
			    
			    int barHeight = (int)(dSize.height*prob);
			    int xOff = (divSize-width)/2;
			    int yOff = (barHeight-height)/2;
			    
			    int cVal = (int)(255*prob);
			    if(cVal>255)
				cVal=255;
			    if(cVal<0)
				cVal =0;
			    g.setColor(new Color(cVal, 0, 255));
			    g.fillRect(currentX, dSize.height-barHeight, divSize, barHeight);
			    g.setColor(Color.white);
			    g.drawString(name, currentX+xOff, dSize.height-40);
			    currentX+=divSize;
			}
	}

	
	private void draw3D(Graphics g, Dimension dSize)
	{
	    g.setColor(Color.lightGray);
	    g.setFont(new Font("SansSerif", Font.PLAIN, 12));
	    g.drawString("3D view", 10, 20);
	    int divSize = dSize.width/nGests;
	    for(int i=0;i<nGests;i++)
		{
		    String name = (String)(names.get(i));
		    double prob = 1.0-(((Double)(probabilities.get(i))).doubleValue());
		    Point3D p3D = new Point3D(divSize*i-(divSize*nGests)/2, 0, prob*100);
		    Point drawPt = Utils3D.project(p3D, dSize.width, 500);
		    double size = 100/((prob*4)+1);
		    int colVal = (int)(size*2);
		    g.setFont(new Font("SansSerif", Font.PLAIN, (int)size));
		    g.setColor(new Color(colVal/2, colVal/2, colVal/2));		    
		    g.drawString(name, drawPt.x+1, drawPt.y+1);
		    g.setColor(new Color(colVal, colVal, colVal));		    
		    g.drawString(name, drawPt.x, drawPt.y);
		}
	}


	public void paint(Graphics g)
	{
	    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					     RenderingHints.VALUE_ANTIALIAS_ON);
	    Dimension dSize = getSize();
	   
	    g.setColor(Color.black);
	    g.fillRect(0,0, dSize.width, dSize.height);	    
	    
	    if(nGests>0)
		{
		    if(!use3D)
			drawHistogram(g, dSize);
		    else
			draw3D(g, dSize);
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

    
    
    public ContinuousProbability()
    {
	super();
    }

}





