-- PostgreSQL/Supabase research-only write-path test.
-- This test is a transaction: all synthetic captain/reply events are ROLLED BACK.
-- Do not run this in parallel with app writes or use as a production send path.
BEGIN;
DO $$
DECLARE
  rid uuid := '96774ce6-c34f-4505-bbcc-c62e07d3ef3a';
  captain_id uuid;
  captain_time timestamptz;
  matched_roles int;
  matched_rows int;
BEGIN
  INSERT INTO public.crew_meeting_bridge_events (room_id,author_role,body,source_kind,provenance)
  VALUES (rid,'captain','[ROLLBACK TEST ONLY] synthetic captain','captain_app','DECLARED_NOT_SESSION_VERIFIED')
  RETURNING id,created_at INTO captain_id,captain_time;

  INSERT INTO public.crew_meeting_bridge_events (room_id,author_role,body,source_kind,provenance)
  SELECT rid, x.role, '회람응답:'||captain_id::text||E'\n[ROLLBACK TEST ONLY] reply',
    'original_chat_connector','DECLARED_NOT_SESSION_VERIFIED'
  FROM (VALUES('navigator'),('controller'),('linkofficer'),('navigator')) x(role);

  INSERT INTO public.crew_meeting_bridge_events (room_id,author_role,body,source_kind,provenance)
  VALUES (rid,'linkofficer','회람응답:123e4567-e89b-12d3-a456-426614174999'||E'\n[ROLLBACK TEST ONLY] mismatched ID',
    'original_chat_connector','DECLARED_NOT_SESSION_VERIFIED');

  SELECT count(DISTINCT author_role),count(*)
    INTO matched_roles, matched_rows
    FROM public.crew_meeting_bridge_events
   WHERE room_id=rid
     AND source_kind='original_chat_connector'
     AND created_at>=captain_time
     AND split_part(body,E'\n',1)='회람응답:'||captain_id::text;

  IF matched_roles<>3 OR matched_rows<>4 THEN
    RAISE EXCEPTION 'Unexpected roles=% rows=%', matched_roles,matched_rows;
  END IF;
END $$;
ROLLBACK;
SELECT count(*)::integer AS persistent_rows_after_rollback FROM public.crew_meeting_bridge_events;
