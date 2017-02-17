import java.util.*;

public class SpaceMatching extends StringMatching
{
    public SpaceMatching(Vector comparison)
    {
	super(comparison);
    }

    public int computeDifferenceStep(int newVal, int dims, int divs)
    {
	double [] matchCoords;
	double [] coords = Resynthesizer.getSpace(newVal, 20, divs, dims);
	int bSize = currentIndex+1;
	int [] newRow = new int[currentIndex+1];
	currentIndex++;
	for(int i=0;i<bSize;i++)
	    {
		if(i==0)
		    newRow[i] = currentIndex;
		else
		    {
			Object bJ = comparison.get(i-1);
			matchCoords = (double []) bJ;
			double diff = Math.sqrt((matchCoords[0]-coords[0])*(matchCoords[0]-coords[0])+
					   (matchCoords[1]-coords[1])*(matchCoords[1]-coords[1])+
					   (matchCoords[2]-coords[2])*(matchCoords[2]-coords[2]));
			System.out.println("Diff = "+diff);
			int min = min3(newRow[i-1], lastRow[i], lastRow[i-1]);
			newRow[i] = (int)(min+diff);
		    }
	    }
	lastRow = newRow;
        return newRow[bSize-1];
    }

    public static int computeDifference(Vector a, Vector b, int dims, int divs)
    {
	int aSize = a.size()+1;
	int bSize = a.size()+1;
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
