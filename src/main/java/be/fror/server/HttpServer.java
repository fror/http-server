/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.server;

import be.fror.common.net.ServerSocketService;
import be.fror.server.http.HttpHandler;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public class HttpServer {

  public static void main(String[] args) {
    int port = args.length >= 1 ? Integer.parseInt(args[0]) : 8000;
    ServerSocketService service = new ServerSocketService(port, new HttpHandler());
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      service.stopAsync();
    }));
    service.startAsync();
  }
}
