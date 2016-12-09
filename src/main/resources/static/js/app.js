/*
var parser = N3.Parser();
var store = N3.Store();

function testSize(){	
	console.log("wtf mrdko 2 "+store.size);
}
function parseAndStore(rdfText, cb) {
  var mickey;
  parser.parse(rdfText, function (error, triple, prefixes) {
    if (error){
		return cb(error);
	}
	if (triple) {
		console.log('Saving triple: ' + triple.subject, triple.predicate, triple.object, '.');
		//console.log('Status: ' + store.addTriple(triple.subject, triple.predicate, triple.object));
		store.addTriple(triple.subject, triple.predicate, triple.object)
		//  console.log (store.size);
    } else { console.log("All triples in store!", prefixes); cb();}
  });  
}
function findObject(subject, predicate, object) {
  console.log("koukej velikost uz je "+store.size + subject);
  var result = store.find(subject, predicate, object)[0];
  console.log(result);
  console.log(result.subject, result.predicate, result.object, '.');
}
*/