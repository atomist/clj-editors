#!/usr/bin/env node

var clj = require("./index.js");

var argv = require('yargs')

  .usage('Usage: $0 <command> [options]')

  .command('read', 'read the vault',
    (yargs) => {
      yargs
      .option('file', {
        describe: 'vault file',
        default: "vault.txt"})
      .option('key', {
        describe: 'key file',
        default: "key.txt"})
    },
    (argv) => {
      console.log( clj.readVault( argv.key, argv.file ) );
    })

  .command('create key', 'create a new key',{},
    (argv) => {
      clj.createKey();
    })

  .command('merge', 'read the vault',
    (yargs) => {
      yargs
      .option('file', {
        describe: 'vault file',
        default: "vault.txt"})
      .option('key', {
        describe: 'key file',
        default: "key.txt"})
      .option('data', {
        describe: 'application/json data to merge into vault',
        default: false})
    },
    (argv) => {
      console.log(clj.mergeVault( argv.key, argv.file, argv.data ));
    })

  .argv
