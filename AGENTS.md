# AGENTS.md — Words 백엔드 분석 및 작업 가이드

## 1. 프로젝트 개요

`words-back`는 영어 단어 학습 서비스의 Spring Boot 백엔드다. Java 21, Spring Boot 3.4.5, Spring Web, Spring Data JPA, MySQL 드라이버를 사용한다.

프론트엔드 `words-front`는 Vite dev server의 `/api` 프록시를 통해 이 백엔드와 통신한다.

## 2. 주요 도메인

### 단어 데이터

- `TepsWord`
  - 일반 TEPS 단어 테이블 `teps_words`에 매핑된다.
  - `seq`, `word`, `meaning`, `description`, `note` 등을 가진다.
  - 품사 값이 없을 수 있으므로 API DTO 변환 시 `partOfSpeech`는 빈 문자열로 내려간다.

- `ConsulTepsWord`
  - 컨설텝스 단어 테이블 `consulteps_words`에 매핑된다.
  - `seq`, `word`, `partOfSpeech`, `meaning` 복합 키를 사용한다.

### 학습 진행 데이터

- `WordBookmark`
  - 즐겨찾기 단어를 저장한다.
  - `word_type`, `seq`, `word`, `part_of_speech`, `meaning` 조합을 unique key로 사용한다.

- `WordWrongAnswer`
  - 오답 단어와 오답 횟수를 저장한다.
  - `wrongCount`, `lastWrongAt`을 통해 오답 우선 복습에 활용한다.

## 3. 주요 API

### 학습 단어 API

- `GET /api/words/random?type=concepts|regular&partOfSpeech=n.`
  - 랜덤 단어를 하나 반환한다.
  - `type=regular`면 일반 TEPS 단어에서 랜덤 조회한다.
  - `type=concepts`면 컨설텝스 단어에서 랜덤 조회한다.
  - 컨설텝스 단어는 `partOfSpeech` 필터를 적용할 수 있다.

- `GET /api/words/range?type=concepts|regular&startSeq=1&endSeq=20`
  - seq 범위의 단어 목록을 반환한다.
  - 이번 브랜치에서 `type` 파라미터를 추가해 일반/컨설텝스 단어를 같은 DTO로 반환하도록 정리했다.
  - 한 번에 너무 큰 범위를 요청하지 않도록 최대 200개로 제한한다.

- `GET /api/words/quiz-choices?type=concepts|regular&seq=1&partOfSpeech=n.&limit=8`
  - 객관식 퀴즈 보기 후보를 반환한다.
  - 기존 프론트의 `seq ± 250` 클라이언트 필터링 대신 서버에서 후보를 제한해 내려준다.
  - 컨설텝스 단어는 동일 품사 후보를 우선 사용한다.
  - `limit`은 2~20 사이로 제한한다.

### 진행 상태 API

- `GET /api/words/progress?...`
  - 현재 단어의 즐겨찾기 여부와 오답 횟수를 반환한다.

- `POST /api/words/bookmarks`
  - 즐겨찾기에 저장한다.

- `DELETE /api/words/bookmarks?...`
  - 즐겨찾기에서 제거한다.

- `GET /api/words/bookmarks`
  - 최근 즐겨찾기 100개를 반환한다.

- `POST /api/words/wrongs`
  - 오답 횟수를 증가시킨다.

- `GET /api/words/wrongs`
  - 최근 오답 100개를 반환한다.

## 4. 이번 브랜치에서 반영한 개선

브랜치: `feature/mobile-study-ux-service-polish`

### API DTO 정리

- `/api/words` 계열 조회 API가 `ApiWordDto`를 반환하도록 개선했다.
- 프론트는 컨설텝스/일반 단어를 같은 `Word` 타입으로 다루기 때문에 API 응답이 통일될수록 방어 코드가 줄어든다.

### 퀴즈 보기 후보 API 추가

- `GET /api/words/quiz-choices`를 추가했다.
- 프론트가 넓은 범위를 가져와 클라이언트에서 보기 후보를 만드는 구조를 개선했다.
- 컨설텝스 단어는 `seq`가 다른 동일 품사 후보를 랜덤으로 조회한다.
- 일반 TEPS 단어는 `seq`가 다른 후보를 랜덤으로 조회한다.

### 일반 단어 진행 상태 저장 보완

- 일반 단어는 `partOfSpeech`가 비어 있을 수 있다.
- `WordProgressService`에서 빈 품사를 `-`로 정규화해 즐겨찾기/오답 저장/조회 시 필수 값 검증에 걸리지 않도록 했다.

### 범위 조회 보호

- `/api/words/range`의 요청 범위를 최대 200개로 제한했다.
- 모바일 프론트에서 실수로 큰 범위를 요청해도 DB/API 부하가 과도하게 커지지 않도록 했다.

## 5. 남아 있는 개선 후보

1. 사용자 분리
   - 현재 즐겨찾기/오답은 사용자 계정이나 deviceId 없이 전역 테이블처럼 동작한다.
   - 실제 서비스화 시 로그인 또는 익명 deviceId 기반 분리가 필요하다.

2. 학습 이력 테이블 추가
   - `seenCount`, `correctCount`, `wrongCount`, `lastSeenAt`, `lastCorrectAt`, `lastWrongAt`을 통합 관리하는 테이블이 있으면 통계/복습 알고리즘 구현이 쉬워진다.

3. 정답 처리 API 추가
   - 현재는 오답 저장만 서버에 반영된다.
   - 객관식 정답도 서버에 저장하면 장기 정답률과 간격 반복 복습을 만들 수 있다.

4. 예외 응답 표준화
   - `IllegalArgumentException` 발생 시 현재는 기본 Spring 오류 응답에 의존한다.
   - `@RestControllerAdvice`로 오류 포맷을 통일하는 것이 좋다.

5. 테스트 추가
   - `ConsulTepsWordController`의 `/random`, `/range`, `/quiz-choices` 테스트
   - `WordProgressService`의 빈 품사 정규화 테스트
   - repository native query smoke test

## 6. 실행 방법

```bash
./mvnw spring-boot:run
```

또는 Maven Wrapper가 없는 환경이라면 다음 명령을 사용한다.

```bash
mvn spring-boot:run
```

## 7. 검증 체크리스트

- `./mvnw test`
- `GET /api/words/random?type=concepts`
- `GET /api/words/random?type=regular`
- `GET /api/words/range?type=concepts&startSeq=1&endSeq=20`
- `GET /api/words/range?type=regular&startSeq=1&endSeq=20`
- `GET /api/words/quiz-choices?type=concepts&seq=1&partOfSpeech=n.&limit=8`
- `GET /api/words/quiz-choices?type=regular&seq=1&limit=8`
- 일반 단어 즐겨찾기 저장 시 빈 품사 때문에 실패하지 않는지 확인
- 일반 단어 오답 저장 시 빈 품사 때문에 실패하지 않는지 확인

## 8. 작업 시 주의사항

- 일반 TEPS 단어에는 품사 컬럼이 없으므로 DTO 변환과 progress 저장 시 빈 품사 방어가 필요하다.
- `ORDER BY RAND()`는 데이터가 커질수록 느려질 수 있다. 단어 수가 크게 늘어나면 랜덤 seq 기반 조회 또는 샘플링 테이블 방식으로 개선한다.
- progress 테이블의 unique key가 `meaning`까지 포함하므로 뜻 문구가 바뀌면 기존 학습 기록과 분리될 수 있다. 장기적으로는 단어 데이터의 안정적인 ID를 별도로 두는 것이 좋다.
- API 변경 시 프론트 배포 순서와의 호환성을 고려해 가능하면 폴백 경로를 유지한다.
