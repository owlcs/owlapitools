package utils.reasonercomparator;

import java.io.File;
import java.io.FilenameFilter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

/** utility to lad a number of ontologies */
public class OntologyLoader {
    AutoIRIMapper mapper;

    /**
     * @param fileName
     *            name
     * @return ontology
     */
    public OWLOntology getOntology(String fileName) {
        try {
            return OWLManager.createOWLOntologyManager()
                    .loadOntologyFromOntologyDocument(new File(fileName));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return all files */
    public File[] listFiles() {
        return folders;
    }

    private File baseFile;
    private File[] folders;
    final FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File arg0, String arg1) {
            return arg1.endsWith("_main.owl") || arg1.endsWith("_main.owl.zip");
        }
    };

    /**
     * @param baseFolder
     *        base folder to list
     */
    public OntologyLoader(String baseFolder) {
        baseFile = new File(baseFolder);
        mapper = new AutoIRIMapper(baseFile, true);
        folders = baseFile.listFiles();
    }

    /**
     * @param folder
     *            base folder
     * @return ontology
     */
    public OWLOntology getOntology(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles(filenameFilter);
            if (files.length == 0) {
                System.out.println("Atomize.getOntology() No main file for "
                        + folder);
                return null;
            }
            if (files.length > 1) {
                System.out
                        .println("Atomize.getOntology() More than one main file for "
                        + folder + ", using: " + files[0].getName());
            }
            File f = files[0];
            try {
                OWLOntologyManager man = OWLManager.createOWLOntologyManager();
                man.getIRIMappers().add(mapper);
                OWLOntology o = man.loadOntologyFromOntologyDocument(f);
                return o;
            } catch (Throwable e) {
                System.out.println("Atomize.main() " + f.getName());
                e.printStackTrace(System.out);
                return null;
            }
        } else {
            return null;
        }
    }
}
