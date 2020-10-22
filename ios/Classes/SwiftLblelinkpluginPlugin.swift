import Flutter
import UIKit

public class SwiftLblelinkpluginPlugin: NSObject, FlutterPlugin {
    
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "lblelinkplugin", binaryMessenger: registrar.messenger())
    let instance = SwiftLblelinkpluginPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    
    LMLBEventChannelSupport.register(with: registrar);
    
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    
    let dict = call.arguments as? [String:Any];
    
    switch call.method {
        case "initLBSdk":
            let argument = dict as! [String:String];
            LMLBSDKManager.shareInstance.initLBSDK(appid: argument["appid"] ?? "", secretKey: argument["secretKey"] ?? "",result: result);
        break
        case "beginSearchEquipment":
            LMLBSDKManager.shareInstance.beginSearchEquipment()
        break
        case "connectToService":
            let argument = dict as! [String:String];
            LMLBSDKManager.shareInstance.linkToService(ipAddress: argument["ipAddress"] ?? "");
        break
        case "disConnect":
            LMLBSDKManager.shareInstance.disConnect();
        break
        case "pause":
            LBPlayerManager.shareInstance.pause();
        break
        case "resumePlay":
            LBPlayerManager.shareInstance.resumePlay();
        break
        case "stop":
            LBPlayerManager.shareInstance.stop();
        break
        case "play":
            
            var url: String = "";
            var start: Int = 0;
            var header: Dictionary<AnyHashable,Any>?
            
            if let playUrlString = dict?["playUrlString"]{
                url = playUrlString as! String;
            }
           
            if let startPosition = dict?["startPosition"]{
                start = startPosition as! Int;
            }
            
            if let h = dict?["headerInfo"]{
                header = h as? Dictionary<AnyHashable,Any>;
            }

            LBPlayerManager.shareInstance.beginPlay(connection: LMLBSDKManager.shareInstance.linkConnection, playUrl: url,startPosition: start,headerInfo: header);

        break
        case "getLastConnectService":
            LMLBSDKManager.shareInstance.getLastConnectService(result: result)
        break
        case "seek2Position":
            let argument = dict as! [String:Int];
            if let a = argument["position"]{
                LBPlayerManager.shareInstance.seekTo(position: a);
            }
        break
    default:
        result(FlutterMethodNotImplemented)
        break;
    }
    }
//    result("iOS " + UIDevice.current.systemVersion)
  
}
