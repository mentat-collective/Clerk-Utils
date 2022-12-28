# Changelog

## [unreleased]

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

## 0.1.0

- #3:

  - Adds `->clerk-only` macro, and better documentation for the
    `mentat.clerk-utils` namespace.

  - Upgrades the documentation notebook and adds a solid README.

## 0.0.1

First real release!

- Added `->clerk` and `cljs` macros.

- Published fleshed-out documentation notebook at https://clerk-utils.mentat.org
