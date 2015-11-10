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

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

@SuppressWarnings("javadoc")
public class OWLSandboxTest {
    @Test
    public void testNNF() {
        OWLOntologyManager mngr = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = mngr.getOWLDataFactory();
        OWLObjectProperty p = df.getOWLObjectProperty(IRI.create("http://example.com/p"));
        OWLClass c = df.getOWLClass(IRI.create("http://example.com/c"));
        OWLObjectComplementOf notPSomeNotD = df.getOWLObjectComplementOf(df
                .getOWLObjectSomeValuesFrom(p, df.getOWLObjectComplementOf(c)));
        System.out.println("notPSomeNotD = " + notPSomeNotD);
        System.out.println("notPSomeNotD.getNNF() = " + notPSomeNotD.getNNF());
    }
}
