package de.mhus.osgi.commands.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

import de.mhus.osgi.commands.db.DelegatedDataSource;

public class DelegateMetaData implements DatabaseMetaData {

	private DatabaseMetaData instance;
	private DelegatedDataSource dataSource;

	public DelegateMetaData(DatabaseMetaData metaData,
			DelegatedDataSource dataSource) {
		instance = metaData;
		this.dataSource = dataSource;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return instance.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return instance.isWrapperFor(iface);
	}

	public boolean allProceduresAreCallable() throws SQLException {
		return instance.allProceduresAreCallable();
	}

	public boolean allTablesAreSelectable() throws SQLException {
		return instance.allTablesAreSelectable();
	}

	public String getURL() throws SQLException {
		return dataSource.getDelegateURL();
	}

	public String getUserName() throws SQLException {
		return instance.getUserName();
	}

	public boolean isReadOnly() throws SQLException {
		return instance.isReadOnly();
	}

	public boolean nullsAreSortedHigh() throws SQLException {
		return instance.nullsAreSortedHigh();
	}

	public boolean nullsAreSortedLow() throws SQLException {
		return instance.nullsAreSortedLow();
	}

	public boolean nullsAreSortedAtStart() throws SQLException {
		return instance.nullsAreSortedAtStart();
	}

	public boolean nullsAreSortedAtEnd() throws SQLException {
		return instance.nullsAreSortedAtEnd();
	}

	public String getDatabaseProductName() throws SQLException {
		return instance.getDatabaseProductName();
	}

	public String getDatabaseProductVersion() throws SQLException {
		return instance.getDatabaseProductVersion();
	}

	public String getDriverName() throws SQLException {
		return instance.getDriverName();
	}

	public String getDriverVersion() throws SQLException {
		return instance.getDriverVersion();
	}

	public int getDriverMajorVersion() {
		return instance.getDriverMajorVersion();
	}

	public int getDriverMinorVersion() {
		return instance.getDriverMinorVersion();
	}

	public boolean usesLocalFiles() throws SQLException {
		return instance.usesLocalFiles();
	}

	public boolean usesLocalFilePerTable() throws SQLException {
		return instance.usesLocalFilePerTable();
	}

	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		return instance.supportsMixedCaseIdentifiers();
	}

	public boolean storesUpperCaseIdentifiers() throws SQLException {
		return instance.storesUpperCaseIdentifiers();
	}

	public boolean storesLowerCaseIdentifiers() throws SQLException {
		return instance.storesLowerCaseIdentifiers();
	}

	public boolean storesMixedCaseIdentifiers() throws SQLException {
		return instance.storesMixedCaseIdentifiers();
	}

	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		return instance.supportsMixedCaseQuotedIdentifiers();
	}

	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		return instance.storesUpperCaseQuotedIdentifiers();
	}

	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		return instance.storesLowerCaseQuotedIdentifiers();
	}

	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		return instance.storesMixedCaseQuotedIdentifiers();
	}

	public String getIdentifierQuoteString() throws SQLException {
		return instance.getIdentifierQuoteString();
	}

	public String getSQLKeywords() throws SQLException {
		return instance.getSQLKeywords();
	}

	public String getNumericFunctions() throws SQLException {
		return instance.getNumericFunctions();
	}

	public String getStringFunctions() throws SQLException {
		return instance.getStringFunctions();
	}

	public String getSystemFunctions() throws SQLException {
		return instance.getSystemFunctions();
	}

	public String getTimeDateFunctions() throws SQLException {
		return instance.getTimeDateFunctions();
	}

	public String getSearchStringEscape() throws SQLException {
		return instance.getSearchStringEscape();
	}

	public String getExtraNameCharacters() throws SQLException {
		return instance.getExtraNameCharacters();
	}

	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		return instance.supportsAlterTableWithAddColumn();
	}

	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		return instance.supportsAlterTableWithDropColumn();
	}

	public boolean supportsColumnAliasing() throws SQLException {
		return instance.supportsColumnAliasing();
	}

	public boolean nullPlusNonNullIsNull() throws SQLException {
		return instance.nullPlusNonNullIsNull();
	}

	public boolean supportsConvert() throws SQLException {
		return instance.supportsConvert();
	}

	public boolean supportsConvert(int fromType, int toType)
			throws SQLException {
		return instance.supportsConvert(fromType, toType);
	}

	public boolean supportsTableCorrelationNames() throws SQLException {
		return instance.supportsTableCorrelationNames();
	}

	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		return instance.supportsDifferentTableCorrelationNames();
	}

	public boolean supportsExpressionsInOrderBy() throws SQLException {
		return instance.supportsExpressionsInOrderBy();
	}

	public boolean supportsOrderByUnrelated() throws SQLException {
		return instance.supportsOrderByUnrelated();
	}

	public boolean supportsGroupBy() throws SQLException {
		return instance.supportsGroupBy();
	}

	public boolean supportsGroupByUnrelated() throws SQLException {
		return instance.supportsGroupByUnrelated();
	}

	public boolean supportsGroupByBeyondSelect() throws SQLException {
		return instance.supportsGroupByBeyondSelect();
	}

	public boolean supportsLikeEscapeClause() throws SQLException {
		return instance.supportsLikeEscapeClause();
	}

	public boolean supportsMultipleResultSets() throws SQLException {
		return instance.supportsMultipleResultSets();
	}

	public boolean supportsMultipleTransactions() throws SQLException {
		return instance.supportsMultipleTransactions();
	}

	public boolean supportsNonNullableColumns() throws SQLException {
		return instance.supportsNonNullableColumns();
	}

	public boolean supportsMinimumSQLGrammar() throws SQLException {
		return instance.supportsMinimumSQLGrammar();
	}

	public boolean supportsCoreSQLGrammar() throws SQLException {
		return instance.supportsCoreSQLGrammar();
	}

	public boolean supportsExtendedSQLGrammar() throws SQLException {
		return instance.supportsExtendedSQLGrammar();
	}

	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		return instance.supportsANSI92EntryLevelSQL();
	}

	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		return instance.supportsANSI92IntermediateSQL();
	}

	public boolean supportsANSI92FullSQL() throws SQLException {
		return instance.supportsANSI92FullSQL();
	}

	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		return instance.supportsIntegrityEnhancementFacility();
	}

	public boolean supportsOuterJoins() throws SQLException {
		return instance.supportsOuterJoins();
	}

	public boolean supportsFullOuterJoins() throws SQLException {
		return instance.supportsFullOuterJoins();
	}

	public boolean supportsLimitedOuterJoins() throws SQLException {
		return instance.supportsLimitedOuterJoins();
	}

	public String getSchemaTerm() throws SQLException {
		return instance.getSchemaTerm();
	}

	public String getProcedureTerm() throws SQLException {
		return instance.getProcedureTerm();
	}

	public String getCatalogTerm() throws SQLException {
		return instance.getCatalogTerm();
	}

	public boolean isCatalogAtStart() throws SQLException {
		return instance.isCatalogAtStart();
	}

	public String getCatalogSeparator() throws SQLException {
		return instance.getCatalogSeparator();
	}

	public boolean supportsSchemasInDataManipulation() throws SQLException {
		return instance.supportsSchemasInDataManipulation();
	}

	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		return instance.supportsSchemasInProcedureCalls();
	}

	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		return instance.supportsSchemasInTableDefinitions();
	}

	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		return instance.supportsSchemasInIndexDefinitions();
	}

	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		return instance.supportsSchemasInPrivilegeDefinitions();
	}

	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		return instance.supportsCatalogsInDataManipulation();
	}

	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		return instance.supportsCatalogsInProcedureCalls();
	}

	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		return instance.supportsCatalogsInTableDefinitions();
	}

	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		return instance.supportsCatalogsInIndexDefinitions();
	}

	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		return instance.supportsCatalogsInPrivilegeDefinitions();
	}

	public boolean supportsPositionedDelete() throws SQLException {
		return instance.supportsPositionedDelete();
	}

	public boolean supportsPositionedUpdate() throws SQLException {
		return instance.supportsPositionedUpdate();
	}

	public boolean supportsSelectForUpdate() throws SQLException {
		return instance.supportsSelectForUpdate();
	}

	public boolean supportsStoredProcedures() throws SQLException {
		return instance.supportsStoredProcedures();
	}

	public boolean supportsSubqueriesInComparisons() throws SQLException {
		return instance.supportsSubqueriesInComparisons();
	}

	public boolean supportsSubqueriesInExists() throws SQLException {
		return instance.supportsSubqueriesInExists();
	}

	public boolean supportsSubqueriesInIns() throws SQLException {
		return instance.supportsSubqueriesInIns();
	}

	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		return instance.supportsSubqueriesInQuantifieds();
	}

	public boolean supportsCorrelatedSubqueries() throws SQLException {
		return instance.supportsCorrelatedSubqueries();
	}

	public boolean supportsUnion() throws SQLException {
		return instance.supportsUnion();
	}

	public boolean supportsUnionAll() throws SQLException {
		return instance.supportsUnionAll();
	}

	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		return instance.supportsOpenCursorsAcrossCommit();
	}

	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		return instance.supportsOpenCursorsAcrossRollback();
	}

	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		return instance.supportsOpenStatementsAcrossCommit();
	}

	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		return instance.supportsOpenStatementsAcrossRollback();
	}

	public int getMaxBinaryLiteralLength() throws SQLException {
		return instance.getMaxBinaryLiteralLength();
	}

	public int getMaxCharLiteralLength() throws SQLException {
		return instance.getMaxCharLiteralLength();
	}

	public int getMaxColumnNameLength() throws SQLException {
		return instance.getMaxColumnNameLength();
	}

	public int getMaxColumnsInGroupBy() throws SQLException {
		return instance.getMaxColumnsInGroupBy();
	}

	public int getMaxColumnsInIndex() throws SQLException {
		return instance.getMaxColumnsInIndex();
	}

	public int getMaxColumnsInOrderBy() throws SQLException {
		return instance.getMaxColumnsInOrderBy();
	}

	public int getMaxColumnsInSelect() throws SQLException {
		return instance.getMaxColumnsInSelect();
	}

	public int getMaxColumnsInTable() throws SQLException {
		return instance.getMaxColumnsInTable();
	}

	public int getMaxConnections() throws SQLException {
		return instance.getMaxConnections();
	}

	public int getMaxCursorNameLength() throws SQLException {
		return instance.getMaxCursorNameLength();
	}

	public int getMaxIndexLength() throws SQLException {
		return instance.getMaxIndexLength();
	}

	public int getMaxSchemaNameLength() throws SQLException {
		return instance.getMaxSchemaNameLength();
	}

	public int getMaxProcedureNameLength() throws SQLException {
		return instance.getMaxProcedureNameLength();
	}

	public int getMaxCatalogNameLength() throws SQLException {
		return instance.getMaxCatalogNameLength();
	}

	public int getMaxRowSize() throws SQLException {
		return instance.getMaxRowSize();
	}

	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		return instance.doesMaxRowSizeIncludeBlobs();
	}

	public int getMaxStatementLength() throws SQLException {
		return instance.getMaxStatementLength();
	}

	public int getMaxStatements() throws SQLException {
		return instance.getMaxStatements();
	}

	public int getMaxTableNameLength() throws SQLException {
		return instance.getMaxTableNameLength();
	}

	public int getMaxTablesInSelect() throws SQLException {
		return instance.getMaxTablesInSelect();
	}

	public int getMaxUserNameLength() throws SQLException {
		return instance.getMaxUserNameLength();
	}

	public int getDefaultTransactionIsolation() throws SQLException {
		return instance.getDefaultTransactionIsolation();
	}

	public boolean supportsTransactions() throws SQLException {
		return instance.supportsTransactions();
	}

	public boolean supportsTransactionIsolationLevel(int level)
			throws SQLException {
		return instance.supportsTransactionIsolationLevel(level);
	}

	public boolean supportsDataDefinitionAndDataManipulationTransactions()
			throws SQLException {
		return instance.supportsDataDefinitionAndDataManipulationTransactions();
	}

	public boolean supportsDataManipulationTransactionsOnly()
			throws SQLException {
		return instance.supportsDataManipulationTransactionsOnly();
	}

	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		return instance.dataDefinitionCausesTransactionCommit();
	}

	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		return instance.dataDefinitionIgnoredInTransactions();
	}

	public ResultSet getProcedures(String catalog, String schemaPattern,
			String procedureNamePattern) throws SQLException {
		return instance.getProcedures(catalog, schemaPattern,
				procedureNamePattern);
	}

	public ResultSet getProcedureColumns(String catalog, String schemaPattern,
			String procedureNamePattern, String columnNamePattern)
			throws SQLException {
		return instance.getProcedureColumns(catalog, schemaPattern,
				procedureNamePattern, columnNamePattern);
	}

	public ResultSet getTables(String catalog, String schemaPattern,
			String tableNamePattern, String[] types) throws SQLException {
		return instance.getTables(catalog, schemaPattern, tableNamePattern,
				types);
	}

	public ResultSet getSchemas() throws SQLException {
		return instance.getSchemas();
	}

	public ResultSet getCatalogs() throws SQLException {
		return instance.getCatalogs();
	}

	public ResultSet getTableTypes() throws SQLException {
		return instance.getTableTypes();
	}

	public ResultSet getColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws SQLException {
		return instance.getColumns(catalog, schemaPattern, tableNamePattern,
				columnNamePattern);
	}

	public ResultSet getColumnPrivileges(String catalog, String schema,
			String table, String columnNamePattern) throws SQLException {
		return instance.getColumnPrivileges(catalog, schema, table,
				columnNamePattern);
	}

	public ResultSet getTablePrivileges(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		return instance.getTablePrivileges(catalog, schemaPattern,
				tableNamePattern);
	}

	public ResultSet getBestRowIdentifier(String catalog, String schema,
			String table, int scope, boolean nullable) throws SQLException {
		return instance.getBestRowIdentifier(catalog, schema, table, scope,
				nullable);
	}

	public ResultSet getVersionColumns(String catalog, String schema,
			String table) throws SQLException {
		return instance.getVersionColumns(catalog, schema, table);
	}

	public ResultSet getPrimaryKeys(String catalog, String schema, String table)
			throws SQLException {
		return instance.getPrimaryKeys(catalog, schema, table);
	}

	public ResultSet getImportedKeys(String catalog, String schema, String table)
			throws SQLException {
		return instance.getImportedKeys(catalog, schema, table);
	}

	public ResultSet getExportedKeys(String catalog, String schema, String table)
			throws SQLException {
		return instance.getExportedKeys(catalog, schema, table);
	}

	public ResultSet getCrossReference(String parentCatalog,
			String parentSchema, String parentTable, String foreignCatalog,
			String foreignSchema, String foreignTable) throws SQLException {
		return instance.getCrossReference(parentCatalog, parentSchema,
				parentTable, foreignCatalog, foreignSchema, foreignTable);
	}

	public ResultSet getTypeInfo() throws SQLException {
		return instance.getTypeInfo();
	}

	public ResultSet getIndexInfo(String catalog, String schema, String table,
			boolean unique, boolean approximate) throws SQLException {
		return instance.getIndexInfo(catalog, schema, table, unique,
				approximate);
	}

	public boolean supportsResultSetType(int type) throws SQLException {
		return instance.supportsResultSetType(type);
	}

	public boolean supportsResultSetConcurrency(int type, int concurrency)
			throws SQLException {
		return instance.supportsResultSetConcurrency(type, concurrency);
	}

	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		return instance.ownUpdatesAreVisible(type);
	}

	public boolean ownDeletesAreVisible(int type) throws SQLException {
		return instance.ownDeletesAreVisible(type);
	}

	public boolean ownInsertsAreVisible(int type) throws SQLException {
		return instance.ownInsertsAreVisible(type);
	}

	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		return instance.othersUpdatesAreVisible(type);
	}

	public boolean othersDeletesAreVisible(int type) throws SQLException {
		return instance.othersDeletesAreVisible(type);
	}

	public boolean othersInsertsAreVisible(int type) throws SQLException {
		return instance.othersInsertsAreVisible(type);
	}

	public boolean updatesAreDetected(int type) throws SQLException {
		return instance.updatesAreDetected(type);
	}

	public boolean deletesAreDetected(int type) throws SQLException {
		return instance.deletesAreDetected(type);
	}

	public boolean insertsAreDetected(int type) throws SQLException {
		return instance.insertsAreDetected(type);
	}

	public boolean supportsBatchUpdates() throws SQLException {
		return instance.supportsBatchUpdates();
	}

	public ResultSet getUDTs(String catalog, String schemaPattern,
			String typeNamePattern, int[] types) throws SQLException {
		return instance.getUDTs(catalog, schemaPattern, typeNamePattern, types);
	}

	public Connection getConnection() throws SQLException {
		return instance.getConnection();
	}

	public boolean supportsSavepoints() throws SQLException {
		return instance.supportsSavepoints();
	}

	public boolean supportsNamedParameters() throws SQLException {
		return instance.supportsNamedParameters();
	}

	public boolean supportsMultipleOpenResults() throws SQLException {
		return instance.supportsMultipleOpenResults();
	}

	public boolean supportsGetGeneratedKeys() throws SQLException {
		return instance.supportsGetGeneratedKeys();
	}

	public ResultSet getSuperTypes(String catalog, String schemaPattern,
			String typeNamePattern) throws SQLException {
		return instance.getSuperTypes(catalog, schemaPattern, typeNamePattern);
	}

	public ResultSet getSuperTables(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		return instance
				.getSuperTables(catalog, schemaPattern, tableNamePattern);
	}

	public ResultSet getAttributes(String catalog, String schemaPattern,
			String typeNamePattern, String attributeNamePattern)
			throws SQLException {
		return instance.getAttributes(catalog, schemaPattern, typeNamePattern,
				attributeNamePattern);
	}

	public boolean supportsResultSetHoldability(int holdability)
			throws SQLException {
		return instance.supportsResultSetHoldability(holdability);
	}

	public int getResultSetHoldability() throws SQLException {
		return instance.getResultSetHoldability();
	}

	public int getDatabaseMajorVersion() throws SQLException {
		return instance.getDatabaseMajorVersion();
	}

	public int getDatabaseMinorVersion() throws SQLException {
		return instance.getDatabaseMinorVersion();
	}

	public int getJDBCMajorVersion() throws SQLException {
		return instance.getJDBCMajorVersion();
	}

	public int getJDBCMinorVersion() throws SQLException {
		return instance.getJDBCMinorVersion();
	}

	public int getSQLStateType() throws SQLException {
		return instance.getSQLStateType();
	}

	public boolean locatorsUpdateCopy() throws SQLException {
		return instance.locatorsUpdateCopy();
	}

	public boolean supportsStatementPooling() throws SQLException {
		return instance.supportsStatementPooling();
	}

	public RowIdLifetime getRowIdLifetime() throws SQLException {
		return instance.getRowIdLifetime();
	}

	public ResultSet getSchemas(String catalog, String schemaPattern)
			throws SQLException {
		return instance.getSchemas(catalog, schemaPattern);
	}

	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		return instance.supportsStoredFunctionsUsingCallSyntax();
	}

	public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
		return instance.autoCommitFailureClosesAllResultSets();
	}

	public ResultSet getClientInfoProperties() throws SQLException {
		return instance.getClientInfoProperties();
	}

	public ResultSet getFunctions(String catalog, String schemaPattern,
			String functionNamePattern) throws SQLException {
		return instance.getFunctions(catalog, schemaPattern,
				functionNamePattern);
	}

	public ResultSet getFunctionColumns(String catalog, String schemaPattern,
			String functionNamePattern, String columnNamePattern)
			throws SQLException {
		return instance.getFunctionColumns(catalog, schemaPattern,
				functionNamePattern, columnNamePattern);
	}

	public ResultSet getPseudoColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws SQLException {
		return instance.getPseudoColumns(catalog, schemaPattern,
				tableNamePattern, columnNamePattern);
	}

	public boolean generatedKeyAlwaysReturned() throws SQLException {
		return instance.generatedKeyAlwaysReturned();
	}
	
}
