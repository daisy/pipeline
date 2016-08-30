# pipeline

## Code

This is a convenience "super" Pipeline project that aggregates all sub-projects and 3rd-party libraries.

This makes branching, building, and releasing of several sub-projects at once easier.

The aggregating and the backporting of changes to the individual projects is done using a tool called [git-subrepo][]. The idea is that all the git magic will be done by the owners, and that committers can just treat this repository as a regular one. There is a rule though that committers need to follow because of some limitations of git-subrepo:

- Pull requests may not contain merge commits. In order to keep things structured, make one pull request per distinct feature.

Committers are of course also free to make pull requests to the individual repositories, or do other advanced things such as switching a certain sub-repository to an existing branch. Advanced git knowledge is required in these cases. Ask for help if needed.

The tree below shows the structure of the project:

- [assembly](https://github.com/daisy/pipeline-assembly)
- [framework](https://github.com/daisy/pipeline-framework)
- libs
  - [braille-css](https://github.com/snaekobbi/braille-css)
  - dotify
    - [dotify.api](https://github.com/brailleapps/dotify.api)
    - [dotify.formatter.impl](https://github.com/brailleapps/dotify.formatter.impl)
  - [jstyleparser](https://github.com/snaekobbi/jStyleParser)
- modules
  - [scripts](https://github.com/daisy/pipeline-scripts)
  - [braille](https://github.com/daisy/pipeline-mod-braille)
- utils
  - [build-utils](https://github.com/daisy/pipeline-build-utils)
- [webui](https://github.com/daisy/pipeline-webui)


## Issues

This is also a common issue tracker for the Pipeline. Use the individual issue trackers of the sub-projects listed above for issues that clearly belong to a specific sub-project. Before creating a new issue, please first check the [existing issues](https://github.com/search?utf8=%E2%9C%93&q=repo%3Adaisy%2Fpipeline-assembly+repo%3Adaisy%2Fpipeline-framework+repo%3Adaisy%2Fpipeline-scripts+repo%3Adaisy%2Fpipeline-build-utils+repo%3Adaisy%2Fpipeline-webui+repo%3Asnaekobbi%2Fbraille-css+repo%3Asnaekobbi%2FjStyleParser+repo%3Abrailleapps%2Fdotify.api+repo%3Abrailleapps%2Fdotify.formatter.impl&type=Issues&ref=searchresults).

## Website

The `gh-pages` branch contains the source of the [Pipeline website](http://daisy.github.io/pipeline).

[git-subrepo]: https://github.com/ingydotnet/git-subrepo
