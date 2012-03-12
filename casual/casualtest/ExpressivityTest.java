package casualtest;

import java.io.File;
import java.util.Collections;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

public class ExpressivityTest {
	public static void main(String[] args) throws Exception{
		OWLOntologyManager m=OWLManager.createOWLOntologyManager();
		OWLOntology o=m.loadOntologyFromOntologyDocument(new File("/Users/ignazio/Downloads/vivo-core-public-1.4.owl"));
		DLExpressivityChecker c=new DLExpressivityChecker(Collections.singleton(o));
		System.out.println("ExpressivityTest.main() "+c.getDescriptionLogicName());
	}

}
