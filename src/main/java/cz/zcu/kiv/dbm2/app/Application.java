package cz.zcu.kiv.dbm2.app;

import cz.zcu.mre.vocab.DS;
import cz.zcu.mre.vocab.DSCL;
import cz.zcu.mre.vocab.MRE;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Matej Lochman on 14.11.16.
 */

@SpringBootApplication(scanBasePackages = {"cz.zcu.kiv.dbm2"})
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }

    /*
    static Property nodeType = RDF.type;
    static RDFNode patient = DS.PATIENT_CLASS;    // Careful, sometimes java attribute names differ from RDF local names
    static Property dateTimeBirth = DS.DATETIME_BIRTH;

    // to prevent infinite recursion loop
    static List<Resource> traversedNodes = new ArrayList<>();

    public static void main(String[] in) {

        try {
            // parse input RDF file into model
            // using Java7 NIO (Files.*, Paths.*) for simplicity, it is possible to provide other valid BufferedReader
            // Model has graph-like structure
            Model model = ModelFactory
                    .createDefaultModel()
                    .read(Files.newBufferedReader(Paths.get("example/dataset.rdf"), Charset.forName("UTF8")), null, "N-TRIPLES");

            // ----- Save model in other formats -----
            new File("out/").mkdir();
            model.write(Files.newBufferedWriter(Paths.get("out/dataset.ttl"), Charset.forName("UTF8")), "TTL");
            model.write(Files.newBufferedWriter(Paths.get("out/dataset.nt"), Charset.forName("UTF8")), "N-TRIPLES");
            model.write(Files.newBufferedWriter(Paths.get("out/dataset.xml"), Charset.forName("UTF8")), "RDF/XML");
            model.write(Files.newBufferedWriter(Paths.get("out/dataset.json"), Charset.forName("UTF8")), "JSON-LD");
            model.write(Files.newBufferedWriter(Paths.get("out/dataset2.json"), Charset.forName("UTF8")), "RDF/JSON");

            // define prefixes in the RDF model
            model.setNsPrefix("dc", DC.NS);
            model.setNsPrefix("ds", DS.NS);
            model.setNsPrefix("dscl", DSCL.NS);
            model.setNsPrefix("mre", MRE.NS);
            model.setNsPrefix("mreid", "http://mre.zcu.cz/id/");

            // some serialization formats will benefit from defined prefixes, compare with previously created files
            model.write(Files.newBufferedWriter(Paths.get("out/prefixedDataset.ttl"), Charset.forName("UTF8")), "TTL");
            model.write(Files.newBufferedWriter(Paths.get("out/prefixedDataset.nt"), Charset.forName("UTF8")), "N-TRIPLES");
            model.write(Files.newBufferedWriter(Paths.get("out/prefixedDataset.xml"), Charset.forName("UTF8")), "RDF/XML");
            model.write(Files.newBufferedWriter(Paths.get("out/prefixedDataset.json"), Charset.forName("UTF8")), "JSON-LD");
            model.write(Files.newBufferedWriter(Paths.get("out/prefixedDataset2.json"), Charset.forName("UTF8")), "RDF/JSON");


            // ----- Get all node types in model ----
            // *** The dangerous way ***
            Set<RDFNode> nodeTypesA = findAllNodeTypesInModel1(model);
            // *** The fancy way ***
            Set<RDFNode> nodeTypesB = findAllNodeTypesInModel2(model);
            // *** Difference? ***
            System.out.format("A: %s types <= B: %s types%n", nodeTypesA.size(), nodeTypesB.size());
            // Each Patient node have two rdf:types statements (ds:Patient + ds:Male or ds:Female)
            // By sheer luck (statement order in import file is irrelevant) ds:Female was not found by getProperty()


            // ----- Print all types and resources of that type ------
            printResourcesOfTypes(model, nodeTypesB);

            // ----- Print patients sorted by age -----
            printPatientsByAge(model);

            // ----- Print addresses not in Plzen -----
            printAddressesNotInPlzen(model);

            // ----- Get property description from ontology -----
            OntModel ontology = loadOntology("example/dasta.owl");
            printPropertyInfo(dateTimeBirth, ontology);
            printPropertyInfo(DS.CLINICAL_EVENT, ontology);

            // ----- Print data hierarchy -----
            traverseGraph(model, "http://mre.zcu.cz/id/e334d34463678e290a107f4c372dca33d573de82");
            traverseGraph(model, "http://mre.zcu.cz/id/ba613d1fc0d9300175611e31cca7cf9f525056cb");

        } catch (IOException e) {
            // when parsing model from file
            System.err.println(e);
        }
    }


    static Set<RDFNode> findAllNodeTypesInModel1(Model model) {
        // get all Resources having rdf:type property
        // (i.e. resource node has at least one outgoing edge "rdf:type")
        ResIterator it = model.listResourcesWithProperty(nodeType);

        // object to keep all the different types found
        // NOTE: Set collection, by its nature, carries only distinct elements
        Set<RDFNode> nodeTypesA = new HashSet<>();

        while (it.hasNext())
            // iterates Resources, finds their rdf:type edge and get the leaf node
            // ?o from triple {it.next() rdf:type ?o .}
            // WARN: getProperty() -- If there are several such statements, any one of them may be returned.
            // 		 better to use Resource.listProperties(nodeType) and iterate
            nodeTypesA.add(it.next().getProperty(nodeType).getObject());

        return nodeTypesA;
    }

    static Set<RDFNode> findAllNodeTypesInModel2(Model model) {
        // get all objects of rdf:type properties
        // NOTE: all elements in returned list are distinct; toList() and toSet() differs in ordering of collection
        return model.listObjectsOfProperty(nodeType).toSet();
    }

    static void printResourcesOfTypes(Model model, Set<RDFNode> nodeTypesB) {
        System.out.format("%n---Print all types and resources of that type---%n");

        for (RDFNode type : nodeTypesB) {
            // get all Resources having rdf:type and specified type from previous line
            // all ?s in triples like {?s rdf:type rdfType .}
            List<Resource> list = model.listResourcesWithProperty(nodeType, type).toList();

            // print node type, number of nodes and list the relevant nodes URI
            System.out.format("(%s) %s%n", list.size(), type);
            for (Resource r : list)
                System.out.format("++  %s%n", r);
        }
    }

    static void printPatientsByAge(Model model) {
        System.out.format("%n---Print patients sorted by age---%n");

        // get list of patients
        List<Resource> patients = model.listResourcesWithProperty(nodeType, patient).toList();
        // sort by custom lambda sorting function
        patients.sort((p1, p2) -> Double.compare(
                // Statement.getString() is shortcut for Statement.getObject().asLiteral().getString()
                dateToAge(p1.getProperty(dateTimeBirth).getString()),
                dateToAge(p2.getProperty(dateTimeBirth).getString())
        ));

        for (Resource p : patients)
            System.out.format("(%.1f) %s%n", dateToAge(p.getProperty(dateTimeBirth).getString()), p.getLocalName());
    }


    // util function calculating number of years passed from input date to present
    static double dateToAge(String isoDateString) {
        // LocalDate - a date without time-zone in the ISO-8601 calendar system, such as 2007-12-03.
        try {
            LocalDate dateBirth = LocalDate.parse(isoDateString, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate dateToday = LocalDate.now();

            return (dateToday.toEpochDay() - dateBirth.toEpochDay()) / 365.25;

        } catch (DateTimeParseException e) {
            System.out.format("DateTimeParseException: %s%n", isoDateString);
            return Double.NaN;
        }
    }

    static void printAddressesNotInPlzen(Model model) {
        System.out.format("%n---Print addresses not in Plzen---%n");

        // Previously used functions
        // * model.listResourcesWithProperty(Property)
        // * model.listObjectsOfProperty(Property)
        // * model.listResourcesWithProperty(Property, RDFNode)
        // are syntactic sugar over a primitive query method model.listStatements(Selector s).

        StmtIterator iter = model.listStatements(
                // first filter
                new SimpleSelector(null, DS.ADDRESS_CITY, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement s) {
                        // additional filter(s)
                        return !s.getString().toLowerCase().contains("plzeň");
                    }
                });

        while (iter.hasNext()) {
            Statement st = iter.next();
            System.out.format("%s %s%n", st.getSubject(), st.getObject());
        }
    }

    static OntModel loadOntology(String filepath) {
        OntModel ontology = ModelFactory.createOntologyModel();

        // alternative: use web source by default and define local file as backup
//		OntDocumentManager dm = ontology.getDocumentManager();
//		if (new File(filepath).isFile()) {
//		    dm.addAltEntry("https://mre.zcu.cz/ontology/dasta.owl", "file:" + filepath);
//		}
//
//		ontology.read("https://mre.zcu.cz/ontology/dasta.owl");

        ontology.read(filepath);
        return ontology;
    }

    static void printPropertyInfo(Property property, OntModel ontology) {
        String propUri = property.toString();
        OntProperty prop = ontology.getOntProperty(propUri);

        System.out.format("%n---Print info about %s---%n", propUri);
        // careful: if there is more then one label/domain/range/type, then prop.getX() choice is arbitrary
        System.out.format("label en: \"%s\"; cs: \"%s\" %n", prop.getLabel("en"), prop.getLabel("cs"));
        System.out.format("domain: \"%s\" %n", prop.getDomain());
        System.out.format("range: \"%s\" %n", prop.getRange());

        System.out.format("RDF type: \"%s\" %n", prop.listRDFTypes(true).toList());
    }

    static void traverseGraph(Model model, String rootUri) {
        System.out.format("%n---Traversing from %s---%n", rootUri);
        traversedNodes.clear();
        printDataHierarchy(model.getResource(rootUri), 1);
    }

    static void printDataHierarchy(Resource root, int layer) {
        // keeping track of traversed nodes to prevent infinite recursion
        traversedNodes.add(root);
        System.out.format("%s%n", root.getLocalName());

        // print datatype properties
        for (Statement statement : root.listProperties().toList()) {
            RDFNode object = statement.getObject();
            if (object.isLiteral())
                System.out.format("%" + (2 * layer) + "s%s %s%n", "", statement.getPredicate().getLocalName(), object.asLiteral().toString());

        }

        // print object properties
        for (Statement statement : root.listProperties().toList()) {
            RDFNode object = statement.getObject();
            if (object.isResource() && !traversedNodes.contains(object.asResource())) {
                System.out.format("%" + (2 * layer) + "s%s ", "", statement.getPredicate().getLocalName());
                printDataHierarchy(object.asResource(), layer + 1);
            }
        }
    }
    */
}