# Watcher

Receive event signals and broadcast to connected downstream by dame simple
HTTP2/3 server stream or websocket protocol.

Warning: 🚧 WIP & Prototyping

## Dev Guide

```sh
sbt compile
sbt test
sbt run
```

### Using `sbt` as Metals build server

While Metals uses `sbt` as the build server, we can also log into the same sbt
session using a thin client. From Terminal section, type in

```sh
sbt --client
```

This lets you log into the sbt session Metals has started.

### Build

By default, all files found in the `src/universal` directory are included in the
distribution.

Build by

```sh
sbt clean
sbt stage
```

### Build to universal package

Create the plain layout app or zipped `tgz`/`tgz` package for the project.

```sh
# create plain layout application directory
sbt packageBin
# create `tgz` (tar.gz) package
sbt packageZipTarball
# create `txz` (tar.xz) package
sbt packageXzTarball
```

### Build to native-image

Before building, you should install prerequisites binary `native-image` from
Graal.

```sh
gu install native-image
```

Then, build to native-image

```sh
sbt GraalVMNativeImage/packageBin
```

### Build to docker

```sh
sbt Docker/stage
sbt Docker/publishLocal
sbt Docker/publish
```

**Warning**: Before executing `Docker/publish`, you need to login to container
registry outside `sbt`.
