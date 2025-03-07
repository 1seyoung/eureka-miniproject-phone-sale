package service;

import dao.ProductDAO;
import dao.SaleDAO;
import dto.Product;
import dto.SaleItem;
import java.util.ArrayList;
import java.util.List;

/**
 * 판매 관련 비즈니스 로직 서비스
 */
public class SalesService {
  private SaleDAO saleDAO;
  private ProductDAO productDAO;
  private InventoryService inventoryService;
  private List<SalesListener> salesListeners = new ArrayList<>();
  private List<WaitingOrderListener> waitingOrderListeners = new ArrayList<>();

  /**
   * 판매 이벤트 리스너 인터페이스
   */
  public interface SalesListener {
    void onSaleCompleted(int saleId, int totalAmount);
    void onSaleFailed(int productId, int requestedQuantity, String reason);
  }

  /**
   * 대기 주문 이벤트 리스너 인터페이스
   */
  public interface WaitingOrderListener {
    void onWaitingOrderProcessed(int productId, int quantity, boolean success);
  }

  /**
   * 파라미터 없는 생성자
   */
  public SalesService() {
    this.saleDAO = new SaleDAO();
    this.productDAO = new ProductDAO();
    this.inventoryService = new InventoryService();

    initializeDatabase();


    // 내부적으로 이벤트 리스너 등록
    this.inventoryService.addInventoryChangeListener(new InventoryEventHandler());
  }

  /**
   * InventoryService 이벤트 처리를 위한 내부 클래스
   */
  private class InventoryEventHandler implements InventoryService.InventoryChangeListener {
    @Override
    public void onInventoryChanged(List<Product> updatedProducts) {
      // 필요시 재고 변경에 반응
    }

    @Override
    public void onStockTransferCompleted(int productId, int fromWarehouse, int toStore) {
      // 재고 이동 완료 시 필요한 처리
    }

    @Override
    public void onLowStockDetected(Product product) {
      // 재고 부족 시 알림 처리
    }
  }

  /**
   * 데이터베이스 테이블 초기화
   */
  private void initializeDatabase() {
    try {
      // 대기 주문 초기화
      saleDAO.clearWaitingOrders();

      // 판매 내역 초기화
      saleDAO.clearSaleItems();
      saleDAO.clearSales();

      System.out.println("✅ 판매 관련 데이터베이스 테이블이 초기화되었습니다.");
    } catch (Exception e) {
      System.err.println("❌ 데이터베이스 초기화 중 오류 발생: " + e.getMessage());
    }
  }

  /**
   * 판매 리스너 등록
   */
  public void addSalesListener(SalesListener listener) {
    salesListeners.add(listener);
  }

  /**
   * 대기 주문 리스너 등록
   */
  public void addWaitingOrderListener(WaitingOrderListener listener) {
    waitingOrderListeners.add(listener);
  }

  /**
   * 판매 처리 - 매장 재고만 사용
   */
  public int processSale(int productId, int quantity) {
    Product product = productDAO.getProductById(productId);
    if (product == null) {
      notifySaleFailed(productId, quantity, "제품을 찾을 수 없습니다.");
      return -1;
    }

    // 수정: 매장 재고만 확인
    if (product.getStoreQuantity() >= quantity) {
      // 매장 재고 감소
      int newStoreQty = product.getStoreQuantity() - quantity;
      boolean stockUpdated = productDAO.updateInventory(
          productId, newStoreQty, product.getWarehouseQuantity());

      if (stockUpdated) {
        // 재고 변경 알림
        inventoryService.fireInventoryChangedEvent();
        // 판매 완료 처리
        return completeSale(productId, quantity, product.getPrice());
      } else {
        notifySaleFailed(productId, quantity, "재고 업데이트 중 오류가 발생했습니다.");
        return -1;
      }
    } else {
      // 매장 재고 부족 시 대기 주문 등록
      return registerWaitingOrder(productId, quantity);
    }
  }

  /**
   * 판매 완료 처리
   */
  private int completeSale(int productId, int quantity, int unitPrice) {
    int totalAmount = quantity * unitPrice;
    int saleId = saleDAO.createSale(totalAmount);

    if (saleId > 0 && saleDAO.addSaleItem(saleId, productId, quantity, unitPrice, totalAmount)) {
      notifySaleCompleted(saleId, totalAmount);
      return saleId;
    }

    notifySaleFailed(productId, quantity, "판매 정보 저장 중 오류가 발생했습니다.");
    return -1;
  }

  /**
   * 대기 주문 등록
   */
  private int registerWaitingOrder(int productId, int quantity) {
    int orderId = productDAO.createWaitingOrder(productId, quantity);

    if (orderId > 0) {
      notifySaleFailed(productId, quantity, "재고 부족으로 대기 주문으로 등록되었습니다.");
    } else {
      notifySaleFailed(productId, quantity, "대기 주문 등록 중 오류가 발생했습니다.");
    }

    return -1;
  }

  /**
   * 대기 주문 목록 조회
   */
  public List<SaleItem> getWaitingOrders() {
    return saleDAO.getWaitingOrders();
  }

  /**
   * 대기 주문 자동 처리 - 창고 재고도 고려
   */
  public int processWaitingOrders() {
    List<SaleItem> waitingOrders = saleDAO.getWaitingOrders();
    int processedCount = 0;

    for (SaleItem order : waitingOrders) {
      Product product = productDAO.getProductById(order.getProductId());

      // 매장 재고와 창고 재고 모두 확인
      int storeQuantity = product.getStoreQuantity();
      int warehouseQuantity = product.getWarehouseQuantity();
      int requestedQuantity = order.getQuantity();

      // 매장 재고만으로 충분한 경우
      if (storeQuantity >= requestedQuantity) {
        int newStoreQty = storeQuantity - requestedQuantity;

        if (productDAO.updateInventory(product.getProductId(), newStoreQty, warehouseQuantity)) {
          int saleId = completeSale(order.getProductId(), requestedQuantity, product.getPrice());

          if (saleId > 0) {
            saleDAO.completeWaitingOrder(order.getSaleItemId());
            notifyWaitingOrderProcessed(order.getProductId(), requestedQuantity, true);
            processedCount++;
          }
        }
      }
      // 매장 재고 + 창고 재고로 해결 가능한 경우
      else if (storeQuantity + warehouseQuantity >= requestedQuantity) {
        // 매장 재고를 먼저 사용
        int remainingQty = requestedQuantity - storeQuantity;
        int newWarehouseQty = warehouseQuantity - remainingQty;

        // 창고에서 매장으로 재고 이동 후 판매 처리
        if (productDAO.updateInventory(product.getProductId(), 0, newWarehouseQty)) {
          int saleId = completeSale(order.getProductId(), requestedQuantity, product.getPrice());

          if (saleId > 0) {
            saleDAO.completeWaitingOrder(order.getSaleItemId());
            notifyWaitingOrderProcessed(order.getProductId(), requestedQuantity, true);
            processedCount++;
          }
        }
      } else {
        notifyWaitingOrderProcessed(order.getProductId(), requestedQuantity, false);
      }
    }

    return processedCount;
  }

  /**
   * 판매 항목 조회 메소드 추가
   */
  public List<SaleItem> getSaleItems(int saleId) {
    return saleDAO.getSaleItemsBySaleId(saleId);
  }

  /**
   * 판매 완료 이벤트 발생
   */
  private void notifySaleCompleted(int saleId, int totalAmount) {
    for (SalesListener listener : salesListeners) {
      listener.onSaleCompleted(saleId, totalAmount);
    }
  }

  /**
   * 판매 실패 이벤트 발생
   */
  private void notifySaleFailed(int productId, int requestedQuantity, String reason) {
    for (SalesListener listener : salesListeners) {
      listener.onSaleFailed(productId, requestedQuantity, reason);
    }
  }

  /**
   * 대기 주문 처리 이벤트 발생
   */
  private void notifyWaitingOrderProcessed(int productId, int quantity, boolean success) {
    for (WaitingOrderListener listener : waitingOrderListeners) {
      listener.onWaitingOrderProcessed(productId, quantity, success);
    }
  }
}