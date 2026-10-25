# ⚓ 항행의자유 공동회의 — 항해 작업대장 / 2026-10-25

## 버전·구분
- 공동회의 **별도 설치판 1.0.3-FLAGSHIP**, Android applicationId `kr.hanghae.crewmeeting.compact`, versionCode 103.
- 화면 공식 표시명: **항행의자유 공동회의**.
- V3 항해사앱, V7 중앙통제실, 기존 공동회의 1.0.0의 패키지·서명·원본 데이터 변경 없음.
- 기 설치 1.0.2-COMPACT과 같은 패키지 및 서명 인증서로 빌드되어 1.0.2 → 1.0.3 업데이트 구조. 공동회의 1.0.0은 **다른 서명**이므로 덮어쓰기 불가, 보존.
- APK 경로: `crew-meeting-android/releases/HANGHAE_CREW_MEETING_1.0.3.apk`
- 최종 SHA256: `db76a68075da7482b126fe982fed455b83e7025b624c093ab7715440fd691a7e`
- 공개 서명 인증서 SHA256: `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be`
- CI 성공: GitHub Actions #40169130307. 앱의 비공개 서명키를 APK 안에 넣지 않음. GitHub 캐시 의존 상태이며 영구 암호화 개인키 복구 사본은 미확보.

## 화면 및 앱 변경
- `crew-meeting.html` main: 휴대폰 전체 높이 대응, 내부 대화 스크롤, 접히는 원본 출처 정보, 하단 선장 지시·전송.
- `crew-meeting.html` main: 접히는 “📎 회의 파일 주고받기” 영역과 파일 선택·업로드·목록·받기 버튼.
- Launcher: `res/drawable/ic_crew_flagship.xml`, 황금 나침반·항공모함·파도 형상의 작은 화면용 벡터 디자인(선장이 승인한 콘셉트의 단순화). AndroidManifest label 업데이트.
- Page UI source commit `6e3cce83bc098f5adeccd05dab9b7ce6b815dc19`. Pages deployment run #40168816446 **success**.
- 앱 1.0.3 서명 메타데이터 정합성 수정 commit `712ef4b9e74ac461062dd8939bc6790a181809b8`.

## 파일 교환 시험 기능
- Supabase project `jyqkohpeowkhuaohlezc`; Edge Function `crew-meeting` **version 3 ACTIVE**.
- Edge code snapshot (secrets excluded): `crew-meeting-android/server/crew-meeting-edge-v3-index.ts`; previous v2 exact backup: `crew-meeting-android/server-backups/crew-meeting-edge-v2-index.ts`.
- 비공개 Storage bucket `crew-meeting-private` (public=false, max 10MiB).
- RLS 차단된 새 메타데이터 테이블 `public.crew_meeting_files`; `anon` 및 `authenticated` 테이블 접근 권한 제거.
- 기존 입장키로 인증한 파일 목록/업로드/짧은 수명의 서명 URL 발급. 파일 SHA256 기록, 폴더 분리 및 URL 상대방 접근 제한.
- 업로드 가능한 파일 종류: PDF/TXT/MD/JSON/CSV/TSV/JS/MJS/TS/HTML/CSS/PNG/JPG/WEBP/ZIP/APK. **개별 최대 10MiB**. 원본 APK가 이를 넘으면 교환 불가하며 별도 업로드 방식을 개발해야 함.
- 기존 V3/V7 자료실과 누적DB에는 **쓰기 없음**. 기존 두 ChatGPT 원본 세션 및 V3/V7 엔진도 **미연결**.
- 현재 서버의 파일 메타데이터 레코드 조회 결과 **0개**. UI JavaScript 구문 검사 통과, 앱 build/signer / artifact checksum 검증 통과.

## 미완료 및 선장폰 점검
1. 선장폰 실제 설치 및 작은 화면에서 버튼과 파일 접기/펼치기 확인 **미실시**.
2. 선장폰에서 파일 선택→비공개 업로드→목록→브라우저 다운로드의 종단간 실사용 검사 **미실시**. 완료로 보고하지 말 것.
3. 원본 AI 항해사·통제관 세션은 공유 채팅에 실제 참여하지 않음. AI API 역할의 서버 응답일 뿐.
4. 10MiB 초과하는 V3/V7 원본 APK·ZIP은 현재 파일 기능으로 교환하지 못함. 직접-Storage 대용량 업로드 및 권한 강화 필요.
5. 기존 공동회의 1.0.0 개인키 복구 미완료. 현재 1.0.3 키 영구 외부 백업도 미확인.
6. 작업대장·파일 자료를 ChatGPT 프로젝트의 **고정 파일 목록**에 자동 업로드하는 권한은 없음. 연결 자료의 정본 보존과 사용자 파일 추가 기능을 혼동 금지.

## 사용
- `https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/crew-meeting.html` 에서도 접힌 파일 메뉴 확인 가능.
- APK 1.0.3은 새 독립 앱 `kr.hanghae.crewmeeting.compact`이며 기존 1.0.2-COMPACT 설치 시 동일 서명 업데이트 가능.
- 앱 삭제, 등록 데이터 초기화, 개인키 무단 교체, V3/V7 운영 DB 변경 금지.

## 2026-10-25 추가 · 중앙연결관 원본 대화방 버튼 / 모바일 다운로드 개선
- 선장 지시: 홈화면의 **중앙연결관 원본채팅방 버튼 누락** 및 **다운로드 클릭 시 표시 없음**.
- 대응: `crew-meeting.html` 본문 상단에 「🔗 중앙연결관 원본채팅방」, 「주소 등록/변경」을 추가. 원본 ChatGPT 대화 링크는 사용자만 확인할 수 있어 **최초 1회 정확한 원본 URL**을 저장하고 그 이후 브라우저에서 엶. 원본 방 새 생성/자동연결이 아님.
- 링크 등록값은 그 기기의 `localStorage`에만 저장, 외부 서버에 전송하지 않음. 입력값은 HTTPS ChatGPT URL로 제한함.
- UI JavaScript 구문검사 통과. 원본 ChatGPT 방 실시간 3인 연결 기능 아님.
- 공개 APK 설치 페이지: `https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/crew-meeting-releases/`
- APK 공개 GitHub main 경로: `crew-meeting-releases/HANGHAE_CREW_MEETING_1.0.3.apk`; Git Blob `9ccf38b1cdc1c493311805cdc0e5ece47a63b01b`, SHA-256 `db76a68075da7482b126fe982fed455b83e7025b624c093ab7715440fd691a7e`.
- 공개 설치 페이지 GitHub Pages 배포 run `40170472219` success. 추가 원본 버튼 반영 main commit `68cfa2364f758485716fc61223695514a3b7e285` 후 Pages 최신 배포 확인 필요.
- APK 다시 빌드할 필요 없이 원격 웹화면 업데이트로 1.0.3에서 버튼 표시. 단, 실제 선장폰 다운로드/설치/버튼/외부 채팅 열기는 미검증.


## 2026-10-25 추가 · 실제 원본 항해사·통제관 직접 대화 연결 (준비/부분완료)
- 선장 재강조: `새 AI API 역할`은 기존 원본 항해사 및 통제관 채팅이 아님. 방을 옮겨도 **원래 ChatGPT 대화에 실제로 다시 들어가 이야기할 수 있어야 함**. 기존 원본 삭제/복제/초기화 금지.
- 공식 ChatGPT 기능 확인: 2026-07-09부터 새 그룹 채팅 생성/참여 또는 기존 개인 채팅의 그룹 전환 제공 중단. 외부 APK에서 ChatGPT 기존 개인 채팅에 메시지를 API로 전송하는 공식 연결 기능 없음.
- 확보된 원본 V3 주소 출처: `V5_이사인계_기존원문_20261015_복사본.txt` 등 사용자 라이브러리의 2026-10-15 인계 기록. 정확한 `/g/g-p-.../c/...` URL을 확보하여 개인 비공개 서버 레지스트리에 등록. **공개 소스코드 안에 개인 URL을 넣지 않음**.
- 원본 V7 통제관 `/c/` URL은 확인되지 않아 **주소 없이 연결 성공을 표시하지 않음**. 주소 등록 UI만 표시.
- 중앙연결관 기존 로컬 링크 저장값 `crew_meeting_central_linkofficer_original_chat_url_v1` 유지.
- 새 DB: `public.crew_meeting_original_links` with RLS, direct anon/authenticated read grants revoked, role-specific URLs. 기 등록값: navigator 1건, controller 0건, linkofficer 0건.
- Edge Function `crew-meeting` **v4 ACTIVE**; 기존 `GET original-links` and `POST original-link` authorization uses room's bearer token. Strict `chatgpt.com` original `/c/<uuid>` or `/g/<id>/c/<uuid>` only; `/share/` excluded. Source snapshot: `crew-meeting-android/server/crew-meeting-edge-v4-index.ts`, commit `9485f9adc9a1978fe457ac5018d836d59d5bf90c`.
- Website `crew-meeting.html` main, commit `ddcd3132fa82f80cdd3a613ae46bf16bd6323685`: three buttons original V3 / original V7 / central link officer. Authenticated original link lookup after login; clicking ready link leaves pilot WebView for original ChatGPT conversation. Existing AI pilot meeting remains visually identified as separate, and **no shared live conversation with three original ChatGPT sessions**.
- HTML JavaScript syntax verified. V3 URL registry and RLS grant status independently read back. GitHub Pages deployment run #40172711562 to verify success.
- **Unverified**: device access to original ChatGPT conversation with user's authenticated account, new buttons on actual Android phone, V7 exact original address and linking, full end-to-end authenticated API tests.
- Next step: Verify GitHub Pages deployment; captain can tap "원본 항해사" in 1.0.3 after login. For V7, obtain its actual ChatGPT conversation URL, one-time register in app. Original chats remain separate; do not imply automatic shared-message forwarding.


### 2026-10-25 정정 · V5 옛 항해사 URL은 V3 현재 원본으로 확정 불가
- 과거 라이브러리 인계자료 `V5_이사인계_기존원문_20261015_복사본.txt`에서 찾은 항해사 URL은 **2026-10-15 V5 원본 기록**임. 선장이 2026-10-24까지 사용한 V3 원본 `기존항해사 - 북극성 - 7316` 대화와 동일하다는 근거 없음.
- 잘못된 원본 연결을 막기 위해 해당 URL은 **비공개 DB 참고기록으로 보존**하되, 앱 자동 원본방 버튼의 활성 링크 목록에서 제외.
- 수정 Edge Function **v5 ACTIVE**, `GET original-links`는 선장이 직접 등록한 `added_by=captain_registered`만 표시; `crew-meeting-android/server/crew-meeting-edge-v5-index.ts`, commit `6d6ae555b4bd2187c93fc91bf6560e3b9371f0e9`.
- 통제관 `통제관 - 푸른등대`와 V3 항해사 `기존항해사 - 북극성 - 7316`의 정확한 채팅 `/c/` URL은 아직 미확보. 선장이 ChatGPT 원본 대화방에서 브라우저 **주소창 URL**을 복사하여 한 번만 등록해야 해당 버튼이 활성화됨. `/share/` 주소를 사용하면 대화 재개 불가.
- 공개 GitHub Pages 원본 3버튼 웹 UI run #40172711562 `success` 확인. 앱 새 빌드는 필요 없으나 선장폰 실사용 미검증.
- 외부 Android APK는 원본 ChatGPT 개인 세션에 메시지를 자동 주고받는 API를 제공받지 않음. 세 원본 ChatGPT 세션의 실시간 회의 연결은 **불가**; 원본 방을 직접 열어 선장이 대화하는 방식만 제공.


### 2026-10-25 / 반복 입력 입장키 문제 · 1.0.3 웹 화면 무설치 업데이트
- 문제: 공동회의 웹 UI가 `sessionStorage['crew_meeting_auth']`만 사용하여 앱/탭 종료 뒤 선장 입장키 재입력을 요구했음.
- 선장 명령: **앱 업데이트**로 한 번 인증한 기기에서 자동 입장되게 수정. 기존 APK/V3/V7/DB 변경 금지.
- 수정: `crew-meeting.html` main commit `aeda388ec309b9b3839ff22030889e7827ab2e4a`; 체크된 「이 기기에서 다음부터 자동 입장」 옵션을 통해 성공한 입장키를 `localStorage['crew_meeting_persistent_auth_v1']`에 보관. `sessionStorage`도 유지. 앱 재실행 시 서버 `GET /messages` 인증검증 후 자동 입장.
- 「퇴장」 누르면 로컬 지속·임시 입장 저장 모두 해제. 서버 401에서만 무효키 정리; 일시적 네트워크 오류는 키 유지. 원본 ChatGPT 방은 별도 직접 링크이며 API 원본방 자동연동 아님.
- 키는 해당 동일 앱·브라우저의 로컬 저장소에 남으므로 개인 기기에서만 '자동 입장'을 권장. 공용 기기에서 선택 해제 및 퇴장 권장.
- 동일 원격 HTML 로딩 방식이므로 공동회의 1.0.3 **APK 재설치 없이 갱신**. V3/V7 앱 영향 없음.
- JavaScript 구문검사 성공. GitHub Pages deployment run #40181116415 검사 중; **실제 선장폰 저장 지속성은 미검증**.

- 후속 배포검증: GitHub Pages Actions #40181116415 **completed/success**, head commit `aeda388ec309b9b3839ff22030889e7827ab2e4a`. 웹 앱 코드 배포 성공. 다만 Android 선장폰에서 실제 키 저장·재진입은 미검증. 같은 앱 내부 저장소에서 동작하며 Chrome과 WebView의 저장소는 공유되지 않을 수 있음.
