package com.psllab.pallettrackingfixedreader.bean

import java.util.*

data class WorkOrderUploadTagBean(
    var batchId: String,
    var epcId: String,
    var workOrderNumber: String,
    var rssi: Int,
    var times: Int,
    var antenna: String,
    var additionalData: String,
    var tagType: String,
    var addedDateTime: String
) {
    constructor(tagBean: TagBean, workOrderNumber: String) : this(
        tagBean.batchId,
        tagBean.epcId,
        workOrderNumber,
        tagBean.rssi,
        tagBean.times,
        tagBean.antenna,
        tagBean.additionalData,
        tagBean.tagType,
        tagBean.addedDateTime
    )
}
