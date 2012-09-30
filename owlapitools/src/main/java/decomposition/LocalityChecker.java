package decomposition;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAxiom;

public interface LocalityChecker {
    boolean local(OWLAxiom axiom);

    /** allow the checker to preprocess an ontology if necessary */
    void preprocessOntology(Collection<AxiomWrapper> vec);

    void setSignatureValue(TSignature sig);

    TSignature getSignature();
}
