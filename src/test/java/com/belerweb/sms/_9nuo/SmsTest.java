package com.belerweb.sms._9nuo;

import org.junit.Test;

public class SmsTest {

  @Test
  public void testGetBalance() {
    System.out.println(Sms.init().getBalance());
  }

  @Test
  public void testSend() {
    SendResult result = Sms.init().send("13811944844", "你好。");
    System.out.println("Phone:" + result.getPhone());
    System.out.println("Success:" + result.isSuccess());
    System.out.println("Description:" + result.getDescription());
  }

}
