# V13 성적관리 30조합·당첨번호 연동 미배포 수정 — 2026-11-01

현재 공개 버전: V13 R8.29 / 원격 UI revision15. 기존 배포 정보 유지.

- 원문분석 실행 호출 구조 확인: runOriginalRawAnalysis -> publishDb1ToLocalRooms -> runConnectedPredraw -> 등록된 각 엔진을 병렬 호출. 네 엔진 모두 실제 등록된 것은 아님.
- 성적관리 view6에 기존 유효한 방별 저장 보고서 기반 30조합 행 표 추가. 각 방 수신 조합과 번호를 개별 표시하고, 미수신방은 미수신으로 표시함. 원본 번호를 임의 생성하지 않음.
- 추첨 후 공식 당첨번호 source='DHLOTTERY_2026' + verifiedOfficial=true 조건에서만 등수 계산·결과후 PNG·축하 문구가 나타나게 scoreAfterDraw에 게이트 추가. 공개 참고번호는 채점 금지.
- 신규 항해사 독립 V1 연구 API는 존재하되 정식 result-before 수집에 등록하지 않는 격리 후보 유지. 연구 출력이 기존 잠금 원장을 차지하는 오류 방지.
- 전체화면과 누적 DB 기존 rev15 내용 유지.
- 후보: releases/staging/v13-ui-r15-score-30-navigator-safe-candidate.html (GitHub blob e141fd2bfe5f498e70aa48c999ec45cb4dc714b0).
- GitHub 재조회 정적 체크: {"revision":15,"scoreTable":true,"officialGate":true,"readOnlyLedger":true,"fullscreen":true,"navigatorResearchIsolated":true,"countContract":true}
- 실제 네 엔진 출력·V12 항행의자유 원본 전체화면 최상단 실기기·APK 빌드·실기기 표시 테스트 미실시. 원격 rev15를 교체하지 않았음.
- V12 전체 프로젝트 자산의 원본 앱 이식은 별도 R8.15 기반 APK 소스 후보로 보존 중; 이 HTML 후보에는 최신 V12 앱 전체 바이너리/원본 자산이 자동 동봉된 것이 아님.

다음: V12 원본 3엔진의 공식 실행 인터페이스와 항해사 별도 엔진을 30조합 규약에 실제 연결, 회차 1249 RAW6 정확성 검증, 브라우저 및 WebView 동작 검증, 최종 APK 빌드·서명·기기 검증 후에만 공개 전환.
