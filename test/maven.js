var editors = require("../main");
var clj = editors.editors.main;

console.log("hello");
console.log("getName:  " + clj.getName("project.clj"));
console.log("getVersion:  " + clj.getVersion("project.clj"));

clj.leinDeps(".").then(result => {
    console.log("leinDeps");
    console.log(result);
});

clj.leinCoordinates(".").then(result => {
    console.log("leinCoordinates");
    console.log(result);
});

clj.mavenCoordinates(".").then(result => {
    console.log("mavenCoordinates");
    console.log(result);
});

clj.mavenDeps(".").then(result => {
    console.log("mavenDeps");
    console.log(result);
});