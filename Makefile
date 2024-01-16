
# ---- Scala ----

bloopInstall:
	sbt bloopInstall

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
