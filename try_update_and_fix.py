import os
import subprocess
from pathlib import Path
import requests
import shutil

def is_valid_repo(url):
    response = requests.get(url)
    return response.status_code != 404

def run_command(workdir, command_array):
    command_string = " ".join(command_array)
    print(f"+{command_string} (in {workdir})")
    output = subprocess.check_output(command_array, cwd=workdir).decode().strip()
    return output

for project in os.listdir("mods"):
    workdir = f"mods/{project}"
    if not Path(workdir + "/.git").exists():
        print("\033[31m-------------------------\033[0m")
        print(f"\033[31mCorrupted mod clone detected at {workdir}\033[0m")
        print("\033[31m-------------------------\033[0m")
        answer = input("Delete? [y/n]\n").lower()
        if answer == "y":
            shutil.rmtree(workdir)
            continue

    remotes = run_command(workdir, ["git", "remote", "-v"]).split("\n")
    origin = next((line for line in remotes if line.startswith("origin")), [""]).replace("origin", "").replace("(fetch)", "").replace("(push)", "").strip()
    print("detected origin: " + origin)
    if "coding-1ab" not in origin:
        username = origin.replace("https://github.com/", "")
        first_slash = username.index("/")
        username = username[:first_slash]
        test_url = origin.replace(username, "coding-1ab")
        if is_valid_repo(test_url):
            run_command("./", ["git", "submodule", "set-url", workdir, test_url])
            run_command(workdir, ["git", "remote", "set-url", "origin", test_url])
            run_command(workdir, ["git", "fetch", "origin"])

print("Subprojects are now probably corrected (hopefully)")

run_command("./", ["git", "pull"])
run_command("./", ["git", "submodule", "update", "--recursive", "--init" "--remote"])
