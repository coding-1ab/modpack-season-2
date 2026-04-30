import os
import subprocess

for project in os.listdir("mods"):
    print(f"+git config -f .gitmodules submodule.mods/{project}.branch")
    branch = subprocess.check_output(["git", "config", "-f", ".gitmodules", f"submodule.mods/{project}.branch"])
    branch = branch.decode("ascii").strip()
    print(project, branch)

    result = subprocess.run(["git", "switch", branch], cwd = f"./mods/{project}", capture_output=True, text=True)
    print(result.stdout)
    print(result.stderr)

