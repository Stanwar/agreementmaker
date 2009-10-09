package am.app.ontology.ontologyParser;

import java.util.HashMap;
import am.app.ontology.Node;
import am.userInterface.vertex.Vertex;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * <p>Title: </p>
 *
 * <p>Description: Ontology to Tree Builder</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: ADVIS LAB</p>
 *
 * @author Nalin Makar
 * @version 1.5
 */
public class RdfsTreeBuilder extends TreeBuilder{
	
	final static String RDFCLASSROOTNAME = "RDFS Classes Hierararchy";
	//instance variables
	private OntModel ontModel;
	private HashMap<OntResource,Node> processedSubs;
	
	/*This variable has been introduced to solve a problem occurred loading OAEI test case ontology in RDF/XML format
	 * These ontologies contains some referenced classes and properties of other namespaces
	 * These classes shouldn't be considered in the matching and we don't want to load them. 
	 * The parser will skip to load this classes if this boolean variable is true, so it has to be true at least for RDF/XML format
	 * In the N3 format can't be true because the namespace organization is different 
	 * ATTENTION: we skip from loading the referenced classes but their sons in the hierarchy may be valid classes. 
	 */
	private boolean skipOtherNamespaces;
	/* To get the namespace of the loaded ontologies we use the method model.getNsPrefixMapping.get("")
	 * This method cannot be used with "" input for N3
	 */
	private String ns = null;
	
	public RdfsTreeBuilder(String fileName, int sourceOrTarget, String language, String format,boolean skip) {
		super(fileName, sourceOrTarget, language, format);
		skipOtherNamespaces = skip;
		
	}
	
	protected void buildTree() {
		System.out.print("Reading Model...");
		ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, null);
		//TODO: Figure out if the 2nd arg in next fn call should be null or someother URI
		ontModel.read( "file:"+ontology.getFilename(), "", ontology.getFormat() );
		System.out.println("done");
		
		
		if(skipOtherNamespaces) { //we can get this information only if we are working with RDF/XML format, using this on N3 you'll get null pointer exception you need to use an input different from ""
			try {//if we can't access the namespace of the ontology we can't skip nodes with others namespaces
				ns = ontModel.getNsPrefixMap().get("").toString();
			}
			catch(Exception e) {
				skipOtherNamespaces = false;
			}
		}
		ontology.setSkipOtherNamespaces(skipOtherNamespaces);
		ontology.setModel(ontModel);
		treeRoot = new Vertex(ontology.getTitle(),ontology.getTitle(),ontModel,  ontology.getSourceOrTarget());//Creates the root of type Vertex for the tree, is a fake vertex with no corresponding node
		Vertex classRoot = new Vertex(RDFCLASSROOTNAME,RDFCLASSROOTNAME,ontModel,  ontology.getSourceOrTarget());
		treeRoot.add(classRoot);
		treeCount = 2;
		processedSubs = new HashMap<OntResource, Node>();
		ExtendedIterator i = ontModel.listHierarchyRootClasses();
		classRoot = createTree(classRoot, i);//should add all valid classes and subclasses in the iterator to the classRoot
		ontology.setClassesTree(classRoot);
		
	}
	
	protected Vertex createTree(Vertex root, ExtendedIterator i){
		while (i.hasNext()) {
			OntClass cls = (OntClass)i.next();
			if(cls.isAnon()) ;//skip
		    else if(skipOtherNamespaces && !cls.getNameSpace().toString().equals(ns)) {
          	   //If a node has a different namespace must be jumped , so sons of that node must be added to the grandfather
          	   ExtendedIterator moreSubs = cls.listSubClasses( true );
          	   root = createTree(root, moreSubs);       	   
            }
		    else {
				Vertex newVertex = createNodeAndVertex(cls, root.getNodeType());
				ExtendedIterator subs = cls.listSubClasses( true );
				newVertex = createTree(newVertex, subs);
				root.add(newVertex);
				treeCount++;
			}
		}
		return root;
	}
	
	/**
	 * Create a TreeNode for the given class
	 * This method is a builder for the pair Node Vertex
	 * Nodes are element to be aligned, the structure keeps all info needed for matchings
	 * Vertex it's the graphical rapresentation. They must be created together.
	 * The node has a reference to the vertex (a node can have more than one vertex if Class has more fathers in the hierarchy)
	 * The vertex has a reference to the node.
	 * Each node is identified by a uniqueKey used as index in the ClassList structure
	 * @param entity
	 * @return
	 */
	public Vertex createNodeAndVertex(OntResource entity, int sourceOrTarget) {
		 
		 Node node;
         if( processedSubs.containsKey( entity ) ) {//the node has been already created, but we need only to create a new vertex;
         	node = processedSubs.get(entity); //reuse of the previous Node information for this class, but we need a new Vertex
         }
         else {
        	// uniqueKey == 0 here, no need to increment it.
     		node = new Node(uniqueKey,entity, Node.OWLCLASS); //new node with a new key, with the link to the graphical Vertex representation
            ontology.getClassesList().add(node);
            processedSubs.put(entity, node);
            uniqueKey++;  // here is where we increment the uniqueKey.
         }
         Vertex vert = new Vertex(node.getLocalName(), entity.getURI(), ontModel, sourceOrTarget);
         node.addVertex(vert);
         vert.setNode(node);
		 return vert;
	}
}