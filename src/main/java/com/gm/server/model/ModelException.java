package com.gm.server.model;

public class ModelException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1750720562438996729L;

  public ModelException(String message, Throwable t) {
    super(message, t);
  }
  
  public ModelException(String message) {
    super(message);
  }
  
  public ModelException(Throwable t) {
    super(t);
  }
}
