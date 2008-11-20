package agreementMaker.userInterface.vertex;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JOptionPane;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import agreementMaker.GSM;
import agreementMaker.application.mappingEngine.ContextMapping;
import agreementMaker.application.mappingEngine.DefnMapping;
import agreementMaker.application.mappingEngine.UserMapping;
import agreementMaker.application.ontology.Node;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Vertex class contains all the information about the node or vertex of the XML tree.
 * Some of the information include tbe name of the vertex, the location on the canvas,
 * if the vertex is visible, if it is mapped, and the actual mapping.
 * 
 * @author ADVIS Laboratory
 * @version 11/27/2004
 */
public class Vertex extends DefaultMutableTreeNode implements Serializable
{
	// instance variables 
	static int key =0;				// unique number assigned by the program
	static final long serialVersionUID = 1;
	private int arcHeight;			// arc height of the rectangular node
	private int arcWidth;			// arc width of the rectangular node
	protected ContextMapping contextMapping;	// context mapping 
	protected DefnMapping defnMapping = null; 
	private String description;		// description of the vertex
	private int height;				// height of the node/vertex
	private int id;					// assign the unique number here
	
	private OntModel ontModel;
	private String uri;
	private boolean isMapped;		// is the vertex mapped by the user
	private boolean isMappedByContext;	// is the vertex mapped by context
	private boolean isMappedByDef;		// is the vertex mapped by definition
	private boolean isSelected;		// is the node or vertex selected
	private boolean isVisible;		// is the node/vertex visible
	private String name;			// name of the vertex (may not be unique)
	private int nodeType;			// the type of node (global or local)
	//private VertexDescriptionPane vertexDescription; //stores the description of the OWL classes
	private int ontNode;		//the type of vertex; XML or OWL -> 0 for XML and 1 for OWL
	private String parentsDesc="";		// String the stores the parentsDesList
	private ArrayList parentsNameList;		// stores the names of the parents and grand parents of the vertex
	private ArrayList siblingsNameList;		// stores the names of siblings of the vertex
	private ArrayList parentsDescList;		// stores the Discriptions of the parents and grand parents of the vertex
	private ArrayList siblingsDescList;		// stores the Discriptions of siblings of the vertex
	private String parentsName= "";		// String the stores the parentsNameList
	private boolean shouldCollapse;	// keeps track if the node or vertex should collapse or not. 
	private String siblingsDesc = "";// String the stores the siblingsDescList
	private String siblingsName ="";	// String the stores the siblingsNameList
	protected UserMapping userMapping;			// user mapping class of this vertex
	private int width;				// width of the node/vertex
	private int x;					// coordinate x location on canvas
	
	private int x2; 				// coordinate x2 = x+width location on canvas
	private int y;					// coordinate y location on canvas
	private int y2; 				// coordinate y2 = y+width location on canvas	    
	
	private Node node;    //the node containing the ontology information of this vertex, there is a double link from the node to the vertex and from the vertex to the node.
	
	/*******************************************************************************************
	 /**
	  * Constructor for objects of class Vertex
	  * @param name 	name of the vertex
	  */
	public Vertex(String name)
	{
		
		super(name);
		// initialize instance variables
		setID(key++);
		setName(name);
		setDesc("");
		this.ontModel = null;
		setIsMapped(false);
		setIsMappedByContext(false);
		setIsMappedByDef(false);
		setX(-1);
		setX2(-1);
		setY(-1);
		setY2(-1);
		setWidth(-1);
		setHeight(-1);
		setArcWidth(-1);
		setArcHeight(-1);
		setIsVisible(true);
		isSelected = false;
		setNodeType(-1);
		setOntNode(GSM.XMLFILE);
		setShouldCollapse(false);
		userMapping = new UserMapping();
		//vertexDescription = (VertexDescriptionPane)jDescriptionPanel;
	}
	public Vertex(String name, String uri, OntModel m) {
		
		super(name);
		
		// initialize instance variables
		setID(key++);
		setName(name);
		setDesc("");
		this.uri = uri;
		setIsMapped(false);
		setIsMappedByContext(false);
		setIsMappedByDef(false);
		setX(-1);
		setX2(-1);
		setY(-1);
		setY2(-1);
		setWidth(-1);
		setHeight(-1);
		setArcWidth(-1);
		setArcHeight(-1);
		setIsVisible(true);
		isSelected = false;
		setNodeType(-1);
		setOntNode(GSM.ONTFILE);
		setShouldCollapse(false);
		userMapping = new UserMapping();
		//vertexDescription = (VertexDescriptionPane)jDescriptionPanel;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns arcHeight of the node
	  *
	  * @return  arcHeight	arc height of the vertex
	  */
	public int getArcHeight()
	{
		return arcHeight;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns arcWidth of the node
	  *
	  * @return  arcWidth	arc width of the vertex
	  */
	public int getArcWidth()
	{
		return arcWidth;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns description
	  * 
	  * @return  description		description of the vertex
	  */
	public String getDesc()	{
		return description;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns height of the node
	  *
	  * @return  height	height of the vertex
	  */
	public int getHeight()
	{
		return height;
	}
	public String getHorizontalDescs(){
		return siblingsDesc;
	}
	public String getHorizontalNames(){
		return siblingsName;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns key
	  * 
	  * @return  id	unique identifier of the vertex
	  */
	public int getID()
	{
		return id;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns true if vertex is mapped else false
	  * 
	  * @return  isMapped 		boolean value indicating if the vertex is mapped or not
	  */
	public boolean getIsMapped()
	{
		return isMapped;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns true if vertex is mapped by context else false
	  * 
	  * @return  isMapped 		boolean value indicating if the vertex is mapped by context or not
	  */
	public boolean getIsMappedByContext()
	{
		return isMappedByContext;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns true if vertex is mapped by defintion else false
	  * 
	  * @return  isMapped 		boolean value indicating if the vertex is mapped by definition or not
	  */
	public boolean getIsMappedByDef()
	{
		return isMappedByDef;
	}	
	/********************************************************************************************
	 /**
	  * Accessor method which returns isSelected
	  * 
	  * @return isSelected	boolean value which indicates if the vertex is selected or not
	  */
	public boolean getIsSelected()
	{
		return isSelected;
	}
	/**
	 * Accessor method which returns name of the vertex
	 * 
	 * @return name	name of the vertex
	 */
	public String getName()
	{
		return name;
	}	
	/*******************************************************************************************
	 /**
	  * Accessor method which returns nodeType
	  * 
	  * @return  nodeType	node type indicating if the node is global (1) or local (2) 
	  */
	public int getNodeType()
	{
		return nodeType;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns description for OWL Class vertices
	  * 
	  * @return  description		description of the vertex
	  */
	public String getOWLDesc()	{
		return OntVertexDescription.getVertexDescription(this.name,this.ontModel);		
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns OWLNode
	  * 
	  * @return  OWLNode	 indicating if the node is for XML file or OWL file 
	  */
	public int getOntNode()	{
		return ontNode;
	}
	/********************************************************************************************
	 /**
	  * Accessor method which returns shouldCollapse
	  * 
	  * @return shouldCollapse	boolean value indicating if the vertex should collapse or not
	  */
	public boolean getShouldCollapse()
	{
		return shouldCollapse;
	}
	public String getVerticalDescs(){
		return parentsDesc;
	}
	public String getVerticalNames(){
		return parentsName;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns width of the node
	  *
	  * @return  width	width of the vertex
	  */
	public int getWidth()
	{
		return width;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns x value
	  *
	  * @return  x		x location of the top left corner of vertex
	  */
	public int getX()
	{
		return x;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns x2 value
	  *
	  * @return  x2		x location of the bottom right corner of vertex
	  */
	public int getX2()
	{
		return x2;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns y value
	  *
	  * @return  y		y location of the top left corner of vertex
	  */
	public int getY()
	{
		return y;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns y2 value
	  *
	  * @return  y2		y location of the bottom right corner of vertex
	  */
	public int getY2()
	{
		return y2;
	}
	/*******************************************************************************************
	 /**
	  * Accessor method which returns isVisible
	  * 
	  * @return  isVisible 	boolean value which indicates if the vertex if visible or not
	  */
	public boolean isVisible()
	{
		return isVisible;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets archeight
	  *
	  * @param  ah 	height of arc
	  */
	public void setArcHeight(int ah)
	{
		arcHeight = ah;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets arcWidth
	  *
	  * @param  aw 	width of the arc
	  */
	public void setArcWidth(int aw)
	{
		arcWidth= aw;
	}	
	/*******************************************************************************************
	 /**
	  * Modifier method which sets description of the vertex
	  * 
	  * @param  desc		description of the vertex
	  */
	public void setDesc(String desc)
	{
		description = desc;
	}
	public OntModel getOntModel() {
		return ontModel;
	}
	public void setOntModel(OntModel ontModel) {
		this.ontModel = ontModel;
	}
	/*******************************************************************************************
	 /**
	  * Modifier method which sets description of the vertex if it is for OWL class
	  * 
	  * @param  pCls		the OWL named class
	  */
	/*public void setDesc(String name, OWLModel ontModel)
	 {
	 vertexDescription = new VertexDescriptionPane(name, ontModel);
	 }	*/
	/********************************************************************************************
	 /**
	  * Modifier method which sets height
	  * 
	  * @param  h 	height of node 
	  */
	public void setHeight(int h)
	{
		height = h;
	}	
	/**
	 * Modifier method which sets unique id of vertex
	 * 
	 * @param  num  unique identifier
	 */
	public void setID(int num)
	{
		id = num;
	}
	/**
	 * Modifier method which sets the boolean isMapped to true or false
	 * 
	 * @param  mapped 	boolean value which indicates if the vertex is mapped
	 */
	public void setIsMapped(boolean mapped)
	{
		isMapped = mapped;
	}
	/*******************************************************************************************
	 /**
	  * Modifier method which sets the boolean isMappedByContext to true or false
	  * 
	  * @param  mapped 	boolean value which indicates if the vertex is mapped by context
	  */
	public void setIsMappedByContext(boolean mapped)
	{
		isMappedByContext = mapped;
	}
	/*******************************************************************************************
	 /**
	  * Modifier method which sets the boolean isMappedByDef to true or false
	  * 
	  * @param  mapped 	boolean value which indicates if the vertex is mapped by definition
	  */
	public void setIsMappedByDef(boolean mapped)
	{
		isMappedByDef = mapped;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets isSelected
	  * 
	  * @param  selected 	boolean value indicating if the vertex is selected
	  */
	public void setIsSelected(boolean selected)
	{
		isSelected = selected;
		
	}
	/*******************************************************************************************
	 /**
	  * Modifier method which sets isVisible
	  * 
	  * @param  visible 	boolean value which indicates if the vertex is visible
	  */
	public void setIsVisible(boolean visible)
	{
		isVisible = visible;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets name of vertex
	  * 
	  * @param  tempName 	name of the vertex 
	  */
	public void setName(String tempName)
	{
		name = tempName;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets nodeType
	  * 
	  * @param  type 	integer value indicating if the vertex is global  (1) or local (2)
	  */
	public void setNodeType(int type)
	{
		nodeType = type;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which set OWLNode
	  * 
	  * @param  type 	integer value indicating if the vertex is for XML file or OWL file
	  */
	public void setOntNode(int type)
	{
		ontNode = type;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets shouldCollapse
	  * 
	  * @param  collapse	boolean value indicating if the vertex should collapse
	  */
	public void setShouldCollapse(boolean collapse)
	{
		shouldCollapse = collapse;
	}
	/********************************************************************************************
	 /**
	  * Sets the parents and siblings variables
	  * 
	  * @param  num  unique identifier
	  */
	public void setVerticalHorizontal()
	{
		
		TreeNode[] pathToRoot = this.getPath();
		String[] parents;
		String[] descs;
		parents = new String[pathToRoot.length];
		descs = new String[pathToRoot.length];
		for(int i=pathToRoot.length-1; i>1; i--){ // from this node to the root, the node itself name is not added, and also the ontology name and the word "Source/Target ontology" are not considered
			parents[i] = ((Vertex) pathToRoot[i]).getName();
			descs[i] = ((Vertex) pathToRoot[i]).getDesc();
			
			if(parents[i] != null && !getName().equals(parents[i])){
				parentsName += parents[i] + " | ";
				parentsDesc += descs[i] + " | ";
				//JOptionPane.showMessageDialog(null, "Node name: " + getName() + "\n parentsName: " + parentsName);
			}
		}
		
//		JOptionPane.showMessageDialog(null,parentsName + " \nDISC: " + parentsDesc);
//		WGS
		Vertex parent = (Vertex) getParent();
		Vertex temp;
		if(parent != null){
			if(parent.getChildCount() != 0){
				
				for (Enumeration e = parent.children() ; e.hasMoreElements(); ) {
					temp = (Vertex)e.nextElement();
					if(temp.getName() != getName()){
						siblingsName += temp.getName() + " | ";
						siblingsDesc += temp.getDesc() + " | ";
					}
				}
				
				
			}
		}
		//JOptionPane.showMessageDialog(null,siblingsName + " \nDISC: " + siblingsDesc);
		
		/*
		 parentsNameList = new ArrayList();
		 parentsDescList = new ArrayList();
		 
		 
		 Vertex temp = this;
		 do{
		 if(temp.getParent() == null ) break;
		 parentsNameList.add( (String) ((Vertex)(temp.getParent())).getName() );
		 parentsDescList.add( (String) ((Vertex)(temp.getParent())).getDesc() );
		 temp = (Vertex) temp.getParent();
		 } while (temp != null);
		 
		 for(int i=0; i<parentsNameList.size();i++){
		 parentsName += (String)parentsNameList.get(i) +" ";
		 parentsDesc += (String)parentsDescList.get(i) +" ";
		 }
		 
		 JOptionPane.showMessageDialog(null,parentsName + " \nDISC: " + parentsDesc);
		 */
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets width
	  * 
	  * @param  w 	width of node 
	  */
	public void setWidth(int w)
	{
		width = w;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets x value
	  * 
	  * @param  xvalue 	x location of the top left corner
	  */
	public void setX(int xvalue)
	{
		x = xvalue;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets x2 value
	  * 
	  * @param  x2value 	x location of the bottom right corner
	  */
	public void setX2(int x2value)
	{
		x2 = x2value;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets y value
	  * 
	  * @param  yvalue 	y location of the top left corner
	  */
	public void setY(int yvalue)
	{
		y = yvalue;
	}
	/********************************************************************************************
	 /**
	  * Modifier method which sets y2 value
	  * 
	  * @param  y2value	y location of the bottom right corner 
	  */
	public void setY2(int y2value)
	{
		y2 = y2value;
	}
	/**
	 * @return Returns the contextMapping.
	 */
	public ContextMapping getContextMapping() {
		return contextMapping;
	}
	/**
	 * @param contextMapping The contextMapping to set.
	 */
	public void setContextMapping(ContextMapping contextMapping) {
		this.contextMapping = contextMapping;
	}
	/**
	 * @return Returns the defnMapping.
	 */
	public DefnMapping getDefnMapping() {
		return defnMapping;
	}
	
	/**
	 * @author cosmin 
	 * @date Oct 12, 2008
	 */
	public void clearDefnMapping() {
		// set defnMapping = null, which will remove any references to the
		// definition that was there before, and garbage collection will destroy it
		// (hopefully (if there's no other references to it in the program))
		defnMapping = null;
		isMappedByDef = false; // this vertex is no longer mapped by definition
	}
	
	/**
	 * @author cosmin
	 * @date Oct 17, 2008
	 */
	public void clearContextMapping() {
		contextMapping = null;
		isMappedByContext = false;
	}
	
	/**
	 * @param defnMapping The defnMapping to set.
	 */
	public void setDefnMapping(DefnMapping defnMapping) {
		this.defnMapping = defnMapping;
	}
	/**
	 * @return Returns the userMapping.
	 */
	public UserMapping getUserMapping() {
		return userMapping;
	}
	/**
	 * @param userMapping The userMapping to set.
	 */
	public void setUserMapping(UserMapping userMapping) {
		this.userMapping = userMapping;
	}
	/**
	 * @return Returns the uri.
	 */
	public String getUri() {
		return this.uri;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	
	public boolean isFake() {
		return node == null;
	}
}
