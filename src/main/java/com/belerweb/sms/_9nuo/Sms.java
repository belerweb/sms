package com.belerweb.sms._9nuo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 九诺短信平台SMS接口
 * 
 * @author jun
 */
public class Sms {

  private static final HttpClient CLIENT;

  private static final String API_BALANCE = "http://admin.9nuo.com/Interface_http/GetBalance.aspx";// 查询指定用户短信剩余额度
  private static final String API_SEND = "http://admin.9nuo.com/Interface_http/SendSms.aspx";// 指定用户发送短信
  private static final String API_SENT_HISTORY =
      "http://admin.9nuo.com/Interface_http/GetReportDetail.aspx";// 查询指定用户某日期范围内短信发送的明细清单

  private static final String CONFIG_KEY_USERNAME = "9nuo.username";
  private static final String CONFIG_KEY_PASSWORD = "9nuo.password";
  private static final String CONFIG_KEY_MD5_PASSWORD = "9nuo.password.md5";

  private static final SimpleDateFormat DATE_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  private static final SimpleDateFormat YMD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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

  /**
   * 查询余额
   * 
   * @return 剩余短信条数
   * @throws SmsException
   */
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

  /**
   * 发送短信
   * 
   * @param to 收信人 （单个）
   * @param content 短信内容
   * @return 发送结果
   */
  public SendResult send(String to, String content) {
    if (!to.matches("\\d+")) {
      return new SendResult(to, false, "此接口只 支持一个号码");
    }

    try {
      NameValuePair phone = new NameValuePair("phone", to);
      NameValuePair note = new NameValuePair("note", content);
      NameValuePair[] parameters = new NameValuePair[] {username, password, phone, note};
      PostMethod post = new PostMethod(API_SEND);
      post.setRequestBody(parameters);
      int code = CLIENT.executeMethod(post);
      if (code == HttpStatus.SC_OK) {
        return parseSendResult(post.getResponseBodyAsStream()).get(0);
      } else {
        throw new SmsException("HTTP:" + code);
      }
    } catch (HttpException e) {
      e.printStackTrace();
      throw new SmsException(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      throw new SmsException(e.getMessage());
    }
  }

  public List<SmsHistory> getSentHistory(Date start, Date end) {
    String htmlResult = null;
    try {
      NameValuePair phone = new NameValuePair("startDate", YMD_DATE_FORMAT.format(start));
      NameValuePair note = new NameValuePair("endDate", YMD_DATE_FORMAT.format(end));
      NameValuePair[] parameters = new NameValuePair[] {username, password, phone, note};
      PostMethod post = new PostMethod(API_SENT_HISTORY);
      post.setRequestBody(parameters);
      int code = CLIENT.executeMethod(post);
      if (code == HttpStatus.SC_OK) {
        htmlResult = post.getResponseBodyAsString();
        return parseHistoryResult(new ByteArrayInputStream(htmlResult.getBytes()));
      } else {
        throw new SmsException(code + ":" + htmlResult);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new SmsException(htmlResult == null ? e.getMessage() : htmlResult);
    }
  }

  private List<SendResult> parseSendResult(InputStream input) {
    try {
      List<SendResult> result = new ArrayList<SendResult>();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(input);
      Element element = document.getDocumentElement();

      NodeList nodes = element.getElementsByTagName("dt");
      for (int i = 0; i < nodes.getLength(); i++) {
        String phone = null;
        boolean success = false;
        String descrption = null;
        Element dtElement = (Element) nodes.item(i);
        NodeList childNodes = dtElement.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
          if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
            String nodeName = childNodes.item(j).getNodeName();
            Node firstChild = childNodes.item(j).getFirstChild();
            if ("FPhone".equals(nodeName)) {
              phone = firstChild.getNodeValue();
            } else if ("FResult".equals(nodeName)) {
              success = "发送成功".equals(firstChild.getNodeValue());
            } else if ("FDescription".equals(nodeName)) {
              if (firstChild != null) {
                descrption = firstChild.getNodeValue();
              }
            }
          }
        }
        result.add(new SendResult(phone, success, descrption));
      }
      return result;
    } catch (Exception e) {
      throw new SmsException(e.getMessage());
    }
  }

  private List<SmsHistory> parseHistoryResult(InputStream input) throws Exception {
    List<SmsHistory> result = new ArrayList<SmsHistory>();
    NodeList nodes =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input).getDocumentElement()
            .getElementsByTagName("Table");
    for (int i = 0; i < nodes.getLength(); i++) {
      String phone = null;
      Date date = null;
      String content = null;
      boolean success = false;
      Element dtElement = (Element) nodes.item(i);
      NodeList childNodes = dtElement.getChildNodes();
      for (int j = 0; j < childNodes.getLength(); j++) {
        if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
          String nodeName = childNodes.item(j).getNodeName();
          Node firstChild = childNodes.item(j).getFirstChild();
          if ("Phone".equals(nodeName)) {
            phone = firstChild.getNodeValue();
          } else if ("SendDate".equals(nodeName)) {
            date = DATE_FORMAT.parse(firstChild.getNodeValue().replaceAll(":(\\d{2})$", "$1"));
          } else if ("Note".equals(nodeName)) {
            content = firstChild.getNodeValue();
          } else if ("result".equals(nodeName)) {
            success = "成功".equals(firstChild.getNodeValue());
          }
        }
      }
      SmsHistory history = new SmsHistory();
      history.setPhone(phone);
      history.setDate(date);
      history.setContent(content);
      history.setSuccess(success);
      result.add(history);
    }
    return result;
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
    CLIENT.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
  }

}
