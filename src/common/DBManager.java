package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 데이터베이스 연결 관리 클래스
 */
public class DBManager {

  private static final String url = "jdbc:mysql://localhost:3307/eureka_project_db";
  private static final String user = "root";
  private static final String pwd = "1234";

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * 데이터베이스 연결 객체 반환
   */
  public static Connection getConnection() {
    Connection con = null;
    try {
      con = DriverManager.getConnection(url, user, pwd);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return con;
  }

}