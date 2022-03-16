/* eslint-disable no-unused-vars */
const fs = require('fs');
const path = require('path');

const resultMap = {};
const reg = /'[\u4e00-\u9fa5].*?'/g;
const likereg = /action: '(.*)'/g;
// const reg = />[\u4e00-\u9fa5].*?</g;
const angleRegleft = />[\u4e00-\u9fa5].*/g;

const likeMap = {
    '准备订阅': '准备订阅',
    '点赞': '点赞',
    '浏览post时间段': '浏览post时间段',
    '浏览post': '浏览post',
    'Post访问次数': 'Post访问次数',
    'Post分享': 'Post分享',
    '赞了你的作品': '赞了你的作品',
    'Post添加评论': 'Post添加评论',
    '取消订阅': '取消订阅',
    '点击续费订阅': '点击续费订阅',
    '达人分享': '达人分享',
    '视频播放完毕': '视频播放完毕',
    '详情页停留的时长': '详情页停留的时长'
  }


a={
    "摄像失败,请重试": "Camera_failed_please_try_again",
    "暂不设置": "Not_set_yet",
    "去设置": "go_to_settings",
    "点击拍照，长按拍视频": "Tap_to_take_a_photo_long_press_to_take_a_video",
    "无法在你的设备使用此贴纸！": "Cannot_use_this_sticker_on_your_device",
    "作品": "work",
    "快拍": "Snapshot",
    "滤镜": "Filter",
    "正在导出, 请不要离开": "Exporting_please_dont_leave",
    "柔柔": "soft",
    "无效果": "no_effect",
    "修剪": "prune",
    "封面": "cover",
    "视频时长不能超过5分钟": "The_length_of_the_video_cannot_exceed_5_minutes",
    "最多选择十张图片": "Select_up_to_ten_pictures",
    "请至少选择一个上传文件": "Please_select_at_least_one_upload_file",
    "加载中...": "Loading",
    "视频处理中...": "Video_processing",
    "快拍作品将在24小时后消失": "Story_works_will_disappear_after_24_hours",
    "简体中文": "Simplified_Chinese"
}
b={
    "美颜": "beauty",
    "继续": "continue",
    "请修剪视频,视频时长不能超过5分钟。": "Please_trim_the_video_the_length_of_the_video_cannot_exceed_5_minutes_",
    "最近相册": "Recent_Albums",
    "新作品": "New_product",
    "无网络连接": "No_internet_connection",
    "发布快拍": "Post_story",
    "滤镜": "filter",
    "取消": "Cancel",
    "背景音乐": "Background_music",
    "完成": "Finish",
    "搜索": "search",
    "配乐": "Soundtrack"
}
function walkSync(currentDirPath, callback) {
	fs.readdirSync(currentDirPath).forEach((name) => {
		const filePath = path.join(currentDirPath, name);
		const stat = fs.statSync(filePath);
		if (stat.isFile() && (filePath.endsWith('.js') || filePath.endsWith('.jsx') || filePath.endsWith('.ts') || filePath.endsWith('.tsx'))) {
			callback(filePath, stat);
		} else if (stat.isDirectory()) {
			walkSync(filePath, callback);
		}
	});
}
const getChinese = (filePath, stat) => {
	try {
		const data = fs.readFileSync(filePath, 'utf8');
        const resdata = data.replace(reg, (res,)=> {
            console.info(res, '哈哈哈')
            const rep = b[res.replace(/\'/g, '')]
            if (!rep || likeMap[rep]) return res
            return `\{\`\${I18n.t('${rep}')}\`\}`
        })
        fs.writeFileSync(filePath, resdata)
		const res = data.match(reg);
		if (res) {
			res.forEach((i) => {
				i = i.replace('action: ', '').replace(/\'/g, '').replace('>', '').replace('<', '');
                if (!likeMap[i]) {
                    resultMap[i] = i;
                }
			});
		}
	} catch (err) {
		console.error(err);
	}
};
function a1 (){
    result_zh = {}
    result_en = {}
    result_tool = {}
    
    a.forEach((i,index)=>{
        const key = i.replace(/\s/g, "_").replace(/\./g, '').replace(/\!/, '').replace(/'/g, '').replace(/\,/, '')
        result_en[key]= i
        result_zh[key]= b[index]
        result_tool[b[index]]= key
    })
}
walkSync('src/', getChinese);
console.info(Object.keys(resultMap));