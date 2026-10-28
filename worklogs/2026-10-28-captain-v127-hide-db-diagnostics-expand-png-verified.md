# ⚓ 중앙통제실 선장 V1.0.27 — V4 문구 숨김 · PNG 확대

## 지시·원인
2026-10-28 선장 실제 V1.0.26 화면 캡처에서 '✓ 내장 DB-1 원문 1,247회차 · 6문장/회 · APK 탑재 확인', '선택 회차 DB-1 원문 6문장 보기', '제1회 · 연락관엔진 R1 보류 ... (INVALID_ROUND)'이 PNG 아래 표시되는 문제. 선장: “숨기시고 png를 더 늘레세요....”

## 실제 수정
- repo `hjj28155-dotcom/BaekhakDosaEmergency`, branch `feature/crew-meeting-standalone-apk-20261025`, 최종 빌드 코드 commit `43ae9b69c99eb630243174cd168af398fd2707c4`.
- `captain_home_v114.html`에 V4 전용 CSS로 다음 요소를 **시각적으로만 숨김**: `#freedomDbStatus`, `.v4DbOriginal`, `#freedomAnalysisStatus`, `#freedomTextBackup`, `#freedomPngStatus`. **내부 원본 검증·DB-1·에러 검출·엔진 원문·결과 자체는 삭제하지 않음**.
- V4 본문 패딩 9→4px, PNG 패널 7→3px, 내부 이미지 너비 100%, 높이 비례 자동, 이미지 영역에 남은 공간 전부 할당. 확대 버튼은 145% 너비로 실제 확대하고 스크롤 지원. 4개 하단 버튼 유지, 높이만 소폭 압축.
- `v4_png_report.js` Canvas 960×1210→**960×1350**, 번호 원 크기 26→29, 번호 글씨 24→31, 6개 번호 가로 위치를 x=184+130j로 넓힘, 5행 세로 간격 52→60. 5조합×2 독립 검증, 출처 게이트, 연락관 원본 미검증 빈칸, PNG 저장 기능 유지.
- `app/build.gradle` versionCode 127 / `1.0.27-CAPTAIN-V4-PNG-EXPANDED`; `MainActivity.java` app_version=127. 기존 패키지·서명키·서버·V3/V7·공식 V4 앱·원본 DB·잠금·승인관리·공식 배포 링크 변경 없음.
- CI 검증에 숨김 CSS·이미지 전체 너비·Canvas 1350 높이·번호 위치 추가.

## 실제 빌드/검증
- GitHub Actions run **40686288603 SUCCESS**: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40686288603
- artifact ID **12814524391**, `HANGHAE-CREW-MEETING-1.0.27-V4-PNG-EXPANDED-APK`.
- APK `HANGHAE_CREW_MEETING_1.0.27_V4_PNG_EXPANDED.apk`, **6,506,367 bytes**, SHA-256 **6bd79a5c21c30ef55231a746522b28dedbaeeb681182c886aa9c76bd9300e8ff**. ZIP CRC PASS, APK CRC PASS, 원본 R46 engine SHA256 `b40c756387dd56e38b0e4119b96da7903d53fe556acceb2184309b9b33d17f46`; DB1 원문 1247회차 APK 탑재 확인. CI 동일 서명 확인.
- **실제 APK에서 추출한 HTML/CSS/PNG JS**를 Chromium/Playwright 320×600, 360×700, 390×844, 412×915 테스트: 4개 숨김 모두 display:none, 진짜 `data:image/png;base64`, 자연 해상도 960×1350, 페이지/방 수직 스크롤 넘침 없음, JS 오류 0, 확대 버튼 390×844에서 이미지 너비 356→517px PASS. 테스트의 R46 1회차 5조합은 이전에 검증한 실제 엔진 출력을 이벤트로 주입하여 UI 표시를 확인; 이번 테스트가 엔진 신규 계산을 수행한 것은 아님.
- 390×844 화면 검증 이미지: `/mnt/data/항행의자유_V1.0.27_문구숨김_PNG확대_화면검증.png`.
- APK: `/mnt/data/항행의자유_중앙통제실_선장전용_V1.0.27_PNG확대_문구숨김.apk`.
- Google Drive ZIP 업로드 및 read-back 성공: https://drive.google.com/file/d/1KHRGsgfY1nKu7jRNiCm6QBl71CUXCRBD/view?usp=drivesdk

## HOLD
- 연락관 R1 원본 사전 검증 미완료; 오류 문구만 시각적으로 숨겼으며 R1 분석·잠금 문제가 해결됐다는 의미 아님. PNG 미검증 번호 빈칸 유지.
- 실기기 Android 설치·PNG 사진함 저장·V4 원본 자동업로드 미검증. 검증 범위는 서명 CI와 Chromium APK 자산 UI.
