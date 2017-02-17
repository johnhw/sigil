package sigil;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * Interface for manipulating objects that support genetic algorithms
 *
 * @author John Williamson
 */

public class GeneticInterface extends JPanel
{
    //Size of the display
    private int displayWidth, displayHeight, inset;

    //The current artwork element
    private MutableArtwork mArt;

    //The list of current GAElements (i.e the parent)
    private Vector curVec;

    //The list of artworks for this display
    private Vector mutableArts;

    //The attached canvas
    private SignalCanvas attachedCanvas;

    //The current mutation factor
    private double mutationFactor;


    //The size of the table. Actual table is tableSize*tableSize
    private int tableSize;

    //Hotspots for clicking on each subdivision or label
    private Vector barHotspots;
    private Vector subdivHotspots;
    private Vector labelHotspots;

    //List of lists of GAElements, one for each section in the table
    private Vector [] elementCopies;

    //Size of this window
    private Dimension dSize;

    //The intial set of GAElements
    private Vector elements;
    
    //True if a drag is in progress
    private boolean dragging = false;

    //Current and target drag element indices
    private int curDragElt=-1;
    private int curTargetElt=-1;

    //Current drag position
    private int dragX, dragY;
    
    private class DragListen extends MouseMotionAdapter
    {
	
	public void mouseDragged(MouseEvent me)
	{
	    int x = me.getX();
	    int y = me.getY();
	    if(x>displayWidth && y>inset && y<(dSize.height-inset))
		{
		    int upsideY = dSize.height-y-inset;
		    mutationFactor = (double)upsideY/(double)(dSize.height-2*inset);
		    makeArts(mArt);
		    makeCopies(curVec);
		}
	    
	    for(int i=0;i<subdivHotspots.size();i++)
		{
		    Rectangle check = (Rectangle)(subdivHotspots.get(i));
		    if(check.contains(x,y))
			{
			    curTargetElt = i;
			    break;
			}
		}
	    
	    if(!dragging)
		{
		    for(int i=0;i<subdivHotspots.size();i++)
			{
			    Rectangle check = (Rectangle)(subdivHotspots.get(i));
			    if(check.contains(x,y))
				{
				    curDragElt = i;
				    dragging = true;
				    break;
				}
			}
		}
	    
	    dragX = x;
	    dragY = y;
	    repaint();
	}
	
    }
    
    private class ClickListen extends MouseAdapter
    {
	
	public void mouseReleased(MouseEvent me)
	{
	    int x = me.getX();
	    int y = me.getY();
            if(MouseUtils.left(me))
		{
		    if(dragging)
			{
			    dragging = false;
                            breed();
			    curDragElt = -1;
			    repaint();
			}
		    else
			{
			    for(int i=0;i<barHotspots.size();i++)
				{
				    Rectangle check = (Rectangle)(barHotspots.get(i));
				    if(check.contains(x,y))
					{
					    changeSize(i+2);
					    return;
					}
				}
			    
			    for(int i=0;i<subdivHotspots.size();i++)
				{
				    Rectangle check = (Rectangle)(subdivHotspots.get(i));
				    if(check.contains(x,y))
					{
					    mArt =   (MutableArtwork)(mutableArts.get(i));
					    makeArts(mArt);
					    curVec = elementCopies[i];
					    makeCopies(curVec);
                                            replaceOriginals(curVec);
					    return;
					}
				}
			}
		}
	    else
		{
		    for(int i=0;i<subdivHotspots.size();i++)
			{
			    Rectangle check = (Rectangle)(subdivHotspots.get(i));
			    if(check.contains(x,y))
				{
				    GAElement gaElt = (GAElement)(elementCopies[i].get(0));
				    gaElt.preview();
				}
			}
		}
	}
    }
    
    
    private void breed()
    {
       Vector parentsA = elementCopies[curTargetElt];
       Vector parentsB = elementCopies[curDragElt];
       for(int i=0;i<parentsA.size();i++)
       {
         GAElement parentA = (GAElement)(parentsA.get(i));
         GAElement parentB = (GAElement)(parentsB.get(i));
         parentA.breed(parentB);
       }
      curVec = elementCopies[curTargetElt];
      makeCopies(curVec);
      replaceOriginals(curVec);
    }

    public GeneticInterface(SignalCanvas sigCan, Vector currentSelection)
    {	
	attachedCanvas = sigCan;
	mutationFactor = 0.1;
	mArt = new MutableArtwork(this);
	addMouseListener(new ClickListen());
	addMouseMotionListener(new DragListen());
	stripNonMutable(currentSelection);
	curVec = elements;
	changeSize(2);
    }
    
    private void stripNonMutable(Vector elts)
    {
	elements = new Vector();

	for(int i=0;i<elts.size();i++)
	    {
		VisualElement elt = (VisualElement)(elts.get(i));
		if(elt!=null)
		    {
			GestureGenerator thisElt = elt.getElement().getElement();
			if(thisElt instanceof GAElement)
			    {
				elements.add(thisElt);
			    }
		    }
	    }
    }
    
    
    public void makeCopies(Vector newVec)
    {
	elementCopies = new Vector[tableSize*tableSize];
	elementCopies[0] = newVec;
	for(int i=1;i<tableSize*tableSize;i++)
	    {
		elementCopies[i] = new Vector();
		for(int j=0;j<elements.size();j++)
		    {
			GAElement gaElt = (GAElement)(newVec.get(j));
			GAElement newElt = gaElt.copy();
			newElt.mutate(mutationFactor);
			elementCopies[i].add(newElt);
		    }
	    }
    }

    private void replaceOriginals(Vector replacements)
    {
     for(int i=0;i<elements.size();i++)
	 {
	     GAElement replaceElt = (GAElement)(replacements.get(i));
	     GAElement toReplace = (GAElement)(elements.get(i));
	     toReplace.copyFrom(replaceElt);
	 }

    }
    
    private void makeArts(MutableArtwork orig)
    {
	mutableArts = new Vector();
	mutableArts.add(orig);
	for(int i=1;i<tableSize*tableSize;i++)
	    {
		MutableArtwork newArt = orig.doClone();
		newArt.mutate(mutationFactor);
		mutableArts.add(newArt);
	    }
	repaint();
    }
    
    private void changeSize(int newSize)
    {
	tableSize = newSize;
	makeArts(mArt);
	makeCopies(curVec);
    }
    
    public void paint(Graphics g)
    {
	dSize = getSize();
	barHotspots = new Vector();
	subdivHotspots = new Vector();
	
	g.setColor(Color.black);
	g.fillRect(0,0,dSize.width,dSize.height);
	int barSize = 60;
	inset = 20;
	
	displayWidth = dSize.width-60;
	displayHeight = dSize.height-barSize-3*inset;
	int squareWidth = displayWidth/tableSize;
	int squareHeight = displayHeight/tableSize;
	
	int curY = inset;
	int barX = inset;
	int barWidth = displayWidth/4;

        //Draw the mutation factor slider bar
	int pos = dSize.height-(int)(mutationFactor*(dSize.height-inset*2));
        g.setColor(new Color(80,80,80));
        g.fillRect(displayWidth+inset*2-10, pos-5-inset, 20, (dSize.height-pos)-5);
        g.setColor(new Color(180,180,180));
        g.drawLine(displayWidth+inset*2-10, pos-5-inset, displayWidth+inset*2+10, pos-5-inset);

	for(int i=0;i<4;i++)
	    {
                g.setColor(new Color(40,40,80));
		Rectangle barRect = new Rectangle(barX,displayHeight+2*inset, barSize, barSize);
		g.fillRect(barRect.x, barRect.y, barRect.width, barRect.height);
		barHotspots.add(barRect);        
		int nDivs = i+2;
                g.setColor(new Color(80,80,160));
		for(int j=0;j<nDivs+1;j++)
		    {
			g.drawLine(barX, displayHeight+2*inset+j*(barSize/nDivs),
				   barX+barSize,
				   displayHeight+2*inset+j*(barSize/nDivs));
		    }
		for(int k=0;k<nDivs+1;k++)
		    {
			g.drawLine(barX+k*(barSize/nDivs), displayHeight+2*inset, 
				   barX+k*(barSize/nDivs),
				   displayHeight+2*inset+barSize);
		    }
		barX+=barWidth;
	    }
	int count = 0;
	for(int i=0;i<tableSize;i++)
	    {
		int curX = inset;
		for(int j=0;j<tableSize;j++)
		    {
			if(dragging && curTargetElt==count)
			    g.setColor(new Color(200,80,80));
			else
			    g.setColor(new Color(80,80,80));
			Rectangle subdivRect = new Rectangle(curX, curY, squareWidth, squareHeight);
			subdivHotspots.add(subdivRect);
			g.drawRect(curX, curY, squareWidth, squareHeight);
			MutableArtwork mArt = (MutableArtwork)(mutableArts.get(count++));
			mArt.paint(g, curX+2, curY+2, squareWidth-4, squareHeight-4);
			curX+=squareWidth;        
		    }
		curY+=squareHeight;
	    }

	if(dragging)
	    {
		g.setColor(new Color(50,50,100,100));
		g.fillRect(dragX-squareWidth/2, dragY-squareHeight/2, squareWidth, squareHeight);
	    }
	
    }
    
}
