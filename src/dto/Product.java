package dto;

public class Product {
  private int productId;
  private String name;
  private String manufacturer;
  private int price;
  private int storeQuantity;
  private int warehouseQuantity;

  // 기본 생성자
  public Product() {
  }

  // 모든 필드를 초기화하는 생성자
  public Product(int productId, String name, String manufacturer, int price,
      int storeQuantity, int warehouseQuantity) {
    this.productId = productId;
    this.name = name;
    this.manufacturer = manufacturer;
    this.price = price;
    this.storeQuantity = storeQuantity;
    this.warehouseQuantity = warehouseQuantity;
  }

  public int getProductId() {
    return productId;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getStoreQuantity() {
    return storeQuantity;
  }

  public void setStoreQuantity(int storeQuantity) {
    this.storeQuantity = storeQuantity;
  }

  public int getWarehouseQuantity() {
    return warehouseQuantity;
  }

  public void setWarehouseQuantity(int warehouseQuantity) {
    this.warehouseQuantity = warehouseQuantity;
  }

  @Override
  public String toString() {
    return "Product [productId=" + productId + ", name=" + name + ", manufacturer=" + manufacturer +
        ", price=" + price + ", storeQuantity=" + storeQuantity +
        ", warehouseQuantity=" + warehouseQuantity + "]";
  }
}