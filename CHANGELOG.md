# Changelog

## [unreleased]

## [0.6.0]

- #34:

  - adds `mentat.clerk-utils.build/release->cas!` for sending compiled JS
    bundles up to Garden.

  -  adds `:custom-js` options to `mentat.clerk-utils.build/{build!, serve!}`
     for specifying a different bundle than Clerk's built-in bundle (from
     [Emmy-Viewers](https://github.com/mentat-collective/emmy-viewers), for
     example)

- #33 modifies `mentat.clerk-utils.build.shadow/install-npm-deps!` to skip
  running an extra npm commnand if there are no uninstalled dependencies in any
  `deps.cljs` files.

## [0.5.1]

- #32 modifies `mentat.clerk-utils.build.shadow` to include
  `nextjournal.clerk.static-app` only when available, adding backwards
  compatibility in for previous Clerk versions.

- #31 upgrades Clerk in the project and the template to
  `"5e875e256a28a6deabf27bd6fc20f44cee5dad20"`, past the sha that removed
  `nextjournal.clerk.static-app` in favor of `nextjournal.clerk.sci-env`.

## [0.5.0]

- #30 adds better support for Windows by invoking `npm` using `npm.bat` when
  appropriate.

- #29 converts `mentat.clerk-utils` to `cljc`. The CLJS side doesn't do
  anything, but this allows the require and macro calls to live inside `cljc`
  files.

- #28:

  - Removes the `babashka` dependency from `mentat.clerk-utils.build`

  - `mentat.clerk-utils.show/q` now applies to the forms passed to `show-sci`,
    allowing for value-splicing, namespace resolution etc inside of `show-sci`.

- #24 renames the clj-kondo hooks to have extension `.clj_kondo`, for the same
  reason as #23.

- #23 renames `.clj` and `.cljs` files in the templates to `.tmpl`, to prevent
  cljdoc and other tooling from trying to process them as Clojure files.

## [0.4.0]

- #17 adds `mentat.clerk-utils.viewers/q`, similar to `quote` or `'`, for
  writing viewers with alias resolution and splicing. The `clj-kondo` config has
  an accompanying hook that handles linting of this form.

- #21:

  - Adds `bb test`, GitHub Action for tests and the `deps.edn` entry required to
    make it work.

  - Fixes a `j/` alias left over in `mentat.clerk-utils.show/loading-viewer`;
    these aliases aren't available in the latest Clerk SCI environment, and need
    to be fully expanded.

- #19:

  - Upgrades to Clerk version `fad499407d979916d21b33cc7e46e73f7a485e37`

  - Modifies the `clerk-utils/custom` template and project to use fully
    qualified vars inside of viewers.

  - Removes `mentat.clerk-utils.sci`, as `register-js!` was superceded by the
    new `:js-libs` option provided by SCI.

- #18 adds a `provided` dependency on `clerk` to the published jar, so that
  cljdoc's analysis will succeed.

- #16:

  - Fix broken deps reference in `clerk-utils/custom` template instructions
    (closes #15)

  - In `mentat.clerk-utils.build/serve!`, call `clerk/show!` before
    `clerk/serve!` to prevent flashing loading screen, if an index is supplied.

  - Fix incorrect docstrings in project template `bb.edn`

  - Add `config.edn` to `clerk-utils/custom` template, so that future
    `clj-kondo` commands (like `--copy-config` calls etc) work.

## [0.3.0]

- #13:

  - Adds a shutdown hook to `mentat.clerk-utils.build` that calls `halt!` when
    the JVM shuts down

  - Switches from npm http-server to babashka's http-server, slimming down the
    required entries in `package.json` just a touch more

  - adds the `clerk-utils/custom` `deps-new` template in the `resources`
    directory, along with an entry in the docs notebook on how to run it. The
    `resources/clerk_utils/custom` directory has a detailed README, as does the
    generated project.

- #11:

  - Adds `mentat.clerk-utils.sci/register-js!` for adding custom JS libraries to
    Clerk's SCI environment.

  - Adds documentation on how to generate custom ClojureScript builds for Clerk.

- #10:

  - Adds `mentat.clerk-utils.css` with `add-css!`, `set-css!` and `reset-css!`
    functions for customizing the CSS served in Clerk's header

  - Adds `mentat.clerk-utils.docs` with `git-sha` and `git-dependency` for
    injecting git dependency instructions into a notebook

  - Adds `mentat.clerk-utils.build` with `serve!`, `build!` and `halt!`
    variations that support custom CLJS compilation

## [0.2.0]

- #8:

  - fleshes out show.cljc notebook

  - adds a guide for how to use Clerk with custom ClojureScript.

- #6:

  - Moves the `cljs` macro to `mentat.clerk-utils.show/show-sci`, to make it
    clearer what it's doing.

  - `show-sci` now works in `cljc` files by emitting nothing on the
    Clojurescript side, and maintaining its old behavior on the clj side.

  - Adds a new `mentat.clerk-utils.show/show-cljs`, ported with permission from
    @mhuebert's
    [clerk-cljs](https://github.com/mhuebert/clerk-cljs/blob/main/src/mhuebert/clerk_cljs.cljc)
    library.

  - Adds a new documentation notebook at
    https://clerk-utils.mentat.org/dev/clerk_utils/show.html showing off how to
    use `show-cljs` to compile Clojurescript code and render it in Clerk in a
    single file.

  - To support the above change, https://clerk-utils.mentat.org now builds its
    own custom JS bundle.

  - Add missing clj-kondo config to the shipped jar.

## [0.1.0]

- #3:

  - Adds `->clerk-only` macro, and better documentation for the
    `mentat.clerk-utils` namespace.

  - Upgrades the documentation notebook and adds a solid README.

## [0.0.1]

First real release!

- Added `->clerk` and `cljs` macros.

- Published fleshed-out documentation notebook at https://clerk-utils.mentat.org
