# ⚓ V13 R8.29 / 원격 15회차 이후 실물 감사 — 2026-11-01 KST

**판정: 정상화 미완료 / 공개 원격 15회차 유지 / 기존 APK·DB·원본·잠금·승인 기록 불변.**

## 확인한 실물 및 버전
- GitHub `releases/v13-release.json`: `latestVersionCode=31337`, `versionName=V13 R8.29`, `uiUpdate.revision=15`.
- Google Drive 실제 APK: [V13 R8.29 31337](https://drive.google.com/file/d/1zBa-KXjceUxj7gbwUp_RTHTCadVoTm0S/view) — 15,032,713 bytes; SHA256 `6ccd9440ff79efb12ab8415de7922179f9a0ff71e8e64befa1ec3d7e3f1c4f11`. ZIP CRC 정상.
- APK 내장 `assets/index.html` SHA256 `07126717a8734b887a070d7f008c8d97aac50c260ba8b4ba5631f70d61874668`.
- **내장 APK**의 6방×5조합 및 `v13_independent_engine_contract.js`(전방 5조합)과 **원격 15회차**의 4방 5+5+15+5 구조가 불일치. 원격 UI가 실기기에 적용되는지 확인 필요.
- 내장 `V13_RESULT_BEFORE.registerEngine` 호출은 도사·항해사 연구용 2개뿐. 선장방·항행의자유방 정식 엔진 없음.
- 내장 DB-1의 `records` 1~1248회만 확인. 1249회 원문 미등록.

## 원본 자료 조사
- V12 R24/R25 소스 ZIP CRC 정상, R24 음성 브리지 모의시험 PASS, R25 음성/멘트 모의시험 7건 PASS(시험 스크립트의 원래 고정 절대경로를 별도 검사 복사본에서만 조정).
- [V3 R17 연구 ZIP](https://drive.google.com/file/d/15mxahUtaHyRQQJ01NV1rbbLRkQi9rjHH/view): 1~100회 *구조* 시험 100/100 PASS. 이 자료는 **원문10/자동15=25조합 연구**, V13 공식 30조합 또는 1249회 결과전이 아님.
- [V3 MASTER R4 별도 폴더](https://drive.google.com/drive/folders/1xfeLuyPmgsClmKbWpybSDlL-eQW3mhZV) 하위 `app/src/main/assets`가 현재 목록상 비어 있음. 이 폴더만으로 정식 엔진 원본 확인 불가. R62/V33을 MASTER로 대체 금지.

## APK 내장 화면 모의시험
- Chromium `page.set_content`으로 추출 APK HTML/JS를 393×852, 768×880에서 실행: 시험 범위 내 pageerror 0건, 누적 DB(3)·성적관리(6)·네 방(7~10) 화면 DOM 이동 확인.
- 1249회 PNG **0건** 및 선장/항행 정식 엔진 미연결 상태 재현. Android 실기기 테스트 또는 원격 rev15 적용 테스트가 아님.

## 실제 안전 수정 — 미배포 초안
- 발견 결함: 원격 rev15 `scoreAfterDraw`는 공개 미검증 번호(`PUBLIC_MIRROR_UNVERIFIED`)만 있어도 성적/축하/PNG가 계산될 수 있음.
- [Draft PR #6](https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/pull/6), branch `audit/v13-r829-score-gate-20261101`, candidate `releases/staging/v13-ui-r16-candidate-official-score-gate.html`.
- 후보는 `verifiedOfficial===true && source==='DHLOTTERY_2026'`에만 등수·성적 PNG·축하 및 누적 DB 공식 완료를 허용. 분리 점수 게이트 모의시험 5건 PASS. **병합·배포·APK 변경 없음**.
- 로컬 미배포 `v13-independent-room-contract-v2.js` 및 `test-contract-v2.cjs`: 5+5+15(RAW5/AUTO_A5/AUTO_B5)+5 구조/중복/연구자료/결과후 혼입 차단, 14건 PASS. **엔진 생성기가 아님**.

## 다음 작업/완료 기준
1. 최신 V3 MASTER, 선장·도사·항행·항해 정식 독립 엔진 원본 및 승인 근거 확보.
2. 1249회 DB-1 원문 6문장 원본 정확성·지문 검증.
3. 4방 30조합 실제 출력·각 방 PNG·합본 PNG·공식 자동 당첨번호·AFTER PNG·누적 DB를 실기기 종단간 검증.
4. 동일 서명·업데이트 팝업·APK 설치를 확인한 뒤에만 공개 배포 변경.

**금지:** 25조합 연구자료를 30조합 정식 결과전으로 재명명, 임의 조합 생성, 미검증 결과 축하/낙선 판정, 기존 자료 삭제·초기화, 실기기 미검증 완료 주장.
