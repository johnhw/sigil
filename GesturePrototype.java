import java.util.*;
import java.io.*;

public class GesturePrototype implements Serializable
{

    private Vector data;
    private String name;
    private Vector pathData;

    public GesturePrototype()
    {
	data = new Vector();
	pathData = new Vector();
    }

    public void addData(int elt, double [] pathElt)
    {
	data.add(new Integer(elt));
	pathData.add(pathElt);
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public String getName()
    {
	return name;
    }

    public Vector getPathData()
    {
	return pathData;
    }

    public Vector getData()
    {
	return data;
    }


}
