import hashlib
import tempfile
import unittest
import zipfile
from pathlib import Path
from check_v30_package import audit

class RecoveryTests(unittest.TestCase):
    def test_missing(self):
        self.assertEqual(audit(Path('/not-present-v30.zip'))['reason'],'ARCHIVE_NOT_FOUND')
    def test_good_synthetic(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d)/'s.zip'
            labels=' '.join(('원문 분석','선장 원문방','신서방','항해사방','분석방','성적 관리','누적 DB','분석 기록','업데이트','백업','자료실','공지사항'))
            with zipfile.ZipFile(p,'w') as z:
                z.writestr('V30_실행시안/index.html',labels)
                for n in (48,72,96,144,192):
                    z.writestr(f'V30_실행시안/icons/icon_{n}.png',b'fake-image-for-contract-test')
            sha=hashlib.sha256(p.read_bytes()).hexdigest()
            r=audit(p,sha)
            self.assertTrue(r['ok'],r)
    def test_sha_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d)/'not-original.zip'
            with zipfile.ZipFile(p,'w') as z:z.writestr('index.html','hi')
            r=audit(p)
            self.assertIn('V30_REPORTED_SHA256_MISMATCH',r['errors'])
            self.assertFalse(r['ok'])
    def test_path_traversal_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d)/'bad.zip'
            with zipfile.ZipFile(p,'w') as z:z.writestr('../evil.txt','bad')
            r=audit(p,hashlib.sha256(p.read_bytes()).hexdigest())
            self.assertIn('UNSAFE_PATH',r['errors'])

if __name__=='__main__': unittest.main()
