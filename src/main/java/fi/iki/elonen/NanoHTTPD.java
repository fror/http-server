package fi.iki.elonen;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * A simple, tiny, nicely embeddable HTTP server in Java
 * <p/>
 * <p/>
 * NanoHTTPD
 * <p>
 * </p>Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen, 2010 by
 * Konstantinos Togias</p>
 * <p/>
 * <p/>
 * <b>Features + limitations: </b>
 * <ul>
 * <p/>
 * <li>Only one Java file</li>
 * <li>Java 5 compatible</li>
 * <li>Released as open source, Modified BSD licence</li>
 * <li>No fixed config files, logging, authorization etc. (Implement yourself if you need
 * them.)</li>
 * <li>Supports parameter parsing of GET and POST methods (+ rudimentary PUT support in 1.25)</li>
 * <li>Supports both dynamic content and file serving</li>
 * <li>Supports file upload (since version 1.2, 2010)</li>
 * <li>Supports partial content (streaming)</li>
 * <li>Supports ETags</li>
 * <li>Never caches anything</li>
 * <li>Doesn't limit bandwidth, request time or simultaneous connections</li>
 * <li>Default code serves files and shows all HTTP parameters and headers</li>
 * <li>File server supports directory listing, index.html and index.htm</li>
 * <li>File server supports partial content (streaming)</li>
 * <li>File server supports ETags</li>
 * <li>File server does the 301 redirection trick for directories without '/'</li>
 * <li>File server supports simple skipping for files (continue download)</li>
 * <li>File server serves also very long files without memory overhead</li>
 * <li>Contains a built-in list of most common mime types</li>
 * <li>All header names are converted lowercase so they don't vary between browsers/clients</li>
 * <p/>
 * </ul>
 * <p/>
 * <p/>
 * <b>How to use: </b>
 * <ul>
 * <p/>
 * <li>Subclass and implement serve() and embed to your own program</li>
 * <p/>
 * </ul>
 * <p/>
 * See the separate "LICENSE.md" file for the distribution license (Modified BSD licence)
 */
public abstract class NanoHTTPD {

  /**
   * Maximum time to wait on Socket.getInputStream().read() (in milliseconds) This is required as
   * the Keep-Alive HTTP connections would otherwise block the socket reading thread forever (or as
   * long the browser is open).
   */
  public static final int SOCKET_READ_TIMEOUT = 5000;
  /**
   * Common mime type for dynamic content: plain text
   */
  public static final String MIME_PLAINTEXT = "text/plain";
  /**
   * Common mime type for dynamic content: html
   */
  public static final String MIME_HTML = "text/html";
  /**
   * Pseudo-Parameter to use to store the actual query string in the parameters map for later
   * re-processing.
   */
  private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";
  private final String hostname;
  private final int myPort;
  private ServerSocket myServerSocket;
  private Set<Socket> openConnections = new HashSet<>();
  private Thread myThread;
  /**
   * Pluggable strategy for asynchronously executing requests.
   */
  private AsyncRunner asyncRunner;
  /**
   * Pluggable strategy for creating and cleaning up temporary files.
   */
  private TempFileManagerFactory tempFileManagerFactory;

  /**
   * Constructs an HTTP server on given port.
   *
   * @param port
   */
  public NanoHTTPD(int port) {
    this(null, port);
  }

  /**
   * Constructs an HTTP server on given hostname and port.
   *
   * @param hostname
   * @param port
   */
  public NanoHTTPD(String hostname, int port) {
    this.hostname = hostname;
    this.myPort = port;
    this.tempFileManagerFactory = new DefaultTempFileManagerFactory(this);
    this.asyncRunner = new DefaultAsyncRunner();
  }

  static void safeClose(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Start the server.
   *
   * @throws IOException if the socket is in use.
   */
  public void start() throws IOException {
    myServerSocket = new ServerSocket();
    myServerSocket.bind((hostname != null) ? new InetSocketAddress(hostname, myPort) : new InetSocketAddress(myPort));

    myThread = new Thread(() -> {
      do {
        try {
          final Socket finalAccept = myServerSocket.accept();
          registerConnection(finalAccept);
          finalAccept.setSoTimeout(SOCKET_READ_TIMEOUT);
          final InputStream inputStream = finalAccept.getInputStream();
          asyncRunner.exec(() -> {
            OutputStream outputStream = null;
            try {
              outputStream = finalAccept.getOutputStream();
              TempFileManager tempFileManager = tempFileManagerFactory.create();
              HTTPSession session = new HTTPSession(tempFileManager, inputStream, outputStream, finalAccept.getInetAddress(), this);
              while (!finalAccept.isClosed()) {
                session.execute();
              }
            } catch (Exception e) {
              // When the socket is closed by the client, we throw our own SocketException
              // to break the  "keep alive" loop above.
              if (!(e instanceof SocketException && "NanoHttpd Shutdown".equals(e.getMessage()))) {
                e.printStackTrace(System.err);
              }
            } finally {
              safeClose(outputStream);
              safeClose(inputStream);
              safeClose(finalAccept);
              unRegisterConnection(finalAccept);
            }
          });
        } catch (IOException e) {
        }
      } while (!myServerSocket.isClosed());
    });
    myThread.setDaemon(true);
    myThread.setName("NanoHttpd Main Listener");
    myThread.start();
  }

  /**
   * Stop the server.
   */
  public void stop() {
    try {
      safeClose(myServerSocket);
      closeAllConnections();
      if (myThread != null) {
        myThread.join();
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  /**
   * Registers that a new connection has been set up.
   *
   * @param socket the {@link Socket} for the connection.
   */
  public synchronized void registerConnection(Socket socket) {
    openConnections.add(socket);
  }

  /**
   * Registers that a connection has been closed
   *
   * @param socket the {@link Socket} for the connection.
   */
  public synchronized void unRegisterConnection(Socket socket) {
    openConnections.remove(socket);
  }

  /**
   * Forcibly closes all connections that are open.
   */
  public synchronized void closeAllConnections() {
    openConnections.stream().forEach((socket) -> {
      safeClose(socket);
    });
  }

  public final int getListeningPort() {
    return myServerSocket == null ? -1 : myServerSocket.getLocalPort();
  }

  public final boolean wasStarted() {
    return myServerSocket != null && myThread != null;
  }

  public final boolean isAlive() {
    return wasStarted() && !myServerSocket.isClosed() && myThread.isAlive();
  }

  /**
   * Override this to customize the server.
   * <p/>
   * <p/>
   * (By default, this delegates to serveFile() and allows directory listing.)
   *
   * @param uri Percent-decoded URI without parameters, for example "/index.cgi"
   * @param method "GET", "POST" etc.
   * @param parms Parsed, percent decoded parameters from URI and, in case of POST, data.
   * @param headers Header entries, percent decoded
   * @param files
   * @return HTTP response, see class Response for details
   */
  @Deprecated
  public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
      Map<String, String> files) {
    return new Response(Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
  }

  /**
   * Override this to customize the server.
   * <p/>
   * <p/>
   * (By default, this delegates to serveFile() and allows directory listing.)
   *
   * @param session The HTTP session
   * @return HTTP response, see class Response for details
   */
  public Response serve(IHTTPSession session) {
    Map<String, String> files = new HashMap<>();
    Method method = session.getMethod();
    if (Method.PUT.equals(method) || Method.POST.equals(method)) {
      try {
        session.parseBody(files);
      } catch (IOException ioe) {
        return new Response(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
      } catch (ResponseException re) {
        return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
      }
    }

    Map<String, String> parms = session.getParms();
    parms.put(QUERY_STRING_PARAMETER, session.getQueryParameterString());
    return serve(session.getUri(), method, session.getHeaders(), parms, files);
  }

  /**
   * Decode percent encoded <code>String</code> values.
   *
   * @param str the percent encoded <code>String</code>
   * @return expanded form of the input, for example "foo%20bar" becomes "foo bar"
   */
  protected String decodePercent(String str) {
    String decoded = null;
    try {
      decoded = URLDecoder.decode(str, "UTF8");
    } catch (UnsupportedEncodingException ignored) {
    }
    return decoded;
  }

  /**
   * Decode parameters from a URL, handing the case where a single parameter name might have been
   * supplied several times, by return lists of values. In general these lists will contain a single
   * element.
   *
   * @param parms original <b>NanoHttpd</b> parameters values, as passed to the <code>serve()</code>
   * method.
   * @return a map of <code>String</code> (parameter name) to <code>List&lt;String&gt;</code> (a
   * list of the values supplied).
   */
  protected Map<String, List<String>> decodeParameters(Map<String, String> parms) {
    return this.decodeParameters(parms.get(QUERY_STRING_PARAMETER));
  }

  /**
   * Decode parameters from a URL, handing the case where a single parameter name might have been
   * supplied several times, by return lists of values. In general these lists will contain a single
   * element.
   *
   * @param queryString a query string pulled from the URL.
   * @return a map of <code>String</code> (parameter name) to <code>List&lt;String&gt;</code> (a
   * list of the values supplied).
   */
  protected Map<String, List<String>> decodeParameters(String queryString) {
    Map<String, List<String>> parms = new HashMap<>();
    if (queryString != null) {
      StringTokenizer st = new StringTokenizer(queryString, "&");
      while (st.hasMoreTokens()) {
        String e = st.nextToken();
        int sep = e.indexOf('=');
        String propertyName = (sep >= 0) ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
        if (!parms.containsKey(propertyName)) {
          parms.put(propertyName, new ArrayList<>());
        }
        String propertyValue = (sep >= 0) ? decodePercent(e.substring(sep + 1)) : null;
        if (propertyValue != null) {
          parms.get(propertyName).add(propertyValue);
        }
      }
    }
    return parms;
  }

  // ------------------------------------------------------------------------------- //
  //
  // Threading Strategy.
  //
  // ------------------------------------------------------------------------------- //
  /**
   * Pluggable strategy for asynchronously executing requests.
   *
   * @param asyncRunner new strategy for handling threads.
   */
  public void setAsyncRunner(AsyncRunner asyncRunner) {
    this.asyncRunner = asyncRunner;
  }

  // ------------------------------------------------------------------------------- //
  //
  // Temp file handling strategy.
  //
  // ------------------------------------------------------------------------------- //
  /**
   * Pluggable strategy for creating and cleaning up temporary files.
   *
   * @param tempFileManagerFactory new strategy for handling temp files.
   */
  public void setTempFileManagerFactory(TempFileManagerFactory tempFileManagerFactory) {
    this.tempFileManagerFactory = tempFileManagerFactory;
  }

}
