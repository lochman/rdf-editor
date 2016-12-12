package cz.zcu.kiv.dbm2.service;

import cz.zcu.kiv.dbm2.app.Node;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
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

    //prepare guide values for node
    public Map<RDFNode, List<String>>getGuideValues(Model rdfModel, Model ontModel, Map<RDFNode, List<RDFNode>> guideObjects) {
        Map<RDFNode, List<String>> guideValues = new HashMap();
        //for every class
        for (Map.Entry<RDFNode, List<RDFNode>> entry : guideObjects.entrySet()) {
            guideValues.put(entry.getKey(), new ArrayList());
            System.out.println("entry je " + entry.getValue());
            //for every range (usualy one)
            for (RDFNode object : entry.getValue()) {
                ResIterator iter = rdfModel.listResourcesWithProperty(RDF.type, object);
                System.out.println("Pro object " + object.toString() + " nalezeno:");
                //add all found instances
                while(iter.hasNext()){
                    Resource r = iter.next();
                  //  System.out.println("bonus" + r.getPropertyResourceValue(RDF.type));
                    String inst = r.toString();
                    if (ontModel.qnameFor(inst) != null) inst = rdfModel.qnameFor(inst);
                    System.out.println("\t res: "+ inst);
                    guideValues.get(entry.getKey()).add(inst);
                }
                //add all named individuals
                ResIterator iter2 = ontModel.listResourcesWithProperty(RDF.type, object); 
                while(iter2.hasNext()){
                    Resource r = iter2.next();
                   // System.out.println(r.getPropertyResourceValue(RDF.type));
                    /*StmtIterator st = r.listProperties(RDF.type);
                    while(st.hasNext()){
                    Statement s = st.next();
                   
                    System.out.println("bonus" +r.getPropertyResourceValue(RDF.type)+" " +s.toString() + " " + s.getObject().toString());
                    }*/
                    String inst = r.toString();
                    if (ontModel.qnameFor(inst) != null) inst = ontModel.qnameFor(inst);
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
        Property property = model.getProperty(predicate);
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
//        System.out.println("Properties:\n" + properties.toString());
        return properties;
    }

    public Map<RDFNode, Map<RDFNode, List<RDFNode>>> getClassesProperties(Model model, String type) {
        Map<RDFNode, Map<RDFNode, List<RDFNode>>> classesProperties = new HashMap<>();
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
//                System.out.println("is in: " + node);
                classesProperties.put(node, getNodeProperties(model, node));
            }
        }
        return classesProperties;
    }

    private String buildUpdateQuery(String nodeId, Map<String, String> deleteValues, Map<String, String> insertValues) {
        StringBuilder query = new StringBuilder();
        nodeId = "<" + nodeId + ">";
        query.append("DELETE {\n");
        for (Map.Entry<String, String> entry : deleteValues.entrySet()) {
            query.append(nodeId + " <" + entry.getKey() + "> \"" + entry.getValue() + "\" .\n");
        }
        query.append("}\nINSERT {\n");
        for (Map.Entry<String, String> entry : insertValues.entrySet()) {
            query.append(nodeId + " <" + entry.getKey() + "> \"" + entry.getValue() + "\" .\n");
        }
        query.append("}\nWHERE { }\n");
        return query.toString();
    }

    private String appendDataType(Node node, Resource resource, String value) {
        Map<RDFNode, Map<RDFNode, List<RDFNode>>> classesProperties = node.getClassesProperties();
        Map<RDFNode, List<RDFNode>> properties = classesProperties.get(resource);
        String property;
        if (properties.containsKey(RDFS.range) && properties.get(RDFS.range).size() > 0) {
            property = properties.get(RDFS.range).get(0).toString();
            if (property.contains("http://www.w3.org/2001/XMLSchema#")) {
                value += "^^" + property;
            }
        }
        return value;
    }

    public String diffBetweenNodes(Model model, Node node, Map<String, String> inputs) {
        Map<String, String> deleteValues = new HashMap<>();
        Map<String, String> insertValues = new HashMap<>();
        List<RDFNode> previousValues;
        int index, delimPosition;
        String nodeid, value;
        Resource resource;
        Map<RDFNode, List<RDFNode>> properties = node.getProperties();
        for (Map.Entry<String, String> input : inputs.entrySet()) {
            delimPosition = input.getKey().lastIndexOf("-");
            nodeid = input.getKey().substring(0, delimPosition);
            try {
                index = Integer.parseInt(input.getKey().substring(delimPosition + 1, input.getKey().length()));
            } catch (NumberFormatException e) {
                index = 0;
            }
//            System.out.println("index " + index);
            resource = model.getResource(nodeid);
            if (!properties.containsKey(resource)) {
                if (StringUtils.isBlank(input.getValue())) { continue; }
                insertValues.put(nodeid, appendDataType(node, resource, input.getValue()));
//                System.out.println("insertValues[" + nodeid + "] = " + input.getValue());
            } else {
                previousValues = properties.get(resource);
//                System.out.println(previousValues.get(index) + " ?==? " + input.getValue());
                value = appendDataType(node, resource, input.getValue());
                if (StringUtils.isBlank(input.getValue())) {
                    deleteValues.put(nodeid, previousValues.get(index).toString());
                } else if (!Objects.equals(previousValues.get(index).toString(), value)) {
                    deleteValues.put(nodeid, previousValues.get(index).toString());
                    insertValues.put(nodeid, value);
                }
            }
        }
        return buildUpdateQuery(node.getNode().toString(), deleteValues, insertValues);
    }
}
