# ⚓ N2 R6.21.39 V1 공식 업데이트 등록 작업대장 (2026-11-11)

선장 지시: "링크에 올여주세요" → N2 R6.21.39 기존 앱의 V1 업데이트 링크 정식 등록.

## 운영 상태
- 패키지: com.hanghae.fold8n2app (N2 전용, 별도 R6 com.hanghae.fold8media 미변경)
- 이전 6.21.35 / 621935 → 신규 6.21.39 / 621939
- 운영 update.json: main:r6-n2-standalone/update.json
- 운영 설정 변경 커밋: bebda41892b5ae00c69852cb0628f1618f6eceee
- APK 배포 브랜치: n2-r62139-publish-20261111
- APK: r6-n2-standalone/publish-621939/N2_R62139_CANDIDATE10_GAMES5_PNG_SIGNED.apk
- Git blob hash 454fdfa67356926da8e5d2e7a046fc1ed0d8e7ff — 로컬 APK git hash-object와 일치, GitHub 등록 후 재조회 통과
- APK SHA-256 38dff0c9b26d0f302242a9c082952c43ea11a43b7c8cb1b3646b5f115dda580c
- bytes 2282523
- 기존 N2 소유자 서명 인증서 SHA-256 81d75da8b4e1c82fbe8af27a0f8512236f2b1c2cdef5d7d015f695fb98893d04
- 공식 Android apksigner 검증 PASS (사전 빌드 단계)
- main update.json에 신규 version_code, package_name, apk_url, apk_sha256 적용 후 재조회 일치 PASS

## 포함 수정
- 제1250·1251회차 분석 상한 오류 수정.
- 각 방 원본 예상수 정확히 10개 선정 → 그 번호로만 5조합 생성.
- 조합 전 후보수 별도 기록과 4방·성적관리·PNG 최상단 표시.
- 기존 기록에 없는 후보수 사후 추정 금지.
- 기존 데이터/DB와 별도 R6, Lotto645, V3, V7 변경 없음.

## 미확인
- 선장 휴대전화 팝업·다운로드·덮어쓰기·1250회 분석·각 방 10예상수/5조합·PNG 실제 생성·기록 재실행 확인은 아직 선장 보고 전.
- 실패 시 기존 앱 삭제/데이터 삭제 금지. 테스트에서 실제 오류와 화면 증상을 수집해 다음 수정.
- 실기기 확인 후 100회차 성능분석 시작.
