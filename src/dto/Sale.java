package dto;

import java.time.LocalDateTime;

/**
 * 판매 정보를 담는 DTO 클래스
 */
public class Sale {
  private int saleId;
  private LocalDateTime saleDate;
  private int totalAmount;

  // 기본 생성자
  public Sale() {
  }

  // 필드 초기화 생성자
  public Sale(int saleId, LocalDateTime saleDate, int totalAmount) {
    this.saleId = saleId;
    this.saleDate = saleDate;
    this.totalAmount = totalAmount;
  }

  // Getter와 Setter 메소드
  public int getSaleId() {
    return saleId;
  }

  public void setSaleId(int saleId) {
    this.saleId = saleId;
  }

  public LocalDateTime getSaleDate() {
    return saleDate;
  }

  public void setSaleDate(LocalDateTime saleDate) {
    this.saleDate = saleDate;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(int totalAmount) {
    this.totalAmount = totalAmount;
  }

  @Override
  public String toString() {
    return "Sale [saleId=" + saleId + ", saleDate=" + saleDate +
        ", totalAmount=" + totalAmount + "]";
  }
}