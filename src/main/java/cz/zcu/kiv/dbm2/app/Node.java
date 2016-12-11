package cz.zcu.kiv.dbm2.app;

import cz.zcu.mre.vocab.IBD;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;
import org.apache.jena.vocabulary.OWL;

/**
 * Created by Matej Lochman on 10.12.16.
 */

public class Node {

    public static final RDFNode LABEL = RDFS.label;
    public static final RDFNode INPUT_TYPE = RDFS.range;
    public static final RDFNode CARDINALITY = IBD.CARDINALITY;
    public static final String CARDINALITY_1 = "1";
    public static final String CARDINALITY_N = "N";
    public static final String NON_LITERAL = "nonLit";

    private RDFNode node;
    private RDFNode type;
    private Map<RDFNode, List<RDFNode>> properties;
    //list of owl classes (objects) to query for guide values
    private Map<RDFNode, List<RDFNode>> guideObjects;
    //guide values for each predicate
    private Map<RDFNode, List<String>> guideValues;
    private List<RDFNode> memberOfClasses;
    private Map<RDFNode, Map<RDFNode, List<RDFNode>>> classesProperties;
    private Map<RDFNode, Map<RDFNode, String>> inputParams;

    public Node(RDFNode node) {
        this.node = node;
        this.type = node.asResource().getProperty(RDF.type).getObject();
        properties = new HashMap<>();
        memberOfClasses = new ArrayList<>();
        guideObjects = new HashMap<>();
        guideValues = new HashMap<>();
        classesProperties = new HashMap<>();
        inputParams = new HashMap<>();
    }

    private String parseLabel(List<RDFNode> labels) {
        for (RDFNode node : labels) {
            if ("cs".equals(node.asLiteral().getLanguage())) {
                return node.toString().substring(0, node.toString().length() - 3);
            }
        }
        return "";
    }

    private String parseInputType(Map<RDFNode, List<RDFNode>> properties) {
        String string = "", type;
        RDFNode node;
        if (!properties.containsKey(RDFS.range)) { return ""; }
        node = properties.get(RDFS.range).get(0);
        type = node.toString();
        if ("http://www.w3.org/2001/XMLSchema#date".equals(type)) {
            string = "date";
        } else if ("http://www.w3.org/2001/XMLSchema#boolean".equals(type)) {
            string = "checkbox";
        } else if ("http://www.w3.org/2001/XMLSchema#integer".equals(type)) {
            string = "number";
        } else if ("http://www.w3.org/2001/XMLSchema#string".equals(type)) {
            string = "text";
        } else {
            string = NON_LITERAL;
        }
        return string;
    }

    private String getParam(Map<RDFNode, List<RDFNode>> properties, RDFNode property) {
        String param;
        List<RDFNode> prop;
        if (!properties.containsKey(property)) { return ""; }
        prop = properties.get(property);
        if (RDFS.label.equals(property)) {
            return parseLabel(prop);
        }
        param = prop.get(0).toString();
        return param;
    }

    public void parseInputParams() {
        for (Map.Entry<RDFNode, Map<RDFNode, List<RDFNode>>> entry : classesProperties.entrySet()) {
//            for (Map.Entry<RDFNode, List<RDFNode>> property : entry.getValue().entrySet()) {}
            Map<RDFNode, String> map = new HashMap<>();
            map.put(LABEL, getParam(entry.getValue(), RDFS.label));
            map.put(INPUT_TYPE, parseInputType(entry.getValue()));
            System.out.println("je to objectproperty" +entry.getValue().get(RDF.type).contains(OWL.ObjectProperty) + " cardinalita " +entry.getValue().get(IBD.CARDINALITY));
            //object property needs to browse deeper
            if(entry.getValue().get(RDF.type).contains(OWL.ObjectProperty)){           
                //naseptavani
                guideObjects.put(entry.getKey(), entry.getValue().get(RDFS.range));
                //only one to one mapping                owl:NamedIndividual  
               if(entry.getValue().get(RDF.type).contains(OWL.FunctionalProperty)){
                    System.out.println("\tsingle cardinality " + entry.getValue().get(RDFS.range));
                    
                }else{
                    //can have multiple values
                    System.out.println("\tmultiple cardinality " + entry.getValue().get(RDFS.range));
                }
            }
            else if(entry.getValue().get(RDF.type).contains(OWL.DatatypeProperty)) {
                if(entry.getValue().get(RDF.type).contains(OWL.FunctionalProperty)){
                    System.out.println("\tsingle datatype " + entry.getValue().get(RDFS.range));
                    
                }else{
                    //can have multiple values
                    System.out.println("\tmultiple datattype " + entry.getValue().get(RDFS.range));
                }
            }
            inputParams.put(entry.getKey(), map);
        }
    }

    public RDFNode getNode() {
        return node;
    }

    public void setNode(RDFNode node) {
        this.node = node;
    }

    public RDFNode getType() {
        return type;
    }

    public void setType(RDFNode type) {
        this.type = type;
    }

    public Map<RDFNode, List<RDFNode>> getProperties() {
        return properties;
    }

    public void setProperties(Map<RDFNode, List<RDFNode>> properties) {
        this.properties = properties;
    }

    public List<RDFNode> getMemberOfClasses() {
        return memberOfClasses;
    }

    public void setMemberOfClasses(List<RDFNode> memberOfClasses) {
        this.memberOfClasses = memberOfClasses;
    }

    public Map<RDFNode, Map<RDFNode, List<RDFNode>>> getClassesProperties() {
        return classesProperties;
    }

    public void setClassesProperties(Map<RDFNode, Map<RDFNode, List<RDFNode>>> classesProperties) {
        this.classesProperties = classesProperties;
    }

    public Map<RDFNode, Map<RDFNode, String>> getInputParams() {
        return inputParams;
    }

    public void setInputParams(Map<RDFNode, Map<RDFNode, String>> inputParams) {
        this.inputParams = inputParams;
    }

    public Map<RDFNode, List<RDFNode>> getGuideObjects() {
        return guideObjects;
    }

    public void setGuideObjects(Map<RDFNode, List<RDFNode>> guideObjects) {
        this.guideObjects = guideObjects;
    }

    public Map<RDFNode, List<String>> getGuideValues() {
        return guideValues;
    }

    public void setGuideValues(Map<RDFNode, List<String>> guideValues) {
        this.guideValues = guideValues;
    }
    
}
