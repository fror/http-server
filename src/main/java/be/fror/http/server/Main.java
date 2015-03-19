/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.http.server;

import static com.google.common.io.ByteStreams.copy;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.nio.charset.StandardCharsets.UTF_8;

import be.fror.common.net.OpenedSocket;
import be.fror.common.net.ServerSocketService;
import be.fror.common.net.SocketHandler;

import com.google.common.util.concurrent.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public class Main {

  public static void main(String[] args) {
    int port = 8000;
    Service httpServer = new ServerSocketService(port, new AsyncEchoer());
    httpServer.addListener(new ServiceTransitionPrinter(), directExecutor());
    httpServer.startAsync().awaitRunning();
    waitAsyncThenRun(5, TimeUnit.SECONDS, () -> httpServer.stopAsync());
    for (int i = 0; i < 100; i++) {
      new Thread(() -> {
        try (
            Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
            Writer writer = new OutputStreamWriter(socket.getOutputStream(), UTF_8);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8))) {
          String msg = String.format("Thread %s used port %d", Thread.currentThread().getName(), socket.getLocalPort());
          writer.write(msg);
          writer.flush();
          socket.shutdownOutput();
          System.out.println(reader.readLine());
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
      }).start();
    }
  }

  private static class AsyncEchoer implements SocketHandler {

    @Override
    public void handleSocket(OpenedSocket socket) throws IOException {
      new Thread(() -> {
        try (InputStream in = socket.inputStream(); OutputStream out = socket.outputStream()) {
          copy(in, out);
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
      }).start();
    }
  }

  private static void waitAsyncThenRun(long timeout, TimeUnit unit, Runnable runnable) {
    new Thread(() -> {
      try {
        unit.sleep(timeout);
      } catch (InterruptedException e) {
      }
      runnable.run();
    }).start();
  }

  private static class ServiceTransitionPrinter extends Service.Listener {

    @Override
    public void starting() {
      System.out.printf("Starting%n");
    }

    @Override
    public void running() {
      System.out.printf("Running%n");
    }

    @Override
    public void stopping(Service.State from) {
      System.out.printf("Stopping%n");
    }

    @Override
    public void terminated(Service.State from) {
      System.out.printf("Terminated%n");
    }

    @Override
    public void failed(Service.State from, Throwable failure) {
      System.out.printf("Failed coming from state %s%n", from);
      failure.printStackTrace(System.out);
    }
  }
}
