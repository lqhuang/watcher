#!/bin/bash

set -ex

REGISTRY="ghcr.io"
REPO="lqhuang"
PROJECT="watcher"
VERSION="0.1.5-SNAPSHOT"

sbt Docker/publishLocal

docker run --rm -t -p 8080:8080 "${REGISTRY}/${REPO}/${PROJECT}:${VERSION}"
