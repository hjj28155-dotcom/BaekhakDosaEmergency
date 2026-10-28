# 2026-10-29 KST V8.0.6 통제실장 원문분석 / 항해의자유 배포판

선장 지시: 통제실장방에 원문분석을 넣고 항해의자유방은 배포판을 넣는다.

기준 V8.0.5 / code805. 수정 V8.0.6-DIRECTOR-ANALYSIS-DISTRIBUTION / code806. V8 기존 package/signing 유지.

- 통제실장 방 첫 화면에 원문분석 버튼 추가. 기존 내장 선장R46/통제실장R1 분석 화면 진입과 결과보고서 기능 재사용. 분석 화면 제목을 통제실장 원문분석으로 변경하고 돌아가기 동작도 통제실장 방으로 연결. engine/DB/분석 원본 변경 없음.
- 홈 항해의자유 카드의 배포판 열기는 별도 배포판 관리 화면으로 연결. 연구분석 화면을 직접 열지 않음. 기존 기록 보기 대신 드라이브 입구 배치. 연구기록은 원문분석 안의 기존 자료실로 계속 조회.
- native bridge는 고정 패키지 com.navigator.freedom.v3final의 설치상태/버전을 확인하고 해당 설치 앱만 실행 요청. 임의 패키지/URL을 JS에서 받지 않음. Android11+ queries에 정확한 배포판 패키지 추가. V3 R60 com.baekhak.dosa.f6 및 V8 kr.hanghae.crewmeeting.compact와 분리.
- 공식 보존 공급 링크: https://drive.google.com/file/d/1ULoKMeNFSOWDebMfY4tFvgyiK4EDxaJ-/view?usp=drivesdk . 메타데이터 확인: HanghaeFreedom_V4.0.41_NO_MOVEICON_SIGNED.apk, 55,178,708 bytes. 기존 공식 링크 변경/교체 없음.
- 최신 확인 시험판: 배포판 브랜치 fix/freedom-v4051-drive-readable-bottom-home-20261029 @4eeff6e72999eab41ab3c00c017271baf63e82af 작업대장 확인. V4.0.51 /40051, 실제 단말 미검증. Drive1PpZeDrzyQK77zzy8qxVDpGcL_KuUSSwI /48,013,530 bytes 메타데이터 확인. 추가 안내 안에서 명시적으로 시험판으로 표시하며 정식 승격 안 함. 더 높은 설치버전을 공식40041로 내리지 않도록 안내.
- V3 자동연동 미완료, V7 서버 삭제 미실행 상태 유지. 원본/인증/잠금 삭제나 초기화 없음.

검증 예정: 360/390/412px에서 원문분석의 통제실장 진입/복귀, 배포판 화면 분리, mock native 설치/미설치 및 실행요청, 고정 Drive href, 기존 간소화 기능 회귀. 실제 폰 설치/다른 앱 실행은 CI 모의 브리지와 구분.
