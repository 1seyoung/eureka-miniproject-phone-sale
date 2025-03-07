package service;

import dto.Product;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * 전체 시뮬레이션 관리 서비스
 */
public class SimulationService implements TimeService.TimeEventListener {
  private TimeService timeService;
  private InventoryService inventoryService;
  private SalesService salesService;
  private Random random = new Random();
  private boolean isRunning = false;

  /**
   * 시뮬레이션 이벤트 리스너 인터페이스
   */
  public interface SimulationListener {
    void onCustomerVisit(int customerId);
    void onSimulationStarted();
    void onSimulationStopped();
    void onInventoryManagement();
    void onHeadquartersDelivery();
  }

  /**
   * 시뮬레이션 로그 이벤트 리스너 인터페이스
   */
  public interface SimulationLogListener {
    void onLogMessage(String message);
  }

  private List<SimulationListener> listeners = new ArrayList<>();
  private List<SimulationLogListener> logListeners = new ArrayList<>();

  public SimulationService(TimeService timeService) {
    this.timeService = timeService;
    this.inventoryService = new InventoryService();
    this.salesService = new SalesService();

    // 중요: 순환 참조 설정
    this.inventoryService.setSalesService(this.salesService);

    timeService.addTimeEventListener(this); // 시간 이벤트 리스너 등록
  }

  /**
   * 시뮬레이션 리스너 등록
   */
  public void addSimulationListener(SimulationListener listener) {
    listeners.add(listener);
  }

  /**
   * 시뮬레이션 로그 리스너 등록
   */
  public void addSimulationLogListener(SimulationLogListener listener) {
    logListeners.add(listener);
  }

  /**
   * 로그 메시지 전달
   */
  private void notifyLogMessage(String message) {
    for (SimulationLogListener listener : logListeners) {
      listener.onLogMessage(message);
    }
  }

  /**
   * 시뮬레이션 시작
   */
  public void startSimulation() {
    if (isRunning) return;
    isRunning = true;
    notifySimulationEvent(SimulationListener::onSimulationStarted);
  }

  /**
   * 시뮬레이션 정지
   */
  public void stopSimulation() {
    if (!isRunning) return;
    isRunning = false;
    notifySimulationEvent(SimulationListener::onSimulationStopped);
  }

  /**
   * 시간 변경 이벤트 처리
   */
  @Override
  public void onTimeChanged(LocalTime time, LocalDate date) {
    if (isRunning && timeService.isBusinessHour()) {
      simulateCustomerVisits();
    }
  }

  /**
   * 시간 변경 이벤트 처리 (특정 시간 이벤트)
   */
  @Override
  public void onHourChanged(int hour) {
    if (!isRunning) return;

    switch (hour) {
      case 9 -> {
        // 9시: 영업 시작 시 창고->매장 재고 이동 + 대기 주문 처리
        notifyLogMessage("🏪 09:00 - 영업 시작!");
        performInventoryManagement();
        processAllWaitingOrders();
      }
      case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> {
        // 영업 시간 중 정각마다 창고->매장 재고 이동
        notifyLogMessage("🕙 " + String.format("%02d", hour) + ":00 - 재고 관리 실행 중...");
        performInventoryManagement();
      }
      case 1 -> {
        // 1시: 본사 배송 (본사 -> 창고)
        notifyLogMessage("🚚 01:00 - 본사 배송 도착!");
        receiveHeadquartersDelivery();
      }

    }
  }


  /**
   * 모든 대기 주문 처리 (8시)
   */
  private void processAllWaitingOrders() {
    try {
      int processed = salesService.processWaitingOrders();
      if (processed > 0) {
        notifyLogMessage("🌅 08:00 - " + processed + "건의 대기 주문이 처리되었습니다.");
      }
    } catch (Exception e) {
      notifyLogMessage("❌ 대기 주문 처리 중 오류 발생: " + e.getMessage());
    }
  }

  /**
   * 날짜 변경 이벤트 처리
   */
  @Override
  public void onDayChanged(LocalDate date) {
    if (isRunning) {
      salesService.processWaitingOrders(); // 09:00 대기 주문 자동 처리
    }
  }

  /**
   * 고객 방문 시뮬레이션
   */
  private void simulateCustomerVisits() {
    if (random.nextInt(100) < 10) { // 10% 확률로 고객 방문
      int customerId = random.nextInt(10) + 1;
      notifySimulationEvent(listener -> listener.onCustomerVisit(customerId));

      List<Product> products = inventoryService.getAllProducts();
      if (!products.isEmpty()) {
        Product selectedProduct = products.get(random.nextInt(products.size()));
        int quantity = random.nextInt(3) + 1;

        int saleId = salesService.processSale(selectedProduct.getProductId(), quantity);
        if (saleId > 0) {
          // System.out.println() 대신 리스너를 통해 로그 메시지 전달
          notifyLogMessage("✅ 고객 " + customerId + "님이 " + selectedProduct.getName() + "을(를) " + quantity + "개 구매했습니다.");
        } else {
          notifyLogMessage("❌ 고객 " + customerId + "님의 구매 실패 (재고 부족)");
        }
      }
    }
  }

  /**
   * 재고 관리 수행 (22시)
   */
  private void performInventoryManagement() {
    inventoryService.transferFromWarehouseToStore();
    notifySimulationEvent(SimulationListener::onInventoryManagement);
  }

  /**
   * 본사 배송 받기 (1시)
   */
  private void receiveHeadquartersDelivery() {
    try {
      inventoryService.receiveDeliveryFromHeadquarters();
      int processed = salesService.processWaitingOrders(); // 🔄 1시 배송 후 대기 주문 처리
      notifyLogMessage("✅ " + processed + "건의 대기 주문이 처리되었습니다.");
      notifySimulationEvent(SimulationListener::onHeadquartersDelivery);
    } catch (Exception e) {
      notifyLogMessage("❌ 본사 배송 처리 중 오류 발생: " + e.getMessage());
    }
  }

  /**
   * 시뮬레이션 실행 중인지 확인
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * 공통 이벤트 알림 메소드
   */
  private void notifySimulationEvent(java.util.function.Consumer<SimulationListener> event) {
    for (SimulationListener listener : listeners) {
      event.accept(listener);
    }
  }

  /**
   * InventoryService 얻기
   */
  public InventoryService getInventoryService() {
    return inventoryService;
  }

  /**
   * SalesService 얻기
   */
  public SalesService getSalesService() {
    return salesService;
  }
}