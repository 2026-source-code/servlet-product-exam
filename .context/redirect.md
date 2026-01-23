# HTTP 리다이렉트 헤더 구조

## 코드 위치

```java
// DispatcherServlet.java (45-47줄)
String url = pc.insert(req, resp);  // "/product.do?cmd=list" 반환
resp.setStatus(302);
resp.setHeader("Location", url);
```

## 실제 HTTP 응답 헤더

### 전체 HTTP 응답 구조

```
HTTP/1.1 302 Found
Location: /product.do?cmd=list
Content-Length: 0
Date: Mon, 23 Jan 2026 10:30:45 GMT
Server: Apache-Coyote/1.1
Connection: close

(바디 없음)
```

## 헤더 상세 설명

### 1. Status Line (상태 라인)

```
HTTP/1.1 302 Found
```

- **HTTP/1.1**: HTTP 프로토콜 버전
- **302**: HTTP 상태 코드 (Found - 리다이렉트)
- **Found**: 상태 코드의 텍스트 설명

**HTTP 상태 코드 의미:**
- `302 Found`: 리소스가 일시적으로 다른 위치로 이동했음을 나타냄
- 브라우저는 이 응답을 받으면 자동으로 `Location` 헤더의 URL로 새로운 요청을 보냄

### 2. Location 헤더

```
Location: /product.do?cmd=list
```

- **Location**: 리다이렉트할 URL을 지정하는 헤더
- **값**: `/product.do?cmd=list` (상대 경로)
  - 절대 경로로도 가능: `http://localhost:8080/product.do?cmd=list`
  - 상대 경로를 사용하면 같은 호스트/포트로 자동 처리

**Location 헤더의 역할:**
- 브라우저가 어디로 리다이렉트할지 알려주는 헤더
- 이 헤더가 없으면 리다이렉트가 작동하지 않음

### 3. Content-Length 헤더

```
Content-Length: 0
```

- **Content-Length**: 응답 바디의 크기 (바이트)
- **0**: 리다이렉트 응답은 바디가 없으므로 0

**리다이렉트 응답의 특징:**
- HTTP 바디가 없음 (빈 응답)
- 헤더만으로 리다이렉트 정보 전달

### 4. Date 헤더

```
Date: Mon, 23 Jan 2026 10:30:45 GMT
```

- **Date**: 서버가 응답을 생성한 날짜와 시간
- 표준 시간대: GMT (UTC)

### 5. Server 헤더

```
Server: Apache-Coyote/1.1
```

- **Server**: 서버 소프트웨어 정보
- **Apache-Coyote**: 톰캣 서버의 내부 이름
- Spring Boot 내장 톰캣에서 사용

### 6. Connection 헤더

```
Connection: close
```

- **Connection**: 연결 관리 방식
- **close**: 응답 후 연결을 닫음 (HTTP/1.1의 경우)

## 코드별 헤더 생성 과정

### 단계별 설명

```java
// 1단계: Controller에서 URL 반환
String url = pc.insert(req, resp);
// url = "/product.do?cmd=list"

// 2단계: HTTP 상태 코드 설정
resp.setStatus(302);
// → HTTP 응답의 Status Line에 "302 Found" 추가

// 3단계: Location 헤더 설정
resp.setHeader("Location", url);
// → HTTP 응답 헤더에 "Location: /product.do?cmd=list" 추가
```

### 실제 생성되는 헤더

```java
resp.setStatus(302);
// 생성: HTTP/1.1 302 Found

resp.setHeader("Location", "/product.do?cmd=list");
// 생성: Location: /product.do?cmd=list
```

## 브라우저의 동작

### 1. POST 요청 전송

```
POST /product.do?cmd=insert HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
Content-Length: 30

name=콜라&price=1000&qty=5개
```

### 2. 서버 응답 (302 리다이렉트)

```
HTTP/1.1 302 Found
Location: /product.do?cmd=list
Content-Length: 0
```

### 3. 브라우저가 자동으로 GET 요청

```
GET /product.do?cmd=list HTTP/1.1
Host: localhost:8080
```

**브라우저의 자동 처리:**
1. 302 상태 코드 확인
2. Location 헤더 읽기
3. 자동으로 새로운 GET 요청 전송
4. URL 주소창이 변경됨 (`/product.do?cmd=list`)

## resp.sendRedirect() vs 수동 설정 비교

### 방법 1: 수동 설정 (현재 코드)

```java
resp.setStatus(302);
resp.setHeader("Location", url);
```

**생성되는 헤더:**
```
HTTP/1.1 302 Found
Location: /product.do?cmd=list
```

### 방법 2: sendRedirect() 사용

```java
resp.sendRedirect(url);
```

**생성되는 헤더:**
```
HTTP/1.1 302 Found
Location: /product.do?cmd=list
```

**차이점:**
- `sendRedirect()`는 내부적으로 `setStatus(302)`와 `setHeader("Location", ...)`를 모두 호출
- 결과는 동일하지만 `sendRedirect()`가 더 간편함
- 현재 코드의 `delete` 메서드에서 `sendRedirect()` 사용 중

## 실제 네트워크 패킷 구조

### HTTP 응답 메시지 (바이트 단위)

```
48 54 54 50 2F 31 2E 31 20 33 30 32 20 46 6F 75 6E 64 0D 0A
(HTTP/1.1 302 Found\r\n)

4C 6F 63 61 74 69 6F 6E 3A 20 2F 70 72 6F 64 75 63 74 2E 64 6F 3F 63 6D 64 3D 6C 69 73 74 0D 0A
(Location: /product.do?cmd=list\r\n)

43 6F 6E 74 65 6E 74 2D 4C 65 6E 67 74 68 3A 20 30 0D 0A
(Content-Length: 0\r\n)

0D 0A
(\r\n - 헤더와 바디 구분)
```

## 요약

### 핵심 헤더

| 헤더 | 값 | 설명 |
|------|-----|------|
| **Status Line** | `HTTP/1.1 302 Found` | 리다이렉트 상태 코드 |
| **Location** | `/product.do?cmd=list` | 리다이렉트할 URL |
| **Content-Length** | `0` | 바디 없음 |

### 코드 동작

```java
resp.setStatus(302);              // → HTTP/1.1 302 Found
resp.setHeader("Location", url); // → Location: /product.do?cmd=list
```

### 브라우저 동작

1. 302 응답 수신
2. Location 헤더 확인
3. 자동으로 GET 요청 전송
4. URL 주소창 변경
