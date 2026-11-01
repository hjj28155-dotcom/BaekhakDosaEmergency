# V13 R19 remote UI — actual release receipt

Date: 2026-11-01 KST.

## Changes and immutable baselines
- Production existing APK: V13 R8.29 / versionCode 31337 / `com.baekhak.dosa.v13r3preview` (NOT rebuilt, not replaced).
- Remote UI r18 original SHA256 `8138d1bcd4e68e9f89b015b84ca5c6072373fd51baf63c91684751d26849256b`.
- New remote UI r19 path: `releases/ui/v13-ui-r19.html`; SHA256 `4470b0d5b1308f7fb72a114cbf7ea20eee9f452738e3c1e461fb8f0fd7f51619`; Git blob SHA `ed4f226cee221caa3339852f51fba1b47793b39c`.
- Home button `원문분석 실행` calls the preserved real `runOriginalRawAnalysis` and four-room asynchronous dispatcher, never inventing missing engine results.
- Freedom V3 original `v12_v3_master/index.html` opens in a top-fullscreen mobile overlay; returns to V13, not replaced with separate V4.
- Previous remote r18 official draw fetching/score gating, outcome messages (축하/낙선), and fanfare are unchanged in source.
- Deployment mode remains `remote_ui_opt_in` and `minVersionCode=31337`.
- Main stored remote file commit: `6130d55c0fd9f4af0024e092db1c8f338017074b`.
- Main release manifest activation commit: `f496221105d7c8ed35bd3db9ffa70dfac879153d`; manifest SHA `df8f4c1954d721e4fe53c5b9493f77b678a63059`; re-read verified versionCode31337, remote rev19, and matching file blob.

## QA scope / limits
- Research-branch r19 static regression GitHub workflow `41274082830`: SUCCESS. Retained raw DB, V1 app update hooks, score, winner/loser/fanfare, independent four-room dispatcher, and inline JS syntax.
- Isolated mobile geometry QA at 320×700, 393×854, 768×880 checked R19 *added CSS and overlay* with genuine bundled V3 HTML and return, no page JS exceptions. It was **not** a full production r19 remote Android WebView test.
- No real phone install/update success was observed; no official draw/30 results were synthesized and no RESULT-BEFORE lock was attested.
- Existing R18 official functions remain, but captain/dosa/freedom **actual current-round verified engine pipelines** are still missing or incomplete; do not misstate 4-room official 30 combinations as complete.

## Status
Production release manifest = R8.29 code31337 / remote UI 19, published. Existing V3/V7/V12, APK DB1 owner originals, signing key and existing reports unchanged. Phone receipt pending captain device verification.
