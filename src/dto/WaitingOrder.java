package dto;

import java.time.LocalDateTime;

/**
 * 대기 주문 정보를 담는 DTO 클래스
 */
public class WaitingOrder {
  private int orderId;
  private int productId;
  private int quantity;
  private LocalDateTime requestDate;
  private String status;
  private LocalDateTime processedDate;
  private int processedSaleId;

  // 기본 생성자
  public WaitingOrder() {
  }

  // 필수 필드만 초기화하는 생성자
  public WaitingOrder(int orderId, int productId, int quantity, LocalDateTime requestDate, String status) {
    this.orderId = orderId;
    this.productId = productId;
    this.quantity = quantity;
    this.requestDate = requestDate;
    this.status = status;
  }

  // 모든 필드를 초기화하는 생성자
  public WaitingOrder(int orderId, int productId, int quantity, LocalDateTime requestDate,
      String status, LocalDateTime processedDate, int processedSaleId) {
    this.orderId = orderId;
    this.productId = productId;
    this.quantity = quantity;
    this.requestDate = requestDate;
    this.status = status;
    this.processedDate = processedDate;
    this.processedSaleId = processedSaleId;
  }

  // Getter와 Setter 메소드
  public int getOrderId() {
    return orderId;
  }

  public void setOrderId(int orderId) {
    this.orderId = orderId;
  }

  public int getProductId() {
    return productId;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public LocalDateTime getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(LocalDateTime requestDate) {
    this.requestDate = requestDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getProcessedDate() {
    return processedDate;
  }

  public void setProcessedDate(LocalDateTime processedDate) {
    this.processedDate = processedDate;
  }

  public int getProcessedSaleId() {
    return processedSaleId;
  }

  public void setProcessedSaleId(int processedSaleId) {
    this.processedSaleId = processedSaleId;
  }

  @Override
  public String toString() {
    return "WaitingOrder [orderId=" + orderId + ", productId=" + productId +
        ", quantity=" + quantity + ", requestDate=" + requestDate +
        ", status=" + status + ", processedDate=" + processedDate +
        ", processedSaleId=" + processedSaleId + "]";
  }
}