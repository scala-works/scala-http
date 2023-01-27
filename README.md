# scala-http

A Scala 3 driven micro framework around the tapir library

## Status

This project is very early proof of concept.

This demo is currently only `Future` based, but can be adapted for `Cats-Effect`
and `ZIO`.

## What this framework does

It handles the aggregating of `ServerEndpont`s, and loads + configures the
server for you. This means you only need to focus on writing your Controllers,
and injecting an instance in the entry point.

It does this using Scala 3's awesome metaprogramming abilities. We are able to
take those Controller instances, collect only the `val`s that are of type
`ServerEndpont[_,_]`, and do the bundling.

## Running

The following should bring up the app on `http://localhost:8080`

> sbt example/run

There should be the following endpoints:

- /health
- /greet
- /docs
