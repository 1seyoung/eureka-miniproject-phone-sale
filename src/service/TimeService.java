package service;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 시뮬레이션 시간 관리 서비스
 */
public class TimeService {
  private LocalTime currentTime;
  private LocalDate currentDate;
  private List<TimeEventListener> listeners = new ArrayList<>();

  // 시간 이벤트를 수신할 리스너 인터페이스
  public interface TimeEventListener {
    void onTimeChanged(LocalTime time, LocalDate date);
    void onHourChanged(int hour);
    void onDayChanged(LocalDate date);
  }

  public TimeService() {
    // 초기 시간 설정 (9:00)
    currentTime = LocalTime.of(9, 0);
    currentDate = LocalDate.now();
  }

  /**
   * 시간 이벤트 리스너 등록
   */
  public void addTimeEventListener(TimeEventListener listener) {
    listeners.add(listener);
  }

  /**
   * 시간 진행 (분 단위)
   */
  public void advanceTime(int minutes) {
    // 이전 시간과 시간
    int previousHour = currentTime.getHour();
    LocalDate previousDate = currentDate;

    // 시간 진행
    currentTime = currentTime.plusMinutes(minutes);

    // 자정을 넘어가면 날짜 변경
    if (currentTime.isBefore(LocalTime.of(previousHour, 0)) && previousHour > 20) {
      currentDate = currentDate.plusDays(1);
    }

    // 모든 리스너에게 시간 변경 알림
    for (TimeEventListener listener : listeners) {
      listener.onTimeChanged(currentTime, currentDate);

      // 시간이 바뀌었으면 시간 변경 이벤트 발생
      if (currentTime.getHour() != previousHour) {
        listener.onHourChanged(currentTime.getHour());
      }

      // 날짜가 바뀌었으면 날짜 변경 이벤트 발생
      if (!currentDate.isEqual(previousDate)) {
        listener.onDayChanged(currentDate);
      }
    }
  }

  /**
   * 현재 시간 설정
   */
  public void setTime(LocalTime time) {
    currentTime = time;
  }

  /**
   * 현재 날짜 설정
   */
  public void setDate(LocalDate date) {
    currentDate = date;
  }

  /**
   * 현재 시간 반환
   */
  public LocalTime getCurrentTime() {
    return currentTime;
  }

  /**
   * 현재 날짜 반환
   */
  public LocalDate getCurrentDate() {
    return currentDate;
  }

  /**
   * 현재 시간을 문자열로 반환 (HH:mm 형식)
   */
  public String getFormattedTime() {
    return currentTime.format(DateTimeFormatter.ofPattern("HH:mm"));
  }

  /**
   * 현재 날짜를 문자열로 반환 (yyyy-MM-dd 형식)
   */
  public String getFormattedDate() {
    return currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }

  /**
   * 영업 시간 여부 확인 (9:00 ~ 18:00)
   */
  public boolean isBusinessHour() {
    int hour = currentTime.getHour();
    return hour >= 9 && hour < 18;
  }
}