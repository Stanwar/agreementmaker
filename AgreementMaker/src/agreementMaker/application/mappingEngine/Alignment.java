
package agreementMaker.application.mappingEngine;

import agreementMaker.application.ontology.*;


public class Alignment
{
    private Node entity1 = null;
    private Node entity2 = null;
    private double similarity = 0;
    private String relation = null;
    
    //THE FONT USED IN THE CANVAS MUST BE A UNICODE FONT TO VIEW THIS SPECIAL CHARS
    public final static String EQUIVALENCE = "=";
    public final static String SUBSET = "\u2282";
    public final static String SUPERSET = "\u2283";
    public final static String SUBSETCOMPLETE = "\u2286";
    public final static String SUPERSETCOMPLETE = "\u2287";

    public Alignment(Node e1, Node e2, double sim, String r)
    {
        entity1 = e1;
        entity2 = e2;
        similarity = sim;
        relation = r;
    }

    public Alignment(Node e1, Node e2, double sim)
    {
        entity1 = e1;
        entity2 = e2;
        similarity = sim;
        relation = "=";
    }

    public Alignment(Node e1, Node e2)
    {
        entity1 = e1;
        entity2 = e2;
        similarity = 1.0;
        relation = "=";
    }

    public Alignment(double s) {
		//This is a fake contructor to use an alignment as a container for sim and relation, to move both values between methods
    	similarity = s;
    	relation = EQUIVALENCE;
	}

	public Node getEntity1()
    {
        return entity1;
    }

    public void setEntity1(Node e1)
    {
        entity1 = e1;
    }

    public Node getEntity2()
    {
        return entity2;
    }

    public void setEntity2(Node e2)
    {
        entity2 = e2;
    }

    public void setSimilarity(double sim)
    {
        similarity = sim;
    }

    public double getSimilarity()
    {
        return similarity;
    }



    public String getRelation()
    {
        return relation;
    }

    public void setRelation(String r)
    {
        relation = r;
    }

    public String filter(String s)
    {
        if (s == null) {
            return null;
        } else {
            int index = s.lastIndexOf("#");
            if (index >= 0) {
                return s.substring(index + 1);
            } else {
                index = s.lastIndexOf("/");
                if (index >= 0) {
                    return s.substring(index + 1);
                } else { 
                    return s;
                }
            }
        }
    }
    
    public boolean equals(Alignment alignment)
    {
        if (entity1.equals(alignment.getEntity1()) 
                && entity2.equals(alignment.getEntity2())) {
            return true;
        } else {
            return false;
        }
    }
    
    public int hashCode() {//Tipically hashcode of a pair is the xor of the hashcodes, i don't know if the hashcode of the single node is good calculated like this
    	return entity1.hashCode() ^ entity2.hashCode();
    }
    
    public String toString() {
    	return "("+entity1+" "+entity2+" "+similarity+")";
    }
}
