package org.semanticweb.owlapitools.builders;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

/** Builder class for OWLSubPropertyChainOfAxiom */
public class BuilderPropertyChain extends
        BaseObjectPropertyBuilder<OWLSubPropertyChainOfAxiom, BuilderPropertyChain> {
    private List<OWLObjectPropertyExpression> chain = new ArrayList<OWLObjectPropertyExpression>();


    /** @param arg
     *            property
     * @return builder */
    public BuilderPropertyChain withPropertyInChain(OWLObjectPropertyExpression arg) {
        chain.add(arg);
        return this;
    }

    @Override
    public OWLSubPropertyChainOfAxiom buildObject() {
        return df.getOWLSubPropertyChainOfAxiom(chain, property, annotations);
    }
}
