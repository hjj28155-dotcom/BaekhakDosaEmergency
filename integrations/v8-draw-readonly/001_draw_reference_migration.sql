-- Isolate 17 legacy draw references before retiring V7 API. No source deletion.
CREATE TABLE IF NOT EXISTS public.v8_draw_references (
 round_no integer PRIMARY KEY CHECK (round_no BETWEEN 1 AND 9999),
 numbers integer[] NOT NULL CHECK (cardinality(numbers)=6),
 bonus integer NOT NULL CHECK (bonus BETWEEN 1 AND 45),
 source_url text NOT NULL,
 confirmed_at timestamptz NOT NULL,
 migrated_from text NOT NULL DEFAULT 'V7_LEGACY_DRAW_REFERENCE' CHECK (migrated_from='V7_LEGACY_DRAW_REFERENCE'),
 independently_source_verified boolean NOT NULL DEFAULT false CHECK (independently_source_verified=false),
 migrated_at timestamptz NOT NULL DEFAULT now()
);
ALTER TABLE public.v8_draw_references ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE public.v8_draw_references FROM PUBLIC, anon, authenticated;
GRANT SELECT ON TABLE public.v8_draw_references TO service_role;
INSERT INTO public.v8_draw_references (round_no,numbers,bonus,source_url,confirmed_at)
SELECT round_no,numbers,bonus,source_url,confirmed_at FROM public.v7_official_draws
ON CONFLICT (round_no) DO NOTHING;
COMMENT ON TABLE public.v8_draw_references IS 'Read-only migrated V7-registered draw references for V3/V8 continuity; not independent verification of source.';
