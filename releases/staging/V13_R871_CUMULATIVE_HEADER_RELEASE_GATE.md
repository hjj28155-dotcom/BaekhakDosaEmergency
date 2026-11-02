# V13 R8.71 누적 DB 화면 갱신 — 업데이트 시험 대기

- 수정 소스 ZIP: https://drive.google.com/file/d/1Vb3WORHeS1FSLY7itXB0LaC-2jdM2bbY/view
- 393px 검증 화면: https://drive.google.com/file/d/1ghV-M5royv4x00xbBDRKe-wpuopfsPov/view
- 검증 리포트: https://drive.google.com/file/d/1tT-v19DmBCgsHCs_AipLZ_GOqPXuB_Bw/view
- 설치본 V13 R8.70 (31378) → 후보 R8.71 (31379), 원래 앱 ID 유지.
- 누적 DB 바깥 머리글 `누적 DB / 제101회차 적용·... / 큰 X` 숨김, 안쪽 `⚓ 누적 DB · 성공률` 우측 작은 X 배치.
- X 클릭은 기존 이벤트 처리로 홈 복귀. 다른 메뉴의 머리글 변경 없음.
- 393px, 768px 화면 시험 합격: 성공률 버튼 7개, 각 방 최종회차 카드 6개, 분포 선택 7개, 적중 8개 모두 유지. JS 오류 없음.
- ZIP SHA256: 85d78dfc39d5e3368b7fd5974aadb071df0fbecc1d6813b6d6f7286f37e91933.
- 기존 엔진 DB/성적 기록과 Java 업데이트 코드는 그대로. 초기화나 덮어쓰기 없음.
- **현재 실제 R8.71 APK가 없음.** 해당 설치본의 업데이트 버튼은 원격 HTML을 바꾸지 않고 더 높은 버전코드 APK URL만 내려받습니다. 따라서 main/releases/v13-release.json 미변경, 시험 브랜치만 등록.
- 실제 배포 단계: 동일 패키지+동일 인증서로 R8.71 APK 생성 → APK 실파일 Drive 업로드 → sha256/버전/서명 확인 → main 배포정보 변경 → 선장 기기 홈 [업데이트]에서 수동 다운로드·설치 → 기록보존 확인.
