/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.jdbc.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * Tests for SQLErrorCodes loading.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 */
public class SQLErrorCodesFactoryTests {

	/**
	 * Check that a default instance returns empty error codes for an unknown database.
	 */
	@Test
	public void testDefaultInstanceWithNoSuchDatabase() {
		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes("xx");
		assertThat(sec.getBadSqlGrammarCodes().length).isEqualTo(0);
		assertThat(sec.getDataIntegrityViolationCodes().length).isEqualTo(0);
	}

	/**
	 * Check that a known database produces recognizable codes.
	 */
	@Test
	public void testDefaultInstanceWithOracle() {
		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes("Oracle");
		assertIsOracle(sec);
	}

	private void assertIsOracle(SQLErrorCodes sec) {
		assertThat(sec.getBadSqlGrammarCodes().length).isGreaterThan(0);
		assertThat(sec.getDataIntegrityViolationCodes().length).isGreaterThan(0);
		// These had better be a Bad SQL Grammar code
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "942")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "6550")).isGreaterThanOrEqualTo(0);
		// This had better NOT be
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "9xx42")).isLessThan(0);
	}

	private void assertIsSQLServer(SQLErrorCodes sec) {
		assertThat(sec.getDatabaseProductName()).isEqualTo("Microsoft SQL Server");

		assertThat(sec.getBadSqlGrammarCodes().length).isGreaterThan(0);

		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "156")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "170")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "207")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "208")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "209")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "9xx42")).isLessThan(0);

		assertThat(sec.getPermissionDeniedCodes().length).isGreaterThan(0);
		assertThat(Arrays.binarySearch(sec.getPermissionDeniedCodes(), "229")).isGreaterThanOrEqualTo(0);

		assertThat(sec.getDuplicateKeyCodes().length).isGreaterThan(0);
		assertThat(Arrays.binarySearch(sec.getDuplicateKeyCodes(), "2601")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getDuplicateKeyCodes(), "2627")).isGreaterThanOrEqualTo(0);

		assertThat(sec.getDataIntegrityViolationCodes().length).isGreaterThan(0);
		assertThat(Arrays.binarySearch(sec.getDataIntegrityViolationCodes(), "544")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getDataIntegrityViolationCodes(), "8114")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getDataIntegrityViolationCodes(), "8115")).isGreaterThanOrEqualTo(0);

		assertThat(sec.getDataAccessResourceFailureCodes().length).isGreaterThan(0);
		assertThat(Arrays.binarySearch(sec.getDataAccessResourceFailureCodes(), "4060")).isGreaterThanOrEqualTo(0);

		assertThat(sec.getCannotAcquireLockCodes().length).isGreaterThan(0);
		assertThat(Arrays.binarySearch(sec.getCannotAcquireLockCodes(), "1222")).isGreaterThanOrEqualTo(0);

		assertThat(sec.getDeadlockLoserCodes().length).isGreaterThan(0);
		assertThat(Arrays.binarySearch(sec.getDeadlockLoserCodes(), "1205")).isGreaterThanOrEqualTo(0);
	}

	private void assertIsHsql(SQLErrorCodes sec) {
		assertThat(sec.getBadSqlGrammarCodes().length).isGreaterThan(0);
		assertThat(sec.getDataIntegrityViolationCodes().length).isGreaterThan(0);
		// This had better be a Bad SQL Grammar code
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "-22")).isGreaterThanOrEqualTo(0);
		// This had better NOT be
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "-9")).isLessThan(0);
	}

	private void assertIsDB2(SQLErrorCodes sec) {
		assertThat(sec.getBadSqlGrammarCodes().length).isGreaterThan(0);
		assertThat(sec.getDataIntegrityViolationCodes().length).isGreaterThan(0);

		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "942")).isLessThan(0);
		// This had better NOT be
		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "-204")).isGreaterThanOrEqualTo(0);
	}

	private void assertIsHana(SQLErrorCodes sec) {
		assertThat(sec.getBadSqlGrammarCodes().length).isGreaterThan(0);
		assertThat(sec.getDataIntegrityViolationCodes().length).isGreaterThan(0);

		assertThat(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "368")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getPermissionDeniedCodes(), "10")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getDuplicateKeyCodes(), "301")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getDataIntegrityViolationCodes(), "461")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getDataAccessResourceFailureCodes(), "-813")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getInvalidResultSetAccessCodes(), "582")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getCannotAcquireLockCodes(), "131")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getCannotSerializeTransactionCodes(), "138")).isGreaterThanOrEqualTo(0);
		assertThat(Arrays.binarySearch(sec.getDeadlockLoserCodes(), "133")).isGreaterThanOrEqualTo(0);

	}

	@Test
	public void testLookupOrder() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			private int lookups = 0;
			@Override
			protected Resource loadResource(String path) {
				++lookups;
				if (lookups == 1) {
					assertThat(path).isEqualTo(SQLErrorCodesFactory.SQL_ERROR_CODE_DEFAULT_PATH);
				}
				else {
					// Should have only one more lookup
					assertThat(lookups).isEqualTo(2);
					assertThat(path).isEqualTo(SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH);
				}
				return null;
			}
		}

		// Should have failed to load without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertThat(sf.getErrorCodes("XX").getBadSqlGrammarCodes().length).isEqualTo(0);
		assertThat(sf.getErrorCodes("Oracle").getDataIntegrityViolationCodes().length).isEqualTo(0);
	}

	/**
	 * Check that user defined error codes take precedence.
	 */
	@Test
	public void testFindUserDefinedCodes() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			@Override
			protected Resource loadResource(String path) {
				if (SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH.equals(path)) {
					return new ClassPathResource("test-error-codes.xml", SQLErrorCodesFactoryTests.class);
				}
				return null;
			}
		}

		// Should have loaded without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertThat(sf.getErrorCodes("XX").getBadSqlGrammarCodes().length).isEqualTo(0);
		assertThat(sf.getErrorCodes("Oracle").getBadSqlGrammarCodes()).hasSize(2);
		assertThat(sf.getErrorCodes("Oracle").getBadSqlGrammarCodes()[0]).isEqualTo("1");
		assertThat(sf.getErrorCodes("Oracle").getBadSqlGrammarCodes()[1]).isEqualTo("2");
	}

	@Test
	public void testInvalidUserDefinedCodeFormat() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			@Override
			protected Resource loadResource(String path) {
				if (SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH.equals(path)) {
					// Guaranteed to be on the classpath, but most certainly NOT XML
					return new ClassPathResource("SQLExceptionTranslator.class", SQLErrorCodesFactoryTests.class);
				}
				return null;
			}
		}

		// Should have failed to load without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertThat(sf.getErrorCodes("XX").getBadSqlGrammarCodes().length).isEqualTo(0);
		assertThat(sf.getErrorCodes("Oracle").getBadSqlGrammarCodes()).isEmpty();
	}

	/**
	 * Check that custom error codes take precedence.
	 */
	@Test
	public void testFindCustomCodes() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			@Override
			protected Resource loadResource(String path) {
				if (SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH.equals(path)) {
					return new ClassPathResource("custom-error-codes.xml", SQLErrorCodesFactoryTests.class);
				}
				return null;
			}
		}

		// Should have loaded without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertThat(sf.getErrorCodes("Oracle").getCustomTranslations()).hasSize(1);
		CustomSQLErrorCodesTranslation translation =
				sf.getErrorCodes("Oracle").getCustomTranslations()[0];
		assertThat(translation.getExceptionClass()).isEqualTo(CustomErrorCodeException.class);
		assertThat(translation.getErrorCodes()).hasSize(1);
	}

	@Test
	public void testDataSourceWithNullMetadata() throws Exception {
		Connection connection = mock();
		DataSource dataSource = mock();
		given(dataSource.getConnection()).willReturn(connection);

		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes(dataSource);
		assertIsEmpty(sec);
		verify(connection).close();

		reset(connection);
		sec = SQLErrorCodesFactory.getInstance().resolveErrorCodes(dataSource);
		assertThat(sec).isNull();
		verify(connection).close();
	}

	@Test
	public void testGetFromDataSourceWithSQLException() throws Exception {
		SQLException expectedSQLException = new SQLException();

		DataSource dataSource = mock();
		given(dataSource.getConnection()).willThrow(expectedSQLException);

		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes(dataSource);
		assertIsEmpty(sec);

		sec = SQLErrorCodesFactory.getInstance().resolveErrorCodes(dataSource);
		assertThat(sec).isNull();
	}

	private SQLErrorCodes getErrorCodesFromDataSource(String productName, SQLErrorCodesFactory factory) throws Exception {
		DatabaseMetaData databaseMetaData = mock();
		given(databaseMetaData.getDatabaseProductName()).willReturn(productName);

		Connection connection = mock();
		given(connection.getMetaData()).willReturn(databaseMetaData);

		DataSource dataSource = mock();
		given(dataSource.getConnection()).willReturn(connection);

		SQLErrorCodesFactory secf = (factory != null ? factory : SQLErrorCodesFactory.getInstance());
		SQLErrorCodes sec = secf.getErrorCodes(dataSource);

		SQLErrorCodes sec2 = secf.getErrorCodes(dataSource);
		assertThat(sec).as("Cached per DataSource").isSameAs(sec2);

		verify(connection).close();
		return sec;
	}

	@Test
	public void testSQLServerRecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSource("MS-SQL", null);
		assertIsSQLServer(sec);
	}

	@Test
	public void testOracleRecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSource("Oracle", null);
		assertIsOracle(sec);
	}

	@Test
	public void testHsqlRecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSource("HSQL Database Engine", null);
		assertIsHsql(sec);
	}

	@Test
	public void testDB2RecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSource("DB2", null);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("DB2/", null);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("DB-2", null);
		assertIsEmpty(sec);
	}

	@Test
	public void testHanaIsRecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSource("SAP DB", null);
		assertIsHana(sec);
	}

	/**
	 * Check that wild card database name works.
	 */
	@Test
	public void testWildCardNameRecognized() throws Exception {
		class WildcardSQLErrorCodesFactory extends SQLErrorCodesFactory {
			@Override
			protected Resource loadResource(String path) {
				if (SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH.equals(path)) {
					return new ClassPathResource("wildcard-error-codes.xml", SQLErrorCodesFactoryTests.class);
				}
				return null;
			}
		}

		WildcardSQLErrorCodesFactory factory = new WildcardSQLErrorCodesFactory();
		SQLErrorCodes sec = getErrorCodesFromDataSource("DB2", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("DB2 UDB for Xxxxx", factory);
		assertIsDB2(sec);

		sec = getErrorCodesFromDataSource("DB3", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("DB3/", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("/DB3", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("/DB3", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("/DB3/", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("DB-3", factory);
		assertIsEmpty(sec);

		sec = getErrorCodesFromDataSource("DB1", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("DB1/", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("/DB1", factory);
		assertIsEmpty(sec);
		sec = getErrorCodesFromDataSource("/DB1/", factory);
		assertIsEmpty(sec);

		sec = getErrorCodesFromDataSource("DB0", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("/DB0", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSource("DB0/", factory);
		assertIsEmpty(sec);
		sec = getErrorCodesFromDataSource("/DB0/", factory);
		assertIsEmpty(sec);
	}

	private void assertIsEmpty(SQLErrorCodes sec) {
		assertThat(sec.getBadSqlGrammarCodes()).isEmpty();
		assertThat(sec.getDataIntegrityViolationCodes()).isEmpty();
	}

}
