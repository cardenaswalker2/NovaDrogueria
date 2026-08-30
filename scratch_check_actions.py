import urllib.request
import json
import sys

def check():
    url = "https://api.github.com/repos/cardenaswalker2/NovaDrogueria/actions/runs"
    req = urllib.request.Request(url, headers={"User-Agent": "AntigravityDevOps"})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        runs = data.get("workflow_runs", [])
        for r in runs:
            print(f"Run ID: {r['id']} | Status: {r['status']} | Conclusion: {r['conclusion']} | Branch: {r['head_branch']} | Event: {r['event']}")
            jobs_url = r["jobs_url"]
            jreq = urllib.request.Request(jobs_url, headers={"User-Agent": "AntigravityDevOps"})
            with urllib.request.urlopen(jreq) as jresp:
                jdata = json.loads(jresp.read().decode())
                for j in jdata.get("jobs", []):
                    print(f"  Job: {j['name']} | Status: {j['status']} | Conclusion: {j['conclusion']}")
                    for step in j.get("steps", []):
                        print(f"    - Step: {step['name']} | Status: {step['status']} | Conclusion: {step.get('conclusion')}")

if __name__ == "__main__":
    check()
