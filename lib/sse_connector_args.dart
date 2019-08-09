enum NotificationChannelImportance {
  IMPORTANCE_HIGH, //4
  IMPORTANCE_DEFAULT, //3
  IMPORTANCE_LOW, // 2
  IMPORTANCE_MIN, // 1
}

class SseConnectorArgs {
  final String wakeLockTag;
  final String pushKey;
  final String sseNotificationsUrl;
  final String pollNotificationUrl;
  final String notificationChannelId;
  final String notificationChannelName;
  final String notificationChannelDescription;
  final NotificationChannelImportance notificationChannelImportance;
  final String notificationSmallIcon;

  SseConnectorArgs(
      {this.wakeLockTag,
      this.pushKey,
      this.sseNotificationsUrl,
      this.pollNotificationUrl,
      this.notificationChannelId,
      this.notificationChannelName,
      this.notificationChannelDescription,
      this.notificationChannelImportance,
      this.notificationSmallIcon})
      : assert(wakeLockTag != null),
        assert(pushKey != null),
        assert(sseNotificationsUrl != null),
        assert(pollNotificationUrl != null),
        assert(notificationChannelId != null),
        assert(notificationChannelName != null),
        assert(notificationChannelDescription != null),
        assert(notificationChannelImportance != null),
        assert(notificationSmallIcon != null);

  int get notificationChannelImportanceValue {
    int value = 0;
    switch (notificationChannelImportance) {
      case NotificationChannelImportance.IMPORTANCE_HIGH:
        value = 4;
        break;
      case NotificationChannelImportance.IMPORTANCE_DEFAULT:
        value = 3;
        break;
      case NotificationChannelImportance.IMPORTANCE_LOW:
        value = 2;
        break;
      case NotificationChannelImportance.IMPORTANCE_MIN:
        value = 1;
        break;
    }
    return value;
  }
}
