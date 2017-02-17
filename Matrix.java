import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * SignalDevice: Manipulates matrices for combining input signals. Can load and save standard MATLAB matrix files
 * Allows interactive editing of matrix values for mixing input signals together
 */
public class Matrix extends SProcessorModel
{

    //Dimensions of the matrix
    private int width, height;

    //The actual matrix values
    private double [] []  matrixValues;

    //The interface frame
    private transient JFrame matrixFrame;

    //The new output signal
    private GestureSignal outSig;

    //The matrix interface component
    private transient MatrixView matrixView;

    //Properties
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "Matrix";
    }
    public String getDescription()
    {
	return "Manipulates matrices for combining input signals. Can load and save standard MATLAB matrix files";
    }
    public String getDate()
    {
	return "Janurary 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }


    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, re-creating interface
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}

	//Recreate interface, as it was transient
	makeInterface();
    }


    /**
     * Show the matrix interface 
     */
    public void showInterface()
    {
	matrixFrame.setTitle(getName());
	matrixFrame.show();
    }

    /**
     * Construct a new matrix
     */
    public Matrix()
    {
       super();
       makeInterface();
    }

    /**
     * Make the user interface, and place it in a frame
     */
    public void makeInterface()
    {
	//Make frame
	matrixFrame = new JFrame(getName());
	matrixFrame.setSize(500,500);
	Container gc = matrixFrame.getContentPane();
	gc.setLayout(new BorderLayout());

	//Create the matrix view
	matrixView = new MatrixView();
	matrixView.setMatrix(matrixValues);
	matrixView.setHeight(height);
	matrixView.setWidth(width);

	//Add the view
	gc.add(matrixView, BorderLayout.CENTER);
    }

    /**
     * Change the matrix width and height
     * preserving the old values. Matrices will be created with the identity
     * elements by default.
     * This will not lose old values if the matrix is made smaller
     * and then larger
     */
    private void extendMatrix(int newWidth, int newHeight)
    {
        double [] [] temp = new double[newWidth][newHeight];

        for(int i=0;i<newWidth;i++)
        {
          for(int j=0;j<newHeight;j++)
          {
	      //Copy if possible
             if(j<height && i<width && matrixValues!=null)
                 temp[i][j] = matrixValues[i][j];
             else //Create identity matrix
               if(j==i)
                 temp[i][j]=1.0;
	       else
		   temp[i][j]=0.0;
          }
       }
	matrixValues = temp;

	//Update the view
	if(matrixView!=null)
	    {
		matrixView.setMatrix(matrixValues);
		matrixFrame.repaint();
      }
    }

    /**
     * Set the height of the matrix
     */
    private void setHeight(int newHeight)
    {
     if(newHeight>height)
        extendMatrix(width, newHeight);

     height=newHeight;

     //Update the view
     matrixView.setHeight(height);
     matrixFrame.repaint();
    }

    /**
     * Take a signal, multiply by the current matrix and then
     * send it back out
     */
    public void processSignal()
    {
	outSig = recalculateMatrix(lastSig);
	setCurSig(outSig);
    }
        
    /**
     * Return the height of the matrix (i.e output width)
     */
    public int getSignalWidth()
    {
	return height;
    }

    /**
     * Update the matrix size as the input connections are changed
     */
    public void connectionChange()
    {
     int tempWidth = getInputWidth();

     //Extend matrix as neccessary
     if(tempWidth>width)
       extendMatrix(tempWidth, tempWidth);

     width = tempWidth;
     height = tempWidth;

     //Update view width/height
     matrixView.setHeight(height);
     matrixView.setWidth(width);
     matrixView.repaint();

    }
    
    /**
     * Multiply a signal by the current matrix and return an
     * output signal
     */
    public GestureSignal recalculateMatrix(GestureSignal input)
    {
        double sum = 0.0;
        double [] inputValues = input.vals;
        double [] outputValues = new double[height];

	//If matrix is not of correct size, return original values
	if(input.vals.length<width)
            return new GestureSignal(outputValues, getID());

	//Do the matrix multiplication
       	for(int i=0;i<height;i++)
	    {
		for(int j=0;j<width;j++)
		    sum+=inputValues[j]*matrixValues[j][i];
		outputValues[i]=sum;
		sum = 0.0;
	    }
	return new GestureSignal(outputValues,  getID());
    }
    
    /**
     * Return the entry at the given position
     */
    public double getMatrixEntry(int x, int y)
    {
	if(x>0 && x<width && y>0 && y<height)
	    return matrixValues[x][y];
	else
	    return 0.0;
    }
    
    /**
     * Set the entry at the given position
     */
    public void setMatrixEntry(int x, int y, double newValue)
    {
	if(x>0 && x<width && y>0 && y<height)
	    matrixValues[x][y] = newValue;
    }


}
    
