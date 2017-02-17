import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
/**
 * SignalDevice: Transfers heterogenous string object across a TCP/IP
 * link. Creates a server socket that processes can connect to
 */
public class NetworkTransfer extends SProcessorModel
{

    //The current socket in use
    private transient Socket transSocket;

    //True if the input thread should be running
    private boolean readingInput = false;

    //A stream which will transmit data to the open socket
    private transient PrintStream sockStream;

    //A stream for information coming from an incoming socket
    private transient BufferedInputStream inSockStream;

    //A textfield holding the current port number information
    private transient JTextField portField;

    //True if socket currently open
    private boolean socketOpen = false;

    //Address to connect to
    private String connectionAddr;

    //Label for the machine address
    private transient JLabel machineLabel;
    
    //Field holding the address data
    private transient JTextField addressField;

    //True if this is a server connection
    private boolean serverConnect = true;


    //The thread that handles input to avoid blocking
    private transient InputThread inputThread;

    //Properties...
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "NetworkTransfer";
    }
    public String getDescription()
    {
	return "Sends string objects across a TCP/IP link";
    }
    public String getDate()
    {
	return "Janurary 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    public void connectionChange()
    {
    }

    public NetworkTransfer()
    {
	super();

    }

    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, re-creating buffer
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
	
    }


    
    /**
     * Try to open a socket on the specified port.
     * Then waits in a separate thread for a connection
     * and create a stream to allow writing to the socket.
     */
    private void openSocket(final int port)
    {
	//Return if no interace initialized
	if(addressField==null)
	    return;

        new Thread()
	    {
		public void run()
		{
		    if(socketOpen)
			closeSocket();
		    try{
			if(serverConnect)
			    {
				ServerSocket sSock  = new ServerSocket(port);
				transSocket = sSock.accept();
				sockStream = new PrintStream(new BufferedOutputStream(
										      transSocket.getOutputStream()));
			    }
			else
			    {
				InetAddress connAddr = InetAddress.getByName(connectionAddr); 
				Socket inSock = new Socket(connAddr, port);
				transSocket = inSock;
				inSockStream = new BufferedInputStream(inSock.getInputStream());
                                readingInput = true;
                                
                                inputThread = new InputThread();
                                inputThread.start();
			    }
				socketOpen = true;
		    }catch(IOException ioe) {
			JOptionPane.showMessageDialog(null, ioe.getMessage(), 
						      "TCP/IP error", 
						      JOptionPane.ERROR_MESSAGE);
		    }
		}      
	    }.start();
    }
    
    /**
     * Close the current socket
     */
    private void closeSocket()
    {
        try{
	    if(sockStream!=null)
		{
		    sockStream.flush();
		    sockStream.close();
		}
	    if(inSockStream!=null)
		{
		    inSockStream.close();
                
                    if(inputThread!=null)
                    {
                      readingInput = false;
                      inputThread = null;
                    }
		}
	    socketOpen = false;
	    transSocket.shutdownOutput();
        }catch(IOException ioe) {      
	    JOptionPane.showMessageDialog(null, ioe.getMessage(), "TCP/IP error", 
					  JOptionPane.ERROR_MESSAGE);}
    }
    
    /**
     * Listener for when the user clicks the open connection
     * control
     */
    private class OpenListener implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    int port;
	    ((JToggleButton)(ae.getSource())).removeActionListener(this);
	    ((JToggleButton)(ae.getSource())).setEnabled(false);
	    try{
		port = Integer.parseInt(portField.getText());
		connectionAddr = addressField.getText();
		openSocket(port);
	    } catch(NumberFormatException nfe) {}
	}	
    }

   
    
    /**
     * Listener for server/client buttons
     */
    private class ServerClientListener implements ActionListener
    {

	private boolean isServer;
	private String hostAddr;

	public void actionPerformed(ActionEvent ae)
	{
	    if(isServer)
		{
		    
		    //Show the address on the frame
		    machineLabel.setText("This machine: ");
		    addressField.setEnabled(false);
		    addressField.setText(hostAddr);
		    serverConnect = true;
		}
	    else
		{
		    //Show the address on the frame
		    machineLabel.setText("Connect to: ");
		    addressField.setEnabled(true);
                    addressField.setText("");
                    addressField.setColumns(20);

		    serverConnect = false;
		    
		}
	}

	/**
	 * Construct a new listener.
	 * Parameter is true if should switch to 
	 * server mode
	 */
	public ServerClientListener(boolean server, String hostAddr)
	{
	    isServer = server;
	    this.hostAddr = hostAddr;
	}

    }
    
    /**
     * Shows the main interface
     */
    public void showInterface()
    {
	//Create frame
	JFrame jf = new JFrame();
        jf.setSize(400,150);
	Container gc = jf.getContentPane();
	jf.setTitle(getName());
	
	//Get the current IP address
	String hostAddr = "Unknown";
	try{
	    InetAddress thisAddr = InetAddress.getLocalHost();
	    hostAddr = thisAddr.getHostAddress();
	} catch(Exception E){}

	//Show the address on the frame
	machineLabel = new JLabel("This machine: ");
	
	//Add the address field and show it
	addressField = new JTextField(hostAddr);
	addressField.setEnabled(false);
	JPanel hostPan = new JPanel(new FlowLayout());
	hostPan.add(machineLabel);
	hostPan.add(addressField);
	gc.add(hostPan, BorderLayout.NORTH);
	
	//Add the port setting field and the serialize box
	portField = new JTextField("1842");
	JPanel portPan = new JPanel(new FlowLayout());
	portPan.add(new JLabel("Port to use: "));
	portPan.add(portField);
	gc.add(portPan, BorderLayout.CENTER);
	
	JPanel buttonPanel = new JPanel(new FlowLayout());

	
	//Add the connection open control
	JToggleButton open = new JToggleButton("Open connection...");
	
	if(socketOpen)
	    {
		open.setSelected(true);
		open.setEnabled(false);
	    }
	else
	    open.addActionListener(new OpenListener());
	
	buttonPanel.add(open);
	
	JRadioButton serverButton = new JRadioButton("Server", true);
	JRadioButton clientButton = new JRadioButton("Client", false);

	ButtonGroup serverClient = new ButtonGroup();
	serverClient.add(serverButton);
	serverClient.add(clientButton);
	buttonPanel.add(serverButton);
	buttonPanel.add(clientButton);

	serverButton.addActionListener(new ServerClientListener(true, hostAddr));
	clientButton.addActionListener(new ServerClientListener(false, hostAddr));
	gc.add(buttonPanel, BorderLayout.SOUTH);
       
	jf.show();
    }
    
    /**
     * Write the data to the current socket, flushing
     * after the write. If object is a gesture,
     * write out it's name
     */
    private void writeNetData(final Object o)
    {
	new Thread()
	{
	    public void run()
	    {
		if(o instanceof ProbabilisticGesture)
		    {
			ProbabilisticGesture pGest = (ProbabilisticGesture) o;

			ParameterizedGesture mostProbable = pGest.getMostProbable();
			if(mostProbable!=null)
			    {
				sockStream.print(mostProbable.getName()+"\0");
			    }
		    }
		sockStream.flush();
	    }
	}.start();
    }
    
    public void deleted()
    {
    }



    /**
     * If this is an input connection
     * check for incoming data from the socket stream.
     * Propogate any objects read to the devices children.
     */
    private class InputThread extends Thread
    {

    public void run()
    {
      while(readingInput)
      {
       try{Thread.sleep(5);} catch(InterruptedException ie){}
	if(!serverConnect && inSockStream!=null)
	    {
		try{
    
		    String newLine = "";
		    char newChar = (char)(inSockStream.read());
		    while(newChar!='\0')
			{
			    newLine += newChar;
			    newChar = (char)(inSockStream.read());
			}
		    distributeHetObject(newLine);
		} catch(IOException ioe)
		    {
			JOptionPane.showMessageDialog(null, ioe.getMessage(), 
						      "TCP/IP reading error", 
						      JOptionPane.ERROR_MESSAGE);}
       }
       }
     }
    }
    
    /**
     * Send the string representation of incoming objects
     * to the current output socket, if there is one
     */
    public void processHetObject(Object o)
    {
	if(serverConnect && sockStream!=null)
	    {
		writeNetData(o);
	 }
    }

}
