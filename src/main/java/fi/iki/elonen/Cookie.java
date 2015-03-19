/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public class Cookie {
  private String n;
  private String v;
  private String e;

  public Cookie(String name, String value, String expires) {
    n = name;
    v = value;
    e = expires;
  }

  public Cookie(String name, String value) {
    this(name, value, 30);
  }

  public Cookie(String name, String value, int numDays) {
    n = name;
    v = value;
    e = getHTTPTime(numDays);
  }

  public String getHTTPHeader() {
    String fmt = "%s=%s; expires=%s";
    return String.format(fmt, n, v, e);
  }

  public static String getHTTPTime(int days) {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    calendar.add(Calendar.DAY_OF_MONTH, days);
    return dateFormat.format(calendar.getTime());
  }
  
}
