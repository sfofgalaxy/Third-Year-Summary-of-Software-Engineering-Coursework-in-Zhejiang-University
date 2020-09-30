package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnMySQL {

	//需要更改对应的mysql的用户名、密码以及创建响应数据库名为hw5
	private static final  String URL="jdbc:mysql://localhost:3306/hw5?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";//链接的mysql
	private static final String USER = "root";
	private static final String PASS = "123456";
	private Connection con = null;
	public Connection getConnect() {
		try {
			//1.加载驱动
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("数据库驱动加载成功！");
			//2.创建连接
			con = DriverManager.getConnection(URL, USER, PASS);
			System.out.println("连接成功！");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}

}
		

