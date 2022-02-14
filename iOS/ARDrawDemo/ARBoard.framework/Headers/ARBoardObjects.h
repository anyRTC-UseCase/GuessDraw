//
//  ARBoardObjects.h
//  ARBoard
//
//  Created by 余生丶 on 2021/9/16.
//

#import <Foundation/Foundation.h>
#import <dsbridge/dsbridge.h>
#import "ARBoardEnumerates.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * @brief 白板视图
 */
@interface ARBoardView : DWKWebView

@end

/**
 * @brief 授权配置
 */
@interface ARBoardAuthParam : NSObject
/// 开发者信息
@property (nonatomic, copy) NSString *appId;
/// 用户 ID,自身业务的用户uid
@property (nonatomic, copy) NSString *uid;
/// 鉴权token，如果该应用开启权限认证，需要对接服务生成该token,没开启权限认证，不用使用字段
@property (nonatomic, copy) NSString *token;

@end


/**
 * @brief 白板常规配置参数
 */
@interface ARBoardInitConfig : NSObject
/// 默认白板宽高比，可传格式如“4:3”、“16:9”的字符串，默认“16:9”
@property (nonatomic, copy) NSString *ratio;
/// 白板默认缩放系数，实际缩放倍数为 scale/100,默认为100
@property (nonatomic, assign) UInt32 scale;
/// 白板工具，默认 ARBoardToolTypeFree_draw
@property (nonatomic, assign) ARBoardToolType toolType;

@end

/**
 * @brief 白板样式配置参数
 */
@interface ARBoardInitStyleConfig : NSObject
/// 文本样式，默认 ARBoardTextStyleNomal
@property (nonatomic, assign) ARBoardTextStyle textStyle;
/// 文本大小，默认：12
@property (nonatomic, assign) int textSize;
/// 文本颜色，默认:blackColor
@property (nonatomic, strong) UIColor *textColor;
/// 画笔颜色，默认:blackColor
@property (nonatomic, strong) UIColor *brushColor;
/// 画笔粗细，默认：待定
@property (nonatomic, assign) int brushThin;
/// 选框颜色，包括选框上的缩放，旋转按钮，默认：待定
@property (nonatomic, strong) UIColor *selectBoxColor;
/// 全局背景色，默认:whiteColor
@property (nonatomic, strong) UIColor *globalBackgroundColor;

@end

/**
 * @brief 白板权限配置参数
 */
@interface ARBoardInitAuthConfig : NSObject
/// 是否允许涂鸦，默认 YES
@property (nonatomic, assign) BOOL drawEnable;
/// 是否启用SDK内置Loading图标,默认为true
@property (nonatomic, assign) BOOL progressEnable;
/// 自定义加载动画，progressEnable为true时生效，支持 jpg、gif、png、svg
@property (nonatomic, copy) NSString *progressBarUrl;

@end

/**
 * @brief 白板基础配置信息
 */
@interface ARBoardBaseParam : NSObject

/// 白板常规配置参数
@property (nonatomic, strong) ARBoardInitConfig *config;
/// 白板样式配置参数
@property (nonatomic, strong) ARBoardInitStyleConfig *styleConfig;
/// 白板权限配置参数
@property (nonatomic, strong) ARBoardInitAuthConfig *authConfig;

@end



/**
 * @brief 白板信息
 */
@interface ARBoardInfo : NSObject

/// 白板 ID
@property (nonatomic, copy) NSString *boardId;
/// 背景图或者背景 H5 页面的 URL
@property (nonatomic, copy) NSString *backgroundUrl;
/// 白板背景色
@property (nonatomic, strong) UIColor *backgroundColor;

@end

/**
 * @brief 文件信息
 */
@interface ARBoardFileInfo : NSObject

/// 文件标识
@property (nonatomic, copy) NSString *fileId;
/// 文件名称
@property (nonatomic, copy) NSString *title;
/// 文件下载地址
@property (nonatomic, copy) NSString *downloadURL;
/// 当前页数
@property (nonatomic, assign) int pageIndex;
/// 页数
@property (nonatomic, assign) int pageCount;
/// 白板信息列表
@property (nonatomic, strong) NSArray<ARBoardInfo *> *boardInfoList;

@end

NS_ASSUME_NONNULL_END
