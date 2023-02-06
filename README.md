# Clerk-Utils

A collection of functions, macros, guides and templates that have come in handy
while documenting libraries with Nextjournal's [Clerk][clerk-url].

[![Build Status][build-status]][build-status-url]
[![License][license]][license-url]
[![cljdoc badge][cljdoc]][cljdoc-url]
[![Clojars Project][clojars]][clojars-url]
[![Discord Shield][discord]][discord-url]

`clerk-utils` also contains:

- Code and guides for generating a [custom ClojureScript build for your Clerk
  projects][custom-cljs-url],
  allowing you to use Clerk Plugin projects like
  [Mafs.cljs](https://mafs.mentat.org), [Leva.cljs](https://leva.mentat.org) and
  more in your Clerk notebooks.
- The [`clerk-utils/custom` template][clerk-utils-custom-url] for generating a
  Clerk project configured for [custom ClojureScript][custom-cljs-url].

## Quickstart

Install `clerk-utils` into your Clojure project using the instructions at
its Clojars page:

[![Clojars Project][clojars]][clojars-url]

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

Another example: `us/show-sci` allows you to push [Reagent][reagent-url] forms
directly into the client's [SCI environment][sci-url] without building out a
Clerk viewer:

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

The project's [interactive documentation](https://clerk-utils.mentat.org) was
generated using Nextjournal's [Clerk](https://github.com/nextjournal/clerk). If
you'd like to edit or play with the documentation, you'll need to install

- [node.js](https://nodejs.org/en/)
- The [clojure command line tool](https://clojure.org/guides/install_clojure)
- [Babashka](https://github.com/babashka/babashka#installation)

Once this is done, run this command:

```
bb clerk-watch
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

[clerk-url]: https://clerk.vision
[build-status-url]: https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml
[build-status]: https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml/badge.svg?branch=main
[cljdoc-url]: https://cljdoc.org/d/org.mentat/clerk-utils/CURRENT
[cljdoc]: https://cljdoc.org/badge/org.mentat/clerk-utils
[clojars-url]: https://clojars.org/org.mentat/clerk-utils
[clojars]: https://img.shields.io/clojars/v/org.mentat/clerk-utils.svg
[discord-url]: https://discord.gg/hsRBqGEeQ4
[discord]: https://img.shields.io/discord/731131562002743336?style=flat&colorA=000000&colorB=000000&label=&logo=discord
[license-url]: LICENSE
[license]: https://img.shields.io/badge/license-MIT-brightgreen.svg
[github-url]: https://github.com/mentat-collective/clerk-utils
[reagent-url]: https://reagent-project.github.io
[sci-url]: https://github.com/babashka/sci
[custom-cljs-url]: https://clerk-utils.mentat.org#custom-clojurescript-builds
[clerk-utils-custom-url]: https://github.com/mentat-collective/clerk-utils/blob/main/resources/clerk_utils/custom
