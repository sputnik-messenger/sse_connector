import 'dart:async';

import 'package:flutter/services.dart';
import 'package:sse_connector/sse_connector_args.dart';


class SseConnector {
  static const MethodChannel _channel =
      const MethodChannel('com.sputnik-messenger.sse_connector');

  static Future<String> initConnection(SseConnectorArgs args) async {

    final map = {
      'wakeLockTag': args.wakeLockTag,
      'pushKey': args.pushKey,
      'sseNotificationsUrl': args.sseNotificationsUrl,
      'pollNotificationUrl': args.pollNotificationUrl,
      'notificationChannelId': args.notificationChannelId,
      'notificationChannelName': args.notificationChannelName,
      'notificationChannelDescription': args.notificationChannelDescription,
      'notificationChannelImportance': args.notificationChannelImportanceValue,
      'notificationSmallIcon': args.notificationSmallIcon,
    };
    final String version =
        await _channel.invokeMethod('initSseConnection', map);
    return version;
  }

  static Future<void> stopConnection() async {
    await _channel.invokeMethod('stopMoonPushConnection');
  }

  static Future<void> openBatterySettings() async {
    await _channel.invokeMethod('openBatterySettings');
  }

  static Future<bool> isBatteryOptimizationEnabled() async {
    return await _channel.invokeMethod('isBatteryOptimizationEnabled');
  }
}
