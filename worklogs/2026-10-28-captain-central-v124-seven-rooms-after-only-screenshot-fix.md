# ⚓ 선장 중앙통제실 V1.0.24 — 누락 3방 복원 · 7방 결과후 전용

## 선장 요청
2026-10-28, V1.0.23 실제 화면 사진: “나머지 3방이 없어요”.
확인: V1.0.23 통합 누적DB는 V3 4방(도사방/항해사방/자동1방/자동2방)만 렌더링했고, V7 통제관방 및 V4 1차·2차가 빠져 있었음. 사진에 'Failed to fetch' 결과후 원본 조회 오류도 존재.
**정정 기준: V3 4방(20조합) + V7 통제관방(5조합) + V4 선장 독립 1차(5조합) + V4 연락관 독립 2차(5조합) = 7방 35조합.** 통합 누적DB에는 결과후만 표시하고 원본 결과전·기존 DB는 보존.

## 실제 코드 변경
- Repo: `hjj28155-dotcom/BaekhakDosaEmergency`, branch `feature/crew-meeting-standalone-apk-20261025`.
- `crew-meeting-android/app/src/main/assets/captain_home_v114.html`: V7 통제관방, V4 선장방, V4 연락관방 결과후 카드 추가. 7방 × 5행 × 6번호 슬롯 및 점수 칩 총 35행. 헤더에 **V7·V4 3방 ↓** 버튼 추가, 전체 7방 세로 스크롤. 기존 4방·결과전 자료 원본 미삭제. `captainCumulativeFetchAfter`가 3앱의 결과후 메타를 독립 조회하도록 수정.
- `cumulative_after_only.js`: `roomIds` 7개, V3/V7/V4 각각 결과후 메타 검증. 3앱 중 일부 fetch 실패 시 실패 앱만 '서버 접속 불가 · 원본 미확인', 다른 앱 결과는 유지. V4 1·2차를 메타가 명확히 구분하지 못하면 각 5조합에 임의 배정하지 않고 '1·2차 미분류' 표시. 원본 6번호 미수신 상태는 **—/6**, 적중 0/6으로 대체하지 않음. V7·V4 버튼 실제 스크롤 동작.
- `cumulative_after_only.css`: 7방 모두 2열 4행으로 배치, 카드 그리드 내부 스크롤, 3개 추가 카드 색상, 화면 320~412px 대응.
- `MainActivity.java` app_version=124, `app/build.gradle` versionCode 124/versionName `1.0.24-CAPTAIN-SEVEN-ROOMS-AFTER-ONLY`, 기존 앱 package `kr.hanghae.crewmeeting.compact` 유지.
- `.github/workflows/build-crew-meeting-apk.yml`: 7카드·35결과후 행·3앱 조회·V4 독립방·동일 서명·DB1 1247건·17 공식번호 검증. 최초 CI 40649304356은 검증용 aapt 버전코드가 123으로 남아 실패했으나 APK assembleDebug 자체 성공. CI 검증 문자열 124 수정 후 재빌드 성공.

## 실제 빌드·검증
- **GitHub Actions 40649512896 SUCCESS** https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40649512896
- 아티팩트 ID **12799096729**, 이름 `HANGHAE-CREW-MEETING-1.0.24-SEVEN-ROOM-AFTER-ONLY-APK`.
- APK `HANGHAE_CREW_MEETING_1.0.24_SEVEN_ROOM_AFTER_ONLY.apk` **6,495,358 bytes**, SHA256 **9c895021422ba72791a2dceadec66e9676546daf3b968e4b0e84a2791a42b6f5**; ZIP 내 sha256 명세와 일치, CRC PASS.
- Android package 기존 `kr.hanghae.crewmeeting.compact`, versionCode 124, 동일 signing certificate SHA256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` 검증 성공.
- APK 내부 실제 HTML/CSS/JS/17 공식번호를 추출해 Chromium/Playwright **320×600, 360×700, 390×844, 412×915** 테스트: 각 7방 이름 모두 존재, 35행 존재, 그리드 스크롤 가능, **V7·V4 3방 ↓** 클릭 시 누락 3방으로 이동, 다음 회차 2회차 이동, 메인 화면 overflow 0, JS 오류 0.
- 모의 API: V3 메타 1건, V7 `Failed to fetch`, V4 앱 메타 1건(1·2차 미분류) → V3 정상 상태 유지, V7만 접속 불가 표시, V4 두 방은 미분류, 35개 점수 모두 `—/6`(허위 생성 0건) PASS.
- 미인증 테스트에서 loginArea가 보이는 것은 기존 접근키 게이트 정책. 실제 인증 상태에 대응하여 테스트에서는 `materialsAuthGate.hidden=true`로 확인했고 V7+V4 3방이 화면 내 정상 노출됨.
- Google Drive ZIP 업로드/읽기 재확인: https://drive.google.com/file/d/19x8OF6l2Dx4jOpFOeKXYWtcuEpHv7ZLk/view?usp=drivesdk

## HOLD / 다음 작업
1. **실제 V3/V7/V4 결과후 35개 6번호 원본 수신 연결은 미완료.** 현재 35칸은 읽기 전용 자리이며 점수 계산 근거 없음. 서버 메타 API `raw_predictions_included:false`인 동안 채점 금지.
2. 실제 선장 Android 휴대전화 설치·로그인·터치 동작 미검증; Chromium 시험과 서명 APK 빌드만 확인.
3. 오프라인 공식번호 검증 17회차(1~16,1247), DB-1 원문 1~1247 별도 보존. 미확인 공식번호를 채워 넣지 않음.
4. 기존 V3/V7/V4 원본 앱, 잠금, 결과전, 이전 APK, 안정판 URL/서버/DB 변경·삭제 없음.
5. 다음: 7방별 결과후 원본 인증 송신·조회, V4 독립 1·2차 출처 구분, 실제 6번호×5와 당첨번호 비교/적중점수 계산, Android 실기기 동일서명 업데이트 확인.
