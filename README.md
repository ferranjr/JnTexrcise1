# Exercise 1

Given a list of developer handles, find all the groups of “fully connected” developers.
Two given developers are considered connected if:

- They follow each other in twitter
- They have a Github organization in common

Assume that people having the same handle both in Twitter and Github are actually the same person.

By “fully connected” we mean that they form a  clique , or in other words, all the developers in a clique
are connected to each other. All subgraphs of a clique are also cliques but we are interested just in the 
maximal  ones (the ones that are not a subset of other clique) that have  at least two developers.

Both input and output will be textual. The input will be plain text file with user handles, e.g.:

```
user1
user2
...
usern
```

The output will have one clique per line with whitespace separated handles in alphabetical order. For example:

```
user1 user3
user2 user4 user5
```

We expect the implementation to query the official Twitter and Github APIs and it is OK to use the existing client
libraries for your target programming language.


# Implementation


## Trade-Offs and decisions made


## Requirements and Configuration


## How to Run the tests

```bash
sbt test
```

## How to Build and Run application
