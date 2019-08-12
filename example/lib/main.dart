import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:sse_connector/sse_connector.dart';
import 'package:sse_connector/sse_connector_args.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {

      await SseConnector.initConnection(SseConnectorArgs(
        pushKey: "matrix pusher device pushKey",
        notificationChannelDescription: "channel description",
        notificationChannelId: "cannel id",
        notificationChannelImportance:
            NotificationChannelImportance.IMPORTANCE_HIGH,
        notificationChannelName: "channel name",
        sseNotificationsUrl: "https://url to event source",
        pollNotificationUrl: "https://url to pull notifications",
        wakeLockTag: "wake lock tag",
        //todo: setting icon is not working
        notificationSmallIcon: "images/small_icon.png",
      ));
    } on PlatformException {
      debugPrint('Failed to get init sse connection.');
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running'),
        ),
      ),
    );
  }
}
