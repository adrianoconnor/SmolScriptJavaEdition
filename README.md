> WARNING: This is a learning project written by someone who is not an expert in this kind of software. It is not suitable production use as-is. Use at your own risk.

> Note: Everything I know about building interpreters and compilers I learned from the book 'Crafting Interpreters' (You can read it online for free at https://craftinginterpreters.com). It is an incredible resource, and I cannot recommend it highly enough. If you want to learn about how programming languages are created, that is where you should go.

# SmolScript

SmolScript is a Javascript-like language that runs as a tiny stack based VM and is implemented fully within a host language and its runtime, making it effectively a fully sandboxed statemachine. As well as this Java version, there are versions implemented in .net, TypeScript and Python. Each version is going to be different because it relies heavily on the host language's features (e.g., the SmolScript types are fairly simple wrappers around the host language's implementation of the same types, plus we lean on the language-native classes for regular expressions, serialization, JSON etc to enable those features in SmolScript). This choice allows SmolScript itself be *very* smol.

You can see a simple demo of SmolScript in action at https://smolscript.org/interactive-demo. The demo is the TypeScript version and runs entirely in the browser as a JavaScript module. It also lets you visualise the compiled program/instructions and the VM internal state.

The primary goals for this project are:

1. For me (Adrian O'Connor) to learn how programming languages are implemented, and get a deeper understanding of how compilers and virtual machines work. It's just an itch I've been wanting to scratch since I started programming many years ago.
2. Build a potentially useful Javascript-like runtime that could be used as embedded scripting language inside other applications. For example, it could be used for adding user scripts safely to an existing application, or running code in a zero trust environment, or building an educational IDE for teaching programming concepts in an interactive way.
3. It is designed to be secure (but there are absolutely no guarantees in this version). The VM is intended to be completely sandboxed from the host application's point of view, making it somewhat suitable for running untrusted code. Smol scripts can't access any of the host language's features or resources, but you can create custom bridges using functions and delegates, so you can expose only the features you need.

One goal we do not have is performance. This is probably the slowest Javascript-like engine you will ever use. We have absolutely prioritised simplicity and clarity of code over high performance. [note: if you run the test suite in SmolScript.net and compare it against nodejs, we're about 3 times slower, but it's not really a true comparison]. We do have some ideas about a few suitably simple optimisations that we might explore one day.

Even though we've based SmolScript on Javascript, it is not anywhere near feature parity with Javascript and never will be. This really is just a little side project.

What is built so far:

* A working 'byte-code' compiler (including hand-rolled scanner and parser) that supports most of the basic language features you'd expect in a tiny Javascript-like language
* A stack-based Virtual Machine that supports break points and step through and a small degree of observability (with more to come)
* An easy way to expose custom functions from js using simple closures to wrap the native code (no need for adding new types/interfaces) that automatically deal with type coercion
* A test suite that I think almost covers the entire language -- we've built this in a way that the test files can be shared across all smol implementations (but I'm still moving stuff from the early .net development unit tests to the shared suite so that's not complete)

The byte-code compiler does not produce byte-code at all, all of the instructions are actually regular objects in the host language that happen to be processed one by one in the VM. This is a potential area for future optimisation.

In terms of language features, we currently have:

* Var for variables (note that var works like let)
* Flow control
* Basic Arrays
* Basic Dictionarys (plain Objects, no inheritence)
* Classes (no inheritence)
* First class and anonymous functions
* => syntax
* Try/Catch
* Regex
* Support for JS optional semi-colon logic (based on line-breaks)
* Pretty good support for registering callbacks and functions and passing arguments (this is by far the best in the .net version because I know my way around .net refection)
* Semi-decent compiler errors

On our list of things that we think we want to add soon-ish:

* for ... in
* Rich and well-thought out JSON support (because JSON processing would be a good use-case for this language)
* More of the Javascript 'standard library'
* Better compiler errors

What is not on our roadmap to support:

* Modules (but I think I might explore this soon)
* Async/await (I might add this to the .net and TS versions)
* file, network, database etc -- for that we expect you to use native custom functions exposed to your VM (this is how we make it secure!)

## I want to use it in my project!

I do not recommend it. This is not a commercially supported project, and I don't have time to support it.

My hope is that somebody will find this interesting at least. I've certainly enjoyed creating it, and I've learned so much.

The source code for this version is MIT licensed.

The name SmolScript is copyright 2023 Arctus Limited.