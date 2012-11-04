package casualtest;

import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

public class FunctionalSyntaxDifferentIndividualsProblem {

	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLOntology ontology = manager.createOntology();
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(IRI.create("http://example.org/person_a"));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(individual));
		manager.addAxiom(ontology, factory.getOWLDifferentIndividualsAxiom(individual));

		String rdfxmlSaved = saveOntology(ontology, new RDFXMLOntologyFormat());
		System.out.println("Loaded: " + loadOntology(rdfxmlSaved));

		//XXX fails because the functional syntax does not accept this nonconformant kind of axiom
		OWLFunctionalSyntaxOntologyFormat functionalFormat = new OWLFunctionalSyntaxOntologyFormat();
		String functionalSaved = saveOntology(ontology, functionalFormat);
		System.out.println("Loaded: " + loadOntology(functionalSaved));
	}

    public static String saveOntology(OWLOntology ontology, PrefixOWLOntologyFormat format) throws IOException, OWLOntologyStorageException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        StringDocumentTarget t=new StringDocumentTarget();
        manager.saveOntology(ontology, format, t);

        return t.toString();
    }

    public static OWLOntology loadOntology(String ontologyFile) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        StringDocumentSource s=new StringDocumentSource(ontologyFile);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(s);
        return ontology;
    }

}
