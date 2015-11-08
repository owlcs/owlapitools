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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.api.PropertySuggestor;
import org.coode.suggestor.impl.SuggestorFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.Profiles;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

@SuppressWarnings("javadoc")
public class CreateExistentialTree extends AbstractSuggestorTest {

    private final Set<Node<OWLClass>> visited = new HashSet<>();

    @Override
    protected OWLOntology createOntology() throws OWLOntologyCreationException {
        return mngr.loadOntologyFromOntologyDocument(IRI
            .create("http://www.co-ode.org/ontologies/pizza/pizza.owl"));
    }

    public void testCreateTree() throws Exception {
        OWLOntology ont = createOntology();
        OWLReasoner r = Profiles.instantiateFactory(Profiles.JFact).createNonBufferingReasoner(ont);
        SuggestorFactory fac = new SuggestorFactory(r);
        PropertySuggestor ps = fac.getPropertySuggestor();
        FillerSuggestor fs = fac.getFillerSuggestor();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            visited.clear();
            printClass(r.getTopClassNode(), 0, ps, r, fs);
        }
        long end = System.currentTimeMillis();
        System.out.println("Complete in " + (end - start) + "ms");
    }

    private void printClass(Node<OWLClass> cNode, int indent, PropertySuggestor ps, OWLReasoner r, FillerSuggestor fs) {
        print(cNode, indent);
        if (visited.add(cNode)) {
            OWLClassExpression c = cNode.getRepresentativeElement();
            for (Node<OWLObjectPropertyExpression> p : ps
                .getCurrentObjectProperties(c, true)) {
                printProperty(c, p, indent + 3, fs);
            }
            for (Node<OWLClass> sub : r.getSubClasses(c, true)) {
                if (!sub.isBottomNode()) {
                    printClass(sub, indent + 1, ps, r, fs);
                }
            }
        }
    }

    private static void printProperty(OWLClassExpression c, Node<OWLObjectPropertyExpression> p, int indent,
        FillerSuggestor fs) {
        print(p, indent);
        for (Node<OWLClass> f : fs.getCurrentNamedFillers(c, p.getRepresentativeElement(), true)) {
            print(f, indent + 1);
        }
    }

    private static void print(Node<? extends OWLObject> node, int indent) {
        System.out.println();
        for (int i = 0; i < indent; i++) {
            System.out.print("    ");
        }
        List<? extends OWLObject> l = asList(node.entities().sorted());
        if (l.size() > 0) {
            out(l.get(0));
            if (l.size() > 1) {
                System.out.print(" == ");
                l.stream().skip(1).forEach(s -> out(s));
            }
        }
    }

    private static void out(OWLObject o) {
        if (o instanceof OWLEntity) {
            System.out.print(((OWLEntity) o).getIRI().getShortForm());
        } else {
            System.out.print(o);
        }
    }
}
