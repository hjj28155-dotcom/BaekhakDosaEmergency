#!/usr/bin/env bash
# Read-only N2 R6.21.29 -> R6.21.34 overwrite-compatibility gate.
# Run from repository root with Android SDK build-tools installed.
set -euo pipefail
OLD="r6-n2-standalone/N2_Update_62129_UPDATE_BUTTON_FIXED.apk"
NEW="r6-n2-standalone/pilot/N2_R62134_OWNER_PILOT.apk"
OLD_SHA="35d0ff12e1c7db0ef514676b3bd6f4d9e007ea7658420aa53e7d0dde1e730878"
NEW_SHA="dae439ba238b55c4584ad0968f16843c0aaa29850015f177681f5ca2579fc121"
EXPECTED_CERT="81d75da8b4e1c82fbe8af27a0f8512236f2b1c2cdef5d7d015f695fb98893d04"
SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
SDK_BUILD="$(find "$SDK_ROOT/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n1)"
test -n "$SDK_BUILD" || { echo "FAIL: Android SDK build-tools missing"; exit 1; }
for file in "$OLD" "$NEW"; do
  test -f "$file" || { echo "FAIL: missing $file"; exit 1; }
done
printf '%s  %s\n%s  %s\n' "$OLD_SHA" "$OLD" "$NEW_SHA" "$NEW" | sha256sum -c
for pair in "$OLD:621929" "$NEW:621934"; do
  file="${pair%:*}"; code="${pair##*:}"
  output="$("$SDK_BUILD/apksigner" verify --verbose --print-certs "$file")"
  echo "$output" | grep -Eiq 'Verified using v2 scheme \(APK Signature Scheme v2\): true' || { echo "FAIL: invalid v2 signature: $file"; exit 1; }
  cert="$(printf '%s\n' "$output" | sed -nE 's/^Signer #1 certificate SHA-256 digest: ([0-9a-fA-F:]+).*$/\1/p' | tr -d ':' | tr '[:upper:]' '[:lower:]')"
  test "$cert" = "$EXPECTED_CERT" || { echo "FAIL: signer mismatch: $file ($cert)"; exit 1; }
  badging="$("$SDK_BUILD/aapt" dump badging "$file")"
  printf '%s\n' "$badging" | grep -Eq "^package: name='com.hanghae.fold8n2app' versionCode='$code'" || { echo "FAIL: package or version mismatch: $file"; exit 1; }
  echo "PASS: $file has expected signature, package, version"
done
echo "PASS: Static APK compatibility checks completed. Device installation and DB preservation are NOT verified."
