package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;

/** Builder class for OWLEntity */
public class BuilderEntity extends BaseEntityBuilder<OWLEntity, BuilderEntity> {
    private EntityType<?> entityType = null;

    /** @param arg
     *            entity type
     * @return builder */
    public BuilderEntity withType(EntityType<?> arg) {
        entityType = arg;
        return this;
    }

    @Override
    public OWLEntity buildObject() {
        if (pm != null && string != null) {
            return df.getOWLEntity(entityType, pm.getIRI(string));
        }
        return df.getOWLEntity(entityType, iri);
    }
}
