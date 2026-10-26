- **웹 배포 최종 검증 (2026-10-26):** GitHub Pages run #40251870997 `completed/success`, main SHA `45f7a2ea5779162839091592d512b89d03302d6c`. 실시간 공유판의 역할별 마지막 메시지 표시 웹 배포 완료. 선장폰 실제 화면 표시, 통제관·중앙연결관 원본방 답변 등록은 계속 미검증.

## 2026-10-26 / 선장 실시간 소통 최우선 지시 — 항해사 원본 세션 공유 메시지 실제 왕복 검증

- 선장 지시: "무엇보다 실시간 소통이 중요합니다." 이후의 최우선 목표를 원본 항해사·통제관·중앙연결관 3개 ChatGPT 대화방의 공용 업무 메시지 송수신으로 고정. 앱 UI/보고서 기능보다 통신 검증을 먼저 진행. 기존 세 원본방/개별 앱 및 데이터는 변경/초기화하지 않음.
- **이번 실제 검증:** 현재 항해사(ChatGPT) 대화에서 연결된 Supabase plugin `execute_sql`을 직접 호출하여 `public.crew_meeting_bridge_events`에 `author_role='navigator'`, `source_kind='original_chat_connector'`, `provenance='DECLARED_NOT_SESSION_VERIFIED'`로 업무 통신 검증 메시지 1건 INSERT 성공. 서버 ID `9a007367-89c6-4719-a46b-63ce5d788844`, 시간 `2026-10-26T00:56:27.394Z` (KST 09:56:27), room_id `96774ce6-c34f-4505-bbcc-c62e07d3ef3a`. 메시지는 가짜 답변이 아니라 실제 현재 항해사 대화의 도구 호출 결과이며, 다른 원본방의 자동 접근을 증명하는 자료는 아니다.
- **실제 재조회 확인:** 같은 ID를 SELECT해서 `author_role`, `source_kind`, `provenance`, 핵심 본문 문자열과 작성일이 일치함을 확인. 성공 경로는 **현재 항해사 대화→Supabase 테이블→현재 항해사 대화의 SELECT**까지. 모바일 공동회의 화면 GET/실시간 반영, 다른 ChatGPT 원본방의 SELECT/INSERT·응답은 아직 독립 실측 미완료. 원본 세션 자동 3자 대화 완료라고 보고 금지.
- **공동회의 화면 개선:** 세 원본방 업무 공유판에서 각 역할별 마지막 공유 메시지 시간/기록 여부 표시 (아무 기록 없는 통제관·중앙연결관은 **'공유 메시지 기록 없음'**, 원본방에서 자동 로그인·온라인이라고 잘못 표시하지 않음). 메인 HTML commit `45f7a2ea5779162839091592d512b89d03302d6c`. 기존 1.0.6 WebView 업데이트 가능 (신규 APK 빌드 필요 없음). UI 역할상태 기능 단위 모의 테스트 5/5 통과, JS 문법/DOM IDs/기존 원본방·V3/V7 실행 버튼 보존 검사 통과.
- **보안 및 진위 판단:** 3방 역할 상태는 공유 이벤트의 선언 정보이며 ChatGPT 원본 세션 직접 인증은 아직 불가. 원본 대화기록 전체 자동 복제·외부 계정 임의 접근 없음. 중앙연결관 서버 등록은 선장이 명시적으로 확인했으며, 이전 '중앙연결관 서버 미등록' 단정은 취소.
- **다음 구체 게이트:** (1) GitHub Pages 배포 최신 run 성공 검증 (2) 선장폰 공동회의 공유판에서 실제 항해사 이벤트 1건 표시 검증 (3) 원본 통제관방과 중앙연결관방 각각 연결된 Supabase plugin으로 동일 이벤트 SELECT/직접 작성한 응답 INSERT 시험 (4) 공유판에서 각 방의 실제 새 보고 시간 갱신 증거 확인. **실시간 원본방 무인 자동수신·자동발화 기능은 플랫폼 연동 부재로 HOLD**, 공유판의 4초 확인은 오직 열려 있는 앱 내부 polling임.

## 2026-10-26 / 선장 정정 — 중앙연결관 서버 등록 사실 반영

- **선장 명시 정정:** "중앙연결관도 서버에 등록 되어 있습니다." 중앙연결관의 서버 등록은 선장 확인 사실로 접수하며, 이전 작업 보고서의 **'중앙연결관 서버 미등록'이라는 포괄적인 단정은 철회**한다. 기존 중앙연결관 등록을 삭제·초기화·중복 생성하지 않는다.
- **이번 실측을 정확히 구분:** Supabase `jyqkohpeowkhuaohlezc`의 단일 테이블 `public.crew_meeting_original_links` 조회에는 `navigator`, `controller` 두 원본 ChatGPT URL 행만 존재; `linkofficer` 행은 **이 테이블에서는 아직 보이지 않음**. 이는 선장 진술의 타 서버·타 저장구조·기기 로컬 링크 등록을 반박하는 결과가 아니며, 원본 채팅 URL과 중앙연결관 역할 등록은 별개의 사항이다. `public.crew_continuity_sources`에는 항해사/통제관 원본 참조 정보만 있고, 새 `crew_meeting_bridge_events`는 아직 메시지 0건이다.
- **다른 레지스트리 점검:** `v5_chatgpt_bridge_*`, `v4_sync_room_registry`, `v6_rooms` 등에서 동일 URL/등록 상태를 확정하지 못했으므로 등록 위치를 임의 추정하지 않는다.
- **확인된 현실적 과제:** 세 원본방의 서버 등록 여부와 상관없이 ChatGPT 원본 대화 간 실시간 자동 송수신은 별도 미구현/HOLD. 기존 계정·키·서명·보고서/누적DB는 변경하지 않고 세 방 공유 로그 및 원본 세션별 도구 호출을 검증할 것.
- **문구 통일:** 이후 '중앙연결관 서버 미등록' 금지. 정확히는 '선장 확인상 중앙연결관 서버 등록됨; `crew_meeting_original_links`의 `linkofficer` 원본 URL 행은 확인되지 않음'으로 표시한다.

### 최종 검증 보완 — 원본 3방 공유판
- GitHub Pages workflow #40202568587 `completed/success` / main commit `997e1b89005b044c83897346dead2b355c7fa4fa`. 공동회의 원격 HTML 공용 공유판 버튼 배포 성공; 기존 APK 버전 `1.0.6-REPORTS`을 새로 빌드하거나 V3/V7 앱을 변경하지 않음.
- 새 Edge server 업무 이벤트 경로 코드 단위 모의시험 8개 PASS: 인증 없는 GET 401, PATCH 405, 빈 POST 400, 정상 선장 POST 200, 임의 role navigator 사칭 POST 요청 시 서버가 저장 역할 captain으로 고정, 기록 출처는 `DECLARED_NOT_SESSION_VERIFIED`, 저장/조회 및 자동 원본 ChatGPT 연동 플래그 false. 실제 인증된 HTTP 요청의 종단간 시험은 아직 못했고 휴대폰에서 실제 새 공유판/메시지 표시 검증도 미실시.
- 원본 링크 서버 레지스트리 확인: `navigator`, `controller` 두 역할만 선장 등록(`captain_registered`)됨. `linkofficer` 원본 ChatGPT 주소는 등록되어 있지 않음(본 등록 DB 조회 기준). 따라서 3방 원본 직접 이동도 미완료이며, 현 단계에서 자동 실시간 동기화 성공으로 보고 금지.
- 개인정보 보호: 새 공유판 데이터 0건으로 실제 original-chat 자동 출처 가짜 메시지 없음, 새 테이블 RLS ON, anon/authenticated/PUBLIC grant 없음. 원본 채팅 URL·고정 입장키 출력/변경 없음.
- 다음 실제 점검: 선장폰 공동회의 앱 1.0.6에서 `📡 세 원본방 업무 공유판` 표시 및 선장 메시지 1건 저장 확인 → 원본 항해사/통제관/중앙연결관 각 ChatGPT 원본방에서 역할 안내 1회 전달·연결된 Supabase 도구 권한 확인 → 해당 방에서 요청 시 SELECT 및 자신의 글 INSERT → 앱의 약 4초 내 새 행 반영 확인. 이후 원본방이 무인 상태에서 자동 응답하도록 만들 수 있는 공식 도구/API 제공 여부 추가 판단. 현재는 불가능으로 HOLD.

## ⚓ 2026-10-25 — 원본 3방 공동 통신 기반 1차 구축 (원본 자동연동은 HOLD)

**선장 지시:** “세방의 실시간 연동 문제 해결해야죠.” V3 원본 항해사, V7 원본 통제관, 중앙연결관 기존 ChatGPT 대화 3방의 공동회의 통신을 최우선 작업으로 전환. 별도의 새 AI 또는 기존 채팅방인 척하는 응답으로 대체하지 말 것.

### 공식 지원 범위와 기술제한 (2026-10-25 공개 문서 기준)
- OpenAI ChatGPT 공유 프로젝트 설명: 별도 대화방은 서로 볼 수 있어도 하나의 원본 대화에 동시 편집하거나 메시지를 동기화하는 기능을 제공하지 않음. https://help.openai.com/en/articles/10169521-projects-in-chatgpt
- 그룹 채팅은 2026-07-09부터 신규 생성/참여가 중단됨. https://help.openai.com/en/articles/12703475-retiring-group-chats-in-chatgpt
- 연결된 앱/커스텀 MCP가 도구 호출 시 외부 공유 저장소에 액션을 할 수 있지만, 이것은 각 ChatGPT 원본방이 상대방 메시지를 자동 수신하거나 백그라운드에서 자율 응답한다는 뜻이 아님. https://help.openai.com/en/articles/12584461-connecting-custom-mcp-servers-in-chatgpt

### 이번 실제 구현 (V3·V7 원본 앱과 기존 DB 보존)
1. 새로운 독립 데이터 테이블 `public.crew_meeting_bridge_events` 생성. 열: UUID id, `room_id`, `author_role` (captain/navigator/controller/linkofficer), `body`, `source_kind` (captain_app/original_chat_connector/captain_manual), `provenance='DECLARED_NOT_SESSION_VERIFIED'`, `created_at`. 기존 `crew_meeting_messages`, V3, V7 분석/구매/누적DB 변경 없음.
2. Supabase migration `crew_meeting_original_three_room_bridge_20261025` success. 새 테이블 RLS ON, `PUBLIC/anon/authenticated` 모든 권한 REVOKE, 무단 공개 열람 차단. 실제 확인 시 테이블 `event_count=0`. 허위 원본 메시지나 시험 가짜 AI 답변을 삽입하지 않았음.
3. 단독 Edge Function `crew-three-room-bridge` v1 `ACTIVE`, 서버 코드 소스 `crew-meeting-android/server/crew-three-room-bridge-v1-index.ts` commit `98f0ba4e44467fa0a8000c0843db3cb228655175`. 기존 공동회의 고정 입장키 SHA-256 인증 후 `GET /events` (최근 100건) 및 `POST /captain` (선장 역할만, 1200자 이내, 6초 쿨다운) 허용. Client에서 항해사·통제관·연결관을 사칭하는 POST 엔드포인트 제공하지 않음.
4. 공동회의 원격 웹 `crew-meeting.html`에 `📡 세 원본방 업무 공유판` 추가. 실제 공유 DB의 이벤트를 펼친 화면에서 약 **4초 간격으로 확인**. 선장 지시 등록/공유 기록 조회/원본방 역할별 `연결 안내 복사` 버튼. 각 원본 ChatGPT 방에서 선장이 초기 지시를 전달하고, 해당 원본 AI가 **연결된 Supabase 도구를 직접 호출하는 턴에서만** 공유 조회/답변 등록할 수 있는 안내문 생성. 앱 역할 표시는 **수동 선언 출처이며 원본 세션 인증 아님**을 명시.
5. 웹 코드 최초 commit `c04dec6a0e18c1195548b2e3f85172a59508e4b3`, 안내 SQL 정교화 commit `997e1b89005b044c83897346dead2b355c7fa4fa`. 기존 1.0.6 드라이브 APK 정본 유지: `https://drive.google.com/file/d/1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL/view?usp=drivesdk`. WebView 최신 HTTPS 화면 로드로 APK 재설치 없이 새로운 접이식 메뉴 표시.
6. 정적 검증: 웹 JS 구문 OK, 새 DOM ID 중복 없음, 기존 V3/V7 실행 버튼·원본 3방 열기·회람·보고서·고정 APK 업데이트 경로 유지; 공유 메시지는 `textContent`로 안전하게 표시. 서버 `ACTIVE v1` 재조회, RLS ON / 공개 grant 없음 / 초기 데이터 0건 확인.

### 아직 해결되지 않은 중요 부분
- **기존 ChatGPT 원본 3방 간 자동 메시지/응답 push-pull 및 무인 백그라운드 처리 불가**, 공식 지원 인터페이스 확인되지 않음. 현재는 '원본방 연결 도구를 통해 호출하는 턴'에서 공유 이벤트 조회/등록 가능한 기반까지만 구축. 원본 ChatGPT 방의 완전한 대화 내역을 자동 읽거나 앱에서 원본 AI에 직접 메시지를 보내는 기능은 구현하지 않음.
- 각 원본방에서 연결 도구의 실제 사용 가능성/권한, 한 번 전달한 초기 안내문 처리, 각 방에서 메시지 INSERT/SELECT 및 공동회의 앱 반영의 종단간 검증 미실시. 4초 polling은 **앱 패널이 열린 동안만** 진행; 진정한 websocket push/ChatGPT 원본방의 자동 대화는 아님.
- Edge API 인증 후 실제 GET/POST 네트워크 확인, Github Pages workflow run #40202568587 최종 성공 확인 필요. 이전 앱·자료에 손대지 않음.
- 다음 검증 기준: 선장 입장 → 공유판 펼치기 → 선장 지시 1건 저장 → **원본 항해사/통제관/연결관** 각 원본 채팅방에서 연결된 Supabase tool로 SELECT 후 자신의 답변 INSERT → 각 답변이 4초 이내 공동회의 패널에 나타나는지 확인. 실패 시 권한/도구 지원 상태와 원본 세션에 대한 실제 연결 여부를 명확히 기록. 세 원본방의 자동 3방 대화 완성으로 승격하지 않음.

## 2026-10-25 / 선장 확인 접수 — 공동회의 업데이트 및 조회 화면

- 선장 답변: “확인했습니다.” 직전 확인 요청은 공동회의 앱 재진입 → `📑 V3·V7 보고서·자료 확인` → `목록 조회`, 그리고 1.0.6 화면의 업데이트 버튼 표시임.
- **확인 범위 기록:** 선장이 해당 확인 요청을 수행했다고 보고한 것으로 접수하되, V7 상세 본문 내용의 정확성, V3 TXT/JSON 파일 미리보기, 실제 Android APK 설치 버전 세부값 및 고정 Drive 링크를 통한 신규 설치의 모든 단계까지 별도 입증된 것으로 확대하지 않음.
- 현재 공동회의 1.0.6-REPORTS / Google Drive 고정 APK ID `1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL`. GitHub Pages 웹 배포 성공 `#40200549637`; 새 APK 추가 배포/재설치 없음.
- 다음 작업 우선순위: V7 1~16회차 목록·상세 데이터 대조, V3 결과전/후 TXT·JSON의 선장폰 미리보기, 보고서 자동 동기화 범위의 보안·출처·권한 설계. 원본 3방 실시간 자동 채팅과 V3/V7 DB 쓰기 연동은 미구현/HOLD 유지.

- **최종 웹 배포 성공:** GitHub Pages workflow #40200549637 `completed/success`, main SHA `b1ee75ae4e0b9d4e5f57787c44e61ace26bd2436`. 구글드라이브 고정 APK 업데이트 버튼/설치 페이지 웹 배포 성공 확인. 선장폰에서 실제 이동·설치·V3/V7 보고서 확인은 사용자 실측 전까지 미검증.

## ⚓ 2026-10-25 / 선장 '진행하십시오' — 구글드라이브 고정 APK 업데이트 경로 정비 및 V7 읽기전용 회귀검증

- **최신 작업 기준:** 공동회의 독립 Android **1.0.6-REPORTS** 유지, V3·V7 설치앱은 선장폰에 설치 및 공동회의에서 실행 연결 성공(선장 보고). V3/V7 자체 APK·서명·DB·잠금 상태·AI 원본방 연결 기록 변경 없음.
- 선장 직전 지시: 폴더를 열지 않고 **구글드라이브 APK 고정 링크로 바로 업데이트**하고, 뒤이어 `진행하십시오`로 미완료된 공개 설치 페이지/보고서 조회 검증 지속.
- **Drive 정본 재검증**: File ID `1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL`, 실제 다운로드 `항행의자유_공동회의_최신업데이트.apk`, 크기 `15618` byte, SHA-256 `8452b96f3f25b9d806535eb790b3614853f8afd4d8243ac74fb2d2723fb016a6`, ZIP CRC 검사 통과. `1.0.6` 배포본 변경·재업로드하지 않고 동일 파일 보존.
- **공식 설치 페이지 수정:** GitHub main `crew-meeting-releases/index.html` 기존 1.0.5-LAUNCH 안내 및 구식 1.0.5 APK 링크를 제거하고 **1.0.6-REPORTS** 최신 확인판으로 갱신. Git commit `443cab7757a7f087f02720be94a361fee741e647`. 대형 '구글드라이브 최신 APK 열기' 링크가 현재 정본 `https://drive.google.com/file/d/1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL/view?usp=drivesdk` 직접 열기. Android 드라이브 다운로드/설치 최종 동작은 사용자가 확인해야 함. 기존 릴리스 APK 파일 1.0.3~1.0.5는 삭제하지 않음.
- **앱 내부 바로가기 추가:** 메인 원격 `crew-meeting.html`에 `📥 공동회의 APK 업데이트 · 구글드라이브 바로가기` 앵커 삽입. Git commit `b1ee75ae4e0b9d4e5f57787c44e61ace26bd2436`. Android 1.0.6의 WebView는 기존과 동일하게 최신 HTTPS 화면을 불러오므로 **새 APK 빌드·재설치 불필요**. 외부 Drive 링크는 Android WebView 네이티브 라우트가 브라우저/드라이브로 실행하도록 설계되어 있음; 실기기 확인은 미실시.
- **검증:** GitHub 최신 HTML readback 정상, JS 문법·DOM ID 중복 없음, 기존 V3/V7 설치 앱 실행버튼, 원본 ChatGPT 3방 링크, 새방 인계/수동 회람 및 V3/V7 보고서 조회 DOM 모두 보존. 정적 회귀 검사 13/13 통과. 구글드라이브 URL 동일성·1.0.6 표기·파일 SHA-256 표기 검증.
- **V7 서버 확인:** 기존 Supabase Edge Function `crew-reports-readonly` version 1 `ACTIVE` (재배포/수정 없음). `public.v7_documents` 읽기전용 조회에서 공용 원문 `V7_SOURCE` 16건과 선장방 `V7_PURCHASE_5` 16건 그대로 확인. 당첨률·추첨 전 확정 데이터로 과대 주장 금지. 개인 입장키를 소스·기록·요청으로 노출하지 않음.
- **배포 상태:** GitHub Pages workflow run `#40200549637`의 최종 완료 확인 필요. 정적 HTML 커밋/소스 readback과 실제 Pages 배포 성공 여부는 구분.
- **남은 검증:** 실제 선장폰에서 기존 1.0.6 앱 열기 → 새 고정 업데이트 버튼 표시/Drive 링크 이동 → 앱 복귀 시 기존 등록·고정키·V3/V7 실행 지속 확인, `📑 V3·V7 보고서·자료 확인` 내 V7 원본 목록/상세 및 V3 내보내기 파일 미리보기 동작 확인. 실제 모바일 조회 성공 증거 확보 전 완료로 판정 금지. 실시간 원본 ChatGPT 3방 자동 송수신과 V3/V7 DB 자동 동기화는 여전히 미구현/HOLD.

## ⚓ 2026-10-25 · 공동회의 APK 업데이트 경로 Google Drive 고정

- 선장 지시: 새 APK 업데이트 시 폴더 탐색 없이 **현재 ChatGPT 대화방에서 직접 누를 수 있는 구글드라이브 고정 링크** 제공.
- Google Drive 신규 등록 정본: `항행의자유_공동회의_최신업데이트.apk`, Drive file ID `1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL` (선장 계정의 연결된 Google Drive).
- Drive가 제공한 실제 파일 주소: `https://drive.google.com/file/d/1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL/view?usp=drivesdk`. 링크 공유·외부 공개 설정은 변경하지 않음. 연결 계정에서 열 수 있는 파일.
- 현재 업로드된 내용: 공동회의 `1.0.6-REPORTS` APK, 15,618 bytes, SHA-256 `8452b96f3f25b9d806535eb790b3614853f8afd4d8243ac74fb2d2723fb016a6`.
- **검증:** Drive `upload_file` 성공 및 반환 file ID 확인 → Drive `fetch`로 직접 원본 다시 다운로드 → 원래 대화 첨부 APK와 바이너리 동일(sha256/byte comparison PASS).
- 차기 업데이트에서는 기존 **동일 Drive file ID**에 `Google_Drive.update_file(fileId, file_uri)`로 바이너리만 덮어씌우고 파일명을 `항행의자유_공동회의_최신업데이트.apk`으로 유지. **새 파일 생성으로 주소를 바꾸지 않는다.** 수정 전에는 동일 app package와 원본 고정 서명, 버전 증가, SHA256 검증을 필수로 수행하고 기존 V3/V7 설치본·원본 기록은 변경하지 않는다.
- 사용자 UX: 본 대화에 고정 Drive 파일 링크를 직접 게재. Drive 파일 화면에서 다운로드하고 Android가 제시하는 설치/업데이트 확인을 누르는 과정은 시스템상 남으며, 앱이 강제로 무단 설치하지 않음. 폴더를 직접 찾아갈 필요 없음.
- 공동회의 1.0.6의 실제 선장폰 설치, 서버 V7 보고서 조회 및 V3 내보내기 파일 미리보기는 아직 선장 확인 대기.


- **APK 최종 산출물 확인:** GitHub Actions run #40197046078의 Android SDK, 원본 서명키 복구, 빌드, `apksigner` 인증서/패키지·versionCode 검사, SHA256 생성, artifact 업로드 단계는 모두 **success**. 작업 전체 status는 마지막 `Publish only this isolated pilot to feature branch` 단계에서 **failure**, 따라서 GitHub 고정 공개 설치 페이지에는 1.0.6 게시 완료라고 주장하지 않음.
- 검증 완료한 artifact ID `12600332803` (`HANGHAE-CREW-MEETING-1.0.6-APK`)을 내려받아 압축 해제 후 실제 Android APK(15,618바이트) 무결성/ZIP 구조 확인. 1.0.6 APK SHA256 `8452b96f3f25b9d806535eb790b3614853f8afd4d8243ac74fb2d2723fb016a6` — artifact 내 배포 해시와 일치.
- 사용자에게 채팅에서 직접 `/mnt/data/항행의자유_공동회의_1.0.6_보고서조회_업데이트.apk` 첨부로 제공(2026-10-25). 공동회의 1.0.5→1.0.6 **업데이트 설치** 지시. **선장폰 실제 업데이트·보고서 기능 열림·V7 서버 상세조회·V3 파일 미리보기는 여전히 미검증**.
- 다음 남은 작업: 원본 서명·V3/V7 설치본 및 기존 데이터 유지 확인(선장폰), 기능 검증 영수증 기록. GitHub 공개 다운로드 페이지 1.0.6 영속 게시 실패 상태를 안전하게 재시도하며, 어떤 경우에도 이전 1.0.5 릴리스는 삭제하지 않음.
## 2026-10-25 / 공동회의 1.0.6-REPORTS APK 요청

- 선장 지적: "업데이트 apk를 주어야 확인하지요". 웹 UI 업데이트만으로 설치 검증을 요청한 이전 응답을 정정하고 **동일 앱 새 APK 제공을 우선**.
- 기존 실제 설치판: 공동회의 1.0.5-LAUNCH / `kr.hanghae.crewmeeting.compact` / versionCode 105. 선장폰 V3 항해사와 V7 중앙통제실의 최초 연결/실행 성공 보고 있음.
- 이번 새 릴리스: 공동회의 **1.0.6-REPORTS / versionCode 106**, `MainActivity.ROOM_URL`을 `?app_version=106`으로 변경하여 2026-10-25 완료된 V3/V7 보고서 읽기전용 웹 메뉴를 로드. V3/V7 직접 실행 버튼, 새방 이사/회람, 고정 입장키, 공동회의 저장소 유지.
- 업데이트는 **기존 공동회의 1.0.5 위에 덮어 설치**하는 독립 앱 업데이트. V3/V7 APK·서명·서버 DB·기기승인·누적DB 변경이나 재설치 없음.
- 수정 파일: 공동회의 Android Gradle, MainActivity, 공개 서명 지문 설명 자산, 독립 APK CI. 네 파일이 기능 브랜치 commit `6201750a57dbc7bcfa1ad3a45bd70e17f8f9b222`까지 반영.
- 서명 검증: 기존 독립 앱의 고정 서명 인증서 SHA256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be`와 불일치하면 빌드를 즉시 실패시키는 CI 보호 유지. 새 키 생성 금지.
- CI run #40197046078 빌드 및 Android 인증서·versionCode 검사 대기. 성공 후 APK를 검증된 파일로 직접 제공하고 공개 배포 주소를 1.0.6으로 변경.
- 사용자 확인 단계: 설치 파일 실행 → 기존 공동회의 **업데이트** → 앱 입장 → `📑 V3·V7 보고서·자료 확인` 메뉴의 V7 서버 목록/상세 및 V3 로컬 TXT·JSON 파일 선택 확인. 모바일 실사용 성공은 아직 미확인.


- **웹 배포 최종 검증:** GitHub Pages run #40195911909 `completed/success` (main SHA `2fe5ab4bc378f0e342342100304aa6276d58fbd7`). 기존 공동회의 APK 1.0.5 WebView는 최신 웹화면을 불러오므로 새 APK 빌드·재설치 불필요. **휴대폰 본인 인증 후 V7 상세 결과와 V3 실제 파일 선택은 아직 미확인**.
## 2026-10-25 / 선장 '진행하십시오' — V3·V7 자료 읽기 전용 연동 1단계

- 작업 기준: 공동회의 독립 Android 앱 1.0.5-LAUNCH 정상 배포·선장폰 V3/V7 설치 앱 실행 연결 성공(선장 보고). V3·V7 원본 앱 삭제·재설치 없음.
- **V7 서버 원본 실측**: public.v7_documents의 `room_key='all' / document_type='V7_SOURCE'` 원문 16건(회차 1~16), `room_key='captain' / document_type='V7_PURCHASE_5'` 선장방 5조합 기록 16건(회차 1~16). 2026-10-23 등록된 과거회차 자료로서 미래 실전 적중률 증거 아님. 다른 V7 자료실·엔진·채점 DB 자동연결로 오인 금지.
- **새 독립 읽기 전용 Edge Function**: `crew-reports-readonly` 버전 1 / `ACTIVE` (Supabase 프로젝트 `jyqkohpeowkhuaohlezc`). 서버 구현 소스 GitHub 기능 브랜치 `crew-meeting-android/server/crew-reports-readonly-v1-index.ts`, 최초 커밋 `063e1cbf6a00da38f70e9824067ad1060e4a5548`.
- 서버 보안: 기존 공동회의 고정 입장키의 SHA-256 해시와 허가된 **단일 선장 공동회의방 ID**를 검증한 뒤에만 GET 요청 허용. 허용 V7 room/type 조합 2개만 고정. 목록 최대 60건 및 특정 UUID 상세 조회. 응답에서 내부 기기 ID·서버 인증키 등 비공개 필드 제거. POST/PATCH 등 변경 메서드는 405. V7 원본 테이블에 어떠한 쓰기·삭제·잠금 갱신도 하지 않음. Service-role 키는 배포 서버 환경변수에서만 읽고 앱/저장소에 포함하지 않음.
- **공동회의 화면 신규 메뉴**: `📑 V3·V7 보고서·자료 확인` (메인 웹 `crew-meeting.html`, GitHub main commit `2fe5ab4bc378f0e342342100304aa6276d58fbd7`). V7 종류·회차 선택 → 서버 원본 목록 조회 → 선택 상세내용 보기. 입장키로 인증한 사용자만 서버 조회.
- **V3 자료 단계:** 선장폰에서 V3가 이미 내보낸 TXT·JSON 파일을 Android 파일 선택기로 고른 뒤, 브라우저 로컬에서 최대 1MB UTF-8 내용을 미리보기(표시 최대 30,000자) 및 SHA-256 표시. V3 앱 내부 비공개 저장소는 타 앱에서 자동 접근 불가. 선택 파일은 공동회의 서버로 업로드되지 않으며 원본을 변경하지 않음. V3 서버 보고서의 자동 동기화·V7 회차 자동 편입을 구현했다고 주장하지 않음.
- **정적·모의 테스트:** V7 조회 함수가 고정 방ID·해시 인증을 강제하고 종류 2개 외 접근 불가, 서버 쓰기 함수 없음, JS 문법·중복 DOM ID·원문/조합 표기·결과전 시점 주의·V3 파일 1MB 제한·로컬 읽기/자동 업로드 없음·기존 V3/V7 설치앱 버튼과 원본 채팅 버튼 보존 등 기능 체크 13/13 통과. 서버 배포 후 상태 `ACTIVE/v1`·인증 가드·허용 스코프 재조회 검증 통과.
- **미검증:** 웹 Pages 작업 run `#40195911909` 배포 최종 성공 확인 필요, 실제 인증 토큰으로 V7 원본 상세 요청 성공은 외부 네트워크 제한상 미검증, 선장폰 Android V3 파일 선택/미리보기와 V7 목록/상세 조회 실제 UI 결과도 미검증. 등록 키 노출·고정키 변경 없음.
- **다음 안전 작업:** Pages 배포 완료 확인 → 선장폰 1.0.5에서 기존 고정키로 접속하여 1~16회 V7 두 종류와 V3 원본 TXT/JSON 한 개 조회 확인 → 수신 내용과 원본 SHA 대조. 이후 서버에 없는 V3/V7 결과후 및 누적DB 자동 읽기 범위는 별도 권한·출처·기록보존 검증 후 추가.

## 2026-10-25 / 선장폰 V3·V7 앱 직접 실행 연결 성공 보고

- **선장 확인:** 공동회의 1.0.5의 V3 항해사앱·V7 중앙통제실 **설치 앱 연결 성공**(선장폰 실사용 보고). 담당 역할별 앱 선택/연결 성공 사실을 수용한다. 직접 캡처·자동 계측 없이 선장 보고 기준이며, 앱 내부 모든 화면·보고서 연동이 시험되었다는 의미는 아니다.
- 공동회의 APK: 1.0.5-LAUNCH, applicationId `kr.hanghae.crewmeeting.compact`, versionCode 105. V3/V7은 **기존 선장폰 설치본을 그대로 사용**하고 재설치/삭제/초기화하지 않았다.
- 구현 출처: GitHub Pages #40194882637 `completed/success`, 커밋 `d95a2c01966ffb2538e17079622a2d52b19f80bb`; 공동회의 APK CI #40194638263 `completed/success`.
- **한계 분리:** 이는 V3/V7 네이티브 앱 실행 연결이며 기존 ChatGPT 원본방 3개의 실시간 자동 채팅 공유, V3/V7 엔진 합병, 결과전/후·누적DB 보고서 양방향 동기화의 완료 증거가 아니다.
- **다음 작업:** 두 설치 앱 및 서버의 읽기전용 보고서/자료 연결 계약과 인증 권한을 확인 → 공동회의에 원본 출처가 명확한 조회 메뉴를 안전하게 제공. 기존 V3/V7 정식 설치본·승인·최초등록·누적DB·서명키 보존. 실제 데이터 자동 저장/수정은 별도 증거·검증 전 금지.


- **최종 확인:** GitHub Pages #40194882637 `completed/success` / main SHA `d95a2c01966ffb2538e17079622a2d52b19f80bb` (2026-10-25). 설치 홈페이지 및 공동회의 1.0.5용 웹 실행 버튼 배포 확인. Android 선장폰 실제 2앱 선택·실행은 아직 미검증.
## 2026-10-25 / 선장폰 V3·V7 설치확인 보고 접수 → 공동회의 1.0.5 별도 앱 실행기

- **선장 최신 정보:** V3 항해사와 V7 중앙통제실은 선장폰에 이미 설치되어 있음. 이는 선장 보고 기준이며 실제 단말 설치 목록이나 실행 결과를 서버에서 직접 읽은 것은 아님. 재설치·APK 합병 불필요.
- **구현 목적:** 공동회의 앱 안에 '📲 V3 항해사앱 실행', '📲 V7 중앙통제실 실행' 버튼 및 역할별 '연결 변경' 추가. 정확한 V3/V7 Android package ID가 확보되지 않아 **선장폰의 OS 앱 선택창에서 설치된 실제 앱을 최초 1회 고르고 이후 저장된 ComponentName으로 실행**. 사용자 확인 없는 패키지 추정·잘못된 앱 자동 매핑 금지.
- 원본 V3·V7 앱을 합치거나 개조하지 않음. Android 앱 실행 `Intent.ACTION_PICK_ACTIVITY` + `Intent.ACTION_MAIN/CATEGORY_LAUNCHER`, 선택 결과를 공동회의 앱의 비공개 SharedPreferences `crew_meeting_installed_app_links_v1`에 역할별로 기록. 앱이 삭제/업데이트되어 실행 컴포넌트가 바뀌면 다시 선택 가능. JS bridge `CrewInstalledApps.openInstalled('v3'|'v7', Boolean)` 외 임의 앱 오픈/패키지 선택 외부 호출은 제공하지 않음.
- 별도 공동회의 앱 1.0.5-LAUNCH, `kr.hanghae.crewmeeting.compact` / Android versionCode `105`. WebView 원격 주소 `?app_version=105`. Android 코드·Gradle·서명정체성·CI만 연구 기능 브랜치에서 수정; V3·V7 소스·DB·기기승인/서명키 불변.
- GitHub Actions CI run #40194638263 `completed/success`. 기존 서명 지문 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` 강제 비교, 기존 앱 package ID 유지, 버전코드 105 검증, apksigner 검증 통과.
- APK SHA256 `0a0f15a866471a7ec6071536acd1fc5f9bf4641cb13d8dd651f80cc7d5cedcde` (파일 15574바이트). Git Blob SHA `64d72ae02019f3ce979ee6e2717c65353eb49080`; GitHub main 공개 경로 `crew-meeting-releases/HANGHAE_CREW_MEETING_1.0.5.apk` (main commit `851ede9671b1a21cf5c84b81d7831fcc8c2b6326`). 별도 해시 파일 존재, 압축 구조 및 SHA 검사 통과. 이전 1.0.4 릴리스 보존.
- GitHub main 웹 UI 버튼 commit `61126906d6412ea7765afba047a1d0a61c591afd`; 설치 페이지 최신화 commit `d95a2c01966ffb2538e17079622a2d52b19f80bb`. Native 브리지 버튼 역할/재선택 전달 기능 JS 모의 호출 검증 PASS. Pages run #40194882637 성공 여부 후속 확인 필요.
- **정확한 기능 범위:** 이는 두 기존 **Android 앱을 공동회의에서 실행**하는 것이며 두 앱 엔진을 동일 프로세스에 이식하거나, ChatGPT 원본 대화 3방을 자동으로 메시지 송수신시키는 기능이 아님. 원본 채팅 바로가기와 수동 회람은 별도 유지.
- **미검증:** 선장폰 기존 1.0.4→1.0.5 앱 업데이트/로컬 설정 보존, OS 앱 선택화면에 V3/V7 표시, 최초 저장 후 클릭 실행, 뒤로 돌아왔을 때 공동회의 화면 상태 유지. 수동 확인 필요. 개인 서명키 장기 보존 사본도 여전히 미확보.
- **다음 안전 단계:** 선장폰에서 기존 공동회의 1.0.4 삭제하지 않고 1.0.5 동일서명 업데이트 → V3/V7 각각 설치앱 선택창에서 한 번 선택 → 이후 버튼 실행/돌아오기 검증. 그 뒤 조회/보고서 연동을 API 권한검증 후 별도 진행.


- **최종 Pages 배포 검증:** GitHub Actions #40193662736 `completed/success`, 배포 대상 main SHA `5b8796a4ee08f25c7ee762aa52bc5cac9959c62c`. 1.0.4 APK 웹 URL에서 신규 회람 UI 표시 대상 확정. Android 선장폰 실제 복사·왕복·로컬저장 테스트는 미실시.
## 2026-10-25 / 선장 '진행하십시오' — 1.0.4 앱 원본방 회람 대화 1차 구현

- **선장 요구:** 공동회의 앱 채팅방에서 기존 V3 항해사·V7 통제관·중앙연결관과 업무 대화를 진행. 원본 AI를 별도 API 모의 AI와 혼동해서는 안 됨.
- **구현된 사실:** 기존 웹 화면 `crew-meeting.html`에 상단 `⚓ 원본 3방 지시·답변 회람` UI 추가. 선장 지시 1~1200자 등록 → 역할별 전달문 복사 → 선장이 실제 원본 ChatGPT 대화방 버튼으로 이동하여 전송 → 원본방 답변을 선장이 복사하여 각 역할 답변란에 수동 입력 → 3인 회람록 복사. 다음 담당자용 전달문에는 다른 담당자의 수동 기록 답변도 포함해 회람할 수 있음.
- **반드시 구별:** 이것은 원본 ChatGPT 채팅방에 선장이 직접 참여하는 **수동 전달·회수 방식**이며, 외부 APK에서 원본 AI 메시지 자동 읽기/전송, 3원본방 실시간 자동 상호 소통, 원본방 대화 내장/임베딩 구현이 아님. 기존 API 시범 채팅은 `시험 AI(API)에만 전달`로 재표시하여 오인 방지.
- **자료·기기 보존:** 회람은 동일 앱/브라우저 로컬 저장소 `crew_meeting_original_manual_relay_v1`의 사용자 단말에만 저장. 서버·V3/V7 DB·기존 chat 기록에 자동 쓰기 없음. 회람록 복사로 수동 백업 지원. 최대 50건 저장; 초과 시 **자동 삭제 없음**. 로컬 저장소 손상·쓰기 실패 시 기록 덮어쓰기 방지. 비공개 원본 대화 URL·공동회의 고정키는 회람록 텍스트에 포함되지 않음.
- GitHub main 웹 소스 수정 커밋: `908df5aa1f33d63af2f7a586faf27e5c0aada80c` (회람 UI), `ca1ff45356b3e06e32fec9eadb3e3499f0ac885d` (클립보드 폼 내용 보호 및 시험 API 표기), `5b8796a4ee08f25c7ee762aa52bc5cac9959c62c` (클립보드 차단 시 복사문 미리보기).
- 자동 JS 문법·순수 기능 11개 검사 PASS: 지시 저장, 회람 답변의 다른 담당자 전달, 자기 답변 제외, 수동 출처 명시, 회람록 내보내기, 기존 원본 버튼/인계 버튼 보존, 클립보드 폼 덮어쓰기 방지. 실제 휴대폰 원본 ChatGPT 왕복 사용 시험 **미실시**.
- **배포:** 공개 GitHub Pages run #40193662736 성공 여부 최종 확인 후 갱신. 앱 1.0.4 WebView는 매번 HTTPS 웹 화면을 불러오므로 **APK를 새로 다시 빌드하거나 기존 앱 삭제할 필요 없음**.
- **남은 작업:** (1) Pages 최신 배포 성공 확인; (2) 선장폰에서 지시 등록·3담당 원본방 열기·복사 붙여넣기·답변 수동 회수·앱 복귀·기록 유지 검증; (3) 공식 지원 API가 없는 실시간 3원본 ChatGPT 직접 공동 채팅은 미구현·HOLD 유지; (4) 통합 지휘본부의 V3/V7 보고서 읽기 API와 권한·데이터 경계 검증.


## 2026-10-25 · 공동회의 독립판 1.0.4-HANDOFF 정식 빌드 · 기존 앱 통합 질문

- 선장 지시: 한 번 업데이트할 수 있도록 APK 제공. 추가 문의: 공동회의 앱에 V3 + V7을 넣을 수 있는지.
- **새 업데이트판**: `kr.hanghae.crewmeeting.compact` / Android `versionCode 104` / `versionName 1.0.4-HANDOFF` (이전 공동회의 1.0.3에서 업데이트하는 독립 앱).
- Android WebView가 `https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/crew-meeting.html?app_version=104` 로드. 최신 원본 3방 바로가기, 이사 후 주소 갱신/구방 보존, 첫머리 인계양식 복사 기능 유지.
- 코드 수정: `crew-meeting-android/app/build.gradle`, `crew-meeting-android/app/src/main/java/kr/hanghae/crewmeeting/MainActivity.java`, `crew-meeting-android/app/src/main/assets/signing-identity.json`, `.github/workflows/build-crew-meeting-apk.yml`.
- 기존 CI 서명키 캐시 `crew-meeting-independent-pilot-signing-v1` 재사용. 복구한 서명 인증서 SHA256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` 와 비교해 다를 경우 빌드 실패하도록 유지.
- 성공 CI GitHub Actions `#40191047213` `completed/success`. 패키지 `kr.hanghae.crewmeeting.compact` / versionCode `104` 검사, Android `apksigner verify` / 서명 인증서 검사 통과.
- APK 파일 SHA256: `ad4b71950c272b5feae01b5c9a5bb326b0bdee9382c49cb6a713a3ea95152a4d`. GitHub blob `4dd3a879c5f0e3c07cd2177b336dafa578538354`.
- 공개 repo main 새 경로: `crew-meeting-releases/HANGHAE_CREW_MEETING_1.0.4.apk`, checksum `crew-meeting-releases/HANGHAE_CREW_MEETING_1.0.4.apk.sha256`; Git commit `2b0de0c252efd8c2b2992d5daa51b12d9fafd79e`. 이전 1.0.3 배포본은 삭제하거나 교체하지 않음.
- 설치 페이지 `crew-meeting-releases/index.html` 1.0.4 버전·파일명·SHA 수정, main `a8309e8318c350d842d32f3eb1672414acef06b6`. Pages run `#40191392543` 성공 여부 최종 확인 필요.
- V3/V7 두 앱 통합 원칙: 단일 공동회의 지휘화면에서 V3/V7 **별도 메뉴, 직접 열기, API 경유의 보고서 조회/검증**은 추가 구현 가능. 그러나 기존 V3/V7 APK 패키지/서명/데이터를 임의 병합하거나 기존 ChatGPT 원본 대화를 외부 API처럼 자동 송수신한다고 주장 금지. V3와 V7 각각 원본은 독립 보존.
- **미검증:** 선장폰 실제 1.0.3→1.0.4 업데이트 성공, 기존 로컬 인증/원본 URL 유지, V3/V7 실행 연계 및 실제 통합 기능(현재 1.0.4에는 엔진 이식 없음). 개인 서명키 장기 보존 복구도 미확보.
- 다음 작업: 1.0.4 선장폰 정상 업데이트/로컬 데이터 보존 확인 → V3와 V7 각 앱의 안정적인 연결 인터페이스와 인증된 보고서 읽기 연동 설계/검증. 사용자 승인 없는 V3/V7 APK 업데이트/데이터 변경 금지.

## ⚓ 2026-10-25 최신 지시 승계 — 원본방 등록 확인 및 새방 첫머리 강제양식 (시험 완료, 모바일 미검증)

- 선장 보고: V3 항해사·V7 통제관에서도 현재 대화방 안에서 대화 가능하도록 구현됨. **선장 보고로만 접수**했으며 원본 세 ChatGPT 세션 사이의 메시지 자동전달·공유방 종단간 성공은 확인하지 않음.
- Supabase 원본링크 DB `crew_meeting_original_links` 실측: `navigator` 및 `controller` 각각 `added_by=captain_registered` 현재 주소 등록됨 (2026-10-25 11:39 UTC). `linkofficer` 서버 등록은 없음. 주소 자체는 공개 인계서에 기록하지 않음.
- 선장이 요구한 다음 단계: “새 방 이사하면 강제양식으로 첫머리에 붙게 하라” 우선 구현. 원본 연결 실시간 게이트는 **자동송수신 검증 전 HOLD 유지**, 그러나 안전한 인계양식 제작·공유 웹 UI 구현은 진행 가능.
- 표준 인계서 정본: `docs/crew-meeting-migration-first-message-v1.md` main commit `1bf48b8d8f7c3acb34ba70db1d6185a6bb59ed5e` (비밀키·개인 채팅 URL 제외).
- 공동회의 1.0.3 원격 웹화면 변경: `crew-meeting.html` main commit `71428bfc03fe7102efc78b7e7b8f174e853121a3`. 상단 「📋 새방 첫머리 필수 인계양식」 추가. 담당 V3/V7/중앙연결관 선택 → 역할별 필수 인계문 생성 → 복사 → 새 ChatGPT 방 열기. 새 방의 첫 메시지는 선장이 직접 붙여넣어 전송하고 새 주소를 기존 역할에 갱신해야 함.
- 모든 3가지 역할 문구를 JS 함수 실행하여 필수 정본 링크, 기존자료 보존, 버전 혼동 방지, 결과검증·새방 주소 갱신 및 비밀키 무포함 조건 검사. **3개 양식 × 각 조건 모두 PASS**, HTML 내 JavaScript 정적 구문 검사 PASS.
- ChatGPT 새 방 메시지 composer에 외부 APK가 **자동 붙여넣거나 대신 전송하는 API는 없음**. 따라서 “강제양식”은 앱의 필수 복사/첫 메시지 지시 흐름을 말하며, 실제 새 채팅 첫머리 전송 여부는 사용자가 수행·확인하는 단계로 남음.
- GitHub Pages 배포: run #40190430701, 최종 완료 여부 확인 뒤 갱신. 구 APK 1.0.3이 동일 HTML을 불러오므로 재설치/기존 데이터 초기화 불필요. 선장폰 모바일 실사용은 미검증.
- V3 원본 작업대장·V7 원본 작업대장 각각 Google Docs 최신 기록 검토. 작업 범위·승격 조건 독립 유지. 기존 V3/V7 앱·실구매·누적DB·승인·서명키·초기잠금 데이터 **수정하지 않음**.
- 다음 단계: GitHub Pages 배포 성공 확인 → 선장폰에서 3종 중 한 역할을 선택해 양식 복사·첫 메시지 전송·새 채팅 URL 저장/과거방 복귀 실사용 확인. 3원본 자동송수신은 별도 증거 확보 전 승격 금지.

> **선장 최우선 보류 게이트 · 2026-10-25 (HOLD)**\n> 중앙연결관 + V3 원본 항해사 + V7 원본 통제관의 **원본 대화방 간 실질적 양방향 소통**이 검증되기 전까지 이후 개발·방 이사 강제양식·정식 운영 승격을 진행하지 않음.\n> 원본방 바로가기, 공유 프로젝트 메모리, 원본 자료 열람, 별도 AI API의 역할 응답은 **3원본방 실시간 연결 성공으로 판정하지 않음**.\n> 검증 조건: (1) 세 기존 ChatGPT 원본 대화를 그대로 보존 (2) 한 곳의 실제 지시가 세 원본방에 도달한 증거 (3) 각 원본방에서 그 지시를 받고 응답한 실제 송수신 기록 (4) 선장폰 종단간 검증. 현재 (2)~(4) 미달성.\n> 현재 ChatGPT 제공 기능에는 외부 APK가 다른 기존 ChatGPT 개인 대화에 자동 송수신하는 공식 API가 없음. 기존 프로젝트 채팅은 상호 참고는 가능하지만 실시간 공동 채팅 아님. 새 그룹 채팅 생성/가입은 종료된 상태. 불가능한 기능을 가능하다고 표현하지 않음.\n> **보존:** V3/V7 앱·원본 자료·누적DB·서명키·채팅 기록 삭제, 초기화, 덮어쓰기 금지. 현재까지 진행한 공동회의 웹·서버·APK는 독립 시험판으로만 보존.\n\n# ⚓ 항행의자유 공동회의 — 항해 작업대장 / 2026-10-25

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

### 2026-10-25 / 선장 지시: 원본 채팅방과 바로 소통 — 실현 가능 범위의 직접 연동
- 요구사항: **기존 V3 항해사·V7 통제관의 원본 ChatGPT 대화방에 직접 들어가 기존 맥락 그대로 소통**. 별도 API AI의 역할 연기는 금지, 기존 채팅·소스·DB 보존.
- 공식 API 검증: ChatGPT 개인 대화방을 외부 APK에서 그대로 읽고 메시지를 제출해 응답을 받는 공개 연결 API 없음. 기존 GPT 외부 웹사이트 임베딩도 불가. ChatGPT 새 그룹 채팅도 2026-07-09부터 생성/참여 종료. 이 정책을 우회하거나 자동 소통 완료로 주장하지 않음.
- 구현: 웹 화면 `crew-meeting.html` main commit `185a5ab2e500f2ddea9dc84ff56c8c7889a7db9f`.
- 세 원본 ChatGPT 링크 버튼을 공동회의 파일/AI API 입장키와 **독립적으로 작동**하도록 수정. 원본방 URL을 처음 한 번 직접 등록하면 `localStorage`에 역할별로 저장하고, 버튼 탭 시 원래 ChatGPT 대화방 `/c/<id>` 또는 `/g/<id>/c/<id>`로 이동.
- 기존 중앙연결관 로컬 키 `crew_meeting_central_linkofficer_original_chat_url_v1` 호환 유지. 서버 인증돼 있을 때는 등록된 URL을 부가적으로 서버와 동기화하지만, 실패해도 로컬 링크는 유지.
- 화면의 시험 AI 채팅은 **기존 원본방에 메시지를 보내거나 답변을 받는 기능이 아님**을 계속 고지.
- 오래된 V5 항해사 2026-10-15 URL은 현재 `기존항해사 - 북극성 - 7316` 주소임을 인증할 수 없어 자동 연결하지 않음. `통제관 - 푸른등대` 원본 정확한 주소도 미확보. 없는 주소 추정 금지. 선장님이 실제 대화방 주소창 URL 2개를 처음 한 번만 등록해야 해당 버튼으로 직접 열린다. 원본 ChatGPT 로그인 필요.
- 정적 JavaScript 구문검사 완료. 웹 Pages Actions run #40181618917 배포 상태 최종 확인 필요. 선장폰 원본방 직접 열기 실사용 검증 미완료.

- **최종 배포 확인:** GitHub Pages Actions run #40181618917 `completed/success`, head main commit `185a5ab2e500f2ddea9dc84ff56c8c7889a7db9f`. JavaScript 구문·원본 주소 로컬 저장·원본 버튼 입장키 독립 처리 소스 확인 통과. **Android 선장폰 실제 원본방 로그인과 대화 송수신은 미검증**.


### 2026-10-25 / 원본 ChatGPT 대화방 이사 시 URL 변경 해결 — 연결 기초 작업 (HOLD 유지)
- 선장 문제: 대화량이 많아 새 방으로 이사하면 `chatgpt.com/c/...` 주소가 바뀌고 이전방 바로가기만 남아 연결이 끊김.
- 설계: **역할 키 `navigator` / `controller` / `linkofficer`는 고정**, URL만 현재방 정보로 교체. ChatGPT 대화방의 고유 URL 자체는 유지·예약·자동 승계할 수 없음.
- 구현: `crew-meeting.html` main `552bff12d481c2173b425976e66b4bfeef56e56b`. 상단 3개 역할 버튼 유지, 「🔄 원본방 이사 · 주소 변경」 추가, 3담당 선택 후 새 URL을 등록. 등록 전 구 URL을 로컬 보관 최대 6개; 「이전방 열기」 버튼으로 복구. 파일명/원본 ChatGPT 쓰기 작업 없음.
- 로컬 저장이므로 **같은 앱/동일 기기 저장소**에서 연결이 유지됨. 저장소 초기화, 다른 기기로 이동하거나 URL 등록 이전에는 자동 승계되지 않음. 서버에 로그인한 경우 기존 등록 API를 통한 추가 동기화를 시도하나 자동 URL 감지는 하지 않음.
- **반드시 이사한 새 ChatGPT 방에서 첫 메시지를 보낸 후** 선장이 해당 새방 주소창의 `/c/` URL을 한번 복사해 등록해야 함. 기존 `/share/` 링크는 재개 불가.
- 자체 정적 JavaScript 구문검사 통과. 역할별 최신 URL 갱신/이전 URL 보존/다른 역할 미변경/공유주소 차단 4개 단위검사 통과(브라우저 URL 의미를 모사한 테스트 환경).
- GitHub Pages 배포 run #40188201801 확인 필요. 실제 선장폰 URL 변경/이전방 복귀 종단간 미검증.
- **HOLD 유지:** 이는 안정된 원본방 위치 관리 기능일 뿐 V3·V7·중앙연결관 세 독립 ChatGPT 세션의 자동 양방향 실시간 공동 소통을 완성하지 않음. 세 원본방 실제 연결 검증 전 정식 공동회의/차기 개발 승격 금지.

- **배포 검증:** GitHub Pages workflow run #40188201801 `completed/success`, main commit `552bff12d481c2173b425976e66b4bfeef56e56b`. Android 실기기에서 새 주소로 바뀌는지 확인은 미완료.

- **최종 배포 결과 (2026-10-25):** GitHub Pages run #40190430701 `completed/success` / main source commit `71428bfc03fe7102efc78b7e7b8f174e853121a3`. 서버 파일 존재·정적 스크립트 구문·3역할 인계 문구 테스트 확인. **휴대폰에서 복사→새방 전송→새 URL 저장 종단간 시험은 아직 미완료**.
