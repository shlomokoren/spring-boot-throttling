# Spring Boot Throttling
![travis-ci](https://travis-ci.org/weddini/spring-boot-throttling.svg?branch=master)

### Overview

Declarative approach of throttling control over the Spring services. 
`@Throttling` annotation helps you to limit the number of service method calls per `java.util.concurrent.TimeUnit`
for a particular user, IP address, HTTP header/cookie value, or using [Spring Expression Language (SpEL)](https://docs.spring.io/spring/docs/4.3.12.RELEASE/spring-framework-reference/html/expressions.html)

Pull requests are always welcome. 


### Getting Started

##### Gradle setup

...

##### Maven setup

...

### Samples

##### Defaults, Remote IP

```java
@Throttling
public void serviceMethod() {
}
```

##### Spring Expression Language (SpEL)
 
```java
@Throttling(limit = 3,
        timeUnit = TimeUnit.MINUTES,
        type = ThrottlingType.SpEL,
        expression = "#model.userName")
public void serviceMethod(Model model) {
    log.info("executing service logic for userName = {}", model.getUserName());
}
```

##### Http cookie value

```java
@Throttling(limit = 24,
        timeUnit = TimeUnit.DAYS,
        type = ThrottlingType.CookieValue,
        cookieName = "JSESSIONID")
public void serviceMethod() {
}
```

##### Http header value

```java
@Throttling(limit = 10,
        timeUnit = TimeUnit.HOURS,
        type = ThrottlingType.HeaderValue,
        headerName = "X-Forwarded-For")
public void serviceMethod() {
}
```

##### User Principal Name

```java
@Throttling(limit = 10,
        timeUnit = TimeUnit.HOURS,
        type = ThrottlingType.HeaderValue,
        headerName = "X-Forwarded-For")
public void serviceMethod() {
}
```


### Error handling

`ThrottlingException` is thrown when method reaches `@Throttling` configuration limit. 

```java
@ResponseStatus(code = HttpStatus.TOO_MANY_REQUESTS, reason = "Too many requests")
public class ThrottlingException extends RuntimeException {
}
```
![Throttling with http header. Exception-handling.](./assets/throttling-with-header-exception-handling.png)


### License
Spring Boot Throttling is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
