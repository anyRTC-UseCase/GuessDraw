//
//  ARBoardKit.h
//  ARBoard
//
//  Created by 余生丶 on 2021/9/16.
//

#import <Foundation/Foundation.h>
#import "ARBoardDelegate.h"
#import "ARBoardEnumerates.h"
#import "ARBoardObjects.h"

NS_ASSUME_NONNULL_BEGIN

@interface ARBoardKit : NSObject

//MARK: - 创建销毁实例

/**-----------------------------------------------------------------------------
* @name 创建销毁实例
* -----------------------------------------------------------------------------
*/

/** 实例化 ARBoardKit 对象

该方法初始化一个 ARBoardKit 对象。使用 ARBoardKit，必须先调用该接口进行初始化。
 
@param authParam 授权配置， anyRTC云平台 为 App 开发者签发的 App ID。每个项目都应该有一个独一无二的 App ID。如果你的开发包里没有 App ID，请从ar云平台官网申请一个新的 App ID。
 
 * 用 App ID 标示你的项目和所属组织
 * uid 为自身业务 id,可以标识业务中的用户
 * token:开启权限认证功能方可使用，使用时需要先进行服务对接生成token，然后向自身业务请求该服务生成的token;如果未开启权限认证功能，该字段无需使用

 
 @param roomId 标识通话频道的字符串，长度在 64 字节以内的字符串。
 以下为支持的字符集范围（共 89 个字符）:

 * 26 个小写英文字母 a-z
 * 26 个大写英文字母 A-Z
 * 10 个数字 0-9
 * 空格
 * "!", "#", "$", "%", "&", "(", ")", "+", "-", ":", ";", "<", "=", ".", ">", "?", "@", "[", "]", "^", "_", "{", "}", "|", "~", ","

 @param baseParam 白板配置项.
 
 @param delegate ARBoardDelegate.

@return 一个 ARBoardKit 实例对象
*/
- (instancetype)initWithAuthParam:(ARBoardAuthParam *)authParam roomId:(NSString *_Nonnull)roomId boardParam:(ARBoardBaseParam *)baseParam delegate:(id<ARBoardDelegate> _Nullable)delegate;

/** 释放白板
 
 * 调用反初始化接口后会释放内部资源，白板功能将失效
 
 @return 0方法调用成功，<0方法调用失败
 */
- (int)destory;

/** 查询 SDK 版本号

 @return 当前的 SDK 版本号，格式为字符串，如 1.0.0
 */
+ (NSString * _Nonnull)getVersion;

/**
 设置／获取 ARBoardDelegate
 */
@property (nonatomic, weak) id<ARBoardDelegate> _Nullable delegate;

//MARK: - 核心方法

/** 获取白板渲染视图

 @return 白板视图
 */
- (ARBoardView *)getBoardRenderView;

/** 通过配置 SDK 提供技术预览或特别定制功能

 @param options 字典格式的 SDK 选项
 
 @return 0方法调用成功，<0方法调用失败
 */
- (int)setParameters:(NSDictionary * _Nonnull)options;

//MARK: - 涂鸦相关接口

/**-----------------------------------------------------------------------------
* @name 涂鸦相关接口
* -----------------------------------------------------------------------------
*/

/** 设置白板是否允许涂鸦

 @param enable YES可以涂鸦，NO不可以涂鸦
 */
- (void)setDrawEnable:(BOOL)enable;

/** 获取白板是否允许涂鸦
 
 @return 白板涂鸦状态
 */
- (BOOL)isDrawEnable;

/** 设置当前白板页背景色

 @param color 背景颜色
 */
- (void)setBackgroundColor:(UIColor *)color;

/** 获取当前白板页背景色
 
 @return 白板页背景色
 */
- (UIColor *)getBackgroundColor;

/** 设置所有白板的背景色
 
 @param color 要设置的全局背景色
 
 **Note**
 调用该接口将导致所有白板的背景色发生改变
 新创建白板的默认背景色取全局背景色
*/
- (void)setGlobalBackgroundColor:(UIColor *)color;

/** 获取白板全局背景色
 
 @return 全局背景色
*/
- (UIColor *)getGlobalBackgroundColor;

/** 设置画笔类型

 @param type 画笔类型
 */
- (void)setToolType:(ARBoardToolType)type;

/** 获取画笔类型
 
 @return 画笔类型
 */
- (ARBoardToolType)getToolType;


/** 设置的画笔颜色
 
 @param color 画笔颜色
 */
- (void)setBrushColor:(UIColor *)color;

/** 获取画笔颜色
 
 @return 画笔颜色
 */
- (UIColor *)getBrushColor;

/** 设置画笔粗细
 
 @param thin 画笔粗细
 */
- (void)setBrushThin:(UInt32)thin;

/** 获取画笔粗细
 
 @return 画笔粗细
 */
- (UInt32)getBrushThin;

/** 设置文本颜色
 
 @param color 文本颜色
 */
- (void)setTextColor:(UIColor *)color;

/** 获取文本颜色
 
 @return 文本颜色回调
 */
- (UIColor *)getTextColor;

/** 设置文本大小
 
 @param size 文本大小
 */
- (void)setTextSize:(UInt32)size;

/** 获取文本大小
 
 @return 文本大小
 */
- (UInt32)getTextSize;

/** 清空当前白板页数据
 
 @param clearBackground 如果 clearBackground 参数为 true，则同时清除涂鸦和一切背景
 
 **Note**
  
 默认只清除当前白板页的涂鸦。
 */
- (void)clear:(BOOL)clearBackground;

/**
 撤销当前白板页上一次动作
 */
- (void)undo;

/**
 重做当前白板页上一次撤销
 */
- (void)redo;

/**
 重新计算白板大小，并渲染
 */
- (void)resize;

/** 重置白板
 
 删除所有白板页和涂鸦
*/
- (void)reset;

//MARK: - 白板页操作接口

/**-----------------------------------------------------------------------------
* @name 白板页操作接口
* -----------------------------------------------------------------------------
*/

/**
 获取当前白板页的快照
 
 @param imageBlock 白板快照回调
 */
- (void)getBoardSnapshot:(void(^ _Nullable)(UIImage *))imageBlock;

/**
 添加一页白板并切换到这一页
 */
- (void)addBoard;

/**
 删除当前白板页并切换到上一页
 */
- (void)deleteBoard;

/** 切换到指定的白板页
 
 @param boardId 要跳转到的白板页 ID
*/
- (void)gotoBoard:(NSString *_Nonnull)boardId;

/** 切换到下一页白板
 
 **Note**
 
 若当前白板页为当前文件的最后一页，则该接口调用无效
*/
- (void)nextBoard;

/** 切换到上一页白板
 
 **Note**
 
 若当前白板页为当前文件的第一页，则该接口调用无效
*/
- (void)preBoard;

/** 获取当前白板页 ID
 
 @return 当前白板页 ID
*/
- (NSString *)getCurrentBoardId;

/** 获取所有文件的白板列表
 
 @return 所有的白板页的 ID 列表
*/
- (NSArray<NSString *> *)getBoardList;

/** 获取指定文件的白板 ID 列表
 
 @param fileId 文件ID
 @param result 指定文件的白板 ID 列表回调
*/
- (void)getFileBoardList:(NSString *_Nonnull)fileId WithResult:(void(^)(NSArray<NSString *> * ))result;

/** 获取当前文件 ID
 
 @return 当前白板文件ID
*/
- (NSString *)getCurrentFileId;

/** 获取白板的基本信息
 
 @param fileId 文件ID
 @param result 指定文件的文件信息回调
*/
- (void)getFileInfo:(NSString *_Nonnull)fileId WithResult:(void(^)(ARBoardFileInfo *fileInfo))result;

/** 设置当前白板页缩放比例
 
 @param scale 要设置的白板缩放比例
 
 **Note**
 100 则为不缩放，范围在 [100 ~ 300] 之间
*/
- (void)setBoardScale:(UInt32)scale;

/** 获取当前白板页缩放比例
 
 @return 白板缩放比例
*/
- (UInt32)getBoardScale;

@end

NS_ASSUME_NONNULL_END
