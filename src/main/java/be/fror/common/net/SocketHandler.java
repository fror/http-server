/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.common.net;

import com.google.common.util.concurrent.MoreExecutors;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public interface SocketHandler {

  /**
   * This method <strong>must</strong> close both streams.
   *
   * @param socket
   * @throws IOException
   */
  public void handleSocket(OpenedSocket socket) throws IOException;

  /**
   * The <tt>Executor</tt> that must be used to run
   * <tt>{@link #handleSocket(be.fror.common.net.OpenedSocket)}</tt>.
   * 
   * This method is called only once per service.
   *
   * The default implementation returns an <tt>Executor</tt> that run in the same thread as its
   * invoker.
   *
   * @return
   */
  public default ExecutorService executorService() {
    return MoreExecutors.newDirectExecutorService();
  }

}
