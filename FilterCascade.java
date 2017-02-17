import java.io.Serializable;

public class FilterCascade implements Serializable
{
    static final long serialVersionUID = 213L;
    private double [] filterValues;
    private double coefficient;
    
    public double filter(double value)
    {
	double oldValue = value;
	for(int i=0;i<filterValues.length;i++)
	    {
		filterValues[i] = (coefficient)*filterValues[i] + (1-coefficient)*oldValue;
		oldValue = filterValues[i];
	    }
	return oldValue;
    }

    public void setCoefficient(double coef)
    {
	coefficient = coef;
    }

    public FilterCascade(int cascade, double coefficient)
    {
	filterValues = new double[cascade];
	this.coefficient = coefficient;
    }


}
