/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

/**
 * HTTP Request methods, with the ability to decode a <code>String</code> back to its enum value.
 */
public enum Method {
  GET, PUT, POST, DELETE, HEAD, OPTIONS;

  static Method lookup(String method) {
    for (Method m : Method.values()) {
      if (m.toString().equalsIgnoreCase(method)) {
        return m;
      }
    }
    return null;
  }
  
}
