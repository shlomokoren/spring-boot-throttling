package com.weddini.throttling;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Class holding method execution context
 * Used as a key in {@link LRUCache}
 *
 * @author Nikolay Papakha (nikolay.papakha@gmail.com)
 */
public class ThrottlingKey {

    private final Method method;
    private final int limit;
    private final ThrottlingType type;
    private final TimeUnit timeUnit;
    private final String headerName;
    private final String cookieName;
    private final String headerValue;
    private final String cookieValue;
    private final String principal;
    private final String expression;

    public ThrottlingKey(Method method, int limit, ThrottlingType type, TimeUnit timeUnit, String headerName, String cookieName,
                         String headerValue, String cookieValue, String principal, String expression) {

        this.method = method;
        this.limit = limit;
        this.type = type;
        this.timeUnit = timeUnit;
        this.headerName = headerName;
        this.cookieName = cookieName;
        this.headerValue = headerValue;
        this.cookieValue = cookieValue;
        this.principal = principal;
        this.expression = expression;
    }

    static Builder builder() {
        return new Builder();
    }

    public Method getMethod() {
        return method;
    }

    public int getLimit() {
        return limit;
    }

    public ThrottlingType getType() {
        return type;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThrottlingKey)) return false;

        ThrottlingKey that = (ThrottlingKey) o;

        if (limit != that.limit) return false;
        if (!method.equals(that.method)) return false;
        if (type != that.type) return false;
        if (timeUnit != that.timeUnit) return false;
        if (headerName != null ? !headerName.equals(that.headerName) : that.headerName != null) return false;
        if (cookieName != null ? !cookieName.equals(that.cookieName) : that.cookieName != null) return false;
        if (headerValue != null ? !headerValue.equals(that.headerValue) : that.headerValue != null) return false;
        if (cookieValue != null ? !cookieValue.equals(that.cookieValue) : that.cookieValue != null) return false;
        if (principal != null ? !principal.equals(that.principal) : that.principal != null) return false;
        return expression != null ? expression.equals(that.expression) : that.expression == null;
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + limit;
        result = 31 * result + type.hashCode();
        result = 31 * result + timeUnit.hashCode();
        result = 31 * result + (headerName != null ? headerName.hashCode() : 0);
        result = 31 * result + (cookieName != null ? cookieName.hashCode() : 0);
        result = 31 * result + (headerValue != null ? headerValue.hashCode() : 0);
        result = 31 * result + (cookieValue != null ? cookieValue.hashCode() : 0);
        result = 31 * result + (principal != null ? principal.hashCode() : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }

    static class Builder {
        private Method method;
        private int limit;
        private ThrottlingType type;
        private TimeUnit timeUnit;
        private String headerName;
        private String cookieName;
        private String headerValue;
        private String cookieValue;
        private String principal;
        private String expression;

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder throttling(Throttling throttling) {
            this.limit = throttling.limit();
            this.type = throttling.type();
            this.timeUnit = throttling.timeUnit();
            this.headerName = throttling.headerName();
            this.cookieName = throttling.cookieName();
            return this;
        }

        public Builder headerValue(String headerValue) {
            this.headerValue = headerValue;
            return this;
        }

        public Builder cookieValue(String cookieValue) {
            this.cookieValue = cookieValue;
            return this;
        }

        public Builder principal(String principal) {
            this.principal = principal;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public ThrottlingKey build() {
            return new ThrottlingKey(method, limit, type, timeUnit, headerName, cookieName, headerValue, cookieValue,
                    principal, expression);
        }
    }
}
