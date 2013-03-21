package org.semanticweb.owlapitools.builders;

import java.util.Arrays;
import java.util.List;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/** Builder class for OWLImportsDeclaration */
public class BuilderImportsDeclaration implements Builder<OWLImportsDeclaration> {
    private static final OWLDataFactory df = new OWLDataFactoryImpl();
    private IRI iri;

    /** @param arg
     *            IRI of imported ontology
     * @return builder */
    public BuilderImportsDeclaration withImportedOntology(IRI arg) {
        iri = arg;
        return this;
    }

    @Override
    public OWLImportsDeclaration buildObject() {
        return df.getOWLImportsDeclaration(iri);
    }

    @Override
    public List<OWLOntologyChange> buildChanges(OWLOntology o) {
        return Arrays.asList((OWLOntologyChange) new AddImport(o, buildObject()));
    }
}
