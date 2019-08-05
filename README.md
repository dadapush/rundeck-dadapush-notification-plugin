# rundeck-dadapush-notification-plugin

Sends Rundeck notification messages to a DaDaPush channel. This plugin is based on [slack-incoming-webhook-plugin](https://github.com/rundeck-plugins/slack-incoming-webhook-plugin).

## Download jarfile

1. Download jarfile from [releases](https://github.com/dadapush/rundeck-dadapush-notification-plugin/releases).
2. copy jarfile to `$RDECK_BASE/libext`

## Build

1. build the source by gradle.
2. copy jarfile to `$RDECK_BASE/libext`


## Configuration

### Project Configuration
```
project.plugin.Notification.DaDaPushNotification.base_path=https://www.dadapush.com
project.plugin.Notification.DaDaPushNotification.channel_token=ctXXXXXXXXXXXXXXXXXX
```
