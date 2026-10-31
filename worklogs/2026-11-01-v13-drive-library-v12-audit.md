# ⚓ V13 R8.29 — Drive·라이브러리·V12 원본 합동 점검 (2026-11-01 KST)

## 선장 지시
V12 엔진/성적관리/누적 DB 원본을 GitHub뿐 아니라 Google Drive와 ChatGPT 라이브러리까지 확인. 기존 원본·DB·APK 삭제 또는 초기화 금지.

## 실사 자료
1. Google Drive V12_통합설계_20261029 폴더: https://drive.google.com/drive/folders/1lsoXcDIh0Cnk1A8gS1FVmg4mBlxC-EVr
  - V12_N13_R25_31225_성적정리_기존멘트복구_소스검증팩_20261030.zip: https://drive.google.com/file/d/1QZQzlbvUYnjCiRHneZm5-HXtugiHG8ZR/view
  - V12_N13_R25_31225_BUILD_VERIFICATION.json: https://drive.google.com/file/d/1B4pfzV4H1YrjsdiqZmY3qLVt9TO2gj_6/view
  - V12_N13_R24_31224_소스검증인계팩_20261030.zip: https://drive.google.com/file/d/1zMNWFaJ8ED2rU7xOYa3_sdFrOWe0J7Mc/view
  - V12_N13_R24_31224_BUILD_VERIFICATION.json: https://drive.google.com/file/d/1FQTSK7_Ky2TaGLxQU9mcXIJLhZH_gXR-/view
2. V12_N13_홈업데이트_최신APK_보관: https://drive.google.com/drive/folders/1UXl9PLd3LearytdcvfPw15_tlF4lKZWd (하위 '이전버전_보존' 폴더 확인)
3. V13 항행의자유 인계서: https://drive.google.com/file/d/13ARsxpRl41G2MLVLuBRSb_JTp7JROeQZ/view
4. V13 오이관 정정 인계서: https://drive.google.com/file/d/1brFSw7R74hyMloxpocvFkkqna9nLM_6D/view
5. 개인 라이브러리 원본: V3_mobile_layout_fixed_index.html (원문분석/실전구매/중앙통합/누적DB UI 소스 검색됨), 항해사앱_V3_0_85_빌드안내_20261003.txt (MASTER versionCode 30085, 패키지 com.baekhak.dosa.v3).

## 중요 판정
- V13의 항행의자유방에 V12 R62/V33 화면을 진짜 V3 원본으로 잘못 넣었다는 2026-10-31 **오이관 정정 인계** 확인. 이 소스를 진짜 V3 엔진으로 인증하지 말 것.
- V12 R25 검증 JSON: versionCode 31225, package com.baekhak.dosa.n13, 서명검증 가능하지만 실제 전화 테스트 미실시. changed_files AndroidManifest.xml, assets/v3_master/index.html. 음성 모의 7건 통과; 실제 성적 PNG/30조합 독립 엔진 전체 정상화 증거 아님.
- V12 R24 검증 JSON도 실제 Android 기기 설치시험 미실시이며 기존 DB 및 잠금 보존이라고 기록.
- V13 현재 UI 누적 DB view 3은 DB-1 원문 조회/동기화 패널. 선장이 요청하는 회차별 채점 누적 성적 DB와는 다른 기능이므로 V12 정본의 데이터 규약 분석이 필요.
- 1249 원문은 인계서에서 공식 V13 정본 등록·원문 6문장 정확성 검증 미완료로 표시. 임의 조합 생성 금지.

## 진행 상태
- Drive 실제 폴더 및 정본 후보 ZIP·검증 파일 위치 조회 완료.
- 라이브러리 V3 원본 후보 소스·빌드 인계서 검색 완료.
- R24/R25 BUILD_VERIFICATION JSON 실물 내용 확인.
- V12 보존 ZIP의 실제 내부 소스 해시/엔진 실행/회차별 누적 데이터는 아직 재현·직접 검증하지 못함.
- 원격 UI 14회차 유지. 새로운 화면 revision/APK/DB 쓰기 없음.

## 다음 순서
1. V12 R25 ZIP / V13 R8.15 원본 ZIP의 바이너리를 확보해 내부 파일 목록·해시·엔진 입출력 조사. 오이관 정정 문서와 대조.
2. V3 MASTER 인증 원본과 V33 구형 연구 엔진을 정확히 분리.
3. 독립 방별 실제 결과전 검증 후에만 5+5+15+5 PNG 수집 및 누적 DB 채점·보존 연결.
4. 실제 전화 화면에서 성적관리/누적DB 출력·PNG 저장 실측.
