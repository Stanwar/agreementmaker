package am.app.mappingEngine.parametricStringMatcher;

import am.Utility;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.AbstractMatcherParametersPanel;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.StringUtil.ISub;
import am.app.mappingEngine.StringUtil.Normalizer;
import am.app.mappingEngine.StringUtil.StringMetrics;
import am.app.ontology.Node;

import uk.ac.shef.wit.simmetrics.similaritymetrics.*; //all sim metrics are in here

public class ParametricStringMatcher extends AbstractMatcher { 


	private Normalizer normalizer;
	
	public ParametricStringMatcher() {
		// warning, param is not available at the time of the constructor
		super();
		needsParam = true;
	}
	
	
	public String getDescriptionString() {
		return "Performs a local matching using a String Based technique.\n" +
				"Different concept strings are considered in the process.\n" +
				"The user can select a different weight to each concept string\n" +
				"Strings are preprocessed with cleaning, stemming, stop-words removing, and tokenization techniques.\n" +
				"Users can also select preprocessing preferences.\n" +
				"Different String similarity techniques are available to compare preprocessed strings.\n" +
				"A similarity matrix contains the similarity between each pair (sourceNode, targetNode).\n" +
				"A selection algorithm select valid alignments considering threshold and number of relations per node.\n"; 
	}
	
	
	
	/* *******************************************************************************************************
	 ************************ Init structures*************************************
	 * *******************************************************************************************************
	 */
	
	
	public void beforeAlignOperations()  throws Exception{
		super.beforeAlignOperations();
		ParametricStringParameters parameters =(ParametricStringParameters)param;
		//prepare the normalizer to preprocess strings
		normalizer = new Normalizer(parameters.normParameter);
	}

	/* *******************************************************************************************************
	 ************************ Algorithm functions beyond this point*************************************
	 * *******************************************************************************************************
	 */

	public Alignment alignTwoNodes(Node source, Node target, alignType typeOfNodes) {
		double localSim = 0;
		double labelSim = 0;
		double commentSim = 0;
		double seeAlsoSim = 0;
		double isDefBySim = 0;
		double sim = 0;
		ParametricStringParameters parameters  = (ParametricStringParameters)param;
		//i need to use local varables for weights  to modify them in case of weights redistribution but without modifying global parameters
		double localWeight = parameters.localWeight;
		double labelWeight = parameters.labelWeight;
		double commentWeight = parameters.commentWeight; 
		double seeAlsoWeight = parameters.seeAlsoWeight; 
		double isDefinedByWeight = parameters.isDefinedByWeight; 
		
		//The redistrubution is implicit in the weighted average mathematical formula
		//i just need to put the weight equal to 0
		//but labels should be first redistributed to localnames and vice-versa
		//we set the weight of a feature to 0 if any of the two value is irrelevant
		if(parameters.redistributeWeights) {
			if(parameters.localWeight!=0){
				if(Utility.isIrrelevant(source.getLocalName()) || Utility.isIrrelevant(target.getLocalName())){
					//we should redistribute localname to label if label is relevant
					if(!Utility.isIrrelevant(source.getLabel()) && !Utility.isIrrelevant(target.getLabel())){
						labelWeight += localWeight;
					}
					localWeight = 0;
				}	
			}
			
			if(parameters.labelWeight!=0){
				if(Utility.isIrrelevant(source.getLabel()) || Utility.isIrrelevant(target.getLabel())){
					//we should redistribute label to localname if localname is relevant
					if(!Utility.isIrrelevant(source.getLocalName()) && !Utility.isIrrelevant(target.getLocalName())){
						localWeight+= labelWeight;
					}
					labelWeight = 0;
				}
			}

			if(parameters.commentWeight == 0 || Utility.isIrrelevant(source.getComment()) || Utility.isIrrelevant(target.getComment()))
				commentWeight = 0;
			if(parameters.seeAlsoWeight == 0 || Utility.isIrrelevant(source.getSeeAlsoLabel()) || Utility.isIrrelevant(target.getSeeAlsoLabel()))
				seeAlsoWeight = 0;
			if(parameters.isDefinedByWeight == 0 || Utility.isIrrelevant(source.getIsDefinedByLabel()) || Utility.isIrrelevant(target.getIsDefinedByLabel()))
				isDefinedByWeight = 0;			
		}
		
		double totWeight = localWeight + labelWeight + commentWeight + seeAlsoWeight + isDefinedByWeight; //important to get total after the redistribution
		if(totWeight > 0) {
			if(localWeight > 0) {
				localSim =  performStringSimilarity(source.getLocalName(), target.getLocalName());
				localSim *= localWeight;
			}
			if(labelWeight > 0) {
				labelSim =  performStringSimilarity(source.getLabel(), target.getLabel());
				labelSim *= labelWeight;
			}
			if(commentWeight > 0) {
				commentSim = performStringSimilarity(source.getComment(), target.getComment());
				commentSim *= commentWeight;
			}
			if(seeAlsoWeight > 0) {
				seeAlsoSim = performStringSimilarity(source.getSeeAlsoLabel(), target.getSeeAlsoLabel());
				seeAlsoSim *= seeAlsoWeight;
			}
			if(isDefinedByWeight > 0) {
				isDefBySim = performStringSimilarity(source.getIsDefinedByLabel(), target.getIsDefinedByLabel());
				isDefBySim *= isDefinedByWeight;
			}
			
			sim = localSim + labelSim + commentSim + seeAlsoSim + isDefBySim;
			//Weighted average, this normalize everything so also if the sum of  weights is not one, the value is always between 0 and 1. 
			//this also automatically redistribute 0 weights.
			sim /= totWeight; 
		}
		
		return new Alignment(source, target, sim);
		
	}
	
	
	private double performStringSimilarity(String sourceString, String targetString) {

		double sim = 0;
		if(sourceString == null || targetString == null )
			return 0; //this should never happen because we set string to empty string always
		
		else { //real string comparison
			ParametricStringParameters parameters  = (ParametricStringParameters)param;
			
			//PREPROCESSING
			String processedSource = normalizer.normalize(sourceString);
			String processedTarget = normalizer.normalize(targetString);
			
			//usually empty strings shouldn't be compared, but if redistrubute weights is not selected 
			//in the redistribute weights case this can't happen because the code won't arrive till here
			if(processedSource.equals("")) 
				if(processedTarget.equals(""))
					return 1;
				else return 0;
			else if(processedTarget.equals(""))
				return 0;
			
			//this could be done with registry enumeration techinque but is not worth it
			if(parameters.measure.equals(ParametricStringParameters.AMSUB)) {
				sim = StringMetrics.AMsubstringScore(processedSource,processedTarget);
			}
			else if(parameters.measure.equals(ParametricStringParameters.AMSUB_AND_EDIT)) {
				Levenshtein lv = new Levenshtein();
				double lsim = lv.getSimilarity(processedSource, processedTarget);
				double AMsim = StringMetrics.AMsubstringScore(processedSource,processedTarget);
				sim = (0.65*AMsim)+(0.35*lsim); 
			}
			else if(parameters.measure.equals(ParametricStringParameters.EDIT)) {
				Levenshtein lv = new Levenshtein();
				sim = lv.getSimilarity(processedSource, processedTarget);
			}
			else if(parameters.measure.equals(ParametricStringParameters.JARO)) {
				JaroWinkler jv = new JaroWinkler();
				sim =jv.getSimilarity(processedSource, processedTarget);
			}
			else if(parameters.measure.equals(ParametricStringParameters.QGRAM)) {
				QGramsDistance q = new QGramsDistance();
				sim = q.getSimilarity(processedSource, processedTarget);
			}
			else if(parameters.measure.equals(ParametricStringParameters.SUB)) {
				sim = StringMetrics.substringScore(processedSource,processedTarget);
			}
			else if(parameters.measure.equals(ParametricStringParameters.ISUB)) {
				sim = ISub.getSimilarity(processedSource,processedTarget);
			}
		}
		return sim;
	}
	
	public AbstractMatcherParametersPanel getParametersPanel() {
		if(parametersPanel == null){
			parametersPanel = new ParametricStringParametersPanel();
		}
		return parametersPanel;
	}
	      
}
