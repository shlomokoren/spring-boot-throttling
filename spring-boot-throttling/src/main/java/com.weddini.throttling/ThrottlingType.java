package com.weddini.throttling;

/**
 * Enumeration of supported throttling types.

 * <p>Used to evaluate method execution context in {@link Throttling} configuration.
 *
 * @author Nikolay Papakha (nikolay.papakha@gmail.com)
 */
public enum ThrottlingType {

    /**
     * Throttling context will be evaluated via the request-scoped bean {@link javax.servlet.http.HttpServletRequest}
     * {@see javax.servlet.http.HttpServletRequest#getRemoteAddr()}
     */
    RemoteAddr,

    /**
     * Throttling context will be evaluated via the request-scoped bean {@link javax.servlet.http.HttpServletRequest}
     * {@see javax.servlet.http.HttpServletRequest#getHeader()}
     */
    HeaderValue,

    /**
     * Throttling context will be evaluated via the request-scoped bean {@link javax.servlet.http.HttpServletRequest}
     * {@see javax.servlet.http.HttpServletRequest#getCookies()}
     */
    CookieValue,

    /**
     * Throttling context will be evaluated via the request-scoped bean {@link javax.servlet.http.HttpServletRequest}
     * {@see javax.servlet.http.HttpServletRequest#getUserPrincipal().getName()}
     */
    PrincipalName,

    /**
     * Throttling context will be evaluated as Spring-EL expression
     * before invoking the protected method
     */
    SpEL

}
