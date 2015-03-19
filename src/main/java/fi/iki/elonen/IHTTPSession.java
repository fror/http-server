/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Handles one session, i.e. parses the HTTP request and returns the response.
 */
public interface IHTTPSession {

  void execute() throws IOException;

  Map<String, String> getParms();

  Map<String, String> getHeaders();

  /**
   * @return the path part of the URL.
   */
  String getUri();

  String getQueryParameterString();

  Method getMethod();

  InputStream getInputStream();

  CookieHandler getCookies();

  /**
   * Adds the files in the request body to the files map.
   *
   * @param files
   * @throws java.io.IOException
   * @throws fi.iki.elonen.NanoHTTPD.ResponseException
   * @arg files - map to modify
   */
  void parseBody(Map<String, String> files) throws IOException, ResponseException;
  
}
