import 'package:flutter/services.dart';

import 'newland_tsd_scanner_platform_interface.dart';

class MethodChannelNewlandTsdScanner implements NewlandTsdScannerPlatform {
  final methodChannel = const MethodChannel('newland_tsd_scanner');

  @override
  Future<String?> startScan() async {
    final String? result =
        await methodChannel.invokeMethod<String>('start_scanner');
    return result;
  }

  @override
  Future<String?> stopScan() async {
    final String? result =
        await methodChannel.invokeMethod<String>('stop_scanner');
    return result;
  }
}
