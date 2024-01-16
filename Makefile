
# ---- Scala ----

bloopInstall:
	sbt bloopInstall

# ---- Python ----
PYTHON ?= python3
PYTEST ?= pytest

.PHONY: venv
venv:
	${PYTHON} -m venv .venv --upgrade-deps

pytest:
	${PYTEST} src/test/python
