/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.server.http;

import static com.google.common.base.Preconditions.*;

import java.nio.charset.Charset;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
class HttpTools {

  private HttpTools() {
  }

  static String decode(String s, Charset charset) {
    checkNotNull(s, "s must not be null");
    checkNotNull(charset, "charset must not be null");
    final int length = s.length();
    final StringBuilder decoded = new StringBuilder(length);
    byte[] bytes = new byte[length / 3 + 1];
    for (int i = 0; i < length; i++) {
      char c = s.charAt(i);
      switch (c) {
        case '+':
          decoded.append(' ');
          break;
        case '%':
          int len = 0;
          do {
            checkArgument(i + 2 < length, "Incomplete trailing escape pattern");
            String sub = s.substring(i + 1, i + 3);
            checkArgument(CharMatchers.HEX.matchesAllOf(sub), "Illegal hex character in escape pattern");
            bytes[len++] = (byte) Integer.parseInt(sub, 16);
            i += 3;
            if (i < length) {
              c = s.charAt(i);
              if (c != '%') {
                i--;
                break;
              }
            } else if (i == length) {
              break;
            }
          } while (true);
          decoded.append(new String(bytes, 0, len, charset));
          break;
        default:
          decoded.append(c);
          break;
      }
    }
    return decoded.toString();
  }
}
