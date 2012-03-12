package casualtest;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class HasReferenceTest extends TestCase {
	@Test
	public void testReferences() throws Exception{
		OWLOntologyManager m=OWLManager.createOWLOntologyManager();
		OWLOntology o=m.loadOntologyFromOntologyDocument(new File("../RTest/materializedOntologies/allobi.owl"));
		for(OWLEntity e: o.getSignature()) {
			assertEquals(o.getReferencingAxioms(e).size()>0, o.hasReferencingAxioms(e));
		}for(OWLAnonymousIndividual e: o.getAnonymousIndividuals()) {
			assertEquals(o.getReferencingAxioms(e).size()>0, o.hasReferencingAxioms(e));
		}
	}
}
