//
//  AppDelegate.swift
//  ARDrawDemo
//
//  Created by zjq on 2022/1/14.
//

import UIKit
import SVProgressHUD
import Bugly

@main

class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        SVProgressHUD.setDefaultStyle(.light)
        SVProgressHUD.setDefaultMaskType(.black)
        SVProgressHUD.setShouldTintImages(false)
        SVProgressHUD.setMinimumSize(CGSize.init(width: 120, height: 120))
        // Bugly
        Bugly.start(withAppId: "96cc948712")
        
        return true
    }
    
    func application(_ application: UIApplication, shouldAllowExtensionPointIdentifier extensionPointIdentifier: UIApplication.ExtensionPointIdentifier) -> Bool {
        return (extensionPointIdentifier.rawValue == "com.apple.keyboard-service") ? false : true
    }
    

}

