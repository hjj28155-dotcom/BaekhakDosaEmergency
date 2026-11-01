# V13 원문분석 전체 엔진·V12 항행의자유 최상단 펼침 검증 — 2026-11-01

## 선장 지시
1. V13 원문분석 실행 한 번으로 선장·도사·항행의자유·항해사 네 엔진이 각각 조합 생성.
2. V12 항행의자유 원본앱은 V13 항행의자유방을 열자마자 화면 최상단에 전체 펼침.

## 확인한 현재 런타임
- 최신 공개 원격 UI revision 15의 runOriginalRawAnalysis()는 DB-1 원문 6문장 확인→publishDb1ToLocalRooms(round)→runConnectedPredraw(round) 호출.
- runConnectedPredraw는 predrawEngines 등록 목록을 모두 병렬 호출(Promise.allSettled)하고, 성공 보고서를 importBeforeReport로 수집.
- 현재 등록은 보존 연구 도사·항해사 어댑터 위주이며 V12 선장/도사/항행 원본 세 엔진이 모두 V13 registerEngine 계약으로 연결된 것이 아님.
- V12 R9 원본 index.html에는 V13이 임의로 postMessage해 실행할 수 있는 공식 메시지 리스너가 발견되지 않음. 원본 iframe 추가만으로 15조합 자동 생성 연결 완료라고 말할 수 없음.
- 연구 항해사 V1을 공식 predraw 수집기에 등록하면 immutable 슬롯 충돌 위험이 있으므로 격리된 safe staging 파일 releases/staging/v13-ui-r15-navigator-v1-isolated-research-safe.html 유지.

## 이번에 만든 미배포 Android 전체 프로젝트
V13_V12_항행의자유_최상단전체화면_4엔진연동진단_미배포_20261101.zip
- 기반: 기존 V13 R8.15 / V12 항행의자유 원본 전체 67개 자산 보존 / 항해사 독립 연구 V1 포함.
- V13 메인 index.html에 view===9일 때 body.v13V12FreedomFullscreen 활성화. stage를 top:0/full viewport 고정, V12 원본 iframe을 가용 높이 전체로 확장하고 기존 화면/DB/엔진 원본은 보존.
- 압축 CRC PASS, 전체 206개 항목, SHA256 cf36f1dbf96ac9a3284a27ad6405fa5581548cd693bf8d712a539e8c3644c714.
- 이 검증본은 최신 원격15회차 UI 통합 및 APK 빌드/휴대전화 사용 검증 전. 실기기 전체화면 성공 주장 금지.

## 남은 작업
V12 세 엔진 정본의 별도 호출·응답 인터페이스 추출, 5+5+15 조합 출처/잠금 검증, 항해사 신규 5조합 실제 실행과 연구 출력 분리, 공식 30조합 입출력, 최신 rev15 통합·APK 빌드/서명/기기 시험. 기존 DB·결과전·승인 기록을 삭제하거나 재기록하지 않음. GitHub 출시 manifest revision15 유지.
