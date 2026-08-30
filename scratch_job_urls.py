import urllib.request
import json

def get_job_urls():
    url = "https://api.github.com/repos/cardenaswalker2/NovaDrogueria/actions/runs"
    req = urllib.request.Request(url, headers={"User-Agent": "AntigravityDevOps"})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        for r in data.get("workflow_runs", []):
            print("RUN ID:", r["id"], "| Branch:", r["head_branch"], "| Conclusion:", r["conclusion"], "| URL:", r["html_url"])
            jreq = urllib.request.Request(r["jobs_url"], headers={"User-Agent": "AntigravityDevOps"})
            with urllib.request.urlopen(jreq) as jresp:
                jdata = json.loads(jresp.read().decode())
                for j in jdata.get("jobs", []):
                    print("  -> JOB ID:", j["id"], "| Name:", j["name"], "| HTML:", j["html_url"])

if __name__ == "__main__":
    get_job_urls()
