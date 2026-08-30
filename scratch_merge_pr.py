import subprocess
import urllib.request
import json

def get_token():
    p = subprocess.Popen(['C:\\Program Files\\Git\\mingw64\\bin\\git-credential-manager.exe', 'get'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    out, _ = p.communicate('protocol=https\nhost=github.com\n')
    for line in out.splitlines():
        if line.startswith('password='):
            return line.split('=', 1)[1].strip()
    return None

def merge_pr():
    token = get_token()
    url = "https://api.github.com/repos/cardenaswalker2/NovaDrogueria/pulls/1/merge"
    payload = {
        "commit_title": "Merge pull request #1 from cardenaswalker2/feature/endpoint-estado",
        "commit_message": "Integración formal del endpoint /api/estado tras validación exitosa de CI/CD.",
        "merge_method": "merge"
    }
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode('utf-8'),
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "User-Agent": "AntigravityDevOps",
            "Content-Type": "application/json"
        },
        method="PUT"
    )
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        print("Merge result:", data)

if __name__ == "__main__":
    merge_pr()
