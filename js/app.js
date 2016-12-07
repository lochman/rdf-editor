var parser = N3.Parser();
var store = N3.Store();

function parseAndStore(rdfText) {
  var mickey;
  parser.parse(rdfText, function (error, triple, prefixes) {
    if (triple) {
      console.log('Saving triple: ' + triple.subject, triple.predicate, triple.object, '.');
      console.log('Status: ' + store.addTriple('\'' + triple.subject + '\'', '\'' + triple.predicate + '\'', '\'' + triple.object + '\''));
    } else { console.log("All triples in store!", prefixes) }
  });
}

function findObject(subject, predicate, object) {
  var result = store.find(subject, predicate, object)[0];
  console.log(result.subject, result.predicate, result.object, '.');
}
