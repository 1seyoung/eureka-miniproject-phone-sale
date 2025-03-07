package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 동그라미 애니메이션 패널
 */
public class CircleAnimationPanel extends JPanel {
  // 애니메이션 관련 변수
  private Timer animationTimer;
  private boolean isAnimating = false;
  private Random random = new Random();

  // 판매원 동그라미 (파란색)
  private Circle salesPerson;

  // 구매자 동그라미들 (빨간색)
  private List<Circle> customers = new ArrayList<>();

  /**
   * 동그라미 클래스
   */
  private class Circle {
    double x, y;             // 현재 위치
    double targetX, targetY; // 목표 위치
    double speed;            // 이동 속도
    int size;                // 크기
    Color color;             // 색상
    boolean isMoving;        // 이동 중인지 여부

    public Circle(double x, double y, int size, Color color) {
      this.x = x;
      this.y = y;
      this.targetX = x;
      this.targetY = y;
      this.size = size;
      this.color = color;
      this.speed = 1.0 + random.nextDouble() * 2.0; // 1~3 사이의 랜덤 속도
      this.isMoving = false;
    }

    // 목표 지점으로 이동
    public void moveTo(double targetX, double targetY) {
      this.targetX = targetX;
      this.targetY = targetY;
      this.isMoving = true;
    }

    // 현재 위치에서 목표 위치로 조금씩 이동
    public void update() {
      if (!isMoving) return;

      double dx = targetX - x;
      double dy = targetY - y;
      double distance = Math.sqrt(dx * dx + dy * dy);

      if (distance < 2.0) {
        // 목표에 거의 도달했으면 정확한 위치로 설정하고 이동 중지
        x = targetX;
        y = targetY;
        isMoving = false;
        return;
      }

      // 목표 방향으로 이동
      double ratio = speed / distance;
      x += dx * ratio;
      y += dy * ratio;
    }

    // 충돌 감지
    public boolean intersects(Circle other) {
      double dx = this.x - other.x;
      double dy = this.y - other.y;
      double distance = Math.sqrt(dx * dx + dy * dy);
      return distance < (this.size/2 + other.size/2);
    }
  }

  /**
   * 생성자
   */
  public CircleAnimationPanel() {
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createTitledBorder("실시간 시뮬레이션"));

    // 판매원 초기화 (파란색 동그라미)
    salesPerson = new Circle(150, 150, 30, new Color(66, 133, 244));

    // 구매자 초기화 (빨간색 동그라미들)
    for (int i = 0; i < 5; i++) {
      customers.add(new Circle(400, 100 + i * 60, 30, new Color(234, 67, 53)));
    }

    // 마우스 클릭 이벤트 처리
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        // 판매원을 클릭한 위치로 이동
        salesPerson.moveTo(e.getX(), e.getY());
      }
    });
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // 레이아웃 영역 그리기
    drawLayout(g2d);

    // 판매원 그리기
    drawCircle(g2d, salesPerson);

    // 모든 구매자 그리기
    for (Circle customer : customers) {
      drawCircle(g2d, customer);

      // 판매원과 구매자가 만났을 때 거래 표시
      if (salesPerson.intersects(customer)) {
        drawTransaction(g2d, salesPerson, customer);
      }
    }
  }

  // 레이아웃 영역 그리기
  private void drawLayout(Graphics2D g2d) {
    int width = getWidth();
    int height = getHeight();

    // 판매원 구역
    g2d.setColor(new Color(240, 248, 255));
    g2d.fillRect(10, 30, width/3 - 20, height - 40);
    g2d.setColor(Color.BLACK);
    g2d.drawString("판매원 구역", 20, 50);

    // 구매자 구역
    g2d.setColor(new Color(255, 240, 245));
    g2d.fillRect(width*2/3 + 10, 30, width/3 - 20, height - 40);
    g2d.setColor(Color.BLACK);
    g2d.drawString("구매자 구역", width*2/3 + 20, 50);

    // 판매 구역
    g2d.setColor(new Color(245, 245, 245));
    g2d.fillRect(width/3, 30, width/3, height - 40);
    g2d.setColor(Color.BLACK);
    g2d.drawString("판매 구역", width/2 - 30, 50);
  }

  // 동그라미 그리기
  private void drawCircle(Graphics2D g2d, Circle circle) {
    g2d.setColor(circle.color);
    g2d.fillOval((int)(circle.x - circle.size/2), (int)(circle.y - circle.size/2), circle.size, circle.size);
  }

  // 거래 표시 그리기
  private void drawTransaction(Graphics2D g2d, Circle salesPerson, Circle customer) {
    // 연결선 그리기
    g2d.setColor(new Color(50, 205, 50, 150)); // 반투명 녹색
    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8}, 0));
    g2d.drawLine((int)salesPerson.x, (int)salesPerson.y, (int)customer.x, (int)customer.y);

    // 거래 중임을 표시하는 텍스트
    int midX = (int)((salesPerson.x + customer.x) / 2);
    int midY = (int)((salesPerson.y + customer.y) / 2);

    g2d.setColor(new Color(0, 100, 0));
    g2d.fillOval(midX - 15, midY - 15, 30, 30);
    g2d.setColor(Color.WHITE);
    g2d.drawString("판매", midX - 15, midY + 5);
  }

  // 애니메이션 시작
  public void startAnimation() {
    if (isAnimating) return;
    isAnimating = true;

    animationTimer = new Timer(30, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateAnimation();
        repaint();
      }
    });
    animationTimer.start();
  }

  // 애니메이션 정지
  public void stopAnimation() {
    if (!isAnimating) return;
    isAnimating = false;

    if (animationTimer != null) {
      animationTimer.stop();
      animationTimer = null;
    }
  }

  // 애니메이션 상태 업데이트
  private void updateAnimation() {
    // 모든 동그라미 업데이트
    salesPerson.update();

    for (Circle customer : customers) {
      customer.update();
    }

    // 가끔 랜덤하게 동그라미들이 움직이도록 설정
    if (random.nextInt(100) < 5) { // 5% 확률로 판매원이 랜덤 위치로 이동
      if (!salesPerson.isMoving) {
        // 판매원 구역 내에서 랜덤 위치로 이동
        double newX = 20 + random.nextInt(getWidth()/3 - 40);
        double newY = 50 + random.nextInt(getHeight() - 100);
        salesPerson.moveTo(newX, newY);
      }
    }

    if (random.nextInt(100) < 3) { // 3% 확률로 구매자가 판매 구역으로 이동
      int idx = random.nextInt(customers.size());
      Circle customer = customers.get(idx);
      if (!customer.isMoving) {
        // 판매 구역으로 이동
        double newX = getWidth()/3 + random.nextInt(getWidth()/3);
        double newY = 50 + random.nextInt(getHeight() - 100);
        customer.moveTo(newX, newY);
      }
    }

    if (random.nextInt(100) < 2) { // 2% 확률로 구매자가 원래 위치로 돌아감
      for (int i = 0; i < customers.size(); i++) {
        Circle customer = customers.get(i);
        if (!customer.isMoving && customer.x < getWidth()*2/3) {
          // 구매자 구역으로 돌아가기
          double newX = getWidth()*2/3 + 50;
          double newY = 100 + i * 60;
          customer.moveTo(newX, newY);
        }
      }
    }
  }
}