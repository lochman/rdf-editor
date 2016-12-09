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
        properties = rdfService.getNodeProperties(rdfModel, nodeId);
        for (Map.Entry<RDFNode, List<RDFNode>> entry : properties.entrySet()) {
            System.out.println("values of: " + entry.getKey().toString() + ":");
            System.out.println("\t" + entry.getValue().toString());
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
