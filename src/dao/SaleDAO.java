package dao;

import dto.SaleItem;
import common.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 판매 관련 데이터베이스 액세스 객체
 */
public class SaleDAO {

  /**
   * 데이터베이스 연결 가져오기
   */
  private Connection getConnection() throws SQLException {
    return DBManager.getConnection();
  }

  /**
   * 대기 주문 테이블 초기화
   */
  public void clearWaitingOrders() {
    String sql = "DELETE FROM waiting_orders";
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 판매 상품 테이블 초기화
   */
  public void clearSaleItems() {
    String sql = "DELETE FROM sale_items";
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 판매 테이블 초기화
   */
  public void clearSales() {
    String sql = "DELETE FROM sales";
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 새 판매 정보 생성
   */
  public int createSale(int totalAmount) {
    int saleId = -1;
    String sql = "INSERT INTO sales (total_amount) VALUES (?)";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setInt(1, totalAmount);
      int result = pstmt.executeUpdate();

      if (result > 0) {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
          if (rs.next()) {
            saleId = rs.getInt(1);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return saleId;
  }

  /**
   * 판매 항목 추가
   */
  public boolean addSaleItem(int saleId, int productId, int quantity, int unitPrice, int totalPrice) {
    boolean success = false;
    String sql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, saleId);
      pstmt.setInt(2, productId);
      pstmt.setInt(3, quantity);
      pstmt.setInt(4, unitPrice);
      pstmt.setInt(5, totalPrice);

      int result = pstmt.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return success;
  }

  /**
   * 판매에 속한 모든 항목 조회
   */
  public List<SaleItem> getSaleItemsBySaleId(int saleId) {
    List<SaleItem> items = new ArrayList<>();
    String sql = "SELECT * FROM sale_items WHERE sale_id = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, saleId);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          SaleItem item = new SaleItem();
          item.setSaleItemId(rs.getInt("sale_item_id"));
          item.setSaleId(rs.getInt("sale_id"));
          item.setProductId(rs.getInt("product_id"));
          item.setQuantity(rs.getInt("quantity"));
          item.setUnitPrice(rs.getInt("unit_price"));
          item.setTotalPrice(rs.getInt("total_price"));

          items.add(item);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return items;
  }


  /**
   * 대기 주문 목록 조회
   */
  public List<SaleItem> getWaitingOrders() {
    List<SaleItem> waitingOrders = new ArrayList<>();
    String sql = "SELECT order_id, product_id, quantity FROM waiting_orders WHERE status = 'waiting' ORDER BY request_date ASC";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        SaleItem item = new SaleItem();
        item.setSaleItemId(rs.getInt("order_id"));  // 대기 주문 ID를 SaleItem의 ID로 재사용
        item.setSaleId(-1); // 대기 주문은 sale_id가 없으므로 기본값 -1 설정
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(0); // 대기 주문은 단가 정보 없음 (0으로 설정)
        item.setTotalPrice(0); // 총 가격도 0으로 설정

        waitingOrders.add(item);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return waitingOrders;
  }

  /**
   * 대기 주문 완료 처리 (status 변경)
   */
  public void completeWaitingOrder(int orderId) {
    String sql = "UPDATE waiting_orders SET status = 'processed' WHERE order_id = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, orderId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}