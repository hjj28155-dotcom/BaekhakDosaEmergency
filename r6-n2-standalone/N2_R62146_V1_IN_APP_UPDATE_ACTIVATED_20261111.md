# N2 R6.21.46 — V1식 앱 내부 업데이트 활성화 기록 (2026-11-11)

## 기존 상태
- 공식 배포 R6.21.41, 패키지 com.hanghae.fold8n2app; GitHub update.json v621941.
- 소유자 시험용 R6.21.46 Google Drive APK 존재; 휴대전화의 실설치 검증은 아직 없음.

## 변경 내용
- 드라이브 원본 APK를 읽어 GitHub binary blob으로 직접 등록: 804677e6ba8134a096d340b3a38e45c4859e0d8d
- 배포 브랜치: n2-r62146-publish-20261111 (commit 0d55b65a4cde3e3096947dd4d8a134e8517fb74c).
- 원격 GitHub tree 재조회 결과 경로·크기·blob SHA 확인: r6-n2-standalone/publish-621946/N2_R62146_FINAL_OWNER_TEST.apk, 2,387,483 bytes.
- 로컬 원본 계산: Git blob SHA1 804677e6ba8134a096d340b3a38e45c4859e0d8d, SHA256 9206dba864861277df414e5b6fa44e478af68d6349b95c6ac8c731a78f4b3785. GitHub tree의 blob SHA와 일치.
- 공식 manifest 업데이트: r6-n2-standalone/update.json 621941 → 621946, commit ec8e52327f8b5adce48d2fd9a66fdd56cfcf0f4d. 변경 후 fetch_file 재조회 확인.
- 실제 V1방식: NativeUpdate.check()로 새 버전 확인; 팝업 표시 후 NativeUpdate.download/install()로 사용자 수동 다운로드·설치 승인. 기존 앱 삭제·누적DB 삭제 금지.

## 공식 다운로드 주소
https://raw.githubusercontent.com/hjj28155-dotcom/BaekhakDosaEmergency/n2-r62146-publish-20261111/r6-n2-standalone/publish-621946/N2_R62146_FINAL_OWNER_TEST.apk

## 검증 결과
- GitHub blob/tree, 로컬 파일 해시 및 원격 manifest 재조회: 통과.
- 최종 사용자 기기에서 팝업 등장, 실제 APK 다운로드 및 Android 업데이트 설치, 원본 누적 DB 유지, 홈 UI·음성·종료: **아직 검증 전**.
- 이 실행 환경에서 raw.githubusercontent.com 직접 HTTP APK 파일 재다운로드가 제한되어 end-to-end 외부 다운로드 해시 검증은 수행하지 못함.

## 사후 확인
1. 기존 N2 앱 R6.21.45 등에서 실행 후 설정·업데이트 확인.
2. R6.21.46 팝업 및 사용자 다운로드 버튼 노출 확인.
3. Android가 삭제/데이터 초기화를 요구하면 설치 즉시 중단.
4. 설치 후 홈화면 수동회차·이전/다음·전체방·종료·프레젠테이션 및 DB 기록 존재 확인.

기존 공식 R6.21.41 APK 및 서명키, 누적DB, 백업 소스는 삭제하거나 변경하지 않았음.
