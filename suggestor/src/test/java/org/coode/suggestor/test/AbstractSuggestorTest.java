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

import org.junit.After;
import org.junit.Before;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

@SuppressWarnings("javadoc")
public abstract class AbstractSuggestorTest {

    private static final String BASE = "http://example.com#";
    protected OWLOntologyManager mngr;
    protected OWLDataFactory df;

    @Before
    public void setUp() {
        mngr = OWLManager.createOWLOntologyManager();
        df = mngr.getOWLDataFactory();
    }

    protected OWLOntology createOntology() throws OWLOntologyCreationException {
        return mngr.createOntology();
    }

    @After
    public void tearDown() {
        mngr = null;
        df = null;
    }

    protected OWLObjectProperty createObjectProperty(String name) {
        return df.getOWLObjectProperty(IRI.create(BASE + name));
    }

    protected OWLDataProperty createDataProperty(String name) {
        return df.getOWLDataProperty(IRI.create(BASE + name));
    }

    protected OWLClass createClass(String name) {
        return df.getOWLClass(IRI.create(BASE + name));
    }

    protected OWLAnnotationProperty createAnnotationProperty(String name) {
        return df.getOWLAnnotationProperty(IRI.create(BASE + name));
    }
}
