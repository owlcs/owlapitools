package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.SWRLVariable;

/** Builder class for SWRLVariable */
public class BuilderSWRLVariable extends BaseBuilder<SWRLVariable, BuilderSWRLVariable> {
    private IRI iri;

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
