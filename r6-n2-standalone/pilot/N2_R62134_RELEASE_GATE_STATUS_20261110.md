# N2 R6.21.34 — 정식 배포 검증 게이트 (2026-11-10)

## 원칙
- V1 방식: 앱 실행 → 업데이트 팝업 → APK 다운로드 → **기존 앱 삭제 없이** 덮어쓰기 설치 → 누적DB 보존.
- 운영 `main/r6-n2-standalone/update.json`은 실기기 검증 완료 전 변경하지 않는다.
- 시험용은 `n2-r62134-pilot-20261110/r6-n2-standalone/pilot/update.json`을 사용한다.

## 확인된 서버 파일
- 시험 패키지: com.hanghae.fold8n2app / version_code 621934 / version_name 6.21.34.
- 시험 SHA-256 기대값: dae439ba238b55c4584ad0968f16843c0aaa29850015f177681f5ca2579fc121 (시험 JSON 기재값, 실물 재검증 전).
- 운영 버전: 6.21.29 / version_code 621929.
- 정적 서명 비교 스크립트: `r6-n2-standalone/pilot/verify-r62134-compatibility.sh`.
- 기본 브랜치 read-only Actions: `.github/workflows/n2-r62134-compatibility-gate.yml` (commit 5c7531f).
- 시험 브랜치 Actions: `.github/workflows/n2-r62134-pilot-compatibility.yml` (commit 2b90a37).

## 현재 미확인 항목 (미검증을 완료로 기록하지 않음)
1. Actions 성공 run ID, job 로그와 테스트 결과.
2. 실제 두 APK의 SHA-256 및 apksigner 인증서 지문 일치 여부.
3. 시험판의 기존 R6.21.29 덮어쓰기 설치 성공 여부.
4. 실기기 136회차 분석, 저장 및 앱 재실행 후 저장 유지.
5. 업데이트 전·후 기존 누적DB 항목 수 및 내용 일치.
6. 기존 앱 실행 시 업데이트 팝업/다운로드/설치 UX.

## 2026-11-10 추가 검증 시도
- GitHub 커넥터로 main 운영 JSON 직접 재조회: 6.21.29 유지.
- 커넥터의 commit workflow runs 조회: 반환 목록이 비어 있어 성공/실패 판정 불가 (해당 API는 일부 트리거 유형만 조회).
- 별도 실행 환경에서 GitHub raw APK 다운로드 시 DNS 접근 실패: APK 파일 실물 검사 수행 불가.
- 기존 앱, 서버 JSON, DB 수정 없음.

## 승격 조건
- [ ] 서명/버전/무결성 자동검사 **성공 로그** 확보
- [ ] 선장 소유 1대에서 기존 앱 삭제 없이 설치 성공
- [ ] 136회차 신규 분석·저장 성공 및 재실행 후 기록 확인
- [ ] 과거 누적 기록 보존 증거 확인
- [ ] 업데이트 팝업/직접 설치 동선 시험 성공

**위 체크 완료 전 운영 update.json 수정 금지.**

다음 1순위: GitHub Actions에서 `N2 R6.21.34 signed APK release gate (read-only)` 실행 결과 및 첨부 로그를 확인한다. 이후 실기기 덮어쓰기 검증을 진행한다.
