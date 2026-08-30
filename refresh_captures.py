import subprocess
import os
import time

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
out_dir = r"C:\Users\USUARIO\Music\CASA\NovaDrogueria\evidencias\cicd"
os.makedirs(out_dir, exist_ok=True)

captures = [
    ("01-build-local.png", r"file:///C:/Users/USUARIO/Music/CASA/NovaDrogueria/target/site/jacoco/index.html"),
    ("02-actions-success.png", "https://github.com/cardenaswalker2/NovaDrogueria/actions/runs/33326051794"),
    ("03-tests-success.png", "https://github.com/cardenaswalker2/NovaDrogueria/actions/runs/33326051794/job/99296251436"),
    ("04-jacoco-artifact.png", "https://github.com/cardenaswalker2/NovaDrogueria/actions/runs/33326051794"),
    ("05-jacoco-report.png", r"file:///C:/Users/USUARIO/Music/CASA/NovaDrogueria/target/site/jacoco/index.html"),
    ("06-pipeline-failed.png", "https://github.com/cardenaswalker2/NovaDrogueria/actions/runs/33325679422"),
    ("07-pipeline-recovered.png", "https://github.com/cardenaswalker2/NovaDrogueria/actions/runs/33325737975"),
    ("08-branch-protection.png", "https://github.com/cardenaswalker2/NovaDrogueria/settings/branches"),
    ("09-secret-config.png", "https://github.com/cardenaswalker2/NovaDrogueria/settings/secrets/actions"),
    ("10-pull-request.png", "https://github.com/cardenaswalker2/NovaDrogueria/compare/main...feature/endpoint-estado"),
    ("11-pull-request-merged.png", "https://github.com/cardenaswalker2/NovaDrogueria/actions/runs/33326051794"),
    ("12-main-final-success.png", "https://github.com/cardenaswalker2/NovaDrogueria/actions?query=branch%3Amain"),
]

for filename, url in captures:
    dest = os.path.join(out_dir, filename)
    cmd = [
        edge_path,
        "--headless",
        "--disable-gpu",
        "--hide-scrollbars",
        "--window-size=1400,900",
        f"--screenshot={dest}",
        url
    ]
    print(f"Capturing {filename} from {url}...")
    subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(1)
    if os.path.exists(dest):
        print(f"  -> OK ({os.path.getsize(dest)} bytes)")
    else:
        print(f"  -> FAILED to create {dest}")

print("All screenshots refreshed successfully!")
