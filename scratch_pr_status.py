import subprocess
import urllib.request
import json
import time

def get_token():
    p = subprocess.Popen(['C:\\Program Files\\Git\\mingw64\\bin\\git-credential-manager.exe', 'get'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    out, _ = p.communicate('protocol=https\nhost=github.com\n')
    for line in out.splitlines():
        if line.startswith('password='):
            return line.split('=', 1)[1].strip()
    return None

def check_pr_and_runs():
    token = get_token()
    headers = {'Authorization': f'Bearer {token}', 'User-Agent': 'AntigravityDevOps'}
    
    # Check PR 1
    req = urllib.request.Request('https://api.github.com/repos/cardenaswalker2/NovaDrogueria/pulls/1', headers=headers)
    with urllib.request.urlopen(req) as resp:
        pr = json.loads(resp.read().decode())
        print("PR #1 Title:", pr["title"])
        print("PR #1 State:", pr["state"])
        print("PR #1 Merged:", pr["merged"])
        print("PR #1 URL:", pr["html_url"])
        print("PR #1 Mergeable:", pr.get("mergeable"))
        print("PR #1 Head:", pr["head"]["ref"], "-> Base:", pr["base"]["ref"])
        
    # Check latest runs
    req = urllib.request.Request('https://api.github.com/repos/cardenaswalker2/NovaDrogueria/actions/runs', headers=headers)
    with urllib.request.urlopen(req) as resp:
        runs = json.loads(resp.read().decode())
        print("\nLatest Workflow Runs:")
        for r in runs.get("workflow_runs", [])[:4]:
            print("  Run ID:", r["id"], "| Event:", r["event"], "| Branch:", r["head_branch"], "| Status:", r["status"], "| Conclusion:", r["conclusion"], "| URL:", r["html_url"])

if __name__ == "__main__":
    check_pr_and_runs()
