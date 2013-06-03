package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDatatype;

/** Builder class for OWLDatatype */
public class BuilderDatatype extends BaseEntityBuilder<OWLDatatype, BuilderDatatype> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDatatype(OWLDatatype expected) {
        withIRI(expected.getIRI());
    }

    /** uninitialized builder */
    public BuilderDatatype() {}

    @Override
    public OWLDatatype buildObject() {
        if (pm != null && string != null) {
            return df.getOWLDatatype(string, pm);
        }
        return df.getOWLDatatype(iri);
    }
}
