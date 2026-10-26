# ⚓ 중앙연결관 작업대장 — 항행의자유 배포용 승인관리방

- 기록일: 2026-10-26 (KST)
- 작업 담당: 중앙연결관
- 작업 대상: 공식 다섯 앱 연결 홈 / ④ 독립 중앙통제실 연결 홈 / ② 배포판 승인관리방
- 저장소: `hjj28155-dotcom/BaekhakDosaEmergency`
- 브랜치: `main`
- 기존 앱/APK/승인기록/DB/서버 코드: 변경 없음

## 변경사항
1. `hanghae-distribution-approval-room.html` 신규 생성. ② 배포판의 승인요청 목록, 선장 인증 후 승인/거절, 개별 요청 토큰 상태확인 UI.
2. `hanghae-central-control.html`에 전용 승인관리방 진입 버튼 연결.
3. `index.html`에 다섯 앱 카드 외부에 독립 '중앙업무방' 진입 버튼 연결. 기존 5개 카드와 이동아이콘 최상단 왼쪽 위치 유지.
4. 기존 Supabase Edge Function `approval-admin` (v12) 및 `approval-status` (v11) 호출. 승인 결정은 관리 인증키와 사용자 확인 필요. 인증키는 소스/URL/localStorage에 저장하지 않음.

## 검증 및 근거
- GitHub 소스 수정 커밋: 신규 방 `1fec7b6948523c11dc33db6ea2923e92cd8ddf21`; 중앙통제실 홈 `f5975298efe93b54ce08992c515818dd22dcf923`; 통합 연결 홈 `de99390ed33894b1c531c3327b248b4232d5ea45`.
- 새 페이지의 스크립트 구문 검사: 통과.
- 정적 10항목 검증: 기존 카드 5개 유지 / 새 방 링크 2곳 연결 / 수동 승인확인 / 무인증 자동승인 없음 / 토큰 미영구저장 / 서버 서비스 키 미포함 / 7단계 문구 보존 등 전부 통과.
- GitHub Actions 'Verify five independent app links and Freedom seven-stage flow': 2026-10-26 새 홈 커밋의 실행 `40324830744` 성공.
- GitHub Pages 배포 실행 `40324827799`: 마지막 조회 당시 in_progress. 완료 확인 전까지 실배포를 단정하지 않음.
- 서버 로그인·신청목록 조회·승인/거절을 실제 관리키로 시험하지 않았으며, DB에 시험 승인/거절을 쓰지 않았음.
- Android 설치/실기기 진입 및 앱 자체 홈 반영은 미검증.
- 관련 없는 기존 Android 빌드 자동화 `build-v1084-mp4.yml`, `build-v1082-test.yml`이 해당 push 후 실패 표시되어 원인 감별은 별도 필요.

## 보안 점검
- 관리 화면은 공개 정적 HTML이지만 신청자 목록 및 승인 처리는 별도 기존 서버의 `x-admin-token` 검사 후에만 수행.
- `public.v7_jotasu_transfer_queue`의 RLS 비활성 보안 경고를 확인했으나 이 방의 승인 테이블과 별도이며 운영 DB 권한/정책을 무단 변경하지 않음.
- 기존 승인요청 `approval_requests` RLS 활성 확인.

## 다음 작업 1순위
1. GitHub Pages 배포 작업 성공 여부 재조회.
2. 선장 실기기에서 '공식 연결 홈 → 배포용 승인관리방' 및 '④ 독립 중앙통제실 홈 → 방' 진입 확인.
3. 관리 인증키로 '조회' 및 한 건의 승인/거절을 선장과 함께 검증하되, 운영 승인 변경은 선장 지시에 따라서만 수행.
4. 필요 시 Android 독립 중앙통제실 APK 내부 홈에 별도 네이티브 버튼 추가 여부 확인. 이 작업은 현재 웹 연결 홈만 수정했음.

> 재시작 지시: 최신 main과 이 대장을 읽고 ② 배포판 전용 승인관리방 실기기 검증부터 이어가십시오. 기존 V3/V7·승인·DB·서명을 삭제하거나 초기화하지 마십시오.


## 정정: 실제 중앙통제실 앱 홈 화면에 승인관리방 연결 (선장 추가 지시)
- 선장 정정: 공식 웹 통합 선택 홈의 버튼만으로는 요구사항 충족 불가. 실제 중앙통제실 홈 화면 `crew-meeting.html` 에 방을 만들어야 함.
- 반영 커밋: `2a9c347787d19c7742d09afad9dd2b46c36cfb0c` (`main`).
- 변경 파일: `crew-meeting.html` 단일 파일, 기존 대화·입장·V3/V7·현회차 조회 기능 보존.
- 홈의 기존 세 앱 실행 카드 아래에 `🚢 배포용 승인관리방 · 승인요청 / 승인확인` 버튼 추가.
- 눌렀을 때 중앙통제실 홈 내부 `#homeApprovalPanel` 개설, 전용 승인관리방 `hanghae-distribution-approval-room.html` 을 내부 프레임에서 조회; 홈으로 복귀 가능, 독립 페이지 이동 대체 링크 제공.
- 정적 검사: 새 버튼·홈 패널·관리 iframe·수동 승인 흐름 등 8항목 통과. 기존 원본 3담당자 버튼, 4 주요 기능, V3/V7 앱 실행버튼, 배포판 카드 및 전회차 조회 보존 검증.
- 전체 기존 JS 문법 검사 `new Function`: 통과.
- GitHub Pages 배포 실행 `40325412894` 및 전용 회귀검사 `40325415351`는 마지막 확인 시 진행 중이었음. 배포 성공 여부와 휴대전화 APK의 원격 WebView 화면 표시 여부는 미검증.
- 기존 APK 재빌드·Drive 파일 변경·승인 DB 기록변경은 수행하지 않음.

**차기 시작:** GitHub Pages 확인 후 실제 선장폰에서 중앙통제실 홈 버튼 → 방 내부 승인화면 입장 확인. 실기기에서 반영되지 않으면 독립 중앙통제실 APK 1.0.7 소스와 동일 서명키를 확보한 뒤 안전 업데이트할 것.

## 최종 업데이트: 공동회의 1.0.8 APK 빌드·고정 Drive 업데이트 성공
- 선장 지시: 별도 링크가 아닌 중앙통제실 앱의 기존 구글 드라이브 고정 업데이트 주소를 이용한다.
- Drive의 신규 중앙통제실 개인서명 원본 `captain-central-release.p12` 실제 존재 및 PrivateKeyEntry 열람 검증: 성공. **단, 이 인증서는 `ef85c53d...`로 기존 공동회의 1.0.7 서명 `e24e9573...`와 달라 사용하지 않음.** 비공개키·암호 원문은 소스/보고서에 저장하지 않음.
- 기존 1.0.7 APK의 ZIP Signing Block v2 서명 인증서를 원본 실물에서 검사: `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be`.
- 원본 공동회의 CI 서명 캐시 `crew-meeting-independent-pilot-signing-v1`에서 동일 서명키 복원 검증 후 신규 빌드.
- GitHub 소스 브랜치: `feature/crew-meeting-standalone-apk-20261025`; 최신 빌드 커밋 `dc990a38bf1b6d8547a4fdc6325f440c21379718`.
- 변경: `crew-meeting-android/app/src/main/java/kr/hanghae/crewmeeting/MainActivity.java`에서 기존 WebView가 중앙통제실 홈 외부화했던 승인관리방 자식 프레임 주소를 내부 허용하며 홈 캐시쿼리 108로 변경. `app/build.gradle` 버전코드 108 / 버전명 1.0.8-APPROVAL. 서명 identity 공개정보 버전 갱신, 빌드 Workflow 버전 검증 갱신.
- GitHub Actions 성공: 실행 `40328701402`, artifact `12654180254` (HANGHAE-CREW-MEETING-1.0.8-APK).
- 파일: `항행의자유_중앙통제실_승인관리방_V1.0.8.apk`, 15,718바이트. package `kr.hanghae.crewmeeting.compact`, code 108. SHA256: `886ad415006031dd83e83a2d864cc3e8000364168a8876d7d4f66d1076ce6244`.
- APK 로컬 ZIP 무결성, `signing-identity.json` 버전, 실제 APK Signing Block v2 인증서 `e24e9573...`가 기존 1.0.7과 같음: 통과.
- 새 버전 별도 Drive 보존 링크: `https://drive.google.com/file/d/1j9fpvNwJQz9LwwPV5eLcsZZsF1JIuMUP/view`.
- **기존 고정 업데이트 Drive ID `1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL`를 동일 파일 ID 유지하면서 신규 APK bytes로 교체 성공.** Google Drive update_file `success=true`, 크기 15,718바이트 확인. 재다운로드한 1.0.8 APK의 SHA256 정확히 일치.
- 공식 독립 중앙통제실 안내 페이지와 고정 업데이트 안내 페이지의 버전/링크/체크섬도 1.0.8로 교정 (`hanghae-central-control.html`, `crew-meeting-releases/index.html`). 사용자가 실제 파일을 새로 내려받아 '업데이트 설치'하도록 안내.
- 현재 Drive APK 권한은 파일 소유자 전용. 선장 본인 다운로드 가능; 외부 사용자 공유는 미검증. 실제 선장 휴대전화의 APK 설치·새 승인방 표시/승인관리 기능 테스트는 **미완료**. 기존 승인요청 DB, V3/V7 APK 및 별도 ② 배포판을 수정하지 않았음.
- 유의: 해당 `captain-central-release.p12`는 이 공동회의 APK의 서명에 쓰지 않았으므로, 이를 공동회의 1.0.8 서명키라고 인계하면 안 됨.
