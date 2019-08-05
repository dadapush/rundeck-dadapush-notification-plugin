package com.dadapush.integration.rundeck;

import com.dadapush.client.ApiClient;
import com.dadapush.client.ApiException;
import com.dadapush.client.Configuration;
import com.dadapush.client.api.DaDaPushMessageApi;
import com.dadapush.client.model.MessagePushRequest;
import com.dadapush.client.model.ResultOfMessagePushResponse;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.plugins.descriptions.Password;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
@Plugin(service = "Notification", name = "DaDaPushNotification")
@PluginDescription(title = "DaDaPush Notification", description = "Sends Rundeck Notifications to DaDaPush channel")
public class DaDaPushNotificationPlugin implements NotificationPlugin {

  @PluginProperty(title = "BasePath",
      description = "DaDaPush Base Path",
      defaultValue = "https://www.dadapush.com",
      scope = PropertyScope.Instance)
  private String base_path;

  @Password
  @PluginProperty(title = "Channel Token",
      description = "Channel Token, like ctXXXXXXXXXXXXXXXXXXXXXXXX",
      scope = PropertyScope.Instance)
  private String channel_token;

  /**
   * Sends a notification to a DaDaPush channel when a job notification event is raised by Rundeck.
   *
   * @param trigger name of job notification event causing notification
   * @param executionData job execution data
   * @param config plugin configuration
   * @return true, if the DaDaPush API response indicates a message was successfully delivered to a
   * channel
   * @throws DaDaPushNotificationPluginException when any error occurs sending the Slack message
   */
  public boolean postNotification(String trigger, Map executionData, Map config) {
    if (this.base_path.isEmpty() || this.channel_token.isEmpty()) {
      throw new IllegalArgumentException("Base Path or Channel Token not set");
    }
    ApiClient apiClient = Configuration.getDefaultApiClient();
    apiClient.setBasePath(base_path);
    DaDaPushMessageApi apiInstance = new DaDaPushMessageApi(apiClient);
    MessagePushRequest body = new MessagePushRequest();
    body.setTitle(generateTitle(trigger, executionData, config));
    body.setContent(generateContent(trigger, executionData, config));
    body.setNeedPush(true);
    try {
      ResultOfMessagePushResponse result = apiInstance.createMessage(body, channel_token);
      return true;
    } catch (ApiException e) {
      throw new DaDaPushNotificationPluginException("send notification fail via DaDaPush", e);
    }
  }

  private String getTrigger(String trigger) {
    switch (trigger) {
      case "start":
        return "Started";
      case "failure":
        return "Failed";
      case "avgduration":
        return "Average exceeded";
      case "retryablefailure":
        return "Retry Failure";
      default:
        return "Succeeded";
    }
  }

  private String getJobName(Map executionData) {
    if (executionData.containsKey("job")) {
      Map jobMap = (Map) executionData.get("job");
      if (jobMap.containsKey("group")) {
        String group = (String) jobMap.get("group");
        group=StringUtils.trimToEmpty(group);
        if (StringUtils.isNotEmpty(group)) {
          return group + " / " + jobMap.get("name");
        } else {
          return (String) jobMap.get("name");
        }
      } else {
        return (String) jobMap.get("name");
      }
    }
    return null;
  }

  private String generateTitle(String trigger, Map executionData, Map config) {
    StringBuilder stringBuilder = new StringBuilder();
    String jobName = getJobName(executionData);
    stringBuilder.append("[").append(getTrigger(trigger)).append("]").append(jobName);
    String string = stringBuilder.toString();
    if (string.length() <= 50) {
      return string;
    } else {
      return string.substring(0, 50);
    }
  }

  private String generateContent(String trigger, Map executionData, Map config) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("Job Name: ").append(getJobName(executionData)).append("\n");
    String project = (String) executionData.get("project");
    String description = (String) executionData.get("description");
    String argstring = (String) executionData.get("argstring");
    String user = (String) executionData.get("user");

    stringBuilder.append("Project: ")
        .append(StringUtils.trimToNull(project) != null ? project : "N/A").append("\n");
    stringBuilder.append("Summary: ")
        .append(StringUtils.trimToNull(description) != null ? description : "N/A").append("\n");
    stringBuilder.append("Status: ").append(executionData.get("status")).append("\n");
    stringBuilder.append("Execution ID: ").append(executionData.get("id")).append("\n");
    stringBuilder.append("Options: ")
        .append(StringUtils.trimToNull(argstring) != null ? argstring : "N/A").append("\n");
    stringBuilder.append("Started By: ").append(StringUtils.trimToNull(user) != null ? user : "N/A")
        .append("\n");
    if ("failure".equals(trigger)) {
      String failedNodeListString = (String) executionData.get("failedNodeListString");
      if (StringUtils.isNotEmpty(failedNodeListString)) {
        stringBuilder
            .append("Failed Nodes: ").append(failedNodeListString)
            .append("\n");
      } else {
        stringBuilder
            .append("Failed Nodes: ").append("- (Job itself failed)").append("\n");
      }
    }
    String string = stringBuilder.toString();
    if (string.length() <= 500) {
      return string;
    } else {
      return string.substring(0, 500);
    }
  }
}
