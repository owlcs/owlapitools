/**
 * Date: Jul 13, 2011
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.coode.suggestor.util.ReasonerHelper;
import org.junit.Test;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLFacet;

@SuppressWarnings("javadoc")
public class UtilTest extends AbstractSuggestorTest {
    @Test
    public void testGetSubDatatypes() throws Exception {
        OWLOntology ont = createOntology();
        OWLReasoner r = factory.createNonBufferingReasoner(ont);
        // SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        // FillerSuggestor fs = fac.getFillerSuggestor();
        // this is not supported by reasoners
        ReasonerHelper helper = new ReasonerHelper(r);
        OWLDataProperty p = df.getOWLDataProperty(IRI.create("http://example.com/p"));
        OWLDatatype integer = df.getIntegerOWLDatatype();
        OWLDatatype flt = df.getFloatOWLDatatype();
        OWLDatatype dbl = df.getDoubleOWLDatatype();
        mngr.applyChanges(Arrays.asList(
            new AddAxiom(ont, df.getOWLFunctionalDataPropertyAxiom(p)), new AddAxiom(
                ont, df.getOWLDeclarationAxiom(integer)),
            new AddAxiom(ont, df.getOWLDeclarationAxiom(dbl)),
            new AddAxiom(ont, df.getOWLDeclarationAxiom(flt))));
        NodeSet<OWLDatatype> subs = helper.getSubtypes(df.getTopDatatype());
        System.out.println(subs);
        assertEquals(3L, subs.nodes().count());
        assertTrue(subs.containsEntity(integer));
        assertTrue(subs.containsEntity(flt));
        assertTrue(subs.containsEntity(dbl));
        NodeSet<OWLDatatype> subsOfFloat = helper.getSubtypes(dbl);
        System.out.println(subsOfFloat);
        assertEquals(0L, subsOfFloat.nodes().count());
        // between incomparable datatypes
        assertFalse(helper.isSubtype(flt, integer));
        // between a range and a named type
        assertTrue(helper.isSubtype(
            df.getOWLDatatypeRestriction(integer,
                df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 2)), integer));
        assertFalse(helper
            .isSubtype(
                df.getOWLDatatypeRestriction(flt,
                    df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 2.0f)),
                integer));
        // between two integer ranges
        assertTrue(helper.isSubtype(
            df.getOWLDatatypeRestriction(integer,
                df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 4)),
            df.getOWLDatatypeRestriction(integer,
                df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 2))));
    }
}
