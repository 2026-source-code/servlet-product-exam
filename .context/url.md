# 상품 관리 프로그램 URL 설계

## 기본 구조
- 모든 요청은 `product.do`로 들어오며, `cmd` 파라미터로 기능을 구분합니다.
- DispatcherServlet이 `*.do` 패턴으로 매핑되어 요청을 처리합니다.

## GET 요청 (화면 이동)

### 1. 상품 목록 화면
- **URL**: `localhost:8080/product.do?cmd=list`
- **기능**: 상품 목록을 조회하고 삭제 버튼을 제공하는 화면
- **템플릿**: `list.mustache`
- **설명**: product 테이블의 모든 상품을 조회하여 목록으로 표시

### 2. 상품 등록 화면
- **URL**: `localhost:8080/product.do?cmd=insert-form`
- **기능**: 새로운 상품을 등록하기 위한 폼 화면
- **템플릿**: `insert-form.mustache`
- **설명**: 상품명, 가격, 개수를 입력받는 등록 폼

### 3. 상품 상세보기 화면
- **URL**: `localhost:8080/product.do?cmd=detail&id={상품ID}`
- **기능**: 특정 상품의 상세 정보를 보여주는 화면
- **템플릿**: `detail.mustache`
- **파라미터**: 
  - `id`: 조회할 상품의 ID (필수)
- **설명**: 선택한 상품의 상세 정보(id, name, price, qty)를 표시

## POST 요청 (기능 수행)

### 4. 상품 등록
- **URL**: `localhost:8080/product.do?cmd=insert`
- **메서드**: POST
- **기능**: 새로운 상품을 데이터베이스에 저장
- **파라미터**:
  - `name`: 상품명 (필수)
  - `price`: 가격 (필수)
  - `qty`: 개수 (필수)
- **처리 후**: 상품 목록 화면(`cmd=list`)으로 리다이렉트

### 5. 상품 삭제
- **URL**: `localhost:8080/product.do?cmd=delete&id={상품ID}`
- **메서드**: POST
- **기능**: 지정한 상품을 데이터베이스에서 삭제
- **파라미터**:
  - `id`: 삭제할 상품의 ID (필수)
- **처리 후**: 상품 목록 화면(`cmd=list`)으로 리다이렉트

## URL 요약표

| 기능 | HTTP 메서드 | URL | 파라미터 | 설명 |
|------|------------|-----|----------|------|
| 상품 목록 | GET | `/product.do?cmd=list` | - | 상품 목록 조회 및 삭제 가능 |
| 상품 등록 폼 | GET | `/product.do?cmd=insert-form` | - | 상품 등록 입력 폼 |
| 상품 상세보기 | GET | `/product.do?cmd=detail` | `id` | 특정 상품 상세 정보 |
| 상품 등록 | POST | `/product.do?cmd=insert` | `name`, `price`, `qty` | 상품 데이터 저장 |
| 상품 삭제 | POST | `/product.do?cmd=delete` | `id` | 상품 데이터 삭제 |

## 데이터베이스 테이블 구조

### product 테이블
- `id`: 상품 ID (Primary Key, Auto Increment)
- `name`: 상품명 (VARCHAR)
- `price`: 가격 (INT 또는 DECIMAL)
- `qty`: 개수 (INT)

## 구현 참고사항

1. **DispatcherServlet**에서 `cmd` 파라미터를 읽어 적절한 처리 로직으로 분기
2. **GET 요청**: `doGet()` 메서드에서 처리, ViewResolver를 통해 템플릿 렌더링
3. **POST 요청**: `doPost()` 메서드에서 처리, DB 작업 후 리다이렉트
4. **DBConnection**: `DBConnection.getConnection()`을 통해 데이터베이스 연결
5. **리다이렉트**: POST 요청 처리 후 `resp.sendRedirect("product.do?cmd=list")` 사용

