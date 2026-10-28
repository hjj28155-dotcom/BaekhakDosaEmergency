# ⚓ 선장 중앙통제실 V1.0.26 — V4 실제 PNG 결과보고서 및 저장 기능

## 선장 실제 화면 지적
2026-10-28: V4 분석방 화면 사진, “여기는 왜 이렇게 나와요......png로 안나오고요”.
기존 V1.0.25는 선장엔진 R46의 5조합을 `<ol><li>` 일반 문자로만 표시해 모바일 화면에서 `01 · 04 · 07 · 10 · …`으로 **6번호가 잘림**. 연락관 R1은 독립 사전원본이 없어 5개 빈칸. PNG 생성기 및 저장 경로 없음.
선장 기존 명령: V4 결과는 우측 PNG로 한 화면에 표시, 5조합×2 독립, 원본 없는 조합 임의생성 금지.

## 수정된 소스
- Repo `hjj28155-dotcom/BaekhakDosaEmergency`, branch `feature/crew-meeting-standalone-apk-20261025`, 최종 코드 commit `a3db001e7abb1b91da0a67a5635a939450495a5b`.
- `crew-meeting-android/app/src/main/assets/captain_home_v114.html`: V4 분석방의 기본 화면을 **실제 PNG 이미지** `<img id="freedomPngPreview">`로 전환, `PNG 저장`과 `확대/전체 보기` 버튼, 원문 번호/엔진 상태는 `<details id="freedomTextBackup">`에 보존. 기존 문자 잘림 CSS는 백업 펼침에서도 줄바꿈 허용. 기존 R46·R1 엔진 로직은 변경하지 않고 **검증 완료 시에만** `captain-v4-verified` 이벤트로 실제 5×6 번호·회차·DB1 원문 SHA 전달. 새 분석/초기화 때 이전 PNG 데이터 무효화.
- `v4_png_report.js`: 960×1210 PNG Canvas로 선장 R46 5행×6번호와 연락관 R1 5행×6번호를 한 장으로 출력. 1~45 범위, 중복 없음, 각 방 정확히 5조합, SHA256·엔진 출처 검증 통과 시만 번호 표시. 연락관 원본 미검증이면 `—`와 '독립 원본 확인 대기' 표기. 기존 PNG는 `data:image/png;base64,` 진짜 PNG로 렌더링. 휴대전화 작은 화면은 PNG 전체 맞춤, 필요 시 확대 후 내부 스크롤. PNG는 공식 결과전 잠금 증빙이라고 오인하지 않도록 하단에 연구자료 주의 문구.
- `MainActivity.java`: 앱 내 고정 파일명과 PNG 헤더·크기 제한을 검증하는 `CrewPngSaver.savePng` 추가. Android 10+는 MediaStore `Pictures/항행의자유`에 저장; Android 7~9는 시스템 파일 저장 위치 선택기로 PNG 저장. 기존 네트워크 및 앱 연결 권한 변경 없음. 새 PNG JS 파일을 오프라인 WebView asset 인터셉트로 서빙.
- `app/build.gradle`: versionCode 126, versionName `1.0.26-CAPTAIN-V4-PNG-REPORT`, package `kr.hanghae.crewmeeting.compact` 그대로, 기존 동일 서명 키. CI에 실제 PNG Canvas 코드, 원본 이벤트, 확대, native bridge 검증 추가.
- 기존 V3/V7/V4 앱, 서버, DB-1 1,247회차, 결과전/결과후 잠금, 자료실, 승인관리, 공식 APK/URL **삭제·초기화·변경 없음**.

## 실제 검증
- GitHub Actions **40684862742 SUCCESS** https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40684862742
- GitHub Actions artifact **12813704195** `HANGHAE-CREW-MEETING-1.0.26-V4-PNG-REPORT-APK`.
- APK `HANGHAE_CREW_MEETING_1.0.26_V4_PNG_REPORT.apk` 6,505,987 bytes, SHA256 `c568dfdff36fce42f2570ed4299f8f4e8558ceefb156dc27572998e2ed60456b`. ZIP CRC PASS 및 ZIP 내부 SHA256 명세와 동일. 원래 서명 인증서 CI PASS.
- **실제 APK에서 추출한 HTML/CSS/JS**를 Chromium/Playwright에서 320×600, 360×700, 390×844, 412×915 4개 크기로 시험. PNG MIME 데이터 URL·960×1210 해상도·기본 표시·기존 원문 접기·5/5 실데이터 이벤트 반영·회차 변경 시 이전 번호 제거·페이지 JS 오류 0·메인 스크롤 넘침 0 PASS. 320×600 PNG 맞춤→확대 169px→276px 너비 변경 PASS. PNG 저장 브리지 호출은 **모의 브리지**로 파일명과 base64 PNG 전달을 확인했으며 Android 실기기 저장은 미검증.
- **원본 R46 엔진 실구동**: APK 추출 `captain_engine_r46.html`에 APK 내장 DB-1 제1회차 원문 6문장을 입력하여 Chromium에서 `V3_ANALYZE` 실행. `V3_ANALYSIS_RESULT ok:true, packGate:PASS`, 5개 6번호:
  1. 01 04 07 10 36 37
  2. 01 03 04 07 36 37
  3. 01 03 04 10 36 37
  4. 01 03 04 07 10 36
  5. 01 03 04 07 10 37
  원문 SHA256 검증 PASS, JS 오류 0. 이 **실제 엔진 출력**으로 V4 PNG 미리보기 생성: `/mnt/data/항행의자유_V4_1회차_선장R46_실제엔진검증_PNG_미리보기.png`; 연락관 R1은 원본 미확인으로 빈칸 유지.
- UI 실제 화면 미리보기: `/mnt/data/항행의자유_V1.0.26_V4_PNG결과보고서_화면검증.png`.
- Google Drive 시험 ZIP 업로드 및 readback 확인: https://drive.google.com/file/d/1syV47vS1M976yWpi117E0AETKeM9mIhS/view?usp=drivesdk

## HOLD / 남은 작업
1. **연락관 R1 독립 사전원본 증빙 미확보**: 5조합을 임의로 채우지 않음. V3/도사 독립 사전원본 검증 뒤에만 PNG 반영.
2. **V3/V4/V7 원본 자동 자료실 업로드 미연결**: PNG는 중앙통제실 기기에서 생성/저장하며 서버 자동등록은 아님. 별도 인증 송신·원본 read-back 구축 필요.
3. 실제 선장 Android 휴대전화 설치/PNG 사진함 저장/갤러리 표시 미검증. Chromium 및 GitHub Actions 검증만 완료.
4. V4 PNG는 원본 분석 결과의 시각화이지 공식 추첨 전 잠금·실전 기록으로 자동 인증된 것은 아님.
