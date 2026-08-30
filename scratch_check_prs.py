import urllib.request
import json

def check_prs():
    url = "https://api.github.com/repos/cardenaswalker2/NovaDrogueria/pulls?state=all"
    req = urllib.request.Request(url, headers={"User-Agent": "AntigravityDevOps"})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        print("Total PRs:", len(data))
        for p in data:
            print("PR #", p["number"], ":", p["title"], "| State:", p["state"], "| Merged At:", p.get("merged_at"), "| HTML:", p["html_url"])

if __name__ == "__main__":
    check_prs()
