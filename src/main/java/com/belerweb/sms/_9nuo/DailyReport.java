package com.belerweb.sms._9nuo;

import java.util.Date;

/**
 * 短信每日汇总
 * 
 * @author jun
 */
public class DailyReport {

  private Date date;
  private boolean success;
  private int count;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

}
