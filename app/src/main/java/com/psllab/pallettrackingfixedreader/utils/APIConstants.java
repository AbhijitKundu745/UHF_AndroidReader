package com.psllab.pallettrackingfixedreader.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class APIConstants {
    public static final String M_POST_INVENTORY = "/PDA/InsertTransactionDetails";
    public static final String M_DUMMY_POST_INVENTORY = "/PDA/InsertTagLoggerDetails";
    public static final String M_GET_WORK_ORDER_DETAILS = "/PDA/GetReaderWorkorderList";
    public static final String M_POST_CURRENT_PALLET = "/PDA/UploadCurrentPallet";
    public static final String M_POST_CURRENT_BIN = "/PDA/UploadCurrentBin";
    //public static final String M_HOST_URL = "https://grbservice.azurewebsites.net";
       public static final String M_HOST_URL = "http://192.168.1.12/GRBAPI";
    //public static final String M_HOST_URL = "http://192.168.74.40/GRB14";

    public static final int API_TIMEOUT = 60;
    public static final String DEVICE_ID = "ClientDeviceID";
    public static final String READER_STATUS = "ReaderStatus";
    public static final String ANTENA_ID = "AntennaID";
    public static final String RSSI = "RSSI";
    public static final String TRANSACTION_DATE_TIME = "TransDatetime";
    public static final String TOUCH_POINT_TYPE = "TouchPointType";
    public static final String COUNT = "Count";
    public static final String PALLET_TAG_ID = "PalletTagID";
    public static final String SUB_TAG_DETAILS = "SubTagDetails";
    public static final String SUB_TAG_ID = "TagID";
    public static final String SUB_TAG_CATEGORY_ID = "CategoryID";
    public static final String SUB_TAG_TYPE = "TagType";
    public static final String K_TOKEN_ERROR_400 = "Message";//error,Message
    public static final String TRANS_ID= "TransID";
    public static final String SUB_TRANS_ID= "TransID";
    public static final String DUMMY_TRANS_ID= "TransID";
    public static final String DUMMY_TAG_ID= "PalletTagID";
    public static final String DUMMY_TAG_DETAILS = "TagDetails";


    public static final String CURRENT_PALLET_NAME = "PalletName";
    public static final String CURRENT_SCANNED_PALLET_NAME = "CurrentScannedPalletName";
    public static final String CURRENT_SCANNED_PALLET_TAG_ID = "CurrentScannedPalletTagID";
    public static final String CURRENT_SCANNED_BIN_TAG_ID = "CurrentScannedBinTagID";
    public static final String CURRENT_PALLET_TAG_ID = "PalletTagID";
    public static final String LAST_UPDATED_DATE_TIME = "LastUpdatedDateTime";
    public static final String CURRENT_TEMP_STORAGE_NAME = "TemporaryStorageName";
    public static final String CURRENT_TEMP_STORAGE_TAG_ID = "TemporaryStorageTagID";
    public static final String CURRENT_LOADING_AREA_NAME = "LoadingAreaName";
    public static final String CURRENT_LOADING_AREA_TAG_ID = "LoadingAreaTagID";
    public static final String CURRENT_BIN_LOCATION_NAME = "BinLocation";
    public static final String CURRENT_BIN_LOCATION_TAG_ID = "BinLocationTagID";
    public static final String CURRENT_WORK_ORDER_LIST_ITEM_STATUS = "ListItemStatus";
    public static final String DATA = "data";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String READER_CONFIGURATION = "configuration";
    public static final String READER_CONFIGURATION_RSSI = "RSSI";
    public static final String READER_CONFIGURATION_POWER = "Power";
    public static final String READER_CONFIGURATION_POLLING_TIMER = "PollingTimer";
    public static final String START_WORK_ORDER = "StartWorkOrder";
    public static final String CURRENT_WORK_ORDER_NUMBER = "WorkorderNumber";
    public static final String CURRENT_WORK_ORDER_TYPE = "WorkorderType";
    public static final String CURRENT_WORK_ORDER_STATUS = "WorkorderStatus";
    public static String getSystemDateTimeInFormatt() {
        try {
            int year, monthformat, dateformat, sec;
            String da, mont, hor, min, yr, systemDate, secs;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            year = calendar.get(Calendar.YEAR);
            monthformat = calendar.get(Calendar.MONTH) + 1;
            dateformat = calendar.get(Calendar.DATE);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            sec = calendar.get(Calendar.SECOND);
            da = Integer.toString(dateformat);
            mont = Integer.toString(monthformat);
            hor = Integer.toString(hours);
            min = Integer.toString(minutes);
            secs = Integer.toString(sec);
            if (da.trim().length() == 1) {
                da = "0" + da;
            }
            if(mont.trim().equals("1")){
                mont = "01";
            }
            if(mont.trim().equals("2")){
                mont = "02";
            }
            if(mont.trim().equals("3")){
                mont = "03";
            }
            if(mont.trim().equals("4")){
                mont = "04";
            }
            if(mont.trim().equals("5")){
                mont = "05";
            }
            if(mont.trim().equals("6")){
                mont = "06";
            }
            if(mont.trim().equals("7")){
                mont = "07";
            }
            if(mont.trim().equals("8")){
                mont = "08";
            }
            if(mont.trim().equals("9")){
                mont = "09";
            }
            if(mont.trim().equals("10")){
                mont = "10";
            }
            if(mont.trim().equals("11")){
                mont = "11";
            }
            if(mont.trim().equals("12")){
                mont = "12";
            }

            if (hor.trim().length() == 1) {
                hor = "0" + hor;
            }
            if (min.trim().length() == 1) {
                min = "0" + min;
            }
            if (secs.trim().length() == 1) {
                secs = "0" + secs;
            }
            yr = Integer.toString(year);
            // systemDate = (da + mont + yr + hor + min + secs);
            systemDate = (yr + "-" + mont + "-" + da + " " + hor + ":" + min + ":" + secs);
            return systemDate;
        } catch (Exception e) {
            // return "01011970000000";
            // return "1970-01-01 00:00:00";
            return "1970-01-01 00:00:00";
        }
    }

    public static String getSystemDateTimeForBatchId() {
        try {
            int year, monthformat, dateformat, sec;
            String da, mont, hor, min, yr, systemDate, secs;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            year = calendar.get(Calendar.YEAR);
            monthformat = calendar.get(Calendar.MONTH) + 1;
            dateformat = calendar.get(Calendar.DATE);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            sec = calendar.get(Calendar.SECOND);
            da = Integer.toString(dateformat);
            mont = Integer.toString(monthformat);
            hor = Integer.toString(hours);
            min = Integer.toString(minutes);
            secs = Integer.toString(sec);
            if (da.trim().length() == 1) {
                da = "0" + da;
            }
            if(mont.trim().equals("1")){
                mont = "01";
            }
            if(mont.trim().equals("2")){
                mont = "02";
            }
            if(mont.trim().equals("3")){
                mont = "03";
            }
            if(mont.trim().equals("4")){
                mont = "04";
            }
            if(mont.trim().equals("5")){
                mont = "05";
            }
            if(mont.trim().equals("6")){
                mont = "06";
            }
            if(mont.trim().equals("7")){
                mont = "07";
            }
            if(mont.trim().equals("8")){
                mont = "08";
            }
            if(mont.trim().equals("9")){
                mont = "09";
            }
            if(mont.trim().equals("10")){
                mont = "10";
            }
            if(mont.trim().equals("11")){
                mont = "11";
            }
            if(mont.trim().equals("12")){
                mont = "12";
            }

            if (hor.trim().length() == 1) {
                hor = "0" + hor;
            }
            if (min.trim().length() == 1) {
                min = "0" + min;
            }
            if (secs.trim().length() == 1) {
                secs = "0" + secs;
            }
            yr = Integer.toString(year);
            // systemDate = (da + mont + yr + hor + min + secs);
            systemDate = (yr + "-" + mont + "-" + da + "-" + hor + ":" + min + ":" + secs);
            return systemDate;
        } catch (Exception e) {
            // return "01011970000000";
            // return "1970-01-01 00:00:00";
            return "1970-01-01-00:00:00";
        }
    }

    public static String getUTCSystemDateTimeInFormatt() {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            //Log.e("UTCDATETIME1",f.format(new Date()));
            // f.setTimeZone(TimeZone.getTimeZone("GMT"));
            System.out.println(f.format(new Date()));
            //Log.e("UTCDATETIME2",f.format(new Date()));
            String utcdatetime = f.format(new Date());
            return utcdatetime;
        } catch (Exception e) {
            // return "01011970000000";
            // return "1970-01-01 00:00:00";
            return "1970-01-01 00:00:00";
        }
    }

}
