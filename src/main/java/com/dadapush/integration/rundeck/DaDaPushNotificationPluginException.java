package com.dadapush.integration.rundeck;

public class DaDaPushNotificationPluginException extends RuntimeException {

  /**
   * Constructor.
   *
   * @param message error message
   */
  public DaDaPushNotificationPluginException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message error message
   * @param cause exception cause
   */
  public DaDaPushNotificationPluginException(String message, Throwable cause) {
    super(message, cause);
  }

}
