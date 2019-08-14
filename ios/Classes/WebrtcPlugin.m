#import "WebrtcPlugin.h"
#import <webrtc_plugin/webrtc_plugin-Swift.h>

@implementation WebrtcPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftWebrtcPlugin registerWithRegistrar:registrar];
}
@end
