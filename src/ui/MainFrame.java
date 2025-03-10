package ui;

import dto.SaleItem;
import dto.Product;
import service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

/**
 * 핸드폰 판매 시스템 메인 윈도우
 */
public class MainFrame extends JFrame {

  // 서비스 객체들
  private TimeService timeService;
  private InventoryService inventoryService;
  private SalesService salesService;
  private SimulationService simulationService;

  // UI 컴포넌트
  private JTable productTable;
  private JTable waitingOrdersTable;
  private JTextArea logArea;
  private JLabel clockLabel;
  private JLabel dateLabel;
  private JButton startButton;
  private JButton stopButton;
  private JButton restockButton;
  private JLabel statusLabel;
  private CircleAnimationPanel simulationPanel;

  // 테이블 모델
  private DefaultTableModel productTableModel;
  private DefaultTableModel waitingOrdersTableModel;

  // 시뮬레이션 타이머
  private Timer simulationTimer;
  private int timeScale = 10;

  public MainFrame() {
    setTitle("핸드폰 판매 관리 시스템");
    setSize(1200, 700);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    initServices();
    initComponents();
    loadProductsFromDatabase();
    refreshWaitingOrdersTable();
  }

  /**
   * 데이터베이스에서 제품 목록 불러오기
   */
  private void loadProductsFromDatabase() {
    try {
      List<Product> products = inventoryService.getAllProducts();
      refreshProductTable(products);
      addLog("✅ 데이터베이스에서 제품 목록을 불러왔습니다.");
    } catch (Exception e) {
      addLog("❌ 제품 목록을 불러오는 중 오류 발생: " + e.getMessage());
      e.printStackTrace();
      productTableModel.setRowCount(0);
    }
  }

  /**
   * 서비스 초기화
   */
  private void initServices() {
    timeService = new TimeService();
    simulationService = new SimulationService(timeService);

    // SimulationService에서 서비스 객체들을 가져옴
    inventoryService = simulationService.getInventoryService();
    salesService = simulationService.getSalesService();

    // 시뮬레이션 로그 리스너 등록
    simulationService.addSimulationLogListener(message -> {
      addLog(message);
    });

    timeService.addTimeEventListener(new TimeService.TimeEventListener() {
      @Override
      public void onTimeChanged(LocalTime time, LocalDate date) {
        SwingUtilities.invokeLater(() -> clockLabel.setText(timeService.getFormattedTime()));
      }

      @Override
      public void onHourChanged(int hour) {
        if (hour == 9) {
          addLog("🏪 09:00 - 영업 시작!");
        } else if (hour == 1) {
          addLog("🚚 01:00 - 본사 배송 도착!");
          receiveDeliveryFromHeadquarters();
        }
      }

      @Override
      public void onDayChanged(LocalDate date) {
        SwingUtilities.invokeLater(() -> {
          dateLabel.setText("📅 " + timeService.getFormattedDate());
          addLog("📅 새로운 날이 시작되었습니다: " + timeService.getFormattedDate());
        });
      }
    });

    inventoryService.addInventoryChangeListener(new InventoryService.InventoryChangeListener() {
      @Override
      public void onInventoryChanged(List<Product> updatedProducts) {
        refreshProductTable(updatedProducts);
      }

      @Override
      public void onStockTransferCompleted(int productId, int fromWarehouse, int toStore) {
        Product product = inventoryService.getProductById(productId);
        String productName = (product != null) ? product.getName() : "제품 #" + productId;
        addLog(String.format("🔄 창고에서 매장으로 재고 이동: %s (%d개)", productName, fromWarehouse));
      }

      @Override
      public void onLowStockDetected(Product product) {
        addLog(String.format("⚠️ 재고 부족 경고: %s (매장 재고: %d개)", product.getName(), product.getStoreQuantity()));
      }
    });

    salesService.addSalesListener(new SalesService.SalesListener() {
      @Override
      public void onSaleCompleted(int saleId, int totalAmount) {
        try {
          List<SaleItem> items = salesService.getSaleItems(saleId);

          if (!items.isEmpty()) {
            SaleItem item = items.get(0);
            Product product = inventoryService.getProductById(item.getProductId());
            addLog(String.format("✅ 판매 완료: %s %d대 - %,d원",
                product.getName(), item.getQuantity(), totalAmount));
          } else {
            addLog(String.format("✅ 판매 완료 (ID: %d) - 총액: %,d원", saleId, totalAmount));
          }
          refreshProductTable(inventoryService.getAllProducts());
        } catch (Exception e) {
          addLog(String.format("✅ 판매 완료 (ID: %d) - 총액: %,d원", saleId, totalAmount));
        }
      }

      @Override
      public void onSaleFailed(int productId, int requestedQuantity, String reason) {
        Product product = inventoryService.getProductById(productId);
        String productName = (product != null) ? product.getName() : "제품 #" + productId;

        if (reason.contains("대기 주문")) {
          addLog(String.format("⏳ 대기 주문 등록: %s %d개 - 재고 부족", productName, requestedQuantity));
          refreshWaitingOrdersTable();
        } else {
          addLog(String.format("❌ 판매 실패: %s %d개 - %s", productName, requestedQuantity, reason));
        }
      }
    });
  }

  /**
   * UI 초기화
   */
  private void initComponents() {
    // 배경색 흰색으로 설정
    this.getContentPane().setBackground(Color.WHITE);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mainPanel.setBackground(Color.WHITE); // 메인 패널 배경색 흰색으로 설정

    JPanel controlPanel = createControlPanel();
    mainPanel.add(controlPanel, BorderLayout.NORTH);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.6);
    splitPane.setBackground(Color.WHITE); // 스플릿 패널 배경색 설정

    JPanel salesPanel = createSalesPanel();
    splitPane.setLeftComponent(salesPanel);

    simulationPanel = new CircleAnimationPanel();
    simulationPanel.setBackground(Color.WHITE); // 시뮬레이션 패널 배경색 설정
    splitPane.setRightComponent(simulationPanel);

    mainPanel.add(splitPane, BorderLayout.CENTER);
    mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);

    add(mainPanel);
  }

  /**
   * 상단 컨트롤 패널 생성
   */
  private JPanel createControlPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(BorderFactory.createTitledBorder("시뮬레이션 제어"));
    panel.setBackground(Color.WHITE);

    JPanel timePanel = new JPanel();
    timePanel.setBorder(BorderFactory.createEtchedBorder());
    clockLabel = new JLabel("09:00");
    clockLabel.setFont(new Font("Arial", Font.BOLD, 16));
    timePanel.add(new JLabel("현재 시간: "));
    timePanel.add(clockLabel);
    panel.add(timePanel);

    dateLabel = new JLabel("날짜: " + LocalDate.now().toString());
    panel.add(dateLabel);

    // 시작 버튼
    startButton = new JButton("시뮬레이션 시작");
    startButton.setBackground(new Color(76, 175, 80));
    startButton.setForeground(Color.BLACK); // 검은색 텍스트
    startButton.setFont(new Font("Arial", Font.BOLD, 12)); // 굵은 폰트
    startButton.setFocusPainted(false); // 포커스 테두리 제거
    startButton.addActionListener(e -> startSimulation());
    panel.add(startButton);

    // 정지 버튼
    stopButton = new JButton("시뮬레이션 정지");
    stopButton.setBackground(new Color(244, 67, 54));
    stopButton.setForeground(Color.BLACK); // 검은색 텍스트
    stopButton.setFont(new Font("Arial", Font.BOLD, 12)); // 굵은 폰트
    stopButton.setFocusPainted(false); // 포커스 테두리 제거
    stopButton.setEnabled(false);
    stopButton.addActionListener(e -> stopSimulation());
    panel.add(stopButton);

    // 재고 관리 버튼
    restockButton = new JButton("재고 관리 실행");
    restockButton.setBackground(new Color(33, 150, 243));
    restockButton.setForeground(Color.BLACK); // 검은색 텍스트
    restockButton.setFont(new Font("Arial", Font.BOLD, 12)); // 굵은 폰트
    restockButton.setFocusPainted(false); // 포커스 테두리 제거
    restockButton.addActionListener(e -> manageInventory());
    panel.add(restockButton);

    JPanel speedPanel = new JPanel();
    speedPanel.add(new JLabel("속도: "));
    String[] speeds = {"1x", "5x", "10x", "50x", "100x"};
    JComboBox<String> speedCombo = new JComboBox<>(speeds);
    speedCombo.setSelectedIndex(2); // 기본 10x
    speedCombo.addActionListener(e -> {
      String selected = (String)speedCombo.getSelectedItem();
      timeScale = Integer.parseInt(selected.substring(0, selected.length() - 1));
      addLog("🔄 시뮬레이션 속도가 " + selected + "로 변경되었습니다.");

      // 타이머가 실행 중이면 속도 변경 적용
      if (simulationTimer != null && simulationTimer.isRunning()) {
        simulationTimer.setDelay(600 / timeScale);
      }
    });
    speedPanel.add(speedCombo);
    panel.add(speedPanel);

    return panel;
  }

  /**
   * 판매 시스템 패널 생성
   */
  private JPanel createSalesPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBackground(Color.WHITE); // 배경색 흰색으로 설정

    // 제품 테이블
    String[] productColumns = {"ID", "제품명", "제조사", "가격(원)", "매장 재고", "창고 재고", "상태"};
    productTableModel = new DefaultTableModel(productColumns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // 편집 불가능하게 설정
      }
    };
    productTable = new JTable(productTableModel);
    productTable.setRowHeight(25);

    // 칼럼 너비 설정
    productTable.getColumnModel().getColumn(0).setPreferredWidth(30);  // ID
    productTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 제품명
    productTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 제조사
    productTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 가격
    productTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // 매장 재고
    productTable.getColumnModel().getColumn(5).setPreferredWidth(70);  // 창고 재고

    JScrollPane productScrollPane = new JScrollPane(productTable);
    productScrollPane.setBorder(BorderFactory.createTitledBorder("제품 목록"));
    panel.add(productScrollPane, BorderLayout.CENTER);

    // 하단 패널 (대기 주문 + 로그)
    JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 5));

    // 대기 주문 테이블
    String[] waitingColumns = {"주문 ID", "제품 ID", "수량", "상태"};
    waitingOrdersTableModel = new DefaultTableModel(waitingColumns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    waitingOrdersTable = new JTable(waitingOrdersTableModel);
    waitingOrdersTable.setRowHeight(25);
    JScrollPane waitingScrollPane = new JScrollPane(waitingOrdersTable);
    waitingScrollPane.setBorder(BorderFactory.createTitledBorder("대기 주문"));
    waitingScrollPane.setPreferredSize(new Dimension(600, 100));
    bottomPanel.add(waitingScrollPane);

    // 로그 영역
    logArea = new JTextArea();
    logArea.setEditable(false);
    logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    JScrollPane logScrollPane = new JScrollPane(logArea);
    logScrollPane.setPreferredSize(new Dimension(600, 150));
    logScrollPane.setBorder(BorderFactory.createTitledBorder("시스템 로그"));
    bottomPanel.add(logScrollPane);

    panel.add(bottomPanel, BorderLayout.SOUTH);

    return panel;
  }

  /**
   * 하단 상태 패널 생성
   */
  private JPanel createStatusPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEtchedBorder());
    panel.setBackground(Color.WHITE); // 배경색 흰색으로 설정


    statusLabel = new JLabel("시스템 준비 완료. 시뮬레이션을 시작하세요.");
    panel.add(statusLabel, BorderLayout.WEST);

    JLabel infoLabel = new JLabel("영업시간: 9:00-18:00 | 정각마에 재고 관리 | 1:00에 본사 배송 | 9시 대기 주문 처리");
    infoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    panel.add(infoLabel, BorderLayout.EAST);

    return panel;
  }

  /**
   * 로그 추가
   */
  public void addLog(String message) {
    SwingUtilities.invokeLater(() -> {
      logArea.append(message + "\n");
      // 자동 스크롤
      logArea.setCaretPosition(logArea.getDocument().getLength());
    });
  }

  /**
   * 시뮬레이션 시작
   */
  private void startSimulation() {
    startButton.setEnabled(false);
    stopButton.setEnabled(true);
    statusLabel.setText("시뮬레이션 실행 중...");

    addLog("▶️ 시뮬레이션이 시작되었습니다. 현재 시간: " + timeService.getFormattedTime());

    // 시뮬레이션 서비스 시작
    simulationService.startSimulation();

    // 시뮬레이션 패널 애니메이션 시작
    simulationPanel.startAnimation();

    // 시간 진행 타이머 시작
    simulationTimer = new Timer(600 / timeScale, e -> {
      // 1분씩 시간 진행
      timeService.advanceTime(1);
    });
    simulationTimer.start();
  }

  /**
   * 시뮬레이션 정지
   */
  private void stopSimulation() {
    startButton.setEnabled(true);
    stopButton.setEnabled(false);
    statusLabel.setText("시뮬레이션 정지됨");

    // 시뮬레이션 서비스 정지
    simulationService.stopSimulation();

    // 애니메이션 정지
    simulationPanel.stopAnimation();

    // 시간 진행 타이머 정지
    if (simulationTimer != null) {
      simulationTimer.stop();
      simulationTimer = null;
    }

    addLog("⏹️ 시뮬레이션이 정지되었습니다.");
  }

  /**
   * 재고 관리 실행 (22시)
   */
  private void manageInventory() {
    addLog("🔄 재고 관리를 시작합니다...");

    // 재고 관리 서비스 호출
    inventoryService.transferFromWarehouseToStore();

    addLog("✅ 창고에서 매장으로 재고 이동이 완료되었습니다.");

    // 테이블 즉시 업데이트
    refreshProductTable(inventoryService.getAllProducts());
  }

  /**
   * 본사 배송 받기 (1시)
   */
  private void receiveDeliveryFromHeadquarters() {
    addLog("🚚 본사에서 배송이 도착했습니다...");

    // 본사 배송 서비스 호출
    inventoryService.receiveDeliveryFromHeadquarters();

    addLog("✅ 창고 재고가 보충되었습니다.");

    // 대기 주문 처리
    processWaitingOrders();

    // 테이블 즉시 업데이트
    refreshProductTable(inventoryService.getAllProducts());
    refreshWaitingOrdersTable();
  }

  /**
   * 대기 주문 처리
   */
  private void processWaitingOrders() {
    addLog("📋 대기 주문 처리 중...");
    try {
      // 서비스에서 대기 주문 처리 메소드 호출
      int processed = salesService.processWaitingOrders();
      addLog("✅ " + processed + "건의 대기 주문이 처리되었습니다.");
    } catch (Exception e) {
      addLog("❌ 대기 주문 처리 중 오류 발생: " + e.getMessage());
    }
  }

  /**
   * 대기 주문 테이블 갱신
   */
  private void refreshWaitingOrdersTable() {
    SwingUtilities.invokeLater(() -> {
      waitingOrdersTableModel.setRowCount(0);
      try {
        List<SaleItem> waitingOrders = salesService.getWaitingOrders();
        if (waitingOrders != null) {
          for (SaleItem order : waitingOrders) {
            Product product = inventoryService.getProductById(order.getProductId());
            String productName = (product != null) ? product.getName() : "제품 #" + order.getProductId();

            waitingOrdersTableModel.addRow(new Object[]{
                order.getSaleItemId(),
                productName,
                order.getQuantity(),
                "대기 중"
            });
          }
        }
      } catch (Exception e) {
        // 대기 주문 조회 실패 시 조용히 무시
        System.err.println("대기 주문 로드 실패: " + e.getMessage());
      }
    });
  }

  /**
   * 제품 테이블 갱신
   */
  private void refreshProductTable(List<Product> products) {
    SwingUtilities.invokeLater(() -> {
      productTableModel.setRowCount(0);
      for (Product product : products) {
        String status;
        if (product.getStoreQuantity() > 5) {
          status = "판매 가능";
        } else if (product.getStoreQuantity() > 0) {
          status = "재고 부족";
        } else if (product.getWarehouseQuantity() > 0) {
          status = "창고에서 가져오기";
        } else {
          status = "재고 없음";
        }

        productTableModel.addRow(new Object[]{
            product.getProductId(),
            product.getName(),
            product.getManufacturer(),
            String.format("%,d", product.getPrice()),
            product.getStoreQuantity(),
            product.getWarehouseQuantity(),
            status
        });
      }
    });
  }

  /**
   * CircleAnimationPanel에 접근하기 위한 메소드
   */
  public CircleAnimationPanel getSimulationPanel() {
    return simulationPanel;
  }
}