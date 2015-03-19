/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

// ------------------------------------------------------------------------------- //

/**
 * Temp file manager.
 * <p/>
 * <p>
 * Temp file managers are created 1-to-1 with incoming requests, to create and cleanup temporary
 * files created as a result of handling the request.</p>
 */
public interface TempFileManager {

  TempFile createTempFile() throws Exception;

  void clear();
  
}
