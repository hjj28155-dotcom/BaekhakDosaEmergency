# V13 원문분석 회차 이동 — R8.72 원격 적용 제약과 R9.13 Stage4 수정본

- 기준일: 2026-11-04 (KST)
- V3 항해사앱 / V7 중앙통제실 / V14·V15 별도. 본 기록은 V13만 해당.
- 사용자 지시: 원문분석 화면에 **[◀ 이전회차] [0000회차] [다음회차 ▶]** 추가. 원문·성적·기존 DB·잠금 불변.

## 1. 현재 배포 경로 확인과 정정
- V13 공개 GitHub 매니페스트 `releases/v13-release.json`은 원격 UI revision=24, URL `releases/ui/v13-ui-r24.html`, SHA256 `b81448290fea62823fc2472b4e54b888f95ac90350d865b9cf196c88f2f7f428`을 가리킴.
- R23→R24 HTML 변경은 내부 문서 revision 22→24 한 줄만. 12개 정적 검사 통과.
- Google Drive R8.72 APK `V13_R8_72_AndroidIDE_PROJECT-debug.apk`를 직접 받아 검증: 크기 17,016,583 bytes; SHA256 `e636d0c27ef232d6a4887ee8ca58ecca59709fa8a72187a000e3a280a3e0a933`; ZIP CRC PASS; GitHub 기존 R8.72 APK 배포 해시와 일치.
- APK `classes2.dex`는 `checkForUpdates`, `checkRemoteUiPatch`, `v13-ui-patch.json`을 포함하지만 `checkForUiUpdates`, `getUiRevision`은 포함하지 않음. `assets/index.html`에 `v13UiPatchBuildCss`가 있으며 제한된 선택자/스타일 속성만 허용한다. 원문분석 회차 버튼의 DOM 구조나 문자 변경은 **현재 R8.72 CSS 패치로 불가**.
- 따라서 **GitHub R24 HTML 게시 및 매니페스트 등록만으로 R8.72 설치 앱의 새 버튼 표시·원격 적용을 완료했다고 주장하지 않는다.** 이전 R23/R24 작업의 완료 범위를 이 기준으로 정정.
- 공개 CDN 직접 확인은 현재 환경 DNS/Cache miss로 미검증. 기기 UI 미검증.

## 2. 최신 검증 가능한 V13 개발 소스의 분리 수정
- Drive에 보관된 최신 확인 소스 후보 중 `V13_R913_STAGE4_451_ROUND_CAP_REMOVED_ANDROIDIDE_20261103.zip` (V13 R9.13, versionCode 31421)을 사용.
- 원본 ZIP SHA256 `9af1fef60c2753d208145dec481701b511105aaaa1c16cac9b1c3a2ef0b1c6d7`, 21,053,552 bytes, 325항목 ZIP CRC PASS.
- 새 작업 사본: `V13_R913_STAGE4_RAW_ROUND_NAV_STAGING_20261104.zip` — SHA256 `20906efa11cc68eb0ae1c4db8e99523aa85fb972f2ba270d2fca7312f4d2c280`, 21,062,727 bytes.
- 수정 파일 유일: `V13_R913_STAGE4_RELEASE/app/src/main/assets/index.html`. 원문분석 회차 바 1곳 치환 및 CSS 1곳 추가. 기존 `applyRound()`, `shift()`, `applyActiveRound()`, `getDb1Raw()` 변경하지 않음.
- 수정 ZIP과 원본 ZIP 내부 325개 항목 전부 비교: 위 HTML 1개 외 SHA256 동일. 원문 DB·V13 성적관리·엔진·결과전·서명 자산 변경하지 않음.
- inline JS `node --check` 2개 통과. HTML 정적 검증 9/9 통과.
- 원본 이동 함수를 Node VM에서 격리 실행: 1249→1248→1249, 1회차/9999회차 경계, 4자리 0124 수동 입력, 0000 오류 거절 **6/6 통과**.
- Playwright 실제 페이지 렌더링 시험은 로컬 HTTP/file URL 접근이 `ERR_BLOCKED_BY_ADMINISTRATOR`로 차단되어 미완료.
- 본 작업 사본 ZIP과 QA .md는 현재 대화 작업 공간에 저장. Google Drive 직접 업로드 미수행.

## 3. 안전성 및 다음 단계
- R9.13은 검증용 AndroidIDE 소스 ZIP이다. APK 빌드·기존 V13 서명키 호환성 검증·설치·실기기 회차 이동은 **미완료**. 새 설치 APK라고 부르지 않는다.
- 기존 V13 R8.72 APK/실기기 DB/잠금·성적 원장과 Drive R9.13 원본 ZIP은 변경하지 않음. 임의 새 서명키로 기존 설치자를 덮어쓰기 시도하지 않음.
- 다음 작업 1순위: 안전한 AndroidIDE/Gradle 환경과 동일 인증서의 V13 서명키를 확보하여 R9.13 수정본을 새 APK로 빌드·서명하고, 실제 설치폰에서 원문분석 회차 이동 + 기존 기록 보존을 확인할 것. 공개 배포는 검증 뒤 선장 승인 절차를 따른다.
