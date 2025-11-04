package com.codeit.monew.common.log;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		ServletException,
		IOException {

		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;

		String requestId = UUID.randomUUID().toString();

		String clientIp = getClientIp(req);

		String previousRequestId = MDC.get("requestId");
		String previousIp = MDC.get("ip");

		try {
			MDC.put("requestId", requestId);
			MDC.put("ip", clientIp);

			res.setHeader("X-Request-ID", requestId);
			res.setHeader("X-Client-IP", clientIp);

			chain.doFilter(request, response);
		} finally {
			restoreMdcValue("requestId", previousRequestId);
			restoreMdcValue("ip", previousIp);
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
