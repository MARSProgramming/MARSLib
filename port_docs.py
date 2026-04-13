import os
import glob
import re
import shutil

src_dir = 'docs'
dest_dir = 'website/src/pages'
static_dir = 'website/static'

# 1. Clean existing dummy pages
if os.path.exists(dest_dir):
    for f in glob.glob(os.path.join(dest_dir, '*.tsx')):
        os.remove(f)
if not os.path.exists(dest_dir):
    os.makedirs(dest_dir)

# 2. Copy Assets
assets_src = os.path.join(src_dir, 'assets')
assets_dest = os.path.join(static_dir, 'assets')
if os.path.exists(assets_dest):
    shutil.rmtree(assets_dest)
if os.path.exists(assets_src):
    shutil.copytree(assets_src, assets_dest)

# 3. Port HTML to React TSX
html_files = glob.glob(os.path.join(src_dir, '*.html'))

def clean_html(content):
    # Extract body content
    body_match = re.search(r'<body[^>]*>(.*?)</body>', content, re.IGNORECASE | re.DOTALL)
    if body_match:
        content = body_match.group(1)
    
    # Remove header and footer placeholders
    content = re.sub(r'<div id="header-placeholder[^>]*>.*?</div>', '', content, flags=re.IGNORECASE | re.DOTALL)
    content = re.sub(r'<div id="footer-placeholder[^>]*>.*?</div>', '', content, flags=re.IGNORECASE | re.DOTALL)
    
    # Remove old scripts as they will be injected globally via config
    content = re.sub(r'<script.*?</script>', '', content, flags=re.IGNORECASE | re.DOTALL)
    
    # Rewrite links: index.html -> /MARSLib/
    content = re.sub(r'href="index\.html(#[^"]*)?"', r'href="/MARSLib/\1"', content)
    # Rewrite feature.html -> feature
    content = re.sub(r'href="([a-zA-Z0-9_-]+)\.html(#[^"]*)?"', r'href="/MARSLib/\1\2"', content)
    
    # Replace javadoc links to point to the static javadoc folder
    content = content.replace('href="javadoc/', 'href="/MARSLib/javadoc/')

    # Escape raw unclosed operators and generics in legacy HTML
    content = re.sub(r'<\s+', '&lt; ', content)
    content = content.replace('<-->', '&lt;--&gt;')
    content = content.replace('<>', '&lt;&gt;')
    content = re.sub(r'<([A-Z][a-zA-Z0-9_]*)>', r'&lt;\1&gt;', content)

    # Escape backticks and standard react interpolations
    content = content.replace('`', '\\`')
    content = content.replace('$', '\\$')
    return content

for file_path in html_files:
    filename = os.path.basename(file_path)
    base_name = os.path.splitext(filename)[0]
    
    with open(file_path, 'r', encoding='utf-8') as f:
        html_content = f.read()
    
    cleaned_html = clean_html(html_content)
    
    component_name = ''.join([part.capitalize() for part in base_name.replace('-', '_').split('_')])
    if base_name == 'index':
        component_name = 'Home'
    
    tsx_content = f"""import React, {{ useEffect }} from 'react';
import Layout from '@theme/Layout';

export default function {component_name}() {{
  return (
    <Layout title="{base_name.replace('-', ' ').title()}">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{{{ __html: `{cleaned_html}` }}}} />
      </div>
    </Layout>
  );
}}
"""
    tsx_path = os.path.join(dest_dir, f"{base_name}.tsx")
    with open(tsx_path, 'w', encoding='utf-8') as f:
        f.write(tsx_content)

print("Ported all HTML files to React TSX.")
