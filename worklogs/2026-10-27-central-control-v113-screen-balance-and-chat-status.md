# ⚓ 항행의자유 중앙통제실 V1.0.13 — 화면 균형·원본방 보존 작업대장

- 기준: 2026-10-27 KST. 기존 V3, V33, V7, 항행의자유 일반 배포판, 독립 중앙통제실을 구분함.
- 선장 실기기 보고: 원본 링크를 누르면 다른 대화방으로 이동하거나 '이 ChatGPT 대화를 불러올 수 없습니다', 'Too many requests'가 나타났음. 이후 **선장이 직접 항해사·통제관·중앙연결관 세 방 모두 대화 가능하다고 보고**. 이 증상은 재현되지 않는 것으로 관리하되, 앱별 자동 링크 검사까지 완료했다는 의미는 아님.
- 금번 작업 범위: 선장 스크린샷 `39216.jpg`의 상단 검은 띠·하단 과도한 빈 배경 보완. 원본방 링크 저장값·서버 연결 로직·V3/V33/V7 엔진·기존 데이터는 변경하지 않음.

## 최신 변경
1. `assets/central-carrier-photo-r13.svg` 신규 추가. R12 사진 내장 JPEG를 재사용해 화면 보이는 구도를 1.5배 확대, 좌우 중앙 크롭, 아래쪽의 검은 빈 바다 대신 갑판·전투기·일몰이 노출되도록 이동. R12 기존 파일은 보존함. GitHub 변경 커밋 `8045b847cc46a035700c676af4c8e4e662ffbf9c`.
2. `crew-meeting.html` R13 배경 참조로 4곳 교체, JavaScript 구문 통과, 기존 원본방 4개·자료방·승인관리 등 DOM ID 유지. 커밋 `6a9a578e0a59a33fec3afc22a72c4be9db56d89e`.
3. `hanghae-distribution-approval-room.html` 배경 동일 적용. 커밋 `f2bce78e551377295409f27887139b1a1667eba2`.
4. Android `crew-meeting-android/app/src/main/java/kr/hanghae/crewmeeting/MainActivity.java`: Android 9+ `layoutInDisplayCutoutMode=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` 적용. Fullscreen의 상단 디스플레이 컷아웃 미사용 영역 보완 의도이며 **실기기 검증 전에는 상단 띠 해결 완료로 단정하지 않음**. 기존 WebView 외부 원본방 URL 라우터 코드는 그대로 둠.
5. 독립 APK 브랜치 `feature/crew-meeting-standalone-apk-20261025`: 커밋 `15182e4646c9b77f0602ee88176a3384823a1a90`. `versionCode=113`, `versionName=1.0.13-SCREEN`, 고정 패키지 `kr.hanghae.crewmeeting.compact`. 세 원본방과 V3/V7 독립앱 동작 코드 변경 없음.
6. GitHub Actions run `40366738349` **completed success**; 아티팩트 `12669773958`. 원본 APK 서명 SHA256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` 강제 확인, 빌드, 버전·패키지 확인, 파일 검증 통과.
7. APK `항행의자유_중앙통제실_화면균형_원본방보존_V1.0.13.apk`, **15,970 bytes**, SHA256 **`e9d37ff7b6caa9f9ad299e83dda6af8371dcd7d8cef1e8c0d555492a1cf89347`**. ZIP 체크섬 검사 PASS.
8. 독립 Drive 보존본: `https://drive.google.com/file/d/1sTcYg8sm98eQWnRIxuFD3R4aiILU59ml/view`. 최초 배경 R12 원본도 그대로 보존.
9. **고정 Drive 최신 설치 파일**: `https://drive.google.com/file/d/1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL/view` **동일 파일 ID 유지, 내용만 V1.0.13으로 교체 성공**. Drive 다시 받기 및 로컬 원본 APK 바이트 비교 PASS, 15,970 바이트, SHA256 정확히 일치. 공유설정 변경 없음(owner-only).
10. R13 별도 확대본 JPG 스냅샷 선장 Drive에 보존: `https://drive.google.com/file/d/1shH57_cfp4bjcyYk5Q_bYgUQ1OdcyxZu/view`, SHA256 `83d56ac432323affbffd11281a2a8be0484b881a3afc06ec69fec12b8d8a3d43`.
11. `hanghae-central-control.html`, `crew-meeting-releases/index.html` 설치 안내 V1.0.13·파일 크기·검증 해시로 갱신.

## 검증·위험·남은 작업
- 완료: GitHub HTML/JS 정적 검사, 원본 버튼 4개 존재 확인, 공식 APK 빌드와 서명·버전·ZIP 해시, 고정 Drive 재다운로드 동일성.
- 미완료: 선장폰에 V1.0.13 업데이트 실제 설치, 삼성 안드로이드 화면 상단 여백 및 리프레임 실표시, ChatGPT 세 역할 각각 중앙통제실 버튼에서 정확한 원본방으로 이동하는지 실기기 확인. 사용자 보고의 '세 방 모두 대화 가능'이 해당 버튼의 링크 정확성까지 보장하지 않음.
- 원본 서버 대화방 주소(DB `crew_meeting_original_links`) 변경 없음; 원본 연결 URL 이력도 건드리지 않음. 보관방·Plus/429 오류를 APK 수정 완료라 주장하지 않음.
- V3/V33/V7 엔진 실작동 업그레이드, 1~1247회차 전체 원본분석, 세 앱 원본DB 실제 연동, 항행의자유 일반판 최근 5회차 보안 검증은 이전 인계서 미완료 상태 유지.
- 다음 1순위: Pages 배포 최신 완료 상태 확인 → 선장폰 기존 중앙통제실 삭제 없이 V1.0.13 업데이트 설치 → 검은 띠 / 항공모함 위치 / 각 원본방 버튼 정확성 스크린샷 및 실검증. 실패 시 원본 R12·1.0.12 보존본으로 회귀 가능.
