package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

/** Builder class for OWLDeclarationAxiom */
public class BuilderDeclaration extends
        BaseBuilder<OWLDeclarationAxiom, BuilderDeclaration> {
    private OWLEntity entity = null;

    /** @param arg
     *            entity
     * @return builder */
    public BuilderDeclaration withEntity(OWLEntity arg) {
        entity = arg;
        return this;
    }

    @Override
    public OWLDeclarationAxiom buildObject() {
        return df.getOWLDeclarationAxiom(entity, annotations);
    }
}
