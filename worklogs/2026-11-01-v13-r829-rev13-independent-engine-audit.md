# ⚓ V13 R8.29 — 13회차 다음 작업: 독립 엔진 출처 감사 (2026-11-01 KST)

## 대상 및 보존
- 현재 원격 배포 revision 13, HTML releases/ui/v13-ui-r13.html
- DB·기존 결과전·APK·승인 기록·V3/V7 별개 원칙 유지.
- 정식 30조합 생성이 검증되기 전까지 공식 등록 금지.

## 실제 소스 검사
- 현 V13 코드의 window.V13_RESULT_BEFORE.registerEngine 호출은 dosa, navigator 두 개뿐.
- captain과 freedom의 실제 독립 엔진 등록 호출 없음.
- dosa 연구어댑터: V2.4-RC, navigator 연구어댑터: V3 R33. 두 어댑터 모두 1247회차를 넘으면 오류 발생하도록 작성됨.
- 항행의자유 연구 PNG 구성은 NAV_R19_RESEARCH, historical round 1~1247, 결과전 공식 등록 부적합.
- 최근 UI rev13은 수신된 freedom 15조합 원본을 우선 PNG로 그리는 분기만 복구. 이 코드는 원본 생성 엔진 자체가 아님.
- 원격 HTML에서 참조한 외부 스크립트 상대 경로:
  v13_independent_engine_contract.js;
  v12_v3_engine/dosa_v2_4_rc_original.js;
  v12_v3_engine/nav_v33_r5_safe.js;
  v12_v3_engine/nav_v3_r33_four_rooms_research.js;
  freedom_v33_engine/nav_v33_r19_three_origins_two_auto.js.
- 위 상대 경로는 GitHub releases/ui 폴더와 루트 조회 시 확인되지 않았음. Android APK 내 웹 자산으로 제공될 수 있으므로 파일 미존재만으로 실기기 로딩 실패 확정은 불가.
- BaekhakCrownAndroid, baekhak-analysis-api 저장소 코드검색에서도 이 엔진 파일 이름 확인 불가.

## 판정
- 현재 소스만으로는 최신 회차의 captain 5 + dosa 5 + freedom 15 + navigator 5를 정식 독립 엔진 결과로 생성 불가능.
- 13회차 배포 변경으로 이 문제가 해결됐다고 표현하면 안 됨.
- 실제 원본/실행 권한/검증 해시 없이 연구용 1247 이전 조합을 최신 회차로 우회 재등록 금지.

## 검증 범위
- GitHub 연결을 이용해 manifest, HTML blob, 엔진 등록 호출 및 연구 유효 범위를 직접 확인.
- 실제 Android APK 자산·서명·실기기 런타임 호출, 신규 회차 30조합 생성은 확인하지 못함.
- 이번 작업에서 UI revision, APK, DB, 결과전 원본 수정/삭제 없음.

## 다음 우선 단계
1. V13 설치 APK 또는 빌드 자산에서 5개 script 파일 및 V3/도사 정본 엔진을 확보하여 파일 해시/버전 비교.
2. captain 및 freedom 독립 어댑터의 신뢰할 수 있는 실제 원본 출력 5/15조합을 연결.
3. 현재 최신 회차에 대한 사전 잠금·원문 지문·출처·조합 수·번호 유효성 검증.
4. 안드로이드 실기기에서 4방 각각 PNG 저장 후 30조합 성적관리 통합 시험.
