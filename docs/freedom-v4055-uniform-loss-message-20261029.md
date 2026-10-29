# ⚓ 항행의자유 V4.0.55 — 5회차 낙첨 음성 통일 / 드라이브 업데이트 인계

**2026-10-29 KST**, 대상은 항행의자유 V4 일반 배포판 `com.navigator.freedom.v3final`이며, V3 항해사앱과 V7 중앙통제실과 별개입니다.

## 사용자 지시 / 확인된 원인
- 선장 보고: “첫회차와나머지차멘트가 다르게 나와요...”
- V4.0.54 `v4054LossVoice(round,outcome,researchOnly)`가 일반 저장 20조합에는 `아쉽습니다. 제1244회는 낙첨입니다. 다음 회차에는 더 좋은 결과를 기대하겠습니다.`, 사후 연구 20조합에는 `아쉽습니다. 제1245회 사후 연구 20조합은 3개 이상 일치한 조합이 없습니다. 다음 회차를 기대하겠습니다.`로 서로 다른 음성 문자열을 사용한 것이 원인.
- 앞선 선장 확정 지시: 팡파레·축하 음성 없이 **낙첨멘트만**. 회차 이동 시 동일한 낙첨 문장 유지. V4 APK는 구글드라이브 업데이트 폴더에서 제공, 설치 중 중복 APK 다운로드를 줄일 것.

## 격리 소스
- 브랜치 `fix/freedom-v4055-uniform-loss-announcement-20261029`.
- 실제 코드: `build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/index.html`.
- 모든 낙첨 회차의 **음성 문장 템플릿을 단일 문자열로 교체**:
  `아쉽습니다. 제{회차}회는 낙첨입니다. 다음 회차에는 더 좋은 결과를 기대하겠습니다.`.
- `researchOnly` 조건은 화면 속 ‘추첨 후 비교 자료 · 실제 구매 성적 아님’ 출처표시에만 남김. 원본/사후 연구 구분, DB-1, 20조합, 당첨 채점, 보너스, 최초잠금, 승인정보, 홈, 기존 Android native TTS 및 브라우저 TTS 경로는 변경하지 않음.
- `versionCode 40055`, `versionName 4.0.55-UNIFORM-LOSS-VOICE`.
- `.github/scripts/verify_freedom_v4049_effects.cjs`: 일반/연구/1244~1248 5회차 각각 음성 문장 **정확한 문자열 동등성** 검사 추가. 발표 전/기록 없음 침묵과 당첨 등수 화면표시, 홈 취소, 다시듣기, 데이터 불변 유지.
- 별도 CI 워크플로 `.github/workflows/build-freedom-v4055-uniform-loss-signer-test.yml` (원본 signing cache scope 빌드 브랜치), 고정 소스 커밋 `6e1695110910ec9b050ae2065b3c1eeea59a2c45`.

## 검증
- 인라인 JS 6개 구문 검사 통과.
- GitHub Actions **전체 성공** https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40804662779
- CI artifact ID **12867696966**, `Freedom_V4055_UNIFORM_LOSS_VOICE_OriginalSigned_TEST_ONLY`.
- 추출 APK `HanghaeFreedom_V4.0.55_UNIFORM_LOSS_VOICE_TEST_ONLY.apk`, **48,014,894 bytes**, SHA-256 `9dd5c6f2bc190715e8efead0acc1e6b1eb3e3b6f1a0ffa5101e3b1b43e47fb45`.
- GitHub CI SHA256.txt 재검산 일치, APK ZIP CRC 통과, 검증된 공식 서명 인증서 SHA256 `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00` 일치. 실제 APK `assets/index.html`에 낙첨 문장 오직 한 개 확인.
- 기기 실제 업그레이드/음성 재생/기존 기기 원본 보존은 **미검증**.

## 구글드라이브 업데이트
- V4 기존 시험 업데이트 폴더 (이미 연결됨): `https://drive.google.com/drive/folders/1VI4a3RZfBdNxfs6unkZzEIaeI4a4bnMY`.
- V4.0.54 APK가 기존 폴더에 있음을 Drive metadata로 확인: `1mdmX7G2Q3-wakF95Nh1Ihlpf4L1fWXJf`, APK 48,014,875 bytes. **기존 파일은 삭제·덮어쓰기하지 않음.**
- V4.0.55 새 APK는 런타임에 생성 및 검증 완료했지만 **아직 Drive 업로드 성공/파일 ID는 확인하지 못함**. 따라서 V4.55의 드라이브 URL이나 공식 최신버전 승격을 성공으로 보고하지 말 것.
- Google Drive connector의 `upload_file`은 서버 파일 URI를 요구하며 로컬 컨테이너 경로를 직접 허용하지 않음. Library를 이용한 업로드 시 저장공간 한도 오류가 발생한 기록이 있어 새 원본 삭제나 인증정보 공유를 통한 우회는 하지 않음.
- 다음 작업: Drive에서 실제 업로드 성공/메타데이터 확인, V4 신규설치 URL과 업데이트 URL 분리 유지. 이후 선장 실기기 시연 확인을 거쳐서만 정식 업데이트 목록 승격.
- Google Drive 경유 설치라도 Android는 설치 때 임시 저장공간을 사용함. 구 APK는 설치 완료 후 기기 내 다운로드 폴더에서 사용자 확인 후 삭제 가능하지만 앱 자체의 ‘데이터 삭제’나 ‘앱 삭제’는 절대 수행하지 말 것.

## V4 다음 단계
- 선장 별도 후속 지시: V4 완료 후 기존 **V3 항해사앱**을 중앙통제실형 새 홈으로 **전면 재설계**. **V7 중앙통제실은 별개** 보존. V4 설치/작동 완료 이전엔 V3/V7 원본에 손대지 않음.
