package sigil;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class SignalCanvas extends JPanel
{
    private FlashBar flashBar;
    private Vector visualElts;
    private Vector currentSelection;
    private VisualElement curDrag;
    private Point dragPt = new Point();
    private boolean shiftDrag;
    private DeviceSelector devSel;
    private boolean devShown = false;
    private boolean undoable = false;
    private boolean redoable = false;
    private boolean undoEnabled = true;
    private LinkedList undoBuffer;
    private LinkedList redoBuffer;
    private Rectangle selRect;
    private Rectangle drawSelRect;
    private boolean selectionReleased = false;
    private boolean selHasGA = false;
    private int glowPhase = 0;
    private Thread glowThread;
    private String flashString;
    private boolean infoShown;
    private InfoDisplay infoDisplay;
    private static final int maxUndo = 10;

    public void closeInfo()
    {
	infoShown = false;
    }

    private void toggleUndoEnabled(boolean enabled)
    {
	int val=JOptionPane.OK_OPTION;
	if(undoEnabled && !enabled)
	    {
	       val = JOptionPane.showConfirmDialog(null, "Confirm disable undo?", 
							"Confirm disable undo?", JOptionPane.OK_CANCEL_OPTION);
	    }

	if(val==JOptionPane.OK_OPTION)
	    {
		undoEnabled = enabled;
	    }
    }


    private void makeUndoable()
    {
	if(undoEnabled)
	    {
		try{
		    TemporarySerializer undoBuf = new TemporarySerializer(visualElts);
		    undoBuffer.addFirst(undoBuf);
		    undoable = (undoBuffer.size()>0);
		    if(undoBuffer.size()>=maxUndo)
			undoBuffer.removeLast();
		} catch(IOException ioe) {
		    JOptionPane.showMessageDialog(null, "Warning: This action cannot be undone", "No undo", JOptionPane.WARNING_MESSAGE);
		    ioe.printStackTrace();
		    undoable = false;}
	    }
    }

    private void makeRedoable()
    {
	if(undoEnabled)
	    {
		try{
		    TemporarySerializer redoBuf = new TemporarySerializer(visualElts);
		    redoBuffer.addFirst(redoBuf);
		    redoable = (redoBuffer.size()>0);
		    
		    if(redoBuffer.size()>=maxUndo)
			redoBuffer.removeLast();
		} catch(IOException ioe) {
		    JOptionPane.showMessageDialog(null, "Warning: This action cannot be redone", "No redo", JOptionPane.WARNING_MESSAGE);
		    ioe.printStackTrace();
		    redoable = false;}
	    }
    }


    

    private void redo()
    {
	if(undoEnabled)
	    {
		if(redoBuffer.size()<1)
		    {
			redoable = false;
			return;
		    }
		try{
		    TemporarySerializer redoBuf = (TemporarySerializer)(redoBuffer.removeFirst());
		    visualElts =  (Vector)redoBuf.deserialize();
		    System.gc();
		    redoable = (redoBuffer.size()>0);
		    repaint();
		} catch(IOException ioe) {}
		catch(ClassNotFoundException cfne) {}
	    }
    }

    private void undo()
    {
	if(undoEnabled)
	    {
		if(undoBuffer.size()<1)
		    {
			undoable = false;
			return;
		    }
		
		try{
		    makeRedoable();
		    TemporarySerializer undoBuf = (TemporarySerializer)(undoBuffer.removeFirst());
		    visualElts =  (Vector)undoBuf.deserialize();
		    updateGroups();
		    System.gc();
		    undoable = (undoBuffer.size()>0);
		    repaint();
		} catch(IOException ioe) {}
		catch(ClassNotFoundException cfne) {}
	    }
    }

 public Vector getSelection()
 {
     return currentSelection;
 }

    public void deselect()
    {
	for(int i=0;i<visualElts.size();i++)
	    ((VisualElement)(visualElts.get(i))).deselect();
	currentSelection = null;
	selHasGA = false;
    }

    private void releaseSelection()
    {
	boolean selectedSomething = false;
	if(drawSelRect!=null)
	    {
		currentSelection = new Vector();
		for(int i=0;i<visualElts.size();i++)
		    {
			VisualElement vElt =  ((VisualElement)(visualElts.get(i)));
			
			if(vElt.isInside(drawSelRect))
			    {
				selectedSomething = true;
				select(vElt);
			    }
		    }
	    }
	selRect = null;
	drawSelRect = null;
	repaint();
    }

    private void select(VisualElement vElt)
    {
	if(vElt!=null)
	    {
		vElt.select();
		if(currentSelection==null)
		    currentSelection = new Vector();
		currentSelection.add(vElt);
		if(!(vElt instanceof Group) && 
		   vElt.getElement().getElement() instanceof GAElement)
		    selHasGA = true;
	    }
    }


 private class KeyListen extends KeyAdapter
 {
     public void keyReleased(KeyEvent ke)
     {
         int code = ke.getKeyCode();

         if((ke.getModifiers() & KeyEvent.CTRL_MASK) != 0)
	     {
		 switch(code)
		     {
		     case KeyEvent.VK_S: save(); break;
		     case KeyEvent.VK_O: load(); break;
		     case KeyEvent.VK_D: showDevices(200,300); break;
		     case KeyEvent.VK_P: updateDevice(); break;
		     case KeyEvent.VK_E: evolve(); break;
		     case KeyEvent.VK_A: aboutBox(); break;
		     case KeyEvent.VK_Z: undo(); break;
		     case KeyEvent.VK_R: redo(); break;
		     case KeyEvent.VK_Y: syncAll(true); break;
		     case KeyEvent.VK_U: syncAll(false); break;

		     }
	     }
     }
 }


 private class canvasMotionListen extends MouseMotionAdapter
 {
     public void mouseMoved(MouseEvent me)
     {
	 flashBar.mouseMoved(me.getX(), me.getY());
     }
     
     public void mouseDragged(MouseEvent me)
     {
	 flashBar.mouseMoved(me.getX(), me.getY());
	 
	 if((devShown && devSel.isOver(me.getX(), me.getY())) ||
	    (infoShown && infoDisplay.isOver(me.getX(), me.getY())))
	     return;
	 
	 if(!MouseUtils.left(me))
	     return;
	 
	 if(curDrag==null)
	     {
		 if(selRect==null)
		     selRect = new Rectangle(me.getX(), me.getY(), 0, 0);
		 else
		     {
			 selRect.width = me.getX()-selRect.x;
			 selRect.height = me.getY()-selRect.y;
		     }
		 if(drawSelRect==null)
		     drawSelRect = new Rectangle(selRect.x, selRect.y,
						 selRect.width, selRect.height);
		 if(selRect.width<0)
		     {
			 drawSelRect.x = me.getX();
			 drawSelRect.width = -selRect.width;
		     }
		 else
		     {
			 drawSelRect.x = selRect.x;
			 drawSelRect.width = selRect.width;
		     }
		 
		 if(selRect.height<0)
		     {
			 drawSelRect.y = me.getY();
			 drawSelRect.height = -selRect.height;
		     }
		 else
		     {
			 drawSelRect.y = selRect.y;
			 drawSelRect.height = selRect.height;
		     }
		 repaint();
		 
		 
	     }
	 else
	     {
		 dragPt = me.getPoint();
		 Dimension dSize = getSize();
		 if(!shiftDrag && me.getX()>10 && me.getY()>10
		    && me.getX()<dSize.width-10 && me.getY()<dSize.height-10)
		     {
			 curDrag.move(me.getX(), me.getY());
		     }
		 
		 repaint();
	     }
     }

 }

  private void aboutBox()
  {
    JFrame jf = new JFrame();
    jf.setSize(300, 370);
    jf.getContentPane().add(new AboutBox());
    jf.setTitle("About...");
    jf.show();

  }


    private void showSource(File sourceFile)
    {
       
	    JFrame sourceFrame = new JFrame();
	    sourceFrame.setSize(560,600);
	    
	    JTextArea sourceText = new JTextArea();
	    sourceText.setLineWrap(true);
	    sourceText.setEditable(false);
	    JScrollPane scroller = new JScrollPane(sourceText);

	    sourceFrame.getContentPane().add(scroller);
	    sourceFrame.setTitle(sourceFile.getName());
	    try{
		BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFile));
		String currentLine = sourceReader.readLine();
		while(currentLine!=null)
		    {
			sourceText.append(currentLine+"\n");
			currentLine = sourceReader.readLine();
		    }
		JScrollBar scroll = scroller.getVerticalScrollBar();
		scroll.setValue(scroll.getMinimum());

		sourceReader.close();
		sourceFrame.show();
	    }catch(IOException ioe) {}
    }

 private void showProperties(VisualElement vElt, int x, int y)
 {
     GestureElement gElt = vElt.getElement();
     
     Vector fieldVector = new Vector();
     fieldVector.add("Name");
     fieldVector.add("Description");
     fieldVector.add("Creation date");
     fieldVector.add("Author");
     Vector valueVector = new Vector();
     valueVector.add(gElt.getDevName());
     valueVector.add(gElt.getDescription());
     valueVector.add(gElt.getDate());
     valueVector.add(gElt.getAuthor());
     infoShown = true;
     infoDisplay = new InfoDisplay(this, x, y, fieldVector, valueVector,
				   "Properties for "+gElt.getName()); 
     repaint();
 }

 private void showDevices(int x, int y)
 {
     if(!devShown){
        devSel = new DeviceSelector(SignalCanvas.this,x,y);        
        devShown = true;
        repaint();
        }
 }

    private void syncAll(boolean sync)
    {
	for(int i=0;i<visualElts.size();i++)
	    {
		VisualElement vElt = (VisualElement)(visualElts.get(i));
		if(!(vElt instanceof Group) && canAsync(vElt))
		    setSynchronize(vElt, sync);
	    }
	
    }

    private boolean canAsync(VisualElement vElt)
    {
	if(vElt.getElement()!=null)
	    return vElt.getElement().canAsync();
	else if (vElt instanceof Group)
	    return true;
	else
	    return false;
	    
    }

    private void setSynchronize(VisualElement vElt, boolean sync)
    {
	if(vElt.getElement()!=null && canAsync(vElt)) 
	    vElt.getElement().setSynchronous(sync);

	else if(vElt instanceof Group) 
	    {
		Vector internalElts = ((Group)vElt).getInternalElements();
		for(int i=0;i<internalElts.size();i++)
		    {
			VisualElement nextElt = (VisualElement)(internalElts.get(i));
			setSynchronize(nextElt, sync);
		    }
	    }
    }

    private boolean getSynchronize(VisualElement vElt)
    {
	if(vElt.getElement()!=null)
	    return vElt.getElement().synchronous();
	else if(vElt instanceof Group) 
	    {
		Vector internalElts = ((Group)vElt).getInternalElements();
		boolean allSync = true;
		for(int i=0;i<internalElts.size();i++)
		    {
			VisualElement nextElt = (VisualElement)(internalElts.get(i));
			allSync = allSync && getSynchronize(nextElt);
		    }
		return allSync;
	    }
	else
	    return false;
    }

    private void deleteSelection(Vector currentSel)
    {
	for(int i=0;i<currentSel.size();i++)
	    {
		VisualElement vElt = (VisualElement)(currentSel.get(i));
		delete(vElt);
	    }
        currentSelection = null;
    }

    private void delete(VisualElement vElt)
    {
	
	vElt.deselect();
	remove(vElt);
	vElt.disconnectSelf();
	if(vElt instanceof Group)
	    {
		Vector internalElts = ((Group)vElt).getInternalElements();
		for(int i=0;i<internalElts.size();i++)
		    {
			VisualElement nextElt = (VisualElement)(internalElts.get(i));
			delete(nextElt);
		    }
	    }
    }

 public void newCanvas()
 {

     int retVal = JOptionPane.showConfirmDialog(null, 
						"Are you sure you want to erase the current setup?",
                                               "Confirm new?", JOptionPane.YES_NO_OPTION);
     if(retVal==JOptionPane.YES_OPTION)
     {
	 makeUndoable();
	 ElementID.setID(0);
	 SignalHeader sigHeader = MasterClock.getHeader();
	 sigHeader.annotation = "";
	
	 sigHeader.verNo = 0;

	 for(int i=0;i<visualElts.size();i++)
	     {
		 VisualElement tElt = (VisualElement)(visualElts.get(i));
		 tElt.deselect();
		 remove(tElt);
		 tElt.disconnectSelf();
	     }
	 visualElts = new Vector();
	 repaint();
	 flashString = "New";
	 startGlow();
     }
 }

    
 private class canvasListen extends MouseAdapter
 {

     public void showPopupMenu(final VisualElement vElt, final int x, final int y)
     {
	 boolean shownEvolve = false;
	 JPopupMenu jMen = new JPopupMenu();
	 boolean curSel = (currentSelection!=null && currentSelection.size()>0);

	 if(vElt==null && !curSel)
	     return;

	 if(!curSel)
	     {
		 if(!(vElt instanceof Group))
		     {
			 
			 if(!(vElt instanceof Group) && !vElt.getElement().isTerminating())
			     {
				 final GestureElement gElt = vElt.getElement();
				 jMen.add(new AbstractAction("Buffer...")     {
					 public void actionPerformed(ActionEvent ae){
					     vElt.getElement().modifyBuffer();
					 }
				     });
			     }
		     }                
		 jMen.addSeparator();
		 
		 if(vElt instanceof Group)
		     {
			 jMen.add(new AbstractAction("Expand")     {
				 public void actionPerformed(ActionEvent ae){
				     ((Group)vElt).expand();
				     repaint();                       
				 }
			     });
			 
			 jMen.add(new AbstractAction("Break apart")     {
				 public void actionPerformed(ActionEvent ae){
				     makeUndoable();
				     ((Group)vElt).breakApart();
				     visualElts.remove(vElt);
				     repaint();                       
				 }
			     });
		     }         
		 
		 if(vElt.isInGroup())
		     {
			 jMen.add(new AbstractAction("Contract")     {
				 public void actionPerformed(ActionEvent ae){
				     vElt.contractParent();
				     repaint();                       
				 }
			     });
			 
			 jMen.add(new AbstractAction("Ungroup")     {
				 public void actionPerformed(ActionEvent ae){
				     vElt.removeFromGroup();
				     repaint();                       
				 }
			     });
			 
		     }
		 
		 jMen.add(new AbstractAction("Rename")     {
			 public void actionPerformed(ActionEvent ae){
			     String newName = 
				 JOptionPane.showInputDialog("Enter new name");
			     if(newName!=null)
				 vElt.rename(newName);
			     repaint();                       
			 }
		     });
	     }

	 if(curSel)
	 {
	     jMen.add(new AbstractAction("Group")     {
			     public void actionPerformed(ActionEvent ae){
				 group();}});

	 }

     if(vElt!=null && !(vElt instanceof Group) && 
	vElt.getElement()!=null && vElt.getElement().getElement() 
	instanceof GAElement)
	 {
             shownEvolve = true;
             jMen.add(new AbstractAction("Evolve...")     {
		   
		     public void actionPerformed(ActionEvent ae){
			 makeUndoable();
             Vector temp = new Vector();
             temp.add(vElt);
             evolve(temp);}});
	 }

     jMen.add(new AbstractAction("Delete")     {
	     public void actionPerformed(ActionEvent ae){
		 makeUndoable();
		 if(currentSelection==null)
		     delete(vElt);
		 else
		     deleteSelection(currentSelection);
		 repaint();
      }
     });

     if(!curSel)
	 {
	     jMen.addSeparator();
	     if(!(vElt instanceof Group))
	     {
		 jMen.add(new AbstractAction("Properties...") {
			 public void actionPerformed(ActionEvent ae)
			 {showProperties(vElt,x,y);}});
	     }
	     String syncName = (vElt instanceof Group) ? "All synchronous" : "Syncronous";
	     final JCheckBoxMenuItem sync = new JCheckBoxMenuItem(new AbstractAction(syncName) {
		     public void actionPerformed(ActionEvent ae)
		     {
			 boolean syncVal = ((JCheckBoxMenuItem)(ae.getSource())).isSelected();
			 setSynchronize(vElt,syncVal);
		     }});
	     sync.setEnabled(canAsync(vElt));
	     sync.setBorderPainted(false);
	     sync.setState(getSynchronize(vElt));
	     jMen.add(sync);
	 }

     else if(!shownEvolve && selHasGA)
	 jMen.add(new AbstractAction("Evolve...")     {
	     public void actionPerformed(ActionEvent ae){
		 makeUndoable();
		 evolve();}});
     
     if(!curSel && !(vElt instanceof Group))
		 {
		     String deviceName = vElt.getElement().getDevName();
		     final File srcFile = new File("."+File.separator+
						   deviceName+".java");
		     if(srcFile.exists() && srcFile.isFile())
			 { 
			     jMen.add(new AbstractAction("Show source...")     {
			     public void actionPerformed(ActionEvent ae)
				     {
					 showSource(srcFile);
				     }
				 });
			 }
		 }

     jMen.show(SignalCanvas.this, x, y);
  }

  public void showObjectMenu(final int x, final int y)
  {
     JPopupMenu jMen = new JPopupMenu();

     jMen.add(new AbstractAction("Save")     {
	     public void actionPerformed(ActionEvent ae){save();}});

     jMen.add(new AbstractAction("Load")     {
	     public void actionPerformed(ActionEvent ae){load();}});

     jMen.add(new AbstractAction("New")     {
	     public void actionPerformed(ActionEvent ae){newCanvas();}});


     jMen.add(new AbstractAction("Merge")     {
	     public void actionPerformed(ActionEvent ae){merge();}});

     jMen.addSeparator();

     jMen.add(new AbstractAction("Devices...")     {
	     public void actionPerformed(ActionEvent ae){
		 showDevices(x,y);}});

     jMen.add(new AbstractAction("Update device...")
	 { public void actionPerformed(ActionEvent ae)
             {updateDevice();}});

     jMen.add(new AbstractAction("Desynchronize all")
	 { public void actionPerformed(ActionEvent ae)
             {syncAll(false);}});

     jMen.add(new AbstractAction("Synchronize all")
	 { public void actionPerformed(ActionEvent ae)
             {syncAll(true);}});
     
     jMen.addSeparator();
     JMenuItem undoItem = new JMenuItem(new AbstractAction("Undo: "+undoBuffer.size())
	 { public void actionPerformed(ActionEvent ae)
             {undo();}});
      undoItem.setEnabled(undoable);
      jMen.add(undoItem);


     JMenuItem redoItem = new JMenuItem(new AbstractAction("Redo: "+redoBuffer.size())
	 { public void actionPerformed(ActionEvent ae)
             {redo();}});
      redoItem.setEnabled(redoable);
      jMen.add(redoItem);
      
      jMen.add(new AbstractAction("Snapshot")
	  { public void actionPerformed(ActionEvent ae)
	      {makeUndoable();}});

     JCheckBoxMenuItem enableUndo = new JCheckBoxMenuItem(new AbstractAction("Undo enabled") {
	      public void actionPerformed(ActionEvent ae)
	      {
		  boolean enabled = ((JCheckBoxMenuItem)(ae.getSource())).isSelected();
		  toggleUndoEnabled(enabled);
	      }});
	     enableUndo.setState(undoEnabled);
	     jMen.add(enableUndo);
      
      jMen.addSeparator();
      jMen.add(new AbstractAction("About...")     {
	      public void actionPerformed(ActionEvent ae){
		  aboutBox();}});

      jMen.show(SignalCanvas.this, x, y);
  }
 
  public void mouseClicked(MouseEvent me)
  {

      if(MouseUtils.left(me) && !MouseUtils.shift(me) && me.getClickCount()==1)
	  {
	      deselect();
	      repaint();
	  }
      
      else if(me.getClickCount()==1 && MouseUtils.left(me))
	  {
	      if(currentSelection==null)
		  currentSelection = new Vector();
	      for(int i=0;i<visualElts.size();i++)
		  {
		      VisualElement vElt =  ((VisualElement)(visualElts.get(i)));
		      if(vElt.isOver(me.getX(), me.getY()))
			  select(vElt);
		  }
	      repaint();
	  }
        
  if(devShown && devSel.isOver(me.getX(), me.getY()))
      return;
  
  if(me.getClickCount()==1 && MouseUtils.left(me) && MouseUtils.ctrl(me))
      {
	  for(int i=0;i<visualElts.size();i++)
	      {
		  VisualElement vElt =  ((VisualElement)(visualElts.get(i)));
		  vElt.deleteConnection(me.getX(), me.getY());
	      }
	  repaint();
      }


  
  if(MouseUtils.dblClick(me))
  {
      for(int i=0;i<visualElts.size();i++)
	  {
	      VisualElement vElt =  ((VisualElement)(visualElts.get(i)));
	      if(vElt.isOver(me.getX(), me.getY()) && !(vElt instanceof Group))
		  vElt.getElement().showInterface();  
	  }
  }
  }

     public void mousePressed(MouseEvent me)
     {
	 if(devShown && devSel.isOver(me.getX(), me.getY()))
	     return;
	 
	 if(curDrag==null && MouseUtils.left(me))
	     {
		 for(int i=0;i<visualElts.size();i++)
		     {
			 VisualElement vElt =  ((VisualElement)(visualElts.get(i)));
			 if(vElt.isOver(me.getX(), me.getY()))
			     curDrag = vElt;    
			 shiftDrag=MouseUtils.shift(me);
		     }
	     }
	 
	 if(MouseUtils.right(me))
	     {
		 boolean over = false;
		 if(currentSelection == null)
		     {
			 for(int i=0;i<visualElts.size();i++)
			     {
				 final VisualElement vElt =  ((VisualElement)(visualElts.get(i)));
				 if(vElt.isOver(me.getX(), me.getY()))
				     {
					 over = true;
					 showPopupMenu(vElt, me.getX(), me.getY());
				     }
			     }
			 
		     }
		 else if(currentSelection.size()>0)
		     {
			 over=true;
			 showPopupMenu(null, me.getX(),me.getY());
		     }
		 if(!over)
		     {
			 showObjectMenu(me.getX(), me.getY());
		     }
	     }
     }


  public void mouseReleased(MouseEvent me)
  {
      grabFocus();
      
      releaseSelection();
      
      flashBar.mouseReleased(me.getX(), me.getY());
      
      if(devShown && devSel.isOver(me.getX(), me.getY()))
	  return;
      
      if(MouseUtils.middle(me))
	  showDevices(me.getX(), me.getY());
      
      
      if(MouseUtils.left(me) && !shiftDrag)
	  curDrag = null;
      else if(MouseUtils.left(me) && curDrag!=null)
	  {
	      for(int i=0;i<visualElts.size();i++)
		  {
		      VisualElement vElt =  ((VisualElement)(visualElts.get(i)));
		      if(vElt.isOver(me.getX(), me.getY()) && curDrag!=null)
			  {
			      curDrag.connect(vElt);
			      curDrag=null;
			      repaint();
			  }
		  }
	  }
      shiftDrag=false;
  }
 }
    
    public void add(VisualElement ve)
    {
	makeUndoable();
	visualElts.add(ve);
    }

  public void remove(VisualElement ve)
  {
   visualElts.remove(ve);
  }

 public void closeDevice()
 {
  devShown = false;
 }

  public void save()
  {
      if(PersistentState.saveState(visualElts))
	  {
	      flashString="Saved";
	      startGlow();
	  }
  }

    private void group()
     {
	 String groupName = JOptionPane.showInputDialog(null, "Group name", 
							"New group...",
							JOptionPane.QUESTION_MESSAGE);
	 if(groupName!=null)
	     {
		 double avgX = 0.0, avgY = 0.0;
		 if(currentSelection==null)
		     return;

		 int len = currentSelection.size();
		 if(len==0)
		     return;

	 	 for(int i=0;i<len;i++)
		     {
			 VisualElement tElt = (VisualElement)(currentSelection.get(i));
			 Rectangle bounds = tElt.getBounds();
			 avgX += bounds.x;
			 avgY += bounds.y;
		     }
		 avgX/=(double)len;
		 avgY/=(double)len;
		 Group newGroup = new Group(groupName, (int)avgX, (int)avgY, SignalCanvas.this);
		 visualElts.add(newGroup);
		 for(int i=0;i<currentSelection.size();i++)
		     {
			 VisualElement tElt = (VisualElement)(currentSelection.get(i));
			 tElt.deselect();
			 tElt.addToGroup(newGroup);
		     }
		 newGroup.contract();
		 currentSelection=null;
		 repaint();                   
	     }
     }


     private void updateDevice()
     {
	 String devName = JOptionPane.showInputDialog(null, "Enter device name", 
						      "Update device", JOptionPane.QUESTION_MESSAGE);
	 if(devName==null)
	     return;
	 ModuleLoader.addModule(devName);
         try{
	     Class updatedClass = Class.forName(devName);
	     boolean shownDialog = false;
	     boolean keepGoing = true;
	     for(int i=0;i<visualElts.size() && keepGoing;i++)
		 {
		     VisualElement vElt = (VisualElement)(visualElts.get(i));
		     GestureGenerator thisGen = vElt.getElement().getElement();
		     if(updatedClass.isInstance(thisGen))
			 {
			     if(!shownDialog)
				 {
				     int yesNo = JOptionPane.showConfirmDialog(null, 
									       "Perform in-place replacement of components?",
									       "Replace", 
									       JOptionPane.YES_NO_OPTION);
				     if(yesNo!=JOptionPane.YES_OPTION)
					 break;
				     else
					 shownDialog = true;
				 }
			     try{
				 TemporarySerializer tempSer = new TemporarySerializer(thisGen);
				 vElt.getElement().setElement(tempSer.deserialize());
				 vElt.disconnectSelf();
				 System.gc(); 
			     } catch(Exception e)
				 {
				     JOptionPane.showMessageDialog(null, "Warning: failed to replace class",
								   "Replace failed", JOptionPane.WARNING_MESSAGE);
				     keepGoing = false;
				 }
			     
			 }
		     
		 }
         } catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
     }


  public void evolve()
  {
   evolve(currentSelection);
  }

  public void evolve(Vector sel)
  {
           JFrame jf = new JFrame();
           jf.setSize(500,500);
           jf.getContentPane().add(new GeneticInterface(SignalCanvas.this, sel));
           jf.show();
  }


  private void startGlow()
    {
	glowThread = new GlowThread();
	glowThread.start();
    }

    private void updateGroups()
    {
        for(int i=0;i<visualElts.size();i++)
	    {
                VisualElement vElt = (VisualElement)(visualElts.get(i));
		if(vElt instanceof Group)
		    {
			Group grp = (Group)vElt;
			grp.setParent(this);
		    }
	    }
    }

  public void load()
  {
      makeUndoable();
        Vector temp = PersistentState.loadState();
        if(temp!=null)
        {


	    Vector fieldVector = new Vector();

	    fieldVector.add("Last modified");
	    fieldVector.add("Version no.");
	    fieldVector.add("Annotation");
	    
	    SignalHeader sigHeader = MasterClock.getHeader();
	    Vector valueVector = new Vector();

	    valueVector.add(sigHeader.date);
	    valueVector.add(""+sigHeader.verNo);
	    valueVector.add(sigHeader.annotation);

	    infoShown = true;
	    infoDisplay = new InfoDisplay(this, 200, 200, fieldVector, valueVector, 
					  "File "+MasterClock.getFilename()); 
	    	    
	    visualElts = temp;
	    updateGroups();
	    System.gc();
	    repaint();
	    flashString="Loaded";
	    startGlow();
        }


  }


    public void merge()
    {
      makeUndoable();
        Vector temp = PersistentState.loadState();
        if(temp!=null)
        {
	    for(int i=0;i<temp.size();i++)
		{
		    VisualElement vElt = (VisualElement)(temp.get(i));
		    if(vElt instanceof Group)
			{
			    Group grp = (Group)vElt;
			    grp.setParent(this);
			}

		}
	    visualElts.addAll(temp);
	    System.gc();
	    repaint();
	    flashString="Merged";
	    startGlow();
        }


  }

    public boolean isFocusTraversable()
    {
	return true;
    }

 private class GlowThread extends Thread
 {
     public void run()
     {
	 glowPhase = 14;
	 while(glowPhase>0)
	     {
		 try{Thread.sleep(25);} catch(InterruptedException ie){}
		 glowPhase--;    
		 repaint();
	     }
     }
 }

 SignalCanvas()
 {
  super();
  setRequestFocusEnabled(true);
  visualElts = new Vector();
  addMouseListener(new canvasListen());
  addMouseMotionListener(new canvasMotionListen());
  addKeyListener(new KeyListen());
  undoBuffer = new LinkedList();
  redoBuffer = new LinkedList();
  flashBar = new FlashBar(new Rectangle(0,0, 800,700), this);

  
 }

 private void drawSigil(Graphics g, Dimension dSize)
 {

     int armThick = 20;
     int armWidth = 250;
     int armXCross = 80;
     int totalWidth = armWidth*2-armXCross;
     int armHeight = 250;
     int armYCross = 160;
     int totalHeight = armHeight*2-armYCross;
     int xOffset = (dSize.width-totalWidth)/2;
     int yOffset = (dSize.height-totalHeight)/2;

     Polygon arm1 = new Polygon();
     Polygon arm2 = new Polygon();

     arm1.addPoint(xOffset+armThick*2, yOffset+armHeight/2);
     arm1.addPoint(xOffset+armWidth-armThick/2, yOffset+armThick);
     arm1.addPoint(xOffset+armWidth-armThick/2, yOffset);
     arm1.addPoint(xOffset, yOffset+armHeight/2);
     arm1.addPoint(xOffset+armWidth-armThick/2, yOffset+armHeight);
     arm1.addPoint(xOffset+armWidth-armThick/2, yOffset+armHeight-armThick);

     arm2.addPoint(xOffset+(armWidth-armThick*2), yOffset+armHeight/2);
     arm2.addPoint(xOffset+armWidth-(armWidth-armThick/2), yOffset+(armHeight-armThick));
     arm2.addPoint(xOffset+armWidth-(armWidth-armThick/2), yOffset+armHeight);
     arm2.addPoint(xOffset+armWidth, yOffset+armHeight/2);
     arm2.addPoint(xOffset+armWidth-(armWidth-armThick/2), yOffset);
     arm2.addPoint(xOffset+armWidth-(armWidth-armThick/2), yOffset+armThick);

     arm2.translate(armWidth-armXCross, armHeight-armYCross);
     Color outLineColor = new Color(glowPhase*9, glowPhase*9, glowPhase*9); 
     Color fillColor = new Color(glowPhase*8, glowPhase*8, glowPhase*8);
     g.setColor(fillColor);
     g.fillPolygon(arm1);
     g.setColor(outLineColor);
     g.drawPolygon(arm1);
    
     g.setColor(fillColor);
     g.fillPolygon(arm2);
     g.setColor(outLineColor);
     g.drawPolygon(arm2);

     g.setFont(new Font("SansSerif", Font.PLAIN, 16));
     g.setColor(new Color(glowPhase*10,glowPhase*3,glowPhase*3));
     int strWidth = g.getFontMetrics().stringWidth(flashString);
     g.drawString(flashString, xOffset+(totalWidth-strWidth)/2, yOffset+totalHeight/2-5);
 }


    public void update(Graphics g)
    {
	paint(g);
    }

 public void paint(Graphics g)
 {
  Dimension dSize = getSize();

  g.setColor(Color.black);
  g.fillRect(0,0,dSize.width,dSize.height);
  ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  if(glowPhase>0)
    drawSigil(g, dSize);

  for(int i=0;i<visualElts.size();i++)
  {
   VisualElement vElt=  ((VisualElement)(visualElts.get(i)));
   vElt.paintConnections(g);
  }


  if(curDrag!=null && shiftDrag)
  {
   curDrag.paintDrag(g,dragPt);
  }

  for(int i=0;i<visualElts.size();i++)
  {
    VisualElement vElt=  ((VisualElement)(visualElts.get(i)));
   vElt.paint(g);

  }

  g.setColor(new Color(50,50,150,180));
  if(drawSelRect!=null)
     g.drawRect(drawSelRect.x, drawSelRect.y, drawSelRect.width, drawSelRect.height);



  if(devShown && glowPhase == 0)
  {
   devSel.paint(g);

  }

  if(infoShown && infoDisplay != null)
      infoDisplay.paint(g);

  if(flashBar!=null && glowPhase == 0)
    flashBar.paint(g);
 }


}
