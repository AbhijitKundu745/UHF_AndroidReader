package com.psllab.pallettrackingfixedreader.bean;

public class WorkOrderListItem {
    String workorderNumber,workorderType,workorderStatus,palletName,palletTagId,lastUpdateDateTime,tempStorageName,tempStorageTagId,loadingAreaName,loadingAreaTagId,binLocationName,binLocationTagId,listItemStatus;

    public String getWorkorderNumber() {
        return workorderNumber;
    }

    public void setWorkorderNumber(String workorderNumber) {
        this.workorderNumber = workorderNumber;
    }
    public String getWorkorderType() {
        return workorderType;
    }

    public void setWorkorderType(String workorderType) {
        this.workorderType = workorderType;
    }
    public String getWorkorderStatus() {
        return workorderStatus;
    }

    public void setWorkorderStatus(String workorderStatus) {
        this.workorderStatus = workorderStatus;
    }
    public String getPalletName() {
        return palletName;
    }

    public void setPalletName(String palletName) {
        this.palletName = palletName;
    }

    public String getPalletTagId() {
        return palletTagId;
    }

    public void setPalletTagId(String palletTagId) {
        this.palletTagId = palletTagId;
    }

    public String getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(String lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    public String getTempStorageName() {
        return tempStorageName;
    }

    public void setTempStorageName(String tempStorageName) {
        this.tempStorageName = tempStorageName;
    }

    public String getTempStorageTagId() {
        return tempStorageTagId;
    }

    public void setTempStorageTagId(String tempStorageTagId) {
        this.tempStorageTagId = tempStorageTagId;
    }

    public String getLoadingAreaName() {
        return loadingAreaName;
    }

    public void setLoadingAreaName(String loadingAreaName) {
        this.loadingAreaName = loadingAreaName;
    }

    public String getLoadingAreaTagId() {
        return loadingAreaTagId;
    }

    public void setLoadingAreaTagId(String loadingAreaTagId) {
        this.loadingAreaTagId = loadingAreaTagId;
    }

    public String getBinLocationName() {
        return binLocationName;
    }

    public void setBinLocationName(String binLocationName) {
        this.binLocationName = binLocationName;
    }

    public String getBinLocationTagId() {
        return binLocationTagId;
    }

    public void setBinLocationTagId(String binLocationTagId) {
        this.binLocationTagId = binLocationTagId;
    }

    public String getListItemStatus() {
        return listItemStatus;
    }

    public void setListItemStatus(String listItemStatus) {
        this.listItemStatus = listItemStatus;
    }
}
