/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

/**
 * Default strategy for creating and cleaning up temporary files.
 */
class DefaultTempFileManagerFactory implements TempFileManagerFactory {
  private final NanoHTTPD outer;

  DefaultTempFileManagerFactory(final NanoHTTPD outer) {
    this.outer = outer;
  }

  @Override
  public TempFileManager create() {
    return new DefaultTempFileManager();
  }
  
}
