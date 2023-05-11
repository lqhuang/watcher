# Watcher

Receive event signals and broadcast to connected downstream.

## Dev

```shell
sbt compile # build the project
sbt test # run the tests
sbt run # run the application (Main)
```

### Using `sbt` as Metals build server

While Metals uses `sbt` as the build server, we can also log into the same sbt
session using a thin client. From Terminal section, type in

```sh
sbt --client
```

This lets you log into the sbt session Metals has started.

## Build

### Build to native-image

Before building, you should install prerequisites binary `native-image` from Graal.

```
gu install native-image
```

### Build to docker

```
sbt Docker/stage
sbt Docker/publishLocal
sbt Docker/publish
```

**Warning**: Before executing `Docker/publish`, you need to login to container
registry outside `sbt`.
