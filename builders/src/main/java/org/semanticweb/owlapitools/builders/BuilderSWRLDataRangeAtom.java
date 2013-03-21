package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;

/** Builder class for SWRLDataRangeAtom */
public class BuilderSWRLDataRangeAtom extends
        BaseBuilder<SWRLDataRangeAtom, BuilderSWRLDataRangeAtom> {
    private SWRLDArgument argument;
    private OWLDataRange predicate;

    /** @param arg
     *            argument
     * @return builder */
    public BuilderSWRLDataRangeAtom with(SWRLDArgument arg) {
        argument = arg;
        return this;
    }

    /** @param arg
     *            predicate
     * @return builder */
    public BuilderSWRLDataRangeAtom with(OWLDataRange arg) {
        predicate = arg;
        return this;
    }

    @Override
    public SWRLDataRangeAtom buildObject() {
        return df.getSWRLDataRangeAtom(predicate, argument);
    }
}
