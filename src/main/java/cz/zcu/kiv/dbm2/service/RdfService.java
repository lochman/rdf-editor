package cz.zcu.kiv.dbm2.service;

import cz.zcu.mre.vocab.DS;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Matej Lochman on 8.12.16.
 */

@Service
public class RdfService {

    public void fileToModel(Model model, MultipartFile file) throws IOException {
        model.read(new ByteArrayInputStream(file.getBytes()), null);
        //model.setNsPrefix("ds", DS.NS);
    }

    public Model createRdfModel(InputStream stream) {
        Model rdfModel = ModelFactory.createDefaultModel();
        rdfModel.read(stream, null);
        return rdfModel;
    }

    public OntModel createOntologyModel(InputStream stream) {
        OntModel ontologyModel = ModelFactory.createOntologyModel();
        ontologyModel.read(stream, null);
        return ontologyModel;
    }

    public void query(Model model, String queryString) {
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                RDFNode node = solution.get("");
                System.out.println(node);
            }
        }
    }

    public void simpleSelect(Model model, String subject, String predicate, String object) {
        Resource resource = model.getResource(subject);
        Property property = DS.PATIENT;
        StmtIterator iterator = model.listStatements(new SimpleSelector(resource, property, object));
        while (iterator.hasNext()) {
            Statement statement = iterator.nextStatement();
            Resource sub = statement.getSubject();
            Property pred = statement.getPredicate();
            RDFNode obj = statement.getObject();
            System.out.print(sub.toString() + " " + pred.toString() + " ");
            if (obj.isResource()) {
                System.out.print(obj.toString());
            } else {
                System.out.print("\"" + obj.toString() + "\"");
            }
            System.out.println();
        }
    }
}
