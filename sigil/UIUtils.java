package sigil;
import java.awt.*;
import javax.swing.*;
import java.text.*;
import javax.swing.event.*;

/**
 * Utility functions for updating user interface components
 * 
 * @author John Williamson
 */
public class UIUtils
{
    //Output format for slider values
    private static NumberFormat numForm = new DecimalFormat("0.00");

    /**
     * Take a component and set and all its children to have
     * the specified fore and background colors
     *
     */
    public static void setColors(Component com, Color fore, Color back)
    {
	if(!(com instanceof JButton))
	    {
		com.setForeground(fore);
		com.setBackground(back);
		
		//Recurse through children
		if(com instanceof Container)
		    {
			Container contain = (Container)(com);
			Component [] children = contain.getComponents();
			for(int i=0;i<children.length;i++)
			    setColors(children[i], fore, back);
		    }
	    }
    }

    /**
     * Take a component and set it and all its children to have
     * the specified enabled status
     *
     */
    public static void setEnabled(Component com, boolean enabled)
    {
                com.setEnabled(enabled);
		
		//Recurse through children
		if(com instanceof Container)
		    {
			Container contain = (Container)(com);
			Component [] children = contain.getComponents();
			for(int i=0;i<children.length;i++)
                            setEnabled(children[i], enabled);
		    }
    }

    /**
     * Take a slider and add a name label, returning a panel with
     * the two of them in it. Top should be true to add the label 
     * above the slider. Slider label is updated with the value of the
     * the slider as it is moved
     */
    public static JPanel nameSliderLabelled(final JSlider slider, final String name, 
					    boolean top)
    {
	return nameSliderLabelled(slider, name, top, 0, 1.0);
    }

    /**
     * Take a slider and add a name label, returning a panel with
     * the two of them in it. Top should be true to add the label 
     * above the slider. Slider label is updated with the value of the
     * the slider as it is moved. The value displayed is equal to the
     * actual value, plus the offset, multiplied by the scale
     */
    public static JPanel nameSliderLabelled(final JSlider slider, final String name, 
					    boolean top, final int offset, final double scale)
    {
	JPanel newPanel = new JPanel(new BorderLayout());

        //Make fillers
	Dimension dFill = new Dimension(5, 5);
	Box.Filler left = new Box.Filler(dFill, dFill, dFill);
	Box.Filler right = new Box.Filler(dFill, dFill, dFill);	
	Box.Filler leftT = new Box.Filler(dFill, dFill, dFill);
	Box.Filler rightT = new Box.Filler(dFill, dFill, dFill);

        //Clear the border
	slider.setBorder(null);

        //Add the slider
	newPanel.add(slider, BorderLayout.CENTER);

        //Add the label
	JPanel labelPanel = new JPanel(new BorderLayout());
	final JLabel nameLabel = new JLabel(name+":"+numForm.format(((slider.getValue()+offset)*scale)));
	slider.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent ce)
		{
		    nameLabel.setText(name+":"+numForm.format(((slider.getValue()+offset)*scale)));
		}
	    });

	nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	nameLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
	labelPanel.add(nameLabel, BorderLayout.CENTER);

        //Add the fillers to space out the sliders
	labelPanel.add(left, BorderLayout.WEST);
	labelPanel.add(right, BorderLayout.EAST);	
	newPanel.add(leftT, BorderLayout.WEST);
	newPanel.add(rightT, BorderLayout.EAST);
        newPanel.add(slider, BorderLayout.CENTER);

        //Add the label to either the top or bottom
	if(top)
	    newPanel.add(labelPanel, BorderLayout.NORTH);
	else
	    newPanel.add(labelPanel, BorderLayout.SOUTH);

	return newPanel;
    }

        /**
	 * Take a slider and add a name label, returning a panel with
	 * the two of them in it. Top should be true to add the label 
	 * above the slider. 
	 */
    public static JPanel nameSlider(JSlider slider, String name, boolean top)
    {
	JPanel newPanel = new JPanel(new BorderLayout());

        //Make fillers
	Dimension dFill = new Dimension(5, 5);
	Box.Filler left = new Box.Filler(dFill, dFill, dFill);
	Box.Filler right = new Box.Filler(dFill, dFill, dFill);	
	Box.Filler leftT = new Box.Filler(dFill, dFill, dFill);
	Box.Filler rightT = new Box.Filler(dFill, dFill, dFill);

        //Clear the border
	slider.setBorder(null);

        //Add the slider
	newPanel.add(slider, BorderLayout.CENTER);

        //Add the label
	JPanel labelPanel = new JPanel(new BorderLayout());
	JLabel nameLabel = new JLabel(name);
	nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	nameLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
	labelPanel.add(nameLabel, BorderLayout.CENTER);

        //Add the fillers to space out the sliders
	labelPanel.add(left, BorderLayout.WEST);
	labelPanel.add(right, BorderLayout.EAST);	
	newPanel.add(leftT, BorderLayout.WEST);
	newPanel.add(rightT, BorderLayout.EAST);
        newPanel.add(slider, BorderLayout.CENTER);

        //Add the label to either the top or bottom
	if(top)
	    newPanel.add(labelPanel, BorderLayout.NORTH);
	else
	    newPanel.add(labelPanel, BorderLayout.SOUTH);

	return newPanel;
    }

    /** 
     * Add four fillers to a container, of size size, and return
     * a new panel representing the center
     */
    public  static JPanel addFillers(Container con, int size)
    {
	return addFillers(con, size, size);
    }

    /** 
     * Add four fillers to a container, of size sizeX by sizeY, and return
     * a new panel representing the center
     */
    public static JPanel addFillers(Container con, int sizeX, int sizeY)
    {
        //Create the fillers
	Dimension dFill = new Dimension(sizeX, sizeY);
	Box.Filler north = new Box.Filler(dFill, dFill, dFill);
	Box.Filler south = new Box.Filler(dFill, dFill, dFill);
	Box.Filler east = new Box.Filler(dFill, dFill, dFill);
	Box.Filler west = new Box.Filler(dFill, dFill, dFill);

        //Add them to the container
	con.add(north, BorderLayout.NORTH);
	con.add(south, BorderLayout.SOUTH);
	con.add(east, BorderLayout.EAST);
	con.add(west, BorderLayout.WEST);

        //Return the center panel, with a BorderLayout as default
	JPanel newPanel = new JPanel(new BorderLayout());
	con.add(newPanel, BorderLayout.CENTER);
	return newPanel;
    }


}
