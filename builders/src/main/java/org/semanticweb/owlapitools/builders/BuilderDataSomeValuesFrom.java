package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;

/** Builder class for OWLDataSomeValuesFrom */
public class BuilderDataSomeValuesFrom extends
        BaseDataBuilder<OWLDataSomeValuesFrom, BuilderDataSomeValuesFrom> {
    @Override
    public OWLDataSomeValuesFrom buildObject() {
        return df.getOWLDataSomeValuesFrom(property, dataRange);
    }
}
