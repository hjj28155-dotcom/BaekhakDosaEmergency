# V13 R8.64 홈 업데이트 실험 (운영 배포 금지)

정본: 선장이 업로드한 V13 R8.62 AndroidIDE 전체 소스 및 V13 R8.63 후보. **V14 미사용.**

- 동일 앱 ID `com.baekhak.dosa.v13r3preview`.
- `versionCode`: 31371→31372, `versionName`: V13 R8.64 (home update experiment).
- 기존 R8.63의 성적 관리 및 홈 업데이트 버튼 Java·HTML 소스는 바이트 동일. 원장 DB 저장키 변경 없음.
- 기존 R8.63 업데이트와 동일 서명키로 AndroidIDE 빌드해야 기존 설치본 위 업데이트 가능.
- **이 브랜치 JSON의 downloadUrl=null**은 아직 설치 가능한 검증된 R8.64 APK가 없다는 표시이며, **실제로 앱을 갱신할 수 있는 배포 링크가 아님**.
- Chromium 모의 네이티브 브리지 시험 4/4 통과, 홈 업데이트 정적 계약 18/18, 성적 관리 23개 회귀 항목 통과, PNG/DB/잠금 등 기존 로직 테스트 통과.
- Android SDK 미보유로 실제 Gradle 빌드, 서명된 R8.64 APK 및 실기기 설치·데이터보존 검증 미수행.
- Drive 소스 ZIP: https://drive.google.com/file/d/1webXNC3DuA4XpmQ4A9C4nvoiNMfjM7Jl/view
- Drive 증빙: https://drive.google.com/drive/folders/1VvS9pD9kMeyWtlP8JOrQbMF8hKsQj17d
- 운영 정보 `main/releases/v13-release.json`의 versionCode=31337 및 기존 UI 업데이트 r22 유지. 변경 금지.

## 실제 버튼 업데이트 배포 전 검증 관문
1. AndroidIDE가 이 ZIP에서 R8.64(31372) APK를 정상 생성했는지 확인.
2. 기존 설치 앱/R8.63 APK와 서명 SHA-256 동일, app ID 동일, versionCode 31372 확인.
3. Drive에 **APK 실파일** 업로드 및 APK SHA-256 검증.
4. 기존 설치본 위 설치 후 성적·원장·잠금 보존 확인.
5. 위 조건 통과 후에만 main 메타데이터 수정 및 실제 설치자 팝업 확인.

실패한 오래된 QA 파일 `r863_score_manager_coverage_test.cjs`은 이전 버전 31371 고정 테스트이므로 변경하지 않음. 버전만 보정한 R8.64 새 QA 파일에서 통과함.
