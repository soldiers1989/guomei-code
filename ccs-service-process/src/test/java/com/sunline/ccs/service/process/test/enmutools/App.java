package com.sunline.ccs.service.process.test.enmutools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 
* @author fanghj
 * 
 */
@Component
public class App {

	/**
	 * @param args
	 */

	@Autowired
	private SessionFactory sessionFactory;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(App.class);
		ClassPathXmlApplicationContext cs = new ClassPathXmlApplicationContext(
				"service-context.xml");
		Set<Class<?>> set = ClassTools
				.getClasses("com.sunline.ppy.dictionary.enums");
		log.info("本次扫描到---"+set.size());
		int i=1;
		for (Class<?> emnuobj : set) {
			if (emnuobj.isEnum()) {
				log.info("执行到"+i++);
				ArrayList<String> al = new ArrayList<String>();
				String packName = emnuobj.getPackage().getName();
				String clsName = emnuobj.getName();
				clsName = clsName.substring(packName.length() + 1);
				String tableName = "D_" + clsName;
				// 找到枚举类型最大的长度,
				int maxLength = 1;
				for (Object obj : emnuobj.getEnumConstants()) {
					Enum<?> enumObj = (Enum<?>) obj;
					String str = enumObj.name();
					al.add(str);
					if (str.length() > maxLength) {
						maxLength = str.length();
					}					
				}
				App app = (App) cs.getBean("app");
				app.createTable(
						ClassTools.genCreateTable(maxLength, tableName)
								.toString(), tableName);
				app.insertValue(al, tableName);
			}
			
		}
		log.info("数据插入完成");
	}

	/**
	 * 
	 * @param tablesql
	 * @param tableName
	 */
	private void createTable(String tablesql, String tableName) {
		log.info("创建数据库["+tableName+"],创建语句["+tablesql+"]");
		StatelessSession stateSession = sessionFactory.openStatelessSession();
		// 判断表是否存在
		try {
//			ResultSet rs = stateSession.connection().getMetaData()
//					.getTables(null, null, tableName, null);
			//DatabaseMetaData dmd = stateSession.connection().getMetaData();
//			ResultSet rs = stateSession.connection().getMetaData().getTables(null, null, tableName, null);
//			while (rs.next()) {
//				log.info("-------------------------数据库表结构[: " + rs.getString(3) + "]");
//			}
			// drop改表
//			if (rs.next()) {
//				log.info("数据库["+tableName+"]存在，删除");
//			}
		//	stateSession.connection().createStatement().execute("DROP TABLE "+ tableName );
			stateSession.connection().createStatement().execute(tablesql);
			
		} catch (SQLException e) {
			e.printStackTrace();
//			try {
//				stateSession.connection().createStatement().execute(tablesql);
//			} catch (SQLException e1) {				
//				e1.printStackTrace();
//			}		
		}finally{
			stateSession.close();
		}
	}

	/**
	 * 
	 * @param al
	 * @param tableName
	 */
	private void insertValue(ArrayList<String> al, String tableName) {
		StatelessSession stateSession = sessionFactory
				.openStatelessSession();
		try {
			for (String values : al) {
				StringBuilder sb = new StringBuilder("insert into " + tableName);
				sb.append("(DATEVALUE) VALUES('" + values + "')");
				log.info("数据表["+tableName+"],插入["+sb.toString()+"]");
				stateSession.connection().createStatement()
						.execute(sb.toString());
			}
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
			stateSession.close();
		}
	}
	
	
}
