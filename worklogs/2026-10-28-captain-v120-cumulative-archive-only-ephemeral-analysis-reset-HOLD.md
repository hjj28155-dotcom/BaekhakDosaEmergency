# ⚓ 중앙통제실 V1.0.20 — 누적자료 자료실 전용 보존 / 분석 임시 상태 초기화

## 선장 명령
"누적자료는 자료실에만 두시고 초기화하세요."

## 보존 우선 해석
- 누적자료는 자료실에서만 열람하고, 새 분석방의 과거 분석 결과·회차 임시 상태는 초기화.
- **절대 금지**: 서버 DB 원본 삭제, 잠금 보고서 덮어쓰기, 기존 V3/V7/V4 배포 앱 초기화, 승인·서명·구매 기록 변경.
- 중앙통제실 전용 UI의 임시 상태만 초기화하며 독립 원본 앱의 내부 분석 캐시 초기화는 범위 밖.

## 실제 변경
- HTML `crew-meeting-android/app/src/main/assets/captain_home_v114.html`
- V3 분석방의 누적 350회 안내 제거, 누적 연구 이력은 자료실 문구에만 보존.
- 홈의 V3 과거 누적 350회 상태 문구를 새 분석 시작 상태로 교체.
- V4 화면: `초기화` 버튼 추가; V4 앱 열기, 홈으로, 자료실 이동 시 **회차 1 + 5개×2 빈칸 + 대기 상태**로 초기화.
- V4 `누적DB` 표시 버튼을 `자료실`로 변경. 이전 분석 회차를 누적 조회 회차에 자동 이월하지 않음. 자료실 누적DB 단계로만 이동.
- V4 자료실 이동 시 결과후 7개 대시보드 오버레이를 종료한 후 누적DB 조회 단계로 전환하는 버그 수정.
- 결과후 7개 화면 및 결과전 화면은 보존. 서버 기록은 읽기 전용.
- V4 실제 5+5 엔진 및 DB-1 원문 읽기 **미연결**. 임의 번호 생성 없음.

## 기술 버전
- `versionCode 120`, `versionName 1.0.20-CAPTAIN-ARCHIVE-ONLY-RESET-TEST`.
- 브랜치 `feature/crew-meeting-standalone-apk-20261025`.
- 최종 UI commit `c7ee317776b04ef54947cd21739f6a4056f057f9`.
- 최종 CI commit `d650c4c1d46bd5240f44f47cec65130bd49af583`.
- 빌드 https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40641972166 — **completed success**. 동일 서명·패키지·versionCode=120 CI 검증.
- GitHub artifact ID `12795738463`, ZIP 67,994 bytes. ZIP 및 APK CRC 통과.
- APK 71,173 bytes, SHA256 `7e2a586c7aabba4edd0285ba4e46170a5bd53763e3c460aa184b85781f9c0eab`, artifact .sha256와 일치.
- 실제 APK 내 `assets/captain_home_v114.html` 추출: 결과후 7개 카드, V4 초기화, 누적자료 자료실 전용 문구, V4 자료실 화면 전환, 누적DB 자료실 탭 확인.
- Google Drive 시험 ZIP https://drive.google.com/file/d/13-_XeKsZ-WVg_wtSDGjk-tCgWkrajPzn/view?usp=drivesdk — 업로드 성공.
- 직접 APK `/mnt/data/항행의자유_중앙통제실_선장전용_V1.0.20_누적자료_자료실보존_초기화.apk`.
- DB 변경 없음: 사후 조회 시 DB-1 1,247건, 누적DB 214건, V7 문서 32건 그대로.
- **선장 실제 휴대전화 설치 및 UI 검증 대기**.

## 남은 작업
1. 실제 휴대전화 V1.0.20 UI 및 자료실 이동 검증.
2. 독립 V3/V7 앱 엔진 상태 초기화는 기존 앱 원본과 선장 지시 범위를 확인한 뒤 별도 진행.
3. DB-1 제1~1247 원문 분석 입력 실제 연결 및 V4 독립 원문 5조합×2 엔진.
4. 7개 결과후 보고서 원본 데이터 자동 연결, 검증된 실적만 표시.
