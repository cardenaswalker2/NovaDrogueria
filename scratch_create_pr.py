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

def create_pull_request():
    token = get_token()
    url = "https://api.github.com/repos/cardenaswalker2/NovaDrogueria/pulls"
    payload = {
        "title": "feat: endpoint de estado del sistema y telemetría de salud (GET /api/estado)",
        "head": "feature/endpoint-estado",
        "base": "main",
        "body": "### Taller CI/CD — Tecnológico Comfenalco\n\n**Proyecto:** NovaDrogueria\n**Integrantes:** Luis Cardenas, Cristobal Villamil, Daniel Gutierrez, Jose Castillo\n\nEste Pull Request introduce el endpoint `/api/estado` para monitorización de salud del sistema, incluyendo verificación de base de datos MongoDB Replica Set (rs0) y pruebas automáticas con MockMvc."
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
        method="POST"
    )
    
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
            print(f"SUCCESS: Created Pull Request #{data['number']}")
            print(f"PR URL: {data['html_url']}")
            print(f"State: {data['state']}")
            print(f"Head: {data['head']['ref']} -> Base: {data['base']['ref']}")
            return data
    except urllib.error.HTTPError as e:
        print(f"HTTPError: {e.code} - {e.read().decode()}")
        return None

if __name__ == "__main__":
    create_pull_request()
