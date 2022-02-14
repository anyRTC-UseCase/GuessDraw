//
//  ARBoardDelegate.h
//  ARBoard
//
//  Created by 余生丶 on 2021/10/20.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "ARBoardEnumerates.h"

NS_ASSUME_NONNULL_BEGIN

@class ARBoardKit;

@protocol ARBoardDelegate <NSObject>

@optional

/** 发生错误回调

@param boardKit   ARBoardKit对象
@param errorCode   错误码，详见 ARErrorCode
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit didOccurError:(ARBoardErrorCode)errorCode;


- (void)didHistoryDataSyncCompleted:(ARBoardKit *)boardKit;
/** 添加白板页的回调

@param boardKit   ARBoardKit对象
@param boardIds   白板页 ID 列表
@param fileId   白板的文件 ID
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit addBoard:(NSArray *)boardIds fileId:(NSString *)fileId;

/** 删除白板页的回调

@param boardKit   ARBoardKit对象
@param boardIds   白板页 ID 列表
@param fileId   白板的文件 ID
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit deleteBoard:(NSArray *)boardIds fileId:(NSString *)fileId;

/** 白板页切换的回调

@param boardKit   ARBoardKit对象
@param fileId   跳转到的白板页所属的文件 ID
@param boardId   跳转到的白板页 ID
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit gotoBoard:(NSString *)fileId boardId:(NSString *)boardId;

/** 白板页缩放的回调

@param boardKit   ARBoardKit对象
@param boardId   缩放变化的白板页 ID
@param scale  缩放比例
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit scaleChannge:(NSString *)boardId scale:(CGFloat)scale;

/** 网络状态回调

@param boardKit   ARBoardKit对象
@param state   当前的网络连接状态
@param reason  触发当前网络连接状态的原因
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit connectionChangedToState:(ARBoardConnectionStateType)state reason:(ARBoardConnectionChangedReason)reason;

/** 当前白板页是否可撤销

@param boardKit   ARBoardKit对象
@param enable 白板是否可执行 undo 操作
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit undoStateChange:(BOOL)enable;

/** 当前白板页是否可重做

@param boardKit   ARBoardKit对象
@param enable 白板是否可执行 redo 操作
*/
- (void)board:(ARBoardKit *_Nonnull)boardKit redoStateChange:(BOOL)enable;

@end

NS_ASSUME_NONNULL_END
