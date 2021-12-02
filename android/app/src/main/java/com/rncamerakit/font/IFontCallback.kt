package com.rncamerakit.font

import com.aliyun.svideo.base.Form.FontForm
import com.aliyun.svideosdk.common.struct.project.Source

open abstract class IFontCallback {

    open fun onFontJsonInfo(mFontList: List<FontForm>?) {}


    open fun onFontSource(source: Source) {}
}