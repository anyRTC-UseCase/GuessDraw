//
//  ARBoardEnumerates.h
//  ARBoard
//
//  Created by 余生丶 on 2021/9/16.
//

#ifndef ARBoardEnumerates_h
#define ARBoardEnumerates_h

/** 错误码 */
typedef NS_ENUM(NSInteger, ARBoardErrorCode) {
    
    /**
     0: APPID 不合法
     */
    ARBoardErrorCodeAppid_Invalid = 0,
    
    /**
     1: 未登录或者重连过程中
     */
    ARBoardErrorCodeAuthentication_Failed = 1,
    
    /**
     2: 获取不到网关地址
     */
    ARBoardErrorCodeNoGetGateWay = 2,
    
    /**
     3: 频道被禁用
     */
    ARBoardErrorCodeChannel_Banned = 3,
    
    /**
     4: 开发者信息不合法
     */
    ARBoardErrorCodeDeveloper_Invalid = 4,
    
    /**
     5: 网关异常
     */
    ARBoardErrorCodeGateWayError = 5,
    
    /**
     6: 非法操作。
     
     通常是因为在当前网络状态不能进行该操作。
     */
    ARBoardErrorCodeInvalid_Operation = 6,
    
    /**
     7: 非法参数。
     */
    ARBoardErrorCodeInvalid_Params = 7,
    
    /**
     8: IP 地址被禁用
     */
    ARBoardErrorCodeIP_Banned = 8,
    
    /**
     9: 缺少参数
     */
    ARBoardErrorCodeMissing_Parameter = 9,
    
    /**
     10: 未检测到服务
     */
    ARBoardErrorCodeServerNotOpen = 10,
    
    /**
     11: TOKEN 过期
     */
    ARBoardErrorCodeToken_Expired = 11,
    
    /**
     12: TOKEN 验证失败
     */
    ARBoardErrorCodeToken_Invalid = 12,
    
    /**
     13: 当前用户被踢出。
     */
    ARBoardErrorCodeUid_Banned = 13
};

/** 画笔类型 */
typedef NS_ENUM(NSInteger, ARBoardToolType) {
    /**
     0: 鼠标
     */
    ARBoardToolTypeNone = 0,
    /**
     1: 选择
     */
    ARBoardToolTypeSelect = 1,
    /**
     2: 涂鸦
     */
    ARBoardToolTypeFree_draw = 2,
    /**
     3: 橡皮擦
     */
    ARBoardToolTypeEraser = 3,
    /**
     4: 激光笔
     */
    ARBoardToolTypeLaser_pointer = 4,
    /**
     5: 直线
     */
    ARBoardToolTypeLine = 5,
    /**
     6: 箭头
     */
    ARBoardToolTypeArrow = 6,
    /**
     7: 矩形
     */
    ARBoardToolTypeRect = 7,
    /**
     8: 椭圆
     */
    ARBoardToolTypeEllipse = 8,
    /**
     9: 文本
     */
    ARBoardToolTypeText = 9
};

/** 网络连接状态类型 */
typedef NS_ENUM(NSInteger, ARBoardConnectionStateType) {
    /**
     1: 网络连接断开
     */
    ARBoardConnectionStateDisconnected = 1,
    
    /**
     2: 建立网络连接中
     */
    ARBoardConnectionStateConnecting = 2,
    /**
     3: 重新建立网络连接中
     */
    ARBoardConnectionStateReconnecting = 3,
    /**
     4: 网络已连接
     */
    ARBoardConnectionStateConnected = 4,
    /**
     5: 网络连接失败
     */
    ARBoardConnectionStateFailed = 5
    
};

/** 引起网络连接状态发生改变的原因 */
typedef NS_ENUM(NSUInteger, ARBoardConnectionChangedReason) {
    
    /**
     0: 默认值
     */
    ARBoardConnectionChangedDefault = 0,
    
    /**
     1: 请求超时，通常是因为网络质量差或连接断开导致与服务器通信失败。
     */
    ARBoardConnectionChangedTimeout = 1,
    
    /**
     2: 响应错误，一般是状态码非法。
     */
    ARBoardConnectionChangedResponseError = 2,
    
    /**
     3: 无法定位的网络错误
     */
    ARBoardConnectionChangedError = 3,
    
    /**
     4: 本地保活超时
     */
    ARBoardConnectionChangedKeepAliveTimeout = 4,
    
    /**
     5: 获取不到网关地址
     */
    ARBoardConnectionChangedGateWay = 5
};

/** 字体样式 */
typedef NS_ENUM(NSUInteger, ARBoardTextStyle) {
    /**
     0: 默认
     */
    ARBoardTextStyleNomal = 0,
    /**
     1: 粗体
     */
    ARBoardTextStyleBold = 1,
    /**
     2: 斜体
     */
    ARBoardTextStyleItalic = 2,
    /**
     2: 粗斜体
     */
    ARBoardTextStyleBoldItalic = 3,
};

#endif /* ARBoardEnumerates_h */
