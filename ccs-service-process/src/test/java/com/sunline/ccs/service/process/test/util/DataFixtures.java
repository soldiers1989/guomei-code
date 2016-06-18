package com.sunline.ccs.service.process.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.db2.Db2Connection;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.ext.mysql.MySqlConnection;
import org.dbunit.ext.oracle.OracleConnection;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * 生成测试数据工具类
* @author fanghj
 *
 */
public class DataFixtures {

	static String xmlPath = "/data/export.xml";

	private static Logger log = LoggerFactory.getLogger(DataFixtures.class);

	private static ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * 装入数据
	 * @param dataSource
	 * @param xmlFilePaths
	 * @throws Exception
	 */
	public static void reloadData(DataSource dataSource, String... xmlFilePaths) throws Exception {
		if (xmlFilePaths.equals("")) {
			execute(DatabaseOperation.CLEAN_INSERT, dataSource, xmlPath);
		} else {
			execute(DatabaseOperation.CLEAN_INSERT, dataSource, xmlFilePaths);
		}
	}

	/**
	 * 装入数据
	 * @param dataSource
	 * @throws Exception
	 */
	public static void reloadData(DataSource dataSource) throws Exception {
		reloadData(dataSource, "");
	}

    /**
     * 加载数据
     * @param dataSource
     * @param xmlFilePaths
     * @throws Exception
     */
	public static void loadData(DataSource dataSource, String... xmlFilePaths) throws Exception {
		execute(DatabaseOperation.INSERT, dataSource, xmlFilePaths);
	}

	/**
	 * 删除数据
	 * @param dataSource
	 * @param xmlFilePaths
	 * @throws Exception
	 */
	public static void deleteData(DataSource dataSource, String... xmlFilePaths) throws Exception {
		execute(DatabaseOperation.DELETE_ALL, dataSource, xmlFilePaths);
	}

	/**
	 * 得到表的字段对象
	 * @param tableName
	 * @param column
	 * @return
	 */
	public static Object getColunmValue(String tableName, String column) {
		try {
			InputStream input = resourceLoader.getResource(xmlPath).getInputStream();
			IDataSet dataSet = new XmlDataSet(input);
			return dataSet.getTable(tableName).getValue(1, column);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataSetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param row
	 * @param tableName
	 * @param column
	 * @return
	 */
	public static Object getColunmValue(int row, String tableName, String column) {
		try {
			InputStream input = resourceLoader.getResource(xmlPath).getInputStream();
			IDataSet dataSet = new XmlDataSet(input);
			return dataSet.getTable(tableName).getValue(row, column);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataSetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param cl
	 * @param name
	 * @return
	 */
	public static String getColumnName(Class<?> cl, String name) {
		Field field;
		String columnName = null;
		try {
			field = cl.getDeclaredField(name);
			if (field.isAnnotationPresent(Column.class)) {
				// 存在
				Column c = field.getAnnotation(Column.class);// 获取实例
				// 获取元素值
				columnName = c.name();
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return columnName;
	}

	/**
	 * 执行
	 * @param operation
	 * @param dataSource
	 * @param xmlFilePaths
	 * @throws DatabaseUnitException
	 * @throws SQLException
	 */
	private static void execute(DatabaseOperation operation, DataSource dataSource, String... xmlFilePaths)
			throws DatabaseUnitException, SQLException {
		IDatabaseConnection connection = getConnection(dataSource);
		try {
			for (String xmlPaths : xmlFilePaths) {
				try {
					InputStream input = resourceLoader.getResource(xmlPaths).getInputStream();
					IDataSet dataSet = new XmlDataSet(input);
					operation.execute(connection, dataSet);
				} catch (IOException e) {
					log.warn(xmlPaths + " file not found", e);
				}
			}
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * 得到数据库连接
	 * @param dataSource
	 * @return
	 * @throws DatabaseUnitException
	 * @throws SQLException
	 */
	protected static IDatabaseConnection getConnection(DataSource dataSource) throws DatabaseUnitException,
			SQLException {
		Connection connection = dataSource.getConnection();
//		ResultSet temp = connection.getMetaData().getTables(connection.getCatalog(), "% ", "% ",
//				new String[] { "TABLE ", "VIEW " });
		DatabaseMetaData dmd = connection.getMetaData();
		ResultSet rs = dmd.getTables(null, null, "%", null);
		String dbURL = connection.getMetaData().getURL();
		String dbUserName = connection.getMetaData().getUserName();
		// log.info("-------------------------数据库schema["+connection.getSchema()+"]");
		log.debug("-------------------------数据库URL:[" + dbURL + "]");
		log.debug("-------------------------数据库用户:[" + dbUserName + "]");
		while (rs.next()) {
			log.debug("-------------------------数据库表结构[: " + rs.getString(3) + "]");
		}
		if (StringUtils.contains(dbURL, ":h2:")) {
			return new H2Connection(connection, null);
		} else if (StringUtils.contains(dbURL, ":mysql:")) {
			return new MySqlConnection(connection, null);
		} else if (StringUtils.contains(dbURL, ":oracle:")) {
			return new OracleConnection(connection, null);
		} else if (StringUtils.contains(dbURL, ":db2")) {
			return new Db2Connection(connection, null);
		} else {
			return new DatabaseConnection(connection);
		}
	}

}
