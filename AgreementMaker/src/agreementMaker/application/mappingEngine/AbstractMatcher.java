package agreementMaker.application.mappingEngine;

import java.util.ArrayList;
import java.util.Iterator;

import agreementMaker.AMException;
import agreementMaker.Utility;
import agreementMaker.application.Core;
import agreementMaker.application.mappingEngine.referenceAlignment.ReferenceEvaluationData;
import agreementMaker.application.ontology.Node;
import agreementMaker.application.ontology.Ontology;
import agreementMaker.userInterface.ProgressDialog;

import java.awt.Color;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;


public abstract class AbstractMatcher extends SwingWorker<Void, Void> implements Matcher {
	
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
	/**Variables needed to calculate execution time, executionTime = (end - start)/ unitMeasure, start has to be init in beforeAlign() and end in aferSelect()*/
	protected long start;
	protected long end;
	protected long executionTime;
	/**Keeps info about reference evaluation of the matcher. is null until the algorithm gets evaluated*/
	protected ReferenceEvaluationData refEvaluation;
	/**Graphical color for nodes mapped by this matcher and alignments, this value is set by the MatcherFactory and modified  by the table so a developer just have to pass it as aparameter for the constructor*/
	protected Color color; 

	
	/**This enum is for the matching functions that take nodes as an input.  Because we are comparing two kinds of nodes (classes and properties), we need to know the kind of nodes we are comparing in order to lookup up the input similarities in the corrent matrix */
	protected enum alignType {
		aligningClasses,
		aligningProperties
	}
	
	protected ProgressDialog progressDialog = null;  // need to keep track of the dialog in order to close it when we're done.  (there could be a better way to do this, but that's for later)
	protected int stepsTotal; // Used by the ProgressDialog.  This is a rough estimate of the number of steps to be done before we finish the matching.
	protected int stepsDone;  // Used by the ProgressDialog.  This is how many of the total steps we have completed.
	
	protected String report = "";
	
	protected ProgressMonitor progressMonitor;
	
	//protected boolean aborted = false;
	
	/**
	 * The constructor must be a Nullary Constructor
	 */
	public AbstractMatcher() {  // index and name will be set by the Matcher Factory
		isAutomatic = true;
		needsParam = false;
		isShown = true;
		modifiedByUser = false;
		threshold = 0.75;
		maxSourceAlign = 1;
		maxTargetAlign = ANY_INT;
		alignClass = true;
		alignProp = true;
		minInputMatchers = 0;
		maxInputMatchers = 0;
		//ALIGNMENTS LIST MUST BE NULL UNTIL THEY ARE CALCULATED
		sourceOntology = Core.getInstance().getSourceOntology();
		targetOntology = Core.getInstance().getTargetOntology();
		inputMatchers = new ArrayList<AbstractMatcher>();
	}
	
	//***************************ALL METHODS TO PERFORM THE ALIGNMENT**********************************
	/**
	 * Match(), buildSimilarityMatrix() and select() are the only 3 public methods to be accessed by the system other then get and set methods
	 * All other methods must be protected so that only subclasses may access them (can't be private because subclasses wouldn't be able to use them)
	 * match method is the one which perform the alignment. It also invokes the select() to scan and select matchings
	 * the system sometimes may need to invoke only the select method for example when the user changes threshold of an algorithm, it's not needed to invoke the whole matching process but only select
	 * so at least those two methods must be implemented and public
	 * both methods contains some empty methods to allow developers to add other code if needed
	 * In all cases a developer can override the whole match method or use this one and override the methods inside, or use all methods except for alignTwoNodes() which is the one which perform the real aligment evaluation
	 * and it has to be different
	 * 
	 */
	
    public void match() throws Exception {
    	matchStart();
    	buildSimilarityMatrices();
    	select();	
    	matchEnd();
    	//System.out.println("Classes alignments found: "+classesAlignmentSet.size());
    	//System.out.println("Properties alignments found: "+propertiesAlignmentSet.size());
    }
    
	/**
	 * Match(), buildSimilarityMatrix() and select() are the only 3 public methods to be accessed by the system other then get and set methods
	 * All other methods must be protected so that only subclasses may access them (can't be private because subclasses wouldn't be able to use them)
	 * match method is the one which perform the alignment. It also invokes the select() to scan and select matchings
	 * the system sometimes may need to invoke only the select method for example when the user changes threshold of an algorithm, it's not needed to invoke the whole matching process but only select
	 * so at least those two methods must be implemented and public
	 * both methods contains some empty methods to allow developers to add other code if needed
	 * In all cases a developer can override the whole match method or use this one and override the methods inside, or use all methods except for alignTwoNodes() which is the one which perform the real aligment evaluation
	 * and it has to be different
	 * It should not be needed often to override the buildSimilarityMatrix(), if you do remember to reinitialize structures everytime at the beginning, and at the end matrices should be filled completely in all cells
	 * It useful to invoke this method instead of the whole matching process while developing a composed matcher which uses other matchers matrices but which doesn't need the alignments sets.
	 * Instead of invoking the whole match() method of each matcher, it can only invoke this one, to avoid the selection process delay.
	 */
	public void buildSimilarityMatrices()throws Exception{
    	beforeAlignOperations();//Template method to allow next developer to add code before align
    	align();
    	afterAlignOperations();//Template method to allow next developer to add code after align
    }


	/**
	 * Match(), buildSimilarityMatrix() and select() are the only 3 public methods to be accessed by the system other then get and set methods
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
    	//this method is also invoked everytime the user change threshold or num relation in the table
    	beforeSelectionOperations();//Template method to allow next developer to add code after selection
    	selectAndSetAlignments();	
    	afterSelectionOperations();//Template method to allow next developer to add code after selection
    }
    
    //***************EMPTY TEMPLATE METHODS TO ALLOW USER TO ADD HIS OWN CODE****************************************

    //reset structures, this is important because anytime we invoke the match() for the secondtime (when we change some values in the table for example)
    //we have to reset all structures. It's the first method that is invoked, when overriding call super.beforeAlignOperations(),
    //IMPORTANT it also takes care of initializing time
	protected void beforeAlignOperations()  throws Exception{
    	classesMatrix = null;
    	propertiesMatrix = null;
    	modifiedByUser = false;
	}
    //TEMPLATE METHOD TO ALLOW DEVELOPERS TO ADD CODE: call super when overriding
    protected void afterAlignOperations()  {}
    //RESET ALIGNMENT STRUCTURES,     //TEMPLATE METHOD TO ALLOW DEVELOPERS TO ADD CODE: call super when overriding
    protected void beforeSelectionOperations() {
    	classesAlignmentSet = null;
    	propertiesAlignmentSet = null;
    	refEvaluation = null;
    }
    //TEMPLATE METHOD TO ALLOW DEVELOPERS TO ADD CODE: call super when overriding
    protected void afterSelectionOperations() {
    	
    } 
    
    //Time calculation, if you override this method remember to call super.afterSelectionOperations()
    protected void matchStart() {
    	if( isProgressDisplayed() ) setupProgress();  // if we are using the progress dialog, setup the variables
    	start = System.nanoTime();
	}
    //Time calculation, if you override this method remember to call super.afterSelectionOperations()
	protected void matchEnd() {
		// TODO: Need to make sure this timing is correct.  - Cosmin ( Dec 17th, 2008 )
		end = System.nanoTime();
    	executionTime = (end-start)/1000000; // this time is in milliseconds.
	    setSuccesfullReport();	
		if( isProgressDisplayed() ) allStepsDone();
    	
	}
    //***************INTERNAL METHODS THAT CAN BE USED BY ANY ABSTRACTMATCHER******************************************
	
	//***************ALIGNING PHASE*****************//

    protected void align() throws Exception {
    	
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

    protected AlignmentMatrix alignProperties(ArrayList<Node> sourcePropList, ArrayList<Node> targetPropList) throws Exception {
		return alignNodesOneByOne(sourcePropList, targetPropList);
	}

    protected AlignmentMatrix alignClasses(ArrayList<Node> sourceClassList, ArrayList<Node> targetClassList)  throws Exception{
		return alignNodesOneByOne(sourceClassList, targetClassList);
	}
	
    protected AlignmentMatrix alignNodesOneByOne(ArrayList<Node> sourceList, ArrayList<Node> targetList) throws Exception {
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
				if( isProgressDisplayed() ) stepDone(); // we have completed one step
			}
			if( isProgressDisplayed() ) updateProgress(); // update the progress dialog, to keep the user informed.
		}
		return matrix;
	}

    protected Alignment alignTwoNodes(Node source, Node target) throws Exception {
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
	
	//***************SELECTION PHASE*****************//
    
    protected void selectAndSetAlignments() {
    	if(alignClass) {
    		classesAlignmentSet = scanMatrix(classesMatrix);
    	}
    	if(alignProp) {
    		propertiesAlignmentSet = scanMatrix(propertiesMatrix);
    	}
	}

    private AlignmentSet scanMatrix(AlignmentMatrix matrix) {
    	int columns = matrix.getColumns();
    	int rows = matrix.getRows();
    	// at most each source can be aligned with all targets (columns) it's the same of selecting ANY for source
		int realSourceRelations = Math.min(maxSourceAlign, columns);
		// at most each target can be aligned with all sources (rows) it's the same of selecting ANY for target
		int realTargetRelations = Math.min(maxTargetAlign, rows);
		
		
		if(realSourceRelations == columns && realTargetRelations == rows) { //ANY TO ANY
			return getThemAll(matrix);
		}
		else if(realSourceRelations != columns && realTargetRelations == rows) { //N - ANY that includes also 1-ANY
			//AT LEAST ONE OF THE TWO CONSTRAINTs IS ANY, SO WE JUST HAVE TO PICK ENOUGH MAX VALUES TO SATISFY OTHER CONSTRAINT 
			return scanForMaxValuesRows(matrix, realSourceRelations);
		}
		else if( realSourceRelations == columns && realTargetRelations != rows) {//ANY-N that includes also ANY-1
			//AT LEAST ONE OF THE TWO CONSTRAINTs IS ANY, SO WE JUST HAVE TO PICK ENOUGH MAX VALUES TO SATISFY OTHER CONSTRAINT 
			return scanForMaxValuesColumns(matrix, realTargetRelations);
    	}
    	else {
			//Both constraints are different from ANY //all cases like 1-1 1-3 or 5-4 or 30-6
    		return scanWithBothConstraints(matrix, realSourceRelations,realTargetRelations);
    		
    		/*
			if(realSourceRelations == 1 && realTargetRelations == 1) {//1-1 mapping
				//TO BE DEVELOPED USING MAX WEIGHTED MATCHING ON BIPARTITE GRAPH, SOLVED USING DAJKSTRA
				//right now we use a non optimal greedy algorithm
				return scanWithBothConstraints(matrix, realSourceRelations,realTargetRelations);
			}
			else { //all cases like 1-3 or 5-4 or 30-6
				return scanWithBothConstraints(matrix, realSourceRelations,realTargetRelations);
			}
			*/
		}
	}


	


    protected AlignmentSet scanForMaxValuesRows(AlignmentMatrix matrix, int numMaxValues) {
		AlignmentSet aset = new AlignmentSet();
		Alignment toBeAdded;
		//temp structure to keep the first numMaxValues best alignments for each source
		//when maxRelations are both ANY we could have this structure too big that's why we have checked this case in the previous method
		Alignment[] maxAlignments;
		for(int i = 0; i<matrix.getRows();i++) {
			maxAlignments = matrix.getRowMaxValues(i, numMaxValues);
			//get only the alignments over the threshold
			for(int e = 0;e < maxAlignments.length; e++) { 
				toBeAdded = maxAlignments[e];
				if(toBeAdded.getSimilarity() >= threshold) {
					aset.addAlignment(toBeAdded);
				}
			}
		}
		return aset;
	}
    

    
    protected AlignmentSet scanForMaxValuesColumns(AlignmentMatrix matrix,int numMaxValues) {
		AlignmentSet aset = new AlignmentSet();
		Alignment toBeAdded;
		//temp structure to keep the first numMaxValues best alignments for each source
		//when maxRelations are both ANY we could have this structure too big that's why we have checked this case in the previous method
		Alignment[] maxAlignments;
		for(int i = 0; i<matrix.getColumns();i++) {
			maxAlignments = matrix.getColMaxValues(i, numMaxValues);
			//get only the alignments over the threshold
			for(int e = 0;e < maxAlignments.length; e++) { 
				toBeAdded = maxAlignments[e];
				if(toBeAdded.getSimilarity() >= threshold) {
					aset.addAlignment(toBeAdded);
				}
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

    protected AlignmentSet scanWithBothConstraints(AlignmentMatrix matrix, int sourceConstraint,int targetConstraint) {
    	
    	
    	IntDoublePair fakePair = IntDoublePair.createFakePair();
    	int rows = matrix.getRows();
    	int cols = matrix.getColumns();
    	
    	AlignmentSet aset = new AlignmentSet();

    	//I need to build a copy of the similarity matrix to work on it, i just need the similarity values
    	//and i don't need values higher than threshold so i'll just set them as fake so they won't be selected
    	double[][] workingMatrix = new double[rows][cols];
    	double sim;
    	for(int i = 0; i < rows; i++) {
    		for(int j = 0; j < cols; j++) {
    			sim = matrix.get(i,j).getSimilarity();
    			if(sim >= threshold)
    				workingMatrix[i][j] = sim;
    			else workingMatrix[i][j] = IntDoublePair.fake;
    		}
    	}
    	
    	//for each source (row) i need to find the SourceConstraint best values
    	//for each maxvalue i need to remember the similarity value and the index of the correspondent column
    	//we init it all to (-1,-1)
    	IntDoublePair[][] rowsMaxValues = new IntDoublePair[matrix.getRows()][sourceConstraint];
    	for(int i= 0; i < rows; i++) {
    		for(int j = 0 ; j < sourceConstraint; j++) {
    			rowsMaxValues[i][j] = fakePair;
    		}
    	}
    	
    	//for each target (column) i need to find the targetConstraint best values
    	//for each maxvalue i need to remember the similarity value and the index of the correspondent column
    	//we init it all to (-1,-1)
    	IntDoublePair[][] colsMaxValues = new IntDoublePair[matrix.getColumns()][targetConstraint];
    	for(int i= 0; i < cols; i++) {
    		for(int j = 0 ; j < targetConstraint; j++) {
    			colsMaxValues[i][j] = fakePair;
    		}
    	}
    	
    	IntDoublePair maxPairOfRow = null;
    	IntDoublePair prevMaxPairOfCol = null;
    	IntDoublePair newMaxPairOfCol = null;
    	
    	//we must continue until the situation is stable
    	//if is a 3-4 mapping it means that we can find at most three alignments for each source and 4 for each target, but not always we can find all
    	boolean somethingChanged = true;
    	while(somethingChanged) {
    		somethingChanged = false;
    		
        	for(int i = 0; i < rows; i++) {
        		
        		//if I haven't found all best alignments for this row
        		if(rowsMaxValues[i][0].isFake()) {
        			
        			//I need to get the max of this row, that is ok also for the column
        			// so the max of this row must be higher the the max previously selected for that column
        			//this do while ends if i find one or if I don't find any so all the cells are fake and the maximum selected is fake too
        			do {
        				//get the max value for this row and the associated column index
                		maxPairOfRow = Utility.getMaxOfRow(workingMatrix, i);

                		if(maxPairOfRow.isFake()) {
                			break; //all the value of these lines are fake
                		}
                		else {
                    		//the minimum of the best values for the column corrisponding to this max
                    		prevMaxPairOfCol = colsMaxValues[maxPairOfRow.index][0];
                    		
                			//and i have to set that matrix value to fake so that that row won't select again that value
                			workingMatrix[i][maxPairOfRow.index] = IntDoublePair.fake;
                		}
        			}
                    while(maxPairOfRow.value <= prevMaxPairOfCol.value);
        			
        			//I don't need the workingMatrix anymore
        			//workingMatrix = null;
        			
            		//if my value is higher than than the minimum of the best values for this column
            		//this value becomes one of the best values and the minimum one is discarded
        			//so if the previous while ended because of the while condition not the break one
        			if(!maxPairOfRow.isFake()) {
        			
            			somethingChanged = true;
            			
            			//this value will be one of the best for this column and row, i had to them and update order.
            			//prevMaxPairOfCol is not anymore one of the best values for this column
            			//i'll switch it with the new one, but i also have to remove it from the best values of his row putting a fake one in it.
            			//i also have to modify the matrix so that that row won't select that max again.
            			newMaxPairOfCol = new IntDoublePair(i,maxPairOfRow.value);
            			colsMaxValues[maxPairOfRow.index][0] = newMaxPairOfCol;
            			//reorder that array of best values of this column to have minimum at the beginning
            			//we have to move the first element to get the right position
            			Utility.adjustOrderPairArray(colsMaxValues[maxPairOfRow.index],0);
            			
            			//the max of this row found must be added to the best values for this row, and then order the array,
            			rowsMaxValues[i][0] = maxPairOfRow;
            			//we have to move the first element to get the right position
            			Utility.adjustOrderPairArray(rowsMaxValues[i],0);
            			
            			
            			if(!prevMaxPairOfCol.isFake()) {
                			//the prev best values has to be removed also from that row best values so i have to find it and set it to fake and reorder
            				for(int k = 0; k < rowsMaxValues[prevMaxPairOfCol.index].length; k++) {
            					if(rowsMaxValues[prevMaxPairOfCol.index][k].index == maxPairOfRow.index) {
            						rowsMaxValues[prevMaxPairOfCol.index][k] = fakePair;
                        			Utility.adjustOrderPairArray(rowsMaxValues[prevMaxPairOfCol.index], k);
            						break;
            					}
            				}
            			}
            		}
        		}
        	}
    	}
    	
    	/*FOR DEBUGGING
    	for(int i = 0; i < rows; i++) {
    		for(int j = 0; j < cols; j++) {
    			System.out.print(workingMatrix[i][j]+" ");
    		}
    		System.out.println("");
    	}
    	*/
    	
    	//now we have the alignments into rowMaxValues
    	IntDoublePair toBeAdded;
    	for(int i = 0; i < rows; i++) {
    		for(int j = 0; j < sourceConstraint; j++) {
    			toBeAdded = rowsMaxValues[i][j];
    			if(!toBeAdded.isFake()) {
        			aset.addAlignment(matrix.get(i,toBeAdded.index));
    			}
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
		//This method must create and return the AbstractMatcherParameter subclass so that the user can select additional parameters needed by the matcher
		//if the matcher doesn't need any parameter then the attribute needsParameters must be false and this method won't be invoked.
		return parametersPanel;
		//You may need to override this method to pass some more information to the panel, in that case instead of initializing the panel in the constructor 
		//you will have to override this method this way: "return new MyParameterPanel(with some more parameters); (see manualCombinationMatcher structure
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

	public ReferenceEvaluationData getRefEvaluation() {
		return refEvaluation;
	}

	public void setRefEvaluation(ReferenceEvaluationData evaluation) {
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
	
	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	
	public void setInputMatchers(ArrayList<AbstractMatcher> inputMatchers) {
		this.inputMatchers = inputMatchers;
	}
	//***********************MEthods used by the interface for some small tasks**************************
	
	/**
	 * Matcher details you can override this method to add or change you matcher details if needed, it is only invoked clicking on the button view details in the control panel
	 * @return a string with details of the matchers
	 */
	public String getDetails() {
		String s = "";
		s+= "Matcher: "+getName().getMatcherName()+"\n\n";
		s+= "Brief Description:\n\n";
		s += getDescriptionString()+"\n";
		s += "Characteristics\n\n";
		s += getAttributesString()+"\n";
		s += "References\n\n";
		s += getReferenceString()+"\n";
		return s;
	}
	
	public String getAttributesString() {
		String s = "";
		s+= "Additional parameters required: "+Utility.getYesNo(needsParam())+"\n";
		s+= "Min number of matchers in input: "+Utility.getStringFromNumRelInt(getMinInputMatchers())+"\n";
		s+= "Max number of matchers in input: "+Utility.getStringFromNumRelInt(getMaxInputMatchers())+"\n";
		s+= "Performs Classes alignment: "+Utility.getYesNo(isAlignClass())+"\n";
		s+= "Performs Properties alignment: "+Utility.getYesNo(isAlignProp())+"\n";
		return s;
	}
	/**
	 * All matchers should implement this method to return a description of the algorithm used, the String should finish with \n;
	 * @return
	 */
	public String getDescriptionString() {
		return "No description available for this matcher\n";
	}
	public String getReferenceString() {
		return "No references available for this matcher\n";
	}

	/**These 3 methods are invoked any time the user select a matcher in the matcherscombobox. Usually developers don't have to override these methods unless their default values are different from these.*/
	public double getDefaultThreshold() {
		// TODO Auto-generated method stub
		return 0.75;
	}
	
	/**These 3 methods are invoked any time the user select a matcher in the matcherscombobox. Usually developers don't have to override these methods unless their default values are different from these.*/
	public int getDefaultMaxSourceRelations() {
		// TODO Auto-generated method stub
		return 1;
	}

	/**These 3 methods are invoked any time the user select a matcher in the matcherscombobox. Usually developers don't have to override these methods unless their default values are different from these.*/
	public int getDefaultMaxTargetRelations() {
		// TODO Auto-generated method stub
		return ANY_INT;
	}
	
	/**This method is invoked at the end of the matching process if the process successed, to give a feedback to the user. Developers can ovveride it to add additional informations.
	 * Developers can also add a global variable like message that is set dinamically during the matching process to have different type of feedback.
	 * **/
	
	public void setSuccesfullReport() {
		report =  "Matching Process Complete Succesfully!\n\n";
		if(areClassesAligned()) {
			report+= "Classes alignments found: "+classesAlignmentSet.size()+"\n";
		}
		if(arePropertiesAligned()) {
			report+= "Properties alignments found: "+propertiesAlignmentSet.size()+"\n";
		}
		if(executionTime != 0) {
			report += "Total execution time (ms): "+executionTime+"\n";
		}
	}
	
	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}
	
	public String getAlignmentsStrings() {
		
		String result = "";
		result+= "Class Alignments: "+classesAlignmentSet.size()+"\n";
		result += "Source Concept\t--->\tTarget Concept\tSimilarity\tRelation\n\n";
		result += classesAlignmentSet.getStringList();
		result+= "Property Alignments: "+propertiesAlignmentSet.size()+"\n";
		result += "Source Concept\t--->\tTarget Concept\tSimilarity\tRelation\n\n";
		result += propertiesAlignmentSet.getStringList();
		return result;
	}
	
	public AbstractMatcher copy() throws Exception {
		AbstractMatcher cloned = MatcherFactory.getMatcherInstance(getName(), Core.getInstance().getMatcherInstances().size());
		cloned.setInputMatchers(getInputMatchers());
		cloned.setParam(getParam());
		cloned.setThreshold(getThreshold());
		cloned.setMaxSourceAlign(getMaxSourceAlign());
		cloned.setMaxTargetAlign(getMaxTargetAlign());
		cloned.setAlignClass(isAlignClass());
		cloned.setAlignProp(isAlignProp());
		cloned.match();
		return cloned;
		
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
  

	
	//****************** PROGRESS DIALOG METHODS *************************8
	
	
    /**
     * This function is used by the Progress Dialog, in order to invoke the matcher.
     * It's just a wrapper for match(). 
     */
	public Void doInBackground() throws Exception {
		try {
			//without the try catch, the exception got lost in this thread, and we can't debug
			match();
		}
		catch(AMException ex2) {
			report = ex2.getMessage();
			this.cancel(true);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			report = Utility.UNEXPECTED_ERROR;
			this.cancel(true);
		}
		return null;
	}
    
    /**
     * Function called by the worker thread when the matcher finishes the algorithm.
     */
    public void done() {
    	if( isProgressDisplayed() ) progressDialog.matchingComplete();  // when we're done, close the progress dialog
    }
	
    /**
     * Need to keep track of the progress dialog we have because right now, there is no button to close it, so we must make it close automatically.
     * @param p
     */
	public void setProgressDialog( ProgressDialog p ) {
		progressDialog = p;
	}
	
	/**
	 * This method sets up stepsDone and stepsTotal.  Override this method if you have a special way of computing the values.
	 * ( If you override this method, it's likely that you will also need to override alignNodesOneByOne(), because it calls stepDone() and updateProgress() ).
	 * @author Cosmin Stroe @date Dec 17, 2008
	 */
	protected void setupProgress() {
    	stepsDone = 0;
    	stepsTotal = 0;  // total number
    	if( alignClass ) {
    		int n = sourceOntology.getClassesList().size();
    		int m = targetOntology.getClassesList().size();
    		stepsTotal += n*m;  // total number of comparisons between the class nodes 
    	}
    	
    	if( alignProp ) {
    		int n = sourceOntology.getPropertiesList().size();
    		int m = targetOntology.getPropertiesList().size();
    		stepsTotal += n*m; // total number of comparisons between the properties nodes
    	}

    	// we have computed stepsTotal, and initialized stepsDone to 0.
	}

	/**
	 * We have just completed one step of the total number of steps.
	 * 
	 * Remember, stepsDone is used in conjunction with stepsTotal, in order to get 
	 * an idea of how much of the total task we have done ( % done = stepsDone / stepsTotal * 100 ).
	 * 
	 *  @author Cosmin Stroe @date Dec 17, 2008
	 */
	protected void stepDone() {
		stepsDone++;
	}


	/**
	 * Update the Progress Dialog with the current progress.
	 * 
	 *  @author Cosmin Stroe @date Dec 17, 2008
	 */
	protected void updateProgress() {
	
		float percent = ((float)stepsDone / (float)stepsTotal);
		int p = (int) (percent * 100);
		setProgress(p);  // this function does the actual work ( via the PropertyChangeListener )
		
	}
	
	/**
	 * If the matcher implemented by a developer doesn't take care of the progress
	 * for example it overrides the align method and doesn't write any code to increase steps
	 * in that case we have to force the progress to be 100% at the end
	 * It is used into matchEnd() method.
	 * For the same reason setupProgress is inside matchStart();
	 * Assuming that a developer shouldn't change match() or matchStart() or matchEnd()
	 */
	
	protected void allStepsDone() {
		if( stepsTotal <= 0 ) stepsTotal = 1; // avoid division by 0;
		if(stepsDone != stepsTotal) {
			stepsDone = stepsTotal;
			updateProgress();
		}
	}
	
	/**
	 * If a matcher invokes the match() method of another matcher internally, the internal matcher 
	 * won't have the progressDialog, and the globalstaticvariable may be still true
	 * so we have to check both conditions
	 */
	public boolean isProgressDisplayed() {
		return progressDialog != null;  // don't need to check for the global static variable, since if it's false, we should never have to call this function
	}





}
