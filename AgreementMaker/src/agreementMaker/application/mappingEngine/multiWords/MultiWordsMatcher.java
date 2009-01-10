package agreementMaker.application.mappingEngine.multiWords;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import com.wcohen.ss.api.StringWrapper;

import agreementMaker.Utility;
import agreementMaker.application.mappingEngine.AbstractMatcher;
import agreementMaker.application.mappingEngine.Alignment;
import agreementMaker.application.mappingEngine.AlignmentMatrix;
import agreementMaker.application.mappingEngine.StringUtil.AMStringWrapper;
import agreementMaker.application.mappingEngine.StringUtil.Normalizer;
import agreementMaker.application.ontology.Node;
import agreementMaker.userInterface.vertex.Vertex;

import uk.ac.shef.wit.simmetrics.similaritymetrics.*; //all sim metrics are in here
import simpack.measure.weightingscheme.StringTFIDF;

public class MultiWordsMatcher extends AbstractMatcher { 


	private Normalizer normalizer;
	private ArrayList<String> sourceClassDocuments = new ArrayList<String>();
	private ArrayList<String> targetClassDocuments = new ArrayList<String>();
	private ArrayList<String> sourcePropDocuments = new ArrayList<String>();
	private ArrayList<String> targetPropDocuments = new ArrayList<String>();
	
	private ArrayList<StringWrapper> classCorpus = new ArrayList<StringWrapper>();
	private ArrayList<StringWrapper> propCorpus = new ArrayList<StringWrapper>();
	
	public MultiWordsMatcher() {
		// warning, param is not available at the time of the constructor
		super();
		needsParam = true;
		parametersPanel = new MultiWordsParametersPanel();
	}
	
	
	public String getDescriptionString() {
		return "Performs a local matching using a Multi words String Based technique.\n" +
				"Different concept and neighbouring strings are considered in the process.\n" +
				"A multi words string is built and preprocessed with cleaning, stemming, stop-words removing, and tokenization techniques.\n" +
				"Differnt token based vector space similarity techniques are available to compare preprocessed strings.\n" +
				"A similarity matrix contains the similarity between each pair (sourceNode, targetNode).\n" +
				"A selection algorithm select valid alignments considering threshold and number of relations per node.\n"; 
	}
	
	
	
	/* *******************************************************************************************************
	 ************************ Init structures*************************************
	 * *******************************************************************************************************
	 */
	
	
	public void beforeAlignOperations()  throws Exception{
		super.beforeAlignOperations();
		MultiWordsParameters parameters =(MultiWordsParameters)param;
		//prepare the normalizer to preprocess strings
		normalizer = new Normalizer(parameters.normParameter);
		
		if(alignClass) {
			//Class corpus is the list of documents from source and target. Each node consists of one document containing many terms: localname, label, all terms from comment and so on...
			sourceClassDocuments = createDocumentsFromNodeList(sourceOntology.getClassesList());
			targetClassDocuments = createDocumentsFromNodeList(targetOntology.getClassesList());
			classCorpus = new ArrayList<StringWrapper>();
		
			//Create the corpus of documents
			//the TFIDF requires a corpus that is the list of total documents
			//each node consist of one document
			//Each document string must be wrapped in a StringWrapper
			Iterator<String> it = sourceClassDocuments.iterator();
			 while(it.hasNext()) {
				 String s = it.next();
				 AMStringWrapper sw = new AMStringWrapper(s);
				 classCorpus.add(sw);
			 }
			 it = targetClassDocuments.iterator();
			 while(it.hasNext()) {
				 String s = it.next();
				 AMStringWrapper sw = new AMStringWrapper(s);
				 classCorpus.add(sw);
			 }
		}
		
		if(alignProp) {
			sourcePropDocuments = createDocumentsFromNodeList(sourceOntology.getPropertiesList());
			targetPropDocuments = createDocumentsFromNodeList(targetOntology.getPropertiesList());
			propCorpus = new ArrayList<StringWrapper>();
			
			//Create the corpus of documents
			//the TFIDF requires a corpus that is the list of total documents
			//each node consist of one document
			//Each document string must be wrapped in a StringWrapper
			Iterator<String> it = sourcePropDocuments.iterator();
			 while(it.hasNext()) {
				 String s = it.next();
				 AMStringWrapper sw = new AMStringWrapper(s);
				 propCorpus.add(sw);
			 }
			 it = targetPropDocuments.iterator();
			 while(it.hasNext()) {
				 String s = it.next();
				 AMStringWrapper sw = new AMStringWrapper(s);
				 propCorpus.add(sw);
			 }
		}
		
			

	}
	
	private ArrayList<String> createDocumentsFromNodeList(ArrayList<Node> nodeList) {
		ArrayList<String> documents = new ArrayList<String>();
		Iterator<Node> it = nodeList.iterator();
		while(it.hasNext()) {
			Node node = it.next();
			String document = createMultiWordsString(node) ;
			String normDocument = normalizer.normalize(document);
			documents.add(normDocument);
		}
		return documents;
	}
	
	private String createMultiWordsString(Node node) {
		
		String multiWordsString = "";
		MultiWordsParameters mp = (MultiWordsParameters)param;
		
		//Add concept strings to the multiwordsstring
		multiWordsString = Utility.smartConcat(multiWordsString, getLabelAndOrNameString(node));
		multiWordsString = Utility.smartConcat(multiWordsString, node.getComment());
		multiWordsString = Utility.smartConcat(multiWordsString, node.getSeeAlso());
		multiWordsString = Utility.smartConcat(multiWordsString, node.getIsDefinedBy());
		
		//add neighbors strings
		if(mp.considerNeighbors) {
			ArrayList<Vertex> duplicateList = node.getVertexList();
			
			//add child strings
			Vertex mainVertex = duplicateList.get(0);
			String childstring = "";
			Enumeration children = mainVertex.children();
			while(children.hasMoreElements()) {
				Vertex childVertex = (Vertex) children.nextElement();
				Node childNode = childVertex.getNode();
				String neighbourString = getLabelAndOrNameString(childNode);
				childstring = Utility.smartConcat(childstring, neighbourString);
			}
			multiWordsString = Utility.smartConcat(multiWordsString, childstring);
			
			//for each father add father strings and create hashSet of siblings
			String parentsString = "";
			HashSet<Node> siblingNodes = new HashSet<Node>();
			
			for(int i = 0; i < duplicateList.size(); i++) {

				Vertex duplicateVertex = duplicateList.get(i);
				Vertex parentVertex = (Vertex)duplicateVertex.getParent();
				if(parentVertex!= null && !parentVertex.isFake()) {
					Node parentNode = parentVertex.getNode();
					String neighbourString = getLabelAndOrNameString(parentNode);;
					parentsString = Utility.smartConcat(parentsString, neighbourString);
					//create hashSet
					Enumeration siblings = parentVertex.children();
					while(siblings.hasMoreElements()) {
						Vertex sibVertex = (Vertex) siblings.nextElement();
						Node sibNode = sibVertex.getNode();
						if(!sibNode.equals(node))//aggiungo tutti i fratelli tranne me, i duplicati non vengono aggiunti perche � un hashset
							siblingNodes.add(sibNode);
					}
				}
				
			}
			multiWordsString = Utility.smartConcat(multiWordsString, parentsString);
			
			//add sibling string from the hashSet, i need to use hashset to avoid adding duplicates.
			Iterator it = siblingNodes.iterator();
			String siblingsString = "";
			while(it.hasNext()) {
				Node sibNode = (Node)it.next();
				String neighbourString = getLabelAndOrNameString(sibNode);
				siblingsString = Utility.smartConcat(siblingsString, neighbourString);
			}
			multiWordsString = Utility.smartConcat(multiWordsString, siblingsString);
		}
		
		//add instances strings
		if(mp.considerInstances) {
			String instancesString = "";
			Iterator<String> it = node.getIndividuals().iterator();
			while(it.hasNext()) {
				String ind = it.next();
				instancesString = Utility.smartConcat(instancesString, ind);
			}
			multiWordsString = Utility.smartConcat(multiWordsString, instancesString);
			
		}
		return multiWordsString;
	}

	private String getLabelAndOrNameString(Node node) {
		String result = "";
		MultiWordsParameters mp = (MultiWordsParameters)param;
		//Add concept strings to the multiwordsstring
		if(!mp.ignoreLocalNames) { 
			//localname sometimes are just irrelevant codes so this boolean value should be false
			//often are equal to label so label and local must be considered once
				if(!node.getLocalName().equalsIgnoreCase(node.getLabel())) {
					result = Utility.smartConcat(result, node.getLocalName());
				}
			}
			result = Utility.smartConcat(result, node.getLabel());
		return result;
	}


	/* *******************************************************************************************************
	 ************************ Algorithm functions beyond this point*************************************
	 * *******************************************************************************************************
	 */

	// overriding the abstract method in order to keep track of what kind of nodes we are aligning
    protected AlignmentMatrix alignProperties(ArrayList<Node> sourcePropList, ArrayList<Node> targetPropList) {
		return alignNodesOneByOne(sourcePropList, targetPropList, alignType.aligningProperties );
	}

	// overriding the abstract method in order to keep track of what kind of nodes we are aligning
    protected AlignmentMatrix alignClasses(ArrayList<Node> sourceClassList, ArrayList<Node> targetClassList) {
		return alignNodesOneByOne(sourceClassList, targetClassList, alignType.aligningClasses);
	}
	
	// this method is exactly similar to the abstract method, except we pass one extra parameters to the alignTwoNodes function
    protected AlignmentMatrix alignNodesOneByOne(ArrayList<Node> sourceList, ArrayList<Node> targetList, alignType typeOfNodes) {
		AlignmentMatrix matrix = new AlignmentMatrix(sourceList.size(), targetList.size());
		Node source;
		Node target;
		Alignment alignment; //Temp structure to keep sim and relation between two nodes, shouldn't be used for this purpose but is ok
		for(int i = 0; i < sourceList.size(); i++) {
			source = sourceList.get(i);
			for(int j = 0; j < targetList.size(); j++) {
				target = targetList.get(j);
				alignment = alignTwoNodes(source, target, typeOfNodes);
				matrix.set(i,j,alignment);
			}
		}
		return matrix;
	}





	public Alignment alignTwoNodes(Node source, Node target,alignType typeOfNodes) {
		MultiWordsParameters mp = (MultiWordsParameters)param;
		double sim = 0;
		
		String sourceString;
		String targetString;
		 if(typeOfNodes == alignType.aligningClasses) {
			 sourceString = sourceClassDocuments.get(source.getIndex());
			 targetString = targetClassDocuments.get(target.getIndex());
		 }
		 else {
			 sourceString = sourcePropDocuments.get(source.getIndex());
			 targetString = targetPropDocuments.get(target.getIndex());
		 }
		
		//calculate similarity
		if(mp.measure.equals(MultiWordsParameters.COSINE)) {
			CosineSimilarity measure = new CosineSimilarity();
			sim = measure.getSimilarity(sourceString, targetString);
		}
		else 	if(mp.measure.equals(MultiWordsParameters.JACCARD)) {
			JaccardSimilarity measure = new JaccardSimilarity();
			sim = measure.getSimilarity(sourceString, targetString); 
		}
		else 	if(mp.measure.equals(MultiWordsParameters.EUCLIDEAN)) {
			EuclideanDistance measure = new EuclideanDistance();
			sim = measure.getSimilarity(sourceString, targetString); 
		}
		else 	if(mp.measure.equals(MultiWordsParameters.DICE)) {
			DiceSimilarity measure = new DiceSimilarity();
			sim = measure.getSimilarity(sourceString, targetString); 
		}
		else 	if(mp.measure.equals(MultiWordsParameters.TFIDF)) {
			 StringTFIDF tfidf;
			 if(typeOfNodes == alignType.aligningClasses) {
				 tfidf = new StringTFIDF(classCorpus);
			 }
			 else tfidf = new StringTFIDF(propCorpus);
			 
			 //calculate similarity
			 sim = tfidf.getSimilarity(sourceString, targetString);
			 
		}
		//System.out.println("***** sim: "+sim+"\nmultisource: "+multiWordsSource+"\n"+processedSource+"\n"+multiWordsTarget+"\n"+processedTarget );
		return new Alignment(source, target, sim);
		
	}
	
	
	
	
	



	      
}

