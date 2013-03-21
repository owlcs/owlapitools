package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWLFacet;

/** Builder class for OWLFacetRestriction */
public class BuilderFacetRestriction extends
        BaseBuilder<OWLFacetRestriction, BuilderFacetRestriction> {

    private OWLLiteral literal = null;
    private OWLFacet facet = null;

    /** @param arg
     *            int value
     * @return builder */
    public BuilderFacetRestriction withLiteral(int arg) {
        literal = df.getOWLLiteral(arg);
        return this;
    }

    /** @param arg
     *            literal value
     * @return builder */
    public BuilderFacetRestriction withLiteral(OWLLiteral arg) {
        literal = arg;
        return this;
    }

    /** @param arg
     *            double value
     * @return builder */
    public BuilderFacetRestriction withLiteral(double arg) {
        literal = df.getOWLLiteral(arg);
        return this;
    }

    /** @param arg
     *            float value
     * @return builder */
    public BuilderFacetRestriction withLiteral(float arg) {
        literal = df.getOWLLiteral(arg);
        return this;
    }

    /** @param arg
     *            facet
     * @return builder */
    public BuilderFacetRestriction withLiteral(OWLFacet arg) {
        facet = arg;
        return this;
    }

    @Override
    public OWLFacetRestriction buildObject() {
        return df.getOWLFacetRestriction(facet, literal);
    }
}
