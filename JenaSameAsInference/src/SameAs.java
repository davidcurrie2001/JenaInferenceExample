import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.jena.ontology.*;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;




public class SameAs {
	
	private static OntModel onto;
	
	// Define the namespaces we'll use later
	static final String marineInstitute   = "http://www.marine.ie/SemanticFishData#";
	static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	static final String owl = "http://www.w3.org/2002/07/owl#";
	
	static final String prefixString = "PREFIX mi:   <" + marineInstitute + ">\r\n"
	        +"PREFIX rdf: <" + rdf + ">\r\n"
			+"PREFIX owl: <" + owl + ">\r\n";
	

	// Select all mi:SamplingSummary records
	private static final String myQueryString1 = "SELECT ?s\r\n" + 
			"WHERE\r\n" + 
			"   { ?s rdf:type mi:SamplingSummary }";

	
	// Select all mi:SamplingSummary records with their mi:isOfSpecies values
	private static final String myQueryString2 = "select *\r\n" + 
			"where {\r\n" + 
			"	?sample rdf:type mi:SamplingSummary .\r\n" + 
			"	?sample mi:isOfSpecies ?species . \r\n" + 
			"}";
	
	// Select all mi:SamplingSummary records with mi:isOfSpecies mi:Species1
	private static final String myQueryString3 = "select *\r\n" + 
			"where {\r\n" + 
			"	?sample rdf:type mi:SamplingSummary .\r\n" + 
			"	?sample mi:isOfSpecies mi:Species1 . \r\n" + 
			"}";
	
	// Select all mi:SamplingSummary records with their mi:isOfSpecies values
	private static final String myQueryString4 = "select *\r\n" + 
			"where {\r\n" + 
			"	?sample rdf:type mi:PelagicSample .\r\n" + 
			"}";

	public static void main(String[] args) {
		

		System.out.println("Run model spec comparison");
		
		// Ontology model class which uses the OWL_MEM spec	
		runQueries(OntModelSpec.OWL_MEM, "OWL_MEM");
		
		// Ontology model class which uses the OWL_MEM_MICRO_RULE_INF spec	
		runQueries(OntModelSpec.OWL_MEM_MICRO_RULE_INF, "OWL_MEM_MICRO_RULE_INF");
		
		// Ontology model class which uses the OWL_MEM_RULE_INF spec
		runQueries(OntModelSpec.OWL_MEM_RULE_INF, "OWL_MEM_RULE_INF");
		
		System.out.println("Finished");
        

	}
	
	private static void runQueries(OntModelSpec mySpec, String myName) {
	
        System.out.println(myName);
		
		// this specification does subclass inference
		//onto = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, null );
		onto = ModelFactory.createOntologyModel(mySpec, null );
		
		// Read in the ontology from file
		System.out.println("Load data from file");
		onto.read( "file:resources/SamplingSummary.n3", "N3" );
		
		System.out.println("Number of statements in model: " + onto.size());
			
		System.out.println("Check how many samples there are in the model - there should be 2");
		executeSPARQL(onto, prefixString + " " + myQueryString1);
		
		System.out.println("Check how many sample/species combinations there are in the model - there are 2 if there are no 'sameAs' inferences");
		executeSPARQL(onto, prefixString + " " + myQueryString2);
		
		System.out.println("Check if there are samples of 'species1'.  If the number is greater then 0 then there is 'sameAs' inference.");
		executeSPARQL(onto, prefixString + " " + myQueryString3);
		
		System.out.println("Check if there are samples of type  'mi:PelagicSample'.  If the number is greater then 0 then there is 'subclass' inference.");
		executeSPARQL(onto, prefixString + " " + myQueryString4);
		
        System.out.println("Save the data to an RDF/XML format text file");      
        
        saveRDF(myName + ".rdf");
        
        onto = null;
		
	}
	
	// Execute a SPARQL query against the OntModel
	private static void executeSPARQL(Model model, String myQueryString) {
		
		//System.out.println("My SPARQL query: " + myQueryString);
		
		// Execute the query and display the results
		QueryExecution qe = QueryExecutionFactory.create (myQueryString,model);

		ResultSet rs;
		
		int resultsCount = 0;
		for (rs = qe.execSelect() ; rs.hasNext() ; ) {
		      QuerySolution binding = rs.nextSolution();
		      resultsCount++;

		}
		System.out.println("Number of results: "+ resultsCount);
		//ResultSetFormatter.out(qe.execSelect());
		
	}
	
	private static void saveRDF(String fileName) {
	
        try (OutputStream myFile = new FileOutputStream(fileName)) {
       	 
			onto.write(myFile, "RDFXML") ;
 
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}


}
