package com.rncamerakit.font

import com.aliyun.svideo.base.Form.FontForm

open abstract class IFontCallback {

    open fun onFontJsonInfo(mFontList: List<FontForm>?) {}

}