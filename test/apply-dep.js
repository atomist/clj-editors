var editors = require("../main");
var clj = editors.editors.main;

console.log(`check ${process.argv[2]}`)

clj.applyFingerprint(
    process.argv[2],
    {
        type: "clojure-project-deps",
        name: "org.clojure::clojure",
        data: ["org.clojure/clojure", "1.11.0"],
        sha: "a"
    }
).then(result => {
    console.log("applyFingerprint");
    console.log(result);
}, error => {
    console.log("applyFingerprint error");
    console.log(error);
});

// clj.logbackFingerprints(process.argv[2]).then(result => {
//     console.log("logback fingerprints");
//     console.log(result);
// })