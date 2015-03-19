/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

/**
 * Default threading strategy for NanoHttpd.
 * <p/>
 * <p>
 * By default, the server spawns a new Thread for every incoming request. These are set to
 * <i>daemon</i> status, and named according to the request number. The name is useful when
 * profiling the application.</p>
 */
public class DefaultAsyncRunner implements AsyncRunner {
  private long requestCount;

  @Override
  public void exec(Runnable code) {
    ++requestCount;
    Thread t = new Thread(code);
    t.setDaemon(true);
    t.setName("NanoHttpd Request Processor (#" + requestCount + ")");
    t.start();
  }
  
}
