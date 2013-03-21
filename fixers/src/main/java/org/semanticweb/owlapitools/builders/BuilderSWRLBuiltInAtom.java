package org.semanticweb.owlapitools.builders;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;

/** Builder class for SWRLBuiltInAtom */
public class BuilderSWRLBuiltInAtom extends
        BaseBuilder<SWRLBuiltInAtom, BuilderSWRLBuiltInAtom> {
    private IRI iri = null;
    private List<SWRLDArgument> args = new ArrayList<SWRLDArgument>();

    /** @param arg
     *            iri
     * @return builder */
    public BuilderSWRLBuiltInAtom with(IRI arg) {
        iri = arg;
        return this;
    }

    /** @param arg
     *            argument
     * @return builder */
    public BuilderSWRLBuiltInAtom with(SWRLDArgument arg) {
        args.add(arg);
        return this;
    }

    @Override
    public SWRLBuiltInAtom buildObject() {
        return df.getSWRLBuiltInAtom(iri, args);
    }
}
