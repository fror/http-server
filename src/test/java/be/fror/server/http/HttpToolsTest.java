/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.server.http;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public class HttpToolsTest {

  public HttpToolsTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of decode method, of class HttpTools.
   */
  @Test
  public void testDecode() {
    String[][] strings = {
      {"âé", "%c3%a2%c3%a9"},
      {"   ", "+++"},
      {"abc", "abc"},
      {"Il était une fois", "Il+%c3%a9tait+une+fois"}
    };
    for (final String[] pair : strings) {
      String decoded = pair[0];
      String encoded = pair[1];
      assertThat(encoded, HttpTools.decode(encoded, UTF_8), is(equalTo(decoded)));
    }
  }
}
