package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;

/** Builder class for OWLDataAllValuesFrom */
public class BuilderDataAllValuesFrom extends
        BaseDataBuilder<OWLDataAllValuesFrom, BuilderDataAllValuesFrom> {
    @Override
    public OWLDataAllValuesFrom buildObject() {
        return df.getOWLDataAllValuesFrom(property, dataRange);
    }
}
