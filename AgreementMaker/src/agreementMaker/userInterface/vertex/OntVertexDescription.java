package agreementMaker.userInterface.vertex;

//import edu.stanford.smi.protegex.ont.ProtegeOWL;
//import edu.stanford.smi.protegex.ont.*;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;


public class OntVertexDescription {
	
	/*private static String annotations;
	private static String disjointClasses;
	private static String properties;
	private static String restrictions;*/
	
	public static void main( String[] args ) {
        // read the argument file, or the default
        String source = (args.length == 0) ? "file:C:\\Documents and Settings\\Nalin\\Desktop\\MSProj\\Ontologies\\travel.owl" : args[0];

        // guess if we're using a daml source
        boolean isDAML = source.endsWith( ".daml" );

        OntModel m = ModelFactory.createOntologyModel(
                        isDAML ? OntModelSpec.DAML_MEM : OntModelSpec.OWL_MEM, null
                     );

        m.read( source );

        Iterator i =  m.listHierarchyRootClasses()
    	//Iterator i =  m.listNamedClasses()
                      .filterDrop( new Filter() {
                                    public boolean accept( Object o ) {
                                        return ((Resource) o).isAnon();
                                    }} );

        while (i.hasNext()) {
            showClass(System.out,  (OntClass) i.next(), new ArrayList());
            System.out.println("===============================================");
            System.out.println("===============================================");
        }
    }
	
	private static void showClass(PrintStream out, OntClass cls, List occurs) {
		renderClassDescription( cls );
        out.println();
		// recurse to the next level down
        if (cls.canAs( OntClass.class )  &&  !occurs.contains( cls )) {
            for (Iterator i = cls.listSubClasses( true );  i.hasNext(); ) {
                OntClass sub = (OntClass) i.next();
                // we push this expression on the occurs list before we recurse
                occurs.add( cls );
                showClass(out, sub, occurs);
                occurs.remove( cls );
            }
        }
		
	}

	private static void renderClassDescription(OntClass cls) {
		OntClass sub;
		if(!cls.isAnon()){
			System.out.println("\n~~~~~~~ " + cls.getLocalName().toUpperCase() + " ~~~~~~~~");
			System.out.println("Super Classes:");//true
			for(Iterator i = cls.listSuperClasses(); i.hasNext();){
				sub = (OntClass)i.next();
				if(!sub.isAnon())System.out.println("\t"+sub.getLocalName());
			}
			System.out.println("Sub Classes:");//true
			for(Iterator i = cls.listSubClasses(); i.hasNext();){
				sub = (OntClass)i.next();
				if(!sub.isAnon())System.out.println("\t"+sub.getLocalName());
			}
			System.out.println("Disjoint With:");
			for(Iterator i = cls.listDisjointWith(); i.hasNext();){
				sub = (OntClass)i.next();
				if(!sub.isAnon())System.out.println("\t"+sub.getLocalName());
			}
			System.out.println("Equivalent Classes:");
			for(Iterator i = cls.listEquivalentClasses(); i.hasNext();){
				sub = (OntClass)i.next();
				if(!sub.isAnon())System.out.println("\t"+sub.getLocalName());
			}
			System.out.println("Declared Properties:");
			for(Iterator i = cls.listDeclaredProperties(); i.hasNext();){//true
				System.out.println("\t"+((OntProperty)i.next()).getLocalName());
			}
			System.out.println("Version Info:");
			for(Iterator i = cls.listVersionInfo(); i.hasNext();){
				System.out.println("\t"+i.next());
			}
			System.out.println("Labels:");
			for(Iterator i = cls.listLabels(null); i.hasNext();){
				System.out.println("\t"+i.next());
			}
			System.out.println("Comments:");
			for(Iterator i = cls.listComments(null); i.hasNext();){
				System.out.println("\t"+i.next());
			}
			System.out.println("Same As:");
			for(Iterator i = cls.listSameAs(); i.hasNext();){
				System.out.println("\t"+i.next());
			}
			System.out.println("See Also:");
			for(Iterator i = cls.listSeeAlso(); i.hasNext();){
				System.out.println("\t"+i.next());
			}
		}
		
	}

	public static String getVertexDescription(String className, OntModel ontModel){
		
		return "Dude.. fix this !!";
	}

	
	
}