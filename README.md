# [LG U+ 유레카] 미니 프로젝트 - 휴대폰 판매 시뮬레이션 시스템

## 프로젝트 요구사항
- JDBC 와 Swing 을 이용한 휴대폰 판매관리 시스템 구현

## 프로젝트 개요
해당 프로젝트는 핸드폰 판매 관리 시스템을 가상으로 시뮬레이션하는 것으로, 매장에서의 핸드폰 판매, 재고 관리, 주문 처리 이벤트를 자동화하여 시각적으로 보여주는 Java Swing 기반 데스크톱 애플리케이션

## 기술 스택

| 기술          | 사용 목적                                   |
|--------------|--------------------------------------------|
| **Java (Swing)**  | GUI 기반 시뮬레이션 View 및 로직 개발    |
| **MySQL**        | 제품 및 판매 데이터 관리                |
| **JDBC**         | 데이터베이스 연동                        |
| **Git / GitHub** | 버전 관리 및 협업                        |


## ERD

![image](https://github.com/user-attachments/assets/b70ef568-b3fe-4254-b01b-28219f259b99)


## 주요 기능

판매 처리: 즉시 판매 처리 및 대기 주문 등록
자동 재고 이동: 영업 시간(9시-18시) 정각마다 창고에서 매장으로 재고 자동 이동
본사 배송: 매일 01시에 본사에서 창고로 제품 배송 처리
대기 주문 처리: 재고 확보 시 자동으로 대기 주문 처리
시뮬레이션 기능: 고객 방문, 판매, 재고 관리 프로세스 시뮬레이션
시각적 모니터링: 재고 현황 및 판매 상태를 실시간으로 시각화


## 시스템 구조 

~~~
eureka-phone-sales-system/
├── src/
│   ├── ui/
│   │   ├── MainFrame.java
│   │   └── CircleAnimationPanel.java
│   ├── service/
│   │   ├── TimeService.java
│   │   ├── InventoryService.java
│   │   ├── SalesService.java
│   │   └── SimulationService.java
│   ├── dao/
│   │   ├── ProductDAO.java
│   │   └── SaleDAO.java
│   ├── dto/
│   │   ├── Product.java
│   │   ├── Sale.java
│   │   ├── SaleItem.java
│   │   └── WaitingOrder.java
│   ├── common/
│   │   └── DBManager.java
│   └── Main.java
├── resources/
│   └── database.properties
├── lib/
│   └── mysql-connector-j-8.3.0.jar
├── README.md
└── .gitignore
~~~

## 스크린샷

<img width="1312" alt="image" src="https://github.com/user-attachments/assets/da5f5f61-67a3-411c-9827-8c2a7f18d767" />
<img width="1312" alt="스크린샷 2025-03-07 오후 11 18 26" src="https://github.com/user-attachments/assets/b2e89286-f508-470b-b843-ecb865918ee3" />
<img width="1312" alt="스크린샷 2025-03-07 오후 11 19 13" src="https://github.com/user-attachments/assets/b9fcf1e5-1601-4252-85b7-d7eb382ff303" />
<img width="1312" alt="스크린샷 2025-03-07 오후 11 20 01" src="https://github.com/user-attachments/assets/680773a2-63e6-400b-9c17-1a0d77814dcc" />
