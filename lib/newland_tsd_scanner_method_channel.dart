import 'dart:core';
import 'package:flutter/services.dart';
import 'broadcasts/src/flutter_broadcasts.dart';
import 'newland_tsd_scanner.dart';
import 'newland_tsd_scanner_platform_interface.dart';

class MethodChannelNewlandTsdScanner implements NewlandTsdScannerPlatform {
  final methodChannel = const MethodChannel('newland_tsd_scanner');
  BroadcastReceiver? receiver;

  @override
  Future<void> startListerning({Function(TsdBarcode code)? onDetect}) async {
    methodChannel.invokeMethod<bool>('init_scanner');
    receiver = BroadcastReceiver(
      names: <String>["nlscan.action.SCANNER_RESULT"],
    );
    receiver!.messages.listen((BroadcastMessage? m) {
      if (m?.data != null) {
        onDetect?.call(TsdBarcode.fromJson(m!.data!));
      }
    });
    receiver!.start();
  }

  @override
  Future<void> stopListerning() async {
    await receiver?.stop();
  }
}
