-include extra.mk

# ---- Scala ----
bloopInstall:
	sbt bloopInstall

install-native-image:
	gu install native-image

# ---- Python ----
PYTHON ?= python3
PYTEST ?= pytest

.PHONY: venv
venv:
	${PYTHON} -m venv .venv --upgrade-deps

exec-scala-test:
	sbt +test

exec-py-test:
	${PYTEST} src/test/python

test: exec-scala-test exec-py-test
