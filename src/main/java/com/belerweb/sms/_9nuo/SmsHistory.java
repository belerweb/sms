package com.belerweb.sms._9nuo;

import java.util.Date;

/**
 * 短信记录
 * 
 * @author jun
 */
public class SmsHistory {

  private String phone;
  private Date date;
  private String content;
  private boolean success;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}
