package sigil;
import java.util.*;
import java.awt.*;

public class Group extends VisualElement
{
    private Vector internalElements;
    private boolean expanded;
    private boolean breakAtExpansion = false;
    transient private Component parent;
    private static final Color groupColor = new Color(170, 50, 50);

    public Group(String name, int x, int y, Component parent)
    {
	super(name, x, y);
	bounds.height=40;
	internalElements = new Vector();
	expanded = true;
	this.parent = parent;
    }

    public Group getExpandedParent()
    {
	if(!inGroup || (inGroup && parentGroup.expanded))
	    return this;
	else
	    return parentGroup.getExpandedParent();
    }
    
    public void setParent(Component parent)
    {
	this.parent = parent;
    }
    
    public void connected(VisualElement ve)
    {

    }
    
    public void paint(Graphics g)
    {
	if(!expanded)
	    {
		g.setColor(groupColor);
		super.paint(g);
	    }
    }


    private class ExpandThread extends Thread
    {
	public void run()
	{
	    boolean allStopped = false;
	    Point target;
	    boolean wasExpanded = expanded;

	    for(int i=0;i<internalElements.size();i++)
		{
		    VisualElement vElt = (VisualElement)(internalElements.get(i));
		    if(expanded)
			{
			    target = new Point(bounds.x, bounds.y);
			    
			}
		    else
			{
			    vElt.move(bounds.x, bounds.y);
			    target = new Point(bounds.x+(int)(vElt.deltaPosition.x),
					       bounds.y+(int)(vElt.deltaPosition.y));
			}
		    vElt.startMoving(target);
		}    
	    if(!wasExpanded)
		expanded=true;

	    while(!allStopped)
		{
		    allStopped = true;
		    for(int i=0;i<internalElements.size();i++)
			{
			    VisualElement vElt = (VisualElement)(internalElements.get(i));
			    vElt.moveOneStep(5.0);
			    allStopped = allStopped && vElt.stoppingNow;
			}
		    if(!allStopped)
			try{Thread.sleep(40);} catch(InterruptedException ie){}
		    parent.repaint();
		}
	    if(wasExpanded)
		expanded = false;


	    for(int i=0;i<internalElements.size();i++)
		{
		    VisualElement vElt = (VisualElement)(internalElements.get(i));
		    vElt.stoppingNow = false;
		    vElt.moving = false;
		}    

	    
	    if(breakAtExpansion)
		{
		    while(internalElements.size()>0)
			{
			    VisualElement vElt = (VisualElement)(internalElements.get(0));
			    vElt.removeFromGroup();
			}
		    breakAtExpansion = false;
		}
	    parent.repaint();
	}
    }
	

    public void expand()
    {
	if(!expanded)
	    new ExpandThread().start();

    }

    
    public void contract()
    {
	if(expanded)
	    new ExpandThread().start();

    }

    public void paintConnections(Graphics g)
    {

    }
    
    public void breakApart()
    {
	breakAtExpansion = true;
	expand();
	

    }

    public boolean isOver(int x, int y)
    {
	return !expanded && super.isOver(x,y);

    }
    
    public void deleteConnected(int x, int y)
    {

    }
    
    public void paintDrag(Graphics g, Point dragPt)
    {
	
    }

    public boolean connect(VisualElement ve)
    {
	return false;
    }

    public Vector getInternalElements()
    {
	return internalElements;
    }

    public void disconnectSelf()
    {
	

    }

    public GestureElement getElement()
    {
	return null;
    }

    public boolean checkTopology(VisualElement ve, Group thisGroup)
    {
	if(ve==this)
	    return false;
	return true;
    }

    public boolean add(VisualElement ve)
    {
	if(checkTopology(ve, this) && !ve.inGroup)
	    {
		internalElements.add(ve);
		return true;
	    }
	return false;
    }

    public boolean isExpanded()
    {
	return expanded;

    }

    public void remove(VisualElement ve)
    {
	internalElements.remove(ve);
    }




}
