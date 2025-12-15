# Spring Transaction / JPA / QueryDSL Internals
--

## Domain Model
- Member
- Item
- ItemStock (`@Version`)
- Order
- OrderLine
- PaymentHistory (트랜잭션 분리 실험용)
  
도메인은 실험을 위한 최소 구조만 유지
---

## Spring Transaction Internals

Spring의 `@Transactional`이 실제로 어떻게 동작하는지를  
프록시, 전파 옵션, 로그를 기준으로 검증했다.

### Self-Invocation
- 같은 클래스 내부 호출 시 프록시를 거치지 않음
- `@Transactional`이 적용되지 않는 원인 재현
- `REQUIRES_NEW`, `MANDATORY`가 무시되는 케이스 확인

### Propagation Behavior
- REQUIRED 중첩 호출
  - 논리적으로는 2개, 실제 물리 트랜잭션은 1개
- REQUIRED + REQUIRES_NEW
  - 빈 분리 시 물리 트랜잭션 분리 확인
  - self-invocation 시 실패 케이스 재현
- REQUIRED + NESTED
  - savepoint 기반 부분 롤백 확인
  - flush 타이밍과의 충돌 가능성 검증
- NOT_SUPPORTED / MANDATORY
  - 트랜잭션 중단 및 강제 요구 동작 확인

### Rollback Rules
- checked / unchecked exception에 따른 기본 롤백 규칙
- `rollbackFor`, `noRollbackFor` 동작 차이 검증

### readOnly Transaction
- `readOnly = true`에서 dirty checking / flush 발생 여부 확인
- readOnly는 쓰기 차단이 아닌 **flush 최적화 힌트**임을 검증

### Logical vs Physical Transaction
- 트랜잭션 로그를 통해 논리/물리 트랜잭션 구분
- 실제 커밋 시점과 트랜잭션 경계 확인

---

## JPA Internals Verification

JPA의 추상적인 개념을 코드와 SQL 로그 기준으로 확인했다.

### 1. First-Level Cache
- 동일 트랜잭션 내 `find()` 호출 시 SQL 1회만 실행
- 동일 엔티티 인스턴스 보장 (`==` 비교 true)

### 2. Flush Timing
- commit 시 flush 발생
- JPQL 실행 시 flush 자동 트리거 확인

### 3. Dirty Checking
- 엔티티 값 변경 시 UPDATE SQL 발생 조건 검증
- 동일 값 재할당 시 UPDATE 미발생 케이스 확인

### 4. detach / clear / merge
- detach / clear 후 변경 사항 미반영 확인
- merge 시 새로운 영속 엔티티 생성 및 SQL 발생 흐름 검증

### 5. Update SQL 발생 조건
- 실제 값 변경 여부에 따른 UPDATE SQL 차이 확인

### 6. JPQL Flush Trigger
- JPQL 실행 전 영속성 컨텍스트 flush 발생 여부 확인

> 관련 서비스  
> `JpaInternalStep1Service`  
> `JpaInternalStep2Service`  
> `JpaInternalStep3Service`

---

## 📊 QueryDSL Verification

QueryDSL을 사용한 조회 로직을 기준으로  
실제 사용 시 마주치는 제약과 동작 방식을 검증했다.

### Basic Query
- 단일 조건 조회 (가격, 이름 contains)
- 복합 조건 조회 (카테고리 + 가격 범위)
- 연관관계 join 조회 (Order ↔ Member)

### Dynamic Condition
- 검색 조건 DTO 분리
- `BooleanExpression` 기반 조건 조립
- null-safe where 조건 구성
- 실제 사용되는 조건만 쿼리에 포함

### Projection
- Constructor Projection 사용
- DTO 매핑 제약 확인
  - 생성자 시그니처 불일치
  - 기본 생성자 필요 여부
  - 필드 접근 방식 차이

### Testing
- Repository 단위 테스트 구성
- 데이터 셋업 후 결과 1:1 검증
- 문자열 포함 / 범위 조건 등 명시적 assertion 사용

---

## Query Performance & Concurrency

### N+1 Problem
- LAZY 연관관계에서 N+1 문제 재현
- Fetch Join으로 N+1 제거
- `batch_size` 적용 시 IN 쿼리 기반 완화 동작 확인

### Optimistic Lock
- `@Version` 기반 동시성 충돌 재현
- 하나의 트랜잭션만 성공
- 나머지는 `ObjectOptimisticLockingFailureException` 발생
- 재시도 필요 구조 확인

### Pessimistic Lock
- `SELECT FOR UPDATE` 기반 row-level lock
- 후행 트랜잭션 락 대기 확인
- 직렬화 처리로 Lost Update 방지 확인

---



