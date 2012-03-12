package casualtest;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import static org.junit.Assert.*;

public class ManchesterImportTests {
	String mch1 = "Prefix: : <http://k1s.org/thesuperont>\n"
			+ "Prefix: rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "Prefix: owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "Prefix: rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "Ontology: <http://k1s.org/thesuperont>\n" + "Class: Thesuperclass";
	String mch2 = "Prefix: : <http://k1s.org/thesubont>\n"
			+ "Prefix: su: <http://k1s.org/thesuperont>\n"
			+ "Prefix: rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "Prefix: owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "Prefix: rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "Ontology: <http://k1s.org/thesubont>\n"
			+ "Import: <http://t.k1s.org/thesuperont.owl>\n"
			// XXX this fixes the case
			//+"Class: su:Thesuperclass\n"
			+ "Class: Thesubclass\n" + "	SubClassOf: su:Thesuperclass";

	@Test
	public void testRemoteIsParseable() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(IRI
				.create("http://t.k1s.org/thesuperont.owl"));
		assertEquals(1, ontology.getAxioms().size());
		final ManchesterOWLSyntaxOntologyFormat ontologyFormat = new ManchesterOWLSyntaxOntologyFormat();
		manager.saveOntology(ontology, ontologyFormat, new SystemOutDocumentTarget());
	}

	@Test
	public void testImports() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		StringDocumentSource source = new StringDocumentSource(mch2);
		try {
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(source);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
