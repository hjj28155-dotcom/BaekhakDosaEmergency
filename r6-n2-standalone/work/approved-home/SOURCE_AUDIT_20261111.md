# N2 R6.21.46 실제 홈 화면 연결을 위한 소스 감사 — 2026-11-11

## 소스 확인 결과
- GitHub `main:r6-n2-standalone/update.json`: versionName 6.21.46, code 621946; 기존 V1식 수동 업데이트 채널 유지.
- GitHub 문서 `N2_R62146_V1_IN_APP_UPDATE_ACTIVATED_20261111.md`: APK Git blob SHA `804677e6ba8134a096d340b3a38e45c4859e0d8d`, APK sha256 `9206dba864861277df414e5b6fa44e478af68d6349b95c6ac8c731a78f4b3785`. 문서에는 실제 단말 검증 미완료.
- Drive 확인: `N2_R62134_136회차저장보완_index.html` (file id `1VqaWC3ZhvOgmqbJXSPpzaT8wZdu81yD6`, 약 4 MB), R6.21.34 시대의 읽을 수 있는 옛 원본 존재.
- 옛 HTML의 실제 `#app`, `pageCanvas`, `R614_SCORE.step(1)` 및 분석연구 기록 `NAVIGATOR_R64_LOCAL_RESEARCH_ARCHIVE` 발견. 이것이 **현재 6.21.46의 실제 DOM/핸들러와 같다는 증거는 없음**.
- Drive 파일명 검색에서 `R62144`, `R62145`, `R62146` 최신 index.html 파일은 발견되지 않음.
- 앱 APK URL의 외부 HTTP 재다운로드는 현재 실행 환경 DNS 차단으로 불가. APK 압축 내부 `assets/index.html` 추출은 따라서 미완료.

## 개발 제한
- 과거 R6.21.34 HTML을 현재 운영 R6.21.46 원본으로 치환하지 말 것: 현재 APK에서 변경된 분석 및 저장 코드를 잃을 수 있음.
- 승인 이미지는 금빛 바다/선박/조타륜 및 이전회차 | 0000 | 다음회차, 저장 회차, 전체방, 6메뉴, 종료.
- `0000`은 입력 자리표시자이며 실제 분석 요청에서는 거부. 실제 저장 회차는 기존 데이터 조회만 사용.
- 브랜치 `work/n2-approved-home-controls-20261111`의 `approved-home-controls.mjs`와 테스트 파일은 별도 스테이징이며 APK에 연결되지 않음.
- 운영 `update.json`, APK, DB, 서명키, 발표기능 모두 변경하지 않음.

## 다음 수행 작업
1. 이미 GitHub에 게시된 6.21.46 APK의 내부 `assets/index.html` 실물 추출(또는 같은 SHA/버전의 개발 원본 확보).
2. 실제 홈 DOM과 기존 분석/저장/프레젠테이션 이벤트 이름을 대조.
3. 승인 디자인 적용 + 스테이징된 회차 입력/이전다음/전체방 기능 연결; 기존 기능 회귀검사.
4. 동일 패키지·서명 인증서 유지된 APK에 한해 시험용 별도 빌드, 선장폰 보존 검증 후 배포. 임의 자동승격 금지.
