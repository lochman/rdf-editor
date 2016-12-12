package cz.zcu.kiv.dbm2.controller;

import cz.zcu.kiv.dbm2.app.Node;
import cz.zcu.kiv.dbm2.service.RdfService;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static Map<String, Node> handledNodes = new HashMap<>();

    @Autowired
    private RdfService rdfService;

    @PostMapping("/upload")
    public String loadFile(@RequestParam(value = "rdf-file") MultipartFile rdfFile,
                           @RequestParam("owl-file") MultipartFile ontFile,
                           RedirectAttributes redirectAttributes) {
        try {
            rdfService.fileToModel(rdfModel, rdfFile, "TTL");
            if (ontFile != null) {
                rdfService.fileToModel(ontModel, ontFile, "RDF/XML");
            }
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

    private Node createNode(String nodeId) {
        RDFNode rdfNode = rdfModel.getResource(nodeId);
        if (rdfNode.isLiteral()) {  } // TODO: handle literal
        Node node = new Node(rdfNode);
        node.setProperties(rdfService.getNodeProperties(rdfModel, rdfNode));
        node.setClassesProperties(rdfService.getClassesProperties(ontModel, node.getType().toString()));
        node.setMemberOfClasses(new ArrayList<>(node.getClassesProperties().keySet()));

        node.parseInputParams();
        node.setGuideValues(rdfService.getGuideValues(rdfModel, ontModel, node.getGuideObjects()));
        System.out.println("created node " + node.getNode().toString());
        handledNodes.put(node.getNode().toString(), node);
        return node;
    }

    @GetMapping(value = "/node", params = "nodeid")
    public String getNodeById(@RequestParam("nodeid") String nodeId, Model model) {
        if (nodeId == null) {
            model.addAttribute("message", "No ID specified");
            return "error";
        }
        model.addAttribute("node", createNode(nodeId));
        model.addAttribute("model", rdfModel);
        model.addAttribute("prefixes", rdfModel.getNsPrefixMap());
        return "node";
    }

    @PostMapping("/node")
    public String getNode(@RequestParam("node-id") String nodeId, Model model) {
        model.addAttribute("node", createNode(nodeId));
        model.addAttribute("model", rdfModel);
        model.addAttribute("prefixes", rdfModel.getNsPrefixMap());
        return "node";
    }

    @PostMapping("/node/save")
    public String acceptForm(@RequestParam Map<String, String> inputs, Model model) {
        String nodeId = inputs.remove("node-id");
//        System.out.println("Saving nodeid: " + nodeId);
        String query = rdfService.diffBetweenNodes(rdfModel, handledNodes.get(nodeId), inputs);
        UpdateAction.parseExecute(query, rdfModel);
        model.addAttribute("message", query);
        handledNodes.remove(nodeId);
        return "error";
    }

    @GetMapping("/export/rdf")
    @ResponseBody
    public byte[] getModel() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        rdfModel.write(outputStream, "TTL");
        rdfModel.write(System.out, "TTL");
        return outputStream.toByteArray();
    }

    @PostMapping("/simpleselect")
    public String simpleSelect(@RequestParam("subject") String subject,
                               @RequestParam("predicate") String predicate,
                               @RequestParam("object") String object) {
        rdfService.simpleSelect(rdfModel, subject, predicate, object);
        return "";
    }
}
