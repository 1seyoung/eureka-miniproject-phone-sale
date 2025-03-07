package service;

import dao.ProductDAO;
import dao.SaleDAO;
import dto.Product;
import dto.SaleItem;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * 재고 관리 서비스
 */
public class InventoryService {
  private List<InventoryChangeListener> listeners = new ArrayList<>();

  private final ProductDAO productDAO;
  private final SaleDAO saleDAO;
  private SalesService salesService;  // 🔹 나중에 set 가능하도록 변경

  public InventoryService() {
    this.productDAO = new ProductDAO();
    this.saleDAO = new SaleDAO();
  }

  // 🔹 Setter를 추가해서 SalesService를 나중에 주입할 수 있도록 변경
  public void setSalesService(SalesService salesService) {
    this.salesService = salesService;
  }
  /**
   * 재고 변경 이벤트를 수신할 리스너 인터페이스
   */
  public interface InventoryChangeListener {
    void onInventoryChanged(List<Product> updatedProducts);
    void onStockTransferCompleted(int productId, int fromWarehouse, int toStore);
    void onLowStockDetected(Product product);
  }

  /**
   * 재고 변경 리스너 등록
   */
  public void addInventoryChangeListener(InventoryChangeListener listener) {
    listeners.add(listener);
  }

  /**
   * 모든 제품 조회
   */
  public List<Product> getAllProducts() {
    return productDAO.getAllProducts();
  }

  /**
   * 22시: 창고에서 매장으로 재고 이동
   */
  public void transferFromWarehouseToStore() {
    List<Product> products = productDAO.getAllProducts();
    boolean anyTransfer = false;

    for (Product product : products) {
      if (product.getStoreQuantity() < 5 && product.getWarehouseQuantity() > 0) {
        int transferQty = Math.min(5 - product.getStoreQuantity(), product.getWarehouseQuantity());

        if (transferQty > 0) {
          int newStoreQty = product.getStoreQuantity() + transferQty;
          int newWarehouseQty = product.getWarehouseQuantity() - transferQty;

          productDAO.updateInventory(product.getProductId(), newStoreQty, newWarehouseQty);

          for (InventoryChangeListener listener : listeners) {
            listener.onStockTransferCompleted(product.getProductId(), transferQty, newStoreQty);
          }

          anyTransfer = true;
        }
      }
    }

    if (anyTransfer) {
      notifyInventoryChanged();
    }
  }

  /**
   * 1시: 본사에서 창고로 배송
   */
  /**
   * 1시: 본사에서 창고로 배송
   */
  public void receiveDeliveryFromHeadquarters() {
    List<Product> products = productDAO.getAllProducts();

    // 대기 주문에 있는 제품에 대해 추가 수량 확인
    Map<Integer, Integer> waitingOrderCounts = new HashMap<>();

    try {
      if (salesService != null) {
        List<SaleItem> waitingOrders = salesService.getWaitingOrders();
        if (waitingOrders != null) {
          // 각 제품별 대기 주문 수량 합산
          for (SaleItem order : waitingOrders) {
            int productId = order.getProductId();
            int quantity = order.getQuantity();
            waitingOrderCounts.put(productId, waitingOrderCounts.getOrDefault(productId, 0) + quantity);
          }
        }
      }
    } catch (Exception e) {
      System.err.println("대기 주문 조회 중 오류: " + e.getMessage());
    }

    for (Product product : products) {
      // 기본 추가 수량 5개 설정 (기존 3개에서 변경)
      int additionalQty = 5;

      // 대기 주문이 있는 경우 해당 수량만큼 추가
      if (waitingOrderCounts.containsKey(product.getProductId())) {
        additionalQty += waitingOrderCounts.get(product.getProductId());
      }

      int newWarehouseQty = product.getWarehouseQuantity() + additionalQty;
      productDAO.updateInventory(product.getProductId(), product.getStoreQuantity(), newWarehouseQty);
    }

    notifyInventoryChanged();

    // 본사 배송 이후 대기 주문 자동 처리 - NullPointerException 방지
    if (salesService != null) {
      salesService.processWaitingOrders();
    }
  }
  /**
   * 판매로 인한 재고 감소 - 매장 재고만 사용
   */
  public boolean decreaseStockForSale(int productId, int quantity) {
    Product product = productDAO.getProductById(productId);

    // 매장 재고만 확인
    if (product.getStoreQuantity() >= quantity) {
      int newStoreQty = product.getStoreQuantity() - quantity;
      productDAO.updateInventory(productId, newStoreQty, product.getWarehouseQuantity());
      notifyInventoryChanged();

      if (newStoreQty < 3) {
        for (InventoryChangeListener listener : listeners) {
          listener.onLowStockDetected(product);
        }
      }

      return true;
    } else {
      // 매장 재고 부족 시 실패
      return false;
    }
  }

  /**
   * 대기 주문 완료 처리 (status 변경)
   */
  public void completeWaitingOrder(int orderId) {
    saleDAO.completeWaitingOrder(orderId);
  }

  /**
   * 재고 변경 알림
   */
  private void notifyInventoryChanged() {
    List<Product> updatedProducts = productDAO.getAllProducts();
    for (InventoryChangeListener listener : listeners) {
      listener.onInventoryChanged(updatedProducts);
    }
  }

  /**
   * 제품 ID로 제품 정보 조회
   */
  public Product getProductById(int productId) {
    return productDAO.getProductById(productId);
  }

  /**
   * 재고 변경 이벤트 발생
   */
  public void fireInventoryChangedEvent() {
    notifyInventoryChanged();
  }
}