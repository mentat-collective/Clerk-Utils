# Changelog

## [unreleased]

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
