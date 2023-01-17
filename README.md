# Watcher

Receive event signals and broadcast to connected downstream.

## Dev

```shell
sbt compile # build the project
sbt test # run the tests
sbt run # run the application (Main)
```


## Build

### Prerequisites

```
gu install native-image
```

### Build to native-image

### Build to docker

```
sbt Docker/stage
sbt Docker/publishLocal
```
