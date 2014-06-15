package owlapitools;

import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class Test_stack {

    @Test
    public void shouldInfer() throws OWLOntologyCreationException {
        String wrong = "@prefix : <urn:test#> .\n@prefix core:    <urn:core#> .\n"
                + ":Person\n"
                + "  a owl:Class ;\n"
                + "  rdfs:subClassOf\n"
                + "    [ owl:onProperty :name ;\n"
                + "      owl:minCardinality \"1\" ] .\n"
                + ":name\n"
                + "  a owl:DatatypeProperty ;\n"
                + "  rdfs:domain :Person .\n"
                + ":tom\n"
                + "  a owl:NamedIndividual ;\n"
                + "  core:name \"Tom\" .";
        System.out.println("wrong ontology: \n" + wrong);
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o_wrongNamespaces = m
                .loadOntologyFromOntologyDocument(new StringDocumentSource(
                        wrong));
        for (OWLAxiom ax : o_wrongNamespaces.getAxioms()) {
            System.out.println(ax);
        }
        System.out.println("Corrections: -------------------------");
        String s = "@prefix : <urn:test#> .\n@prefix core:    <urn:core#> .\n"
                + ":Person a owl:Class ; rdfs:subClassOf [ owl:onProperty core:name ; owl:minCardinality \"1\" ] .\n"
                + "core:name a owl:DatatypeProperty ; rdfs:domain :Person .\n"
                + ":tom a owl:NamedIndividual ; core:name \"Tom\" .";
        System.out.println("ontology: \n" + s);
        OWLOntology o = m
                .loadOntologyFromOntologyDocument(new StringDocumentSource(s));
        for (OWLAxiom ax : o.getAxioms()) {
            System.out.println(ax);
        }
        OWLReasoner r = new Reasoner.ReasonerFactory().createReasoner(o);
        r.precomputeInferences(InferenceType.values());
        OWLNamedIndividual tom = m.getOWLDataFactory().getOWLNamedIndividual(
                IRI.create("urn:test#tom"));
        System.out.println("Types: " + r.getTypes(tom, false));
    }
}
