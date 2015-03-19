/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

import java.io.OutputStream;

/**
 * A temp file.
 * <p/>
 * <p>
 * Temp files are responsible for managing the actual temporary storage and cleaning themselves up
 * when no longer needed.</p>
 */
public interface TempFile {

  OutputStream open() throws Exception;

  void delete() throws Exception;

  String getName();
  
}
