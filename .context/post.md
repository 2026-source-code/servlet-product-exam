# HTTP POST 요청 구조 분석

## 폼 데이터
- **name**: 콜라
- **price**: 1000
- **qty**: 5개

## HTTP 요청 전체 구조

```
POST /product.do?cmd=insert-form HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
Content-Length: 30
Connection: keep-alive
Cache-Control: max-age=0
Origin: http://localhost:8080
Referer: http://localhost:8080/product.do?cmd=insert-form
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) ...

name=콜라&price=1000&qty=5
```

## HTTP 헤더 (Request Headers)

### 필수 헤더
```
POST /product.do?cmd=insert-form HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
Content-Length: 30
```

### 헤더 설명
- **POST**: HTTP 메서드
- **/product.do?cmd=insert-form**: 요청 URL (form의 action이 "#"이므로 현재 페이지 URL 사용)
- **HTTP/1.1**: HTTP 프로토콜 버전
- **Host**: 서버 호스트명과 포트
- **Content-Type**: 요청 본문의 데이터 형식 (`application/x-www-form-urlencoded`)
- **Content-Length**: 요청 본문의 바이트 길이

### 추가 헤더 (브라우저가 자동으로 추가)
```
Connection: keep-alive
Cache-Control: max-age=0
Origin: http://localhost:8080
Referer: http://localhost:8080/product.do?cmd=insert-form
User-Agent: Mozilla/5.0 ...
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
Accept-Encoding: gzip, deflate, br
```

## HTTP 바디 (Request Body)

### URL 인코딩된 형식
```
name=%EC%BD%9C%EB%9D%BC&price=1000&qty=5%EA%B0%9C
```

### 디코딩된 내용
```
name=콜라&price=1000&qty=5개
```

### 바디 구조 설명
- **형식**: `key1=value1&key2=value2&key3=value3`
- **구분자**: `&` (앰퍼샌드)로 각 필드 구분
- **인코딩**: 
  - 한글과 특수문자는 URL 인코딩됨
  - `콜라` → `%EC%BD%9C%EB%9D%BC` (UTF-8 인코딩)
  - `5개` → `5%EA%B0%9C` (UTF-8 인코딩)
  - 영문과 숫자는 그대로 전송

### 필드별 상세
| 필드명 | 원본 값 | 인코딩된 값 | 설명 |
|--------|---------|------------|------|
| name | 콜라 | %EC%BD%9C%EB%9D%BC | 상품명 (UTF-8 URL 인코딩) |
| price | 1000 | 1000 | 가격 (숫자는 인코딩 불필요) |
| qty | 5개 | 5%EA%B0%9C | 개수 (한글 포함, UTF-8 URL 인코딩) |

## 서버에서 받는 방법

### Java Servlet에서 파라미터 읽기
```java
String name = req.getParameter("name");        // "콜라"
String price = req.getParameter("price");      // "1000"
String qty = req.getParameter("qty");          // "5개"
```

### 주의사항
1. **Content-Type**: `application/x-www-form-urlencoded` 형식이어야 `getParameter()`로 읽을 수 있음
2. **인코딩**: 브라우저가 자동으로 UTF-8로 인코딩하여 전송
3. **Content-Length**: 실제 바디의 바이트 수 (인코딩된 문자열 길이)

## 실제 바이트 스트림 (16진수)

```
50 4F 53 54 20 2F 70 72 6F 64 75 63 74 2E 64 6F 3F 63 6D 64 3D 69 6E 73 65 72 74 2D 66 6F 72 6D 20 48 54 54 50 2F 31 2E 31 0D 0A
48 6F 73 74 3A 20 6C 6F 63 61 6C 68 6F 73 74 3A 38 30 38 30 0D 0A
43 6F 6E 74 65 6E 74 2D 54 79 70 65 3A 20 61 70 70 6C 69 63 61 74 69 6F 6E 2F 78 2D 77 77 77 2D 66 6F 72 6D 2D 75 72 6C 65 6E 63 6F 64 65 64 0D 0A
43 6F 6E 74 65 6E 74 2D 4C 65 6E 67 74 68 3A 20 33 30 0D 0A
0D 0A
6E 61 6D 65 3D 25 45 43 25 42 44 25 39 43 25 45 42 25 39 44 25 42 43 26 70 72 69 63 65 3D 31 30 30 30 26 71 74 79 3D 35 25 45 41 25 42 30 25 39 43
```

## 요약

1. **HTTP 메서드**: POST
2. **URL**: `/product.do?cmd=insert-form` (현재 페이지 URL)
3. **Content-Type**: `application/x-www-form-urlencoded`
4. **바디 형식**: `name=콜라&price=1000&qty=5개` (URL 인코딩됨)
5. **인코딩**: UTF-8 URL 인코딩
6. **서버 처리**: `req.getParameter()`로 각 필드 읽기 가능

