import java.io.*;
import sigil.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

public class ClickPad extends GeneratorModel 
{
    private double down;
    private transient JFrame jf;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "ClickPad";
    }
    public String getDescription()
    {
	return "Produces pulses when mouse button is clicked";
    }
    public String getDate()
    {
	return "August 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }

    
    public int getSignalWidth()
    {
	return 1;
    }
    

    
    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, re-creating histoty information
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
    }

    public void tock()
    {
	double [] sig = new double[1];
	sig[0]=down;
	GestureSignal gs = new GestureSignal(sig,getID());
	setCurrentSignal(gs);
	down = 100;
    }


    private class ClickListener extends MouseAdapter
    {
	
	public void mousePressed(MouseEvent me)
	{
	    down = -100;
	}
	
    }


    public ClickPad()
    {
	super();

	
    }
    
    public void showInterface()
    {
	jf = new JFrame(getName());
	jf.setSize(400,400);
	jf.getContentPane().setBackground(Color.black);
	jf.addMouseListener(new ClickListener());
	jf.setTitle(getName());
	jf.show();
  
 }


}
