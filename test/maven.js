var editors = require("../main");
var ac = require("@atomist/automation-client")
var clj = editors.editors.main;

ac.configureLogging(ac.MinimalLogging);
ac.logger.info("hey!");
