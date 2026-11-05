# V13 R9.13 + R25 AndroidIDE 빌드 실패 수정 — 2026-11-05

## 선장 보고
- AndroidIDE Gradle BUILD FAILED: `app/src/main/assets/shinseo_original_notice001/????????_HYBRID_GENERATOR_FINAL_CORE_INTEGRATION_V0.2_NOTICE001_20260909.js` 파일이 존재하지 않는다는 오류.
- 이 오류는 설치 APK 서명 문제가 아니라 압축해제된 리소스 파일명/경로 문제. 원본 ZIP에는 실제 한글 명칭 자산 파일이 포함돼 있으며, AndroidIDE 추출본에선 물음표로 훼손된 것으로 추정됨.

## 안전 수정
- 이전 V13 통합 ZIP SHA256 `6d56776f1dbd82960fc64e0c5cd1eea59816c37ffed499faf55c7b338733f5be` 사용.
- `app/src/main/assets/shinseo_original_notice001/` 내 한글 파일명 네 개만 ASCII 영문 파일명으로 치환. **파일 본문 및 엔진 바이트 변경 없음**. 다른 기존 파일 내용/경로 변경 없음.
- 설명 파일 `ANDROIDIDE_ASSET_ASCII_FILENAME_FIX_README.txt` 한 개 추가.
- 신규 ZIP `V13_R913_R25_선장전용_AndroidIDE_파일명수정_20261105.zip` size 21,050,333 bytes, SHA256 `991007c3240b908f3585aff07d1ef23046903fad294970bcfd80e26322b34c8e`.
- 압축 기존 비디렉터리 299개 파일 바이트 대조 **299/299 동일**, 네 파일 경로만 변경, ZIP CRC PASS, Android `app/src/main/assets`의 파일명 ASCII 검사 PASS.
- Gradle/AndroidIDE 실기기 재빌드는 선장 기기에서 수행해야 확인 가능. **빌드 통과로 보고하지 않음**.
- 수정 ZIP은 ChatGPT 대화 작업공간과 Library 저장 완료; Drive 업로드는 커넥터 파일 참조 제약으로 미완료. 이전 Drive ZIP에는 여전히 한글명칭 경로가 있으므로 이전 링크로 재빌드하지 말 것.
- 기존 APK 삭제·데이터 초기화 금지. 빌드 성공한 APK도 동일 서명 검증 전 설치 금지.
