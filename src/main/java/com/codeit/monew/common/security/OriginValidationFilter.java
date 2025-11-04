package com.codeit.monew.common.security;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Minimal CSRF mitigation filter that rejects state-changing requests
 * initiated from 다른 Origin/Referer. 프런트엔드 수정이 어려운 상황에서
 * 서버 단에서만 적용 가능한 완화책이다.
 */
public class OriginValidationFilter extends OncePerRequestFilter {
	private final Set<String> allowedOrigins;

	public OriginValidationFilter(Set<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String method = request.getMethod();
		if (!requiresValidation(method)) {
			filterChain.doFilter(request, response);
			return;
		}

		String originHeader = request.getHeader("Origin");
		String detectedOrigin = extractOrigin(originHeader);
		if (!StringUtils.hasText(detectedOrigin)) {
			String refererHeader = request.getHeader("Referer");
			detectedOrigin = extractOriginFromReferer(refererHeader);
		}

		if (!StringUtils.hasText(detectedOrigin)) {
			// 브라우저가 Origin/Referer를 보내지 않는 경우(예: Postman) 그대로 허용
			filterChain.doFilter(request, response);
			return;
		}

		if (isSameOrigin(request, detectedOrigin) || allowedOrigins.contains(detectedOrigin)) {
			filterChain.doFilter(request, response);
			return;
		}

		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid cross-origin request");
	}

	private boolean requiresValidation(String method) {
		if (!StringUtils.hasText(method)) {
			return false;
		}
		HttpMethod httpMethod;
		try {
			httpMethod = HttpMethod.valueOf(method.toUpperCase());
		} catch (IllegalArgumentException ex) {
			return true;
		}
		return httpMethod != HttpMethod.GET
			&& httpMethod != HttpMethod.HEAD
			&& httpMethod != HttpMethod.OPTIONS
			&& httpMethod != HttpMethod.TRACE;
	}

	private String extractOrigin(String originHeader) {
		if (!StringUtils.hasText(originHeader) || "null".equals(originHeader)) {
			return null;
		}
		return originHeader.trim();
	}

	private String extractOriginFromReferer(String refererHeader) {
		if (!StringUtils.hasText(refererHeader)) {
			return null;
		}
		try {
			URI refererUri = URI.create(refererHeader);
			if (!StringUtils.hasText(refererUri.getScheme()) || !StringUtils.hasText(refererUri.getHost())) {
				return null;
			}

			int port = refererUri.getPort();
			if (port == -1 || isDefaultPort(refererUri.getScheme(), port)) {
				return refererUri.getScheme() + "://" + refererUri.getHost();
			}
			return refererUri.getScheme() + "://" + refererUri.getHost() + ":" + port;
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private boolean isSameOrigin(HttpServletRequest request, String detectedOrigin) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int port = request.getServerPort();

		String requestOrigin;
		if (isDefaultPort(scheme, port)) {
			requestOrigin = scheme + "://" + serverName;
		} else {
			requestOrigin = scheme + "://" + serverName + ":" + port;
		}

		return requestOrigin.equalsIgnoreCase(detectedOrigin);
	}

	private boolean isDefaultPort(String scheme, int port) {
		return ("http".equalsIgnoreCase(scheme) && port == 80)
			|| ("https".equalsIgnoreCase(scheme) && port == 443);
	}
}
