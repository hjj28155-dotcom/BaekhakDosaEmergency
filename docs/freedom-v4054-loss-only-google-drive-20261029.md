# ⚓ 항행의자유 일반 배포판 V4.0.54 — 낙첨멘트 전용 · 구글드라이브 업데이트 인계서

작성 기준: 2026-10-29 KST

## 선장님 지시
- 선장님: "드라이브에 올려주세요. 그리고 멘트가 왜 그러죠. 낙첨멘트만 넣어주세요. 다운로드하면 폰저장용량이 많이 줄어드니 반드시 구글드라이브를 이용하여 업데이트 할수 있게해주세요."
- V3 항해사앱 / V7 중앙통제실과 혼동하지 않는 **V4 일반 배포판**: `com.navigator.freedom.v3final`.
- 앱 삭제/데이터 초기화 금지, 신규설치와 업데이트 경로 별개 관리.

## 코드 변경 및 테스트
- 전용 브랜치: `fix/freedom-v4054-only-loss-drive-update-20261029`.
- 검증된 실제 빌드 기준 소스 커밋: `8d5bf01c7781a4c6a7ca5d457015d1147e59a45a`.
- `versionCode=40054`, `versionName=4.0.54-LOSS-ONLY-DRIVE`.
- `v4054LossVoice` 하나로 **당첨 조합이 0개인 회차에만 짧은 낙첨멘트**. 기존 실적: "아쉽습니다. 제N회는 낙첨입니다. 다음 회차에는 더 좋은 결과를 기대하겠습니다."
- 추첨 후 사후 연구 20조합은 구분된 **짧은 사후 연구용 낙첨 안내**. 실제 구매 당첨으로 오인되는 표현 금지.
- 결과표의 1~5등 판정·2등 보너스·20조합·최근 5회·홈 버튼 유지. **당첨 조합이 있는 경우 자동 음성/팡파레 실행하지 않음**(선장 최신 "낙첨멘트만" 지시). 당첨번호 없는 경우/조합 원본 없는 경우 음성 없음.
- 낙첨 결과에서만 `🔊 다시듣기` 표시.
- 회차별 연속 이동·동일 멘트 중복 억제·홈 이동 시 음성 취소·Native TTS와 Web Speech fallback 회귀검사 적용.
- 기존 DB-1 1~1247회 원문, 저장된 20조합, 잠금, 승인 정보, V3/V7, 홈 UI, 신규설치 URL 변경 없음.

## 실제 빌드·서명·APK 무결성
- GitHub Actions **모든 단계 성공**: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40802956208
- 아티팩트 ZIP ID `12867171338`, 이름 `Freedom_V4054_LOSS_ONLY_DRIVE_OriginalSigned_TEST_ONLY`; 6개 해상도 렌더/DB-1/음성/원본 잠금/동일 서명/실제 APK 빌드/내부 파일 검사 통과.
- APK: `HANGHAE_FREEDOM_V4054_LOSS_ONLY_DRIVE_ORIGINAL_SIGNED_TEST_ONLY.apk`.
- APK 실제 크기 **48,014,875 bytes**, SHA-256 `ffd65e7d36486fe8f579b985d2a178a1e6764e991def23b6f2ef671c07e68429`.
- CI `SHA256.txt`와 추출 파일 일치, ZIP/APK CRC 문제 없음.
- `package.txt` = `com.navigator.freedom.v3final`, `versionCode=40054`. 실제 APK V2 인증서 SHA-256 = `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00` (기존 원본 서명과 동일).

## Google Drive 직접 APK 업로드 완료
- **기존 V4 업데이트 시험판 전용 폴더**:
  https://drive.google.com/drive/folders/1VI4a3RZfBdNxfs6unkZzEIaeI4a4bnMY
- 실제 APK 단독 파일: `HanghaeFreedom_V4.0.54_LOSS_ONLY_DRIVE_UPDATE_TEST.apk`.
- **업로드 성공 확인 Google Drive 파일 ID:** `1mdmX7G2Q3-wakF95Nh1Ihlpf4L1fWXJf`.
- **직접 열기 링크**: https://drive.google.com/file/d/1mdmX7G2Q3-wakF95Nh1Ihlpf4L1fWXJf/view?usp=drivesdk
- 새 파일 생성 뒤 폴더 이동 완료; Google Drive 파일 metadata = MIME `application/vnd.android.package-archive`, 48,014,875 bytes, parent_id `1VI4a3RZfBdNxfs6unkZzEIaeI4a4bnMY`.
- Google Drive에서 **실제 업로드된 APK를 다시 다운로드**해 로컬의 CI 검증 APK와 SHA-256 `ffd65e7d36486fe8f579b985d2a178a1e6764e991def23b6f2ef671c07e68429` 동일, CRC 통과.
- 부가 검증 ZIP `HanghaeFreedom_V4.0.54_LOSS_ONLY_검증자료_TEST_ONLY.zip`도 같은 폴더에 업로드(ID `1eWkO9AR_BekGELj3IZTh0ngtTPppKbGp`) — **설치에는 APK를 사용**, ZIP은 설치용 아님. 둘 다 기존 파일 삭제 없음.
- Google Drive 권한 검증: 새 APK는 **현재 사용자 계정 소유, 공유되지 않은 상태**. 선장님은 본인 동일 Google 계정으로 로그인하여 접근 가능. 다른 구글 계정/불특정 공개 사용자 접근은 미검증.
- V4 Android `MainActivity.openOfficialUpdateLink()`는 이미 Drive만 열게 되어 있음: `freedom-release-latest.json`이 설치 버전보다 낮거나 조회 실패 시 **위 V4 업데이트 폴더**로 이동, 아니면 검증된 Drive `apk_url`로 이동. 기존 GitHub Pages `freedom-release-latest.json`은 이전 `versionCode 40041`로 남아 있으며 **정식 배포에 V4.0.54를 승격하지 않음**.
- 사용자가 Google Drive에서 APK 설치 시 Android가 **임시 저장공간을 사용**할 수 있음. 드라이브 이용은 다운로드본의 불필요한 중복/보관을 줄이지만 로컬 다운로드 용량 0을 보장하지 않음.
- 기존 앱 삭제나 저장공간 초기화 없이 40053→40054 업데이트로 설치해야 함.

## 남은 일
1. 선장님 휴대전화에서 기존 V4를 삭제하지 않고 Drive 링크(동일 계정)에서 APK 열기/업데이트 성공 확인.
2. 회차별 **낙첨 시에만** 짧은 멘트, 당첨 조합 회차는 음성 없음, 드라이브 앱에서 여는 방법/용량 확인.
3. 사용자 요청/실기기 검증 후 필요 시 *V4 업데이트 메타 JSON만* 최신 Drive APK 주소로 승격하고 **신규설치 페이지는 별도 보존**; 공개 배포가 필요하면 Drive의 링크 공유 권한을 사용자가 별도로 확인해야 함.
4. V4 마무리 후에만 V3 항해사앱 중앙통제실형 홈 전면 변경을 진행하고 V7 중앙통제실은 별도 보존.
