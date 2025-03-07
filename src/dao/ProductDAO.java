package dao;

import dto.Product;
import common.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 제품 및 대기 주문 관련 데이터베이스 액세스 객체
 */
public class ProductDAO {

  /**
   * 데이터베이스 연결 가져오기
   */
  private Connection getConnection() throws SQLException {
    return DBManager.getConnection();
  }

  /**
   * 모든 제품 목록 조회
   */
  public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT * FROM products";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setName(rs.getString("name"));
        product.setManufacturer(rs.getString("manufacturer"));
        product.setPrice(rs.getInt("price"));
        product.setStoreQuantity(rs.getInt("store_quantity"));
        product.setWarehouseQuantity(rs.getInt("warehouse_quantity"));

        products.add(product);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return products;
  }

  /**
   * 제품 ID로 제품 정보 조회
   */
  public Product getProductById(int productId) {
    Product product = null;
    String sql = "SELECT * FROM products WHERE product_id = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, productId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          product = new Product();
          product.setProductId(rs.getInt("product_id"));
          product.setName(rs.getString("name"));
          product.setManufacturer(rs.getString("manufacturer"));
          product.setPrice(rs.getInt("price"));
          product.setStoreQuantity(rs.getInt("store_quantity"));
          product.setWarehouseQuantity(rs.getInt("warehouse_quantity"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return product;
  }

  /**
   * 재고 수량 업데이트
   */
  public boolean updateInventory(int productId, int storeQuantity, int warehouseQuantity) {
    boolean success = false;
    String sql = "UPDATE products SET store_quantity = ?, warehouse_quantity = ? WHERE product_id = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, storeQuantity);
      pstmt.setInt(2, warehouseQuantity);
      pstmt.setInt(3, productId);

      int result = pstmt.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return success;
  }

  /**
   * 대기 주문 생성
   */
  public int createWaitingOrder(int productId, int quantity) {
    int orderId = -1;
    String sql = "INSERT INTO waiting_orders (product_id, quantity, status) VALUES (?, ?, 'waiting')";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setInt(1, productId);
      pstmt.setInt(2, quantity);

      int result = pstmt.executeUpdate();

      if (result > 0) {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
          if (rs.next()) {
            orderId = rs.getInt(1);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return orderId;
  }

}