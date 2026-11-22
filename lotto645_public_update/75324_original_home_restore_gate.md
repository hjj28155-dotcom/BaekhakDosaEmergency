# LOTTO645 V75324 original-home restoration (2026-11-22)

- User requested removal of the two introduced home launchers (round analysis and presentation) and return to the original 3-button home. Experimental test-settings panel also removed from home.
- Changed stage13 signed APK entries only: AndroidManifest.xml (75323 -> 75324), assets/index.html, assets/r476-presentation.js, assets/r477-presentation-candidates.js, assets/lotto645_v75322_stage8_backup_ui.js. No user database reset or original source deletion.
- Test APK: https://drive.google.com/file/d/1VVZxsTWEQiokcv6NKSszOteiLsEd1fLA/view
- SHA256 cd9633dfd056e05021c667272781f3d046f7ec2ee3ca12bf2b41715456a6956d; owner cert 94d01930adc6b370c487a437e03edf9d37c4dd8ec695e65979e1023d5e37711b.
- ZIP 138 entries intact, V2 signature digest PASS, 3 modified JS scripts passed node --check.
- BLOCKER: hidden authentication/backup UI means central reservation cannot be authorized through the default home; not functional production version. Physical device installation/UI/data preservation untested.
- PRODUCTION NO-GO; official release remains 75313.
