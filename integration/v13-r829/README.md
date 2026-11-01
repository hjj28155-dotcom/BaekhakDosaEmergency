# V13 R8.29 3-room integration staging
This is an isolated **runtime connection point**, not a complete engine transplant.
- captain / dosa / navigator: bind authenticated V12 Captain V10 original entrypoints and check exact source digest.
- freedom: preserve existing handler and delegate unchanged.
- No fallback tickets, no DB writes, no production asset replacement.
- Missing authenticated originals fail closed with V12_V10_ORIGINAL_NOT_BOUND.
- Integrate into actual Android assets only after V12 V10 source entrypoints are identified and end-to-end tested. Do not publish update manifest or APK before that.
