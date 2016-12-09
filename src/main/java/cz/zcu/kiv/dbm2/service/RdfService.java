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
import java.util.*;

/**
 * Created by Matej Lochman on 8.12.16.
 */

@Service
public class RdfService {

    public void fileToModel(Model model, MultipartFile file, String language) throws IOException {
        model.read(new ByteArrayInputStream(file.getBytes()), null, language);
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

    public List<RDFNode> query(Model model, String queryString) {
        List<RDFNode> nodes = new ArrayList<>();
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                for (String var : query.getResultVars()) {
                    nodes.add(solution.get(var));
                }
            }
        }
        return nodes;
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

    public Map<RDFNode, List<RDFNode>> getNodeProperties(Model model, String nodeId) {
        Map<RDFNode, List<RDFNode>> properties = new HashMap<>();
        Resource resource = model.getResource(nodeId);
        if (resource.isLiteral()) {
            return properties;
        }
        StmtIterator iterator = resource.listProperties();
        Statement statement;
        Property property;
        while (iterator.hasNext()) {
            statement = iterator.nextStatement();
            property = statement.getPredicate();
            if (!properties.containsKey(property)) {
                properties.put(property, new ArrayList<>());
            }
            properties.get(property).add(statement.getObject());
        }
        System.out.println("Properties:\n" + properties.toString());
        return properties;
    }

    public Map<RDFNode, List<RDFNode>> getSuggestions(Model ontModel, List<RDFNode> nodes) {
        Map<RDFNode, List<RDFNode>> suggestions = new HashMap<>();
        for (RDFNode node : nodes) {
            if (node.isResource()) {
                StmtIterator iterator = node.asResource().listProperties();
                while (iterator.hasNext()) {
                    if (!suggestions.containsKey(node)) {
                        suggestions.put(node, new ArrayList<>());
                    }
                    suggestions.get(node).add(iterator.nextStatement().getObject());
                }
            }
        }
        return suggestions;
    }
}
