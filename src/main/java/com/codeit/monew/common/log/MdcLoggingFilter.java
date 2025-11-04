package com.codeit.monew.common.log;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MdcLoggingFilter implements Filter {

	private static final String REQUEST_ID_KEY = "requestId";
	private static final String IP_KEY = "ip";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		ServletException,
		IOException {

		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;

		String requestId = UUID.randomUUID().toString();

		String clientIp = getClientIp(req);

		String previousRequestId = MDC.get(REQUEST_ID_KEY);
		String previousIp = MDC.get(IP_KEY);

		try {
			MDC.put(REQUEST_ID_KEY, requestId);
			MDC.put(IP_KEY, clientIp);

			res.setHeader("X-Request-ID", requestId);
			res.setHeader("X-Client-IP", clientIp);

			chain.doFilter(request, response);
		} finally {
			restoreMdcValue(REQUEST_ID_KEY, previousRequestId);
			restoreMdcValue(IP_KEY, previousIp);
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader != null) {
			String forwardedIp = xfHeader.split(",")[0].trim();
			if (!forwardedIp.isEmpty() && !"unknown".equalsIgnoreCase(forwardedIp)) {
				return forwardedIp;
			}
		}
		return request.getRemoteAddr();
	}

	private void restoreMdcValue(String key, String previousValue) {
		if (previousValue == null) {
			MDC.remove(key);
		} else {
			MDC.put(key, previousValue);
		}
	}
}
