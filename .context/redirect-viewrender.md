# 리다이렉트 vs View 렌더링 방식 비교

## 전체 요청-응답 흐름

```
[브라우저] 
    ↓ HTTP 요청 (GET/POST)
[톰캣 서버]
    ↓ req, resp 객체 생성
[DispatcherServlet] 
    ↓ cmd 파라미터로 라우팅
[ProductController]
    ↓ 비즈니스 로직 처리
[응답 방식 선택]
    ├─ View 렌더링 방식 (GET 요청)
    └─ 리다이렉트 방식 (POST 요청)
```

---

## 방식 1: View 렌더링 방식 (Forward)

### 코드 위치
```java
// DispatcherServlet.java (24-26줄)
String viewName = pc.list(req, resp);
ViewResolver.render(viewName).forward(req, resp);
```

### 전체 흐름 다이어그램

```
┌─────────────┐
│   브라우저   │
│  GET 요청    │
│ /product.do? │
│ cmd=list    │
└──────┬──────┘
       │ HTTP 요청
       ↓
┌─────────────────┐
│   톰캣 서버      │
│ req, resp 생성   │
└──────┬──────────┘
       │
       ↓
┌─────────────────────┐
│ DispatcherServlet    │
│ @WebServlet("*.do")  │
│                      │
│ 1. cmd 파라미터 읽기 │
│ 2. 라우팅 결정       │
│ 3. Controller 호출  │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│ ProductController   │
│ list()              │
│                      │
│ 1. Service 호출     │
│    (DB 조회)        │
│ 2. req.setAttribute │
│    (데이터 저장)    │
│ 3. view 이름 반환    │
│   return "list"     │
└──────┬──────────────┘
       │ viewName = "list"
       ↓
┌─────────────────────┐
│ ViewResolver        │
│ render("list")      │
│                      │
│ 1. templates/        │
│    list.mustache     │
│    읽기             │
│ 2. Mustache 템플릿   │
│    컴파일            │
│ 3. View 객체 생성    │
└──────┬──────────────┘
       │ View 객체
       ↓
┌─────────────────────┐
│ View                │
│ forward(req, resp)  │
│                      │
│ [1단계] 데이터 수집   │
│ - req.getAttribute  │
│   Names() 순회       │
│ - models: List<     │
│   Product>          │
│ - what: "엉?"       │
│                      │
│ [2단계] Model 생성   │
│ Map<String, Object> │
│ model = {           │
│   "models": [...],  │
│   "what": "엉?",    │
│   "request": req    │
│ }                   │
│                      │
│ [3단계] SSR 시작     │
│ template.execute(   │
│   model,            │
│   resp.getWriter()  │
│ )                   │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────────────────┐
│ Mustache 템플릿 엔진 (SSR)       │
│                                  │
│ list.mustache 템플릿:            │
│ {{#models}}                     │
│   <tr>                          │
│     <td>{{id}}</td>             │
│     <td>{{name}}</td>           │
│   </tr>                         │
│ {{/models}}                     │
│                                  │
│ [렌더링 과정]                    │
│ 1. 정적 HTML 부분 그대로 출력   │
│    "<!DOCTYPE html>..."         │
│                                  │
│ 2. {{what}} 발견                │
│    → model.get("what")          │
│    → "엉?" 출력                 │
│                                  │
│ 3. {{#models}} 발견             │
│    → List<Product> 순회 시작    │
│    → for (Product p : models)  │
│                                  │
│    [반복 1회차]                 │
│    - {{id}} → p.getId()        │
│      → "1" 출력                │
│    - {{name}} → p.getName()    │
│      → "바나나" 출력           │
│    - <tr>...</tr> 생성          │
│                                  │
│    [반복 2회차]                 │
│    - {{id}} → "2"              │
│    - {{name}} → "감자"          │
│    - <tr>...</tr> 생성          │
│                                  │
│ 4. 정적 HTML 마무리             │
│    "</table></body></html>"     │
│                                  │
│ [결과]                          │
│ 완성된 HTML 문자열 생성         │
└──────┬─────────────────────────┘
       │
       ↓
┌─────────────────────────────────┐
│ resp.getWriter() 버퍼           │
│                                  │
│ [버퍼에 쌓이는 HTML]            │
│ "<!DOCTYPE html>                │
│  <html>                         │
│    <h1>what : 엉?</h1>          │
│    <table>                      │
│      <tr>                       │
│        <td>1</td>               │
│        <td>바나나</td>          │
│      </tr>                      │
│      <tr>                       │
│        <td>2</td>               │
│        <td>감자</td>            │
│      </tr>                      │
│    </table>                     │
│  </html>"                       │
│                                  │
│ resp.getWriter().flush()        │
│ → 버퍼 내용을 HTTP 응답으로 전송 │
└──────┬─────────────────────────┘
       │ HTTP 응답 (HTML)
       ↓
┌─────────────┐
│   브라우저   │
│ HTML 수신   │
│ DOM 파싱     │
│ 화면 렌더링  │
└─────────────┘
```

### SSR (Server-Side Rendering) 상세 과정

#### 1. 템플릿 파일: list.mustache
```mustache
<!DOCTYPE html>
<html>
<body>
    <h1>what : {{what}}</h1>
    <table>
        {{#models}}
        <tr>
            <td>{{id}}</td>
            <td>{{name}}</td>
        </tr>
        {{/models}}
    </table>
</body>
</html>
```

#### 2. req 객체에 저장된 데이터
```java
// ProductController.list()에서 설정
req.setAttribute("models", List<Product>);  // [Product(id=1, name="바나나"), Product(id=2, name="감자")]
req.setAttribute("what", "엉?");
```

#### 3. View.forward()에서 데이터 수집
```java
// View.java의 forward() 메서드
Map<String, Object> model = new HashMap<>();

// req 객체에서 모든 attribute 수집
Enumeration<String> names = req.getAttributeNames();
while (names.hasMoreElements()) {
    String key = names.nextElement();
    Object value = req.getAttribute(key);
    model.put(key, value);  // "models", "what" 등 저장
}

// 결과: model = {
//   "models": [Product(id=1, name="바나나"), Product(id=2, name="감자")],
//   "what": "엉?",
//   "request": req
// }
```

#### 4. Mustache 템플릿 엔진이 SSR 수행
```java
template.execute(model, resp.getWriter());
```

**SSR 내부 동작:**
1. **정적 HTML 부분**: 그대로 버퍼에 출력
   ```
   "<!DOCTYPE html><html><body><h1>what : "
   ```

2. **변수 치환**: `{{what}}` 발견
   - `model.get("what")` → `"엉?"`
   - 버퍼에 추가: `"엉?</h1><table>"`

3. **반복문 처리**: `{{#models}}` 발견
   - `model.get("models")` → `List<Product>`
   - **for문 시작**: `for (Product product : models)`
   
   **1회차 반복:**
   - `{{id}}` → `product.getId()` → `1`
   - `{{name}}` → `product.getName()` → `"바나나"`
   - 버퍼에 추가: `"<tr><td>1</td><td>바나나</td></tr>"`
   
   **2회차 반복:**
   - `{{id}}` → `2`
   - `{{name}}` → `"감자"`
   - 버퍼에 추가: `"<tr><td>2</td><td>감자</td></tr>"`

4. **정적 HTML 마무리**: `"</table></body></html>"`

#### 5. 최종 HTML (버퍼 내용)
```html
<!DOCTYPE html>
<html>
<body>
    <h1>what : 엉?</h1>
    <table>
        <tr>
            <td>1</td>
            <td>바나나</td>
        </tr>
        <tr>
            <td>2</td>
            <td>감자</td>
        </tr>
    </table>
</body>
</html>
```

#### 6. HTTP 응답으로 전송
```java
resp.getWriter().flush();  // 버퍼 내용을 HTTP 응답 바디로 전송
```

**HTTP 응답:**
```
HTTP/1.1 200 OK
Content-Type: text/html; charset=UTF-8
Content-Length: 234

<!DOCTYPE html>
<html>
<body>
    <h1>what : 엉?</h1>
    <table>
        <tr><td>1</td><td>바나나</td></tr>
        <tr><td>2</td><td>감자</td></tr>
    </table>
</body>
</html>
```

### 특징

1. **서버 내부 처리 (SSR)**
   - 모든 처리가 서버 내부에서 완료
   - 브라우저는 완성된 HTML만 수신
   - URL이 변경되지 않음 (브라우저 주소창: `/product.do?cmd=list`)
   - **서버에서 Java 변수들이 for문을 돌면서 HTML로 변환됨**

2. **데이터 전달 및 렌더링**
   - `req.setAttribute()`로 데이터 저장
   - View에서 모든 attribute를 수집하여 템플릿에 전달
   - Mustache 템플릿 엔진이 서버에서 for문을 돌면서 HTML 생성
   - `{{#models}}` → Java의 for문과 동일한 역할
   - `{{id}}`, `{{name}}` → Java 객체의 getter 메서드 호출

3. **SSR의 핵심**
   - **템플릿 파일 (list.mustache)** → **서블릿 코드로 변환** → **실행**
   - **req 객체의 데이터** → **템플릿 엔진이 처리** → **HTML 문자열 생성**
   - **resp.getWriter() 버퍼** → **HTML이 쌓임** → **HTTP 응답으로 전송**

4. **사용 시나리오**
   - 화면 표시 (GET 요청)
   - 데이터 조회 후 화면 렌더링
   - 예: 목록 조회, 상세보기, 등록 폼 표시
   - 실제 예시: `list` 요청 (DB에서 상품 목록 조회 후 화면 표시)

---

## SSR 핵심: list.mustache → 서블릿 변환 과정

### 템플릿 파일 (list.mustache)
```mustache
<!DOCTYPE html>
<html>
<body>
    <h1>what : {{what}}</h1>
    <table>
        {{#models}}
        <tr>
            <td>{{id}}</td>
            <td><a href="/product.do?cmd=detail&id={{id}}">{{name}}</a></td>
        </tr>
        {{/models}}
    </table>
</body>
</html>
```

### ViewResolver.render()에서의 변환
```java
// ViewResolver.java
Template template = Mustache.compiler().compile(reader);
```

**내부적으로 Mustache 엔진이 수행하는 작업:**
1. 템플릿 파일을 파싱
2. `{{변수명}}` 패턴을 찾아서 Java 코드로 변환
3. `{{#리스트}}` 패턴을 찾아서 for문으로 변환
4. 컴파일된 Template 객체 생성

**의사 코드 (실제 내부 동작):**
```java
// Mustache 엔진이 내부적으로 생성하는 코드 (의사 코드)
public void render(Map<String, Object> model, Writer writer) {
    writer.write("<!DOCTYPE html><html><body>");
    writer.write("<h1>what : ");
    writer.write(String.valueOf(model.get("what")));  // {{what}}
    writer.write("</h1><table>");
    
    // {{#models}} → for문으로 변환
    List<Product> models = (List<Product>) model.get("models");
    for (Product product : models) {
        writer.write("<tr>");
        writer.write("<td>");
        writer.write(String.valueOf(product.getId()));  // {{id}}
        writer.write("</td>");
        writer.write("<td><a href=\"/product.do?cmd=detail&id=");
        writer.write(String.valueOf(product.getId()));  // {{id}}
        writer.write("\">");
        writer.write(String.valueOf(product.getName()));  // {{name}}
        writer.write("</a></td>");
        writer.write("</tr>");
    }
    
    writer.write("</table></body></html>");
}
```

### 실제 실행 과정

**입력 데이터 (req 객체에서):**
```java
models = [
    Product(id=1, name="바나나", price=1000, qty=10),
    Product(id=2, name="감자", price=2000, qty=5)
]
what = "엉?"
```

**SSR 실행 결과 (resp.getWriter() 버퍼):**
```html
<!DOCTYPE html>
<html>
<body>
    <h1>what : 엉?</h1>
    <table>
        <tr>
            <td>1</td>
            <td><a href="/product.do?cmd=detail&id=1">바나나</a></td>
        </tr>
        <tr>
            <td>2</td>
            <td><a href="/product.do?cmd=detail&id=2">감자</a></td>
        </tr>
    </table>
</body>
</html>
```

### 핵심 포인트

1. **템플릿 → 서블릿 코드 변환**
   - `list.mustache` 파일이 Mustache 엔진에 의해 내부적으로 Java 코드로 변환됨
   - `{{#models}}` → `for (Product p : models)` 와 동일한 역할
   - 서버에서 실행되는 코드이므로 **서블릿 코드**라고 볼 수 있음

2. **req 객체 → 응답 버퍼**
   - `req.setAttribute("models", ...)` 에 저장된 데이터
   - → `View.forward()`에서 `req.getAttribute()`로 수집
   - → `template.execute(model, writer)`로 전달
   - → Mustache 엔진이 for문을 돌면서 HTML 생성
   - → `resp.getWriter()` 버퍼에 HTML이 쌓임
   - → `flush()`로 HTTP 응답 바디로 전송

3. **SSR의 의미**
   - **Server-Side Rendering**: 서버에서 HTML을 완성하여 전송
   - 브라우저는 완성된 HTML만 받아서 렌더링
   - Java 변수들이 서버에서 for문을 돌면서 HTML 문자열로 변환됨
   - 클라이언트(브라우저)는 JavaScript 없이도 동적 콘텐츠를 볼 수 있음

---

## 방식 2: 리다이렉트 방식 (Redirect)

### 코드 위치
```java
// DispatcherServlet.java (44-46줄)
String url = pc.insert(req, resp);
resp.setStatus(302);
resp.setHeader("Location", url);
```

### 전체 흐름 다이어그램

```
┌─────────────┐
│   브라우저   │
│  POST 요청   │
│ /product.do? │
│ cmd=insert  │
│ (name,      │
│  price, qty)│
└──────┬──────┘
       │ HTTP 요청 (1차)
       ↓
┌─────────────────┐
│   톰캣 서버      │
│ req, resp 생성   │
└──────┬──────────┘
       │
       ↓
┌─────────────────────┐
│ DispatcherServlet    │
│ @WebServlet("*.do")  │
│                      │
│ 1. cmd 파라미터 읽기 │
│ 2. 라우팅 결정       │
│ 3. Controller 호출  │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│ ProductController   │
│ insert()            │
│                      │
│ 1. req.getParameter │
│    로 데이터 수집    │
│ 2. Service 호출     │
│    (DB 저장)        │
│ 3. URL 반환         │
│   return "/product. │
│    do?cmd=list"      │
└──────┬──────────────┘
       │ url = "/product.do?cmd=list"
       ↓
┌─────────────────────┐
│ DispatcherServlet   │
│                      │
│ 1. HTTP 302 상태코드 │
│    설정             │
│ 2. Location 헤더     │
│    설정             │
│    Location: /      │
│    product.do?cmd=  │
│    list             │
└──────┬──────────────┘
       │ HTTP 302 응답
       ↓
┌─────────────┐
│   브라우저   │
│ 302 응답 받음│
│ Location 헤더│
│ 확인         │
└──────┬──────┘
       │ 자동으로 새로운 요청
       ↓
┌─────────────┐
│   브라우저   │
│  GET 요청    │
│ /product.do? │
│ cmd=list    │
└──────┬──────┘
       │ HTTP 요청 (2차)
       ↓
┌─────────────────┐
│   톰캣 서버      │
│ req, resp 생성   │
│ (새로운 요청)    │
└──────┬──────────┘
       │
       ↓
┌─────────────────────┐
│ DispatcherServlet   │
│ cmd=list 처리       │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│ ProductController   │
│ list()              │
│                      │
│ 1. DB 조회          │
│ 2. req.setAttribute │
│ 3. "list" 반환      │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│ ViewResolver        │
│ render("list")      │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│ View                │
│ forward()           │
│ (HTML 렌더링)       │
└──────┬──────────────┘
       │ HTML 응답
       ↓
┌─────────────┐
│   브라우저   │
│ HTML 렌더링  │
│ (최종 화면)  │
└─────────────┘
```

### 특징

1. **두 번의 요청**
   - 첫 번째 요청: POST (데이터 저장)
   - 두 번째 요청: GET (결과 화면 표시)
   - 브라우저가 자동으로 두 번째 요청 수행

2. **URL 변경**
   - 브라우저 주소창이 변경됨
   - 첫 번째: `/product.do?cmd=insert`
   - 두 번째: `/product.do?cmd=list`

3. **데이터 전달**
   - 첫 번째 요청의 데이터는 사라짐
   - 두 번째 요청에서 새로 데이터 조회
   - `req.setAttribute()`는 각 요청마다 독립적

4. **사용 시나리오**
   - 데이터 변경 후 다른 페이지로 이동 (POST 요청)
   - 예: 상품 등록 후 목록으로 이동, 삭제 후 목록으로 이동

---

## 비교표

| 구분 | View 렌더링 (Forward) | 리다이렉트 (Redirect) |
|------|----------------------|---------------------|
| **요청 횟수** | 1번 | 2번 (자동) |
| **URL 변경** | 변경 안 됨 | 변경됨 |
| **서버 처리** | 내부에서 완료 | 두 번 처리 |
| **데이터 전달** | req.setAttribute() | 첫 요청 데이터는 사라짐 |
| **HTTP 상태코드** | 200 OK | 302 Found |
| **응답 헤더** | Content-Type: text/html | Location: /product.do?cmd=list |
| **사용 시점** | 화면 표시 (GET) | 데이터 변경 후 이동 (POST) |
| **Controller 반환값** | View 이름 (String) | URL (String) |
| **예시** | list (목록 조회), detail (상세보기), insert-form (등록 폼) | insert (등록), delete (삭제) |

---

## HTTP 상태코드 및 헤더 상세

### View 렌더링 방식 응답
```
HTTP/1.1 200 OK
Content-Type: text/html; charset=UTF-8
Content-Length: 1234

<!DOCTYPE html>
<html>
...
</html>
```

### 리다이렉트 방식 응답
```
HTTP/1.1 302 Found
Location: /product.do?cmd=list
Content-Length: 0

(바디 없음)
```

---

## 코드 흐름 비교

### View 렌더링 방식 (SSR 상세)
```java
// 1. DispatcherServlet
String viewName = pc.list(req, resp);       // "list" 반환
// Controller 내부:
//   - productService.상품목록() 호출 (DB 조회)
//   - List<Product> models = [Product(id=1, name="바나나"), Product(id=2, name="감자")]
//   - req.setAttribute("models", models) 데이터 저장
//   - req.setAttribute("what", "엉?")
//   - return "list"

// 2. ViewResolver
View view = ViewResolver.render(viewName);   // 템플릿 로드 및 컴파일
// - templates/list.mustache 파일 읽기
// - Mustache.compiler().compile(reader) 
//   → 템플릿을 서블릿 코드로 변환 (내부적으로)
// - Template 객체 생성 (컴파일된 템플릿)

// 3. View.forward() - SSR 수행
view.forward(req, resp);
// [단계 1] req 객체에서 데이터 수집
//   Map<String, Object> model = new HashMap<>();
//   model.put("models", req.getAttribute("models"));  // List<Product>
//   model.put("what", req.getAttribute("what"));      // "엉?"
//   model.put("request", req);
//
// [단계 2] Mustache 템플릿 엔진 실행 (SSR)
//   template.execute(model, resp.getWriter());
//   
//   내부 동작:
//   1. list.mustache 템플릿을 순회하면서
//   2. {{what}} 발견 → model.get("what") → "엉?" 출력
//   3. {{#models}} 발견 → for (Product p : models) 시작
//      - 1회차: {{id}} → p.getId() → "1" 출력
//               {{name}} → p.getName() → "바나나" 출력
//      - 2회차: {{id}} → "2" 출력
//               {{name}} → "감자" 출력
//   4. 완성된 HTML 문자열이 resp.getWriter() 버퍼에 쌓임
//
// [단계 3] 버퍼 내용을 HTTP 응답으로 전송
//   resp.getWriter().flush();
//   → 브라우저로 완성된 HTML 전송
```

### 리다이렉트 방식
```java
// 1. DispatcherServlet
String url = pc.insert(req, resp);           // "/product.do?cmd=list" 반환

// 2. DispatcherServlet
resp.setStatus(302);                         // HTTP 302 상태코드
resp.setHeader("Location", url);            // Location 헤더 설정

// 3. 브라우저
// 자동으로 Location 헤더의 URL로 GET 요청
// → 다시 DispatcherServlet → Controller → View 렌더링
```

---

## 선택 기준

### View 렌더링을 사용하는 경우
- ✅ 단순히 화면을 보여주는 경우
- ✅ 데이터 조회 후 바로 표시
- ✅ URL을 변경할 필요가 없는 경우
- ✅ GET 요청 처리

### 리다이렉트를 사용하는 경우
- ✅ 데이터를 변경한 후 (POST 요청)
- ✅ 다른 페이지로 이동해야 하는 경우
- ✅ 새로고침 시 중복 처리 방지 (PRG 패턴)
- ✅ URL을 변경하여 사용자가 직접 접근 가능하게 하려는 경우

---

## PRG 패턴 (Post-Redirect-Get)

리다이렉트 방식은 **PRG 패턴**을 구현합니다:

1. **Post**: 데이터 변경 요청
2. **Redirect**: 다른 URL로 리다이렉트
3. **Get**: 결과 화면을 GET으로 조회

이 패턴의 장점:
- 새로고침 시 POST 요청이 다시 발생하지 않음
- URL이 최종 결과 페이지를 가리킴
- 브라우저 뒤로가기/앞으로가기 동작이 자연스러움

---

## 다이어그램 그리기 참고사항

### 시퀀스 다이어그램 요소
- **액터**: 브라우저, 톰캣, DispatcherServlet, Controller, ViewResolver, View
- **메시지**: HTTP 요청/응답, 메서드 호출, 데이터 전달
- **활성화 박스**: 각 컴포넌트의 처리 시간
- **반복**: 리다이렉트는 두 번의 요청-응답 사이클

### 플로우차트 요소
- **시작/종료**: 타원형
- **처리**: 사각형
- **분기**: 다이아몬드
- **화살표**: 데이터 흐름

### 상태 변화
- **View 렌더링**: 단일 상태 변화 (요청 → 처리 → 응답)
- **리다이렉트**: 상태 변화 2회 (요청1 → 응답1 → 요청2 → 응답2)
