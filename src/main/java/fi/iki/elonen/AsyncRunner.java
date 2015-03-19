/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.iki.elonen;

/**
 * Pluggable strategy for asynchronously executing requests.
 */
public interface AsyncRunner {

  void exec(Runnable code);
  
}
