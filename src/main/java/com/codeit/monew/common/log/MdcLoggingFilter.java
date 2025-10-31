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

		try {
			MDC.put("requestId", requestId);
			MDC.put("ip", clientIp);

			res.addHeader("X-Request-ID", requestId);
			res.addHeader("X-Client-IP", clientIp);

			chain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}
