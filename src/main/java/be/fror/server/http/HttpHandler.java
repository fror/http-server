/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.server.http;

import be.fror.common.net.OpenedSocket;
import be.fror.common.net.SocketHandler;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public class HttpHandler implements SocketHandler {

  @Override
  public void handleSocket(OpenedSocket socket) throws IOException {

  }

  @Override
  public ExecutorService executorService() {
    return Executors.newCachedThreadPool();
  }
}
