/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.common.net;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public final class OpenedSocket implements Closeable {

  public static OpenedSocket fromSocket(Socket socket) throws IOException {
    checkNotNull(socket, "socket must not be null");
    return new OpenedSocket(new SocketOutputStream(socket), new SocketInputStream(socket));
  }

  private final InputStream inputStream;
  private final OutputStream outputStream;

  private OpenedSocket(OutputStream outputStream, InputStream inputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public InputStream inputStream() {
    return inputStream;
  }

  public OutputStream outputStream() {
    return outputStream;
  }

  @Override
  public void close() throws IOException {
    try (InputStream in = inputStream(); OutputStream out = outputStream()) {
      in.close();
      out.close();
    }
  }

  private static class SocketInputStream extends FilterInputStream {

    private final Socket socket;

    private SocketInputStream(Socket socket) throws IOException {
      super(socket.getInputStream());
      this.socket = socket;
    }

    @Override
    public void close() throws IOException {
      synchronized (this.socket) {
        if (!this.socket.isInputShutdown()) {
          this.socket.shutdownInput();
        }
        if (this.socket.isOutputShutdown()) {
          this.socket.close();
        }
      }
    }
  }

  private static class SocketOutputStream extends FilterOutputStream {

    private final Socket socket;

    private SocketOutputStream(Socket socket) throws IOException {
      super(socket.getOutputStream());
      this.socket = socket;
    }

    @Override
    public void close() throws IOException {
      synchronized (this.socket) {
        if (!this.socket.isOutputShutdown()) {
          this.socket.shutdownOutput();
        }
        if (this.socket.isInputShutdown()) {
          this.socket.close();
        }
      }
    }
  }
}
