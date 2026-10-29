# V8.0.7 실제 원문분석 불능 원인 수정 — 2026-10-29

선장 보고: V8 대화방, V3 자동연동, 원문분석 모두 작동하지 않음. 공식 파일은 Google Drive로만 전달.

## V8.0.6 원본 APK 직접 재현한 확정 결함
- 다운로드한 원본 APK SHA-256 225de7c3aabdd45a1f91248ad37a4808976c76fa020714b75fd94c29e062dfa6.
- APK 내 HTML이 요청한 crew-db1-1-1247.json / crew-captain-engine-r46.html / crew-liaison-blind-r1.mjs **3개 파일 모두 APK 내 부재**.
- 실제 APK 파일은 assets/db1_rounds_1_1247.json / assets/captain_engine_r46.html / assets/liaison-blind-engine.mjs.
- 원문 DB1 1~1247회차의 각 source_text SHA-256과 source_sha256 **1247/1247 일치**.
- 기존 V8.0.6 원문분석은 경로 문제로 DB와 R46 엔진 로드 실패가 확정됨.

## 실제 변경
- crew-meeting-android/app/src/main/assets/captain_home_v114.html 의 위 세 경로를 실제 APK 자산명으로 수정 (bfb00142489a2c5646166da5bf348672a8993bd7).
- build.gradle versionCode 807, versionName 8.0.7-DIRECTOR-ASSET-FIX로 상승 (cdd0947b2deeb56a749257f4ab005559fab6ddd6).
- APK CI의 파일 경로 존재/구경로 부재 회귀 검사 추가 및 V8.0.7 아티팩트명으로 변경 (3a6bcb354708657341e3e3b356adcf6a93e31207).
- CI workflow run 40808853484 시작, 완료/서명/Drive 배포 확인 전에는 APK 완료로 보고하지 않음.

## 중요하게 남은 장애
- 통제실장 R1은 독립 보관 V3·도사 사전원본의 증빙 없이는 정상 연구 결과를 생성할 수 없도록 설계됨. 파일명 수정으로 독립 증빙을 대체하지 않음.
- 원본 ChatGPT 대화방 연결은 URL 열기 기능이지 자동 메시지 읽기/쓰기/세션 동기화가 아님. 실제 자동 동기화는 원본방 권한과 명시적 커넥터 연동이 필요.
- V3 R60 자동송신은 동일서명 native host 통합/단말 등록/ACK 실기기 검증 미완료. V8만 고쳐도 자동 전송이 생기지 않음.
- V8 공동 소통방은 서버 입장키 인증과 실제 이벤트 읽기/쓰기 실기기 검증 필요.
- 기존 DB/잠금/원본/서명/V7 서버 자산 삭제·초기화 없음.
