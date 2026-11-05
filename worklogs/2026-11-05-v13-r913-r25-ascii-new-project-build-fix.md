# V13 R9.13 + R25 AndroidIDE 반복된 누락 파일 오류 대응 (2026-11-05 KST)

## 선장 보고
- 선장 AndroidIDE 화면: `V13_R913_STAGE4_RELEASE/app/src/main/assets/shinseo_original_notice001/????????_HYBRID_GENERATOR_FINAL_CORE_INTEGRATION_V0.2_NOTICE001_20260909.js` does not exist; BUILD FAILED, 2 executed, 14 up-to-date.
- 앞서 영문 파일명으로 수정한 ZIP 내부 해당 파일은 이미 `shinseo_hybrid_generator_final_core_integration_v02_notice001_20260909.js`로 존재하므로, 같은 구버전 경로로 빌드 실패가 난 것은 기존 압축해제 프로젝트를 다시 열었을 가능성이 높다. 전체 Gradle 로그 확인 전 절대 원인 확정 아님.
- 원본 V13/DB/기존 앱 설치본 변경 없음.

## 새 폴더를 강제하는 수정 ZIP
- 입력: `V13_R913_R25_선장전용_AndroidIDE_파일명수정_20261105.zip`.
- 출력: `V13_R913_R25_ANDROIDIDE_ASCII_NEW_PROJECT_20261105.zip`.
- 루트 디렉터리 `V13_R913_R25_ASCII_BUILD_20261105/` (기존 `V13_R913_STAGE4_RELEASE/`와 명백히 다름).
- 이전 영문 변경된 4개 신서방 자산 유지, 나머지 한글/깨진 유니코드 이름 23개는 경로만 ASCII `legacy_qa_NNN`로 교체(기존 문서/QA 파일이며 app 소스/엔진 내용 수정 없음).
- 기존 300개 파일의 **압축 해제 후 본문 바이트 전부 동일(300/300)**. 프로젝트 루트에 `BUILD_README_FIRST.txt` 추가.
- ZIP 내 경로 **전체 ASCII**, 중복 없음, CRC PASS, settings.gradle 존재 검증.
- 새 ZIP 21,053,954바이트, SHA256 `c053ca738a01143f392ac922a19d813058044f71cb8a4e3aaad0a52870ffcd1e`.
- AndroidIDE Gradle 실제 빌드 및 선장폰 설치 아직 미검증; 기존 서명 미확인. 빌드 완료 주장 금지.

## 선장 실행 요령
1. AndroidIDE에서 현재 열려 있는 `V13_R913_STAGE4_RELEASE` 프로젝트 **닫기**. 기존 폴더 삭제할 필요 없음.
2. 이번 새 ZIP을 이전 프로젝트 경로와 다른 새 폴더에 압축해제.
3. `V13_R913_R25_ASCII_BUILD_20261105` 안의 `settings.gradle`이 존재하는지 확인하고 **그 폴더를 새 프로젝트로 열기**.
4. 빌드 로그 경로 첫 부분이 여전히 `V13_R913_STAGE4_RELEASE`이면 이전 폴더를 빌드 중인 상태이므로 재선택.
5. 실패하면 화면 상단 `Copy Log`의 전체 내용을 요청하여 실제 Gradle 입력 경로 추적.
6. 기존 V13 앱 삭제·기록 초기화·새 APK 즉시 설치 금지. 인증서 동일 여부 확인이 우선.
