# ⚓ 선장 지시: V7 전체 삭제 범위 및 공유서버 의존성 감사

- 접수일: 2026-10-29 KST
- 선장 지시: “V7 삭제하세요” → 범위 확인 질문(홈 카드만 vs V7 앱·서버 자료까지) → “모두요”.
- 따라서 **V7 UI·앱·V7 고유 서버 기능 및 원본 자료 전체가 삭제 대상**.
- **이 문서는 사전 감사 기록이며 삭제·초기화·배포를 실행했다는 증거가 아님**.
- 프로젝트 공유 보호: V3 R60, V4, V8 중앙통제실, 선장/항해사/통제실장 대화방 및 타 앱 원본은 삭제 지시의 대상이 아님.

## 2026-10-29 읽기 전용 검증
- Supabase 프로젝트 `jyqkohpeowkhuaohlezc` 이름 `BaekhakDosa-Approval`, 상태 ACTIVE_HEALTHY. **V7 전용이 아닌 V3/V4/V8와 공유하는 서버**.
- `v7_documents` 32행, `v7_official_draws` 17행, `v7_archive_folders` 42행, `v7_archive_entries` 6행, `v7_first_reports` 16행, `v7_grading_receipts` 16행, `v7_after_report_jobs` 128행, `v7_filename_assignments` 219행 등 V7 네임스페이스가 존재. 단, 이름이 v7인 테이블 안에도 다른 앱의 참조가 섞일 수 있으므로 테이블명만 보고 통째 삭제하면 안 됨.
- 동일 프로젝트의 비-V7 공유 자료: `v4_library_assets` 220행, `v6_room_assets` 220행, `v6_round_sources` 1247행, `crew_meeting_messages` 6행, `crew_meeting_original_links` 3행, `crew_engine_result_receipts` 1행. **공유 프로젝트 전체 삭제 금지**.
- `v7_jotasu_transfer_queue`은 RLS disabled 보안 경고 존재. 기존 데이터는 0행. 접근 통제 별도 검토 필요.
- V8 standalone HTML에 `data-app-card="v7"`, `openInstalledV7`, `originalController`(V7 전담 표시), 누적DB `data-room="v7"`, 결과후 `data-after-seven="v7"`, V7 서버 보고서 및 통합 자료방 의존성이 존재.
- V3 R60 → V7 항해사방 결과전 인계는 새 receiver·ACK 미구현. V7 고유 서비스 삭제 시 V3 전송 대상과 V8 조회 구조 재설계가 필요.
- Android 기기에 설치된 V7 앱은 서버/소스 변경만으로 원격 제거되지 않음. 사용자가 기기에서 제거해야 함.

## 안전한 순서
1. V7만의 서버·스토리지·Edge Function·APK 배포 경로·앱별 참조를 V3/V4/V8 공유 의존성과 분리한 목록 작성.
2. 선장 자료의 검증 가능한 **외부 백업** 생성, 파일/행 수·SHA-256 확인 및 복구 절차 기록.
3. V8의 V7 의존 기능(누적DB 7방/35조합, 자료실·결과전 접수)을 제거 또는 V8 독립 방식으로 교체하고 회귀 테스트.
4. V8 UI의 V7 카드·원본 링크·V7 기록 조회 제거, V8 새 APK 빌드·검증·배포.
5. **공유 프로젝트를 보존**하면서 V7 고유 자료·서버 기능만 순차 제거하고 재조회 검증.
6. 선장 휴대전화의 V7 설치 앱은 직접 제거 안내.

## 현재 실행 여부
- 이번 감사에서 **Supabase 데이터 삭제 0, 테이블 삭제 0, Edge Function 삭제 0, 스토리지 삭제 0, 앱 원격 제거 0**.
- 읽기 전용 목록 확인만 완료. 원본/DB 삭제는 외부 백업과 의존성 분리 전까지 실행하지 않음.
