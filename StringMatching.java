import java.util.*;
import java.io.Serializable;

public class StringMatching implements Serializable
{
    
    protected int [] lastRow;
    protected Vector comparison;
    protected int currentIndex;
    
    public void reset()
    {
	addFirstRow();
	currentIndex = 0;
    }

    public StringMatching(Vector comparison)
    {
	this.comparison = comparison;
	reset();
    }

    protected void addFirstRow()
    {
	lastRow = new int[comparison.size()+1];
	for(int i=0;i<lastRow.length;i++)
	    lastRow[i] = i;
    }
    
    protected static int min3(int a, int b, int c)
    {
	return (int)(Math.min(a,Math.min(b,c)));
    }

    public int computeDifferenceStep(Object newVal)
    {
	int bSize = comparison.size()+1;
	
	int [] newRow = new int[bSize];
	currentIndex++;
	for(int i=0;i<bSize;i++)
	    {
		
		if(i==0)
		    newRow[i] = currentIndex;
		else
		    {
			if(i<comparison.size())
			    {
				Object bJ = comparison.get(i-1);
				if(newVal.equals(bJ))
				    newRow[i] = lastRow[i-1];
				else
				    {
					int min;
					if(i<lastRow.length)
					    min = min3(newRow[i-1], lastRow[i], lastRow[i-1]);
					else
					    min = Math.min(newRow[i-1], lastRow[i-1]);
					newRow[i] = min+1;
				    }
			    }
			else
			    {
				int min;
				if(i<lastRow.length)
					    min = min3(newRow[i-1], lastRow[i], lastRow[i-1]);
					else
					    min = Math.min(newRow[i-1], lastRow[i-1]);

				
				newRow[i] = min+1;
			    }
		    }

	    }
	lastRow = newRow;
        return newRow[bSize-1];
    }

    public static int computeDifference(Vector a, Vector b)
    {
	int aSize = a.size()+1;
	int bSize = b.size()+1;
	int [] [] diffArray = new int [aSize][];
	for(int i=0;i<aSize;i++)
	    {
		diffArray[i] = new int[bSize];
		{
		    for(int j=0;j<bSize;j++)
			{
			    
			    if(j==0)
				diffArray[i][j] = i;
			    else if(i==0)
				diffArray[i][j] = j;
			    else
				{
				    Object aI = a.get(i-1);
				    Object bJ = b.get(j-1);
				    if(aI.equals(bJ))
					diffArray[i][j] = diffArray[i-1][j-1];
				    else
					{
					    int min = min3(diffArray[i][j-1], diffArray[i-1][j], 
						     diffArray[i-1][j-1]);
					    diffArray[i][j] = min+1;
					}
				}
			}
		}
	    }
	return diffArray[aSize-1][bSize-1];
    }


}
