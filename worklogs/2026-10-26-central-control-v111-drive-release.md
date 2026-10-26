# 중앙연결관 — 중앙통제실 APK V1.0.11 구글 드라이브 배포 기록
일시: 2026-10-26 (KST)
담당: 중앙연결관
대상: ④ 독립 중앙통제실 공동회의 (V7 통제관과 별도 앱)

## 이번 수정
- 기존 원본 채팅방 3개(항해사·통제관·중앙연결관)를 보존하며 선장방을 더해 4개로 확장.
- 통합자료방: V7·V3 항해사앱·항해의자유 화면의 결과전/결과후/누적DB 및 1~1247회차 자료 **상태 조회** 화면 추가.
- V7 원본 서버 메타데이터 읽기전용. V3와 항해의자유 원본 자동 DB 연결은 미완료로 표시. 결과전 허위 소급 생성 및 생산 엔진 성능 승격 없음.
- 항해의자유 일반 사용자 최근 5회차 열람 제한은 기존 앱 설정 유지. 독립 배포판 APK 미변경.

## 빌드와 서명 검증
- GitHub 저장소: hjj28155-dotcom/BaekhakDosaEmergency
- APK 브랜치: feature/crew-meeting-standalone-apk-20261025
- 변경 커밋: 97d80ba8249f5b7d3e387e1264f397f99155ae12
- 빌드: GitHub Actions 40345026578 — SUCCESS
- 버전: 1.0.11-MATERIALS / versionCode 111
- Android package: kr.hanghae.crewmeeting.compact
- 기존 서명 인증서 SHA256: e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be (CI 실제 APK 검사 통과)
- 파일: 항행의자유_중앙통제실_4원본채팅_3앱통합자료방_V1.0.11.apk
- 바이트: 15,794
- SHA256: 6eadd9f423879e02cefa832b30df36c61a6e8975f60e46edabce1ea24dcbac71
- ZIP 테스트 및 SHA-256 검사 통과.

## 구글 드라이브 업데이트
- 먼저 개별 보존본 업로드: https://drive.google.com/file/d/1WUH5jxKJFnUK_2BMD2yLTJ5FLohXW9bp/view
- 기존 고정 업데이트 **파일 ID를 그대로 유지**하면서 내용만 새 APK로 갱신:
  https://drive.google.com/file/d/1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL/view
- Drive 파일 교체 성공, 새 파일 15,794바이트 재조회. 재다운로드한 APK 바이트·SHA256이 CI Artifact와 완전히 일치.
- 기존 1.0.10 APK 및 승인 기록/DB 삭제 없음. 기존 드라이브 공유설정 변경 없음(소유자 전용).

## 검증 미완료
- 선장폰 실제 1.0.11 설치 및 WebView에서 4개 원본 채팅방과 통합자료방 노출.
- V3/항해의자유 독립 원본 DB 실시간 연동 및 1~1247회차 3앱 전체 과거 분석.
- 실기기 상 배포판 최근 5회차 노출제한 회귀검사.
- 다음: 기기 업데이트 설치→4개 원본 링크 등록→자료방 항목 조회 검증→3개 독립 엔진 원본 연동 확대.

원칙: V3 항해사앱, V7 통제관, 항해의자유, ④ 독립 중앙통제실을 혼동하지 않는다.
