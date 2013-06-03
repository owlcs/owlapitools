package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.SWRLVariable;

/** Builder class for SWRLVariable */
public class BuilderSWRLVariable extends BaseBuilder<SWRLVariable, BuilderSWRLVariable> {
    private IRI iri;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderSWRLVariable(SWRLVariable expected) {
        with(expected.getIRI());
    }

    /** uninitialized builder */
    public BuilderSWRLVariable() {}

    /** @param arg
     *            iri
     * @return builder */
    public BuilderSWRLVariable with(IRI arg) {
        iri = arg;
        return this;
    }

    @Override
    public SWRLVariable buildObject() {
        return df.getSWRLVariable(iri);
    }
}
