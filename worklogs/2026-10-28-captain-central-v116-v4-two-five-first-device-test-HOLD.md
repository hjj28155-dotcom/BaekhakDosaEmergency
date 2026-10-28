# ⚓ 중앙통제실 V1.0.16 — V4 원문 5조합 × 2 분석방 1차 시험 APK (2026-10-28)

## 선장 지시
- "5조합 2개로 만드세요." / "1차로 현재까지 진행된 중앙통제실에 올리세요." / "앱 올리세요..검증해야 겠어요"
- 선장 전용 중앙통제실만 업데이트하고 일반 V4 배포판, 기존 고정 Drive 정식 링크, 구매잠금/승인 및 원본 데이터를 보존.
- V4 독립 원문 분석 **제1분석 5조합 + 제2분석 5조합 = 총 10조합**. 두 번째 5조합의 실제 엔진 생성과 중복 검증은 후속 구현. 무근거 번호 생성 금지.

## 소스 변경
- 브랜치: `feature/crew-meeting-standalone-apk-20261025`
- V4 화면 추가 커밋 `5d0bdb7da4c7e37a45375d61e71ce53bf666bcfd`: `crew-meeting-android/app/src/main/assets/captain_home_v114.html`에 V4 분석방 열기, 회차 입력, 5조합 2개 버튼, V4 보고서 조회, 미연결 시 거짓 분석 방지 상태문구.
- 버전 상승 커밋 `0ce1c943f62b01e8c872562cb1fdab1705387950`: `versionCode=116`, `versionName=1.0.16-CAPTAIN-V4-ROOM-TEST`.
- Android 번들 완전성 검사 커밋 `b58a5e4f815aebc01c5b5a1405fd1c292800e265`: V4 5조합×2 필수 DOM 검증, app_version=116.
- CI 검증 강화 커밋 `e883df5df0d435e1303dc98e72d07c52e036a033`: 두 분석 버튼/상태 문구, 기존 V3 4방·승인관리 보존, 패키지/서명 고정.

## 빌드·검증 (실제 확인)
- GitHub Actions: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40632147559 — **completed success**.
- Artifact ID `12791589603`, 이름 `HANGHAE-CREW-MEETING-1.0.16-V4ROOM-TEST-APK`.
- 앱 패키지 `kr.hanghae.crewmeeting.compact`, `versionCode=116`, 기존 인증서 SHA-256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` 동일. 키가 없으면 중단하는 CI 유지.
- APK `HANGHAE_CREW_MEETING_1.0.16_V4ROOM_TEST.apk`: **66,741 bytes**, SHA-256 `4cd838af3d94f51e11ebdbb5feff9e95c918053483d96623bca5152ed6045561`.
- ZIP artifact **63,562 bytes**. ZIP CRC 정상, APK 내부 ZIP CRC 정상, 내부 HTML에 `freedomAnalysisPanel`, `freedomFirstFive`, `freedomSecondFive`, `freedomOpenRecords`, `homeV3RoomsPanel`, `homeApprovalBtn` 존재 확인. 아티팩트 SHA256 체크파일과 실제 APK 일치.
- Drive 시험본 (APK+SHA256 ZIP): https://drive.google.com/file/d/1DOnkkV5_wPN5F9Ck8KZp09396ajaxrEr/view?usp=drivesdk — 업로드 성공 후 Drive raw fetch로 **63,562 bytes** 재조회.
- 대화 직접 APK: `/mnt/data/항행의자유_중앙통제실_선장전용_V1.0.16_V4분석방_1차검증.apk`.

## HOLD / 다음 단계
1. 선장 휴대전화에서 V1.0.16 업데이트 설치·홈·V3 4방·V7·V4 분석방·승인관리·보고서 조회 **실기기 검증 미완료**.
2. **V4 실제 엔진 분석 연결 미완료**. 현재 버튼은 연결 대기 안내만 표시하며 원문 5조합 × 2를 실제 계산하지 않음. 두 엔진 호출·입력·결과 provenance, 중복검사, 연구용 결과전 저장, 자동송신 후속.
3. V3/V7/V4 결과전·결과후 원본 자동송신 및 공식 추첨 전 잠금검증 미완료.
4. 기존 V1.0.13 정식 고정 Drive 링크, V1.0.14/V1.0.15 시험본, 일반 배포판 및 데이터 **변경 없음**.
