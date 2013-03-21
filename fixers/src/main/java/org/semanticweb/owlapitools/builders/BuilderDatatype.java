package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDatatype;

/** Builder class for OWLDatatype */
public class BuilderDatatype extends BaseEntityBuilder<OWLDatatype, BuilderDatatype> {
    @Override
    public OWLDatatype buildObject() {
        if (pm != null && string != null) {
            return df.getOWLDatatype(string, pm);
        }
        return df.getOWLDatatype(iri);
    }
}
