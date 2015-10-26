/*
	 Creating an alternative version of MyMatcher with better function for splitting strings. 

*/

package am.matcher.myMatcher;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.HashBag;
import org.openrdf.sail.rdbms.schema.HashTable;

import am.Utility;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.DefaultMatcherParameters;
import am.app.mappingEngine.Mapping;
import am.app.mappingEngine.ReferenceEvaluationData;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentMatcher;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentParameters;
import am.app.mappingEngine.referenceAlignment.ReferenceEvaluator;
import am.app.mappingEngine.similarityMatrix.SimilarityMatrix;
import am.app.ontology.Node;
import am.app.ontology.Ontology;
import am.app.ontology.ontologyParser.OntoTreeBuilder;
import am.app.ontology.ontologyParser.OntologyDefinition;
import am.app.ontology.profiling.manual.ManualOntologyProfiler;
import edu.smu.tspell.wordnet.*;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import rita.*;




public class MyMatcher2  extends AbstractMatcher  {

	private static String ONTOLOGY_BASE_PATH ="/Users/CRI/Documents/OneDrive/Coding/Class_Projects/CS586/AgreementMaker/agreementmaker/AgreementMaker-OSGi/AgreementMaker-Matchers/src/main/java/am/matcher/myMatcher/conference_dataset/"; // Use your base path
	private static String[] confs = {"conference","confOf"};
	//"cmt","conference","confOf","edas","ekaw","iasted","sigkdd"
	private static int count;
	private static int counter;
	private static int xx_count;
/*	private static String SOURCE_ONTOLOGY = "cmt";  // Change this for TESTING
	private static String TARGET_ONTOLOGY = "confOf";// Change this for TESTING
*/

	public MyMatcher(){
		super();
		count = 0;
		setName("MyMatcher");
		setCategory(MatcherCategory.UNCATEGORIZED);		
	}
	
	public double getCosineSimilarity(Double[] sourceVector , Double[] targetVector){
		
		double product = 0.0;
		double normalSource = 0.0;
		double normalTarget = 0.0;
		for (int i = 0; i < sourceVector.length; i++) {
			//System.out.println("sourceVector " + sourceVector[i] + "targetVector " + targetVector[i]);
			product +=  ((sourceVector[i]) * (targetVector[i]));
			//System.out.println("product " + i + " " + +product);
			normalSource += Math.pow(sourceVector[i], 2);
			//System.out.println("normal source " + i + " " + normalSource);
			normalTarget += Math.pow(targetVector[i], 2);
			//System.out.println( "normal target " + i + " " +normalTarget);
		 }
		
		return product/(Math.sqrt(normalSource)*Math.sqrt(normalTarget));
		
	}
	
	protected static void testWriteFile(SimilarityMatrix matrix) throws IOException{
		
		
		File file = new File("Source_Properties.txt");
		
		if(!file.exists()){
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file);
		BufferedWriter writer = new BufferedWriter(fw);
		List<Node> properties = matrix.getSourceOntology().getPropertiesList();

		try{
			for(Node items:properties){
				//writer.write(items.getLabels().toString() + "\n");
				
			}
			writer.flush();
			writer.close();
		}
		catch(Exception ex){
			System.out.println("No one loves the file");
		}
		finally{
			writer.close();
		}
		
		File file2 = new File("Source_Classes.txt");
		
		if(!file2.exists()){
			file2.createNewFile();
		}
		FileWriter fw2 = new FileWriter(file2);
		writer = new BufferedWriter(fw2);
		List<Node> classes = matrix.getSourceOntology().getClassesList();

		try{
			for(Node items:classes){
				writer.write(items.getLocalName().toString() + "\n");
			}
			writer.flush();
			writer.close();
		}
		catch(Exception ex){
			System.out.println("No one loves the file");
		}
		finally{
			writer.close();
		}
		
		File file3 = new File("Target_Properties.txt");
		
		if(!file3.exists()){
			file3.createNewFile();
		}
		FileWriter fw3 = new FileWriter(file3);
		writer = new BufferedWriter(fw3);
		List<Node> target_properties = matrix.getTargetOntology().getPropertiesList();

		try{
			for(Node items:target_properties){
				writer.write(items.getLocalName().toString() + "\n");
			}
			writer.flush();
			writer.close();
		}
		catch(Exception ex){
			System.out.println("No one loves the file");
		}
		finally{
			writer.close();
		}
		
		file3 = new File("Target_Classes.txt");
		
		if(!file3.exists()){
			file3.createNewFile();
		}
		fw3 = new FileWriter(file3);
		writer = new BufferedWriter(fw3);
		List<Node> target_classes = matrix.getTargetOntology().getClassesList();

		try{
			for(Node items:target_classes){
				writer.write(items.getLocalName().toString() + "\n");
			}
			writer.flush();
			writer.close();
		}
		catch(Exception ex){
			System.out.println("No one loves the file");
		}
		finally{
			writer.close();
		}
		
	}
	protected String curateString(String s){
		s = s.replace("-", "_");
		s = s.replace("_","");
		return s;
	}
	protected double ancestorScore(Node Source, Node Target){
		double cosSim = 0.0d;
		LinkedHashMap<String, Integer> hSource = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> hTarget = new LinkedHashMap<>();
		
		//ArrayList<String> arl1 = new ArrayList<String>();
		//ArrayList<String> arl2 = new ArrayList<String>();
		
		//arl1.add(Source.getLocalName());
		//arl2.add(Target.getLocalName());
		////
		hSource.put(curateString(Source.getLocalName().toLowerCase()), 0);
		hTarget.put(curateString(Target.getLocalName().toLowerCase()), 0);
		
		int count =1;
		for(Node s : Source.getAncestors()){
			
			hSource.put(curateString(s.getLocalName().toLowerCase()), count);
			count++;
		}
		
		count = 1;
		
		for(Node s : Target.getAncestors()){
			
			hTarget.put(curateString(s.getLocalName().toLowerCase()), count);
			count++;
		}
		
		Iterator iSource = hSource.entrySet().iterator();
		Iterator iTarget = hTarget.entrySet().iterator();
		
		for (HashMap.Entry<String, Integer> entry : hSource.entrySet()) {
			for(HashMap.Entry<String, Integer> targetEntry : hTarget.entrySet()){
				
				if(targetEntry.getKey().equalsIgnoreCase(entry.getKey())){
					if(targetEntry.getValue() <= entry.getValue()){
						if (entry.getValue() == 0){
							cosSim = 0.4d;
						}
						else if( entry.getValue() == 1){
							cosSim = 0.35d;
						}
						else if(entry.getValue() == 2){
							cosSim = 0.30d;
						}
						else{
							cosSim = 0.0d;
						}
					}
					else{
						if(targetEntry.getValue() == 0 ){
							cosSim = 0.4d;
						}
						else if(targetEntry.getValue() == 1 ){
							cosSim = 0.35d;
						}
						else if(targetEntry.getValue() == 2 ){
							cosSim = 0.30d;
						}
						else{
							cosSim = 0.0d;
						}
					}
					
				}
			}
		    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
		///
		/*for(Node s : Source.getAncestors()){
			arl1.add(s.getLocalName());
		}
		
		for(Node s : Target.getAncestors()){
			arl2.add(s.getLocalName());
		}

		Iterator itr = arl1.iterator();
		Iterator itr2 = arl2.iterator();
		int count;
		int counter2;
		double ret;
		/*while(itr.hasNext()){
			count = 0;
			while(itr2.hasNext()){
				count++;
				
				if(itr.hasNext() == itr2.hasNext()){
					if(count ==1 ){
						ret =  0.9d;
					}
					else if(count ==2){
						ret =  0.85d;
					}
					else if(count == 3){
						ret =  0.8d;
					}
					else{
						ret =  0.75d;
					}
				}
				
			}
		}*/
		if(cosSim > 0.0d){
			//System.out.println(Source.getLocalName() + cosSim);
			//System.out.println(cosSim);
		}
		return cosSim;

	}
	protected boolean removeStopWords(String word){
		
		if(word.equalsIgnoreCase("the") || 
				   word.equalsIgnoreCase("is") || 
				   word.equalsIgnoreCase("this") || 
				   word.equalsIgnoreCase("are") || 
				   word.equalsIgnoreCase("to") || 
				   word.equalsIgnoreCase("a") ||
				   word.equalsIgnoreCase("e") ||
				   word.equalsIgnoreCase("an") || 
				   word.equalsIgnoreCase("in") ||
				   word.equalsIgnoreCase("or") ||
				   word.equalsIgnoreCase("and") || 
				   word.equalsIgnoreCase("for") || 
				   word.equalsIgnoreCase("that") ||
				   word.equalsIgnoreCase("of") || 
				   word.equalsIgnoreCase("has")) {
			return true;
		}
		return false;
	}
	protected boolean checkDataRange(String word){
		
		if(word.equalsIgnoreCase("anyURI") || 
				   word.equalsIgnoreCase("base64Binary") || 
				   word.equalsIgnoreCase("boolean") || 
				   word.equalsIgnoreCase("byte") || 
				   word.equalsIgnoreCase("dateTime") || 
				   word.equalsIgnoreCase("dateTimeStamp") ||
				   word.equalsIgnoreCase("decimal") ||
				   word.equalsIgnoreCase("double") || 
				   word.equalsIgnoreCase("float") ||
				   word.equalsIgnoreCase("hexBinary") ||
				   word.equalsIgnoreCase("int") || 
				   word.equalsIgnoreCase("integer") || 
				   word.equalsIgnoreCase("Literal") || 
				   word.equalsIgnoreCase("short") ||
				   word.equalsIgnoreCase("Literal") ||
				   word.equalsIgnoreCase("long") || 
				   word.equalsIgnoreCase("for")) {
			return true;
		}
		return false;
	}
	@Override
	protected Mapping alignTwoNodes(Node source, Node target,
			alignType typeOfNodes, SimilarityMatrix matrix) throws Exception {

			
			NounSynset nounSynset;
			NounSynset[] hyponyms;
			System.setProperty("wordnet.database.dir","/usr/local/WordNet-3.0/dict");
			
			WordNetDatabase database = WordNetDatabase.getFileInstance();
			double cosSim = 0.0d;
			double nounSim = 0.0d;
			double verbSim = 0.0d;
			testWriteFile(matrix);
			
			String sourceLocalName;
			String targetLocalName;
			
			
			sourceLocalName = curateString(source.getLocalName());
			targetLocalName = curateString(target.getLocalName());
			//double sim=0.0d;

			//HashSet sourceSet = new HashSet();
			//HashSet targetSet = new HashSet();
			
			HashMap<String, Integer> sourceSet = new HashMap();
			HashMap<String, Integer> targetSet = new HashMap();
 
				if( typeOfNodes.toString().equalsIgnoreCase("aligningClasses")){
					

					if(sourceLocalName.equalsIgnoreCase(targetLocalName))
						cosSim=1.0d;
					else{ 
						//System.out.println(cosSim);
						
						String x1 = source.getLocalName().replace("-", "_");
						x1 = x1.replace("_", " ");
						x1 = x1.replace(".", " ");
						String x2 = target.getLocalName().replace("-", "_");
						x2 = x2.replace("_", " ");
						x2 = x2.replace(".", " ");
						
						if(x1.equalsIgnoreCase(x2)){
							cosSim = 1.0d;
						}
						if (x1.length() == x2.length()){
					    	int same = 0;
						    for (int i = 0; i < x1.length(); i++) {
						        if (x1.charAt(i) == x2.charAt(i))
						            same++;
						    }
						    if(same> x1.length() - 2){
						    	cosSim = 1.0;
						    }

						    char[] first = x1.toCharArray();
							  char[] second = x2.toCharArray();
							  Arrays.sort(first);
							  Arrays.sort(second);
							  Boolean result =  Arrays.equals(first, second);
							  
							  if(result){
								  cosSim = 1.0;
							  }
					    }
						else{
								
							Pattern pattern = Pattern.compile("\\s");
							Matcher matcher = pattern.matcher(x1);
							boolean found = matcher.find();
							matcher = pattern.matcher(x2);
							boolean found2 = matcher.find();

							sourceSet.clear();
							targetSet.clear();
							
							if(found && x1.length() > 1){
								String[] s = x1.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
								for(String ss : s){
									if(!removeStopWords(ss)){
										sourceSet.put(ss.toLowerCase(),0);
									}
								
								}
								
							}else{
								sourceSet.put(x1.toLowerCase(),0);
							}
							
							if(found2 && x2.length() > 1){
								String[] t = x2.split(" ");
								for(String tt : t){
																					
									if(!removeStopWords(tt)){
										targetSet.put(tt.toLowerCase(),0);
									}
								}
							}else{
								targetSet.put(x2.toLowerCase(),0);
							}
							Iterator iSource = sourceSet.entrySet().iterator();
							Iterator iTarget = targetSet.entrySet().iterator();
							
							if(target.getLocalName().equalsIgnoreCase("Dinner_banquet") && source.getLocalName().equalsIgnoreCase("Banquet")){
								System.out.println("XX");
							}
							int checkCounter = 0;
							for (HashMap.Entry<String, Integer> entry : sourceSet.entrySet()) {
								for(HashMap.Entry<String, Integer> targetEntry : targetSet.entrySet()){
									if(targetEntry.getKey().equalsIgnoreCase(entry.getKey()) ){
										//System.out.println("CHECK");
										checkCounter++;
										//break;
									}
								}
							}
							
							cosSim = (double) (checkCounter)/(double) Math.max(sourceSet.size() ,targetSet.size()) ;
							
							
							//cosSim = 0.0d;
							
						}
						
						if(cosSim <0.3d ){
							String[] s = x1.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
							String[] t = x2.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
							double tempSim = 0.0d;
							
							if(s.length == 1 && t.length == 1){
								for(String in : s){
									if(!removeStopWords(in)){
										for(String tar : t){
											if(!(removeStopWords(tar))){
												
												Synset[] synsets = database.getSynsets(in, SynsetType.NOUN );
												Synset[] synsets2 = database.getSynsets(tar, SynsetType.NOUN);
												
												Synset[] synsetsVerb = database.getSynsets(in, SynsetType.VERB );
												Synset[] synsetsVerb2 = database.getSynsets(tar, SynsetType.VERB);
												Hashtable sourceTable = new Hashtable();
												
									
												if(synsets.length != 0 && synsets2.length != 0 ){
													HashBag sourceBag = new HashBag();
													HashBag targetBag = new HashBag();
													HashBag BagOfWords = new HashBag();
									
													for( int i = 0; i < synsets.length; i++ ) {
														String[] words = synsets[i].getWordForms();
									
														for( int j = 0; j < words.length; j++ ) {
															int number = synsets[i].getTagCount(words[j]);
															//if(!BagOfWords.contains(words[j])){
																if(number > 0 && !sourceLocalName.equalsIgnoreCase(words[j])){
																	words[j].replace("_", "");
																	words[j].replace(" ", "");
																	//System.out.print(words[j]);
																	sourceBag.add(words[j]);
																	BagOfWords.add(words[j]);
																}
														}
													}
														
													for( int i = 0; i < synsets2.length; i++ ) {
														String[] words = synsets2[i].getWordForms();
														//System.out.println("Synset "+ i +": " + synsets[i].getDefinition() +".");
														//int counter = 0;
														for( int j = 0; j < words.length; j++ ) {
															int number = synsets2[i].getTagCount(words[j]);
															//if(!BagOfWords.contains(words[j])){
																if(number > 0 && !sourceLocalName.equalsIgnoreCase(words[j])){
																	words[j].replace("_", "");
																	words[j].replace(" ", "");
																	//System.out.print(words[j]);
																	targetBag.add(words[j]);
																	BagOfWords.add(words[j]);
																}
															//}
														}
														//System.out.println(".");
													}
													//System.out.println(BagOfWords.size());	
													Double[] sourceVector = new Double[BagOfWords.uniqueSet().size()];
													Double[] targetVector = new Double[BagOfWords.uniqueSet().size()];
													
													
													Object[] arl =  BagOfWords.uniqueSet().toArray();
													
													for(int i=0;i<BagOfWords.uniqueSet().size();i++){
														if( sourceBag.contains(arl[i])){
															sourceVector[i] = 1.0; 
														}
														else{
															sourceVector[i] = 0.0 ;
														}
														if(targetBag.contains(arl[i])){
															targetVector[i] = 1.0;
														}
														else{
															targetVector[i] = 0.0;
														}
														//System.out.println(sourceVector[i]);
													}
													nounSim = getCosineSimilarity(sourceVector, targetVector);
														
												}
												else 
												if(synsetsVerb.length != 0 && synsetsVerb2.length !=0) {
													HashBag sourceBag = new HashBag();
													HashBag targetBag = new HashBag();
													HashBag BagOfWords = new HashBag();
													for( int i = 0; i < synsetsVerb.length; i++ ) {
														String[] words = synsetsVerb[i].getWordForms();
														
														for( int j = 0; j < words.length; j++ ) {
															int number = synsetsVerb[i].getTagCount(words[j]);
															//if(!BagOfWords.contains(words[j])){
																if(number > 0 && !sourceLocalName.equalsIgnoreCase(words[j])){
																	//if( j > 0 && j < words.length )
																		//System.out.print(", ");
																	
																		//arl.add(words[j]);
																	
																	words[j].replace("_", "");
																	words[j].replace(" ", "");
																	//System.out.print(words[j]);
																	sourceBag.add(words[j]);
																	BagOfWords.add(words[j]);
																	}
																//}
															}
															//System.out.println(".");
														}
													
													for( int i = 0; i < synsetsVerb2.length; i++ ) {
														String[] words = synsetsVerb2[i].getWordForms();
														//System.out.println("Synset "+ i +": " + synsets[i].getDefinition() +".");
														for( int j = 0; j < words.length; j++ ) {
															int number = synsetsVerb2[i].getTagCount(words[j]);
															//if(!BagOfWords.contains(words[j])){
																if(number > 0 && !sourceLocalName.equalsIgnoreCase(words[j])){
																	words[j].replace("_", "");
																	words[j].replace(" ", "");
																	//System.out.print(words[j]);
																	targetBag.add(words[j]);
																	BagOfWords.add(words[j]);
																}
															//}
														}
														//System.out.println(".");
													}
													//System.out.println(BagOfWords.size());	
													Double[] sourceVector = new Double[BagOfWords.uniqueSet().size()];
													Double[] targetVector = new Double[BagOfWords.uniqueSet().size()];
													
													
													Object[] arl =  BagOfWords.uniqueSet().toArray();
													
													for(int i=0;i<BagOfWords.uniqueSet().size();i++){
														if( sourceBag.contains(arl[i])){
															sourceVector[i] = 1.0; 
														}
														else{
															sourceVector[i] = 0.0 ;
														}
														if(targetBag.contains(arl[i])){
															targetVector[i] = 1.0;
														}
														else{
															targetVector[i] = 0.0;
														}
														//System.out.println(sourceVector[i]);
														}
														verbSim = getCosineSimilarity(sourceVector, targetVector);
													}
									
													if( nounSim >= verbSim ){
														tempSim = nounSim;
													}
													else {
														tempSim = verbSim;
													}
													
													if(tempSim >= cosSim){
														cosSim = tempSim;
													}
												}
											}
												
										}
									}
							}
										
						}
					}
				}else 
					if (typeOfNodes.toString().equalsIgnoreCase("aligningProperties")){
						// Creating lists for both source and target. 
						List<String> l1 = new ArrayList<String>();
						cosSim = 0.0d;
						String sourceX = source.getLocalName().replace("-", "_");
						sourceX = sourceX.replace(".", "_");
						String[] x1 = sourceX.split("_");
						
						for(int i=0;i<x1.length;i++){
						    String[] inner =  x1[i].split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
						    for(int j=0;j<inner.length;j++){
						    	if(!removeStopWords(inner[j])){
						    		l1.add(inner[j].toLowerCase());
						    	}
						    }
						} 
						
						List<String> l2 = new ArrayList<String>();
						
						String targetX = target.getLocalName().replace("-", "_");
						sourceX = targetX.replace(".", "_");
						String[] x2 = targetX.split("_");
						
						for(int i=0;i<x2.length;i++){
						    String[] inner =  x2[i].split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
						    for(int j=0;j<inner.length;j++){
						    	if(!removeStopWords(inner[j])){
							        l2.add(inner[j].toLowerCase());
						    	}
						    }
						} 
						
						// Applying Domain and Range constraint for each Property Node
						try{
							if(sourceX.equalsIgnoreCase(targetX)){
								if((source.getPropertyDomain().getLocalName() != null) && (target.getPropertyDomain().getLocalName() !=null) 
										&& (source.getPropertyRange().getLocalName() != null) && (target.getPropertyRange().getLocalName() != null)){
									if(source.getPropertyDomain().getLocalName().equalsIgnoreCase(target.getPropertyDomain().getLocalName())  && source.getPropertyRange().getLocalName().equalsIgnoreCase(target.getPropertyRange().getLocalName())){
										cosSim = 1.0d;
									}
									
								}
								else
									if(checkDataRange(source.getPropertyRange().getLocalName()) || checkDataRange(target.getPropertyRange().getLocalName())){
										cosSim = 1.0d;
									}
								else{
										cosSim =0.0d;
									}
							}
							else{
								try{
									if((source.getPropertyDomain().getLocalName() != null) && (target.getPropertyDomain().getLocalName() !=null) 
											&& (source.getPropertyRange().getLocalName() != null) && (target.getPropertyRange().getLocalName() != null)){
										
										if(source.getPropertyDomain().getLocalName().equalsIgnoreCase(target.getPropertyDomain().getLocalName())  && source.getPropertyRange().getLocalName().equalsIgnoreCase(target.getPropertyRange().getLocalName())){
											if (sourceX.length() == targetX.length()){
											    	int same = 0;
												   // HashSet x = new HashSet();
												    //HashSet y = new HashSet();
												    for (int i = 0; i < sourceX.length(); i++) {
												    	//x.add(x1.toLowerCase().charAt(i));
												    	//y.add(x2.toLowerCase().charAt(i));
												        if (sourceX.charAt(i) == targetX.charAt(i))
												            same++;
												    }
												    if(same> sourceX.length() - 2){
												    	cosSim = 1.0;
												    }
												    
//												    if(x.containsAll(y)){
//												    	cosSim = 1.0;
//												    }
												    char[] first = sourceX.toCharArray();
													  char[] second = targetX.toCharArray();
													  Arrays.sort(first);
													  Arrays.sort(second);
													  Boolean result =  Arrays.equals(first, second);
													  
													  if(result){
														  cosSim = 1.0;
													  }
											}else{

												int checkCounter = 0;
												ListIterator litr = l1.listIterator();
												ListIterator titr = l2.listIterator();
												while(!litr.hasNext()){
													while(!titr.hasNext()){
														if(litr.next().equals(titr.next())){
															//System.out.println("CHECK");
															checkCounter++;
															//break;
														}
													}
												}
												
												
												cosSim = (double) (checkCounter)/(double) Math.max(l1.size(), l2.size());
											}
											
										}
									}
								}
								catch(Exception Ex){
									cosSim = 0.0d;
								}
							
							}
						}
						catch(Exception e){
							cosSim = 0.0d;
						}
						
				}

			if(Double.isNaN(cosSim)){
				cosSim = 0.0d;
			}
			
			if(cosSim < 0.51 && typeOfNodes.toString().equalsIgnoreCase("aligningClasses")){
				RiWordNet wordnet = new RiWordNet("/usr/local/WordNet-3.0");
				double similarity1 = (double) ( wordnet.getDistance(source.getLocalName(), target.getLocalName(), "a"));
				//similarity = similarity1;
				double similarity2 = (double) ( wordnet.getDistance(source.getLocalName(), target.getLocalName(), "n"));
				double similarity3 = (double) ( wordnet.getDistance(source.getLocalName(), target.getLocalName(), "v"));
				//*/
				double min =0.0d;
				similarity1 = 1.0 - similarity1 ;
				similarity2 = 1.0 - similarity2 ;
				similarity3 = 1.0 -similarity3 ; 
				if(similarity1 > similarity2 && similarity1 > similarity3){
					cosSim = similarity1;
				}
				else if(similarity2 > similarity1 && similarity2 > similarity3){
					cosSim = similarity2;
				}
				else {
					cosSim = similarity3;
				}
				
				if(cosSim < .51){
					cosSim = 0.0d;
				}
			} 
			if(cosSim > 1.0){
				cosSim = 1.0;
			}
			
			if (cosSim != 0.0d){
				counter++;
				System.out.println(" Source : " + source.getLocalName() + "  Target : " + target.getLocalName() + " CosSim : " + cosSim + " TypeOfNode : "  + typeOfNodes + " maxValue : " + matrix.getMaxValue());
				 
			}
			
		return new Mapping(source, target, cosSim);
	}


	private ArrayList<Double> referenceEvaluation(String pathToReferenceAlignment)
			throws Exception {

	
		// Run the reference alignment matcher to get the list of mappings in
		// the reference alignment
		ReferenceAlignmentMatcher refMatcher = new ReferenceAlignmentMatcher();


		// these parameters are equivalent to the ones in the graphical
		// interface
		ReferenceAlignmentParameters parameters = new ReferenceAlignmentParameters();
		parameters.fileName = pathToReferenceAlignment;
		parameters.format = ReferenceAlignmentMatcher.OAEI;
		parameters.onlyEquivalence = false;
		parameters.skipClasses = false;
		parameters.skipProperties = false;
		refMatcher.setSourceOntology(this.getSourceOntology());
		refMatcher.setTargetOntology(this.getTargetOntology());

		// When working with sub-superclass relations the cardinality is always
		// ANY to ANY
		if (!parameters.onlyEquivalence) {
			parameters.maxSourceAlign = AbstractMatcher.ANY_INT;
			parameters.maxTargetAlign = AbstractMatcher.ANY_INT;
		}

		refMatcher.setParam(parameters);

		// load the reference alignment
		refMatcher.match();
		//System.out.println(refMatcher.getAlignment());
		//System.out.println(refMatcher.getAlignmentsStrings());
		refMatcher.printAllPairs(); 
		Alignment<Mapping> referenceSet;
		if (refMatcher.areClassesAligned() && refMatcher.arePropertiesAligned()) {
			referenceSet = refMatcher.getAlignment(); // class + properties
		} else if (refMatcher.areClassesAligned()) {
			referenceSet = refMatcher.getClassAlignmentSet();
		} else if (refMatcher.arePropertiesAligned()) {
			referenceSet = refMatcher.getPropertyAlignmentSet();
		} else {
			// empty set? -- this should not happen
			referenceSet = new Alignment<Mapping>(Ontology.ID_NONE,
					Ontology.ID_NONE);
		}

		// the alignment which we will evaluate
		Alignment<Mapping> myAlignment;

		if (refMatcher.areClassesAligned() && refMatcher.arePropertiesAligned()) {
			myAlignment = getAlignment();
		} else if (refMatcher.areClassesAligned()) {
			myAlignment = getClassAlignmentSet();
		} else if (refMatcher.arePropertiesAligned()) {
			myAlignment = getPropertyAlignmentSet();
		} else {
			myAlignment = new Alignment<Mapping>(Ontology.ID_NONE,
					Ontology.ID_NONE); // empty
		}
		
		// use the ReferenceEvaluator to actually compute the metrics
		ReferenceEvaluationData rd = ReferenceEvaluator.compare(myAlignment,
				referenceSet);

		// optional
		setRefEvaluation(rd);

		// output the report
		StringBuilder report = new StringBuilder();
		report.append("Reference Evaluation Complete\n\n").append(getName())
				.append("\n\n").append(rd.getReport()).append("\n");
		
		
		double precision=rd.getPrecision();
		double recall=rd.getRecall();
		double fmeasure=rd.getFmeasure();
		String reportXX = rd.getReport();
		
		ArrayList<Double> results=new ArrayList<Double>();
		results.add(precision);
		results.add(recall);
		results.add(fmeasure);
		System.out.println(reportXX);
		return results;
		
		//log.info(report);
		
		// use system out if you don't see the log4j output
	  	

	}

	
	
	public static void main(String[] args) throws Exception {
	
		MyMatcher mm = new MyMatcher();
		double precision=0.0d;
		double recall=0.0d;
		double fmeasure=0.0d;
		int size=21;
		for(int i = 0; i < confs.length-1; i++)
		{
			for(int j = i+1; j < confs.length; j++)
			{
				Ontology source = OntoTreeBuilder.loadOWLOntology(ONTOLOGY_BASE_PATH + "/"+confs[i]+".owl");
				Ontology target = OntoTreeBuilder.loadOWLOntology(ONTOLOGY_BASE_PATH + "/"+confs[j]+".owl");
				
				OntologyDefinition def1=new OntologyDefinition(true, source.getURI(), null, null);
				OntologyDefinition def2=new OntologyDefinition(true, target.getURI(), null, null);
		
				def1.largeOntologyMode=false;
				source.setDefinition(def1);
				def2.largeOntologyMode=false;
				target.setDefinition(def2);
				ManualOntologyProfiler mop=new ManualOntologyProfiler(source, target);
				mm.setSourceOntology(source);
				mm.setTargetOntology(target);
		
				DefaultMatcherParameters param = new DefaultMatcherParameters();
		
		//Set your parameters
				param.threshold = 0.0;
				param.maxSourceAlign = 1;
				param.maxTargetAlign = 1;
			//	mm.setName(TARGET_ONTOLOGY);
				mm.setParameters(param);

				try {
					mm.match();			
			
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			ArrayList<Double> results=	mm.referenceEvaluation(ONTOLOGY_BASE_PATH + confs[i]+"-"+confs[j]+".rdf");
			precision+=results.get(0);
			recall+=results.get(1);
			fmeasure+=results.get(2);
			
			
			}
			
		}

		StringBuilder sb= new StringBuilder();
		
		precision/=size;
		recall/=size;
		fmeasure/=size;
		
		String pPercent = Utility.getOneDecimalPercentFromDouble(precision);
		String rPercent = Utility.getOneDecimalPercentFromDouble(recall);
		String fPercent = Utility.getOneDecimalPercentFromDouble(fmeasure);
		
		sb.append("Total Matches : " + counter);
		sb.append("Precision = Correct/Discovered: "+ pPercent+"\n");
		sb.append("Recall = Correct/Reference: "+ rPercent+"\n");
		sb.append("Fmeasure = 2(precision*recall)/(precision+recall): "+ fPercent+"\n");
	
		String report=sb.toString();
		System.out.println("Evaulation results:");
		System.out.println(report);

	}
	
 

	
}
/*
Evaulation results:
Total Matches : 464Precision = Correct/Discovered: 77.3%
Recall = Correct/Reference: 60.7%
Fmeasure = 2(precision*recall)/(precision+recall): 66.6%
*/