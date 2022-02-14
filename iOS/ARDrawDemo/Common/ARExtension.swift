//
//  ARExtension.swift
//  ARDrawDemo
//
//  Created by zjq on 2022/1/14.
//

import Foundation
import AVFoundation
import UIKit

let ARScreenHeight = UIScreen.main.bounds.size.height
let ARScreenWidth = UIScreen.main.bounds.size.width

let PingFang = "PingFang SC"
let PingFangBold = "PingFangSC-Semibold"

// att
let ARKeyChannel = "ARKeyChannel"

extension NSObject {
    func stringAllIsEmpty(string :String) -> Bool {
        let trimmedStr =  string.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmedStr.isEmpty
    }
    // 是否是刘海屏
    func isFullScreen() -> Bool {
        if #available(iOS 11, *) {
              guard let w = UIApplication.shared.delegate?.window, let unwrapedWindow = w else {
                  return false
              }
              if unwrapedWindow.safeAreaInsets.left > 0 || unwrapedWindow.safeAreaInsets.bottom > 0 {
                  print(unwrapedWindow.safeAreaInsets)
                  return true
              }
        }
        return false
    }
    // 本地数据
    func getUid() -> String {
        let userID = UserDefaults.standard.string(forKey: "AR_UserId")
        if  userID != nil {
            return userID ?? ""
        }else{
            let uuid = randomString(length: 6)
            UserDefaults.standard.set(uuid, forKey: "AR_UserId")
            return uuid
        }
    }
    // 随机字符串
    func randomString(length: Int) -> String {
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789
        let letters : NSString = "0123456789"
        let len = UInt32(letters.length)
     
        var randomString = ""
     
        for _ in 0 ..< length {
            let rand = arc4random_uniform(len)
            var nextChar = letters.character(at: Int(rand))
            randomString += NSString(characters: &nextChar,length: 1) as String
        }
        return randomString
    }

    
}

extension UIColor {
    convenience init(hexString:String) {
        // 处理数值
        var cString = hexString.uppercased().trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
        let length = (cString as NSString).length
        // 错误处理
        if length < 6 || length > 7 || (!cString.hasPrefix("#") && length == 7 ){
            self.init(red: 0.0, green: 0.0, blue: 0.0, alpha: 1.0)
            return
        }
        // 截取#后面的字符
        if cString.hasPrefix("#") {
            cString = (cString as NSString).substring(from: 1)
        }
        var range = NSRange()
        range.location = 0
        range.length = 2
        let rString = (cString as NSString).substring(with: range)
        
        range.location = 2
        let gString = (cString as NSString).substring(with: range)
        
        range.location = 4
        let bString = (cString as NSString).substring(with: range)
        
        // 存储转换后的数值
        var r: UInt32 = 0,g: UInt32 = 0,b: UInt32 = 0
        // 进行转换
        Scanner(string: rString).scanHexInt32(&r)
        Scanner(string: gString).scanHexInt32(&g)
        Scanner(string: bString).scanHexInt32(&b)
        
        self.init(red: CGFloat(r)/255.0, green: CGFloat(g)/255.0, blue: CGFloat(b)/255.0, alpha: 1.0)
    }
    // 获取颜色的rgba
    func getRBGA() -> (r: CGFloat, g: CGFloat, b: CGFloat, a: CGFloat) {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        getRed(&r, green: &g, blue: &b, alpha: &a)
        return (r, g, b, a)
    }
    // 随机颜色
    func randomColor() -> UIColor {
        let red = CGFloat(arc4random() % 256)/255.0
        let green = CGFloat(arc4random() % 256)/255.0
        let blue = CGFloat(arc4random() % 256)/255.0
        return UIColor(red: red, green: green, blue: blue, alpha: 1.0)
    }
}

// MARK: - 类扩展
class UnderLineTextField: UITextField {
    
    override func draw(_ rect: CGRect) {
           //线条的高度
           let lineHeight : CGFloat = 0.5
           //线条的颜色
           let lineColor = UIColor.gray
           
           guard let content = UIGraphicsGetCurrentContext() else { return }
           content.setFillColor(lineColor.cgColor)
           content.fill(CGRect.init(x: 0, y: self.frame.height - lineHeight, width: self.frame.width, height: lineHeight))
           UIGraphicsBeginImageContextWithOptions(rect.size, true, 0)
       }
}
class TopLineView: UIView {
    
    override func draw(_ rect: CGRect) {
        if (self.frame.width == 0 || self.frame.height == 0) {
            return
        }
        //线条的高度
        let lineHeight : CGFloat = 0.5
        //线条的颜色
        let lineColor = UIColor.gray
        
        guard let content = UIGraphicsGetCurrentContext() else { return }
        content.setFillColor(lineColor.cgColor)
        content.fill(CGRect.init(x: 0, y: lineHeight, width: self.frame.width, height: lineHeight))
        UIGraphicsBeginImageContextWithOptions(rect.size, true, 0)
       }
}

class RightLineButton: UIButton {
   
    var lineWidth : CGFloat? {
        didSet {
            self.draw(self.frame)
        }
    }
    
    override func draw(_ rect: CGRect) {
        //线条的高度
//        let lineWidth : CGFloat = 0.5
        
        if (self.frame.width == 0 || self.frame.height == 0) {
            return
        }
        var lineHeight : CGFloat = 0
        if self.frame.height > 0 {
            lineHeight = self.frame.height * (2.0/3.0)
        }
        
        //线条的颜色
        let lineColor = UIColor.gray
        
        guard let content = UIGraphicsGetCurrentContext() else { return }
        content.setFillColor(lineColor.cgColor)
        let  rect:CGRect = CGRect.init(x: self.frame.width - lineWidth! , y: (self.frame.height - lineHeight)/2, width: lineWidth!, height: lineHeight)

        content.fill(rect)
        UIGraphicsBeginImageContextWithOptions(rect.size, true, 0)
    }
}
