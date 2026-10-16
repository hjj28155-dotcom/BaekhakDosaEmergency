# 항행의자유 V6 — AndroidIDE 설치 및 업데이트 안내

## 0. 원칙
- V5 앱은 지우지 않습니다. V6는 별도 앱입니다.
- 승인하신 V6 홈 화면(전체방 + 1~5번방) 디자인과 배치를 유지합니다.
- ZIP은 **Android Studio/AndroidIDE용 소스 프로젝트**입니다. APK 파일이 아닙니다.
- 이 프로젝트는 Android WebView를 통해 배포된 V6 대시보드를 표시합니다. 인터넷 연결이 필요하며, 첫 실행 전후에 서버 연결 코드로 기기를 등록해야 업로드 등 서버 기능을 사용할 수 있습니다.
- '전체방 전달'은 **V6 서버 수신함** 전달입니다. 원본 ChatGPT 대화창 자동 입력이 아닙니다.
- Google Drive의 파일은 Android 시스템 파일 선택기에서 직접 골라 업로드할 수 있으나, Drive 전체 자동 동기화는 아직 구현되지 않았습니다.

## 1. AndroidIDE 빌드
1. ZIP을 **압축 풀기** 하세요. 결과 폴더 `V6_AndroidIDE_Project`가 나옵니다.
2. AndroidIDE를 실행하고 **Open Project(프로젝트 열기)**에서 이 폴더를 선택하세요.
3. 첫 실행 시 Gradle/Android SDK 구성요소를 다운로드할 수 있으므로 인터넷을 연결하세요.
4. 필요 버전: **JDK 17 / Android Gradle Plugin 8.7.3 / Gradle 8.10.2 / compileSdk 35 / minSdk 24**.
5. 프로젝트 동기화 후 `:app:assembleDebug` (Build APK 또는 debug 빌드)를 실행하세요.
6. 결과 예시: `app/build/outputs/apk/debug/app-debug.apk`.
7. 이 APK를 설치합니다. 앱 이름은 **항행의자유 V6**입니다.

## 2. 이후 업데이트 설치 조건 — 가장 중요
- 앱 식별자(applicationId)는 **`kr.hanghae.v6.centralcontrol`**로 고정합니다. 이 값을 바꾸지 마세요.
- 최초 설치가 AndroidIDE 빌드 APK라면, 다음 업데이트도 **같은 AndroidIDE의 같은 debug.keystore(서명키)**를 사용해야 기존 V6 위에 설치할 수 있습니다.
- AndroidIDE를 삭제하거나 휴대전화를 바꿀 경우 서명키가 바뀔 수 있습니다. **첫 빌드에 사용한 `~/.android/debug.keystore`를 안전하게 별도 백업하세요.** 비밀번호나 키 파일을 채팅/공개 저장소에 올리지 마세요.
- 신규 업데이트 프로젝트의 `app/build.gradle`에서 `versionCode` 값을 기존 값보다 증가시켜야 합니다. 첫 버전: **600001**. 다음 버전 예시: **600002**. `versionName`도 V6.0.1과 같이 증가시킵니다.
- 이후 업데이트 순서: **새 ZIP 압축 풀기 → 기존 서명키가 유지된 AndroidIDE에서 빌드 → 새 APK 설치(기존 V6 삭제하지 않음)**.
- GitHub Actions에서 따로 서명된 V6 APK가 이미 설치돼 있으면 AndroidIDE debug 키와 서명이 다를 수 있습니다. **그 APK 위로 바로 덮어쓸 수 있다고 보장할 수 없습니다.** 데이터/연결 상태를 백업하고 서명 일치 여부를 먼저 확인하세요.

## 3. 각 방 업로드 및 DB 누적
- V6 앱의 설정 → 기기 등록에서 기존 서버가 발급한 유효한 일회용 연결코드를 사용합니다.
- 1~5번방(또는 추가한 방) → 파일함 → 회차 입력 → 파일 선택 → 업로드
- 정상 등록 시 방 전용 폴더, V6 앱자료실, V6 누적DB에 같은 원본이 색인됩니다.
- 실구매 5조합은 정형 JSON (`document_type: V6_PURCHASE_5`, `stage: RESULT-BEFORE`)만 추출하고 **최종 승인 전**에는 후보 상태로 표시합니다.
- 파일별 최대 크기는 **10MB**입니다. V6 내부의 1회 업로드가 검증되기 전까지 작업 완료로 간주하지 마세요.

## 4. 자료 및 연결 범위 주의
- 서버 기존 V5 라이브러리에서 `IMPORTED`인 파일은 내려받을 수 있지만 `PENDING`은 메타데이터 목록만 존재할 수 있습니다.
- **V5 휴대전화 내부 누적 DB → V6 자동 이관**, **Drive 폴더 자동 양방향 연동**, **ChatGPT 개인 라이브러리 직접 자동 연동**은 현재 별도 구현 단계입니다.
- 서버가 점검 중이면 앱 화면 또는 일부 기능에 접근하지 못할 수 있습니다.

## 5. AndroidIDE 오류 발생 시 보내주실 것
- AndroidIDE **Build 출력의 첫 번째 오류** 화면
- 사용한 AndroidIDE 버전과 JDK/SDK 상태
- 압축 푼 뒤 프로젝트 폴더에서 `settings.gradle`, `gradlew`, `app`이 보이는지
- **개인정보, 서버 연결 토큰, 서명키 파일은 보내지 마세요.**
