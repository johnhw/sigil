package sigil;
import java.awt.*;
import javax.swing.*;


/**
 * About box display, shows an image fading in on a JPanel
 *
 * @author John Williamson
 */

public class AboutBox extends JPanel
{
    private ImageIcon splash; //The splash image
    private int fade;         //Fade amount and position in title
    private static String title = "SIGIL"; 

    /**
     * Construct the box and start a thread to animate it
     */
  public AboutBox()
  {
      
      //Load the splash image
      splash = new ImageIcon(Library.getURL("sigil.jpg"));
      
      fade = 255;
      new Thread()
	  {
	      public void run()
	      {
		  repaint();
		  try{Thread.sleep(200);} catch(Exception e){}
		  
		  //Change the fade
                  while(fade>8)
		      {
                          fade -= 8;
                          try{Thread.sleep(10);} catch(Exception e){}
			  repaint();
		      }
	      }
	  }.start();

  }
   
    /**
     * Paint the about box 
     */
    public void paint(Graphics g)
    {
	((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					 RenderingHints.VALUE_ANTIALIAS_ON);
	
	Dimension dSize = getSize();

	//Clear screen
	g.setColor(Color.black);
	g.fillRect(0,0,dSize.width, dSize.height);
	
	g.drawImage(splash.getImage(),0,0,this); //Paint image
	g.setColor(new Color(0,0,0,fade));
	g.fillRect(0,0,dSize.width, dSize.height); //Fade image
	
  }

}
