/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.impl.SuggestorFactory;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLFacet;

@SuppressWarnings("javadoc")
public class FillerSuggestorTest extends AbstractSuggestorTest {

    private OWLClass ca = createClass("ca");
    private OWLClass ca1 = createClass("ca1");
    private OWLClass cb = createClass("cb");
    private OWLClass cb1 = createClass("cb1");
    private OWLClass cc = createClass("cc");
    private OWLClass cc1 = createClass("cc1");
    private OWLClass cd = createClass("cd");
    private OWLClass ce = createClass("ce");
    private OWLObjectProperty oa = createObjectProperty("oa");
    private OWLObjectProperty ob = createObjectProperty("ob");
    private OWLObjectProperty ob1 = createObjectProperty("ob1");
    private OWLDataProperty da = createDataProperty("da");

    /*
     * ca -> oa some cb // "redundant" ca -> oa some cb1 ca -> ob1 some cb ca ->
     * da some integer cd == oa some cb cd -> oa some ce cb1 -> cb cc1 -> cc ca1
     * -> ca ob1 -> ob
     */
    private OWLOntology createModelA() throws Exception {
        OWLOntology ont = createOntology();
        ont.add(df.getOWLSubClassOfAxiom(cb1, cb));
        ont.add(df.getOWLSubClassOfAxiom(ca1, ca));
        ont.add(df.getOWLSubObjectPropertyOfAxiom(ob1, ob));
        ont.add(df.getOWLSubClassOfAxiom(ca, df.getOWLObjectSomeValuesFrom(oa, cb)));
        ont.add(df.getOWLSubClassOfAxiom(ca, df.getOWLDataSomeValuesFrom(da, df.getIntegerOWLDatatype())));
        ont.add(df.getOWLEquivalentClassesAxiom(cd, df.getOWLObjectSomeValuesFrom(oa, cb)));
        ont.add(df.getOWLSubClassOfAxiom(cd, df.getOWLObjectSomeValuesFrom(oa, ce)));
        ont.add(df.getOWLSubClassOfAxiom(ca, df.getOWLObjectSomeValuesFrom(oa, cb1)));
        ont.add(df.getOWLSubClassOfAxiom(ca, df.getOWLObjectSomeValuesFrom(ob1, cb)));
        ont.add(df.getOWLSubClassOfAxiom(cc1, cc));
        return ont;
    }

    @Test
    public void testIsCurrentFiller() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = factory.createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        assertFalse(fs.isCurrent(ca, oa, cb1, true));
        assertTrue(fs.isCurrent(ca, oa, cb, true));
        assertFalse(fs.isCurrent(ca, oa, df.getOWLObjectIntersectionOf(cb, df.getOWLObjectSomeValuesFrom(ob, cc)),
            true));
        assertFalse(fs.isCurrent(ca, oa, cc, true));
        assertTrue(fs.isCurrent(ca, oa, ce, false)); // from interaction with d
        assertTrue(fs.isCurrent(ca, da, df.getIntegerOWLDatatype(), false));
        assertFalse(fs.isCurrent(ca, da, df.getOWLDatatypeRestriction(df.getIntegerOWLDatatype(),
            OWLFacet.MIN_INCLUSIVE, df.getOWLLiteral(2)), true));
        assertTrue(fs.isCurrent(ca, oa, df.getOWLThing(), true));
        assertTrue(fs.isCurrent(ca, oa, cb1));
        assertTrue(fs.isCurrent(ca, oa, cb));
        assertFalse(fs.isCurrent(ca, oa, df.getOWLObjectIntersectionOf(cb, df.getOWLObjectSomeValuesFrom(ob, cc))));
        assertFalse(fs.isCurrent(ca, oa, cc));
        assertTrue(fs.isCurrent(ca, oa, df.getOWLThing()));
        assertTrue(fs.isCurrent(ca, da, df.getIntegerOWLDatatype()));
        assertTrue(fs.isCurrent(ca, da, df.getTopDatatype()));
        // inherited
        assertFalse(fs.isCurrent(ca1, oa, cb1, true));
        assertTrue(fs.isCurrent(ca1, oa, cb1));
    }

    /*
     * ca -> not(oa some cb) cb1 -> cb cc1 -> cc
     */
    private OWLOntology createModelB() throws Exception {
        OWLOntology ont = createOntology();
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(ca, df.getOWLObjectComplementOf(df
            .getOWLObjectSomeValuesFrom(oa, cb)))));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cb1, cb)));
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cc1, cc)));
        mngr.applyChanges(changes);
        return ont;
    }

    @Test
    public void testIsPossibleFiller() throws Exception {
        OWLOntology ont = createModelB();
        OWLReasoner r = factory.createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        assertFalse(fs.isPossible(ca, oa, ca));
        assertFalse(fs.isPossible(ca, oa, cb));
        assertFalse(fs.isPossible(ca, oa, cc));
        assertFalse(fs.isPossible(ca, oa, cc1));
        assertFalse(fs.isPossible(ca, oa, df.getOWLThing()));
    }

    /*
     * ca -> oa some cb ca -> ob some cc ca -> ob some cd ob -> oa cd -> cc
     */
    @Test
    public void testGetCurrentFillers() throws Exception {
        OWLOntology ont = createOntology();
        ont.add(df.getOWLSubClassOfAxiom(ca, df.getOWLObjectSomeValuesFrom(oa, cb)));
        ont.add(df.getOWLSubClassOfAxiom(ca, df.getOWLObjectSomeValuesFrom(ob, cc)));
        ont.add(df.getOWLSubClassOfAxiom(ca, df.getOWLObjectSomeValuesFrom(ob, cd)));
        ont.add(df.getOWLSubObjectPropertyOfAxiom(ob, oa));
        ont.add(df.getOWLSubClassOfAxiom(cd, cc));
        OWLReasoner r = factory.createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        NodeSet<OWLClass> all = fs.getCurrentNamedFillers(ca, oa, false);
        assertTrue(all.containsEntity(cb));
        assertTrue(all.containsEntity(cc));
        assertTrue(all.containsEntity(cd));
        assertTrue(all.containsEntity(df.getOWLThing()));
        assertEquals(4L, all.nodes().count());
        NodeSet<OWLClass> direct = fs.getCurrentNamedFillers(ca, oa, true);
        assertTrue(direct.containsEntity(cb));
        // as cd is more specific
        assertFalse(direct.containsEntity(cc));
        assertTrue(direct.containsEntity(cd));
        // as more specific properties have been found
        assertFalse(direct.containsEntity(df.getOWLThing()));
        assertEquals(2L, direct.nodes().count());
    }

    @Test
    public void testGetPossibleFillers() throws Exception {
        OWLOntology ont = createModelA();
        OWLReasoner r = factory.createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        // PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        NodeSet<OWLClass> pSuccessorsA = fs.getPossibleNamedFillers(ca, oa, null, false);
        // assertTrue(pSuccessorsA.containsEntity(ca));
        // assertTrue(pSuccessorsA.containsEntity(ca1));
        assertTrue(pSuccessorsA.containsEntity(cb));
        assertTrue(pSuccessorsA.containsEntity(cb1));
        // assertTrue(pSuccessorsA.containsEntity(cc));
        // assertTrue(pSuccessorsA.containsEntity(cc1));
        // assertTrue(pSuccessorsA.containsEntity(cd));
        assertTrue(pSuccessorsA.containsEntity(ce));
        assertEquals(3L, pSuccessorsA.nodes().count());
        NodeSet<OWLClass> pSuccessorsADirect = fs.getPossibleNamedFillers(ca, oa, null, true);
        // not ca as it is a sub of cd
        assertTrue(pSuccessorsADirect.containsEntity(cb));
        // assertTrue(pSuccessorsADirect.containsEntity(cc));
        // assertTrue(pSuccessorsADirect.containsEntity(cd));
        assertTrue(pSuccessorsADirect.containsEntity(ce));
        assertEquals(2L, pSuccessorsADirect.nodes().count());
    }
}
