import sigil.*;
import duotonic.*;
import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.*;

public class OrbitSound extends SProcessorModel
{
    static final long serialVersionUID = 213L;
    private double phaseAngle, phaseShift = 0;
    private Vector phaseSoundMarkers;
    private transient JPanel orbitPanel;

    private int clipAngle(double angle)
    {
	while(angle<0)
	    angle+=360;
	return (int)(angle%360);
    }

    public void showInterface()
    {
	JFrame jf = new JFrame(getName());
	jf.setSize(400,400);
	orbitPanel = new OrbitPanel();
	jf.getContentPane().add(orbitPanel);
	jf.show();
    }

    private class OrbitPanel extends JPanel
    {
    public void paint(Graphics g)
	{
	    Dimension dSize = getSize();
	    g.setColor(Color.black);
	    g.fillRect(0,0,dSize.width, dSize.height);

	    g.setColor(Color.white);
	    g.fillOval(19,19,dSize.width-40, dSize.height-40);
	    g.setColor(new Color(80,80,80));
	    g.fillOval(20,20,dSize.width-40, dSize.height-40);

	    g.setColor(Color.darkGray);
	    g.fillOval(40,40,dSize.width-80, dSize.height-80);
	    g.setColor(Color.darkGray);
	    g.drawOval(41,41,dSize.width-80, dSize.height-80);

	    g.setColor(Color.gray);
	    g.drawOval(40,40,dSize.width-80, dSize.height-80);
	    int cx = dSize.width/2;
	    int cy = dSize.height/2;
	    int dx = (int)(Math.sin(phaseAngle/180.0*Math.PI)*(cx-40));
	    int dy = (int)(Math.cos(phaseAngle/180.0*Math.PI)*(cy-40));
	    
	    g.setColor(new Color(100, 10, 10));
	    g.drawLine(cx+1, cy+1, cx+dx+1, cy+dy+1);
	    g.setColor(Color.red);
	    g.drawLine(cx, cy, cx+dx, cy+dy);

	    for(int i=0;i<phaseSoundMarkers.size();i++)
		{
		    PhaseMarker pm = (PhaseMarker) (phaseSoundMarkers.get(i));
		    int marX = (int)(Math.sin(pm.getPhase()/180.0*Math.PI)*(cx-30));
		    int marY = (int)(Math.cos(pm.getPhase()/180.0*Math.PI)*(cy-30));
		    g.setColor(Color.white);
		    g.fillOval(cx+marX-5, cy+marY-5, 8, 8);
		    g.setColor(Color.black);
		    g.fillOval(cx+marX-3, cy+marY-3, 8, 8);
		    g.setColor(Color.darkGray);
		    g.fillOval(cx+marX-4, cy+marY-4, 8, 8);
		}
	}
}
    private class PhaseSound implements Serializable
    {
	public void play()
	{
	    MIDIUtils.playNote(0, (int)(40+(clipAngle(phaseAngle))/20), 127, 100, 0);
	}
    }

    private class PhaseMarker implements Serializable
    {
	static final long serialVersionUID = 213L;
	private boolean wasBefore = true;
	private double phase;
	private PhaseSound sound;
	
	public PhaseMarker(double phase, PhaseSound sound)
	{
	    this.phase = phase;
	    this.sound = sound;
	}
	
	public double getPhase()
	{
	    return phase;
	}

	public void checkPassed(double testPhase)
	{
	    int tPhase = clipAngle(testPhase);
	    if(tPhase>phase && wasBefore)
		{
		    sound.play();
		    wasBefore = false;
		}
	    else if(tPhase<phase)
		wasBefore = true;
	}
    }

    public String getGenName()
    {
	return "OrbitSound";
    }

    public String getDescription()
    {
	return "Produces sounds at various points around a circular orbit";
    }

    public String getDate()
    {
	return "July 2002";
    }

    public String getAuthor()
    {
	return "John Williamson";
    }
 
    public void connectionChange()
    {
    }

    public OrbitSound()
    {
	super();
	phaseSoundMarkers = new Vector();
	for(int i=0;i<360;i+=45)
	    phaseSoundMarkers.add(new PhaseMarker(i, new PhaseSound()));
    }


    private void checkAngle()
    {
	if(orbitPanel!=null)
	    orbitPanel.repaint();
	
	testSound(phaseAngle + phaseShift);
    }

    private void testSound(double angle)
    {
	Iterator iter = phaseSoundMarkers.iterator();
	while(iter.hasNext())
	    {
		PhaseMarker phaseMark = (PhaseMarker)(iter.next());
		phaseMark.checkPassed(angle);
	    }
    }


    public void processSignal()
    {
	GestureSignal sig = lastSig;	
	if(sig.vals.length==sigWidth && sigWidth>0)
	    {
		double theta = sig.vals[0];
		phaseAngle = theta;
		checkAngle();
	    }   
    }
    
}
