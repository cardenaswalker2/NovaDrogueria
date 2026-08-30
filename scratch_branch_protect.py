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

def set_branch_protection():
    token = get_token()
    url = "https://api.github.com/repos/cardenaswalker2/NovaDrogueria/branches/main/protection"
    payload = {
        "required_status_checks": {
            "strict": True,
            "contexts": ["Build, Test & JaCoCo Coverage"]
        },
        "enforce_admins": False,
        "required_pull_request_reviews": None,
        "restrictions": None
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
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
            print("Branch Protection successfully applied:", data.get("required_status_checks"))
    except urllib.error.HTTPError as e:
        print("Error setting protection:", e.code, e.read().decode())

if __name__ == "__main__":
    set_branch_protection()
