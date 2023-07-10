import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'newland_tsd_scanner_method_channel.dart';

abstract class NewlandTsdScannerPlatform extends PlatformInterface {
  NewlandTsdScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static NewlandTsdScannerPlatform _instance = MethodChannelNewlandTsdScanner();

  static NewlandTsdScannerPlatform get instance => _instance;

  static set instance(NewlandTsdScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> startScan() {
    throw UnimplementedError('startScan() has not been implemented.');
  }

  Future<String?> stopScan() {
    throw UnimplementedError('stopScan() has not been implemented.');
  }
}
