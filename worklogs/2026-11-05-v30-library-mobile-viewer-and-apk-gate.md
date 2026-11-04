# V30 자료실 · 휴대전화 핵심기록 열람 단계 (2026-11-05)

- 자료실 단계: **오프라인 열람 HTML 보관 및 Drive 목록 확인**, V30 Android 앱 내장/연동 **아직 미완료**
- 승인된 홈화면·엔진·데이터베이스·서명키·기존 앱은 수정하지 않음.
- Google Drive 파일: https://drive.google.com/file/d/1j_0tnJwHszXiJYc42FECL3vhGxE0R957/view
- Google Drive 폴더: https://drive.google.com/drive/folders/1jtHte883jAz8Rv_sYwFnZGwMqRZ_-z0Y
- 8개 옛 선장/도사 핵심 문서가 UTF-8 원문으로 포함된 독립 HTML, 파일별 내려받기 단추 제공. HTML **89,200 bytes**, SHA256 `bbff0bb14e002625274dd3f14a2df1996cfb3c1fb77d32da565d4f6f85004470`.
- 구조 테스트: 텍스트 키워드 및 다운로드 핸들러 포함 확인. 실기기·브라우저 렌더링 검증 미실시. Drive HTML 미리보기가 실행 HTML을 직접 표시하지 않을 수 있으므로 내려받아 Chrome 파일 열기 필요.
- 추가로 핵심 문서 ZIP **18,689 bytes**, Drive https://drive.google.com/file/d/15g-eX5ZodLaUDsZCitEgvLOf8j1t_OrA/view
- **V30 신규 APK는 이 대화 첨부 및 Drive 검색에서 확인되지 않음.** V3_R30 / V13 / V16 APK를 V30으로 재표기하지 않음.
- V30 버튼 계약 `integration/v30/button-and-update-contract.json`의 자료실 `binding:null`: 네이티브 앱 원본 확보와 WebView/Assets 연동이 선행되어야 한다.
- 선장 휴대전화에서 APK 실제 자료실 안의 목록 표시 및 문서 열람 검증 전에 라이브러리 원본을 삭제하지 않음.
- 사용자 요청: "이번 APK 자료실에 넣으시고 V30 진행". APK가 확인되면 정확한 파일을 동일 자료실에 보관하고 해시·패키지명·서명 검증을 시행한다.
