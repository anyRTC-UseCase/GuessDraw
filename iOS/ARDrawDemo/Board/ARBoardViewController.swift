//
//  ARBoardViewController.swift
//  ARDrawDemo
//
//  Created by zjq on 2022/1/17.
//

import UIKit
import SnapKit
import ARtcKit
import Popover
import ARBoard
import SVProgressHUD

class ARBoardViewController: ARKeyboardInputViewController {
    
    var roomId: String!
    // 在线人数
    var onlineLabel: UILabel!
    // 当前谁来作画
    var drawingLabel: UILabel!
    // 画板
    var boardView: UIView!
    // 画板操作工具栏
    var drawToolView: UIView!
    // 底部工具栏
    var bottomView :TopLineView!
    // 结束画画
    var closeDrawButton :UIButton!
    // 开始画画
    var drawButton :UIButton!
    // 聊天窗口
    var chatViewController: ARChatViewController!
    // 视频库
    var rtcKit: ARtcEngineKit!
    // 画板库
    var boradKit: ARBoardKit!
    // 画笔出席
    fileprivate var panLine: Int = 2
    fileprivate var panColor: UIColor = UIColor.red
   
    deinit {
        ARtcEngineKit.destroy()
        boradKit.destory()
        print("deinit boardViewController \n")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        initRTC()
        initTopView()
        initBoardView()
        initBottomView()
        initChatView()
        // 添加输入框view
        self.view.addSubview(getKeyBoardView())
    }
    func initRTC() -> Void {
        rtcKit = ARtcEngineKit.sharedEngine(withAppId: AppID, delegate: self)
        rtcKit.setChannelProfile(.liveBroadcasting)
        rtcKit.setClientRole(.broadcaster)
        rtcKit.setAudioProfile(.musicStandard, scenario: .gameStreaming)
        rtcKit.joinChannel(byToken: nil, channelId: self.roomId, uid: getUid()) { channel, uid, elapsed in
            //登录成功
        }
    }
    
    func initTopView() -> Void {
        
        self.view.backgroundColor = UIColor(hexString: "#EDEDED")
        
        let topY:CGFloat = isFullScreen() ? 59 : 35
        // 房间号
        let roomLabel: UILabel = UILabel()
        roomLabel.font = UIFont.boldSystemFont(ofSize: 18)
        roomLabel.textColor = UIColor(hexString: "#181818")
        roomLabel.text = roomId
        self.view.addSubview(roomLabel)
        roomLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(topY)
            make.left.equalToSuperview().offset(20)
            make.width.equalTo(ARScreenWidth/2)
        }
        
        // 关闭按钮
        let closeButton: UIButton = UIButton.init(type:.custom)
        closeButton.setImage(UIImage.init(named: "closeButton"), for: .normal)
        closeButton.addTarget(self, action: #selector(closeButtonEvent), for: .touchUpInside)
        closeButton.layer.masksToBounds = true
        closeButton.layer.cornerRadius = 22
        closeButton.backgroundColor = UIColor(hexString: "#E0E0E0")
        self.view.addSubview(closeButton)
        closeButton.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-20)
            make.top.equalToSuperview().offset(topY - 10 )
            make.width.height.equalTo(44)
        }
        // 人数
        onlineLabel = UILabel()
        onlineLabel.font = UIFont.systemFont(ofSize: 14)
        onlineLabel.textColor = UIColor(hexString: "#B0B0B0")
        self.view.addSubview(onlineLabel)
        onlineLabel.text = "在线：1"
        onlineLabel.snp.makeConstraints { make in
            make.top.equalTo(roomLabel.snp.bottom).offset(25)
            make.left.equalToSuperview().offset(20)
            make.width.equalTo(ARScreenWidth/3)
        }
        // 当前作画的人
        drawingLabel = UILabel()
        drawingLabel.font = UIFont.boldSystemFont(ofSize: 14)
        drawingLabel.textColor = UIColor(hexString: "#59BE6C")
        drawingLabel.textAlignment = .right
        self.view.addSubview(drawingLabel)
        drawingLabel.text = "发多开始作画"
        drawingLabel.snp.makeConstraints { make in
            make.centerY.equalTo(onlineLabel.snp.centerY)
            make.right.equalToSuperview().offset(-20)
            make.width.equalTo(ARScreenWidth/3)
        }
    }
    
    func initBoardView() -> Void {
        let authParam = ARBoardAuthParam()
        authParam.appId = AppID
        authParam.uid = getUid()
    
        let baseParam = ARBoardBaseParam()
        baseParam.authConfig.drawEnable = false
        baseParam.config.ratio = "1:1"
        
        baseParam.styleConfig.brushColor = UIColor.red
        baseParam.styleConfig.brushThin = 2
        
        boradKit = ARBoardKit(authParam: authParam, roomId: self.roomId, boardParam: baseParam, delegate: self)
        // 画板View
        boardView = boradKit.getBoardRenderView()
        boardView.layer.masksToBounds = true
        boardView.layer.cornerRadius = 5
        boardView.backgroundColor = UIColor.white
        self.view.addSubview(boardView)
        boardView.snp.makeConstraints { make in
            make.top.equalTo(onlineLabel.snp.bottom).offset(15)
            make.left.equalToSuperview().offset(20)
            make.right.equalToSuperview().offset(-20)
            make.height.equalTo(boardView.snp.width).multipliedBy(1.0)
        }
        drawButton = UIButton(type: .custom)
        drawButton.layer.masksToBounds = true
        drawButton.layer.cornerRadius = 5
        drawButton.backgroundColor = UIColor(hexString: "#59BE6C")
        drawButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 12)
        drawButton.setTitle("开始画", for: .normal)
        drawButton.addTarget(self, action: #selector(startDrawEvent), for: .touchUpInside)
        boardView.addSubview(drawButton)
        drawButton.snp.makeConstraints { make in
            make.width.equalTo(80)
            make.height.equalTo(40)
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview().multipliedBy(0.85)
        }
        
        // 工具栏
        drawToolView = UIView()
        drawToolView.layer.masksToBounds = true
        drawToolView.layer.cornerRadius = 5
        drawToolView.backgroundColor = UIColor.white
        drawToolView.alpha = 0.7
        boardView.addSubview(drawToolView)
        drawToolView.snp.makeConstraints { make in
            make.left.equalToSuperview().offset(10)
            make.bottom.equalToSuperview().offset(-10)
            make.width.equalTo(200)
            make.height.equalTo(40)
        }
        drawToolView.isHidden = true
        //
        let stackView: UIStackView = UIStackView()
        stackView.axis = .horizontal
        stackView.distribution = .fillEqually
        stackView.spacing = 0
        stackView.alignment = .fill
        drawToolView.addSubview(stackView)
        stackView.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0))
        }
        
        for i in 0..<4 {
            let button:RightLineButton = RightLineButton()
            button.tag = i
            button.addTarget(self, action: #selector(toolButtonEvent(sender:)), for: .touchUpInside)
            switch i {
            case 0:
                button.setImage(UIImage.init(named: "panNomal"), for: .normal)
                button.setImage(UIImage.init(named: "panNomalSelected"), for: .selected)
                button.lineWidth = 0.5
            case 1:
                button.setImage(UIImage.init(named: "drawColorNomal"), for: .normal)
                button.setImage(UIImage.init(named: "drawColorSelected"), for: .selected)
                button.lineWidth = 0.5
            case 2:
                button.setImage(UIImage.init(named: "under"), for: .normal)
                button.lineWidth = 0.5
                button.isHighlighted = true
                
            case 3:
                button.setImage(UIImage.init(named: "clearDraw"), for: .normal)
                button.lineWidth = 0
                button.isHighlighted = true
            default:
                break
            }
            stackView.addArrangedSubview(button)
            stackView.layoutIfNeeded()
        }
        
        // 截图
        let snapButton: UIButton = UIButton.init(type:.custom)
        snapButton.setImage(UIImage.init(named: "snapPic"), for: .normal)
        snapButton.addTarget(self, action: #selector(snapButtonEvent), for: .touchUpInside)
        snapButton.layer.masksToBounds = true
        snapButton.layer.cornerRadius = 5
        snapButton.backgroundColor = UIColor(hexString: "#E0E0E0")
        boardView.addSubview(snapButton)
        snapButton.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-10)
            make.bottom.equalToSuperview().offset(-10 )
            make.width.height.equalTo(40)
        }
    }
    // 底部工具栏
    func initBottomView() -> Void {
        bottomView = TopLineView()
        bottomView.backgroundColor = UIColor.white
        self.view.addSubview(bottomView)
        bottomView.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview().offset(0)
            make.height.equalTo(60)
        };
        let stackView: UIStackView = UIStackView()
        stackView.axis = .horizontal
        stackView.distribution = .fillEqually
        stackView.spacing = 0
        stackView.alignment = .fill
        bottomView.addSubview(stackView)
        stackView.snp.makeConstraints { make in
            make.top.left.equalToSuperview().offset(10)
            make.bottom.equalToSuperview().offset(-10)
            make.width.equalTo(110)
        }
        for i in 0..<2 {
            let button:RightLineButton = RightLineButton()
            button.tag = i
            button.addTarget(self, action: #selector(bottomButtonEvent(sender:)), for: .touchUpInside)
            switch i {
                case 0:
                    button.setImage(UIImage.init(named: "micopen"), for: .normal)
                    button.setImage(UIImage.init(named: "micclose"), for: .selected)
                    button.lineWidth = 0.5
                case 1:
                    button.setImage(UIImage.init(named: "chat"), for: .normal)
                    button.lineWidth = 0
                default:
                    break
            }
            stackView.addArrangedSubview(button)
            stackView.layoutIfNeeded()
        }
        // 关闭
        closeDrawButton = UIButton(type: .custom)
        closeDrawButton.setImage(UIImage.init(named: "closeDraw"), for: .normal)
        closeDrawButton.addTarget(self, action: #selector(closeDrawButtonEvent), for: .touchUpInside)
        bottomView.addSubview(closeDrawButton)
        closeDrawButton.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(10)
            make.bottom.equalToSuperview().offset(-10)
            make.right.equalToSuperview().offset(-10)
            make.width.equalTo(125)
        }
        closeDrawButton.isHidden = true
        
    }
    
    // 聊天窗口
    func initChatView() -> Void {
        chatViewController = UIStoryboard(name: "Chat", bundle: nil)
            .instantiateViewController(withIdentifier: "Chat") as? ARChatViewController
        chatViewController.roomId = self.roomId
        chatViewController.boardVc = self
        let tableView = chatViewController.tableView!
        self.view.addSubview(tableView)
        tableView.snp.makeConstraints({ make in
            make.left.equalToSuperview().offset(20)
            make.right.equalToSuperview().offset(-20)
            make.top.equalTo(boardView.snp.bottom).offset(20)
            make.bottom.equalTo(bottomView.snp.top).offset(-20)
            
        })
    }
    
// MARK: - 方法
    
    // 关闭页面
    @objc func closeButtonEvent() -> Void {
        self.dismiss(animated: true) {
            self.rtcKit.leaveChannel(nil)
            self.chatViewController.boardVc = nil
            // 自己画离开先重置状态
            if !self.closeDrawButton.isHidden {
                self.chatViewController.stopDrawRequest()
            }
        }
    }
    @objc func toolButtonEvent(sender :UIButton) -> Void {
        let button: UIButton = sender
        switch button.tag {
            case 0:
                // 画笔
                var startPoint = self.view.convert(drawToolView.frame.origin, from: boardView)
                // 精准对其按钮
                startPoint.x = 55
                let aView = ARSelectedView(frame: CGRect(x: 40, y: 0, width: boardView.frame.width - 20, height: 44))
                aView.mode = .PanModel
                aView.line = self.panLine
                aView.panBlock = { (line: Int) -> Void in
                    // 画笔粗细
                    self.panLine = line
                    self.boradKit.setBrushThin(UInt32(line))
                }
                let options = [.type(.up),
                               .cornerRadius(4),
                               .animationIn(0.3),
                               .blackOverlayColor(UIColor.clear),
                               .arrowSize(CGSize(width: 8 , height: 8))] as [PopoverOption]
                
                let popover = Popover(options: options, showHandler: nil, dismissHandler: nil)
                popover.show(aView, point: startPoint)
            
               break
            case 1:
                // 颜色
                var startPoint = self.view.convert(drawToolView.frame.origin, from: boardView)
                // 精准对其按钮
                startPoint.x = 105
                let aView = ARSelectedView(frame: CGRect(x: 40, y: 0, width: boardView.frame.width - 20, height: 132))
                aView.mode = .ColorMode
                aView.color = self.panColor
                aView.colorBlock = { (color: UIColor) -> Void in
                    // 画笔粗细
                    self.panColor = color
                    self.boradKit.setBrush(color)
                }
                let options = [.type(.up),
                               .cornerRadius(4),
                               .animationIn(0.3),
                               .blackOverlayColor(UIColor.clear),
                               .arrowSize(CGSize(width: 8 , height: 8))] as [PopoverOption]
                
                let popover = Popover(options: options, showHandler: nil, dismissHandler: nil)
                popover.show(aView, point: startPoint)
                break
            case 2:
                // 撤销
                self.boradKit.undo();
                break
            case 3:
                // 删除
                self.boradKit.clear(true)
                break
            default:
                break
        }
      
    }
    
    @objc func snapButtonEvent() -> Void {
        // 快照截图
        self.boradKit.getBoardSnapshot { (image: UIImage) in
            UIImageWriteToSavedPhotosAlbum(image, self, #selector(self.saveImage(image:didFinishSavingWithError:contextInfo:)), nil)

        }
    }
    
    @objc func bottomButtonEvent(sender :UIButton) -> Void {
        let button: UIButton = sender
        switch button.tag {
            case 0:
                // 语音
                button.isSelected = !button.isSelected
                if button.isSelected {
                    // 关闭语音
                    self.rtcKit.muteLocalAudioStream(true)
                }else{
                    // 打开语音
                    self.rtcKit.muteLocalAudioStream(false)
                }
               break
            case 1:
                // 聊天
                chatTextField.becomeFirstResponder()
                break
            default:
                break
        }
    }
    // 结束画画
    @objc func closeDrawButtonEvent() -> Void {
        chatViewController.stopDrawRequest()
    }
    // 开始画
    @objc func startDrawEvent() -> Void {
        chatViewController.startDrawRequest()
    }
    
    func changeBoardState(value: Bool) {
        if  (self.boradKit != nil) {
            self.boradKit.setDrawEnable(value)
        }
       
    }
    // MARK: - 私有方法
    @objc private func saveImage(image: UIImage, didFinishSavingWithError error: NSError?, contextInfo: AnyObject) {
           if error != nil{
               SVProgressHUD.showError(withStatus: "保存失败")
               SVProgressHUD.dismiss(withDelay: 1, completion: nil)
           }else{
               SVProgressHUD.show(withStatus: "保存成功")
               SVProgressHUD.dismiss(withDelay: 1, completion: nil)
           }
    }
    @objc override func sendMessage()->Void {
       
        let chatModel :ARChatModel =  ARChatModel(uid: getUid(), text: chatTextField.text)
        chatViewController.addMessage(chatMessage: chatModel, isLocal: true)
        
        view.endEditing(true)
        chatTextField.text = ""
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

extension ARBoardViewController: ARtcEngineDelegate {
    func rtcEngine(_ engine: ARtcEngineKit, didOccurError errorCode: ARErrorCode) {
        //print("didOccurError:%d",errorCode)
    }
    func rtcEngine(_ engine: ARtcEngineKit, networkTypeChangedTo type: ARNetworkType) {
        // 检测网络变化，刷新下频道属性
        if (type.rawValue != 0) {
            if (self.chatViewController != nil) {
                self.chatViewController.getChannelAttributes()
            }
        }
    }
    
}
extension ARBoardViewController: ARBoardDelegate {
    
    func didHistoryDataSyncCompleted(_ boardKit: ARBoardKit) {
        //print("didHistoryDataSyncCompleted \n")
        
    }
   
    
}
