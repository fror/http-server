/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public final class ResponseException extends Exception {
  private final Status status;

  public ResponseException(Status status, String message) {
    super(message);
    this.status = status;
  }

  public ResponseException(Status status, String message, Exception e) {
    super(message, e);
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }
  
}
