package sigil;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * Class handling the bar displayed at the bottom of the SignalCanvas
 * @author John Williamson
 */

public class FlashBar
{
   

    //History, showing how many milliseconds missed
    //in the last few cycles
    private LinkedList performanceHistory;

    //Size of the history, in seconds
    private int histSize = 45;

    //Parent component
    private SignalCanvas parent;

    //Size of screen
    private Rectangle screenBounds;

    //Calculated size of the bar
    private Rectangle barBounds;

    //Shape of the bar
    private Polygon barShape;

    //Ratio of high segment to low segment ie where the slash goes
    private static final int heightRat = 5;

    //Steepness of the slash
    private static final double barSlashRatio = 0.25;

    //Colors and fonts
    private Color barColor = new Color(35,35,35,100);
    private Color outlineColor = new Color(190,190,190,100);
    private Color textColor = new Color(140,130,140);
    private Font textFont = new Font("SansSerif", Font.PLAIN, 12);
    private Font largeFont = new Font("SansSerif", Font.PLAIN, 12);
    
    //Is the bar drawn?
    private boolean visible = false;

    //Current fade values
    private int alpha = 60, minAlpha = 20, alphaInc = 20, maxAlpha= 100;

    //Slash positions
    private int slashHeight, slashWidth, slashPt;

    //Hashtable for the button name/rectangle pairs
    private Hashtable buttons;

    //The button names
    private String [] buttonNames = {"Save", "Load", "New"};



    private class PerformanceUpdate extends Thread
    {

	public void run()
	{
	    while(true)
		{

		    try{Thread.sleep(1000);} catch(InterruptedException ioe){}
		    performanceHistory.addLast(new Integer(MasterClock.getSecondPerformance()));
		    if(performanceHistory.size()>=histSize)
			performanceHistory.removeFirst();
		    parent.repaint();

		}

	}
    }
    
    
    /**
     * Make colors, taking the current fade values into account
     */
    private void makeColors()
    {
	if(SignalSystem.useAlpha)
	    {
		barColor = new Color(alpha,alpha,alpha,100);
		outlineColor = new Color(alpha*2,alpha*2,alpha*2,100);
	    }
	else
	    {
		barColor = new Color(alpha/2,alpha/2,alpha/2);
		outlineColor = new Color(alpha,alpha,alpha);
	    }
    }
    

    /**
     * Handle the mouse being released
     * checking each button to see if it was clicked
     */
    public void mouseReleased(int x, int y)
    {
	Enumeration rects = buttons.keys();
	while(rects.hasMoreElements())
	    {
		Rectangle testRect = (Rectangle)(rects.nextElement());
		if(testRect.contains(x,y))
		    {
			String action = buttons.get(testRect).toString();
			if(action.equals("Save"))
			    parent.save();
			if(action.equals("Load"))
			    parent.load();
                        if(action.equals("New"))
                            parent.newCanvas();

		    }
		
	    }
    }

    /**
     * Fade in and out as the mouse moves over
     */
    public void mouseMoved(int x, int y)
    {
	if(barBounds.contains(x,y))
	    {                     
		if(alpha<maxAlpha)
		    {
			visible = true;
			alpha+=alphaInc;
			makeColors();
			parent.repaint();
		    }
	    }
	else
	    {
		if(visible)
		    {
			alpha=minAlpha;
			makeColors();
			visible = false;     
			parent.repaint();
		    }
	    }
    }

    /**
     * Create a new flashbar, taking the screen bounds and the 
     * attached SignalCanvas 
     */
    public FlashBar(Rectangle sBounds, SignalCanvas parent)
    {
	this.parent = parent;
	screenBounds = sBounds;
	buttons = new Hashtable();
	int height = (sBounds.height)/(heightRat);
	barBounds = new Rectangle(sBounds.x, sBounds.y+sBounds.height-height,
				  sBounds.width, height);
	
	//Calculate the slash
	barShape = new Polygon();
	slashHeight = (int)(barBounds.height*barSlashRatio);
	slashPt = barBounds.width-(int)(barBounds.width/1.61803);
	slashWidth = (int)(barBounds.width/20.0);

	//Calculate the shape
	barShape.addPoint(barBounds.x, barBounds.y);
	barShape.addPoint(barBounds.x+slashPt, barBounds.y);
	barShape.addPoint(barBounds.x+slashPt+slashWidth, barBounds.y+slashHeight);
	
	barShape.addPoint(barBounds.x+barBounds.width, barBounds.y+slashHeight);
	barShape.addPoint(barBounds.x+barBounds.width, barBounds.y+barBounds.width);
	barShape.addPoint(barBounds.x, barBounds.y+barBounds.width);                                                 

	performanceHistory = new LinkedList();
	new PerformanceUpdate().start();
 }

    
    /**
     * Draw the information about the current configuration onto the bar
     * shows the filename, the last modification time, and the version number
     */
    private void drawText(Graphics g)
    {
	int xOff = 50;
	g.setColor(textColor);
	g.setFont(textFont);
	SignalHeader head = MasterClock.getHeader();
	g.drawString(MasterClock.getFilename(), slashPt+slashWidth+xOff, screenBounds.height-slashHeight-50);
	g.drawString("Last modified "+head.date, slashPt+slashWidth+xOff, screenBounds.height-slashHeight-10);
	g.drawString("Version "+head.verNo, slashPt+slashWidth+xOff, screenBounds.height-slashHeight-30);
    }

    /**
     * Draw the buttons onto the bar
     */
    private void drawButtons(Graphics g)
    {
	
	int xInset = 20;
	int yInset = 5;
	int xGap = 20;
	int buttonWidth = (slashPt-2*xInset)/buttonNames.length;
	buttonWidth -= xGap;
	int currentX = xInset;

	//For each button
	for(int i=0;i<buttonNames.length;i++)
	    {
		
		//Set the colors
		if(SignalSystem.useAlpha)
		    g.setColor(new Color(100,100,100,100));
		else
		    g.setColor(new Color(60,60,60));
		
		//Get the button bounds
		Rectangle butRect = new Rectangle(currentX,  (screenBounds.height-slashHeight*2)-yInset,
						  buttonWidth, (slashHeight-2*yInset));

		//Update the name/rect table
		buttons.put(butRect, buttonNames[i]);
		g.drawRect(butRect.x, butRect.y, butRect.width, butRect.height);
		
		if(SignalSystem.useAlpha)
		    g.setColor(new Color(0,0,0,100));
		else
		    g.setColor(new Color(20,20,20));
		
		//Draw the box and the button text
		g.fillRect(butRect.x, butRect.y, butRect.width, butRect.height);
		g.setColor(textColor);
		g.setFont(largeFont);
		int centrePoint = (buttonWidth-(g.getFontMetrics().stringWidth(buttonNames[i])))/2;
		g.drawString(buttonNames[i], butRect.x+centrePoint, butRect.y+18);
		
		currentX+=buttonWidth+xGap;
	    }
    }

    /**
     * Draw the buttons onto the bar
     */
    private void drawPerformance(Graphics g)
    {
        int xInset = (screenBounds.width*3)/4;
	int yInset = 25;
	int xGap = 20;
	int perfWidth = 180;
	int currentX = xInset;
	
	//Set the colors
	if(SignalSystem.useAlpha)
	    g.setColor(new Color(100,100,100,100));
	else
	    g.setColor(new Color(60,60,60));

	//Get the button bounds
	Rectangle perfRect = new Rectangle(currentX,  (screenBounds.height-slashHeight*2)-yInset,
					   perfWidth, 53);
	
	g.drawRect(perfRect.x, perfRect.y, perfRect.width, perfRect.height);
	
	if(SignalSystem.useAlpha)
	    g.setColor(new Color(0,0,0,100));
	else
	    g.setColor(new Color(20,20,20));
	
	//Draw the box and the button text
	g.fillRect(perfRect.x, perfRect.y, perfRect.width, perfRect.height);

	for(int i=0;i<performanceHistory.size();i++)
	    {
		int histVal = ((Integer)(performanceHistory.get(i))).intValue();
		renderPerfBar(g, currentX+1, perfRect.y+perfRect.height-1, 4, perfRect.height-1, histVal);
		currentX+=4;
	    }

	g.setColor(textColor);
        g.drawString("CPU Load", perfRect.x+56, perfRect.y+perfRect.height+13);
	    
    }

    /**
     * Render one bar of the performance history
     */
    private void renderPerfBar(Graphics g, int x, int y, int width, int maxHeight, int historyVal)
    {
	
	double logVal = Math.log(historyVal+1);

        logVal/=2;
	if(logVal>maxHeight)
	    logVal = maxHeight;

	double colInterp = logVal/(double)maxHeight;
       
	int red = (int)(colInterp*255.0);
	int green = (int)((1.0-colInterp)*255.0);
	Color barColor = new Color(red, green, 0);
	g.setColor(barColor);
	g.fillRect(x, y-(int)(logVal), 4, (int)(logVal));
    }

    
    /**
     * Paint the flashbar
     */
    public void paint(Graphics g)
    {
	if(!visible)
	    return;
	g.setColor(barColor);
	g.fillPolygon(barShape);
	g.setColor(outlineColor);
	g.drawPolygon(barShape);
	drawText(g);
	drawButtons(g);
	drawPerformance(g);
	   
    }



}
