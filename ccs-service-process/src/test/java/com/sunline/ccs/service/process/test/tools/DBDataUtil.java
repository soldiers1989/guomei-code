package com.sunline.ccs.service.process.test.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JFileChooser;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.operation.DatabaseOperation;
import org.hsqldb.lib.FileUtil;

/**
 * 描述：
 * 
* @author fanghj
 * @date 2013-4-12 上午11:42:04
 * @version 1.0
 */
public class DBDataUtil {
	private IDatabaseConnection connection = null;

	/**
	 * 打开连接，相当于JDBC的DriverManager.getConnection()
	 * 
	 * @param conn
	 * @return
	 */
	public boolean open(final Connection conn) {
		try {
			this.connection = new DatabaseConnection(conn);
			return connection != null;
		} catch (DatabaseUnitException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 关闭连接，相当于JDBC的connection.close;
	 * 
	 * @return
	 */
	public boolean close() {
		if (this.connection != null) {
			try {
				this.connection.close();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	/**
	 * 导出数据到指定的目录下
	 * 
	 * @param tableNames
	 *            表名列表，注意外键关系，要按照顺序排列（先导出字典，再导出主表）
	 * @param destFolder
	 *            导出目录
	 * @return
	 */
	public boolean exp(final List<String> tableNames, final File destFolder) {
		QueryDataSet dataSet = new QueryDataSet(connection);
		try {
			for (String tableName : tableNames) {
				dataSet.addTable(tableName);
			}
			CsvDataSetWriter.write(dataSet, destFolder);
			this.saveDefaultPath(destFolder.getParent());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 导入数据，会根据导出时的顺序依次导入，如果出现了因外键约束而导入失败的情况，请调整导出时的表顺序
	 * 
	 * @param sourceFolder
	 * @return
	 */
	public boolean imp(final File sourceFolder) {
		try {
			IDataSet dataSet = new CsvDataSet(sourceFolder);
			// 准备读入数据
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 选择打开目录
	 * 
	 * @return
	 */
	public static String showOpenFolder() {
		String defaultPath = loadDefaultPath();
		JFileChooser jf = new JFileChooser(defaultPath);
		jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = jf.showOpenDialog(null);
		if (ret != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return jf.getSelectedFile().getAbsolutePath();
	}

	/**
	 * 选择保存目录
	 * 
	 * @param defaultPath
	 *            默认目录
	 * @return
	 */
	public static String showSaveFolder() {
		String defaultPath = loadDefaultPath();
		JFileChooser jf = new JFileChooser(defaultPath);
		jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = jf.showSaveDialog(null);
		if (ret != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return jf.getSelectedFile().getAbsolutePath();
	}

	public static String loadDefaultPath() {
		File f = new File(".lastpath");
		System.out.println(f.getAbsolutePath());
		if (f.isFile() && f.exists()) {
			return null;
			
//			return FileUtil.readAbsolutFile(f.getAbsolutePath());
		}
		return null;
	}

	public static void saveDefaultPath(final String defaultPath) {
		File f = new File(".lastpath");
		//FileUtil.writeFile(".lastpath", defaultPath);
		System.out.println(f.getAbsolutePath());
	}
}
