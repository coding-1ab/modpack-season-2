import os
import subprocess

for project in os.listdir("mods"):
    ignore_upstream = False
    try:
        ignore_value = subprocess.check_output(["git", "config", "-f", ".gitmodules", f"submodule.mods/{project}.ignore-upstream"]).decode().strip().lower()
        if ignore_value == "true":
            ignore_upstream = True
    except subprocess.CalledProcessError:
        pass

    if ignore_upstream:
        continue

    workdir = f"mods/{project}"
    remotes = subprocess.check_output(["git", "remote"], cwd=workdir).decode().strip().split("\n")
    if "upstream" not in remotes:
        try:
            upstream_url = subprocess.check_output(["git", "config", "-f", ".gitmodules", f"submodule.mods/{project}.upstream"]).decode().strip()
        except subprocess.CalledProcessError:
            print(f"모드 {project}에 upstream URL이 지정되지 않았습니다!")
            continue

        print(f"+git remote add upstream {upstream_url} (in {workdir})")
        print(subprocess.check_output(["git", "remote", "add", "upstream", upstream_url], cwd=workdir).decode())

    print(f"+git fetch upstream (in {workdir})")
    print(subprocess.check_output(["git", "fetch", "upstream"], cwd=workdir).decode())
    local_branch = subprocess.check_output(["git", "config", "-f", ".gitmodules", f"submodule.mods/{project}.branch"]).decode().strip()
    upstream_branch = local_branch
    try:
        upstream_branch = subprocess.check_output(["git", "config", "-f", ".gitmodules", f"submodule.mods/{project}.upstream-branch"]).decode().strip()
    except subprocess.CalledProcessError:
        pass

    print(f"+git log --oneline origin/{local_branch}..upstream/{upstream_branch} (in {workdir})")
    new_changes = subprocess.check_output(["git", "log", "--oneline", f"origin/{local_branch}..upstream/{upstream_branch}"], cwd=workdir).decode().strip().split("\n")
    new_changes = [change for change in new_changes if change]
    if len(new_changes) > 0:
        print("업스트림에서 받아와야 하는 새로운 커밋들:")
        for new_change in new_changes:
            print(new_change)
