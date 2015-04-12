KT - A Groovy Kafka Tail
------------------------

[![Circle CI](https://circleci.com/gh/charliek/kt/tree/master.svg?style=svg)](https://circleci.com/gh/charliek/kt/tree/master)

This is a simple program that allows you to tail a kafka topic and run all
logic through one or more groovy scripts. These scripts will be external to
the jar and changes to them will be watched and compiled on the fly without
disconnecting from kafka. The program is meant to be executed from the
commandline and has the following options:

```
Usage: <main class> [options]
  Options:
    -gid
       Specify a non-random group id if you need to pick up where you left off.
    -loc
       Start from head or tail of topic (use 'head' to override).
       Default: tail
  * -s, -script
       The scripts you would like to load. Can be specified multiple times.
       Default: []
    -threads
       The number of kafka worker threads.
       Default: 1
  * -topic
       The kafka topic name to listen to.
  * -zk
       The zookeeper host(s) hostname1:port1/chroot/path.
```

So for example the program could be started with the command:

```
java -jar build/libs/kt-0.0.1-SNAPSHOT.jar -gid kt-test -loc head -s src/examples/groovy/example.groovy -topic logs -zk localhost:2181
```

The scripts that are specified should contain a method with a signature of
`void onMessage(byte[] bytes)` which all messages will be routed to. See the
`com.charlieknudsen.kt.BaseListener` class for other methods that can be used
as this is the class that you script will be mixed into.
