#!/bin/bash

set -ex

REGISTRY=""
REPO=""
VERSION="0.1.2-SNAPSHOT"

docker run --rm -t -p 8080:8080 "${REGISTRY}/${REPO}:${VERSION}"
