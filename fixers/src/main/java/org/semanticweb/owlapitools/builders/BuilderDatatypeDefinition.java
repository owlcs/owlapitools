package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;

/** Builder class for OWLDatatypeDefinitionAxiom */
public class BuilderDatatypeDefinition extends
        BaseBuilder<OWLDatatypeDefinitionAxiom, BuilderDatatypeDefinition> {
    private OWLDataRange range = null;
    private OWLDatatype type = null;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDatatypeDefinition(OWLDatatypeDefinitionAxiom expected) {
        with(expected.getDatatype()).withType(expected.getDataRange()).withAnnotations(
                expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderDatatypeDefinition() {}

    /** @param arg
     *            range
     * @return builder */
    public BuilderDatatypeDefinition withType(OWLDataRange arg) {
        range = arg;
        return this;
    }

    /** @param arg
     *            type
     * @return builder */
    public BuilderDatatypeDefinition with(OWLDatatype arg) {
        type = arg;
        return this;
    }

    @Override
    public OWLDatatypeDefinitionAxiom buildObject() {
        return df.getOWLDatatypeDefinitionAxiom(type, range, annotations);
    }
}
