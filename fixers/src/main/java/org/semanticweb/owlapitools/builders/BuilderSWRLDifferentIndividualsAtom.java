package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;

/** Builder class for SWRLDifferentIndividualsAtom */
public class BuilderSWRLDifferentIndividualsAtom extends
        BaseBuilder<SWRLDifferentIndividualsAtom, BuilderSWRLDifferentIndividualsAtom> {
    private SWRLIArgument arg0;
    private SWRLIArgument arg1;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderSWRLDifferentIndividualsAtom(SWRLDifferentIndividualsAtom expected) {
        withArg0(expected.getFirstArgument()).withArg1(expected.getSecondArgument());
    }

    /** uninitialized builder */
    public BuilderSWRLDifferentIndividualsAtom() {}

    /** @param arg
     *            arg0
     * @return builder */
    public BuilderSWRLDifferentIndividualsAtom withArg0(SWRLIArgument arg) {
        arg0 = arg;
        return this;
    }

    /** @param arg
     *            arg0
     * @return builder */
    public BuilderSWRLDifferentIndividualsAtom withArg1(SWRLIArgument arg) {
        arg1 = arg;
        return this;
    }

    @Override
    public SWRLDifferentIndividualsAtom buildObject() {
        return df.getSWRLDifferentIndividualsAtom(arg0, arg1);
    }
}
