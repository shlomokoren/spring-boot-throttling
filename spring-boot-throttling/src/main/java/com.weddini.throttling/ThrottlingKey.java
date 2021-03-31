package com.weddini.throttling;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Class holding method execution context
 * Used as a key in {@link com.weddini.throttling.cache.Cache}
 *
 * @author Nikolay Papakha (nikolay.papakha@gmail.com)
 */
public class ThrottlingKey {

    private final Method method;
    private final int limit;
    private final ThrottlingType type;
    private final TimeUnit timeUnit;
    private final String evaluatedValue;
    private final long duration;

    private ThrottlingKey(Method method, int limit, ThrottlingType type, TimeUnit timeUnit, String evaluatedValue, long duration) {
        this.method = method;
        this.limit = limit;
        this.type = type;
        this.timeUnit = timeUnit;
        this.evaluatedValue = evaluatedValue;
        this.duration = duration;
    }

    public static Builder builder() {
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

    public String getEvaluatedValue() {
        return evaluatedValue;
    }

    public long getDuration() {return duration; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThrottlingKey)) return false;

        ThrottlingKey that = (ThrottlingKey) o;

        if (limit != that.limit) return false;
        if (!method.equals(that.method)) return false;
        if (type != that.type) return false;
        if (timeUnit != that.timeUnit) return false;
        if (duration != that.duration) return false;
        return evaluatedValue != null ? evaluatedValue.equals(that.evaluatedValue) : that.evaluatedValue == null;
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + limit;
        result = 31 * result + type.hashCode();
        result = 31 * result + timeUnit.hashCode();
        result = 31 * result + (evaluatedValue != null ? evaluatedValue.hashCode() : 0);
        result = (int) (31 * result + timeUnit.toMillis(duration));
        return result;
    }

    @Override
    public String toString() {
        return "ThrottlingKey{" +
                "method=" + method +
                ", limit=" + limit +
                ", type=" + type +
                ", duration = " + duration +
                ", timeUnit=" + timeUnit +
                ", evaluatedValue='" + evaluatedValue + '\'' +
                '}';
    }

    public static class Builder {
        private Method method;
        private int limit;
        private ThrottlingType type;
        private TimeUnit timeUnit;
        private String evaluatedValue;
        private long duration;

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder annotation(Throttling throttling) {
            this.limit = throttling.limit();
            this.type = throttling.type();
            this.timeUnit = throttling.timeUnit();
            this.duration = throttling.duration();
            return this;
        }

        public Builder evaluatedValue(String evaluatedValue) {
            this.evaluatedValue = evaluatedValue;
            return this;
        }

        public ThrottlingKey build() {
            return new ThrottlingKey(method, limit, type, timeUnit, evaluatedValue, duration);
        }
    }
}
