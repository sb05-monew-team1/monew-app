package com.codeit.monew.common.util;

import java.util.Set;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SearchRequestNormalizer {

	public static int clampSize(Integer size, int def, int min, int max) {
		if (size == null) {
			return def;
		}
		if (size < min) {
			return min;
		}
		return Math.min(size, max);
	}

	public static String normalizeOrderBy(String raw, Set<String> allowed, String def) {
		if (raw == null) {
			return def;
		}
		return allowed.contains(raw) ? raw : def;
	}
}
