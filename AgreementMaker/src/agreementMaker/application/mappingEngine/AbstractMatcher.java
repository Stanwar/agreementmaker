package agreementMaker.application.mappingEngine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import agreementMaker.Utility;
import agreementMaker.application.Core;
import agreementMaker.application.ontology.Node;
import agreementMaker.application.ontology.Ontology;
import java.awt.Color;

public abstract class AbstractMatcher implements Matcher{
	
	/**Unique identifier of the algorithm used in the JTable list as index
	 * if an algorithm gets deleted we have to decrease the index of all others by one
	 * */
	protected int index;
	/**Name of the algorithm, there should be also a final static String in the instance class
	 * in the constructor of the non-abstract class should happen "name = FINALNAME"
	 * */
	protected MatchersRegistry name;
	/**User mapping should be the only one with this variable equal to false*/
	protected boolean isAutomatic;
	/**True if the algorithm needs additional parameter other than threshold, in this case the developer must develop a JFrame to let the user define them*/
	protected boolean needsParam;
	/**Parameter of this method, if needsParam this item will be generated by the userinterface if not this will be automatically generated with default values*/
	protected AbstractParameters param;
	/**Developer will have to instantiate this object in the constructor with real parameterpanel for the method which needs param, if a method don't needs param you don't have to touch this attribute*/
	protected AbstractMatcherParametersPanel parametersPanel;
	/**True means that AM should show its alignments*/
	protected boolean isShown;
	protected boolean modifiedByUser;
	protected double threshold;
	
	/**ANY means any numer of relations for source or target*/
	public final static int ANY_INT = Integer.MAX_VALUE;
	protected int maxSourceAlign;
	protected int maxTargetAlign;
	
	/**Contain alignments, NULL if alignment has not been calculated*/
	protected AlignmentSet propertiesAlignmentSet;
	protected AlignmentSet classesAlignmentSet;
	
	/**Structure containing similarity values between classes nodes, matrix[source][target]
	 * should not be accessible outside of this class, the system should only be able to access alignments sets
	 * */
	protected AlignmentMatrix classesMatrix;
	/**Structure containing similarity values between classes nodes, matrix[source][target]*/
	protected AlignmentMatrix propertiesMatrix;
	
	/**Reference to the Core istances*/
	protected Ontology sourceOntology;
	protected Ontology targetOntology;
	
	/**If the algo calculates prop alignments*/
	protected boolean alignProp;
	/**If the algo calculates prop alignments*/
	protected boolean alignClass;
	/***Some algorithms may need other algorithms as input*/
	protected ArrayList<AbstractMatcher> inputMatchers;
	/**Minum and maximum number of input matchers
	 * a generic matcher which doesn't need any inputs should have 0, 0
	 * */
	protected int minInputMatchers;
	protected int maxInputMatchers;
	/**Keeps info about reference evaluation of the matcher. is null until the algorithm gets evaluated*/
	protected ResultData refEvaluation;
	/**Graphical color for nodes mapped by this matcher and alignments, this value is set by the MatcherFactory and modified  by the table so a developer just have to pass it as aparameter for the constructor*/
	protected Color color; 
	
	/**This enum is for the matching functions that take nodes as an input.  Because we are comparing two kinds of nodes (classes and properties), we need to know the kind of nodes we are comparing in order to lookup up the input similarities in the corrent matrix */
	protected enum alignType {
		aligningClasses,
		aligningProperties
	}
	
	
	/**
	 * The constructor must be a Nullary Constructor
	 */
	public AbstractMatcher() {  // index and name will be set by the Matcher Factory
		isAutomatic = true;
		needsParam = false;
		isShown = true;
		modifiedByUser = false;
		threshold = 0.75;
		maxSourceAlign = ANY_INT;
		maxTargetAlign = 1;
		alignClass = true;
		alignProp = true;
		minInputMatchers = 0;
		maxInputMatchers = 0;
		//ALIGNMENTS LIST MUST BE NULL UNTIL THEY ARE CALCULATED
		sourceOntology = Core.getInstance().getSourceOntology();
		targetOntology = Core.getInstance().getTargetOntology();
		inputMatchers = new ArrayList<AbstractMatcher>();
	}
	
	/**
	 * Create - used by the matcher factory
	 */
	public AbstractMatcher create( int key, MatchersRegistry theName ) {
		throw new RuntimeException("DEVELOPER:  You must implement a create() function for your Matcher.");
	}
	
	//***************************ALL METHODS TO PERFORM THE ALIGNMENT**********************************
	/**
	 * Match() and select() are the only two public methods to be accessed by the system other then get and set methods
	 * All other methods must be protected so that only subclasses may access them (can't be private because subclasses wouldn't be able to use them)
	 * match method is the one which perform the alignment. It also invokes the select() to scan and select matchings
	 * the system sometimes may need to invoke only the select method for example when the user changes threshold of an algorithm, it's not needed to invoke the whole matching process but only select
	 * so at least those two methods must be implemented and public
	 * both methods contains some empty methods to allow developers to add other code if needed
	 * In all cases a developer can override the whole match method or use this one and override the methods inside, or use all methods except for alignTwoNodes() which is the one which perform the real aligment evaluation
	 * and it has to be different
	 * 
	 */
    public void match() {
    	beforeAlignOperations();//Template method to allow next developer to add code before align
    	align();
    	afterAlignOperations();//Template method to allow next developer to add code after align
    	select();	
    	//System.out.println("Classes alignments found: "+classesAlignmentSet.size());
    	//System.out.println("Properties alignments found: "+propertiesAlignmentSet.size());
    }
    
	/**
	 * Match() and select() are the only two public methods to be accessed by the system other then get and set methods
	 * All other methods must be protected so that only subclasses may access them (can't be private because subclasses wouldn't be able to use them)
	 * match method is the one which perform the alignment. It also invokes the select() to scan and select matchings
	 * the system sometimes may need to invoke only the select method for example when the user changes threshold of an algorithm, it's not needed to invoke the whole matching process but only select
	 * so at least those two methods must be implemented and public
	 * both methods contains some empty methods to allow developers to add other code if needed
	 * In all cases a developer can override the whole match method or use this one and override the methods inside, or use all methods except for alignTwoNodes() which is the one which perform the real aligment evaluation
	 * and it has to be different
	 * It should not be needed often to override the select(), in all cases remember to consider all selection parameters threshold, num relations per source and target.
	 */
    public void select() {
    	beforeSelectionOperations();//Template method to allow next developer to add code after selection
    	selectAndSetAlignments();	
    	afterSelectionOperations();//Template method to allow next developer to add code after selection
    }
    
    //***************EMPTY TEMPLATE METHODS TO ALLOW USER TO ADD HIS OWN CODE****************************************

    //reset structures, this is important because anytime we invoke the match() for the secondtime (when we change some values in the table for example)
    //we have to reset all structures. It's the first method that is invoked, when overriding call super.beforeAlignOperations()
	protected void beforeAlignOperations() {
    	classesMatrix = null;
    	propertiesMatrix = null;
    	modifiedByUser = false;
	}
    //DO NOTHING FOR NOW
    protected void afterAlignOperations() {}
    //RESET ALIGNMENT STRUCTURES
    protected void beforeSelectionOperations() {
    	classesAlignmentSet = null;
    	propertiesAlignmentSet = null;
    	refEvaluation = null;
    }
    //DO nothing for now
    protected void afterSelectionOperations() {}
    
    
    //***************INTERNAL METHODS THAT CAN BE USED BY ANY ABSTRACTMATCHER******************************************

    protected void align() {

		if(alignClass) {
			ArrayList<Node> sourceClassList = sourceOntology.getClassesList();
			ArrayList<Node> targetClassList = targetOntology.getClassesList();
			classesMatrix = alignClasses(sourceClassList,targetClassList );	
			//classesMatrix.show();
		}
		if(alignProp) {
			ArrayList<Node> sourcePropList = sourceOntology.getPropertiesList();
			ArrayList<Node> targetPropList = targetOntology.getPropertiesList();
			propertiesMatrix = alignProperties(sourcePropList, targetPropList );					
		}

	}

    protected AlignmentMatrix alignProperties(ArrayList<Node> sourcePropList, ArrayList<Node> targetPropList) {
		return alignNodesOneByOne(sourcePropList, targetPropList);
	}

    protected AlignmentMatrix alignClasses(ArrayList<Node> sourceClassList, ArrayList<Node> targetClassList) {
		return alignNodesOneByOne(sourceClassList, targetClassList);
	}
	
    protected AlignmentMatrix alignNodesOneByOne(ArrayList<Node> sourceList, ArrayList<Node> targetList) {
		AlignmentMatrix matrix = new AlignmentMatrix(sourceList.size(), targetList.size());
		Node source;
		Node target;
		Alignment alignment; //Temp structure to keep sim and relation between two nodes, shouldn't be used for this purpose but is ok
		for(int i = 0; i < sourceList.size(); i++) {
			source = sourceList.get(i);
			for(int j = 0; j < targetList.size(); j++) {
				target = targetList.get(j);
				alignment = alignTwoNodes(source, target);
				matrix.set(i,j,alignment);
			}
		}
		return matrix;
	}

    protected Alignment alignTwoNodes(Node source, Node target) {
		//TO BE IMPLEMENTED BY THE ALGORITHM, THIS IS JUST A FAKE ABSTRACT METHOD
		double sim;
		String rel = Alignment.EQUIVALENCE;
		if(source.getLocalName().equals(target.getLocalName())) {
			sim = 1;
		}
		else {
			sim = 0;
		}
		return new Alignment(source, target, sim, rel);
	}
	
    protected void selectAndSetAlignments() {
		if(maxSourceAlign == 1 && maxTargetAlign == 1) {
			//TO BE DEVELOPED USING MAX WEIGHTED MATCHING ON BIPARTITE GRAPH, SOLVED USING DAJKSTRA
			scanForMaxValues();//TO BE CHANGED
		}
		else if(maxSourceAlign != ANY_INT && maxTargetAlign != ANY_INT) {
			//TO BE DEVELOPED: I DON'T KNOW YET HOW TO DO THIS
			scanForMaxValues();//TO BE CHANGED
		}
		else {//AT LEAST ONE OF THE TWO CONSTRAINTs IS ANY, SO WE JUST HAVE TO PICK ENOUGH MAX VALUES TO SATISFY OTHER CONSTRAINT 
			scanForMaxValues();
		}
	}

    protected void scanForMaxValues() {
		if(alignClass) {
			classesAlignmentSet = scanForMaxValuesMatrix(classesMatrix);
		}
		if(alignProp) {
			propertiesAlignmentSet = scanForMaxValuesMatrix(propertiesMatrix);
		}
	}
	
    protected AlignmentSet scanForMaxValuesMatrix(AlignmentMatrix matrix){
		AlignmentSet aset;
		int tempMaxSource = maxSourceAlign; //I could have used MaxSourceAlign directly but i modify them in this method while i will need them again later
		int tempMaxTarget = maxTargetAlign;
		int numMaxValues;
		//IF both values are ANY we can have at most maxSourceRelations equals to the target nodes and maxTargetRelations equal to source node
		if(maxTargetAlign == ANY_INT  && maxSourceAlign == ANY_INT) {
			aset = getThemAll(matrix);
		}
		else if(tempMaxTarget >= tempMaxSource) {//Scan rows and then columns
			numMaxValues = tempMaxSource;
			aset = scanForMaxValuesRowColumn(matrix, numMaxValues);
		}
		else {//scan column and then row
			numMaxValues = tempMaxTarget;
			aset = scanForMaxValuesColumnRow(matrix, numMaxValues);
		}
		return aset;
	}

    protected AlignmentSet scanForMaxValuesRowColumn(AlignmentMatrix matrix, int numMaxValues) {
		AlignmentSet aset = new AlignmentSet();
		Alignment currentValue;
		Alignment currentMax;
		//temp structure to keep the first numMaxValues best alignments for each source
		//when maxRelations are both ANY we could have this structure too big that's why we have checked this case in the previous method
		Alignment[] maxAlignments;
		for(int i = 0; i<matrix.getRows();i++) {
			maxAlignments = new Alignment[numMaxValues];
			for(int h = 0; h<maxAlignments.length;h++) {
				maxAlignments[h] = new Alignment(-1); //intial max alignments have sim equals to 0
			}
			for(int j = 0; j<matrix.getColumns();j++) {
				currentValue = matrix.get(i,j);
				currentMax = maxAlignments[0];
				for(int k = 0;currentValue.getSimilarity() >= currentMax.getSimilarity() ; k++) {
					maxAlignments[k] = currentValue;
					currentValue = currentMax;
					if(k+1 < maxAlignments.length) {
						currentMax = maxAlignments[k+1];
					}
					else break;
				}
			}
			currentValue = maxAlignments[0];
			for(int e = 0;currentValue.getSimilarity() >= threshold; e++) {
				aset.addAlignment(currentValue);
				if(e+1 < maxAlignments.length)
					currentValue = maxAlignments[e+1];
				else break;
				//System.out.println(currentValue);
			}
		}
		return aset;
	}

    protected AlignmentSet scanForMaxValuesColumnRow(AlignmentMatrix matrix,int numMaxValues) {
		AlignmentSet aset = new AlignmentSet();
		Alignment currentValue;
		Alignment currentMax;
		//temp structure to keep the first numMaxValues best alignments for each source
		//when maxRelations are both ANY we could have this structure too big that's why we have checked this case in the previous method
		Alignment[] maxAlignments;
		for(int i = 0; i<matrix.getColumns();i++) {
			maxAlignments = new Alignment[numMaxValues];
			for(int h = 0; h<maxAlignments.length;h++) {
				maxAlignments[h] = new Alignment(-1); //intial max alignments have sim equals to 0
			}
			for(int j = 0; j<matrix.getRows();j++) {
				currentValue = matrix.get(j,i);
				currentMax = maxAlignments[0];
				for(int k = 0;currentValue.getSimilarity() >= currentMax.getSimilarity() ; k++) {
					maxAlignments[k] = currentValue;
					currentValue = currentMax;
					if(k+1 < maxAlignments.length) {
						currentMax = maxAlignments[k+1];
					}
					else break;
				}
			}
			currentValue = maxAlignments[0];
			for(int e = 0;currentValue.getSimilarity() >= threshold; e++) {
				aset.addAlignment(currentValue);
				if(e+1 < maxAlignments.length)
					currentValue = maxAlignments[e+1];
				else break;
				//System.out.println(currentValue);
			}
		}
		return aset;
	}
    
    protected AlignmentSet getThemAll(AlignmentMatrix matrix) {
		AlignmentSet aset = new AlignmentSet();
		Alignment currentValue;
		for(int i = 0; i<matrix.getColumns();i++) {
			for(int j = 0; j<matrix.getRows();j++) {		
				currentValue = matrix.get(j,i);
				if(currentValue.getSimilarity() >= threshold)
					aset.addAlignment(currentValue);
			}
		}
		return aset;
	}

    //*****************USER ALIGN METHOD*****************************
    
	public void addManualAlignments(ArrayList<Alignment> alignments) {
		Iterator<Alignment> it = alignments.iterator();
		Alignment al;
		while(it.hasNext()) {
			al = it.next();
			if(al.getEntity1().isClass() && al.getEntity2().isClass()) {
				if(alignClass) {
					addManualClassAlignment(al) ;
				}
			}
			else if(al.getEntity1().isProp() && al.getEntity2().isProp()) {
				if(alignProp) {
					addManualPropAlignment(al) ;
				}
			}
		}
		select();
		modifiedByUser = true;
	}
		
    public void addManualClassAlignment(Alignment a) {
    	addManualAlignment(a, classesMatrix);
    }

    public void addManualPropAlignment(Alignment a) {
    	addManualAlignment(a, propertiesMatrix);
    }
    
    public void addManualAlignment(Alignment a, AlignmentMatrix matrix) {
    	matrix.set(a.getEntity1().getIndex(), a.getEntity2().getIndex(), a);
    }
    
	//*****************SET AND GET methods ******************************************
	public AbstractMatcherParametersPanel getParametersPanel() {
		return parametersPanel;
		//This method must create and return the AbstractMatcherParameter subclass so that the user can select additional parameters needed by the matcher
		//if the matcher doesn't need any parameter then the attribute needsParameters must be false and this method won't be invoked.
	}
	
	public AlignmentSet getAlignmentSet() {
    	AlignmentSet aligns = new AlignmentSet();
    	if(areClassesAligned()) {
    		aligns.addAll(classesAlignmentSet);
    	}
    	if(arePropertiesAligned()) {
    		aligns.addAll(propertiesAlignmentSet);
    	}
    	return aligns;
    }

    public AlignmentSet getClassAlignmentSet() {
    	return classesAlignmentSet;
    }

    public AlignmentSet getPropertyAlignmentSet() {
    	return propertiesAlignmentSet;
    }
    /**AgreementMaker doesn't calculate instances matching, if you add this you should also modify getAlignmenSet*/
    public AlignmentSet getInstanceAlignmentSet() {
    	throw new RuntimeException("trying to invoking a function not implemented yet");
    }
    
    public boolean areClassesAligned() {
    	return classesAlignmentSet != null;
    }
    
   
    public boolean arePropertiesAligned() {
    	return propertiesAlignmentSet != null;
    }
    
    public boolean isSomethingAligned() {
    	return areClassesAligned() || arePropertiesAligned();
    }
    
    public int getNumberClassAlignments() {
    	int numAlign = 0;
		if(areClassesAligned()) {
			numAlign += getClassAlignmentSet().size();
		}
		return numAlign;
    }
    
    public int getNumberPropAlignments() {
    	int numAlign = 0;
		if(arePropertiesAligned()) {
			numAlign += getPropertyAlignmentSet().size();
		}
		return numAlign;
    }
    
    public int getTotalNumberAlignments() {
    	return getNumberClassAlignments()+getNumberPropAlignments();
    }

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public MatchersRegistry getName() {
		return name;
	}

	public void setName(MatchersRegistry name) {
		this.name = name;
	}

	public boolean isAutomatic() {
		return isAutomatic;
	}

	public void setAutomatic(boolean isAutomatic) {
		this.isAutomatic = isAutomatic;
	}

	public boolean needsParam() {
		return needsParam;
	}

	public void setNeedsParam(boolean needsParam) {
		this.needsParam = needsParam;
	}

	public AbstractParameters getParam() {
		return param;
	}

	public void setParam(AbstractParameters param) {
		this.param = param;
	}

	public boolean isShown() {
		return isShown;
	}

	public void setShown(boolean isShown) {
		this.isShown = isShown;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getMaxSourceAlign() {
		return maxSourceAlign;
	}

	public void setMaxSourceAlign(int maxSourceAlign) {
		this.maxSourceAlign = maxSourceAlign;
	}

	public int getMaxTargetAlign() {
		return maxTargetAlign;
	}

	public void setMaxTargetAlign(int maxTargetAlign) {
		this.maxTargetAlign = maxTargetAlign;
	}

	public int getMinInputMatchers() {
		return minInputMatchers;
	}

	public void setMinInputMatchers(int minInputMatchers) {
		this.minInputMatchers = minInputMatchers;
	}

	public int getMaxInputMatchers() {
		return maxInputMatchers;
	}

	public void setMaxInputMatchers(int maxInputMatchers) {
		this.maxInputMatchers = maxInputMatchers;
	}

	public ArrayList<AbstractMatcher> getInputMatchers() {
		return inputMatchers;
	}
    
	public void addInputMatcher(AbstractMatcher a) {
		inputMatchers.add(a);
	}

	public boolean isModifiedByUser() {
		return modifiedByUser;
	}

	public void setModifiedByUser(boolean modifiedByUser) {
		this.modifiedByUser = modifiedByUser;
	}

	public ResultData getRefEvaluation() {
		return refEvaluation;
	}

	public void setRefEvaluation(ResultData evaluation) {
		this.refEvaluation = evaluation;
	}
	
	public boolean isRefEvaluated() {
		return refEvaluation != null;
	}

	public boolean isAlignProp() {
		return alignProp;
	}

	public boolean isAlignClass() {
		return alignClass;
	}

	public void setAlignProp(boolean alignProp) {
		this.alignProp = alignProp;
	}

	public void setAlignClass(boolean alignClass) {
		this.alignClass = alignClass;
	}
	
	public AlignmentMatrix getClassesMatrix() {
		return classesMatrix;
	}

	public AlignmentMatrix getPropertiesMatrix() {
		return propertiesMatrix;
	}
	
	/**
	 * Matcher details you can override this method to add or change you matcher details if needed, it is only invoked clicking on the button view details in the control panel
	 * @return a string with details of the matchers
	 */
	public String getDetails() {
		// TODO Auto-generated method stub
		String s = "";
		s+= "Matcher: "+getName().getMatcherName()+"\n\n";
		s+= "Additional parameters required: "+Utility.getYesNo(needsParam())+"\n";
		s+= "Min number of matchers in input: "+getMinInputMatchers()+"\n";
		s+= "Max number of matchers in input: "+getMaxInputMatchers()+"\n";
		s+= "Performs Classes alignment: "+Utility.getYesNo(isAlignClass())+"\n";
		s+= "Performs Properties alignment: "+Utility.getYesNo(isAlignProp())+"\n";
		return s;
	}

//*************************UTILITY METHODS**************************************
	public boolean equals(Object o) {
		if(o instanceof AbstractMatcher) {
			AbstractMatcher a = (AbstractMatcher)o;
			return a.getIndex() == this.getIndex();
		}
		return false;
	}
	
	public int hashCode() {
		return index;
	}

	
	public Color getColor() {
		return color;
	}
	

	public void setColor(Color color) {
		this.color = color;
	}




}
