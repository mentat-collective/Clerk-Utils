# clerk-utils/custom template

This directory contains a [`deps-new`][deps-new-url] template that creates a new
[Clerk][clerk-url] project with everything described in the ["Custom
ClojureScript
Builds"](https://clerk-utils.mentat.org/#custom-clojurescript-builds) section of
the [`clerk-utils` documentation notebook][clerk-utils-url] already configured.

See create a project with

To use the template, install the [`deps-new`][deps-new-url] tool:

```sh
clojure -Ttools install io.github.seancorfield/deps-new '{:git/tag "v0.4.13"}' :as new
```

Then create a project using the `clerk-utils/custom` template:

```
clojure -Sdeps '{:deps {io.github.nextjournal/clerk-utils {:git/tag "v0.3.0"}}}' \
-Tnew create \
:template clerk-utils/custom \
:name myusername/my-notebook-project
```

See https://github.com/mentat-collective/clerk-utils-custom-template for an
example of a fresh project generated from this template, with `:name
mentat-collective/clerk-utils-custom-template`.

> **Note**
> The `:name` argument should match the GitHub slug (ie,
> `org_name/project_name`) where you expect to host the project. The above
> command will create a new project in the folder `my-notebook-project` in the
> directory where you run the command.

The generated project will contains more guides and information in its
`README.md` and in the generated Clerk notebook.

See the [Project Template documentation
section](https://clerk-utils.mentat.org/#project-template) for more details
about this template.

## Template Keyword Options

You can customize the `clerk-utils/custom` template by supplying any of the
following key-value pairs to the above command (default values in parentheses):

- `:description`: This string is inserted at the top of your generated project's
  README.md.
- `:clerk-port`: (`7777`) the port used by `clerk/serve!` during interactive
  development.
- `:clerk-sha`: (`"4180ed31c2864687a770f6d4f625303bd8e75437"`) the hash of the
  Clerk version you'd like to use in the template. (`clerk-utils/custom` uses a
  [git dependency](https://clojure.org/news/2018/01/05/git-deps) for Clerk.)
- `:shadow-port`: (`8765`) the port that [`shadow-cljs`][shadow-url] uses to
  serve compiled JavaScript during interactive development.
- `:shadow-version`: (`"2.20.14"`) the version of [`shadow-cljs`][shadow-url]
  required by the generated project.
- `:clj-version`: (`"1.11.1"`) the version of Clojure required by the generated
  project.
- `:cljs-version`: (`"1.11.60"`) the version of ClojureScript required by the
  generated project. (_note that this needs to meet or exceed the version
  declared in the [`shadow-cljs` `deps.edn`
  file](https://github.com/thheller/shadow-cljs/blob/master/deps.edn) for the
  `shadow-cljs` version you've chosen._)
- `:http-server-port`: (`8080`) The port used by `bb serve` and `bb
  publish-local` to serve the local statically built site.
- `:cname`: (`""`) If you're serving your GitHub Pages build from a custom URL,
  pass the value (like `"clerk-utils.mentat.org"`) of the custom site via this
  argument.

## Thanks and Support

To support this work and my other open source projects, consider sponsoring me
via my [GitHub Sponsors page](https://github.com/sponsors/sritchie). Thank you
to my current sponsors!

## License

Copyright © 2023 Sam Ritchie.

Distributed under the [MIT License](LICENSE). See [LICENSE](LICENSE).

[clerk-url]: https://clerk.vision
[clerk-utils-url]: https://clerk-utils.mentat.org
[deps-new-url]: https://github.com/seancorfield/deps-new
[shadow-url]: https://shadow-cljs.github.io/docs/UsersGuide.html
