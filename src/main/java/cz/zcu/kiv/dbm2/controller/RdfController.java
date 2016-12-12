package cz.zcu.kiv.dbm2.controller;

import cz.zcu.kiv.dbm2.app.Node;
import cz.zcu.kiv.dbm2.service.RdfService;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
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

    private org.apache.jena.rdf.model.Model rdfModel;
    private OntModel ontModel;
    private static Map<String, Node> handledNodes = new HashMap<>();
//    private static Map<String, String> handledQueries = new HashMap<>();
    private String rdfFilename = "";

    @Autowired
    private RdfService rdfService;

    private String getFileType(MultipartFile file) {
        String type, suffix, filename = file.getOriginalFilename();
        suffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        switch (suffix) {
            case "ttl": { type = "TTL"; break; }
            case "owl":
            case "xml": { type = "RDF/XML"; break; }
            case "nt": { type = "N-TRIPLES"; break; }
            case "json": { type = "JSON-LD"; break; }
            default: type = "TTL";
        }
        return type;
    }

    @PostMapping("/upload")
    public String loadFile(@RequestParam(value = "rdf-file") MultipartFile rdfFile,
                           @RequestParam("owl-file") MultipartFile ontFile,
                           RedirectAttributes redirectAttributes) {
        try {
            rdfModel = ModelFactory.createDefaultModel();
            rdfService.fileToModel(rdfModel, rdfFile, getFileType(rdfFile));
            rdfFilename = rdfFile.getOriginalFilename();
            if (ontFile != null) {
                ontModel = ModelFactory.createOntologyModel();
                rdfService.fileToModel(ontModel, ontFile, getFileType(ontFile));
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
        handledNodes.put(node.getNode().toString(), node);
        return node;
    }

    @GetMapping(value = "/node", params = "nodeid")
    public String getNodeById(@RequestParam("nodeid") String nodeId, Model model) {
        if (nodeId == null) {
            model.addAttribute("message", "Není specifikované URL uzlu");
            return "error";
        }
        Resource r = ResourceFactory.createResource(nodeId);
        if (!rdfModel.containsResource(r)){
            model.addAttribute("message", "Uzel v RDF nenalezen!");
            return "error";
        }
        model.addAttribute("node", createNode(nodeId));
        model.addAttribute("model", rdfModel);
        model.addAttribute("prefixes", rdfModel.getNsPrefixMap());
        return "node";
    }

    @PostMapping("/node")
    public String getNode(@RequestParam("node-id") String nodeId, Model model) {
        Resource r = ResourceFactory.createResource(nodeId);
        if (!rdfModel.containsResource(r)){
            model.addAttribute("message", "Uzel v RDF nenalezen!");
            return "error";
        }
        model.addAttribute("node", createNode(nodeId));
        model.addAttribute("model", rdfModel);
        model.addAttribute("prefixes", rdfModel.getNsPrefixMap());
        return "node";
    }

    @PostMapping("/node/save")
    public String acceptForm(@RequestParam Map<String, String> inputs, RedirectAttributes redirectAttributes) {
        String nodeId = inputs.remove("node-id");
//        System.out.println("Saving nodeid: " + nodeId);
        String query = rdfService.diffBetweenNodes(rdfModel, handledNodes.get(nodeId), inputs);
        UpdateAction.parseExecute(query, rdfModel);
        redirectAttributes.addFlashAttribute("query", "SPARQL Update query:\n" + query);
        handledNodes.remove(nodeId);
        return "redirect:/rdf/node?nodeid=" + nodeId;
    }

//    @GetMapping("/export/sparql")
//    public String getUpdateQuery() {
//        handledQueries.remove();
//        return null;
//    }

    @GetMapping("/export/rdf")
    public String getModel(HttpServletResponse response, Model model) throws IOException {
        if (rdfModel.isEmpty()) {
            model.addAttribute("message", "RDF soubor je prázdný");            
            return "error";
        }
        OutputStream outputStream = response.getOutputStream();
        response.setContentType("text/turtle");
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", rdfFilename));
        rdfModel.write(outputStream, "TTL");
        return null;
    }
    @PostMapping("/simpleselect")
    public String simpleSelect(@RequestParam("subject") String subject,
                               @RequestParam("predicate") String predicate,
                               @RequestParam("object") String object) {
        rdfService.simpleSelect(rdfModel, subject, predicate, object);
        return "";
    }
}
