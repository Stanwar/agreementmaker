package evaluation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import am.app.mappingEngine.referenceAlignment.MatchingPair;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentMatcher;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentParameters;

public class NYTEvaluator {
	/*
	 * Put the path of the alignment file you want to evaluate
	 * (the one you generated)
	 */
	static String toEvaluate = "C:/Users/federico/workspace/MyInstanceMatcher/alignment.rdf";
	/*
	 * Put the path of the reference alignment file
	 * Also paths relative to the root of the project are ok.
	 */
	static String reference = "OAEI2011/NYTReference/nyt-dbpedia-organizations-mappings.rdf";
	
	static String redirectsFile = "dbpediaRedirects.ser";
	
	static boolean printWrongMappings = true;
	
	static boolean matchingDBPedia = true;
	
	
	public static String evaluate(String file, String reference, double threshold) throws Exception{
		ReferenceAlignmentMatcher matcher = new ReferenceAlignmentMatcher();
		
		ReferenceAlignmentParameters param = new ReferenceAlignmentParameters();
		param.fileName = toEvaluate;
		matcher.setParam(param);		
		ArrayList<MatchingPair> filePairs = matcher.parseStandardOAEI();
		
		param.fileName = reference;
		ArrayList<MatchingPair> refPairs = matcher.parseStandardOAEI();
		
		return compare(filePairs, refPairs, threshold);	
	}
	
	public static String compare(ArrayList<MatchingPair> toEvaluate, ArrayList<MatchingPair> reference, double threshold){
		int count = 0;
		MatchingPair p1;
		MatchingPair p2;
		
		MatchingPair right = null;
		
		if(matchingDBPedia)
			cleanDBPediaMappings(toEvaluate);
		
		boolean found;
		for (int i = 0; i < toEvaluate.size(); i++) {
			found = false;
			p1 = toEvaluate.get(i);
			
			//System.out.println("Presented: "+ p1.sourceURI + " " + p1.targetURI);
			
			for (int j = 0; j < reference.size(); j++) {
				p2 = reference.get(j);
				
				p1.targetURI = p1.targetURI.replaceAll("Category:", "");
				
				//System.out.println(p2.getTabString());
				if(p1.sourceURI.equals(p2.sourceURI)){
					right = p2;
				}
				
				if(p1.sourceURI.toLowerCase().equals(p2.sourceURI.toLowerCase()) && p1.targetURI.toLowerCase().equals(p2.targetURI.toLowerCase())
						&& p1.relation.equals(p2.relation) && p1.similarity >= threshold){
					count++;
					found = true;
					break;
				}
			}
			if(found == false && printWrongMappings){
				if(right != null)
					System.out.println("Right:" + right.sourceURI + " " + right.targetURI);
				System.out.println("Wrong: " + p1.sourceURI + " " + p1.targetURI);
			}
		}	
		//System.out.println("right mappings: "+count);
		//System.out.println("prec:"+ (float)count/toEvaluate.size() + " rec: " +  (float)count/reference.size());
		float precision = (float)count/toEvaluate.size();
		float recall = (float)count/reference.size();
		float fmeasure = 2 * precision * recall / (precision + recall);
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		String retValue = df.format(threshold) + "\t" + precision + "\t" + recall + "\t" + fmeasure;
		return retValue;
	}
	
	public static void cleanDBPediaMappings(List<MatchingPair> pairs){
		HashMap<String, String> answers = new HashMap<String, String>();
		
		ObjectInput in;
		try {
			in = new ObjectInputStream(new FileInputStream(redirectsFile));
			Object input = in.readObject();
			answers = (HashMap<String, String>) input;	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String dbpedia = "http://dbpedia.org/sparql";
		MatchingPair pair;
		String uri;
		for (int i = 0; i < pairs.size(); i++) {
			pair = pairs.get(i);
			pair.targetURI = pair.targetURI.replaceAll("Category:", "");
			
			uri = pair.targetURI;
			
			System.out.println(uri);
			String query = "select ?r WHERE {" +
					"\n    <" + uri + ">	<http://dbpedia.org/ontology/wikiPageRedirects> ?r"  +
					"\n} LIMIT 10";
			
			String newUri = answers.get(query);
			
			if(newUri == null){
				QueryExecution qe = QueryExecutionFactory.sparqlService(dbpedia, query);
				ResultSet res = qe.execSelect();
				
				if(res.hasNext()){
					QuerySolution result = res.next();
					System.out.println("Redirects to " + result.get("r"));	
					newUri = result.get("r").toString();
					answers.put(query, result.get("r").toString());
				}
				else answers.put(query, "");
			}
			
			if(!newUri.isEmpty())
				pair.targetURI = newUri;			
			}
			
			
		
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(redirectsFile));
			out.writeObject(answers);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(evaluate(toEvaluate, reference, 0.0));
	}
}
