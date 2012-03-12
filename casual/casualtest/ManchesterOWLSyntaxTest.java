package casualtest;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ManchesterOWLSyntaxTest {

	public static void main(String[] args) throws Exception{
		OWLOntologyManager m=OWLManager.createOWLOntologyManager();
		final String str = "urn:test:onto1";
		OWLOntology o1=m.createOntology(IRI.create(str));
		final OWLDataFactory f = m.getOWLDataFactory();
		final OWLClass owlClass1 = f.getOWLClass(IRI.create(str+"#class1"));
		final OWLClass owlClass2 = f.getOWLClass(IRI.create(str+"#class2"));
		m.addAxiom(o1, f.getOWLSubClassOfAxiom(owlClass1, owlClass2));
		final ManchesterOWLSyntaxOntologyFormat ontologyFormat = new ManchesterOWLSyntaxOntologyFormat();
		ontologyFormat.setPrefix(":", str+"#");
		StringDocumentTarget t1=new StringDocumentTarget();
		m.saveOntology(o1, ontologyFormat, t1);

		final String str2 = "urn:test:onto2";
		OWLOntology o2=m.createOntology(IRI.create(str2));
		m.applyChange(new AddImport(o2, f.getOWLImportsDeclaration(o1.getOntologyID().getOntologyIRI())));
		m.addAxiom(o2, f.getOWLSubClassOfAxiom(f.getOWLClass(IRI.create(str2+"#class1")), owlClass1));
		ManchesterOWLSyntaxOntologyFormat ontologyFormat2 = new ManchesterOWLSyntaxOntologyFormat();
		ontologyFormat2.setPrefix("o1", str+"#");
		ontologyFormat2.setPrefix(":", str2+"#");
		StringDocumentTarget t2=new StringDocumentTarget();
		m.saveOntology(o2, ontologyFormat2, t2);

		System.out.println("ManchesterOWLSyntaxTest.main() \n"+t1);
		System.out.println("ManchesterOWLSyntaxTest.main() \n"+t2);


		OWLOntologyManager test=OWLManager.createOWLOntologyManager();
		o1= test.loadOntologyFromOntologyDocument(new StringDocumentSource(t1.toString()));
		o2= test.loadOntologyFromOntologyDocument(new StringDocumentSource(t2.toString()));
		test.saveOntology(o1, new ManchesterOWLSyntaxOntologyFormat(), new SystemOutDocumentTarget());
		test.saveOntology(o2, new ManchesterOWLSyntaxOntologyFormat(), new SystemOutDocumentTarget());

	}

}
