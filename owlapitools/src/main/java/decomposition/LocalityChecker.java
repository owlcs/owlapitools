package decomposition;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

public interface LocalityChecker {
    boolean local(OWLAxiom axiom);

    /** allow the checker to preprocess an ontology if necessary */
    void preprocessOntology(Collection<AxiomWrapper> vec);

    void setSignatureValue(Signature sig);

    Signature getSignature();

    boolean isTopEquivalent(OWLObject expr);

    boolean isBotEquivalent(OWLObject expr);

    boolean isREquivalent(OWLObject expr);
}
