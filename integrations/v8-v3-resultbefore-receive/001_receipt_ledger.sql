-- V8 independent receipt ledger for V3 R60. ADDITIVE ONLY.
-- V7 tables, V3 source assets and old locks are not changed or deleted.
CREATE TABLE IF NOT EXISTS public.v8_v3_resultbefore_receipts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  origin_app text NOT NULL DEFAULT 'V3_NAVIGATOR' CHECK (origin_app='V3_NAVIGATOR'),
  epoch text NOT NULL CHECK (epoch ~ '^V3_[A-Z0-9_]{6,100}$'),
  round_no integer NOT NULL CHECK (round_no BETWEEN 1 AND 9999),
  asset_id uuid NOT NULL REFERENCES public.v6_room_assets(asset_id) ON DELETE RESTRICT,
  source_device uuid NOT NULL REFERENCES public.v4_sync_devices(id) ON DELETE RESTRICT,
  payload_sha256 text NOT NULL CHECK (payload_sha256 ~ '^[0-9a-f]{64}$'),
  storage_key text NOT NULL CHECK (length(storage_key) BETWEEN 10 AND 1024),
  analysis_mode text NOT NULL CHECK (analysis_mode IN ('HISTORICAL_BLIND_PRACTICE','PROSPECTIVE_PRE_DRAW')),
  locked_at timestamptz NOT NULL,
  accepted_at timestamptz NOT NULL DEFAULT now(),
  status text NOT NULL DEFAULT 'ACCEPTED_RESEARCH_ARCHIVE' CHECK (status='ACCEPTED_RESEARCH_ARCHIVE'),
  official_predraw_certified boolean NOT NULL DEFAULT false CHECK (official_predraw_certified=false),
  CONSTRAINT v8_v3_receipt_round_once UNIQUE (origin_app,epoch,round_no),
  CONSTRAINT v8_v3_receipt_asset_once UNIQUE (asset_id)
);
COMMENT ON TABLE public.v8_v3_resultbefore_receipts IS 'Independent V8 receipt for V3 original 4x5 result-before research. Not V7 engine first_reports; never implies official pre-draw certification.';
ALTER TABLE public.v8_v3_resultbefore_receipts ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE public.v8_v3_resultbefore_receipts FROM PUBLIC, anon, authenticated;
GRANT SELECT, INSERT ON TABLE public.v8_v3_resultbefore_receipts TO service_role;
CREATE OR REPLACE FUNCTION public.v8_v3_receipts_reject_mutation()
RETURNS trigger LANGUAGE plpgsql SET search_path = '' AS $$
BEGIN
  RAISE EXCEPTION 'V8_V3_RECEIPTS_IMMUTABLE';
END;
$$;
DROP TRIGGER IF EXISTS v8_v3_receipts_immutable ON public.v8_v3_resultbefore_receipts;
CREATE TRIGGER v8_v3_receipts_immutable
  BEFORE UPDATE OR DELETE ON public.v8_v3_resultbefore_receipts
  FOR EACH ROW EXECUTE FUNCTION public.v8_v3_receipts_reject_mutation();
