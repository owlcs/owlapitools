package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;

/** Builder class for SWRLDataPropertyAtom */
public class BuilderSWRLDataPropertyAtom extends
        BaseDataPropertyBuilder<SWRLDataPropertyAtom, BuilderSWRLDataPropertyAtom> {
    private SWRLDArgument arg1;
    private SWRLIArgument arg0;


    /** @param arg
     *            data argument
     * @return builder */
    public BuilderSWRLDataPropertyAtom with(SWRLDArgument arg) {
        arg1 = arg;
        return this;
    }

    /** @param arg
     *            individual
     * @return builder */
    public BuilderSWRLDataPropertyAtom with(SWRLIArgument arg) {
        arg0 = arg;
        return this;
    }

    @Override
    public SWRLDataPropertyAtom buildObject() {
        return df.getSWRLDataPropertyAtom(property, arg0, arg1);
    }
}
