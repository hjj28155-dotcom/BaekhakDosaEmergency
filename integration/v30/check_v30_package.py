#!/usr/bin/env python3
"""Read-only V30 home preview archive recovery gate. Never extracts or writes to source."""
import argparse
import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path

EXPECTED_SHA = 'f43678175712a2243135c49b24feab02096f3fe42a76518b4356af1a7a929354'
ICON_SIZES = (48, 72, 96, 144, 192)
BUTTONS = ('원문 분석','선장 원문방','신서방','항해사방','분석방','성적 관리',
           '누적 DB','분석 기록','업데이트','백업','자료실','공지사항')

def audit(path: Path, expected_sha=EXPECTED_SHA):
    if not path.is_file():
        return {'ok':False,'reason':'ARCHIVE_NOT_FOUND','path':str(path)}
    with path.open('rb') as f:
        sha = hashlib.file_digest(f, 'sha256').hexdigest()
    result = {'path':str(path),'sha256':sha,'sha256_matches_reported':sha == expected_sha,
              'archive_valid':False,'html_found':False,'image_found':False,
              'icon_sizes_found':[],'buttons_seen':[],'errors':[]}
    if sha != expected_sha:
        result['errors'].append('V30_REPORTED_SHA256_MISMATCH')
    try:
        with zipfile.ZipFile(path) as z:
            infos = [i for i in z.infolist() if not i.is_dir()]
            if len(infos) > 500 or sum(i.file_size for i in infos) > 100_000_000:
                result['errors'].append('ARCHIVE_SIZE_LIMIT')
                return result
            names = [i.filename.replace('\\','/') for i in infos]
            if any(n.startswith('/') or '..' in n.split('/') for n in names):
                result['errors'].append('UNSAFE_PATH')
                return result
            if z.testzip() is not None:
                result['errors'].append('ZIP_CRC_FAILURE')
                return result
            htmls = [n for n in names if n.endswith('V30_실행시안/index.html') or n == 'index.html']
            result['html_found'] = bool(htmls)
            if htmls:
                html = z.read(htmls[0]).decode('utf-8', errors='replace')
                result['buttons_seen'] = [s for s in BUTTONS if s in html]
            images = [n for n in names if re.search(r'\.(png|jpe?g|webp)$',n,re.I)]
            result['image_found'] = bool(images)
            result['icon_sizes_found'] = [size for size in ICON_SIZES
                if any(re.search(rf'(?<!\d){size}(?!\d)', n) for n in images)]
            if not htmls:
                result['errors'].append('HOME_HTML_NOT_FOUND')
            if not images:
                result['errors'].append('IMAGE_ASSETS_NOT_FOUND')
            if len(result['icon_sizes_found']) != 5:
                result['errors'].append('FIVE_ICON_SIZES_NOT_DETECTED')
            if len(result['buttons_seen']) < 12:
                result['errors'].append('TWELVE_BUTTON_LABELS_NOT_DETECTED')
            result['archive_valid'] = True
    except (zipfile.BadZipFile, OSError) as exc:
        result['errors'].append(type(exc).__name__)
    result['ok'] = result['archive_valid'] and not result['errors']
    return result

if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('archive',type=Path)
    p.add_argument('--expected-sha',default=EXPECTED_SHA)
    args=p.parse_args()
    report=audit(args.archive,args.expected_sha)
    print(json.dumps(report,ensure_ascii=False,indent=2))
    sys.exit(0 if report.get('ok') else 2)
