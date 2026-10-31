# V13 R8.29 UI revision 15 — cumulative ledger (2026-11-01 KST)

## 선장 지시 및 출처
V12 R24/R25 원본 소스 검증 이후 V13 30조합 결과전과 누적 성적 DB의 안전한 결합을 진행.

## 실제 변경
- 신규 원격 UI: releases/ui/v13-ui-r15.html (blob 73fdf1add6472ce55a8ac76939df81e58d084ea9)
- SHA256: e7a0cc928dea4bbadd762ed3d827a678d16a0227186baf07537d22b19bcbb648
- releases/v13-release.json revision 15, APK 31337 및 기존 다운로드 URL 유지.
- 누적 DB (view 3) 맨 위에 기존 V13의 localStorage 결과전(v13.predraw.real-room-reports.v1)과 추첨후(v13.score.draw-after.v1) 원장에 기반한 읽기 전용 회차별 집계 추가.
- 회차·실제 수신 조합수·방 수·원문/결과전 유효성·공식 번호 확인 여부·공식 확인 시 1~5등 조합수.
- 30조합 미수신 / 연구용 자료 / 검증 오류 / 공식 번호 미확인을 채점 결과와 구분.
- 누적 DB 기존 DB-1 원문 패널은 아래에 그대로 두고, rev14 전체화면과 X 복귀 유지.
- V12 성적 DB 원본을 V13 형식으로 **자동 이관하지 않음**. 기존 자료 및 승인 기록 수정 없음.

## 검증 수준
- GitHub blob 재조회로 신규 UI 함수와 view3 연결 부분 확인, manifest revision15 및 URL·SHA 등록 완료.
- Android 전화에서 실행/화면/성적 자료 진위 여부, 전체 JS 구문 컴파일 검사는 별도 미검증.
- 실제 선장/항행의자유 독립 엔진 연결 및 새 30조합 생성은 미완료.
- 현재 전화에 V13 저장 결과전/추첨후 기록이 없으면 누적현황은 기록 없음으로 표시되는 것이 정상. 다른 기기/V12 데이터를 삭제하지 않았음.

## 다음
- 실기기 revision15 적용 및 누적DB 기록 표 검사.
- V12/V3 MASTER 정본 방별 엔진 출처·1249 지원 범위 검증, 5+5+15+5 실제 결과전 결합.
- 검증된 후 성적 PNG 및 회차별 장기 보존 실기기 회귀 검사.
