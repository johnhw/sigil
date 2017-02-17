package duotonic;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PositionDemo
{
    private int posX, posY;
    private int xSize=350, ySize=350;
    private double yFactor, xFactor;
    private JTextField toSay;
    private Speech speaker;


    private class PositionWindow extends JPanel
    {	
	private class ClickListen extends MouseAdapter
	{
	    public void mouseReleased(MouseEvent me)
	    {
		posX = me.getX();
		posY = me.getY();
		say();
		repaint();
	    }
	}

	public PositionWindow()
	{
	    super();
	    addMouseListener(new ClickListen());
	}

	public void paint(Graphics g)
	{
	    Dimension dSize = getSize();
	    yFactor = 1.0/dSize.height;
	    xFactor = 1.0/dSize.width;
	    g.setColor(Color.black);
	    g.fillRect(0,0,dSize.width, dSize.height);
	    g.setColor(Color.red);
	    g.fillOval(posX-4, posY-4, 8, 8);
	}
    }


    public PositionDemo()
    {

	JFrame jf = new JFrame();
	jf.setSize(xSize, ySize+30);
	Container gc = jf.getContentPane();
	JButton sayButton = new JButton("Say");
	toSay = new JTextField(20);
	JPanel sayPanel = new JPanel(new FlowLayout());

	
	sayPanel.add(toSay);

	gc.add(sayPanel, BorderLayout.NORTH);
	gc.add(new PositionWindow(), BorderLayout.CENTER);
	jf.setTitle("Position demo");
	jf.show();
    }

    private void say()
    {
	speaker = new Speech();
	speaker.setDistance(posY*yFactor+1.0);
	speaker.setPan(posX*xFactor);
	speaker.textToSpeech(toSay.getText(), false);
    }
    

    public static void main(String args[])
    {
	PositionDemo pDemo = new PositionDemo();
    }
	

}
