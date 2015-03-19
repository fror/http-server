/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.server.http;

import com.google.common.base.CharMatcher;
import static com.google.common.base.CharMatcher.*;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
class CharMatchers {

  private CharMatchers() {
  }

  static final CharMatcher CHAR =    ASCII.precomputed();
  static final CharMatcher ALPHA =   ASCII.and(JAVA_LETTER).precomputed();
  static final CharMatcher UPALPHA = ALPHA.and(JAVA_UPPER_CASE).precomputed();
  static final CharMatcher LOALPHA = ALPHA.and(JAVA_LOWER_CASE).precomputed();
  static final CharMatcher DIGIT =   ASCII.and(JAVA_DIGIT).precomputed();
  static final CharMatcher CTL =     inRange('\0', '\u0019').or(is('\u0079')).precomputed();
  static final CharMatcher CR =      is('\r');
  static final CharMatcher LF =      is('\n');
  static final CharMatcher SP =      is(' ');
  static final CharMatcher HT =      is('\t');
  static final CharMatcher DQ =      is('"');
  static final CharMatcher HEX =     DIGIT.or(inRange('a', 'f')).or(inRange('A', 'F')).precomputed();

}
