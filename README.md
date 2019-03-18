## packaging ClojureScript in NPM

We build a node module `@atomist/clj-editors` here.  This allows us to construct clojure editors
using [`rewrite-cljs`][rewrite-cljs].  A Node.js application, like an Atomist automation client, can now easily
package clojure editing functions into a typescript application.

This also has a simple command line to manage the clojure-sdm credentials.  Install this module with:

```
npm install @atomist/clj-editors@0.3.8 -g
```
then

```
sdm-vault --help
```

to manage maps encrypted with `goog.crypt.Aes` keys.

### clj-editors Usage

Add the dependency to your application's `package.json` file.

```
{
  "dependencies": {
    ...
    "@atomist/clj-editors": "^0.3.8",
    ...
   },
   ...
}
```

## npm link

This module works with `npm link` so we can co-develop these editors alongside the sdm-pack-clojure.
These editors are pretty easy to unit test, and testing from a Node.js repl works quite well!

## Development

### Compiling

```
npm run compile
```

### Repl

```
npm run repl
```

This will give you a socket repl running on port 7777, so you can connect emacs or intellij and evaluate the cljs forms
on the fly.  It's a pretty good workflow, and contains `clojure.test` (although it's in `cljs.test` here).  I haven't
gotten it working with `nrepl` so it's not quite as smooth as working with a Java repl but it can do the main things:

- evaluate forms by passing them through `cljs.repl`, whether they are compiled to `.js`
- `.js` expressions are sent to a node.js process running alongside the `cljs.repl`
- `(deftest)` forms work great so you can code and evaluate unit tests using our standard workflow.

### Unit Tests

Run cljs.test unit tests using npm:

```
npm run test
```

### Publish

It's just normal `npm publish` for the rest.

[rewrite-cljs]: https://github.com/rundis/rewrite-cljs
[api]: http://rundis.github.io/rewrite-cljs

## sdm-vault

This project also [has a simple command line to manipulate vault.txt files.](docs/sdm-vault.md)
