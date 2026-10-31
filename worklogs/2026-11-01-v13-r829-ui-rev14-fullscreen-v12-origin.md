# ⚓ V13 R8.29 원격 수정 14회차 / V12 원본 엔진 추적 (2026-11-01 KST)

## 선장 지시
- 성적관리와 누적 DB 메뉴를 누르면 화면 상단부터 전체 너비·전체 높이로 펼쳐 표시.
- 선장 지적: 독립 엔진 원본은 V12에 존재. V13만 검색하지 말고 V12 보존 소스를 찾아 연결.

## 완료
- 새 HTML releases/ui/v13-ui-r14.html (blob f53f542b28de22f78c85808c790fa6137a799746)
- SHA256 5bc8ed1a3905c86f3e863cc9b3f47315d151717f1354eaa7f30676d5929bc967
- manifest releases/v13-release.json revision 14로 갱신. APK 주소와 버전코드 불변.
- 화면 선택 view 3 (누적 DB), view 6 (성적관리)에만 body.v13FullDataView 클래스를 적용.
- 패널을 fixed, top 0, 100vw, 100dvh, 독립 스크롤로 표시. X 닫기 시 정상 홈/메뉴 복귀. 다른 방의 레이아웃·원본 데이터 미수정.

## V12 원본 경로 정보
- 종전 V12→V13 작업 인계서가 지목한 V12 보존 원본: app/src/main/assets/v3_original_r62/original_v12_r62_unmodified.html
- 실행/엔진 폴더: app/src/main/assets/v3_original_r62/index.html, engines/*.js, raw_bank.js, 이미지
- 보존 연구 폴더: research_V12_original_preserved, research_Freedom_V33_R22_preserved, research_R2_original
- 이 경로들을 현재 BaekhakDosaEmergency main에서 직접 조회했으나 NOT_FOUND. 이는 다른 브랜치/백업/보존 APK에 있을 가능성이 있으므로 원본 부재라고 단정하지 않음.
- 현 V13은 V12 보존 V3 R33 연구 엔진을 일부 참조하지만, 1249회 정식 각방 독자 엔진 이관 완료 증거는 없음.

## 검증
- GitHub revision 14 등록 및 HTML 파일 저장 확인. 실제 선장 휴대전화 풀스크린 적용은 미검증.
- JS 전체 구문/브라우저 viewport 실측 미검증.
- 저장된 DB, 결과전 원본, 승인 잠금, APK, V3/V7 관련 파일 삭제/교체 없음.

## 다음 작업 1순위
- V12 백업/브랜치/실제 APK assets에서 원본과 정본 엔진 해시 확보 후 4방 결과전 5+5+15+5 연결 및 실기기 테스트.
