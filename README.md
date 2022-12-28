# clerk-utils

[![Build Status](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml/badge.svg?branch=main)](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml)
[![License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE)
[![cljdoc badge](https://cljdoc.org/badge/org.mentat/clerk-utils)](https://cljdoc.org/d/org.mentat/clerk-utils/CURRENT)
[![Clojars Project](https://img.shields.io/clojars/v/org.mentat/clerk-utils.svg)](https://clojars.org/org.mentat/clerk-utils)

A small collection of functions and macros that have come in handy while
documenting libraries with Nextjournal's [Clerk][CLERK].

## Quickstart

Install `clerk-utils` into your Clojure project using the instructions at
its Clojars page:

[![Clojars Project](https://img.shields.io/clojars/v/org.mentat/clerk-utils.svg)](https://clojars.org/org.mentat/clerk-utils)

Require `mentat.clerk-utils` and `mentat.clerk-utils.show` in your namespace:

```clj
(ns my-app
   (:require [mentat.clerk-utils :as u]
             [mentat.clerk-utils.show :as us]
             [nextjournal.clerk :as-alias clerk]))
```

Use `->clerk` and `->clerk-only` to include forms that will only evaluate if
Clerk is available, but will act as `comment` otherwise:

```clj
(u/->clerk
  (defn exclaim [s] (str s "!"))

  [:pre (exclaim "I won't run if Clerk is missing")])
```

Another example: `us/show-sci` allows you to push Reagent forms directly into
the client's [SCI environment](https://github.com/babashka/sci) without building
out a Clerk viewer:

```
(us/show-sci
 (let [text "Include any Reagent vector!"]
   [:pre text]))
```

<img width="805" alt="image" src="https://user-images.githubusercontent.com/69635/207616925-c4f22afd-2579-4c3f-8fef-856323510849.png">

See the project's [interactive documentation
notebook](https://clerk-utils.mentat.org) for more guides and examples.

## Who is using clerk-utils?

The following documentation notebooks include examples of the `show-sci` macro:

- [JSXGraph.cljs](https://jsxgraph.mentat.org)
- [MathLive.cljs](https://mathlive.mentat.org)
- [MathBox.cljs](https://mathbox.mentat.org)
- [emmy-viewers](https://emmy-viewers.mentat.org)
- [clerk-utils](https://clerk-utils.mentat.org)

## Interactive Documentation via Clerk

The project's [interactive documentation](https://jsxgraph.mentat.org) was
generated using Nextjournal's [Clerk](https://github.com/nextjournal/clerk). If
you'd like to edit or play with the documentation, you'll need to install

- [node.js](https://nodejs.org/en/)
- The [clojure command line tool](https://clojure.org/guides/install_clojure)
- [Babashka](https://github.com/babashka/babashka#installation)

Once this is done, run this command in one terminal window to build and serve the custom JS required by the notebook:

```
bb dev-notebook
```

In another terminal window, run

```
bb start-clerk
```

This should open a browser window to `http://localhost:7777` with the contents
of the documentation notebook. Any edits you make to `dev/jsxgraph/notebook.clj`
will be picked up and displayed in the browser on save.

## Thanks and Support

To support this work and my other open source projects, consider sponsoring me
via my [GitHub Sponsors page](https://github.com/sponsors/sritchie). Thank you
to my current sponsors!

I'm grateful to [Clojurists Together](https://www.clojuriststogether.org/) for
financial support during this library's creation. Please consider [becoming a
member](https://www.clojuriststogether.org/developers/) to support this work and
projects like it.

## License

Copyright © 2022 Sam Ritchie.

Distributed under the [MIT License](LICENSE). See [LICENSE](LICENSE).

[CLERK]: https://clerk.vision
