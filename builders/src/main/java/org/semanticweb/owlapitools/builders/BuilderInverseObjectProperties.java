package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Builder class for OWLInverseObjectPropertiesAxiom */
public class BuilderInverseObjectProperties extends
        BaseObjectPropertyBuilder<OWLInverseObjectPropertiesAxiom, BuilderInverseObjectProperties> {
    private OWLObjectPropertyExpression inverseProperty = null;

    /** @param arg
     *            inverse property
     * @return builder */
    public BuilderInverseObjectProperties withInverseProperty(
            OWLObjectPropertyExpression arg) {
        inverseProperty = arg;
        return this;
    }

    @Override
    public OWLInverseObjectPropertiesAxiom buildObject() {
        return df.getOWLInverseObjectPropertiesAxiom(property, inverseProperty,
                annotations);
    }
}
