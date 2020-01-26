/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public DelegateMetaData(DatabaseMetaData metaData, DelegatedDataSource dataSource) {
        instance = metaData;
        this.dataSource = dataSource;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return instance.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return instance.isWrapperFor(iface);
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return instance.allProceduresAreCallable();
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return instance.allTablesAreSelectable();
    }

    @Override
    public String getURL() throws SQLException {
        return dataSource.getDelegateURL();
    }

    @Override
    public String getUserName() throws SQLException {
        return instance.getUserName();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return instance.isReadOnly();
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return instance.nullsAreSortedHigh();
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return instance.nullsAreSortedLow();
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return instance.nullsAreSortedAtStart();
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return instance.nullsAreSortedAtEnd();
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return instance.getDatabaseProductName();
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return instance.getDatabaseProductVersion();
    }

    @Override
    public String getDriverName() throws SQLException {
        return instance.getDriverName();
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return instance.getDriverVersion();
    }

    @Override
    public int getDriverMajorVersion() {
        return instance.getDriverMajorVersion();
    }

    @Override
    public int getDriverMinorVersion() {
        return instance.getDriverMinorVersion();
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return instance.usesLocalFiles();
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return instance.usesLocalFilePerTable();
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return instance.supportsMixedCaseIdentifiers();
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return instance.storesUpperCaseIdentifiers();
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return instance.storesLowerCaseIdentifiers();
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return instance.storesMixedCaseIdentifiers();
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return instance.supportsMixedCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return instance.storesUpperCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return instance.storesLowerCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return instance.storesMixedCaseQuotedIdentifiers();
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return instance.getIdentifierQuoteString();
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return instance.getSQLKeywords();
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return instance.getNumericFunctions();
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return instance.getStringFunctions();
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return instance.getSystemFunctions();
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return instance.getTimeDateFunctions();
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return instance.getSearchStringEscape();
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return instance.getExtraNameCharacters();
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return instance.supportsAlterTableWithAddColumn();
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return instance.supportsAlterTableWithDropColumn();
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return instance.supportsColumnAliasing();
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return instance.nullPlusNonNullIsNull();
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return instance.supportsConvert();
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return instance.supportsConvert(fromType, toType);
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return instance.supportsTableCorrelationNames();
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return instance.supportsDifferentTableCorrelationNames();
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return instance.supportsExpressionsInOrderBy();
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return instance.supportsOrderByUnrelated();
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return instance.supportsGroupBy();
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return instance.supportsGroupByUnrelated();
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return instance.supportsGroupByBeyondSelect();
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return instance.supportsLikeEscapeClause();
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return instance.supportsMultipleResultSets();
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return instance.supportsMultipleTransactions();
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return instance.supportsNonNullableColumns();
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return instance.supportsMinimumSQLGrammar();
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return instance.supportsCoreSQLGrammar();
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return instance.supportsExtendedSQLGrammar();
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return instance.supportsANSI92EntryLevelSQL();
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return instance.supportsANSI92IntermediateSQL();
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return instance.supportsANSI92FullSQL();
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return instance.supportsIntegrityEnhancementFacility();
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return instance.supportsOuterJoins();
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return instance.supportsFullOuterJoins();
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return instance.supportsLimitedOuterJoins();
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return instance.getSchemaTerm();
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return instance.getProcedureTerm();
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return instance.getCatalogTerm();
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return instance.isCatalogAtStart();
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return instance.getCatalogSeparator();
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return instance.supportsSchemasInDataManipulation();
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return instance.supportsSchemasInProcedureCalls();
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return instance.supportsSchemasInTableDefinitions();
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return instance.supportsSchemasInIndexDefinitions();
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return instance.supportsSchemasInPrivilegeDefinitions();
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return instance.supportsCatalogsInDataManipulation();
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return instance.supportsCatalogsInProcedureCalls();
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return instance.supportsCatalogsInTableDefinitions();
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return instance.supportsCatalogsInIndexDefinitions();
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return instance.supportsCatalogsInPrivilegeDefinitions();
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return instance.supportsPositionedDelete();
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return instance.supportsPositionedUpdate();
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return instance.supportsSelectForUpdate();
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return instance.supportsStoredProcedures();
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return instance.supportsSubqueriesInComparisons();
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return instance.supportsSubqueriesInExists();
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return instance.supportsSubqueriesInIns();
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return instance.supportsSubqueriesInQuantifieds();
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return instance.supportsCorrelatedSubqueries();
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return instance.supportsUnion();
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return instance.supportsUnionAll();
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return instance.supportsOpenCursorsAcrossCommit();
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return instance.supportsOpenCursorsAcrossRollback();
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return instance.supportsOpenStatementsAcrossCommit();
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return instance.supportsOpenStatementsAcrossRollback();
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return instance.getMaxBinaryLiteralLength();
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return instance.getMaxCharLiteralLength();
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return instance.getMaxColumnNameLength();
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return instance.getMaxColumnsInGroupBy();
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return instance.getMaxColumnsInIndex();
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return instance.getMaxColumnsInOrderBy();
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return instance.getMaxColumnsInSelect();
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return instance.getMaxColumnsInTable();
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return instance.getMaxConnections();
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return instance.getMaxCursorNameLength();
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return instance.getMaxIndexLength();
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return instance.getMaxSchemaNameLength();
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return instance.getMaxProcedureNameLength();
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return instance.getMaxCatalogNameLength();
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return instance.getMaxRowSize();
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return instance.doesMaxRowSizeIncludeBlobs();
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return instance.getMaxStatementLength();
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return instance.getMaxStatements();
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return instance.getMaxTableNameLength();
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return instance.getMaxTablesInSelect();
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return instance.getMaxUserNameLength();
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return instance.getDefaultTransactionIsolation();
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return instance.supportsTransactions();
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return instance.supportsTransactionIsolationLevel(level);
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return instance.supportsDataDefinitionAndDataManipulationTransactions();
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return instance.supportsDataManipulationTransactionsOnly();
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return instance.dataDefinitionCausesTransactionCommit();
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return instance.dataDefinitionIgnoredInTransactions();
    }

    @Override
    public ResultSet getProcedures(
            String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return instance.getProcedures(catalog, schemaPattern, procedureNamePattern);
    }

    @Override
    public ResultSet getProcedureColumns(
            String catalog,
            String schemaPattern,
            String procedureNamePattern,
            String columnNamePattern)
            throws SQLException {
        return instance.getProcedureColumns(
                catalog, schemaPattern, procedureNamePattern, columnNamePattern);
    }

    @Override
    public ResultSet getTables(
            String catalog, String schemaPattern, String tableNamePattern, String[] types)
            throws SQLException {
        return instance.getTables(catalog, schemaPattern, tableNamePattern, types);
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return instance.getSchemas();
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        return instance.getCatalogs();
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        return instance.getTableTypes();
    }

    @Override
    public ResultSet getColumns(
            String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        return instance.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
    }

    @Override
    public ResultSet getColumnPrivileges(
            String catalog, String schema, String table, String columnNamePattern)
            throws SQLException {
        return instance.getColumnPrivileges(catalog, schema, table, columnNamePattern);
    }

    @Override
    public ResultSet getTablePrivileges(
            String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return instance.getTablePrivileges(catalog, schemaPattern, tableNamePattern);
    }

    @Override
    public ResultSet getBestRowIdentifier(
            String catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        return instance.getBestRowIdentifier(catalog, schema, table, scope, nullable);
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table)
            throws SQLException {
        return instance.getVersionColumns(catalog, schema, table);
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException {
        return instance.getPrimaryKeys(catalog, schema, table);
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException {
        return instance.getImportedKeys(catalog, schema, table);
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException {
        return instance.getExportedKeys(catalog, schema, table);
    }

    @Override
    public ResultSet getCrossReference(
            String parentCatalog,
            String parentSchema,
            String parentTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable)
            throws SQLException {
        return instance.getCrossReference(
                parentCatalog,
                parentSchema,
                parentTable,
                foreignCatalog,
                foreignSchema,
                foreignTable);
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return instance.getTypeInfo();
    }

    @Override
    public ResultSet getIndexInfo(
            String catalog, String schema, String table, boolean unique, boolean approximate)
            throws SQLException {
        return instance.getIndexInfo(catalog, schema, table, unique, approximate);
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        return instance.supportsResultSetType(type);
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return instance.supportsResultSetConcurrency(type, concurrency);
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return instance.ownUpdatesAreVisible(type);
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return instance.ownDeletesAreVisible(type);
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return instance.ownInsertsAreVisible(type);
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return instance.othersUpdatesAreVisible(type);
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return instance.othersDeletesAreVisible(type);
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return instance.othersInsertsAreVisible(type);
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return instance.updatesAreDetected(type);
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return instance.deletesAreDetected(type);
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return instance.insertsAreDetected(type);
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return instance.supportsBatchUpdates();
    }

    @Override
    public ResultSet getUDTs(
            String catalog, String schemaPattern, String typeNamePattern, int[] types)
            throws SQLException {
        return instance.getUDTs(catalog, schemaPattern, typeNamePattern, types);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return instance.getConnection();
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return instance.supportsSavepoints();
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return instance.supportsNamedParameters();
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return instance.supportsMultipleOpenResults();
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return instance.supportsGetGeneratedKeys();
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)
            throws SQLException {
        return instance.getSuperTypes(catalog, schemaPattern, typeNamePattern);
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern)
            throws SQLException {
        return instance.getSuperTables(catalog, schemaPattern, tableNamePattern);
    }

    @Override
    public ResultSet getAttributes(
            String catalog,
            String schemaPattern,
            String typeNamePattern,
            String attributeNamePattern)
            throws SQLException {
        return instance.getAttributes(
                catalog, schemaPattern, typeNamePattern, attributeNamePattern);
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return instance.supportsResultSetHoldability(holdability);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return instance.getResultSetHoldability();
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return instance.getDatabaseMajorVersion();
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return instance.getDatabaseMinorVersion();
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return instance.getJDBCMajorVersion();
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return instance.getJDBCMinorVersion();
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return instance.getSQLStateType();
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return instance.locatorsUpdateCopy();
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return instance.supportsStatementPooling();
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return instance.getRowIdLifetime();
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return instance.getSchemas(catalog, schemaPattern);
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return instance.supportsStoredFunctionsUsingCallSyntax();
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return instance.autoCommitFailureClosesAllResultSets();
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return instance.getClientInfoProperties();
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
            throws SQLException {
        return instance.getFunctions(catalog, schemaPattern, functionNamePattern);
    }

    @Override
    public ResultSet getFunctionColumns(
            String catalog,
            String schemaPattern,
            String functionNamePattern,
            String columnNamePattern)
            throws SQLException {
        return instance.getFunctionColumns(
                catalog, schemaPattern, functionNamePattern, columnNamePattern);
    }

    @Override
    public ResultSet getPseudoColumns(
            String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        return instance.getPseudoColumns(
                catalog, schemaPattern, tableNamePattern, columnNamePattern);
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return instance.generatedKeyAlwaysReturned();
    }
}
