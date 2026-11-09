# DatabaseUtil API 深度解析

## 📚 目录

*   [**`LoggerCore` (日志核心)**](#loggercore-日志核心)
    *   [`setDebugLogEnabled(boolean enabled)`](#setdebuglogenabledboolean-enabled)
    *   [`setSqlDebugLogEnabled(boolean enabled)`](#setsqldebuglogenabledboolean-enabled)
    *   [`isDebugLogEnabled()`](#isdebuglogenabled)
    *   [`isSqlDebugLogEnabled()`](#issqldebuglogenabled)
    *   [`debugLog(String message)`](#debuglogstring-message)
    *   [`infoLog(String message)`](#infologstring-message)
    *   [`warnLog(String message)`](#warnlogstring-message)
    *   [`debugErrorLog(String message)`](#debugerrorlogstring-message)
    *   [`logException(String context, Exception e)`](#logexceptionstring-context-exception-e)
    *   [`formatSqlWithParams(String sql, Object... params)`](#formatsqlwithparamsstring-sql-object-params)
    *   [`debugSqlWithParams(String title, String sql, Object... params)`](#debugsqlwithparamsstring-title-string-sql-object-params)
    *   [`debugSqlExecution(String operationType, String dbName, String sql, Object[] params, Object result)`](#debugsqlexecutionstring-operationtype-string-dbname-string-sql-object-params-object-result)
*   [**`DataSourceCore` (数据源核心)**](#datasourcecore-数据源核心)
    *   [`register(String dbName, String dbUrl, String username, String password, String driver)`](#registerstring-dbname-string-dburl-string-username-string-password-string-driver)
    *   [`getConnection()`](#getconnection)
    *   [`getConnection(String dbName)`](#getconnectionstring-dbname)
    *   [`closeDatabase(String dbName)`](#closedatabasestring-dbname)
    *   [`closeAllDatabases()`](#closealldatabases)
    *   [`getRegisteredDatabases()`](#getregistereddatabases)
    *   [`getDataSourceNames()`](#getdatasourcenames)
    *   [`getDefaultDatabaseName()`](#getdefaultdatabasename)
    *   [`isDatabaseRegistered(String dataSourceName)`](#isdatabaseregisteredstring-datasourcename)
    *   [`testConnection()`](#testconnection)
    *   [`testConnection(String dbName)`](#testconnectionstring-dbname)
    *   [`setDefaultDataSource(String dataSourceName)`](#setdefaultdatasourcestring-datasourcename)
    *   [`setThreadDataSourceContext(String dataSourceName)`](#setthreaddatasourcecontextstring-datasourcename)
    *   [`clearThreadDataSourceContext()`](#clearthreaddatasourcecontext)
    *   [`getCurrentDataSourceName()`](#getcurrentdatasourcename)
    *   [`getDataSourceType(String dbName)`](#getdatasourcetypestring-dbname)
    *   [`getAllDataSourceInfo()`](#getalldatasourceinfo)
*   [**`SimpleSqlParser` (简单SQL解析器)**](#simplesqlparser-简单sql解析器)
    *   [`SimpleSqlParameterConverter.convertDirectSql(String sql, Map<String, Object> params)`](#simplesqlparameterconverterconvertdirectsqlstring-sql-mapstring-object-params)
    *   [`SimpleSqlParameterConverter.convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params)`](#simplesqlparameterconverterconvertxmlfilestring-xmlfilename-string-sqlid-mapstring-object-params)
    *   [`SimpleSqlParameterConverter.convertXmlAuto(String sqlId, Map<String, Object> params)`](#simplesqlparameterconverterconvertxmlautostring-sqlid-mapstring-object-params)
*   [**`MyBatisSqlParser` (MyBatis SQL解析器)**](#mybatissqlparser-mybatis-sql解析器)
    *   [`MyBatisSqlParameterConverter.convertDirectSql(String sql, Map<String, Object> params)`](#mybatissqlparameterconverterconvertdirectsqlstring-sql-mapstring-object-params)
    *   [`MyBatisSqlParameterConverter.convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params)`](#mybatissqlparameterconverterconvertxmlfilestring-xmlfilename-string-sqlid-mapstring-object-params)
    *   [`MyBatisSqlParameterConverter.convertXmlAuto(String sqlId, Map<String, Object> params)`](#mybatissqlparameterconverterconvertxmlautostring-sqlid-mapstring-object-params)
    *   [`parse(String sql, Map<String, Object> params)`](#parsestring-sql-mapstring-object-params)
*   [**`CommonSqlExecutor` (通用SQL执行器)**](#commonsqlexecutor-通用sql执行器)
    *   [`executeDDL(String dbName, String sql)`](#executeddlstring-dbname-string-sql)
    *   [`executeDDL(Connection conn, String sql)`](#executeddlconnection-conn-string-sql)
    *   [`executeDQL(String dbName, String sql)`](#executedqlstring-dbname-string-sql)
    *   [`executeDQL(Connection conn, String sql)`](#executedqlconnection-conn-string-sql)
    *   [`executeDML(String dbName, String sql)`](#executedmlstring-dbname-string-sql)
    *   [`executeDML(Connection conn, String sql)`](#executedmlconnection-conn-string-sql)
*   [**`SimpleSqlExecutor` (简单SQL执行器)**](#simplesqlexecutor-简单sql执行器)
    *   [`selectList(String dbName, String sql, Map<String, Object> params)`](#simplesqlexecutor-selectlist)
    *   [`selectOne(String dbName, String sql, Map<String, Object> params)`](#simplesqlexecutor-selectone)
    *   [`execute(String dbName, String sql, Map<String, Object> params)`](#simplesqlexecutor-execute)
    *   [`selectListFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`](#simplesqlexecutor-selectlistfromxml)
    *   [`selectOneFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`](#simplesqlexecutor-selectonefromxml)
    *   [`executeFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`](#simplesqlexecutor-executefromxml)
    *   [`selectListFromXmlAuto(String dbName, String sqlId, Map<String, Object> params)`](#simplesqlexecutor-selectlistfromxmlauto)
    *   [`selectOneFromXmlAuto(String dbName, String sqlId, Map<String, Object> params)`](#simplesqlexecutor-selectonefromxmlauto)
    *   [`executeFromXmlAuto(String dbName, String sqlId, Map<String, Object> params)`](#simplesqlexecutor-executefromxmlauto)
*   [**`MyBatisSqlExecutor` (MyBatis SQL执行器)**](#mybatissqlexecutor-mybatis-sql执行器)
    *   [`selectListByMp(String dbName, String sql, Map<String, Object> params)`](#mybatissqlexecutor-selectlistbymp)
    *   [`selectOneByMp(String dbName, String sql, Map<String, Object> params)`](#mybatissqlexecutor-selectonebymp)
    *   [`executeByMp(String dbName, String sql, Map<String, Object> params)`](#mybatissqlexecutor-executebymp)
    *   [`selectListFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`](#mybatissqlexecutor-selectlistfromxmlbymp)
    *   [`selectOneFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`](#mybatissqlexecutor-selectonefromxmlbymp)
    *   [`executeFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`](#mybatissqlexecutor-executefromxmlbymp)
    *   [`selectListFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params)`](#mybatissqlexecutor-selectlistfromxmlautobymp)
    *   [`selectOneFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params)`](#mybatissqlexecutor-selectonefromxmlautobymp)
    *   [`executeFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params)`](#mybatissqlexecutor-executefromxmlautobymp)
*   [**`PaginationHandler` (分页处理器)**](#paginationhandler-分页处理器)
    *   [`getStrategy(DatabaseType dbType, String sql)`](#paginationhandler-getstrategy)
    *   [`getAvailableStrategies()`](#paginationhandler-getavailablestrategies)
    *   [`testStrategySelection(DatabaseType dbType, String sql)`](#paginationhandler-teststrategyselection)
*   [**`BatchOperationHandler` (批量操作处理器)**](#batchoperationhandler-批量操作处理器)
    *   [`getOptimalBatchSize(DatabaseType dbType, int dataSize)`](#batchoperationhandler-getoptimalbatchsize)
    *   [`batchUpsert(String dbName, String tableName, List<T> entities, String[] keyColumns, String[] valueColumns)`](#batchoperationhandler-batchupsert)
*   [**`DatabaseUtil` (主类)**](#DatabaseUtil-主类)
    *   [链式访问器](#链式访问器)
        *   [`accessor()`](#accessor)
        *   [`accessor(String dataSourceName)`](#accessorstring-datasourcename)
    *   [直接数据库操作 (DDL, DQL, DML)](#直接数据库操作-ddl-dql-dml)
        *   [`executeDDL(String sql)`](#DatabaseUtil-executeddl)
        *   [`executeDQL(String sql)`](#DatabaseUtil-executedql)
        *   [`executeDML(String sql)`](#DatabaseUtil-executedml)
    *   [简单SQL - API 1: 直接SQL](#简单sql---api-1-直接sql)
        *   [`selectList(String sql, Map<String, Object> params)`](#DatabaseUtil-selectlist)
        *   [`selectOne(String sql, Map<String, Object> params)`](#DatabaseUtil-selectone)
        *   [`execute(String sql, Map<String, Object> params)`](#DatabaseUtil-execute)
        *   [`selectPage(String sql, int pageNum, int pageSize, Map<String, Object> params)`](#DatabaseUtil-selectpage)
        *   [`batchExecute(String sql, List<Map<String, Object>> paramsList, int batchSize)`](#DatabaseUtil-batchexecute)
    *   [简单SQL - API 2: 指定XML](#简单sql---api-2-指定xml)
        *   [`selectListFromXml(String xmlFileName, String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectlistfromxml)
        *   [`selectOneFromXml(String xmlFileName, String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectonefromxml)
        *   [`executeFromXml(String xmlFileName, String sqlId, Map<String, Object> params)`](#DatabaseUtil-executefromxml)
        *   [`selectPageFromXml(String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params)`](#DatabaseUtil-selectpagefromxml)
        *   [`batchExecuteFromXml(String xmlFileName, String sqlId, List<Map<String, Object>> paramsList, int batchSize)`](#DatabaseUtil-batchexecutefromxml)
    *   [简单SQL - API 3: 自动搜索XML](#简单sql---api-3-自动搜索xml)
        *   [`selectListFromXmlAuto(String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectlistfromxmlauto)
        *   [`selectOneFromXmlAuto(String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectonefromxmlauto)
        *   [`executeFromXmlAuto(String sqlId, Map<String, Object> params)`](#DatabaseUtil-executefromxmlauto)
        *   [`selectPageFromXmlAuto(String sqlId, int pageNum, int pageSize, Map<String, Object> params)`](#DatabaseUtil-selectpagefromxmlauto)
        *   [`batchExecuteFromXmlAuto(String sqlId, List<Map<String, Object>> paramsList, int batchSize)`](#DatabaseUtil-batchexecutefromxmlauto)
    *   [MyBatis SQL - API 1: 直接SQL](#mybatis-sql---api-1-直接sql)
        *   [`selectListByMp(String sql, Map<String, Object> params)`](#DatabaseUtil-selectlistbymp)
        *   [`selectOneByMp(String sql, Map<String, Object> params)`](#DatabaseUtil-selectonebymp)
        *   [`executeByMp(String sql, Map<String, Object> params)`](#DatabaseUtil-executebymp)
    *   [MyBatis SQL - API 2: 指定XML](#mybatis-sql---api-2-指定xml)
        *   [`selectListFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectlistfromxmlbymp)
        *   [`selectOneFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectonefromxmlbymp)
        *   [`executeFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params)`](#DatabaseUtil-executefromxmlbymp)
    *   [MyBatis SQL - API 3: 自动搜索XML](#mybatis-sql---api-3-自动搜索xml)
        *   [`selectListFromXmlAutoByMp(String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectlistfromxmlautobymp)
        *   [`selectOneFromXmlAutoByMp(String sqlId, Map<String, Object> params)`](#DatabaseUtil-selectonefromxmlautobymp)
        *   [`executeFromXmlAutoByMp(String sqlId, Map<String, Object> params)`](#DatabaseUtil-executefromxmlautobymp)
*   [**`DatabaseAccessor` (链式访问器)**](#databaseaccessor-链式访问器)
    *   [事务管理](#事务管理)
        *   [`result()`](#result)
        *   [`resultAll()`](#resultall)
        *   [`setTransaction()`](#settransaction)
        *   [`commit()`](#commit)
        *   [`rollback()`](#rollback)
        *   [`isInTransaction()`](#isintransaction)
    *   [数据库操作 (DDL, DQL, DML)](#数据库操作-ddl-dql-dml-1)
        *   [`executeDDL(String sql)`](#databaseaccessor-executeddl)
        *   [`executeDQL(String sql)`](#databaseaccessor-executedql)
        *   [`executeDML(String sql)`](#databaseaccessor-executedml)
    *   [数据库操作 (简单SQL)](#数据库操作-简单sql)
        *   [`selectList(String sql, Map<String, Object> params)`](#databaseaccessor-selectlist)
        *   [`selectOne(String sql, Map<String, Object> params)`](#databaseaccessor-selectone)
        *   [`execute(String sql, Map<String, Object> params)`](#databaseaccessor-execute)
        *   [`selectPage(String sql, int pageNum, int pageSize, Map<String, Object> params)`](#databaseaccessor-selectpage)
        *   [`batchExecute(String sql, List<Map<String, Object>> paramsList, int batchSize)`](#databaseaccessor-batchexecute)
    *   [数据库操作 (MyBatis SQL)](#数据库操作-mybatis-sql)
        *   [`selectListByMp(String sql, Map<String, Object> params)`](#databaseaccessor-selectlistbymp)
        *   [`selectOneByMp(String sql, Map<String, Object> params)`](#databaseaccessor-selectonebymp)
        *   [`executeByMp(String sql, Map<String, Object> params)`](#databaseaccessor-executebymp)

## `LoggerCore` (日志核心)

`LoggerCore` 是一个静态内部类，提供了整个工具的日志记录功能。它支持分级别的日志输出，并能格式化SQL语句以便于调试。

### `setDebugLogEnabled(boolean enabled)`

*   **功能**：开启或关闭普通调试日志。
*   **参数**：`enabled` (boolean) - `true` 为开启, `false` 为关闭。

### `setSqlDebugLogEnabled(boolean enabled)`

*   **功能**：开启或关闭SQL执行日志。
*   **参数**：`enabled` (boolean) - `true` 为开启, `false` 为关闭。

### `isDebugLogEnabled()`

*   **功能**：检查普通调试日志是否已开启。
*   **返回**：`boolean`。

### `isSqlDebugLogEnabled()`

*   **功能**：检查SQL执行日志是否已开启。
*   **返回**：`boolean`。

### `debugLog(String message)`

*   **功能**：记录一条调试级别的日志。

### `infoLog(String message)`

*   **功能**：记录一条信息级别的日志。

### `warnLog(String message)`

*   **功能**：记录一条警告级别的日志。

### `debugErrorLog(String message)`

*   **功能**：记录一条错误级别的日志。

### `logException(String context, Exception e)`

*   **功能**：记录一个异常的详细信息。
*   **参数**：
    *   `context` (String): 异常发生的上下文描述。
    *   `e` (Exception): 异常对象。

### `formatSqlWithParams(String sql, Object... params)`

*   **功能**：将一个带 `?` 占位符的SQL和其参数列表格式化为一条完整的、可执行的SQL语句字符串。
*   **返回**：`String` - 格式化后的SQL。

### `debugSqlWithParams(String title, String sql, Object... params)`

*   **功能**：以调试级别记录一条格式化后的SQL。

### `debugSqlExecution(String operationType, String dbName, String sql, Object[] params, Object result)`

*   **功能**：记录一次完整的SQL操作详情，包括操作类型、数据库、参数、完整SQL和执行结果。

## `DataSourceCore` (数据源核心)

`DataSourceCore` 负责管理一个或多个数据库连接池。它处理数据源的注册、获取、关闭和状态监控，并支持多数据源环境下的线程级切换。

### `register(String dbName, String dbUrl, String username, String password, String driver)`

*   **功能**：注册一个新的数据源。
*   **参数**：
    *   `dbName` (String): 数据源的唯一名称。
    *   `dbUrl` (String): JDBC URL。
    *   `username` (String): 用户名。
    *   `password` (String): 密码。
    *   `driver` (String): JDBC驱动类名。
*   **返回**：`DatabaseAccessor` - 一个绑定到新注册数据源的访问器实例，方便链式调用。

### `getConnection()`

*   **功能**：获取默认数据源的数据库连接。
*   **返回**：`Connection`。

### `getConnection(String dbName)`

*   **功能**：获取指定名称的数据源的数据库连接。
*   **返回**：`Connection`。

### `closeDatabase(String dbName)`

*   **功能**：关闭指定的数据源并从注册中移除。

### `closeAllDatabases()`

*   **功能**：关闭所有已注册的数据源。

### `getRegisteredDatabases()`

*   **功能**：获取所有已注册数据源名称的列表。
*   **返回**：`List<String>`。

### `getDataSourceNames()`

*   **功能**：获取所有已注册数据源名称的集合。
*   **返回**：`Set<String>`。

### `getDefaultDatabaseName()`

*   **功能**：获取当前全局默认数据源的名称。
*   **返回**：`String`。

### `isDatabaseRegistered(String dataSourceName)`

*   **功能**：检查指定名称的数据源是否已注册。
*   **返回**：`boolean`。

### `testConnection()`

*   **功能**：测试与默认数据源的连接是否通畅。
*   **返回**：`boolean`。

### `testConnection(String dbName)`

*   **功能**：测试与指定数据源的连接是否通畅。
*   **返回**：`boolean`。

### `setDefaultDataSource(String dataSourceName)`

*   **功能**：设置一个新的全局默认数据源。
*   **返回**：`boolean` - 如果设置成功。

### `setThreadDataSourceContext(String dataSourceName)`

*   **功能**：为当前线程设置一个特定的数据源。一旦设置，该线程后续所有数据库操作都将使用此数据源，直到被清除。
*   **注意**：这是一个高级功能，使用后务必在 `finally` 块中调用 `clearThreadDataSourceContext()` 以避免连接泄漏或混乱。

### `clearThreadDataSourceContext()`

*   **功能**：清除当前线程的数据源设置，使其恢复到使用全局默认数据源。

### `getCurrentDataSourceName()`

*   **功能**：获取当前操作生效的数据源名称（优先返回线程设置，否则返回全局默认）。
*   **返回**：`String`。

### `getDataSourceType(String dbName)`

*   **功能**：获取指定数据源的类型信息（例如，"简单数据源"）。
*   **返回**：`String`。

### `getAllDataSourceInfo()`

*   **功能**：获取所有已注册数据源的详细信息，包括类型、是否为默认、连接测试结果等。
*   **返回**：`Map<String, Map<String, Object>>`。

## `SimpleSqlParser` (简单SQL解析器)

`SimpleSqlParser` 负责解析包含 `${...}` (直接替换) 和 `#{...}` (参数化) 占位符的SQL语句，并处理简单的动态SQL标签（如 `<if>`)。

### `SimpleSqlParameterConverter.convertDirectSql(String sql, Map<String, Object> params)`

*   **功能**：转换直接传入的SQL字符串。
*   **返回**：`SimpleSqlWithParams` - 包含JDBC格式的SQL和参数列表的对象。

### `SimpleSqlParameterConverter.convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定的XML文件中加载SQL ID对应的内容，并进行转换。
*   **返回**：`SimpleSqlWithParams`。

### `SimpleSqlParameterConverter.convertXmlAuto(String sqlId, Map<String, Object> params)`

*   **功能**：在所有已缓存的XML文件中自动搜索SQL ID并进行转换。
*   **返回**：`SimpleSqlWithParams`。

## `MyBatisSqlParser` (MyBatis SQL解析器)

`MyBatisSqlParser` 是一个功能强大的解析器，它兼容大部分标准的MyBatis动态SQL语法，如 `<if>`, `<where>`, `<set>`, `<foreach>`, `<choose>`, `<trim>`, `<bind>` 和 `<include>`。它使用JEXL引擎来评估表达式，提供了高度的灵活性。

### `MyBatisSqlParameterConverter.convertDirectSql(String sql, Map<String, Object> params)`

*   **功能**：转换直接传入的、包含MyBatis动态标签的SQL字符串。
*   **返回**：`MyBatisSqlWithParams` - 包含JDBC格式的SQL和参数列表的对象。

### `MyBatisSqlParameterConverter.convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定的XML文件中加载SQL ID对应的内容，并进行MyBatis语法解析和转换。
*   **返回**：`MyBatisSqlWithParams`。

### `MyBatisSqlParameterConverter.convertXmlAuto(String sqlId, Map<String, Object> params)`

*   **功能**：在所有已缓存的MyBatis XML文件中自动搜索SQL ID并进行解析转换。
*   **返回**：`MyBatisSqlWithParams`。

### `parse(String sql, Map<String, Object> params)`

*   **功能**：这是 `MyBatisSqlParser` 的核心解析方法，负责处理所有动态标签和参数。
*   **返回**：`MyBatisSqlWithParams`。

## `CommonSqlExecutor` (通用SQL执行器)

`CommonSqlExecutor` 用于执行不带任何参数的、原生的SQL语句。它主要用于DDL（数据定义语言）操作或非常简单的DQL/DML查询。

### `executeDDL(String dbName, String sql)`

*   **功能**：在指定的数据库上执行一条DDL语句（如 `CREATE TABLE`）。
*   **返回**：`boolean`。

### `executeDDL(Connection conn, String sql)`

*   **功能**：使用一个已有的数据库连接执行一条DDL语句。
*   **返回**：`boolean`。

### `executeDQL(String dbName, String sql)`

*   **功能**：在指定的数据库上执行一条DQL查询语句。
*   **返回**：`List<Map<String, Object>>` - 查询结果集。

### `executeDQL(Connection conn, String sql)`

*   **功能**：使用一个已有的数据库连接执行一条DQL查询。
*   **返回**：`List<Map<String, Object>>`。

### `executeDML(String dbName, String sql)`

*   **功能**：在指定的数据库上执行一条DML（插入、更新、删除）语句。
*   **返回**：`int` - 受影响的行数。

### `executeDML(Connection conn, String sql)`

*   **功能**：使用一个已有的数据库连接执行一条DML语句。
*   **返回**：`int` - 受影响的行数。

## `SimpleSqlExecutor` (简单SQL执行器)

`SimpleSqlExecutor` 是执行由 `SimpleSqlParser` 解析后的SQL的执行器。它封装了JDBC的 `PreparedStatement` 的创建、参数设置、执行和结果集处理。

### `selectList(String dbName, String sql, Map<String, Object> params)`

*   **功能**：执行一个查询，并返回多行结果。
*   **返回**：`List<Map<String, Object>>`。

### `selectOne(String dbName, String sql, Map<String, Object> params)`

*   **功能**：执行一个查询，并返回单行或零行结果。
*   **返回**：`Map<String, Object>`。

### `execute(String dbName, String sql, Map<String, Object> params)`

*   **功能**：执行一个DML操作（INSERT, UPDATE, DELETE）。
*   **返回**：`int` - 受影响的行数。

### `selectListFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定XML文件中获取SQL并执行查询，返回多行结果。

### `selectOneFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定XML文件中获取SQL并执行查询，返回单行结果。

### `executeFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定XML文件中获取SQL并执行DML操作。

### `selectListFromXmlAuto(String dbName, String sqlId, Map<String, Object> params)`

*   **功能**：自动在所有XML中搜索SQL并执行查询，返回多行结果。

### `selectOneFromXmlAuto(String dbName, String sqlId, Map<String, Object> params)`

*   **功能**：自动在所有XML中搜索SQL并执行查询，返回单行结果。

### `executeFromXmlAuto(String dbName, String sqlId, Map<String, Object> params)`

*   **功能**：自动在所有XML中搜索SQL并执行DML操作。

## `MyBatisSqlExecutor` (MyBatis SQL执行器)

`MyBatisSqlExecutor` 是与 `MyBatisSqlParser` 配套的执行器，专门用于执行经过MyBatis语法解析后的SQL。

### `selectListByMp(String dbName, String sql, Map<String, Object> params)`

*   **功能**：执行一个MyBatis SQL查询，并返回多行结果。
*   **返回**：`List<Map<String, Object>>`。

### `selectOneByMp(String dbName, String sql, Map<String, Object> params)`

*   **功能**：执行MyBatis SQL查询，返回单行结果。

### `executeByMp(String dbName, String sql, Map<String, Object> params)`

*   **功能**：执行MyBatis SQL的DML操作。

### `selectListFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定XML文件中获取MyBatis SQL并执行查询，返回多行。

### `selectOneFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定XML文件中获取MyBatis SQL并执行查询，返回单行。

### `executeFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params)`

*   **功能**：从指定XML文件中获取MyBatis SQL并执行DML。

### `selectListFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params)`

*   **功能**：自动在所有XML中搜索MyBatis SQL并执行查询，返回多行。

### `selectOneFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params)`

*   **功能**：自动在所有XML中搜索MyBatis SQL并执行查询，返回单行。

### `executeFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params)`

*   **功能**：自动在所有XML中搜索MyBatis SQL并执行DML。

## `PaginationHandler` (分页处理器)

`PaginationHandler` 采用策略模式，负责将普通查询自动转换为对应数据库（如MySQL, Oracle）的物理分页查询。

### `getStrategy(DatabaseType dbType, String sql)`

*   **功能**：根据数据库类型和原始SQL获取一个分页策略实例。
*   **返回**：`PaginationStrategy`。

### `getAvailableStrategies()`

*   **功能**：获取当前所有已注册的分页策略名称。
*   **返回**：`List<String>`。

### `testStrategySelection(DatabaseType dbType, String sql)`

*   **功能**：一个测试方法，用于验证针对特定数据库和SQL是否能正确选出分页策略。
*   **返回**：`String` - 被选中的策略类名。

## `BatchOperationHandler` (批量操作处理器)

`BatchOperationHandler` 提供了高效执行批量数据操作（特别是“插入或更新”，即Upsert）的能力。

### `getOptimalBatchSize(DatabaseType dbType, int dataSize)`

*   **功能**：为特定数据库和数据量计算推荐的批处理大小。
*   **返回**：`int` - 建议的批次大小。

### `batchUpsert(String dbName, String tableName, List<T> entities, String[] keyColumns, String[] valueColumns)`

*   **功能**：执行批量的“插入或更新”操作。它能根据数据库类型自动生成MERGE或ON DUPLICATE KEY UPDATE等语句。
*   **返回**：`BatchResult` - 包含执行结果的详细信息。

## `DatabaseUtil` (主类)

`DatabaseUtil` 是整个工具的核心静态入口，提供了所有数据库操作的便捷方法。

### 链式访问器

### `accessor()`
*   **功能**：获取一个使用默认数据源的数据库访问器实例。
*   **返回**：`DatabaseAccessor`。

### `accessor(String dataSourceName)`
*   **功能**：获取一个使用指定数据源的数据库访问器实例。
*   **返回**：`DatabaseAccessor`。

---
### 直接数据库操作 (DDL, DQL, DML)

### `executeDDL(String sql)`
*   **功能**：在默认数据源上执行一条DDL语句。

### `executeDQL(String sql)`
*   **功能**：在默认数据源上执行一条DQL查询。
*   **返回**：`List<Map<String, Object>>`。

### `executeDML(String sql)`
*   **功能**：在默认数据源上执行一条DML语句。
*   **返回**：`int` - 受影响的行数。

---
### 简单SQL - API 1: 直接SQL

### `selectList(String sql, Map<String, Object> params)`
*   **功能**：执行查询并返回多行结果。

### `selectOne(String sql, Map<String, Object> params)`
*   **功能**：执行查询并返回单行结果。

### `execute(String sql, Map<String, Object> params)`
*   **功能**：执行DML操作。

### `selectPage(String sql, int pageNum, int pageSize, Map<String, Object> params)`
*   **功能**：执行分页查询。
*   **返回**：`PageResult<Map<String, Object>>`。

### `batchExecute(String sql, List<Map<String, Object>> paramsList, int batchSize)`
*   **功能**：批量执行DML操作。
*   **返回**：`BatchResult`。

---
### 简单SQL - API 2: 指定XML

### `selectListFromXml(String xmlFileName, String sqlId, Map<String, Object> params)`
*   **功能**：从指定XML加载SQL，执行查询并返回多行。

### `selectOneFromXml(String xmlFileName, String sqlId, Map<String, Object> params)`
*   **功能**：从指定XML加载SQL，执行查询并返回单行。

### `executeFromXml(String xmlFileName, String sqlId, Map<String, Object> params)`
*   **功能**：从指定XML加载SQL，执行DML操作。

### `selectPageFromXml(String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params)`
*   **功能**：从指定XML加载SQL，执行分页查询。

### `batchExecuteFromXml(String xmlFileName, String sqlId, List<Map<String, Object>> paramsList, int batchSize)`
*   **功能**：从指定XML加载SQL，批量执行DML操作。

---
### 简单SQL - API 3: 自动搜索XML

### `selectListFromXmlAuto(String sqlId, Map<String, Object> params)`
*   **功能**：自动搜索SQL ID，执行查询并返回多行。

### `selectOneFromXmlAuto(String sqlId, Map<String, Object> params)`
*   **功能**：自动搜索SQL ID，执行查询并返回单行。

### `executeFromXmlAuto(String sqlId, Map<String, Object> params)`
*   **功能**：自动搜索SQL ID，执行DML操作。

### `selectPageFromXmlAuto(String sqlId, int pageNum, int pageSize, Map<String, Object> params)`
*   **功能**：自动搜索SQL ID，执行分页查询。

### `batchExecuteFromXmlAuto(String sqlId, List<Map<String, Object>> paramsList, int batchSize)`
*   **功能**：自动搜索SQL ID，批量执行DML操作。

---
### MyBatis SQL - API 1: 直接SQL

### `selectListByMp(String sql, Map<String, Object> params)`
*   **功能**：执行MyBatis语法的查询，返回多行。

### `selectOneByMp(String sql, Map<String, Object> params)`
*   **功能**：执行MyBatis语法的查询，返回单行。

### `executeByMp(String sql, Map<String, Object> params)`
*   **功能**：执行MyBatis语法的DML操作。

---
### MyBatis SQL - API 2: 指定XML

### `selectListFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params)`
*   **功能**：从指定XML加载MyBatis SQL，执行查询并返回多行。

### `selectOneFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params)`
*   **功能**：从指定XML加载MyBatis SQL，执行查询并返回单行。

### `executeFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params)`
*   **功能**：从指定XML加载MyBatis SQL，执行DML操作。

---
### MyBatis SQL - API 3: 自动搜索XML

### `selectListFromXmlAutoByMp(String sqlId, Map<String, Object> params)`
*   **功能**：自动搜索MyBatis SQL ID，执行查询并返回多行。

### `selectOneFromXmlAutoByMp(String sqlId, Map<String, Object> params)`
*   **功能**：自动搜索MyBatis SQL ID，执行查询并返回单行。

### `executeFromXmlAutoByMp(String sqlId, Map<String, Object> params)`
*   **功能**：自动搜索MyBatis SQL ID，执行DML操作。

## `DatabaseAccessor` (链式访问器)

`DatabaseAccessor` 提供了链式调用的能力，并封装了对特定数据源的数据库操作，包括强大的事务管理功能。

### 事务管理

### `result()`
*   **功能**：结束事务并返回事务期间第一个DML或DQL操作的结果。
*   **返回**：`Object` - 结果可以是 `List<Map<String, Object>>`, `Map<String, Object>`, 或 `int`。

### `resultAll()`
*   **功能**：结束事务并返回事务期间所有DML/DQL操作的结果列表。
*   **返回**：`List<Object>`。

### `setTransaction()`
*   **功能**：显式地开启一个事务。
*   **返回**：`DatabaseAccessor`。

### `commit()`
*   **功能**：手动提交当前事务。
*   **返回**：`DatabaseAccessor`。

### `rollback()`
*   **功能**：手动回滚当前事务。
*   **返回**：`DatabaseAccessor`。

### `isInTransaction()`
*   **功能**：检查当前访问器是否正处于一个活动事务中。
*   **返回**：`boolean`。

---
### 数据库操作 (DDL, DQL, DML)

### `executeDDL(String sql)`
*   **功能**：执行一条DDL语句。
*   **返回**：`DatabaseAccessor`。

### `executeDQL(String sql)`
*   **功能**：执行一条DQL查询。
*   **返回**：`DatabaseAccessor`。

### `executeDML(String sql)`
*   **功能**：执行一条DML语句。
*   **返回**：`DatabaseAccessor`。

---
### 数据库操作 (简单SQL)

### `selectList(String sql, Map<String, Object> params)`
*   **功能**：使用简单SQL语法执行查询，返回多行。
*   **返回**：`DatabaseAccessor`。

### `selectOne(String sql, Map<String, Object> params)`
*   **功能**：使用简单SQL语法执行查询，返回单行。
*   **返回**：`DatabaseAccessor`。

### `execute(String sql, Map<String, Object> params)`
*   **功能**：使用简单SQL语法执行DML操作。
*   **返回**：`DatabaseAccessor`。

### `selectPage(String sql, int pageNum, int pageSize, Map<String, Object> params)`
*   **功能**：使用简单SQL语法执行分页查询。
*   **返回**：`DatabaseAccessor`。

### `batchExecute(String sql, List<Map<String, Object>> paramsList, int batchSize)`
*   **功能**：使用简单SQL语法批量执行DML操作。
*   **返回**：`DatabaseAccessor`。

### 数据库操作 (MyBatis SQL)

### `selectListByMp(String sql, Map<String, Object> params)`
*   **功能**：使用MyBatis SQL语法执行查询，返回多行。
*   **返回**：`DatabaseAccessor`。

### `selectOneByMp(String sql, Map<String, Object> params)`
*   **功能**：使用MyBatis SQL语法执行查询，返回单行。
*   **返回**：`DatabaseAccessor`。

### `executeByMp(String sql, Map<String, Object> params)`
*   **功能**：使用MyBatis SQL语法执行DML操作。
*   **返回**：`DatabaseAccessor`。
