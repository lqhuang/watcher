#!/bin/bash

set -ex

sbt_stainless_url="https://github.com/epfl-lara/stainless/releases/download/v0.9.7/sbt-stainless.zip"
fname="sbt-stainless.zip"

curl -fL -o "${fname}" "${sbt_stainless_url}"
unzip -q "${fname}"
rm "${fname}"
