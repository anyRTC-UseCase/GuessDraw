//
//  ViewController.swift
//  ARDrawDemo
//
//  Created by zjq on 2022/1/14.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var enterButton: UIButton!
    @IBOutlet weak var roomTextField: UnderLineTextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        roomTextField.addTarget(self, action: #selector(textFieldValueChange), for: .editingChanged)
        roomTextField.keyboardType = .numberPad
        
        enterButton.layer.masksToBounds = true
        enterButton.layer.cornerRadius = 5
    }

    @IBAction func joinRoom(_ sender: Any) {
        if roomTextField.text?.count != 0 {
            let sb = UIStoryboard(name: "Main", bundle:nil)
            let vc = sb.instantiateViewController(withIdentifier: "ARBoardViewController") as! ARBoardViewController
            vc.roomId = roomTextField.text
            vc.modalPresentationStyle = .fullScreen
            self.present(vc, animated: true, completion: nil)
        
        }
    }
    
    @objc func textFieldValueChange() {
        let roomId:String = roomTextField.text!
        if roomId.count > 16 {
            roomTextField.text = String(roomId.prefix(16))
        }

        enterButton.backgroundColor = UIColor(hexString: roomId.count==0 ? "#CEECD4" : "#59BE6C" )
    }
}
