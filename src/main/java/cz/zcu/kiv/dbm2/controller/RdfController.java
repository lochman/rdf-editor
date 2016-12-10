package cz.zcu.kiv.dbm2.controller;

import cz.zcu.kiv.dbm2.service.RdfService;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;

/**
 * Created by Matej Lochman on 14.11.16.
 */

@Controller
//@SessionAttributes({"rdfModel", "ontModel"})
@Scope("session")
@RequestMapping("/rdf")
public class RdfController {

    private org.apache.jena.rdf.model.Model rdfModel = ModelFactory.createDefaultModel();
    private OntModel ontModel = ModelFactory.createOntologyModel();

    @Autowired
    private RdfService rdfService;

    @PostMapping("/upload")
    public String loadFile(@RequestParam(value = "rdf-file", required = true) MultipartFile rdfFile,
                           @RequestParam("owl-file") MultipartFile ontFile,
                           RedirectAttributes redirectAttributes, Model model) {
        try {
//            rdfModel = rdfService.createRdfModel(new ByteArrayInputStream(rdfFile.getBytes()));
            rdfService.fileToModel(rdfModel, rdfFile, "TTL");
            if (ontFile != null) {
                rdfService.fileToModel(ontModel, ontFile, "RDF/XML");
//                ontModel = rdfService.createOntologyModel(new ByteArrayInputStream(owlFile.getBytes()));
            }

            //redirectAttributes.addFlashAttribute("message", message);
            //System.out.println(message);
        } catch (IOException e) {
            System.err.println("Failed to load files");
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/uploaded";
    }

    @PostMapping("/query")
    public String query(@RequestParam("query") String queryString) {
        rdfService.query(rdfModel, queryString);
        return "";
    }

    @PostMapping("/node")
    public String selectNode(@RequestParam("node-id") String nodeId) {
        System.out.println("Properties of node " + nodeId);
        Map<RDFNode, List<RDFNode>> properties;
        properties = rdfService.getNodeProperties(rdfModel, rdfModel.getResource(nodeId));
        String type = "";
        for (Map.Entry<RDFNode, List<RDFNode>> entry : properties.entrySet()) {
            System.out.println("values of: " + entry.getKey().toString() + ":");
            if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(entry.getKey().toString())){
                type = entry.getValue().toString().substring(1, entry.getValue().toString().length()-1) ;
            }
            System.out.println("\t" + entry.getValue().toString());
        }
        String queryString = "prefix :      <http://example.org/>\n" +
            "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "prefix owl:   <http://www.w3.org/2002/07/owl#>\n" +
            "prefix xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
            "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "select ?p ?d where {"+
            "?p rdfs:domain/(owl:unionOf/rdf:rest*/rdf:first)* ?d\n"+
            "filter (isIri(?d) && ?d =  <"+type+"> )\n"+
            "}" ;
        System.out.println("wtf proc se nic nedeje" + type);
        Query query = QueryFactory.create(queryString) ;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, ontModel)) {
            ResultSet results = qexec.execSelect() ;
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                RDFNode node = solution.get("p");
                System.out.println("je ve tridach a typech : " + node); // Get a result variable by name.
          //  Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
          //  Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
                rdfService.getNodeProperties(ontModel, node);
            }
        }
        return "node";
    }

    @PostMapping("/simpleselect")
    public String simpleSelect(@RequestParam("subject") String subject,
                               @RequestParam("predicate") String predicate,
                               @RequestParam("object") String object) {
        rdfService.simpleSelect(rdfModel, subject, predicate, object);
        return "";
    }
}
