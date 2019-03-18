# sdm-vault

## install

```
npm install @atomist/clj-editors@0.3.8 -g
```

## Usage

```
12:52 $ sdm-vault --help
Usage: sdm-vault <command> [options]

Commands:
  sdm-vault read        read the vault
  sdm-vault create key  create a new key
  sdm-vault merge       read the vault

Options:
  --help     Show help                                                 [boolean]
  --version  Show version number                                       [boolean]

```

For example, you can read the existing vault file with:

```
sdm-vault read --key key.txt --file ~/repo/clojure-sdm/resources/vault.txt
```

where the contents of the key.txt file are stored in our 1password account with the title `vault.txt encryption key`.

Merge in some more data using:

```
sdm-vault merge --key key.txt --file ~/repo/clojure-sdm/resources/vault.txt --data `{"key": "value"}`
```

Top-level fields in the vault map will be merged into the environment that clojure-sdm uses to run processes.  The key
will be transformed to all uppercase, with `-` migrated to `_`.  Do not use spaces in names.

We current have a clojure-sdm wide vault.txt that we distribute from ~/repo/clojure-sdm/resources/vault.txt, but local
copies of vault.txt in the root of a git repository will be merged, with the local version over-riding anything in the
team copy.
