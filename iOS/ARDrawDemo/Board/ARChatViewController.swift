//
//  ARChatViewController.swift
//  ARDrawDemo
//
//  Created by zjq on 2022/1/18.
//

import UIKit
import AttributedString
import ARtmKit
import SVProgressHUD
import CoreAudioTypes

struct ARChatModel {
    var uid: String?
    var text: String?
}

class ARChatCell: UITableViewCell {
    
    @IBOutlet weak var colorView: UIView!
    
    @IBOutlet weak var messageLabel: UILabel!
    
    override func awakeFromNib() {
        super .awakeFromNib()
        colorView.layer.masksToBounds = true
        colorView.layer.cornerRadius = 5
        self.backgroundView = nil
        self.backgroundColor = UIColor.clear
    }
    func update(chatModel: ARChatModel) ->Void {
        let idColor: UIColor = UIColor.init(red: 24.0/255, green: 24.0/255, blue: 24.0/255, alpha: 0.6)
        let messageColor: UIColor = UIColor.init(red: 24.0/255, green: 24.0/255, blue: 24.0/255, alpha: 1.0)
        
        messageLabel.attributed.text = """
        \(chatModel.uid ?? "", .foreground(idColor)) \(":", .foreground(idColor)) \(chatModel.text ?? "", .foreground(messageColor))
        """
    }
    
}

class ARChatViewController: UITableViewController {
    
    private lazy var list = [ARChatModel]()
    var roomId: String!
    var joinChannel: Bool = false
    var connected: Bool = false
    var boardVc: ARBoardViewController!
    var rtmKit: ARtmKit!
    var rtmChannel: ARtmChannel!
    
    deinit {
        rtmChannel.leave()
        rtmChannel = nil
        rtmKit = nil
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        initRtm()
        // Do any additional setup after loading the view.
        tableView.backgroundColor = UIColor.clear
        tableView.backgroundView = nil
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 44
        
    }
    
    func initRtm() {
        if rtmKit == nil {
            rtmKit = ARtmKit(appId: AppID, delegate: self)!
        }
        
        rtmKit.login(byToken: nil, user: getUid()) { [weak self]
            errorCode in
            guard let weakself = self else { return }
            weakself.rtmChannel = weakself.rtmKit.createChannel(withId: weakself.roomId, delegate: self)
            weakself.rtmChannel?.join(completion: { code in
                if code == .channelErrorOk {
                    weakself.joinChannel = true
                    // 获取频道属性
                    weakself.getChannelAttributes()
                }
            })
        }
        
    }
    //MARK: - 方法
    // 获取频道属性
    public func getChannelAttributes() -> Void {
        self.rtmKit.getChannelAllAttributes(self.roomId) { channelAttributes , code in
            if code.rawValue == 0 {
                self.changeStateWithAtt(attributes: channelAttributes ?? [])
            }
        }
    }
    // 请求画画
    public func startDrawRequest() -> Void {
        if !self.joinChannel || !self.connected {
            // 提示错误
            SVProgressHUD.showError(withStatus: "服务出错")
            SVProgressHUD.dismiss(withDelay: 1)
            return
        }
        let options = ARtmChannelAttributeOptions()
        options.enableNotificationToChannelMembers = true
    
        let attribute: ARtmChannelAttribute = ARtmChannelAttribute()
        attribute.key = ARKeyChannel
        attribute.value = randomString(length: 9)
    
        rtmKit.addOrUpdateChannel(self.roomId, attributes: [attribute], options: options) { errorCode in
            if errorCode == .attributeOperationErrorOk {
                
            }
        }
    }
    // 结束画画
    public func stopDrawRequest() -> Void {
        let option :ARtmChannelAttributeOptions = ARtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        
        rtmKit.deleteChannel(self.roomId, attributesByKeys: [ARKeyChannel], options: option) { erroCode in
            
        }
    }
    func changeStateWithAtt(attributes: [ARtmChannelAttribute]) -> Void {
        if attributes.count != 0 {
            let rtmAtt: ARtmChannelAttribute = attributes[0]
            if rtmAtt.lastUpdateUid == getUid() {
                // 自己
                boardVc.drawingLabel.text = rtmAtt.lastUpdateUid + "开始作画"
                boardVc.drawToolView.isHidden = false
                boardVc.closeDrawButton.isHidden = false
                boardVc.drawButton.isHidden = true
                boardVc.changeBoardState(value: true)
            }else{
                // 别人
                boardVc.drawingLabel.text = rtmAtt.lastUpdateUid + "开始作画"
                boardVc.drawToolView.isHidden = true
                boardVc.closeDrawButton.isHidden = true
                boardVc.drawButton.isHidden = true
                boardVc.changeBoardState(value: false)
            }
            
        }else{
            // 没人了
            if (boardVc != nil) {
                boardVc.drawingLabel.text = ""
                boardVc.drawToolView.isHidden = true
                boardVc.closeDrawButton.isHidden = true
                boardVc.drawButton.isHidden = false
                boardVc.changeBoardState(value: false)
            }
        }
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return list.count
    }
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ARChatCell", for: indexPath) as! ARChatCell
        cell.update(chatModel: list[indexPath.row])
        return cell
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

extension ARChatViewController :ARtmDelegate,ARtmChannelDelegate{
    // 添加消息
    func addMessage(chatMessage: ARChatModel,isLocal: Bool) -> Void {
        if !self.joinChannel || !self.connected {
            // 提示错误
            print("RTM 初始化失败！或未连接成功 \n")
            return
        }
        if isLocal {
            // 发送消息
            let message = ARtmMessage(text: chatMessage.text ?? "")
            let options = ARtmSendMessageOptions()
            options.enableHistoricalMessaging = true
            rtmChannel?.send(message, sendMessageOptions: options, completion: { code  in
                // 本地显
                self.list.append(chatMessage)
                let index = IndexPath(row: self.list.count-1, section: 0)
                self.tableView.insertRows(at: [index], with: .automatic)
                self.tableView.scrollToRow(at: index, at: .middle, animated: true)
            })
        }else{
            // 远程消息
            self.list.append(chatMessage)
            let index = IndexPath(row: self.list.count-1, section: 0)
            self.tableView.insertRows(at: [index], with: .automatic)
            self.tableView.scrollToRow(at: index, at: .middle, animated: true)
        }
        
    }
    // MARK: - ARtmDelegate
    func rtmKit(_ kit: ARtmKit, connectionStateChanged state: ARtmConnectionState, reason: ARtmConnectionChangeReason) {
        if state == .connected {
            self.connected = true
            SVProgressHUD.dismiss()
            // 重连成功获取下状态
            self.getChannelAttributes()
        }else{
            self.connected = false
            SVProgressHUD.show(UIImage(named: "icon-76")!, status: "加载中")
            if (reason.rawValue == 4 || reason.rawValue == 8) {
                // 重新登录
                self.initRtm()
            }
        }
    }
    
    //            
    // 消息
    func channel(_ channel: ARtmChannel, messageReceived message: ARtmMessage, from member: ARtmMember) {
        let chatModel = ARChatModel(uid: member.uid, text: message.text)
        addMessage(chatMessage: chatModel, isLocal: false)
    }
    // 状态
    func channel(_ channel: ARtmChannel, attributeUpdate attributes: [ARtmChannelAttribute]) {
        changeStateWithAtt(attributes: attributes)
    }
    // 人数
    func channel(_ channel: ARtmChannel, memberCount count: Int32) {
        boardVc.onlineLabel.text = "在线:" + "\(count)"
    }
}
