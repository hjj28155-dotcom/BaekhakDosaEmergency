# ⚓ 공동회의 1.0.7 — 세 원본방 연결 시험 절차 (연구용)
2026-10-26 KST / 정식 1.0.7 수정·배포 없음

## 현재 사실
- 기존 1.0.7은 원본 ChatGPT 세 방의 URL을 등록하고 바로가기로 이동할 수 있음. 직접 자동 메시지를 보내거나 읽는 기능은 없음.
- 공동회의 `crew-three-room-bridge` Edge Function은 선장 인증키를 통한 공유 이벤트 접근을 제공하며, 원본 세션 연동을 제공하지 않음.
- 2026-10-26 읽기 확인: navigator 선언 이벤트 2건, captain/controller/linkofficer 이벤트 0건.
- 앞서 이 항해사 역할의 연결된 Supabase 도구에서 공유 DB로 기록하고 재조회한 이벤트 `ae6f135b-6e05-46ed-a48c-1c6ddc2ee36f` 존재. 원본 ChatGPT 세션 자체 인증 증거가 아님.
- V3 항해사앱, V7 중앙통제실, 공동회의 1.0.7, 원본 소스·DB·기기승인·누적DB 및 모든 방 링크는 보존.

## 실제 세 방 시험 (선장 승인된 사용자 동작 필요)
1. 선장폰 설치된 공동회의 1.0.7 > '📡 실시간 공유' > '실시간 업무 공유'에서 선장의 실제 짧은 시험 지시 1건을 기록한다.
2. 이미 등록된 원본 **항해사** 방을 열어 위 지시를 공유 장부에서 **조회**하도록 요청한다. 그 방에서 연결된 Supabase 도구가 허용된 경우에만 실행한다.
3. 항해사방이 해당 지시를 실제로 검토하고 답하면, 최신 선장 이벤트 UUID에 대응해 답변 첫 줄을 정확히 `회람응답:<선장 이벤트 UUID>`로 하고, `author_role=navigator`, `source_kind=original_chat_connector`, `provenance=DECLARED_NOT_SESSION_VERIFIED`로 공유 장부에 기록한다. 등록 결과 ID를 확인한다. 이미 같은 지시에 대해 해당 역할의 기록이 있으면 재전송하지 않는다.
4. 통제관 실제 원본방에서 위 2~3단계를 `author_role=controller`로 독립 수행한다.
5. 중앙연결관 실제 원본방에서 위 2~3단계를 `author_role=linkofficer`로 독립 수행한다.
6. 아래 읽기 전용 SQL 또는 연구판 화면을 통해 지시 ID에 대해 서로 다른 역할 3개가 기록됐는지 확인한다. 원본 세션 자동 인증과 동일시하지 않는다.
7. 선장폰 화면에서 새 기록이 실제 표시되는지, 앱을 끄고 다시 켜도 기존 데이터를 보존하는지 확인한다.

## 읽기 전용 회신 확인 SQL
```sql
WITH last_captain AS (
 SELECT id, room_id, created_at
 FROM public.crew_meeting_bridge_events
 WHERE room_id = '96774ce6-c34f-4505-bbcc-c62e07d3ef3a'
   AND author_role='captain'
   AND source_kind='captain_app'
 ORDER BY created_at DESC, id DESC LIMIT 1
), replies AS (
 SELECT e.author_role, count(*) AS rows_seen
 FROM public.crew_meeting_bridge_events e
 JOIN last_captain c ON c.room_id=e.room_id
 WHERE e.source_kind='original_chat_connector'
   AND e.created_at >= c.created_at
   AND split_part(e.body, E'\n', 1)='회람응답:'||c.id::text
   AND e.author_role IN ('navigator','controller','linkofficer')
 GROUP BY e.author_role
)
SELECT
 c.id AS captain_instruction_id,
 c.created_at AS sent_at,
 coalesce((SELECT rows_seen FROM replies WHERE author_role='navigator'),0) AS navigator_entries,
 coalesce((SELECT rows_seen FROM replies WHERE author_role='controller'),0) AS controller_entries,
 coalesce((SELECT rows_seen FROM replies WHERE author_role='linkofficer'),0) AS linkofficer_entries,
 (SELECT count(*) FROM replies) AS distinct_role_count,
 'DECLARED_NOT_SESSION_VERIFIED'::text AS verification_limit
FROM last_captain c;
```

## 성공 판정
- 주장 가능한 결과: 각 실제 방이 연결 도구를 통해 공유 이벤트를 조회/작성했고, DB에 방별 회신 ID와 시각이 존재하며 선장폰에 표시됨 (해당 방에서 실제로 실행한 증거까지 확인하는 경우).
- 주장할 수 없는 결과: 원본 ChatGPT URL만으로 ChatGPT가 자동으로 메시지를 감지·송신·수신하거나, 백그라운드에서 자율 응답한다는 것.
- 3/3은 서버 공유 장부의 역할별 응답 수이며 원본 세션 암호학적 인증의 의미가 아님.

## 보안·보존
- 기기 입장키, 개인키, AI API 키, service_role 키, 개인 채팅 내역, 파일 전체를 공개 GitHub에 저장하지 않는다.
- 원본 자료를 삭제하거나 초기화하지 않으며 테스트용 다른 APK를 설치하지 않는다.
- 본 문서는 연구 브랜치에만 저장하며, 본 문서를 읽거나 실행한다고 정식 APK가 자동 수정되지 않는다.
