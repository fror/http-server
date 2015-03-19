/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.http.server.net;

import static com.google.common.io.ByteStreams.readFully;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import be.fror.common.net.OpenedSocket;
import be.fror.common.net.ServerSocketService;
import be.fror.common.net.SocketHandler;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public class ServerSocketServiceTest {

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  private final SocketHolder serverHandler = new SocketHolder();
  private final ServerSocketService service = new ServerSocketService(0, serverHandler);
  private int port;

  @Before
  public void setUp() {
    this.service.startAsync().awaitRunning();
    this.port = this.service.getLocalPort();
  }

  @After
  public void tearDown() {
    this.service.stopAsync().awaitTerminated();
  }

  @Test
  public void canConnect() throws Exception {
    try (OpenedSocket client = connect()) {
      assertEndsConnected(client, serverHandler.sockets.take());
    }
  }

  private OpenedSocket connect() throws IOException {
    OpenedSocket client = OpenedSocket.fromSocket(new Socket(InetAddress.getLoopbackAddress(), this.port));
    return client;
  }

  private void assertEndsConnected(OpenedSocket client, OpenedSocket server) throws IOException {
    assertNotNull("client is null", client);
    assertNotNull("server is null", server);
    assertEndConnected(client.outputStream(), server.inputStream());
    assertEndConnected(server.outputStream(), client.inputStream());
  }

  private void assertEndConnected(OutputStream out, InputStream in) throws IOException {
    byte[] toWrite = new byte[10];
    byte[] toRead = new byte[10];
    new Random().nextBytes(toWrite);
    out.write(toWrite);
    out.flush();
    readFully(in, toRead);
    assertArrayEquals(toWrite, toRead);
  }

  private static class SocketHolder implements SocketHandler {

    private BlockingQueue<OpenedSocket> sockets = new LinkedBlockingQueue();

    @Override
    public void handleSocket(OpenedSocket socket) throws IOException {
      this.sockets.offer(socket);
    }
  }
}
