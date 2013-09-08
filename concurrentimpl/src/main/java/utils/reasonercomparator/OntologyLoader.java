package utils.reasonercomparator;

import java.io.File;
import java.io.FilenameFilter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class OntologyLoader {
    AutoIRIMapper mapper;
    OWLOntologyManager m;

    public OWLOntology getOntology(String fileName) {
        try {
            return m.loadOntologyFromOntologyDocument(new File(fileName));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public File[] listFiles() {
        return folders;
    }

    private File baseFile;
    private File[] folders;
    final FilenameFilter filenameFilter = new FilenameFilter() {
        public boolean accept(File arg0, String arg1) {
            return arg1.endsWith("_main.owl") || arg1.endsWith("_main.owl.zip");
        }
    };

    public OntologyLoader(String baseFolder) {
        baseFile = new File(baseFolder);
        mapper = new AutoIRIMapper(baseFile, true);
        folders = baseFile.listFiles();
    }

    public OWLOntology getOntology(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles(filenameFilter);
            if (files.length == 0) {
                System.out.println("Atomize.getOntology() No main file for " + folder);
                return null;
            }
            if (files.length > 1) {
                System.out.println("Atomize.getOntology() More than one main file for "
                        + folder + ", using: " + files[0].getName());
            }
            File f = files[0];
            try {
                OWLOntologyManager m = OWLManager.createOWLOntologyManager();
                m.addIRIMapper(mapper);
                OWLOntology o = m.loadOntologyFromOntologyDocument(f);
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
