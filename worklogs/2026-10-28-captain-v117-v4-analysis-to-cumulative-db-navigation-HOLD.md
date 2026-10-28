# ⚓ 중앙통제실 V1.0.17 — V4 원문 분석방 → 누적DB 이동 (2026-10-28)

## 선장 지시
- V4 항해의자유 분석 후 누적DB로 이동할 수 있는 버튼 추가.
- 앞선 지시: V4는 V3/V7과 동일한 앱 열기 흐름, 회차 분석 한 화면에 독립 5조합 × 2, 원문 DB-1 연동. **이번 버전은 그중 누적DB 이동 버튼만 구현**, 실제 엔진 10조합 연동과 한 화면 UX는 미완료.
- 기존 일반 V4 배포판, 고정 정식 Drive 링크, 승인관리, V3/V7 원본 및 자료는 변경 금지.

## 수정 및 구현
- Branch `feature/crew-meeting-standalone-apk-20261025`.
- HTML `crew-meeting-android/app/src/main/assets/captain_home_v114.html` commit `625c10da76fe25a7b58aa4bef0f4e81330ca5a3c`.
- V4 분석방에 `🗄️ 누적DB로 이동` (`freedomGoCumulativeDb`) 추가.
- 클릭 시 회차 1~1247 정수 검증, 기존 자료실 회차 필드에 값 전달, `homeView('materials')`, `materialsSelect('app','freedom')`, `materialsSelect('stage','db')`, 상세 구역 펼침 및 조회 호출. 기존 인증/서버 조회 로직 재사용; DB 원본 수정 없음.
- `versionCode=117`, `versionName=1.0.17-CAPTAIN-V4-DB-NAV-TEST`; Android integrity check와 CI 검사 추가.
- Build head `32f495f7c752d4a815c4eccd3ed5d81167d2184b`.

## 검증
- GitHub Actions https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40633457363 — **success**. Same signer/package/version verified by CI.
- Artifact ID `12791918762`, ZIP 63,917 bytes.
- APK 67,105 bytes, SHA-256 `dbbfcf279264afdbdcbc025864fa90f503c3aa2bb1f8f593947d943eac3b4770`; artifact .sha256 match, ZIP and APK CRC pass, embedded HTML button+DB route present.
- Direct conversation APK: `/mnt/data/항행의자유_중앙통제실_선장전용_V1.0.17_V4누적DB_검증.apk`.
- Google Drive ZIP https://drive.google.com/file/d/1HCV7FsyoTVLd78vc_1oxLL9W_ypIY91Z/view?usp=drivesdk — upload success and raw fetch verified size 63,917 bytes.
- **실제 선장 휴대전화 동작 검증 미완료**.

## HOLD
1. V4 5조합×2 독립 원문 엔진 호출 및 결과 생성, 원문 DB-1 제1~1247회차 실연동, 중복/출처 검증 미완료.
2. V4 앱 열기 → 회차 분석 → 5+5 결과 한 화면 표시 UX 미완료.
3. 결과전·결과후 원본 자동송신, 누적DB 원문 실데이터 연동 미검증. 현재 버튼은 기존 누적DB **등록 메타 조회 화면으로 이동**만 함; 원문 DB-1 전체를 읽는 기능으로 오인 금지.
4. V1.0.16 및 기존 정식 배포 링크 유지. 원본·구매잠금·승인 기록 보존.
