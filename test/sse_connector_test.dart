import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sse_connector/sse_connector.dart';

void main() {
  const MethodChannel channel = MethodChannel('sse_connector');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });


}
