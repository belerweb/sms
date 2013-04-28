package com.belerweb.sms._9nuo;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * 九诺短信平台SMS接口
 * 
 * @author jun
 */
public class Sms {

  private static final HttpClient CLIENT;

  private static final String API_BALANCE = "http://admin.9nuo.com/Interface_http/GetBalance.aspx";// 查询指定用户短信剩余额度

  private static final String CONFIG_KEY_USERNAME = "9nuo.username";
  private static final String CONFIG_KEY_PASSWORD = "9nuo.password";
  private static final String CONFIG_KEY_MD5_PASSWORD = "9nuo.password.md5";

  public static final String PARAM_NAME_USERNAME = "userName";
  public static final String PARAM_NAME_PASSWORD = "pwd";

  private NameValuePair username;
  private NameValuePair password;

  private Sms(Properties properties) {
    this.username =
        new NameValuePair(PARAM_NAME_USERNAME, properties.getProperty(PARAM_NAME_USERNAME));
    this.password =
        new NameValuePair(PARAM_NAME_PASSWORD, properties.getProperty(PARAM_NAME_PASSWORD));
  }

  public int getBalance() throws SmsException {
    try {
      PostMethod post = new PostMethod(API_BALANCE);
      NameValuePair[] parameters = new NameValuePair[] {username, password};
      post.setRequestBody(parameters);
      int code = CLIENT.executeMethod(post);
      String result = post.getResponseBodyAsString();
      if (code == HttpStatus.SC_OK) {
        try {
          return Integer.parseInt(result);
        } catch (NumberFormatException e) {
          throw new SmsException(result);
        }
      } else {
        throw new SmsException(code + ":" + result);
      }
    } catch (HttpException e) {
      e.printStackTrace();
      throw new SmsException(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      throw new SmsException(e.getMessage());
    }
  }

  public static Sms init() {
    String username = System.getProperty(CONFIG_KEY_USERNAME, System.getenv(CONFIG_KEY_USERNAME));
    assert username != null : configError(CONFIG_KEY_USERNAME);

    String password = System.getProperty(CONFIG_KEY_PASSWORD, System.getenv(CONFIG_KEY_PASSWORD));
    String md5Password =
        System.getProperty(CONFIG_KEY_MD5_PASSWORD, System.getenv(CONFIG_KEY_MD5_PASSWORD));
    assert password != null || md5Password != null : configError(CONFIG_KEY_PASSWORD + " or  "
        + CONFIG_KEY_MD5_PASSWORD);

    Properties properties = new Properties();
    properties.put(PARAM_NAME_USERNAME, username);
    properties.put(PARAM_NAME_PASSWORD, md5Password != null ? md5Password : DigestUtils
        .md5Hex(password));
    return init(properties);
  }

  private static String configError(String key) {
    return "Need " + key + " config in system properties or environment";
  }

  public static Sms init(Properties properties) {
    assert properties != null : "properties is need.";
    assert properties.get(PARAM_NAME_USERNAME) != null : "userName is need.";
    assert properties.get(PARAM_NAME_PASSWORD) != null : "pwd is need.";
    return new Sms(properties);
  }

  static {
    CLIENT = new HttpClient();
  }

}
