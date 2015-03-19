/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.common.net;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public class ServerSocketService extends AbstractExecutionThreadService {

  private final int port;
  private final SocketHandler socketHandler;

  private ExecutorService pool;
  private ServerSocket serverSocket;

  public ServerSocketService(int port, SocketHandler socketHandler) {
    this.port = port;
    this.socketHandler = checkNotNull(socketHandler);
  }

  public int getLocalPort() {
    awaitRunning();
    checkState(serverSocket != null, "Socket has not been opened yet");
    return serverSocket.getLocalPort();
  }

  @Override
  protected void startUp() throws IOException {
    this.serverSocket = new ServerSocket(this.port);
    this.pool = this.socketHandler.executorService();
  }

  @Override
  protected void run() throws IOException {
    while (isRunning()) {
      try {
        Socket clientSocket = this.serverSocket.accept();
        this.pool.execute(() -> {
          try {
            OpenedSocket openedSocket = OpenedSocket.fromSocket(clientSocket);
            this.socketHandler.handleSocket(openedSocket);
          } catch (Throwable t) {
            t.printStackTrace(System.err);
          }
        });
      } catch (SocketException e) {
        if (isRunning()) {
          throw e;
        } // else interrupted because of shutdown, we don't add exceptions...
      }
    }
  }

  @Override
  protected void triggerShutdown() {
    try {
      this.serverSocket.close();
    } catch (IOException e) {
    }
  }

  @Override
  protected void shutDown() throws IOException {
    this.pool.shutdown();
    try {
      if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
          throw new RuntimeException("Executor service did not end in time");
        }
      }
    } catch (InterruptedException ie) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    } finally {
      try {
        this.serverSocket.close();
      } catch (IOException e) {
      }
    }
  }
}
