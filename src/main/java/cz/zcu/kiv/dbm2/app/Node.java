package cz.zcu.kiv.dbm2.app;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDF;

import java.util.List;
import java.util.Map;

/**
 * Created by Matej Lochman on 10.12.16.
 */

public class Node {

    private RDFNode node;
    private RDFNode type;
    private Map<RDFNode, List<RDFNode>> properties;
    private List<RDFNode> memberOfClasses;
    private Map<RDFNode, Map<RDFNode, List<RDFNode>>> classesProperties;

    public Node(RDFNode node) {
        this.node = node;
        this.type = node.asResource().getProperty(RDF.type).getObject();
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
}
