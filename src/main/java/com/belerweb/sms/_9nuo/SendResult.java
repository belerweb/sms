package com.belerweb.sms._9nuo;

/**
 * 短信发送结果
 * 
 * @author jun
 * 
 */
public class SendResult {

  private String phone;
  private boolean success;
  private String description;

  public SendResult(String phone, boolean success) {
    this.phone = phone;
    this.success = success;
  }

  public SendResult(String phone, boolean success, String description) {
    this.phone = phone;
    this.success = success;
    this.description = description;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
