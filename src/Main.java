import javax.swing.UIManager;
import ui.MainFrame;

/**
 * 애플리케이션 메인 클래스
 */
public class Main {
  public static void main(String[] args) {
    try {
      // Look & Feel 설정
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Swing UI는 EDT(Event Dispatch Thread)에서 실행
    javax.swing.SwingUtilities.invokeLater(() -> {
      try {
        // 메인 프레임 생성 및 표시
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}