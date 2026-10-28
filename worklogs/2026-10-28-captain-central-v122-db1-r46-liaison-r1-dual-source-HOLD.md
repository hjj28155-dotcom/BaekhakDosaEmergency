# ⚓ 중앙통제실 V1.0.22 — DB-1 실제 APK 탑재 / V4 선장엔진 R46·연락관엔진 R1 분리

## 선장 지시
"어직도 DB가 탑재 안되어 있잖아요. 그리고 V4 하나는 선장엔진분석5조합. 또 하나는 연락관엔진 분석5조합으로 만드세요."

## 기준 및 원본
- 선장 전용 중앙통제실: `feature/crew-meeting-standalone-apk-20261025`, 별도 패키지 `kr.hanghae.crewmeeting.compact`.
- **원본 V3 R46 선장엔진**: Google Drive `V3_R46_선장엔진원문근거확장_소스검증인계_20261028.zip`, 엔진 `source_overlay/assets/engine_v2.html` SHA256 `b40c756387dd56e38b0e4119b96da7903d53fe556acceb2184309b9b33d17f46`.
- **원본 V7 연락관 R1 블라인드 엔진**: Google Drive `V7_중앙연락관_신서_블라인드엔진_R1_20261026.zip`, 모듈 `liaison-blind-engine.mjs` + `v7-engine100-baseline.mjs`; V3·도사 사전 원본의 독립 보관 증빙이 없으면 조합 생성 거부하는 원본 게이트 보존.
- DB-1: Supabase `public.v6_round_sources` 제1~1247회 1,247건, 회차당 여섯 문장과 개별 SHA256.

## 실제 수정
1. `crew-meeting-android/app/src/main/assets/db1_rounds_1_1247.json`: DB-1 1,247건 실제 JSON 자산으로 포함; 서버 API 접속 없이 앱에서 읽음. DB 원본 쓰기 없음.
2. `MainActivity.java`: 앱 시작 시 1,247건, 연속 회차, 6문장, 1,247개 원문 SHA-256 전체 검증 후 화면 로드. 고정 HTTPS 경로의 WebView 리소스 요청을 APK 내부 자산으로 전달. 서브프레임 내부 엔진 이동은 외부 브라우저로 넘기지 않음.
3. `crew-meeting-android/patches/captain_r46.patch.gz.b64`: 원본 V4 엔진 베이스 SHA256 `5519b83b9a31095a70db8efee91b502a4ce65aa02ba9c0b9727575a2f16504d9`에 R46 패치를 적용해 원본 V3 R46 SHA256 일치 검증.
4. `crew-meeting-android/research-engines/`: R1 독립 연락관 연구 모듈 및 E100-R1 기준 모듈. 기존 V7 운영 서버·DB를 수정하지 않음.
5. `captain_home_v114.html`: V4 한 화면에 **선장엔진 R46 5조합** / **연락관엔진 R1 5조합** 분리, 원문 SHA·6문장 확인, DB 탑재 상태, 엔진별 성공/보류 상태, 초기화, 자료실 버튼.
6. 분석 결과는 현재 화면 메모리에만 표시, 기존 잠금 보고서나 누적 DB에 쓰지 않음. 누적 연구자료는 자료실 전용.
7. `versionCode 122`, `versionName 1.0.22-CAPTAIN-DB1-DUAL-ENGINE-STATUS-TEST`.

## 검증
- DB-1: APK V1.0.21 중간 빌드에서 1,247건/1,247건 SHA-256 일치, 1~1247 전회차 확인.
- R46: 실제 원본 엔진에 1회 원문 주입 → `packGate=PASS`, 검증된 5조합. 2회 원문 주입 → `packGate=PASS`, R46 근거확장 후보 4·1·42 감사 증거와 5조합.
- 브라우저 UI V1.0.21: DB-1 1,247건 탑재 표시, 회차1·2 원문 분석 5조합, 초기화(회차1·빈칸) PASS, JS 오류 0.
- R1 원본 모듈 테스트: 15/15 PASS. 별도 앱 번들 모듈도 독립 원본증빙 부재 시 `MISSING_TRUSTED_ARCHIVE_REGISTRY` 차단 PASS.
- **중요: 연락관 R1은 V3·도사 사전 원본의 독립 보관 증빙이 아직 없어 5조합 생성 보류가 정상이다.** 두 번째 5조합이 실제 생성됐다고 주장 금지.
- V1.0.22 최종 APK CI/실기기 검증 결과는 별도 기록할 것.

## 남은 작업
1. V1.0.22 최종 동일서명 APK 빌드·APK 내 자산·해시 검증 및 선장 폰 설치 검증.
2. V3·도사 사전 원본의 독립 SHA-256/보관시점 증빙을 확보하고 연락관 R1 5조합 실제 실행 검증. **DB-1 단독으로는 R1 원본 게이트 통과 불가**.
3. V4 실제 결과전 보고서·자료실 보관(과거 재분석과 공식 사전잠금 구분) 및 결과후 채점 연동.
4. V3·V7·V4 독립 원본 앱/배포판은 변경하지 않음.
