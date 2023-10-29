/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;

/**
 * Utility class for working with Strings that have placeholder values in them.
 * A placeholder takes the form {@code ${name}}. Using {@code PropertyPlaceholderHelper}
 * these placeholders can be substituted for user-supplied values.
 *
 * <p>Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceholderHelper {

	private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

	private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);

	static {
		wellKnownSimplePrefixes.put("}", "{");
		wellKnownSimplePrefixes.put("]", "[");
		wellKnownSimplePrefixes.put(")", "(");
	}


	private final String placeholderPrefix;

	private final String placeholderSuffix;

	private final String simplePrefix;

	@Nullable
	private final String valueSeparator;

	private final boolean ignoreUnresolvablePlaceholders;

	private final char escapeCharacter;

	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * Unresolvable placeholders are ignored.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
		this(placeholderPrefix, placeholderSuffix, null, true, '\\');
	}

	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * Default escape character '\' is used.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 * @param valueSeparator the separating character between the placeholder variable
	 * and the associated default value, if any
	 * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
	 * be ignored ({@code true}) or cause an exception ({@code false})
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
			@Nullable String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

		this(placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders, '\\');
	}

	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 * @param valueSeparator the separating character between the placeholder variable
	 * and the associated default value, if any
	 * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
	 * be ignored ({@code true}) or cause an exception ({@code false})
	 * @param escapeCharacter the escape character that denotes that the following placeholder
	 * must not be resolved
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
			@Nullable String valueSeparator, boolean ignoreUnresolvablePlaceholders, char escapeCharacter) {

		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
		this.placeholderSuffix = placeholderSuffix;
		String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
		if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
			this.simplePrefix = simplePrefixForSuffix;
		}
		else {
			this.simplePrefix = this.placeholderPrefix;
		}
		this.valueSeparator = valueSeparator;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
		this.escapeCharacter = escapeCharacter;
	}


	/**
	 * Replaces all placeholders of format {@code ${name}} with the corresponding
	 * property from the supplied {@link Properties}.
	 * @param value the value containing the placeholders to be replaced
	 * @param properties the {@code Properties} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, final Properties properties) {
		Assert.notNull(properties, "'properties' must not be null");
		return replacePlaceholders(value, properties::getProperty);
	}

	/**
	 * Replaces all placeholders of format {@code ${name}} with the value returned
	 * from the supplied {@link PlaceholderResolver}.
	 * @param value the value containing the placeholders to be replaced
	 * @param placeholderResolver the {@code PlaceholderResolver} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "'value' must not be null");
		return parseStringValue(value, placeholderResolver, null);
	}

	protected String parseStringValue(
			String value, PlaceholderResolver placeholderResolver, @Nullable Set<String> visitedPlaceholders) {
		Property property = new Property(value, placeholderResolver);
		property.resolvePlaceholders(visitedPlaceholders);
		return property.removeEscapeCharacters();
	}

	protected class Property {

		protected final String escapedPrefix = escapeCharacter + placeholderPrefix;

		protected final String originalPlaceholder;
		protected final StringBuilder result;
		protected final PlaceholderResolver placeholderResolver;

		protected Property(String value, PlaceholderResolver placeholderResolver) {
			this.originalPlaceholder = value;
			this.result = new StringBuilder(value);
			this.placeholderResolver = placeholderResolver;
		}

		protected String resolvePlaceholders(Set<String> visitedPlaceholders) {
			int startIndex = findPlaceholderPrefixIndex(0);

			while (startIndex != -1) {
				int endIndex = findPlaceholderEndIndex(startIndex);
				if (endIndex != -1) {
					String placeholderWithoutBrackets = this.result.substring(startIndex + placeholderPrefix.length(), endIndex);
					visitedPlaceholders = updateVisitedPlaceholders(visitedPlaceholders, placeholderWithoutBrackets);

					Property property = new Property(placeholderWithoutBrackets, this.placeholderResolver);
					property.resolvePlaceholders(visitedPlaceholders);
					String placeholderValue = property.getValue();

					if (placeholderValue != null) {
						Property valueProperty = new Property(placeholderValue, this.placeholderResolver);
						placeholderValue = valueProperty.resolvePlaceholders(visitedPlaceholders);
						this.result.replace(startIndex, endIndex + placeholderSuffix.length(), placeholderValue);
						if (logger.isTraceEnabled()) {
							logger.trace("Resolved placeholder '" + property.result + "'");
						}
						startIndex = this.result.indexOf(placeholderPrefix, startIndex + placeholderValue.length());

					}
					else if (ignoreUnresolvablePlaceholders) {
						// Proceed with unprocessed value.
						startIndex = this.result.indexOf(placeholderPrefix, endIndex + placeholderSuffix.length());
					}
					else {
						throw new IllegalArgumentException("Could not resolve placeholder '" +
								property.result + "'" + " in value \"" + this.originalPlaceholder + "\"");
					}
					visitedPlaceholders.remove(placeholderWithoutBrackets);
				}
				else {
					startIndex = -1;
				}
			}
			return this.result.toString();
		}

		protected Set<String> updateVisitedPlaceholders(Set<String> visitedPlaceholders, String placeholderWithoutBrackets) {
			if (visitedPlaceholders == null) {
				visitedPlaceholders = new HashSet<>(4);
			}
			if (!visitedPlaceholders.add(placeholderWithoutBrackets)) {
				throw new IllegalArgumentException(
						"Circular placeholder reference '" + placeholderWithoutBrackets + "' in property definitions");
			}
			return visitedPlaceholders;
		}

		protected int findPlaceholderPrefixIndex(int startIndex) {
			int prefixIndex = this.result.indexOf(placeholderPrefix, startIndex);
			//check if prefix is not escaped
			if (prefixIndex < 1) {
				return prefixIndex;
			}
			if (this.result.charAt(prefixIndex - 1) == escapeCharacter) {
				int endOfEscaped = findPlaceholderEndIndex(prefixIndex);
				if (endOfEscaped == -1) {
					return -1;
				}
				return findPlaceholderPrefixIndex(endOfEscaped + 1);
			}
			return prefixIndex;
		}

		protected int findPlaceholderEndIndex(int startIndex) {
			int index = startIndex + placeholderPrefix.length();
			int withinNestedPlaceholder = 0;
			while (index < this.result.length()) {
				if (StringUtils.substringMatch(this.result, index, placeholderSuffix)) {
					if (withinNestedPlaceholder > 0) {
						withinNestedPlaceholder--;
						index = index + placeholderSuffix.length();
					}
					else {
						return index;
					}
				}
				else if (StringUtils.substringMatch(this.result, index, simplePrefix)) {
					withinNestedPlaceholder++;
					index = index + simplePrefix.length();
				}
				else {
					index++;
				}
			}
			return -1;
		}

		protected String getValue() {
			String propVal = this.placeholderResolver.resolvePlaceholder(this.result.toString());
			if (propVal == null && valueSeparator != null) {
				int separatorIndex = this.result.indexOf(valueSeparator);
				if (separatorIndex != -1) {
					String actualPlaceholder = this.result.substring(0, separatorIndex);
					String defaultValue = this.result.substring(separatorIndex + valueSeparator.length());
					propVal = this.placeholderResolver.resolvePlaceholder(actualPlaceholder);
					if (propVal == null) {
						propVal = defaultValue;
					}
				}
			}
			return propVal;
		}

		protected String removeEscapeCharacters() {
			int startIndex = this.result.indexOf(this.escapedPrefix);
			while (startIndex != -1) {
				int endIndex = findPlaceholderEndIndex(startIndex + 1);
				if (endIndex != -1) {
					this.result.replace(startIndex, startIndex + this.escapedPrefix.length(), placeholderPrefix);
					startIndex = this.result.indexOf(this.escapedPrefix, endIndex + placeholderSuffix.length());
				}
				else {
					break;
				}
			}
			return this.result.toString();
		}
	}

	/**
	 * Strategy interface used to resolve replacement values for placeholders contained in Strings.
	 */
	@FunctionalInterface
	public interface PlaceholderResolver {

		/**
		 * Resolve the supplied placeholder name to the replacement value.
		 * @param placeholderName the name of the placeholder to resolve
		 * @return the replacement value, or {@code null} if no replacement is to be made
		 */
		@Nullable
		String resolvePlaceholder(String placeholderName);
	}

}
