#!/usr/bin/env python3
"""Require the latest Linux main-push preflight for an exact commit; never trust a tag/PR run."""
import argparse
import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

REPOSITORY = "mosqueteroweb/gasolineras"
WORKFLOW = "release.yml"


class PreflightError(RuntimeError):
    pass


def github_get(path, token=None):
    headers = {"Accept": "application/vnd.github+json", "User-Agent": "Gass-release-preflight",
               "X-GitHub-Api-Version": "2026-03-10"}
    if token:
        headers["Authorization"] = "Bearer " + token
    request = urllib.request.Request("https://api.github.com" + path, headers=headers)
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            return json.load(response)
    except urllib.error.HTTPError as error:
        raise PreflightError("GitHub no permite comprobar la validación (HTTP %s). No se publica." % error.code) from None
    except (OSError, ValueError) as error:
        raise PreflightError("No se pudo comprobar la validación de GitHub; no se publica.") from error


def preflight_status(sha, get):
    if not re.fullmatch(r"[0-9a-f]{40}", sha):
        raise PreflightError("Se requiere el SHA completo del commit.")
    query = urllib.parse.urlencode({"event": "push", "branch": "main", "head_sha": sha, "per_page": 100})
    data = get("/repos/%s/actions/workflows/%s/runs?%s" % (REPOSITORY, WORKFLOW, query))
    runs = [r for r in data.get("workflow_runs", []) if r.get("head_sha") == sha
            and r.get("head_branch") == "main" and r.get("event") == "push"
            and r.get("repository", {}).get("full_name", "").lower() == REPOSITORY.lower()]
    if not runs:
        return "pending", "Todavía no hay una validación de main para " + sha
    latest = max(runs, key=lambda r: int(r["id"]))
    url = "https://github.com/%s/actions/runs/%s" % (REPOSITORY, latest["id"])
    if latest.get("status") != "completed":
        return "pending", url
    if latest.get("conclusion") != "success":
        return "failure", url
    jobs = get("/repos/%s/actions/runs/%s/jobs?filter=latest&per_page=100" % (REPOSITORY, latest["id"]))
    validation = [j for j in jobs.get("jobs", []) if j.get("name") == "validate"]
    if len(validation) != 1 or validation[0].get("conclusion") != "success":
        return "failure", url
    # Do not accept old successful runs from before the cold-preflight policy existed.
    required = {"Test release guards", "Clean Linux dependency verification and build"}
    steps = {step.get("name") for step in validation[0].get("steps", []) if step.get("conclusion") == "success"}
    if not required.issubset(steps):
        return "failure", "La ejecución no contiene la comprobación limpia requerida: " + url
    return "success", url


def wait_for_preflight(sha, timeout=900, interval=15, get=None):
    get = get or github_get
    deadline = time.monotonic() + timeout
    while True:
        status, detail = preflight_status(sha, get)
        if status == "success":
            return detail
        if status == "failure":
            raise PreflightError("La validación previa ha fallado. No se crea la etiqueta: " + detail)
        if time.monotonic() >= deadline:
            raise PreflightError("La validación previa no está terminada. No se crea la etiqueta: " + detail)
        print("Esperando validación Linux del commit: " + detail, flush=True)
        time.sleep(min(interval, max(0, deadline - time.monotonic())))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--sha", required=True)
    parser.add_argument("--timeout", type=int, default=0)
    args = parser.parse_args()
    # Only CI uses its explicitly supplied, read-only Actions token. Local publication uses public GETs.
    token = os.environ.get("GITHUB_TOKEN")
    try:
        print(wait_for_preflight(args.sha, args.timeout, get=lambda p: github_get(p, token)))
    except PreflightError as error:
        print(str(error), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
