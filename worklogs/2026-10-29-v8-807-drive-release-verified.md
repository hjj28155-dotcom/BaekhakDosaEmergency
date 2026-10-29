# V8.0.7 원문분석 자산 경로 긴급 수정 — 빌드 및 Drive 검증

날짜 2026-10-29 KST. 선장 보고: 대화방·V3 자동연동·원문분석 모두 미작동. 모든 배포 파일 Google Drive.

## 확정 원인과 소스 수정
- V8.0.6 APK 안에서 HTML이 요청한 3개 자산 파일명과 실제 포함 자산명이 서로 다름. 원문분석 DB-1과 R46 로드 실패 원인.
- HTML 수정: db1_rounds_1_1247.json / captain_engine_r46.html / liaison-blind-engine.mjs. commit bfb00142489a2c5646166da5bf348672a8993bd7.
- DB-1 1247개 회차의 source_text SHA-256 전부 일치.
- V8.0.7 versionCode807, package kr.hanghae.crewmeeting.compact, 기존 동일 서명 인증서 SHA256 e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be.
- CI 구형 잘못된 자산 경로를 기대하던 검사도 수정. 최종 수정 commit ad5781075f67cad6d0692059fbe763402bfcc98a.

## 검증 결과
- GitHub Actions build run 40809193042 **success**: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40809193042
- V3 source module test run 40809193122 **success**: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40809193122
- APK CI artifact 12869263779. 빌드된 APK 크기 6,596,196 bytes, SHA-256 e186da5fe6b66d2f97256e8226e5133fd6e1b640e1ba92c4906f8b1cb6311fd4.
- APK ZIP CRC pass; 3개 자산이 HTML에 실제 참조되고 APK에 실제 포함, 구 경로는 더 이상 참조하지 않음.
- Google Drive APK **직접 업로드 완료**: ID 1tqKKu2mA-f7fE6CdGoRedqg2PVXEd13i, https://drive.google.com/file/d/1tqKKu2mA-f7fE6CdGoRedqg2PVXEd13i/view?usp=drivesdk
- Drive 메타데이터 6,596,196 bytes, APK MIME 확인. Drive에서 다시 내려받은 APK SHA-256도 e186da5fe6b66d2f97256e8226e5133fd6e1b640e1ba92c4906f8b1cb6311fd4 일치.
- Drive CI artifact ZIP (별도 개발보존): ID 1HYwhgqPpFFO8mHNpI1dgvp3_dPi1d9B6.

## 현재도 미완료
- V8.0.7 휴대전화 실제 설치/원문분석 R46 실행/PNG 저장은 미검증. 정식 승격하지 않음.
- 통제실장 R1은 검증된 V3/도사 사전원본 없으면 정상 결과를 만들지 않음. 파일명 수정만으로 독립 사전원본 확보된 것은 아님.
- ChatGPT 원본 대화방은 URL 열기 기능이며 자동 세션 메시지 동기화 아님. 권한/커넥터를 통한 실제 연결 별도 필요.
- V3 R60 자동송신, 단말 인증, 동일서명 V3 네이티브 호스트, V8 영수증 검증 아직 미완료.
- 기존 DB/최초잠금/V7 서버원본/서명키 삭제·변경 없음.
