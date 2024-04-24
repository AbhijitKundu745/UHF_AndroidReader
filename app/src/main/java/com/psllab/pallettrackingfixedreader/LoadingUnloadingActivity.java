package com.psllab.pallettrackingfixedreader;

import static com.seuic.uhfandroid.ext.DataExtKt.getCategoryID;
import static com.seuic.uhfandroid.ext.DataExtKt.getTagType;
import static com.seuic.uhfandroid.ext.DataExtKt.getTotalCounts;
import static com.seuic.uhfandroid.ext.DataExtKt.typeBean;
import static com.seuic.uhfandroid.ext.DataExtKt.typePallet;
import static com.seuic.uhfandroid.ext.DataExtKt.typeTemporaryStorage;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettrackingfixedreader.R;
import com.psl.pallettrackingfixedreader.databinding.ActivityLoadingUnloadingBinding;
import com.psllab.pallettrackingfixedreader.adapter.TagInfoAdapter;
import com.psllab.pallettrackingfixedreader.bean.TagBean;
import com.psllab.pallettrackingfixedreader.bean.WorkOrderListItem;
import com.psllab.pallettrackingfixedreader.bean.WorkOrderUploadTagBean;
import com.psllab.pallettrackingfixedreader.databases.DatabaseHandler;
import com.psllab.pallettrackingfixedreader.utils.APIConstants;
import com.psllab.pallettrackingfixedreader.utils.DataStoreUtils;
import com.psllab.pallettrackingfixedreader.utils.LoadingUnloadingActivityHelpers;
import com.psllab.pallettrackingfixedreader.utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoadingUnloadingActivity extends UHFActivity {

    SharedPreferencesUtils sharedPreferencesUtils;
    private ActivityLoadingUnloadingBinding binding;
    private Context context = this;
    private boolean isOtherWorkIsInProgress = false;
    private boolean isRfidReadingIsInProgress = false;
    private DatabaseHandler db;
    private Handler workOrderPollingApiHandler = new Handler();
    private Runnable workOrderPollingApiRunnable;
    private String CURRENT_WORK_ORDER_NUMBER = "";
    private String CURRENT_WORK_ORDER_TYPE = "";
    private String CURRENT_WORK_ORDER_STATUS = "";
    private boolean START_WORK_ORDER = false;
    private List<TagBean> dummyList = new ArrayList<>();

    List<WorkOrderListItem> workOrderListItemList = new ArrayList<>();
    boolean clearWorkOrderListItemList = false;


    private void stopWorkOrderPollingApiHandler() {
        // Remove any pending callbacks and messages
        workOrderPollingApiHandler.removeCallbacks(workOrderPollingApiRunnable);
    }

    private void startWorkOrderPollingApiHandler() {
        workOrderPollingApiRunnable = new Runnable() {
            @Override
            public void run() {
                // Do something after every 15 seconds
                workOrderPollingApiHandler.postDelayed(this, sharedPreferencesUtils.getPollingTimer());
                if (!isOtherWorkIsInProgress) {
                    Log.e("Call", "abcd");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getWorkOrderDetailsFromServer();
                        }
                    }).start();
                }

                if (dummyList.size() > 0) {
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                dummyAPICall();
                            }
                        }).start();
                    } catch (Exception exxx) {
                        Log.e("Exception in dummy API Call", exxx.getMessage());
                    }
                } else {
                    Log.e("No Data in dummyList", "5");
                }
                //TODO upload workorders
                if (db.getOfflineTagMasterCount() > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadWorkOrderItemToServer();
                        }
                    }).start();
                }
            }
        };
        // Post the initial Runnable with a delay of 2 seconds first time start handler after 2 seconds
        workOrderPollingApiHandler.postDelayed(workOrderPollingApiRunnable, 2000);
    }

    @Override
    protected void onDestroy() {
        // Stop the handler when the activity is destroyed
        stopWorkOrderPollingApiHandler();
        super.onDestroy();
    }

    private String androidID;
    private TagInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_loading_unloading);
        binding = ActivityLoadingUnloadingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        db = new DatabaseHandler(context);

        sharedPreferencesUtils = new SharedPreferencesUtils(context);

        androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        androidID = androidID.toUpperCase();
        Log.e("Device ID", "" + androidID);
        binding.deviceID.setText(androidID);

        adapter = new TagInfoAdapter(R.layout.layout_tag);
        adapter.notifyDataSetChanged();
        db.deleteTagMaster();
        //// db.deleteOfflineTagMaster();


        startWorkOrderPollingApiHandler();


        //Inventory is started and data is observed continously from below function
        getTagData().observe(this, new Observer<TagBean>() {
            @Override
            public void onChanged(TagBean bean) {
                if (bean != null) {
                    setListData(bean);
                }
            }
        });


        binding.btnParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnParent.setText("Parent Pallet");
            }
        });

        binding.rlvEpc.setAdapter(adapter);
        binding.rlvEpc.setLayoutManager(
                new LinearLayoutManager(LoadingUnloadingActivity.this));

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTagData().postValue(null);
                adapter.notifyDataSetChanged();
                db.deleteTagMaster();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("START INV", "HERE111");
                        startInventory();
                    }
                }, 1000);
            }
        });

        binding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTagData().postValue(null);
                adapter.notifyDataSetChanged();
                Log.e("STOPRFID","FROM STOP BUTTON: MANUAL");
                stopInventory();
                isRfidReadingIsInProgress = false;
                getTotalCounts().set(0);
            }
        });
        binding.power.setText(""+sharedPreferencesUtils.getPower());
    }

    boolean considerNextTagId = true;
    private void setListData(TagBean bean1) {
        if (bean1 != null) {
            TagBean myBean = bean1;
            if(considerNextTagId){
                considerNextTagId = false;
                Log.e("STOPRFID","RFID TAG ID:"+bean1.getEpcId());

                String epcId = myBean.getEpcId();
                final int rssiValue0 = myBean.getRssi();
                int rssiValue = rssiValue0;
                String antenaId = myBean.getAntenna();

                isRfidReadingIsInProgress = true;
                int rssi = rssiValue;
                if (rssi < 0) {
                    rssi = (-1) * rssi;
                    rssiValue = rssi;
                }
                else{

                }
                myBean.setRssi(rssi);
                dummyList.add(myBean);
                adapter.notifyDataSetChanged();
                Log.e("STOPRFID:tagid", "" + epcId);
                Log.e("STOPRFID:ANTENA", "" + antenaId);
                if (workOrderListItemList != null) {
                    if (workOrderListItemList.size() == 0) {
                        Log.e("STOPRFID","WORKORDER LIST ZERO");
                        stopInventory();
                        isRfidReadingIsInProgress = false;
                        getTagData().postValue(null);
                        adapter.notifyDataSetChanged();
                        getTotalCounts().set(0);

                        db.deleteTagMaster();
                        getTagData().postValue(null);
                        adapter.notifyDataSetChanged();
                        getTotalCounts().set(0);
                        return;
                    }
                }
                if (rssi > sharedPreferencesUtils.getRssi()) {
                    //return;
                }

                //add only those tags which have stronger rssi than configured rssi
                // if (rssi <= sharedPreferencesUtils.getRssi()) {
                //set positive rssi value to bean

                //check here from database
                //if already present then update it accordingly else add it according to logic
                //if pallet tag then if already present then check for time diff if time diff is < x sec then update it else delete all data and check for new pallet tag

                //check current tag is present in workorder list
                if (!LoadingUnloadingActivityHelpers.isEpcPresentInWorkOrder(epcId, workOrderListItemList)) {
                    //Wrong Tag Found, Start Long Buzzer
                    // buzzerForWrongTag();
                    // return;
                }
                List<TagBean> lst = new ArrayList<>();
                lst.add(myBean);
                if (myBean.getTagType().equalsIgnoreCase(typePallet)) {

                    // if(!LoadingUnloadingActivityHelpers.isWorkOrderItemStatusIsPending(bean.getEpcId(),workOrderListItemList)){
                    if (!LoadingUnloadingActivityHelpers.isPalletTagIdPresentInWorkOrder(epcId, workOrderListItemList)) {
                        //this pallet is already completed
                        //buzzerForWrongTag();
                        int finalRssiValue = rssiValue;
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                sendCurrentScannedPallet(epcId, finalRssiValue);
//                            }
//                        }).start();
                        return;
                    }
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                        Date currentDateTime = new Date();

                        String currentDate = simpleDateFormat.format(currentDateTime);
                        String previousDate = db.getAddedDateTimeForPalletEPC();
                        if (previousDate.equalsIgnoreCase("")) {
                            previousDate = currentDate;
                        }

                        Date date1 = simpleDateFormat.parse(previousDate);
                        Date date2 = simpleDateFormat.parse(currentDate);

                        long different = date1.getTime() - date2.getTime();
                        long secondsInMilli = 1000;
                        long elapsedSeconds = different / secondsInMilli;
                        if (elapsedSeconds < 0) {
                            elapsedSeconds = elapsedSeconds * -1;
                        }
                        if (elapsedSeconds > DataStoreUtils.getINSTANCE(context).getPalletTagConfigTime()) {
                            if (db.getTagMasterCount() <= 1) {
                                db.deleteTagMaster();
                                db.storeTagMaster(lst);
                                int finalRssiValue1 = rssiValue;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendCurrentScannedPallet(epcId, finalRssiValue1);
                                    }
                                }).start();
                            }
                        }
                    } catch (ParseException e) {
                        Log.e("EXC", "" + e.getMessage());
                        e.printStackTrace();
                        Log.e("PARSEEXC", "" + e.getMessage());
                    }

                    if (db.isPalletTagPresent()) {
                        if (db.isEpcPresent(epcId)) {
                            //pallet tag is present and it is old one then do nothing only update that tag data
                            db.deletePalletTag(epcId);
                            db.storeTagMaster(lst);
                        } else {
                            //Other pallet tag found, Now here need to check RSSI,and also check if other tags are scanned,,,, give rssi high preference, here other pallet tag is scanned
                            try {
                                long tagCount = db.getTagMasterCount();
                                //Replace pallet tag only if other tags are not scanned
                                if (tagCount == 1) {
                                    String oldRSSIString = db.getRSSIPalletEPC();
                                    int oldRSSI = Integer.parseInt(oldRSSIString);
                                    int newRSSI = rssiValue;
                                    if (oldRSSI < 0) {
                                        oldRSSI = (-1) * oldRSSI;
                                    }else{
                                        oldRSSI = oldRSSI;
                                    }

                                    if (newRSSI < 0) {
                                        newRSSI = (-1) * newRSSI;
                                    }else{
                                        newRSSI = newRSSI;
                                    }
                                    if (newRSSI <= oldRSSI) {
                                        //replace only new pallet tag with old pallet tag as he has greater RSSI and keep child tags as it is in transaction
                                        db.deletePalletTag(epcId);
                                        db.storeTagMaster(lst);
                                        int finalRssiValue2 = rssiValue;
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                sendCurrentScannedPallet(epcId, finalRssiValue2);
                                            }
                                        }).start();
                                    }
                                }
                            } catch (Exception ex) {
                                Log.e("RSSIEXC", "" + ex.getMessage());
                            }
                        }

                    } else {
                        //no old pallet data found now add this in list .
                        db.storeTagMaster(lst);
                        adapter.setList(db.getAllTagData());
                        adapter.notifyDataSetChanged();
                        int finalRssiValue3 = rssiValue;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendCurrentScannedPallet(epcId, finalRssiValue3);
                            }
                        }).start();
                    }
                } else {
                    if (db.isPalletTagPresent()) {
                        //check here type of workorder and then get corrosponding child tags.
                        // and check if current tag is corresponding to pallet tag then do action
                        String palletTag = db.getPalletTag().getEpcId();
                        if (palletTag != null && !palletTag.equalsIgnoreCase("")) {
                            switch (CURRENT_WORK_ORDER_TYPE) {
                                case "":
                                    break;
                                case "U0":
                                    String u0LoadingAreaTagId = LoadingUnloadingActivityHelpers.getU0LoadingAreaTagIdForPallet(palletTag, workOrderListItemList);
                                    if (!epcId.equalsIgnoreCase(u0LoadingAreaTagId)) {
                                        //buzzerForWrongTag();
                                        Log.e("STOPRFID","AREA TAG NOT IN WORK ORDER U0:"+epcId);
                                        return;
                                    }
                                    //add tag into db and take action.
                                    addTagAndTakeAction(lst, "U0");

                                    break;
                                case "U1":
                                    String u1BinLocationTagId = LoadingUnloadingActivityHelpers.getU1BinTagIdForPallet(palletTag, workOrderListItemList);

                                    if (!epcId.equalsIgnoreCase(u1BinLocationTagId)) {
                                        //buzzerForWrongTag();
                                        Log.e("STOPRFID","BIN TAG NOT IN WORK ORDER U1:"+epcId);

                                        return;
                                    }
                                    //add tag into db and take action.
                                    addTagAndTakeAction(lst, "U1");
                                    break;
                                case "L0":
                                    // String l0TempStorageTagId = LoadingUnloadingActivityHelpers.getL0TempStorageTagIdForPallet(palletTag,workOrderListItemList);

                                    // if(!getTagType(l0TempStorageTagId).equalsIgnoreCase(typeTemporaryStorage)){
                                    //buzzerForWrongTag();
                                    //   return;
                                    // }
                                    if (getTagType(epcId).equalsIgnoreCase(typeTemporaryStorage)) {
                                        addTagAndTakeAction(lst, "L0");
                                    }
                                    //add tag into db and take action.
                                    break;
                                case "L1":
                                    String l1LoadingAreaTagId = LoadingUnloadingActivityHelpers.getL1LoadingAreaTagIdForPallet(palletTag, workOrderListItemList);
                                    if (!epcId.equalsIgnoreCase(l1LoadingAreaTagId)) {
                                        //buzzerForWrongTag();
                                        Log.e("STOPRFID","AREA TAG NOT IN WORK ORDER L1:"+epcId);

                                        return;
                                    }
                                    //add tag into db and take action.
                                    addTagAndTakeAction(lst, "L1");
                                    break;
                                case "I0":
                                    if(getTagType(epcId).equalsIgnoreCase(typeBean)){
                                        addTagAndTakeAction(lst, "I0");
                                    }
                                    break;
                            }
                        }
                    }
                    if(myBean.getTagType().equalsIgnoreCase(typeBean)){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendCurrentScannedBin(epcId);
                            }
                        }).start();
                    }
                }


                // }
            }
            considerNextTagId = true;

        }
    }

    private void addTagAndTakeAction(List<TagBean> lst, String workOrderType) {
        db.storeTagMaster(lst);
        adapter.setList(db.getAllTagData());
        adapter.notifyDataSetChanged();
        getTotalCounts().set(0);//changed by Abhijit wait
        //buzzerForCorrectTag();

        List<TagBean> allTags = db.getAllTagData();
        List<WorkOrderUploadTagBean> allTagsOffline1 = new ArrayList<>();
        String batchId = APIConstants.getSystemDateTimeForBatchId();
        for (int i = 0; i < allTags.size(); i++) {
            TagBean bean1 = allTags.get(i);
            bean1.setBatchId(batchId);

            String workOrderNumber = CURRENT_WORK_ORDER_NUMBER;
            workOrderType = CURRENT_WORK_ORDER_TYPE;
            WorkOrderUploadTagBean workOrderUploadTagBean = new WorkOrderUploadTagBean(bean1, workOrderNumber);
            Log.e("WokrUpload", workOrderUploadTagBean.getWorkOrderNumber());
            allTagsOffline1.add(workOrderUploadTagBean);
        }
        db.storeOfflineTagMaster(allTagsOffline1);
        //update status of listitem
        workOrderListItemList = LoadingUnloadingActivityHelpers.getUpdatedWorkOrderList(workOrderListItemList, db.getPalletTag().getEpcId(), "Completed");
        db.deleteTagMaster();//changed
        adapter.setList(db.getAllTagData());
        getTotalCounts().set(0);//changed by Abhijit
        adapter.notifyDataSetChanged();
        if (LoadingUnloadingActivityHelpers.isWorkOrderCompleted(workOrderListItemList)) {
            Log.e("STOPRFID","WORK ORDER COMPLETED");
            stopInventory();
            isRfidReadingIsInProgress = false;
            db.deleteTagMaster();
            getTagData().postValue(null);
            adapter.notifyDataSetChanged();
            getTotalCounts().set(0);//changed by Abhijit

            db.deleteTagMaster();

            if (workOrderListItemList != null) {
                workOrderListItemList.clear();
            }
            CURRENT_WORK_ORDER_TYPE = "";
            CURRENT_WORK_ORDER_NUMBER = "";
            CURRENT_WORK_ORDER_STATUS = "";
        } else {

        }
    }

    private void sendCurrentScannedPallet(String palletTagId,int rssi) {
        try {
            if(rssi<0){
                rssi =  (-1)*rssi;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.DEVICE_ID, androidID);
            //jsonObject.put(APIConstants.CURRENT_WORK_ORDER_NUMBER, CURRENT_WORK_ORDER_NUMBER);
            jsonObject.put(APIConstants.CURRENT_SCANNED_PALLET_TAG_ID, palletTagId);

            String palletName = LoadingUnloadingActivityHelpers.getPalletNameByPalletTagId(palletTagId, workOrderListItemList);
            jsonObject.put(APIConstants.CURRENT_SCANNED_PALLET_NAME, palletName);
            jsonObject.put(APIConstants.RSSI, rssi);
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            String Url = APIConstants.M_HOST_URL + APIConstants.M_POST_CURRENT_PALLET;
            AndroidNetworking.post(APIConstants.M_HOST_URL + APIConstants.M_POST_CURRENT_PALLET).addJSONObjectBody(jsonObject)
                    .setTag("test")
                    //.addHeaders("Authorization",SharedPreferencesManager.getAccessToken(context))
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject result) {
                            Log.e("result", result.toString());
                            isOtherWorkIsInProgress = false;
                            try {
                                if (result.getString(APIConstants.STATUS).equalsIgnoreCase("true")) {

                                }
                            } catch (JSONException e) {
                                // throw new RuntimeException(e);
                            }
                        }
                        @Override
                        public void onError(ANError anError) {
                            Log.e("error", anError.getErrorDetail());
                            Log.e("errorcode", "" + anError.getErrorCode());
                            isOtherWorkIsInProgress = false;
                        }
                    });
            Log.e("URL", "" + Url);
            Log.e("URL", jsonObject.toString());
        } catch (JSONException e) {
            Log.e("Exception", e.getMessage());
        }
    }

    private void getWorkOrderDetailsFromServer() {
        try {
            isOtherWorkIsInProgress = true;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.DEVICE_ID, androidID);
            jsonObject.put(APIConstants.READER_STATUS, "1");

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            String Url = APIConstants.M_HOST_URL + APIConstants.M_GET_WORK_ORDER_DETAILS;

            AndroidNetworking.post(APIConstants.M_HOST_URL + APIConstants.M_GET_WORK_ORDER_DETAILS).addJSONObjectBody(jsonObject)
                    .setTag("test")
                    //.addHeaders("Authorization",SharedPreferencesManager.getAccessToken(context))
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject result) {
                            Log.e("result", result.toString());
                            isOtherWorkIsInProgress = false;
                            try {
                                if (result.getString(APIConstants.STATUS).equalsIgnoreCase("true")) {
                                    if (result.has(APIConstants.READER_CONFIGURATION)) {
                                        JSONObject configObject = result.getJSONObject(APIConstants.READER_CONFIGURATION);
                                        if (configObject.has(APIConstants.READER_CONFIGURATION_RSSI)) {
                                            String rssiStr = configObject.getString(APIConstants.READER_CONFIGURATION_RSSI);
                                            sharedPreferencesUtils.setRssi(Integer.parseInt(rssiStr));
                                        }
                                        if (configObject.has(APIConstants.READER_CONFIGURATION_POWER)) {
                                            String power = configObject.getString(APIConstants.READER_CONFIGURATION_POWER);
                                            sharedPreferencesUtils.setPower(Integer.parseInt(power));
                                            if (!isRfidReadingIsInProgress) {
                                                setPower(Integer.parseInt(power));
                                            }
                                        }
                                        if (configObject.has(APIConstants.READER_CONFIGURATION_POLLING_TIMER)) {
                                            int pollingTimer = configObject.getInt(APIConstants.READER_CONFIGURATION_POLLING_TIMER);
                                            sharedPreferencesUtils.setPollingTimer(pollingTimer);
                                        }
                                        boolean startOperation = true;
                                        if (result.has(APIConstants.DATA)) {
                                            try {
                                                JSONArray dataArray = result.getJSONArray(APIConstants.DATA);
                                                if (dataArray != null) {
                                                    if (dataArray.length() == 0) {
                                                        startOperation = false;
                                                    }
                                                } else {
                                                    startOperation = false;
                                                }
                                                Log.e("result2", "result2 passed");
                                            } catch (JSONException ex) {
                                                startOperation = false;
                                                Log.e("result1", ex.getMessage());
                                            }
                                        }
                                        if (result.has(APIConstants.START_WORK_ORDER)) {
                                            boolean startWorkorder = result.getBoolean(APIConstants.START_WORK_ORDER);
                                            if (startWorkorder) {
                                                Log.e("result3", "started workorder");
                                                START_WORK_ORDER = true;
                                            } else {
                                                START_WORK_ORDER = false;
                                            }
                                        }
                                        if (result.has(APIConstants.DATA)) {
                                            Log.e("result9", "get data array");
                                            JSONArray dataArray = result.getJSONArray(APIConstants.DATA);
                                            Log.e("result9", "got data array");
                                            if (dataArray.length() > 0) {
                                                for (int i = 0; i < dataArray.length(); i++) {
                                                    JSONObject dataObject = dataArray.getJSONObject(i);
                                                    Log.e("result8", String.valueOf(i));
                                                    WorkOrderListItem workOrderListItem = new WorkOrderListItem();
                                                    String workorderNumber = "";
                                                    String workorderType = "";
                                                    String workorderStatus = "";
                                                    String palletName = "";
                                                    String palletTagId = "";
                                                    String lastUpdateDateTime = "";
                                                    String tempStorageName = "";
                                                    String tempStorageTagId = "";
                                                    String loadingAreaName = "";
                                                    String loadingAreaTagId = "";
                                                    String binLocationName = "";
                                                    String binLocationTagId = "";
                                                    String listItemStatus = "";

                                                    if (dataObject.has(APIConstants.CURRENT_WORK_ORDER_STATUS)) {
                                                        Log.e("result 3.1", "get work order status");
                                                        workorderStatus = dataObject.getString(APIConstants.CURRENT_WORK_ORDER_STATUS);
                                                        Log.e("result 3.2", "get work order status");
                                                        // if (workorderStatus!=null && !workorderStatus.equalsIgnoreCase(CURRENT_WORK_ORDER_STATUS)) {
                                                        if (workorderStatus != null) {
                                                            CURRENT_WORK_ORDER_STATUS = workorderStatus;
                                                            Log.e("WorkorderStatus1", workorderStatus);
                                                        } else {
                                                            Log.e("result6", "workorderstatus is null");
                                                        }
                                                    } else {
                                                        Log.e("result4", "not found work order statuds");
                                                    }
                                                    if (dataObject.has(APIConstants.CURRENT_WORK_ORDER_NUMBER)) {

                                                        workorderNumber = dataObject.getString(APIConstants.CURRENT_WORK_ORDER_NUMBER);
                                                        if (workorderNumber != null && !workorderNumber.equalsIgnoreCase("")) {
                                                            //clearWorkOrderListItemList = true;
                                                            CURRENT_WORK_ORDER_NUMBER = workorderNumber;
                                                            //  if (CURRENT_WORK_ORDER_STATUS.equalsIgnoreCase("A") || CURRENT_WORK_ORDER_STATUS.equalsIgnoreCase("R")) {
                                                            if (START_WORK_ORDER && startOperation) {
                                                                START_WORK_ORDER = false;
                                                                if (!isRfidReadingIsInProgress) {
                                                                    db.deleteTagMaster();
                                                                    Log.e("STOPRFID","SERVER SAYS START  AND RF NOT ON");
                                                                    stopInventory();
                                                                    getTagData().postValue(null);
                                                                    adapter.notifyDataSetChanged();
                                                                    db.deleteTagMaster();
                                                                    new Handler().postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Log.e("START INV", "HERE111");
                                                                            startInventory();
                                                                        }
                                                                    }, 1000);

                                                                } else {
                                                                    Log.e("stp", "HERE111");
                                                                    Log.e("STOPRFID","SERVER SAYS START  AND RF ALREADY ON THEN STOP AND AGAIN START");
                                                                    stopInventory();
                                                                    isRfidReadingIsInProgress = false;
                                                                    getTagData().postValue(null);
                                                                    adapter.notifyDataSetChanged();
                                                                    getTotalCounts().set(0);//changed by Abhijit

                                                                    db.deleteTagMaster();
                                                                    getTagData().postValue(null);
                                                                    adapter.notifyDataSetChanged();
                                                                    getTotalCounts().set(0);//changed by Abhijit
                                                                    new Handler().postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Log.e("START INV", "HERE111");
                                                                            startInventory();
                                                                        }
                                                                    }, 1000);
                                                                }
                                                            } else {
                                                                Log.e("STOPRFID","SERVER SAYS STOP");

                                                                stopInventory();
                                                                isRfidReadingIsInProgress = false;
                                                                getTagData().postValue(null);
                                                                adapter.notifyDataSetChanged();
                                                                getTotalCounts().set(0);//changed by Abhijit

                                                                db.deleteTagMaster();
                                                                getTagData().postValue(null);
                                                                adapter.notifyDataSetChanged();
                                                                getTotalCounts().set(0);//changed by Abhijit
                                                            }
                                                            // }
                                                        } else {
                                                            if (db.getTagMasterCount() == 0) {
                                                                // stopInventory();
                                                                //isRfidReadingIsInProgress = false;
                                                                adapter.notifyDataSetChanged();
                                                                getTotalCounts().set(0);//changed by Abhijit
                                                                getTagData().postValue(null);
                                                            } else {
                                                                //automatically stops after transaction gets completed.
                                                            }
                                                        }

                                                    }
                                                    if (dataObject.has(APIConstants.CURRENT_WORK_ORDER_TYPE)) {
                                                        workorderType = dataObject.getString(APIConstants.CURRENT_WORK_ORDER_TYPE);
                                                        if (workorderType != null) {
                                                            CURRENT_WORK_ORDER_TYPE = workorderType;
                                                        }
                                                    }

                                                    if (dataObject.has(APIConstants.CURRENT_PALLET_NAME))
                                                        palletName = dataObject.getString(APIConstants.CURRENT_PALLET_NAME);

                                                    if (dataObject.has(APIConstants.CURRENT_PALLET_TAG_ID))
                                                        palletTagId = dataObject.getString(APIConstants.CURRENT_PALLET_TAG_ID);

                                                    if (dataObject.has(APIConstants.LAST_UPDATED_DATE_TIME))
                                                        lastUpdateDateTime = dataObject.getString(APIConstants.LAST_UPDATED_DATE_TIME);

                                                    if (dataObject.has(APIConstants.CURRENT_TEMP_STORAGE_NAME))
                                                        tempStorageName = dataObject.getString(APIConstants.CURRENT_TEMP_STORAGE_NAME);

                                                    if (dataObject.has(APIConstants.CURRENT_TEMP_STORAGE_TAG_ID))
                                                        tempStorageTagId = dataObject.getString(APIConstants.CURRENT_TEMP_STORAGE_TAG_ID);

                                                    if (dataObject.has(APIConstants.CURRENT_LOADING_AREA_NAME))
                                                        loadingAreaName = dataObject.getString(APIConstants.CURRENT_LOADING_AREA_NAME);

                                                    if (dataObject.has(APIConstants.CURRENT_LOADING_AREA_TAG_ID))
                                                        loadingAreaTagId = dataObject.getString(APIConstants.CURRENT_LOADING_AREA_TAG_ID);

                                                    if (dataObject.has(APIConstants.CURRENT_BIN_LOCATION_NAME))
                                                        binLocationName = dataObject.getString(APIConstants.CURRENT_BIN_LOCATION_NAME);

                                                    if (dataObject.has(APIConstants.CURRENT_BIN_LOCATION_TAG_ID))
                                                        binLocationTagId = dataObject.getString(APIConstants.CURRENT_BIN_LOCATION_TAG_ID);

                                                    if (dataObject.has(APIConstants.CURRENT_WORK_ORDER_LIST_ITEM_STATUS))
                                                        listItemStatus = dataObject.getString(APIConstants.CURRENT_WORK_ORDER_LIST_ITEM_STATUS);

                                                    workOrderListItem.setPalletName(palletName);
                                                    workOrderListItem.setPalletTagId(palletTagId);
                                                    workOrderListItem.setWorkorderNumber(workorderNumber);
                                                    workOrderListItem.setWorkorderType(workorderType);
                                                    workOrderListItem.setWorkorderStatus(workorderStatus);
                                                    workOrderListItem.setLastUpdateDateTime(lastUpdateDateTime);
                                                    workOrderListItem.setTempStorageName(tempStorageName);
                                                    workOrderListItem.setTempStorageTagId(tempStorageTagId);
                                                    workOrderListItem.setLoadingAreaName(loadingAreaName);
                                                    workOrderListItem.setLoadingAreaTagId(loadingAreaTagId);
                                                    workOrderListItem.setBinLocationName(binLocationName);
                                                    workOrderListItem.setBinLocationTagId(binLocationTagId);
                                                    workOrderListItem.setListItemStatus(listItemStatus);

                                                    workOrderListItemList.add(workOrderListItem);
                                                }
                                            } else {
                                                Log.e("result7", "no data array");
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("STOPRFID","SERVER RESULT FALSE FOR API");

                                    stopInventory();
                                    isRfidReadingIsInProgress = false;
                                    db.deleteTagMaster();
                                    if (workOrderListItemList != null) {
                                        workOrderListItemList.clear();
                                    }
                                    CURRENT_WORK_ORDER_TYPE = "";
                                    CURRENT_WORK_ORDER_NUMBER = "";
                                    CURRENT_WORK_ORDER_STATUS = "";
                                    getTagData().postValue(null);
                                    adapter.notifyDataSetChanged();
                                    getTotalCounts().set(0);//changed by Abhijit
                                }
                            } catch (JSONException e) {
                                Log.e("EXC1", e.getMessage());
                                // throw new RuntimeException(e);
                            }
                        }
                        @Override
                        public void onError(ANError anError) {
                            Log.e("error", anError.getErrorDetail());
                            Log.e("errorcode", "" + anError.getErrorCode());
                            isOtherWorkIsInProgress = false;
                        }
                    });
            Log.e("URL", "" + Url);
            Log.e("URL", jsonObject.toString());
        } catch (JSONException e) {
            Log.e("Exception", e.getMessage());
        }
    }

    private void uploadWorkOrderItemToServer() {
        try {
            String topBatchId = db.getTopBatchId();
            if (topBatchId == null) {
                return;
            }
            WorkOrderUploadTagBean bean = db.getPalletTagForBatchId(topBatchId);
            Log.e("BEFORE4", topBatchId);
            if (bean == null) {
                db.deleteOfflineTagMasterForBatch(topBatchId);
                Log.e("INWORK", "4");
                return;
            }
            String palletTagId = bean.getEpcId();
            String workOrderNumber = bean.getWorkOrderNumber();
            String workOrderType = CURRENT_WORK_ORDER_TYPE;
            String listItemStatus = "Completed";
            String palletTagRssi = "" + bean.getRssi();
            String palletTagCount = "" + getTotalCounts().incrementAndGet();
            String TransID = UUID.randomUUID().toString();
            Log.e("COUNT", palletTagCount);
            String palletTagAntenaId = "" + bean.getAntenna();
            //String date_time = APIConstants.getSystemDateTimeInFormatt();
            String date_time = "" + bean.getAddedDateTime();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.DEVICE_ID, androidID);
            jsonObject.put(APIConstants.TRANS_ID, TransID);
            jsonObject.put(APIConstants.CURRENT_WORK_ORDER_NUMBER, workOrderNumber);
            jsonObject.put(APIConstants.CURRENT_WORK_ORDER_LIST_ITEM_STATUS, listItemStatus);
            jsonObject.put(APIConstants.CURRENT_WORK_ORDER_TYPE, workOrderType);
            jsonObject.put(APIConstants.RSSI, palletTagRssi);
            jsonObject.put(APIConstants.TRANSACTION_DATE_TIME, date_time);
            jsonObject.put(APIConstants.COUNT, palletTagCount);
            jsonObject.put(APIConstants.PALLET_TAG_ID, palletTagId);
            jsonObject.put(APIConstants.ANTENA_ID, palletTagAntenaId);
            jsonObject.put(APIConstants.SUB_TAG_CATEGORY_ID, getCategoryID(palletTagId));

            jsonObject.put(APIConstants.TOUCH_POINT_TYPE, "T");
            JSONArray tagDetailsArray = new JSONArray();
            List<WorkOrderUploadTagBean> allTags = db.getAllTagDataForBatch(topBatchId);
            for (int i = 0; i < allTags.size(); i++) {
                JSONObject obj = new JSONObject();
                WorkOrderUploadTagBean tagBean = allTags.get(i);
                //obj.put(APIConstants.SUB_TRANS_ID,TransID);
                obj.put(APIConstants.SUB_TAG_ID, tagBean.getEpcId());
                obj.put(APIConstants.COUNT, "" + getTotalCounts().incrementAndGet());
                obj.put(APIConstants.RSSI, "" + tagBean.getRssi());
                obj.put(APIConstants.SUB_TAG_CATEGORY_ID, getCategoryID(tagBean.getEpcId()));
                obj.put(APIConstants.SUB_TAG_TYPE, "" + tagBean.getTagType());
                obj.put(APIConstants.TRANSACTION_DATE_TIME, "" + tagBean.getAddedDateTime());
                if (!tagBean.getTagType().equalsIgnoreCase(typePallet)) {
                    tagDetailsArray.put(obj);
                }
            }
            jsonObject.put(APIConstants.SUB_TAG_DETAILS, tagDetailsArray);
            //jsonObject.put(APIConstants.K_ASSET_SERIAL_NUMBER,serialnumber);
            Log.e("OFFLINEDATA", jsonObject.toString());
            postInventoryData(topBatchId, jsonObject);
        } catch (JSONException e) {
            Log.e("INWORK", "5");
            Log.e("Exception", e.getMessage());
        }
    }

    public void postInventoryData(String batchId, final JSONObject loginRequestObject) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        String Url = APIConstants.M_HOST_URL + APIConstants.M_POST_INVENTORY;

        AndroidNetworking.post(APIConstants.M_HOST_URL + APIConstants.M_POST_INVENTORY).addJSONObjectBody(loginRequestObject)
                .setTag("test")
                //.addHeaders("Authorization",SharedPreferencesManager.getAccessToken(context))
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.e("result", result.toString());

                        try {
                            if (result.getString("status").equalsIgnoreCase("true")) {
                                db.deleteOfflineTagMasterForBatch(batchId);
                            }
                        } catch (JSONException e) {
                            // throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        Log.e("error", anError.getErrorDetail());
                        Log.e("errorcode", "" + anError.getErrorCode());
                    }
                });
        Log.e("URL", "" + Url);
        Log.e("URL", loginRequestObject.toString());
    }

    private void dummyAPICall() {
        try {
            if (dummyList.size() > 0) {
                //TODO CALL API
                try {
                    JSONObject jsonObject1 = new JSONObject();
                    JSONArray tagDetailsArray1 = new JSONArray();
                    List<TagBean> allTags1 = dummyList;
                    for (int i = 0; i < allTags1.size(); i++) {
                        JSONObject obj1 = new JSONObject();
                        TagBean tagBean1 = allTags1.get(i);
                        obj1.put(APIConstants.DUMMY_TAG_ID, tagBean1.getEpcId());
                        obj1.put(APIConstants.ANTENA_ID, tagBean1.getAntenna());
                        obj1.put(APIConstants.DEVICE_ID, androidID);
                        obj1.put(APIConstants.COUNT, "" + getTotalCounts().incrementAndGet());
                        obj1.put(APIConstants.RSSI, "" + tagBean1.getRssi());
                        obj1.put(APIConstants.TRANSACTION_DATE_TIME, "" + tagBean1.getAddedDateTime());
                        tagDetailsArray1.put(obj1);
                    }
                    jsonObject1.put(APIConstants.DUMMY_TAG_DETAILS, tagDetailsArray1);
                    postData(jsonObject1);
                    Log.e("Logger Posting Complete", jsonObject1.toString());

                } catch (Exception ex) {
                    Log.e("Exception", ex.getMessage());
                }
            } else{
                Log.e("No Tag data found", "5");
            }
        } catch (Exception ex) {
        }
    }

    public void postData(final JSONObject dummyObject) {

        try {
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            String Url = APIConstants.M_HOST_URL + APIConstants.M_DUMMY_POST_INVENTORY;

            AndroidNetworking.post(APIConstants.M_HOST_URL + APIConstants.M_DUMMY_POST_INVENTORY).addJSONObjectBody(dummyObject)
                    .setTag("test")

                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject result) {

                            if (dummyList != null) {
                                dummyList.clear();
                            }
                        }
                        @Override
                        public void onError(ANError anError) {

                        }
                    });
        } catch (Exception exx) {
        }

    }
    private void sendCurrentScannedBin(String binTagId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.DEVICE_ID, androidID);
            jsonObject.put(APIConstants.CURRENT_SCANNED_BIN_TAG_ID, binTagId);
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            String Url = APIConstants.M_HOST_URL + APIConstants.M_POST_CURRENT_BIN;
            AndroidNetworking.post(APIConstants.M_HOST_URL + APIConstants.M_POST_CURRENT_BIN).addJSONObjectBody(jsonObject)
                    .setTag("test")
                    //.addHeaders("Authorization",SharedPreferencesManager.getAccessToken(context))
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject result) {
                            Log.e("resultCurrentBin", result.toString());
                            isOtherWorkIsInProgress = false;
                            try {
                                if (result.getString(APIConstants.STATUS).equalsIgnoreCase("true")) {

                                }
                            } catch (JSONException e) {
                                // throw new RuntimeException(e);
                            }
                        }
                        @Override
                        public void onError(ANError anError) {
                            Log.e("errorBin", anError.getErrorDetail());
                            Log.e("errorcodeBin", "" + anError.getErrorCode());
                            isOtherWorkIsInProgress = false;
                        }
                    });
            Log.e("URLBin", "" + Url);
            Log.e("URLBin", jsonObject.toString());
        } catch (JSONException e) {
            Log.e("ExceptionBin", e.getMessage());
        }
    }

}