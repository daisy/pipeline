# Ace validation module for ePubs

This module tries to check the compliance to the [EPUB Accessibility Specification](http://www.idpf.org/epub/a11y/) for an ePub, using the [DAISY Ace App](https://daisy.github.io/ace/).

This module requires the Ace App to be installed on your system to work (ace must be in your PATH enviroment variable).

If not, a warning message will be raised instead of the html and json reports.

## Installing Ace

Instruction to install Ace on your system are available on [Ace website](https://daisy.github.io/ace/getting-started/installation/).

The easiest way is to download and install the [latest LTS version of NodeJS](https://nodejs.org/en/download/) for your system, and then to launch this command in a terminal (windows or unix) : 
```bash
npm install @daisy/ace -g
```
