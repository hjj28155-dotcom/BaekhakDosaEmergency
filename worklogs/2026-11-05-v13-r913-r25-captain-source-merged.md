# V13 선장 전용 R9.13 + R25 승인 홈 병합 검증 — 2026-11-05

## 범위
- V13 선장 전용 앱만 대상. V3 및 V7 소스/원문/DB/기존 설치판 변경 없음.
- Drive 원본: V13_R913_STAGE4_RAW_ROUND_NAV_STAGING_20261104.zip, SHA256 `20906efa11cc68eb0ae1c4db8e99523aa85fb972f2ba270d2fca7312f4d2c280` 확인.
- 325개 ZIP 엔트리 중 기존 `app/src/main/assets/index.html` 1개만 변경하고 검증기록 1개 추가. 나머지 297개 기존 파일은 SHA/bytes 동일.
- R9.13 소스의 기존 엔진, DB-1, Java, versionCode 31421, AndroidManifest, 연구실 원본, 분석 기록 화면과 Native 업데이트 기능은 변경하지 않음.
- R24 기반 R25 전체 HTML로 덮지 않고, R9.13 홈 메뉴 요소만 이식함.

## 적용 내용
- 중앙 금빛 `📜 원문 분석` 버튼 추가 → 기존 `pick(0)`.
- 좌측 중복 원문 분석 제외, 1~300회차 복구 연구실 메뉴 링크 숨김(파일 미삭제), 항행 자동 A/B는 R9.13 좌측 메뉴에 없음.
- 유지 메뉴 9개: 선장방, 신서방(구 도사방), 항해사방, 항행의자유방, 성적 관리, 누적 DB, 원문 채팅방, 작업 기록, 설정.
- 좌측 메뉴는 상단 ☰를 누르면 홈 범선 배경 전체 폭 위로 나타나는 오버레이. 닫으면 숨김.
- CSS는 **실제 메인 HTML head**에 삽입. 인쇄용 템플릿 문자열에 잘못 넣었던 중간 시안은 버리고 수정·재시험.

## 현재 생성 산출물
- `V13_R913_R25_선장전용_홈메뉴_통합소스_20261105.zip`
- SHA256 `6d56776f1dbd82960fc64e0c5cd1eea59816c37ffed499faf55c7b338733f5be`, 21,064,377 bytes.
- HTML SHA256 `87693b272ccb5c3eb4d625724837b2de6bdabb831c21dd33f6769b84ee9f5622`.
- ZIP CRC PASS, JS inline 2/2 Node syntax PASS.
- 실제 Chromium에서 프로젝트 HTML + 보존 JS를 인메모리 실행. 360×780와 920×1400에서 초기 메뉴 숨김, 메뉴 표시, 9개 버튼, 복구연구실 노출 제외, 배경 폭 유지, 메뉴 닫기, 중앙 원문 분석 화면 진입, 브라우저 오류 없음: **20/20 PASS**.
- Chrome file URL/가짜 도메인 접근 차단을 우회하기 위해 Playwright `page.set_content` 와 원본 zip JS 인라인을 사용. Android 실기기 WebView 테스트는 아님.
- 캡처 파일: `V13_R913_R25_홈화면_360.png`, `V13_R913_R25_메뉴열림_360.png` 외 두 920px 시안.

## 아직 미완료
- 동일 서명 인증서로 Android APK 빌드/서명: 미완료. 설치 APK 자체는 만들지 않음.
- 선장폰 기존 R8.72 (31380) → 새 versionCode 31421 이상의 동일 서명 APK 덮어쓰기 호환 및 실제 DB 유지 확인: 미완료.
- 현재 작업 컨테이너에는 Android SDK/Gradle/apksigner가 없어 APK 빌드가 불가.
- Google Drive에 수정 ZIP 신규 업로드: 완료. 아래 신규 파일 메타데이터 재조회 확인. 원본 Drive ZIP은 변경 없음.
- 선장 V13 R8.72에는 전체 HTML 원격 교체 API가 없으므로, 기존 앱에 R25 원격 HTML 등록만 해서는 업데이트 정보가 표시되지 않는 문제가 해결되지 않음.

## 다음 작업
동일 서명키 인증서 일치 확인 → Android 빌드 환경 구성 → 새 APK 서명 및 설치 전 검증 → 선장폰 데이터 백업 확인 → 기존 앱 삭제 없이 업데이트 설치 → 실제 동작 검증.

## 추가: Google Drive 신규 보관 완료
- 선장 전용 V13 폴더에 수정 ZIP 신규 업로드 성공. 원본 ZIP 덮어쓰기 없음.
- 새 Drive 파일 ID `1qtOQfrwAMQTKeCQze7AyWEOPLnbde8oU`
- Google Drive 링크: https://drive.google.com/file/d/1qtOQfrwAMQTKeCQze7AyWEOPLnbde8oU/view?usp=drivesdk
- 업로드 후 metadata 재조회: `size=21064377`, zip MIME 및 부모 `1KKF32iMa7l5ulap_NKjmtmZv9AfqnKGi` 확인.
- 원본 소스 ZIP SHA256 `20906efa...` 보존, 수정 ZIP 로컬 SHA256 `6d56776f1dbd82960fc64e0c5cd1eea59816c37ffed499faf55c7b338733f5be`. Drive 재다운로드 해시 검증 완료.

- Drive 업로드본을 다시 내려받아 SHA256 `6d56776f1dbd82960fc64e0c5cd1eea59816c37ffed499faf55c7b338733f5be` 일치, ZIP 326 엔트리 및 CRC PASS 확인. **실제 Drive 저장본 검증 완료**.
