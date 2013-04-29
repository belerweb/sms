package com.belerweb.sms._9nuo;

import java.util.Date;
import java.util.List;

import org.junit.Test;

public class SmsTest {

  @Test
  public void testGetBalance() {
    System.out.println(Sms.init().getBalance());
  }

  @Test
  public void testSend() {
    SendResult result = Sms.init().send("13811944844", "你好。我正在使用9诺短信平台 Java SDK 测试发送短信。");
    System.out.println("Phone:" + result.getPhone());
    System.out.println("Success:" + result.isSuccess());
    System.out.println("Description:" + result.getDescription());
  }

  @Test
  public void testGetSentHistory() {
    Date end = new Date();
    Date start = new Date(end.getTime() - 86400000);
    List<SmsHistory> histories = Sms.init().getSentHistory(start, end);
    for (SmsHistory smsHistory : histories) {
      System.out.println(smsHistory.getDate() + "\t" + smsHistory.getPhone() + "\t"
          + smsHistory.getContent());
    }
  }

  @Test
  public void testGetDailyReport() {
    Date end = new Date();
    Date start = new Date(end.getTime() - 86400000);
    List<DailyReport> reports = Sms.init().getDailyReport(start, end);
    for (DailyReport report : reports) {
      System.out.println(report.getDate() + "\t" + report.isSuccess() + "\t" + report.getCount());
    }
  }

}
