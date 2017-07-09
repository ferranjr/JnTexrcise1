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

I went for the most basic implementation algorithm for cliques. Left for the end, but it took me longer than expected
to deal with the Twitter Ratelimit limitations, which I didn't considered from the very beginning. 

Todo:
[ ] Check, if possible, https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm as more performand solution.
[ ] Improve error handling with 3rd party services... also, not very happy with any of the two clients used. 


## Requirements and Configuration

Configuration it's only require to run the application, which is basically set up the following environment or
go and modify the application.conf with your values.

```bash
export TWITTER_CONSUMER_KEY='yourvaluehere'
export TWITTER_CONSUMER_SECRET='yourvaluehere'
export TWITTER_ACCESS_TOKEN_KEY='yourvaluehere'
export TWITTER_ACCESS_TOKEN_SECRET='yourvaluehere'
export GITHUB_ACCESS_TOKEN='yourvaluehere'
```

## How to Run the tests

```bash
sbt test
```

## How to Build and Run application

The only required parameter is the path to the source file where users are defined, but a results path can be
specified.

```bash
sbt "run path/to/sourcefile.txt path/to/resultPath.txt
```
