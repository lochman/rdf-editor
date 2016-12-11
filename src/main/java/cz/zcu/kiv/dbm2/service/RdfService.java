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
import org.apache.jena.vocabulary.RDF;

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

    //prepare guide values for node
    public Map<RDFNode, List<String>>getGuideValues(Model rdfModel, Model ontModel, Map<RDFNode, List<RDFNode>> guideObjects) {
        Map<RDFNode, List<String>> guideValues = new HashMap();
        //for every class
        for (Map.Entry<RDFNode, List<RDFNode>> entry : guideObjects.entrySet()) {
            guideValues.put(entry.getKey(), new ArrayList());
            //for every range (usualy one)
            for (RDFNode object : entry.getValue()) {
                ResIterator iter = rdfModel.listResourcesWithProperty(RDF.type, object);
                System.out.println("Pro object " + object.toString() + " nalezeno:");
                //add all found instances
                while(iter.hasNext()){
                    String inst = iter.next().toString();
                    System.out.println("\t res: "+ inst);
                    guideValues.get(entry.getKey()).add(inst);
                }
                //add all named individuals
                ResIterator iter2 = ontModel.listResourcesWithProperty(RDF.type, object); 
                while(iter2.hasNext()){
                    String inst = iter2.next().toString();
                    System.out.println("\t enum: "+ inst);
                    guideValues.get(entry.getKey()).add(inst);
                }  
            }
        }
        return guideValues;
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

    public Map<RDFNode, List<RDFNode>> getNodeProperties(Model model, RDFNode node) {
        Map<RDFNode, List<RDFNode>> properties = new HashMap<>();
        if (node.isLiteral()) {
            return properties;
        }
        StmtIterator iterator = node.asResource().listProperties();
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

    public Map<RDFNode, Map<RDFNode, List<RDFNode>>> getClassesProperties(Model model, String type) {
        Map<RDFNode, Map<RDFNode, List<RDFNode>>> classesProperties = new HashMap<>();
        //"prefix :      <http://example.org/>\n" +
        String queryString = "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                             "prefix owl:   <http://www.w3.org/2002/07/owl#>\n" +
                             "prefix xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
                             "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                             "select ?p ?d where {" +
                             "?p rdfs:domain/(owl:unionOf/rdf:rest*/rdf:first)* ?d\n" +
                             "filter (isIri(?d) && ?d =  <" + type + "> )\n" +
                             "}";
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                RDFNode node = solution.get("p");
                System.out.println("is in: " + node);
                classesProperties.put(node, getNodeProperties(model, node));
            }
        }
        return classesProperties;
    }
}
