package dto;

/**
 * 판매 항목 정보를 담는 DTO 클래스
 */
public class SaleItem {
  private int saleItemId;
  private int saleId;
  private int productId;
  private int quantity;
  private int unitPrice;
  private int totalPrice;


  public SaleItem() {
  }

  public SaleItem(int saleItemId, int saleId, int productId, int quantity, int unitPrice, int totalPrice) {
    this.saleItemId = saleItemId;
    this.saleId = saleId;
    this.productId = productId;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.totalPrice = totalPrice;
  }

  public int getSaleItemId() {
    return saleItemId;
  }

  public void setSaleItemId(int saleItemId) {
    this.saleItemId = saleItemId;
  }


  public void setSaleId(int saleId) {
    this.saleId = saleId;
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

  public int getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(int unitPrice) {
    this.unitPrice = unitPrice;
  }


  public void setTotalPrice(int totalPrice) {
    this.totalPrice = totalPrice;
  }

  @Override
  public String toString() {
    return "SaleItem [saleItemId=" + saleItemId + ", saleId=" + saleId +
        ", productId=" + productId + ", quantity=" + quantity +
        ", unitPrice=" + unitPrice + ", totalPrice=" + totalPrice + "]";
  }
}