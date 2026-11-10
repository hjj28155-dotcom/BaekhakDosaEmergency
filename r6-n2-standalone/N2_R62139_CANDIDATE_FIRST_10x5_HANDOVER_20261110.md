# ⚓ N2 6.21.39 — before 100-round analysis: freeze 8–10 candidate numbers per room, then form five combinations

Captain-directed pre-analysis rule: each of the four rooms selects **8–10 original candidate numbers first** (current implementation freezes 10 per room), then produces precisely five 6-number games **only using its frozen candidates**. Save pre-combination candidates separately, show them at TOP of the four-room screen, score panel and PNG. Candidate-pool 6/6 and game-level 6/6 must not be confused.

Full Korean handover: https://drive.google.com/file/d/1MfLBNtww5di5QJEiy8GEi3sMiULzSzio/view

## What changed
- 6.21.38 pilot was PNG showing **union of final-game numbers** only; this is NOT source candidates.
- 6.21.39 engine now selects `rank(room).slice(0,10)` before combinations; combinations are a deterministic choice of five six-number subsets from C(10,6), with candidate coverage and cross-room duplicate-game guards.
- Additive storage fields: `candidate_numbers`, `candidate_source`, `candidate_frozen_before_games` in newly generated room records and IndexedDB payload.
- In-memory four-room UI and score-card show true stored pre-combination candidates. PNG 1280×1160 does too.
- Old records without candidate fields explicitly show `원본 미기록`; never invent pre-combination candidates based on five games or subsequent winning results.

## Verified
- JS syntax 32/32 PASS.
- Chromium actual engine for rounds 100, 1249, 1250, 1251: four rooms × ten frozen candidates, five six-number sets per room, zero out-of-pool numbers, five unique games per room, all ten candidate numbers covered.
- Chromium synthetic-record score panel: 4 original-candidate cards and 4 legacy-record missing-source notices.
- Actual PNG rendered 1280×1160 with 10 candidates at top of each room (synthetic fixture, not device history).
- ZIP CRC, v2 content signature, Android official apksigner --min-sdk-version 24 PASS; cert equals existing N2 permanent key.
- **Pilot-only APK**: size 2,282,523 bytes; SHA256 `38dff0c9b26d0f302242a9c082952c43ea11a43b7c8cb1b3646b5f115dda580c`; version 6.21.39, code 621939, package com.hanghae.fold8n2app.
- Owner Drive APK: https://drive.google.com/file/d/1yOMcNVQaeDGb3mJVt6icZJYu_e0NYtVC/view
- Live `main:r6-n2-standalone/update.json` **left at 621935**. No operation on original R6 / Lotto645 / V3 / V7.

## NOT verified / next action
- Full Android handset in-place update, IndexedDB durable write/readback and previous-record continuity are not yet tested.
- **Do not begin 100-round performance comparison until these structural device checks are complete**. Current implementation chooses exactly 10, within the captain's 8–10 range; other pool-size policies may require future A/B validation.
- No claim about prediction accuracy improvement; no hindsight target draw numbers fed into candidates.
- Keep permanent signing key/password in private separate Google Drive folders; no delete/reset.
