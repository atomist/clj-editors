var editors = require("../main");
var clj = editors.editors.main;

console.log(`check ${process.argv[2]}`)

clj.leinDeps(process.argv[2]).then(result => {
    console.log("leinDeps");
    console.log(result);
});

clj.leinCoordinates(process.argv[2]).then(result => {
    console.log("leinCoordinates");
    console.log(result);
});

// clj.logbackFingerprints(process.argv[2]).then(result => {
//     console.log("logback fingerprints");
//     console.log(result);
// })