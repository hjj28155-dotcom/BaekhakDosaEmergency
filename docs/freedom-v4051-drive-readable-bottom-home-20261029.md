# V4.0.51 드라이브 업데이트·가독성·우측 최하단 홈

2026-10-29 KST. 기준 V4.0.50 5fff92e35e56ee570d608a104cbaeaf910bc0bee.
브랜치 fix/freedom-v4051-drive-readable-bottom-home-20261029.

## 선장 지시 및 변경
- 기존 화면은 번호가 작은 반면 0/6 원과 여백이 큼. 번호 6개를 각 카드의 전체 폭에 표시하고 조합/적중수는 작은 상단 메타로 분리. 4묶음×5조합과 기존 채점 유지.
- 홈 버튼은 자동20조합/원문분석·원문입력/업데이트 화면 우측 최하단, 스크롤 영역 바깥에 고정. 상단 닫기/홈은 이 화면들에서 숨김. 최신 지시에 따라 이전 상단 홈 유지 지시는 대체됨.
- 네이티브 업데이트 버튼은 신규설치 안내 페이지가 아니라 검증된 공식 메타데이터의 Drive APK URL을 직접 실행.
- 버전 조회 오류가 나도 수동 Drive 버튼을 숨기지 않음. 공식 메타데이터가 없거나 설치본보다 오래된 경우 Drive 시험판 폴더를 열어 사용자가 최신 후보 파일에 접근 가능.
- Drive 시험판 폴더: https://drive.google.com/drive/folders/1VI4a3RZfBdNxfs6unkZzEIaeI4a4bnMY (항행의자유_배포판 아래).
- 기존 공식 버전정보 JSON 파일이 main/작업 브랜치에 없음을 확인. main에 기존 공식 4.0.41 Drive APK를 그대로 가리키는 freedom-release-latest.json 추가. 기존 설치/업데이트 공급 APK를 시험판으로 승격하지 않음.
- 승인, DB-1, 전체 원문, 조합, 최초잠금, V3/V7 및 기존 APK 삭제/초기화 없음.

## 검증 상태
로컬 20조합/전체원문/잠금, 축하·낙선 음성, 자동 홈 알림, Drive 경로 회귀검사 통과.
다음: CI 실제 브라우저 6화면 크기 렌더·홈 위치/120번호 표시 검사, 원본 동일서명 APK 빌드, Drive에 APK 단일파일 업로드·메타데이터 확인.
휴대전화 실제 업데이트·화면·음성 미검증. 공식 시험판 승격 금지. GitHub ZIP을 사용자 설치경로로 제시하지 않음.


## 최종 확인 · 2026-10-29 01:53 KST
- 검증 소스 커밋 5b7d183d99ca6631ffa47bc4921e109ad9697a1c.
- CI https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40729627979 모든 단계 성공.
- 실제 Chromium 347×768, 360×800, 393×851, 412×915, 720×1000, 320×568에서 20행/번호120개 모두 표시, 번호 겹침 없음, 홈 우측 최하단 및 스크롤 독립 위치 검사 통과. 원문입력/업데이트 홈 클릭 및 고정 검사 통과. 347×768 렌더 이미지 육안 확인 완료.
- 원본 동일 인증서 bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00, package com.navigator.freedom.v3final / versionCode40051 확인.
- APK SHA256 20908b930bc500d210162dff626e6f8b928944f764ba170e306f58415837773a, 48013530 bytes.
- Drive APK 단일파일 업로드 및 메타데이터 readback 확인 (MIME application/vnd.android.package-archive, 48013530 bytes, 의도한 폴더).
- 사용자 업데이트 링크: https://drive.google.com/file/d/1PpZeDrzyQK77zzy8qxVDpGcL_KuUSSwI/view?usp=drivesdk
- 공개 버전정보 파일 main 추가 후 Pages 배포 CI40729096819 성공, 실제 HTTPS GET200 및 기존40041/Drive URL 응답 확인. 공식 신규설치·업데이트 APK 공급 링크 시험판 승격 없음.
- 현재 설치된 구버전은 새 Home 배치를 소급 적용할 수 없으므로 위 Drive APK로 업데이트 설치해야 함. 기존 앱 삭제/데이터 초기화 금지.
- 다음 1순위: 선장 휴대전화 실제 업데이트·승인/DB유지·화면·음성 확인. 실기기 검증은 아직 미완료.
