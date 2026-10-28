# ⚓ 중앙통제실 선장전용 V1.0.23 — 통합 누적DB 결과후 전용 4방 UI

## 선장 지시 / 기준
2026-10-28: “연락관님은 통합 누적DB방에 결과후만 보여주시고 이 화면처럼 해 주세요.”
선장 첨부 화면: 누적DB 회차 제목, 도사방·항해사방·자동1방·자동2방 4개 카드, 각 5줄, 당첨/보너스 색상 및 조합별 적중 수, 이전·다음 회차 조작.
원본 V3 R48 결과전/결과후 데이터는 별도 로컬 IndexedDB와 전송대기열에 보존되어 있으며 중앙통제실 서버 자동송신·영수증 미검증. 임의 번호를 결과후로 생성하지 않는다.

## 코드 변경
- 대상: GitHub `hjj28155-dotcom/BaekhakDosaEmergency` branch `feature/crew-meeting-standalone-apk-20261025`. 기존 V3/V7/V4 원본 앱·APK·서버·DB 및 안정판 다운로드 주소 변경 없음.
- `crew-meeting-android/app/src/main/assets/captain_home_v114.html`: 전용 `cumulativeAfterDashboard` 추가. 통합 누적DB 진입을 기존 7카드 결과후/결과전 자료실 대신 **결과후 전용 4방**으로 연결. V4 “자료실” 버튼에서도 선택 회차를 유지하여 진입. 원본 결과전·기존 7방 결과후·누적 자료는 삭제하지 않고 다른 기존 경로에 보존.
- `cumulative_after_only.css`: 첨부 참고 화면과 유사한 진남색·금색 테두리, 2×2 카드, 4방 색상, 5행·6개 번호 슬롯, 0~6 적중 칩, 공식번호·보너스 강조, 이전/다음/조회. 화면 높이 600~915px 반응형.
- `cumulative_after_only.js`: **결과후 읽기 전용** `crew-three-app-materials?app=v3&stage=after` 계약 사용. 현 API는 메타만 제공(`raw_predictions_included:false`)하므로 결과후 조합/점수는 허위 생성하지 않고 `—/6`과 “원본 미연결”을 표시. V3 각 방 원본 결과후 실제 수신 전에는 0/6이나 적중률을 생성하지 않음.
- `official_draws_verified_17.json`: `public.v7_official_draws`에서 확인된 **1~16회차, 1247회차만** 오프라인 포함. 미확인 다른 회차는 “공식 당첨번호 미등록”으로 표시. DB-1 1,247회차 원문과 공식 결과후를 혼동하지 않음.
- `MainActivity.java`: 위 세 파일을 APK 내부 WebView 자산으로 서빙. 기존 DB-1 1,247건 SHA-256 검증 및 선장 R46 / 연락관 R1 엔진 유지.
- `app/build.gradle`: `versionCode 123`, `versionName 1.0.23-CAPTAIN-CUMULATIVE-AFTER-ONLY`.
- `.github/workflows/build-crew-meeting-apk.yml`: 4방/20칸·17개 공식번호·기존 원본·동일 서명 검증. 최초 CI 40645566767은 `grep -c`의 줄수/일치수 혼동으로 실패, `grep -o | wc -l` 수정 후 성공.

## 실제 검증
- GitHub Actions **40645712624 SUCCESS**: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40645712624
- 아티팩트 ID **12797383622**, 이름 `HANGHAE-CREW-MEETING-1.0.23-CUMULATIVE-AFTER-ONLY-APK`.
- 동일 서명 인증서 SHA256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` CI PASS.
- APK **6,493,906 bytes**, SHA256 **475c5a64eb0cf665dded4fe595d73fecca1307de6fee4783b5919ad2e5ff8f0d**; ZIP 내 SHA256 명세와 일치, ZIP CRC PASS.
- APK 실제 추출 자산: HTML 181454 bytes, 누적 CSS 5354 bytes, JS 6548 bytes, 공식번호 17건, DB-1 1247건, R46 원본 8730334 bytes, 연락관 R1.
- 실제 APK 추출 HTML+CSS+JS+17개 공식번호 JSON을 Chromium `page.set_content`로 인라인 자산 시험: **320×600 / 360×700 / 390×844 / 412×915** 모두 4방·20행, 결과후 전용 표시, 기존 결과전 숨김, 1회차 공식번호/보너스, 다음회차 2회, 미등록 100회차 차단, V4 3회차 자료실 연동, 메인/그리드 스크롤 넘침 0, JS 오류 0.
- 메타 응답 모의: V3 제1·2방 각 1건이 있어도 원본 5조합 미제공이면 점수 칸은 `—/6` 유지, **허위 점수 생성 0건** PASS.
- Google Drive 시험 ZIP 업로드·읽기 재확인: https://drive.google.com/file/d/1K2cL6UJmUho4dgHSVjXettIrG8ILwVPo/view?usp=drivesdk

## 미완료·주의
1. **V3 R48 원본 결과후 4방 × 5조합의 실제 자동전송/원본 서버 read-back은 미연결**. 이 상태에서는 조합 숫자와 적중 점수 빈칸이 정상. 화면 배치 완료를 결과후 데이터 연동 완료로 주장 금지.
2. 공식번호 오프라인 보유 17건, 나머지 1230건 공식번호 데이터 확인·연결 미완료.
3. 실제 선장 Android 휴대전화 설치·로그인·화면 동작은 미검증; Chromium 화면 및 CI APK 빌드 검증만 완료.
4. 연락관 R1 독립 V3·도사 사전보관 증빙 미확보로 실제 5조합 생성 HOLD (V1.0.22와 동일).
5. 기존 원본 결과전/결과후·잠금·자료실·공식 배포 APK 및 URL은 변경·삭제하지 않음.

## 다음 작업
V3 R48 실제 결과후 내보내기 또는 인증된 서버 수신 원본 4방×5조합 확보 → 원본 출처/회차/6번호/공식번호 일치 검증 → 결과후 화면 20행 적중표시 실데이터 연동 → 실기기 동일서명 업데이트 검증.
