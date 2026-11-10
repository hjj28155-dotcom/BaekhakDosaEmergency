# ⚓ N2 R6.21.41 — 4방 동일 예상수 원인 수정 / 시험판 / 2026-11-11

## 사용자 실기기 신고
선장님: “4방 모두 예상수가 똑같습니다.” 직전 N2 6.21.39는 10개 후보수 → 5조합 규칙은 구현했지만 네 방의 예상수가 실질적으로 같아지는 문제가 있었음.

## 검증된 근본 원인
원래 6.21.39 통합 엔진 `rank(profile)`은 네 방의 공통 `scores[n]`에 서로 다른 양의 상수를 곱해 정렬했고, 검증된 과거회차 이월 기록이 없으면 순위가 그대로 유지됨. 방별 프로필은 이름뿐이며 실제 후보수 상위 10개가 같아질 수 있음.

## 작업 내역
- 6.21.40의 화면 버전표시/네이티브 설치버전 진단 수정을 기반으로 6.21.41 제작.
- 각 방의 별도 점수기준: 원문사전 / 이전회차 이월·인접 / 과거 희소성·빈도 교차 / 후보수 분포 다양화. 앞선 방과 완전히 같은 후보 풀을 만들지 않고 방 간 겹침을 최대 8개로 억제.
- 각 방에서 조합 전 후보 10개 별도 확정 후 그 번호만으로 5조합, 원본 후보 필드/PNG 표시 기능 보존.
- 과거 이력이 없을 경우 도사·교차방은 'NO_VERIFIED_HISTORY_EXPLORATORY' 태그로 해당 후보가 실제 과거 데이터 근거가 아닌 탐색용임을 표시.
- 엔진 점수 구분 및 후보 다양성은 통계적 성능 개선의 검증 결과가 아니며 100회차 A/B 테스트 전 당첨 성능 주장 금지.

## 검증
- JavaScript 33개 구문 검사 통과.
- Chromium 엔진 직접실행 100/1249/1250/1251회차: 각 4방 10개 후보 집합이 서로 다름; 각 방 5조합(총20조합), 후보수 밖 번호 0건, 최대 방 간 겹침 8개; 페이지 JS 예외 없음.
- Chromium NativeUpdate.status 모의: 버전 6.21.41, 코드 621941 표시 및 수동 체크 동작 PASS.
- APK package `com.hanghae.fold8n2app`; versionName `6.21.41`, code `621941`; bytes 2,290,146.
- Signed APK SHA256 `437576032e5783b0ea9659bd1fde9c4c05a79c4285a49eb3b53ab0d8973e122e`.
- Official Android apksigner v2 PASS; N2 permanent cert SHA256 `81d75da8b4e1c82fbe8af27a0f8512236f2b1c2cdef5d7d015f695fb98893d04`.
- APK diff vs signed 6.21.40: AndroidManifest.xml and assets/index.html only.
- Drive signed owner-only pilot: https://drive.google.com/file/d/1_wKIM3OXcP5SkbMpYIscINTFiZxgzneZ/view
- Full Korean handover: https://drive.google.com/file/d/18Gjgu6H_rzIze1CgeDkCl4sDbx8DIFCC/view

## 운영 상태
**6.21.41 is NOT deployed.** Last live N2 updater `main:r6-n2-standalone/update.json` remains 6.21.39 until reverified. Device save/PNG/install not yet tested. No original R6, Lotto645, V3, V7, historical backup or saved data changed. Private owner signing secrets never committed. Next step is owner phone test and 100-round validation only after structure is confirmed.
