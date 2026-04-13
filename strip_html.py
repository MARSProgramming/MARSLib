import os
import re

directory = "website/docs/tutorials"

for root, _, files in os.walk(directory):
    for file in files:
        if not file.endswith(".md"):
            continue
            
        path = os.path.join(root, file)
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
            
        # Target only EXACT full lines for removal to avoid matching nested tags incorrectly
        # Strip <main class="container" >
        content = re.sub(r'^\s*<main\s+class="container"\s*>\s*$', '', content, flags=re.MULTILINE)
        content = re.sub(r'^\s*<main\s+className="container"\s*>\s*$', '', content, flags=re.MULTILINE)
        content = re.sub(r'^\s*</main>\s*$', '', content, flags=re.MULTILINE)
        
        # Only remove the very first <div > and </div> that immediately wrap the H1
        # This regex strictly targets the H1 block
        content = re.sub(r'^\s*<div\s*>\s*<h1>.*?</h1>\s*</div>\s*$', '', content, flags=re.MULTILINE | re.DOTALL)
        
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)

print("Done stripping root DOM tags safely.")
