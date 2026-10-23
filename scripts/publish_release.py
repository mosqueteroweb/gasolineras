#!/usr/bin/env python3
"""Push main, wait for a cold Linux preflight, then create and push a NEW release tag."""
import argparse
from pathlib import Path
import re
import subprocess
import sys
from release_preflight import PreflightError, wait_for_preflight

ROOT = Path(__file__).resolve().parents[1]


def redact(text):
    text = re.sub(r"https?://[^\s/@]+@github\.com", "https://github.com", text)
    return re.sub(r"(?:github_pat_|gh[pousr]_)[A-Za-z0-9_]+", "[REDACTADO]", text)


def git(*args, allowed=(0,)):
    result = subprocess.run(["git", *args], cwd=ROOT, text=True, capture_output=True)
    if result.returncode not in allowed:
        raise PreflightError("Git no completó la operación: " + redact(result.stderr))
    return result.returncode, result.stdout.strip()


def require_checkout(sha):
    if git("status", "--porcelain")[1]:
        raise PreflightError("Hay cambios sin confirmar; no se crea la etiqueta.")
    if git("branch", "--show-current")[1] != "main" or git("rev-parse", "HEAD")[1] != sha:
        raise PreflightError("La rama o el commit han cambiado; no se crea la etiqueta.")


def require_remote_commit(sha):
    remote = git("ls-remote", "--exit-code", "origin", "refs/heads/main")[1].split()
    if not remote or remote[0] != sha:
        raise PreflightError("main de GitHub no coincide con el commit validado; no se crea la etiqueta.")


def require_new_tag(tag):
    if git("show-ref", "--verify", "--quiet", "refs/tags/" + tag, allowed=(0, 1))[0] == 0:
        raise PreflightError("La etiqueta local ya existe; no se sobrescribe: " + tag)
    if git("ls-remote", "--exit-code", "--tags", "origin", "refs/tags/" + tag, allowed=(0, 2))[0] == 0:
        raise PreflightError("La etiqueta de GitHub ya existe; no se sobrescribe: " + tag)


def publish(check_only=False, timeout=900):
    sha = git("rev-parse", "HEAD")[1]
    require_checkout(sha)
    text = (ROOT / "app/build.gradle.kts").read_text()
    version = re.search(r'versionName = "([^"]+)"', text).group(1)
    if not re.fullmatch(r"\d+\.\d+\.\d+", version):
        raise PreflightError("La versión debe tener formato X.Y.Z.")
    tag = "v" + version
    if not check_only:
        require_new_tag(tag)
        # Fast-forward push only; never force main or rewrite published tags.
        git("push", "origin", "HEAD:refs/heads/main")
    require_remote_commit(sha)
    # Local public API reads do not extract or reuse credentials from the Git remote.
    url = wait_for_preflight(sha, timeout=timeout)
    require_checkout(sha)
    require_remote_commit(sha)
    if check_only:
        print("Validación previa correcta, sin crear ni subir etiquetas: " + url)
        return
    require_new_tag(tag)
    git("-c", "user.name=Mosqueteroweb", "-c", "user.email=mosqueteroweb@gmail.com",
        "tag", "-a", tag, "-m", "Gass " + version, sha)
    git("push", "origin", "refs/tags/" + tag)
    print("Etiqueta nueva enviada después de validar el commit: " + url)
    print("Consulta el resultado de firma y publicación en GitHub Actions.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check-only", action="store_true", help="Solo comprobar; no subir código ni crear etiquetas")
    parser.add_argument("--timeout", type=int, default=900)
    args = parser.parse_args()
    try:
        publish(args.check_only, args.timeout)
    except (PreflightError, OSError) as error:
        print(redact(str(error)), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
