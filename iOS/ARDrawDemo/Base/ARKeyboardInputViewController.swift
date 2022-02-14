//
//  ARKeyboardInputViewController.swift
//  ARDrawDemo
//
//  Created by zjq on 2022/1/17.
//

import UIKit

class ARKeyboardInputViewController: UIViewController {
    // 遮挡View
    var keepOutView: UIView!
    var keyBoardView: UIView!
    var chatTextField: UITextField!
    var sendButton: UIButton!
    let keyBoardViewHeight:CGFloat = 49
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardChange(notify:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardChange(notify:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    func getKeyBoardView() -> UIView {
        keepOutView = UIView()
        keepOutView.backgroundColor = UIColor.black
        keepOutView.alpha = 0.3
        self.view.addSubview(keepOutView)
        keepOutView.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0))
        }
        let tap :UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(tapView))
        keepOutView.addGestureRecognizer(tap)
        keepOutView.isHidden = true
        
        keyBoardView = UIView.init(frame: CGRect(x: 0, y: ARScreenHeight, width: ARScreenWidth, height: keyBoardViewHeight))
        keyBoardView.backgroundColor = UIColor.init(red: 245.0/255, green: 245.0/255, blue: 245.0/255, alpha: 1.0)
        self.view.addSubview(keyBoardView)
        
        chatTextField = UITextField.init(frame: CGRect(origin: CGPoint(x: 10, y: 5), size: CGSize(width: ARScreenWidth - 110, height: keyBoardViewHeight - 10)))
        chatTextField.font = UIFont.systemFont(ofSize: 14)
        chatTextField.layer.masksToBounds = true
        chatTextField.layer.cornerRadius = 5
        chatTextField.returnKeyType = .send
        chatTextField.placeholder = "请输入聊天消息"
        chatTextField.backgroundColor = UIColor.white
        chatTextField.delegate = self
        chatTextField.addTarget(self, action: #selector(chatTextFieldLimit), for: .editingChanged)
        keyBoardView.addSubview(chatTextField)
        
        sendButton = UIButton.init(type: .custom)
        sendButton.frame = CGRect(x: ARScreenWidth - 92, y: 5, width: 79, height: keyBoardViewHeight - 10)
        sendButton.setTitleColor(UIColor.white, for: .normal)
        sendButton.titleLabel?.font = UIFont.init(name: PingFangBold, size: 12)
        sendButton.setTitle("发送", for: .normal)
        sendButton.backgroundColor = UIColor(hexString: "#59BE6C")
        sendButton.layer.masksToBounds = true
        sendButton.layer.cornerRadius = 5
        sendButton.addTarget(self, action: #selector(sendMessageButtonEvent), for: .touchUpInside)
        sendButton.alpha = 0.3
        keyBoardView.addSubview(sendButton)
        
        return keyBoardView
    }
    
    @objc func chatTextFieldLimit() -> Void {
        if chatTextField.text?.count ?? 0 > 512 {
            chatTextField.text = String((chatTextField.text?.prefix(512))!)
        }
        if chatTextField.text!.isEmpty || stringAllIsEmpty(string: chatTextField.text!) {
            sendButton.alpha = 0.3
        }else{
            sendButton.alpha = 1.0
        }
    }
    @objc func sendMessageButtonEvent() -> Void {
        if sendButton.alpha != 0.3 {
            sendMessage()
        }
    }
    @objc func sendMessage()->Void {
    
    }
    @objc func keyboardChange(notify:NSNotification) ->Void {
        if chatTextField.isFirstResponder {
            // 时间
            let duration: Double = notify.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as! Double
            if notify.name == UIResponder.keyboardWillShowNotification {
               
                // 计算键盘高度
                let keyBoardY:CGFloat = (notify.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as! NSValue).cgRectValue.size.height
                
                let high = ARScreenHeight - keyBoardY - keyBoardViewHeight
                self.keepOutView.isHidden = false
                self.keepOutView.alpha = 0.0
                UIView.animate(withDuration: duration) {
                    self.keyBoardView.frame = CGRect(x: 0, y: high, width: ARScreenWidth, height: self.keyBoardViewHeight)
                    self.keepOutView.alpha = 0.3
                    self.view.layoutIfNeeded()
                }
            }else if notify.name == UIResponder.keyboardWillHideNotification {
                
                UIView.animate(withDuration: duration) {
                    self.keyBoardView.frame = CGRect(x: 0, y: ARScreenHeight, width: ARScreenWidth, height: self.keyBoardViewHeight)
                    self.keepOutView.alpha = 0.0
                    self.view.layoutIfNeeded()
                }completion: { _ in
                    self.keepOutView.isHidden = true
                }
            }
        }
    }
    
//    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
//        chatTextField.endEditing(true)
//    }
    @objc func tapView() -> Void {
        chatTextField.endEditing(true)
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}

extension ARKeyboardInputViewController:UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
       sendMessage()
        return true
    }
}
