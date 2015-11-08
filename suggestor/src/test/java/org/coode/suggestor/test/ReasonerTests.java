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

import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.Profiles;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

@SuppressWarnings("javadoc")
@Ignore
public class ReasonerTests {

    public static final String JFACT_FACTORY = "uk.ac.manchester.cs.jfact.JFactFactory";

    @Test
    public void testReasoner() throws Exception {
        OWLOntologyManager mngr = OWLManager.createOWLOntologyManager();
        OWLOntology ont = mngr.createOntology();
        OWLReasoner r = Profiles.instantiateFactory(Profiles.JFact).createNonBufferingReasoner(ont);
        OWLDataFactory df = mngr.getOWLDataFactory();
        OWLClass a = df.getOWLClass(IRI.create("http://example.com/a"));
        OWLClass b = df.getOWLClass(IRI.create("http://example.com/b"));
        OWLClass c = df.getOWLClass(IRI.create("http://example.com/c"));
        OWLObjectProperty p = df.getOWLObjectProperty(IRI.create("http://example.com/p"));
        mngr.applyChange(new AddAxiom(ont, df.getOWLSubClassOfAxiom(a,
            df.getOWLObjectSomeValuesFrom(p, b))));
        mngr.applyChange(new AddAxiom(ont, df.getOWLSubClassOfAxiom(c, df.getOWLThing())));
        NodeSet<OWLClass> subs = r.getSubClasses(df.getOWLObjectSomeValuesFrom(p, b),
            true);
        assertEquals(1L, subs.nodes().count());
        assertTrue(subs.containsEntity(a));
        subs = r.getSubClasses(df.getOWLObjectSomeValuesFrom(p, df.getOWLThing()), true);
        assertEquals(1L, subs.nodes().count());
        assertTrue(subs.containsEntity(a));
        subs = r.getSubClasses(
            df.getOWLObjectSomeValuesFrom(df.getOWLTopObjectProperty(),
                df.getOWLThing()), true);
        assertEquals(3L, subs.nodes().count());
        assertTrue(subs.containsEntity(a));
        assertTrue(subs.containsEntity(b));
        assertTrue(subs.containsEntity(c));
    }

    @Test
    public void testReasoner4() throws Exception {
        OWLOntologyManager mngr = OWLManager.createOWLOntologyManager();
        OWLOntology ont = mngr.createOntology();
        OWLReasoner r = Profiles.instantiateFactory(Profiles.JFact).createNonBufferingReasoner(ont);
        assertTrue(r.getTopDataPropertyNode().entities().count() > 0);
    }
}
