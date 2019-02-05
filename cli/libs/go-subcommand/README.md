go-subcommand
=============

[![Build Status](https://travis-ci.org/capitancambio/go-subcommand.png?branch=master)](https://travis-ci.org/capitancambio/go-subcommand)

Option parser for go programs a la git/mercurial/go loosely inspired by Ruby's OptionParser.


It allows to build CLIs easily supporting syntax similar to:

```
program --prog_option1 opt1 --prog_switch1 subcommand --subcommand_opt1 opt1 --subcommand_switch more_arguments
```

