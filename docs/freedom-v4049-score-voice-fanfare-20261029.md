# V4.0.49 시험판 작업 기록 · 2026-10-29 KST

대상: 항행의자유 V4 일반 배포판 (`com.navigator.freedom.v3final`). V3/V7 별도 보존.
기준: `fix/freedom-v4049-score-voice-fanfare-20261029`, 통합 기준 HEAD `e2c76c7dc95ba135978e90df9f64f861bb95c677`. 작업 중 추가된 다시듣기·연구실적 제외·회귀검사 수정을 보존. 별도 통합 브랜치에서 개선하여 동시 작업을 덮어쓰지 않음.

## 수정
- versionCode 40049, versionName 4.0.49-EMERGENCY-SCORE-VOICE, 시험용 APK명 분리.
- 원래 showFanfare/showComfort와 네이티브 음성/팡파레, 브라우저 대체음을 20조합 채점 화면에 연결.
- 검증된 당첨번호와 실제 표시 20조합이 있을 때만 재생. 5+보너스 2등 포함 최고 등수 음성 일치.
- 같은 화면 결과 재방문 시 세션 내 중복 재생 방지. 추첨 전/20조합 없음은 침묵.
- 사후 연구는 기존 화면 표시를 유지하고 구매 당첨 축하/낙선 음성 대상에서 제외.
- 홈/회차/다른 모달 이동/오버레이 터치 시 대기 팝업·지연 음성·WebAudio 취소.
- 네이티브 예약 음성 전용 Handler로 분리하여 시작 영상 등 기존 Handler 작업과 독립 취소. TTS 준비 전 대기 문장도 제거.
- 오버레이가 최상단 모달 위에 표시되고 좁은 화면 카드 폭 제한. 20조합 표와 홈 버튼 기존 배치 유지.

## 검증
- 로컬 Node VM 회귀검사 verify_freedom_v4046_emergency.cjs 통과: 1247개 전체 원문 분석, 당회차 70/30 및 없음 0/100, 4×5 조합, 최근 5회차, 기존 잠금·연구 기록 보존, JS 문법.
- verify_freedom_v4049_effects.cjs 통과: 1~5등/보너스 2등, 0~2개 낙선 안내, 네이티브/브라우저 음성·팡파레 경로, 연구 축하 제외, 재방문 방지, 미발표/원본없음 침묵, 이동 취소.
- 이 기록 시점 APK 빌드/서명 확인은 아직 미완료. 별도 CI 결과를 확인 후 추가 기록.
- 휴대전화 실제 설치, 작은 화면 20조합, 실제 스피커·TTS 동작은 미검증.

## 보존 및 다음 단계
DB-1·전체 원문·20조합·최초잠금·승인·V3/V7·공식 설치/업데이트 경로를 수정하거나 초기화하지 않음. 중복 재생 표시는 메모리에만 존재.
원본 인증서 SHA256 `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00`와 일치하는 시험용 APK만 생성. 기존 배포 링크 승격 금지.
다음: 동일 서명 CI 성공 확인 후 실제 휴대전화에서 데이터 보존·20조합/홈·음성 확인. 실기기 검증 전 정식 배포 금지.

실패 CI 40725189724 원인은 5개 숫자로 구성한 낙선 테스트 조합이 유효하지 않아 기존 보관 조합으로 대조된 것. 후속 e2c76c7에서 6개 유효 번호로 수정된 검사를 통합하여 통과 확인.

## APK CI 최종 확인 · 2026-10-29 01:28 KST
- 검증 소스 커밋: 7cf82d3ef0cae2636894d8691fb89a6fc4b1d79d.
- GitHub Actions: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40725810068 — 모든 단계 success.
- 원본 인증서 일치, 전체 회귀검사, Android 빌드, versionCode 40049/패키지 일치, APK 내부 소스·CSS 바이트 일치 확인 성공.
- 시험용 아티팩트: Freedom_V4049_AUDIO_CANCEL_OriginalSigned_TEST_ONLY, ID 12831429597.
- 다운로드: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40725810068/artifacts/12831429597
- ZIP 아티팩트 digest (APK 자체 digest와 구분): sha256:df0b7dbd96139619fb331e1b17080655aac3093fbdaa9610871113dff22bcb5f.
- PR: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/pull/5. 동시 작업의 문서/다시듣기/연구 분리 보존.
- 휴대전화 실제 설치·승인/DB 유지·한 화면 20조합·실제 음성·홈 이동은 미검증. 공식 신규설치·업데이트 URL은 변경하지 않음.
- 다음 작업 1순위: 이 시험용 APK로 휴대전화 실검증. 기존 앱 삭제/데이터 초기화 금지.
