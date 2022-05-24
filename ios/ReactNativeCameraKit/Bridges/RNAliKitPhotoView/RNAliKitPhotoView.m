//
//  RNAliavkitPhotoView.m
//  AFNetworking
//
//  Created by Mac on 2022/5/11.
//

#import <Foundation/Foundation.h>

#import "RNAliKitPhotoView.h"
#import "RNAliKitPhotoViewManager.h"
#if __has_include(<React/RCTBridge.h>)
#import <React/RCTComponent.h>
#else
#import "RCTComponent.h"
#endif

#import "AliyunPhotoLibraryManager.h"
#import "AliyunCompositionCell.h"
#import "AliyunCompositionInfo.h"

#import "ImageCacheTool.h"


//RN交互参数
@interface RNAliKitPhotoView ()
/**单页显示数据量,用于计算缩略图提前计算*/
@property (nonatomic) NSUInteger pageSize;
/**横向列数,用于页面显示和计算默认itemWidth*/
@property (nonatomic) NSUInteger numColumns;
/**是否多选,默认flase,注意视频即使开启开关也不能多选,相册上限10等待后续增加字段*/
@property (nonatomic) BOOL multiSelect;
// 最多可选数量
@property (nonatomic) NSUInteger maxSelectCount;
// 默认选中下标
@property (nonatomic) NSUInteger defaultSelectedPosition;
/**高优先级设置每个item的宽高,不做上下线干涉*/
@property (nonatomic) CGFloat itemWidth;
/**高优先级设置每个item的宽高,不做上下线干涉*/
@property (nonatomic) CGFloat itemHeight;
/**每次点击照片/视频后回调RN,注意是点击不是选择*/
@property (nonatomic, copy) RCTBubblingEventBlock onSelectedPhotoCallback;
/** 触发最大照片选择回调*/
@property (nonatomic, copy) RCTBubblingEventBlock onMaxSelectCountCallback;
/** error回调 (errorCode,errorMessage)=>{}*/
@property (nonatomic, copy) RCTBubblingEventBlock onErrorCallback;
@end

//photoView内部交互参数
@interface RNAliKitPhotoView ()<UICollectionViewDelegate, UICollectionViewDataSource ,AYCellDelegate>
//标识出当前是否需要刷新页面(配合RN的特殊刷新机制)
@property (atomic,assign)BOOL renderStatus;
/** 布局样式只在RN入参更改的时候改变*/
@property (nonatomic,strong)UICollectionViewFlowLayout *flowLayout;
//主显示网格视图
@property (nonatomic,weak)UICollectionView *collectionView;
//相册只支持一次取出所有数据
@property (nonatomic,strong)NSArray<AliyunAssetModel *> *libraryDataArray;
//本地做数据分页处理
@property (nonatomic,strong)NSArray<AliyunAssetModel *> *viewDataArray;
//当前选中照片的下标存放集合
@property (nonatomic,strong)NSMutableArray *selectedIndexs;
//单选多选的临时保存
@property (nonatomic,strong)NSMutableArray *tempSelectedIndexs;
/** 最后选中的元素下标*/
@property (nonatomic,assign)NSInteger lastSelectIndex;
//滑到最下面的时候会显示出底部foot栏,之后才刷新数据可以节约内存
@property (nonatomic,assign)BOOL showFooterStatus;

@end
    
@implementation RNAliKitPhotoView

#pragma mark - KVO 监视滑动区域来进行分页
//当前视图在RN中以组件的形式存在,会随RN中组件的销毁而在RN关联框架中被调用removeFromSuperview
- (void)dealloc
{
    //不需要super dealloc
    [self.collectionView removeObserver:self forKeyPath:@"contentOffset"];
}
//监听滑动,到底最下面的时候再拼接新的数据再下面
- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if ([keyPath isEqualToString:@"contentOffset"] && object == self.collectionView)
    {
        NSValue *newvalue   = change[NSKeyValueChangeNewKey];
        NSValue *oldvalue   = change[NSKeyValueChangeOldKey];
        CGFloat newoffset_y = newvalue.UIOffsetValue.vertical;
        CGFloat oldoffset_y = oldvalue.UIOffsetValue.vertical;
        CGFloat viewHeight  = self.collectionView.frame.size.height;
        CGFloat maxOffsetY  = self.collectionView.contentSize.height - viewHeight;
        //第二阶段,触发回弹,并且回弹结束
        if(self.showFooterStatus && oldoffset_y > newoffset_y && maxOffsetY > (newoffset_y-30))
        {
            //极端情况可能会出现滑动速度快于数据加载
            @synchronized (self.viewDataArray) {
                NSInteger dataCount = self.viewDataArray.count;
                NSInteger pageSize  = MIN(self.libraryDataArray.count - dataCount, self.pageSize);
                NSRange range       = NSMakeRange(dataCount, pageSize);
                NSArray *moreData   = [self.libraryDataArray subarrayWithRange:range];
                self.viewDataArray  = [self.viewDataArray arrayByAddingObjectsFromArray:moreData];
                [self.collectionView reloadData];
                self.showFooterStatus = NO;
            }
        }
        //第一阶段,滑动到最底部
        if(maxOffsetY <= newoffset_y && self.viewDataArray.count <= self.libraryDataArray.count){
            self.showFooterStatus = YES;
        }
    }
}
-(void)addObserver
{
    [self.collectionView addObserver:self forKeyPath:@"contentOffset" options:(NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld) context:nil];
}
#pragma mark - rn reset view
//部分属性在RN中修改的时候原生环境下UI需要做出改变
-(void)resetPhotoView
{
    if(!self.renderStatus)
    {
        return;
    }
    
    //刷新页面的时候需要处理历史数据
    if(self.selectedIndexs.count > 0 && !self.multiSelect)
    {
        //多选切换单选需要处理数据
        self.selectedIndexs = [NSMutableArray arrayWithObject:@(self.lastSelectIndex).stringValue];
        [self sendSelectPhotoDataToRN];
    }
    
    self.flowLayout = [self getFlowLayout];
    self.collectionView.collectionViewLayout = self.flowLayout;
    [self.collectionView reloadData];
}
- (void)setPageSize:(NSUInteger)pageSize
{
    if(_pageSize != pageSize){
        _pageSize = pageSize;
        [self resetPhotoView];
    }
}
- (void)setNumColumns:(NSUInteger)numColumns
{
    if(_numColumns != numColumns){
        _numColumns = numColumns;
        [self resetPhotoView];
    }
}
- (void)setMultiSelect:(BOOL)videoMute
{
    if(_multiSelect != videoMute){
        _multiSelect = videoMute;
        [self resetPhotoView];
    }
}
- (void)setItemWidth:(CGFloat)itemWidth
{
    if(_itemWidth != itemWidth){
        _itemWidth = itemWidth;
        [self resetPhotoView];
    }
}
- (void)setItemHeight:(CGFloat)itemHeight
{
    if(_itemHeight != itemHeight){
        _itemHeight = itemHeight;
        [self resetPhotoView];
    }
}

#pragma mark - setup view

//初次页面基本渲染后,后续修改入参动态调整UI显示;
-(void)layoutSubviews
{
    [super layoutSubviews];
    self.renderStatus = true;
    //后台重新进入页面
    if(!self.collectionView)
    {
        [self setupSubviews];
        [self fetchPhotoData];
    }
}
//初始化UI
- (void)setupSubviews
{
    self.flowLayout         = [self getFlowLayout];
    CGFloat viewWidth       = self.frame.size.width;
    CGFloat viewHeight      = self.frame.size.height;
    CGRect collectionFrame  = CGRectMake(0, 0, viewWidth, viewHeight);
    UICollectionView *collectionView = [[UICollectionView alloc] initWithFrame:collectionFrame collectionViewLayout:_flowLayout];
    self.collectionView     = collectionView;
    self.collectionView.backgroundColor = [UIColor clearColor];
    [self.collectionView registerClass:[AliyunCompositionCell class] forCellWithReuseIdentifier:@"AliyunCompositionCell"];
    self.collectionView.alwaysBounceVertical = YES;
    self.collectionView.delegate             = self;
    self.collectionView.dataSource           = self;
    [self addSubview:self.collectionView];
}


- (void)fetchPhotoData
{
    __weak typeof(self)weakSelf =self;
    [[AliyunPhotoLibraryManager sharedManager] requestAuthorization:^(BOOL authorization) {
        if (!authorization) {
            return;
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            //分页处理
            [[AliyunPhotoLibraryManager sharedManager] getCameraRollAssetWithallowPickingVideo:NO allowPickingImage:NO durationRange:(VideoDurationRange){2,60*60} completion:^(NSArray<AliyunAssetModel *> *models, NSInteger videoCount){
                weakSelf.libraryDataArray = models;
                weakSelf.viewDataArray = [weakSelf.libraryDataArray subarrayWithRange:NSMakeRange(0, MIN(models.count,self.pageSize))];
                [weakSelf.collectionView reloadData];
                [weakSelf addObserver];
                //默认选中第一张照片
                [weakSelf selectDataUpdate:MIN(models.count,self.defaultSelectedPosition)];
            }];
        });
    }];
}

//layou参数决定了每个item的显示
- (UICollectionViewFlowLayout *)getFlowLayout
{
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    //numColumns优先级大于viewWidth
    CGFloat viewWidth       = self.frame.size.width;
    NSUInteger numColumns   = _numColumns ?: 4;
    //列数决定item实际宽度
    CGFloat cellWidth       = viewWidth / numColumns;
    //item宽度需要和传入的_itemWidth算是间距
    CGFloat itemSpacing     = _itemWidth==0?0:cellWidth - MIN(cellWidth , _itemWidth);
    //有间距时重新计算cellWidth的大小
    if(itemSpacing > 0)
    {
        cellWidth = (viewWidth - itemSpacing * numColumns) / numColumns;
    }
    //item高度没有传入时用itemWidth
    CGFloat cellHeight          = _itemHeight ?: cellWidth;
    layout.itemSize             = CGSizeMake(cellWidth, cellHeight);
    layout.minimumLineSpacing   = 0;
    layout.minimumInteritemSpacing = itemSpacing;
    return layout;
}


#pragma mark - UICollectionViewDelegate
//UICollectionView内部结构:{组:cell群},在不使用多组结构时,section没有用处
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return self.viewDataArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    AliyunCompositionCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"AliyunCompositionCell" forIndexPath:indexPath];
    AliyunAssetModel *model = self.viewDataArray[indexPath.item];
    cell.delegate       = self;
    cell.labelDuration  = model.timeLength;
    cell.hiddenDuration = model.type == AliyunAssetModelMediaTypePhoto;
    //涉及cell重用,元素需要保持初始状态或保证每次都赋值
    cell.photoImage     = nil;
    //cell样式处理
    BOOL selectStatus   = [self.selectedIndexs containsObject:@(indexPath.item).stringValue];
    cell.photoIndex     = 0;
    cell.indexPath      = indexPath;
    if(!self.multiSelect){
        //非多选状态下页面只有默认状态和白色模版状态
        cell.cellStatus     = selectStatus ? AYPhotoCellStatusSelect : AYPhotoCellStatusDefault;
        cell.selectStatus   = AYPhotoSelectStatusDefault;
    }else if(self.selectedIndexs.count == 0){
        //多选情况下允许不选择
        cell.cellStatus     =  AYPhotoCellStatusDefault;
        cell.selectStatus   = AYPhotoSelectStatusUnchecked;
    }else{
        // 当前已选中视频数据
        BOOL selectVideoStatus = [self selectVideoStatus];
        // 当前item不是视频
        BOOL noVideoData       = model.type != AliyunAssetModelMediaTypeVideo;
        // 已选中数据:白色蒙版
        if(selectStatus){
            BOOL lastSelectData = self.lastSelectIndex == indexPath.item;
            cell.cellStatus   = lastSelectData ? AYPhotoCellStatusSelect : AYPhotoCellStatusDefault;
            cell.selectStatus = selectVideoStatus ? AYPhotoSelectStatusCheck : AYPhotoSelectStatusNumber;
            cell.photoIndex   = [self.selectedIndexs indexOfObject:@(indexPath.item).stringValue]+1;
        }else if(selectVideoStatus == noVideoData){
            //选中视频时,非视频数据加蒙版
            //没选中视频时,视频数据加蒙版
            cell.cellStatus   = AYPhotoCellStatusNoEnabled;
            cell.selectStatus = AYPhotoSelectStatusUnchecked;
        }else{
            cell.cellStatus   = AYPhotoCellStatusDefault;
            cell.selectStatus = AYPhotoSelectStatusUnchecked;
        }
    }
    NSString *filename = [model.asset valueForKey:@"filename"];
    if (model.fetchThumbnail) {
        cell.photoImage = model.thumbnailImage;
    }else if([ImageCacheTool checkImageCache:filename]){
        //直接从缓存中取出缩略图
        UIImage *photo       = [ImageCacheTool imageWithName:filename];
        model.fetchThumbnail = YES;
        model.thumbnailImage = photo;
        cell.photoImage      = photo;
    } else {
        CGFloat photoWidth = self.flowLayout.itemSize.width;
        [[AliyunPhotoLibraryManager sharedManager] getPhotoWithAsset:model.asset thumbnailImage:YES photoWidth:photoWidth completion:^(UIImage *photo, NSDictionary *info) {
            if(photo) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    model.fetchThumbnail = YES;
                    model.thumbnailImage = photo;
                    cell.photoImage      = photo;
                    //缩略图存到到缓存中
                    [ImageCacheTool saveImageToCache:photo name:filename];
                });
            }
        }];
    }
    return cell;
}
// 设置Footer的尺寸
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout referenceSizeForFooterInSection:(NSInteger)section
{
    CGFloat screenWidth = [UIScreen mainScreen].bounds.size.width;
    //避免刘海屏最下面遮住照片
    CGFloat safeAreaBottom = 34;
    if (@available(iOS 11.0, *)) {
        //RN嵌入页面的view拿不到safeAreaInsets
        //safeAreaBottom = self.safeAreaInsets.bottom;
        safeAreaBottom = [self viewController].view.safeAreaInsets.bottom;
    }
    CGFloat itemheight = self.flowLayout.itemSize.height;
    return CGSizeMake(screenWidth, safeAreaBottom + itemheight/2);
}
//获取当前的view的根vc
- (UIViewController *)viewController {
    UIView *next = self;
    while ((next = [next superview])) {
        UIResponder *nextResponder = [next nextResponder];
        if ([nextResponder isKindOfClass:[UIViewController class]])
        {
            return (UIViewController *)nextResponder;
        }
    }
    return nil;
}



#pragma mark - photo view control

//当前是否选中了视频
-(BOOL)selectVideoStatus
{
    for(NSString *indexStr in self.selectedIndexs)
    {
        AliyunAssetModel *model = self.viewDataArray[[indexStr integerValue]];
        //懒得写两个if了,不是bug
        return model.type == AliyunAssetModelMediaTypeVideo;
    }
    return NO;
}

//cell右上角点击事件
- (void)cell:(AliyunCompositionCell *)cell didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    [self selectDataUpdate:indexPath.item];
}
//cell主要点击事件,多选状态下,照片取消要依靠点击右上角
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    //oc的array不允许直接放int
    NSString *indexStr      = @(indexPath.item).stringValue;
    //已有数据
    BOOL cellSelectStatus   = [self.selectedIndexs containsObject:indexStr];
    //多选模式下,照片只能通过右上角勾选按钮删除
    if(self.multiSelect && cellSelectStatus)
    {
        if(self.lastSelectIndex != indexPath.item)
        {
            //修改白色模版的位置
            self.lastSelectIndex = indexPath.item;
            [self.collectionView reloadData];
            [self sendSelectPhotoDataToRN];
        }else{
            //多选模式下,已选中视频/照片的第二次点击不做处理
        }
    }else{
        [self selectDataUpdate:indexPath.item];
    }
}

//选中数组更新,
-(void)selectDataUpdate:(NSInteger)index
{
    AliyunAssetModel *model = self.viewDataArray[index];
    PHAsset *phAsset = model.asset;
    if (phAsset.pixelWidth+phAsset.pixelHeight <=0)
    {
        //文件已损坏
        self.onErrorCallback(@{@"code":@"10001",@"message":@"file error"});
        return;
    }
    if(phAsset.mediaType != PHAssetMediaTypeImage && phAsset.mediaType != PHAssetMediaTypeVideo){
         //特殊类型
        self.onErrorCallback(@{@"code":@"10002",@"message":@"type not supported"});
        return;
    }
    //oc的array不允许直接放int
    NSString *indexStr = @(index).stringValue;
    //已有数据,删除后刷新页面
    BOOL containIndex = [self.selectedIndexs containsObject:indexStr];
    NSInteger dataIndex = [self.selectedIndexs indexOfObject:indexStr];
    //单选情况的逻辑比较简单,不能取消和替换元素
    if(!self.multiSelect){
        //单选下,不允许取消,点击无效处理
        if(containIndex){
            return;
        }else{
            self.selectedIndexs  = [NSMutableArray arrayWithObject:indexStr];
            self.lastSelectIndex = index;
        }
    }else{
        if(!self.selectedIndexs)
        {
            self.selectedIndexs  = [NSMutableArray new];
        }
        //多选下可以删除所有元素
        if(containIndex)
        {
            [self.selectedIndexs removeObjectAtIndex:dataIndex];
            //多选下如果删除了当前选中的照片,即从选中已选照片中最后的一张
            if(self.selectedIndexs.count > 0)
            {
                NSString *lastObj = self.selectedIndexs[self.selectedIndexs.count-1];
                if(self.lastSelectIndex == index)//index为取消元素的下标
                {
                    self.lastSelectIndex = [lastObj integerValue];
                }
            }
        }else {
            //是否已选中视频
            BOOL selectVideoStatus = [self selectVideoStatus];
            //如果之后允许多视频的话,只需要使用else部分
            if(selectVideoStatus){
                self.selectedIndexs  = [NSMutableArray arrayWithObject:indexStr];
            }else{
                //当前可选择数据的上限,单个视频或十张照片
                NSInteger maxCount = selectVideoStatus ? 1 : self.maxSelectCount;
                //数据上限
                if (maxCount <= self.selectedIndexs.count) {
                    //选择的照片达到上限
                    self.onMaxSelectCountCallback(@{});
                    return;
                }
                [self.selectedIndexs addObject:indexStr];
            }
            //多选模式下,新增照片是选中状态(白色蒙版)
            NSString *lastObj = self.selectedIndexs[self.selectedIndexs.count-1];
            self.lastSelectIndex = [lastObj integerValue];
        }
    }
    [self.collectionView reloadData];
    [self sendSelectPhotoDataToRN];
}

// 发送本地选择数据给RN
-(void)sendSelectPhotoDataToRN
{
    //当前选择的照片/视频变动后调用RN响应
    if (!self.onSelectedPhotoCallback) {
        return;
    }

    NSMutableArray *selectData = [NSMutableArray new];
    for(NSString *indexStr in self.selectedIndexs){
        AliyunAssetModel *model = self.viewDataArray[indexStr.integerValue];
        PHAsset *phAsset        = model.asset;
        NSString *filename      = [phAsset valueForKey:@"filename"];
        NSArray *resources      = [PHAssetResource assetResourcesForAsset:phAsset];
        NSString *photoURI      = [NSString stringWithFormat:@"ph://%@", phAsset.localIdentifier];
        NSString *localPath     = [(PHAssetResource*)resources[0] valueForKey:@"privateFileURL"];
        NSString *fileSize      = [(PHAssetResource*)resources[0] valueForKey:@"fileSize"];
        NSString *duration      = @(model.assetDuration*1000).stringValue;
        BOOL imageType          = phAsset.mediaType == PHAssetMediaTypeImage;
        NSString *type          = imageType ? @"image/" : @"video/";
        NSString *fileFormat    = [[[filename componentsSeparatedByString:@"."]lastObject]lowercaseString];
        NSString *rotation      = @"0";//视频和照片都会自动旋转,暂时拿不到角度
        NSArray *resourceArray = [PHAssetResource assetResourcesForAsset:phAsset];
        BOOL icloudFile = ![[resourceArray.firstObject valueForKey:@"locallyAvailable"] boolValue];
        NSDictionary *imageData = @{
            @"index":       @(selectData.count),//下标：选择的图片/视频数组的顺序
            @"width":       @(phAsset.pixelWidth),//该图片/视频的宽, 视频可能需要根据角度宽高对换
            @"height":      @(phAsset.pixelHeight),//该图片/视频的高
            @"path":         [NSString stringWithFormat:@"%@",localPath],//文件本地地址
            @"url":         [NSString stringWithFormat:@"%@",localPath],//兼容
            @"fileSize":    fileSize,//文件大小（字节大小）
            @"filename":    filename,//文件名称
            @"type":        [type stringByAppendingString:fileFormat],//"video/mp4" "image/jpeg"
            @"playableDuration":duration,// 视频时长,图片为0,视频为 ms
            @"rotation":    rotation,// 视频角度
            @"icloudFile":  @(icloudFile),//是否为iCloud
            @"uri":         photoURI?:@""
        };
        [selectData addObject:imageData];
    }
    if(selectData.count > 0){
        NSNumber *selectedIndex = @0;
        AliyunAssetModel *model = self.viewDataArray[self.lastSelectIndex];
        for(NSDictionary *imageData in selectData)
        {
            if([imageData[@"filename"] isEqualToString:[model.asset valueForKey:@"filename"]])
            {
                selectedIndex = imageData[@"index"];
                break;
            }
        }
        self.onSelectedPhotoCallback(@{@"selectedIndex":selectedIndex, @"data":selectData});
    }else{
        self.onSelectedPhotoCallback(@{@"selectedIndex":@(0), @"data":@[]});
    }
}

@end

