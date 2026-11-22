# LOTTO645 V75326 development gate — original 4-room engine benchmark (2026-11-23)

- Captain direction: original LOTTO645 home/menu never changed; 4 independent room candidate pools; no shared candidate 12-number pool.
- Verified original V75320 **already has distinct candidates per room**, V75325 changed candidate ranking and underperformed.
- Recomputed same fixed seed, walk-forward history only, including research-only restored draw 951.
- Rounds 1-800: V75320 4 matches=29, 3 matches=375; V75325 4 matches=23, 3 matches=339.
- Rounds 801-1250: V75320 4 matches=11, 3 matches=218; V75325 4 matches=8, 3 matches=194.
- All 1250 comparisons re-executed in four independently saved batches, zero test errors. Results are historical comparison, no evidence of predicted wins.
- Detailed source/tests JSON: https://drive.google.com/file/d/1AUy7ph84QkvugPMT1Em9HVv1sFNYEYwN/view
- **V75326 is a research gate, NOT an APK or improved engine**. Existing V75325 rollout rejected. Need develop and validate a candidate policy without making all four rooms share the same pool.
- Captain requested all phone data reset, but neither remote deletion nor local backup validation completed; NO user data deleted.
- Production release V75313 unchanged, NO-GO.
