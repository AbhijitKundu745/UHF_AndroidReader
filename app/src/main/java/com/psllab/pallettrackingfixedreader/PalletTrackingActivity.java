package com.psllab.pallettrackingfixedreader;


import static com.seuic.uhfandroid.ext.DataExtKt.getCategoryID;
import static com.seuic.uhfandroid.ext.DataExtKt.getTotalCounts;
import static com.seuic.uhfandroid.ext.DataExtKt.typePallet;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.psl.pallettrackingfixedreader.databinding.ActivityPalletTrackingBinding;
import com.psllab.pallettrackingfixedreader.adapter.TagInfoAdapter;
import com.psllab.pallettrackingfixedreader.bean.TagBean;
import com.psllab.pallettrackingfixedreader.bean.WorkOrderUploadTagBean;
import com.psllab.pallettrackingfixedreader.databases.DatabaseHandler;
import com.psllab.pallettrackingfixedreader.utils.APIConstants;
import com.psllab.pallettrackingfixedreader.utils.DataStoreUtils;

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

public class PalletTrackingActivity extends UHFActivity  {
    private boolean isUnload = false;
    ActivityPalletTrackingBinding binding;
    private Context context = this;
    private TagInfoAdapter adapter;
    private DatabaseHandler db;
    private boolean isWorkInProgress = false;
    private boolean isReadingOn = false;
    String androidID= "";
    private Handler handler = new Handler();
    private Runnable runnable;
    private void stopHandler() {
        // Remove any pending callbacks and messages
        handler.removeCallbacks(runnable);
    }
    @Override
    protected void onDestroy() {
        // Stop the handler when the activity is destroyed
        stopHandler();
        super.onDestroy();
    }
    private void startHandler() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // Do something after 15 seconds
                handler.postDelayed(this, 15000);
                if(!isReadingOn) {
                    checkValidationsAndCallAPI();
                }
                if(db.getOfflineTagMasterCount()>0) {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            Log.e("OFFLINEDATACOUNT",""+db.getOfflineTagMasterCount());
                            doOfflineAPICall();
                        }
                    }, 100);

                }else{
                    Log.e("NOOFFLINE","DATA");
                }
            }
        };

        // Post the initial Runnable with a delay of 2 seconds
        handler.postDelayed(runnable, 2000);
    }
    private void startReadingHandler() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // Do something after 2.5 seconds
                handler.postDelayed(this, 2500);
                try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                Date currentDateTime = new Date();

                String currentDate = simpleDateFormat.format(currentDateTime);
                String previousDate = db.getAddedDateTimeForPalletEPC();
                if(previousDate.equalsIgnoreCase("")){
                    previousDate = currentDate;
                }

                Date date1 = simpleDateFormat.parse(previousDate);
                Date date2 = simpleDateFormat.parse(currentDate);

                long different = date1.getTime() - date2.getTime();
                long secondsInMilli = 1000;
                long elapsedSeconds = different / secondsInMilli;
                if(elapsedSeconds<0){
                    elapsedSeconds = elapsedSeconds*-1;
                }
                if(elapsedSeconds > DataStoreUtils.getINSTANCE(context).getPalletTagConfigTime()){
                    isReadingOn = false;
                }
                } catch (ParseException e) {
                    Log.e("EXC",""+e.getMessage());
                    e.printStackTrace();
                    Log.e("PARSEEXC",""+e.getMessage());
                    isReadingOn = false;
                }
            }
        };

        // Post the initial Runnable with a delay of 2 seconds
        handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPalletTrackingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        db = new DatabaseHandler(context);
        adapter = new TagInfoAdapter(R.layout.layout_tag);
        adapter.notifyDataSetChanged();
        db.deleteTagMaster();

        //db.deleteOfflineTagMaster();

        androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        androidID = androidID.toUpperCase();
        Log.e("Device ID", ""+androidID);

        startHandler();
        startReadingHandler();

        if(isUnload){
            binding.btnParent.setText("Parent Bin");
        }else{
            binding.btnParent.setText("Parent Pallet");
        }

        binding.btnParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isUnload){
                    isUnload = false;
                    binding.btnParent.setText("Parent Pallet");

                }else{
                    isUnload = true;
                    binding.btnParent.setText("Parent Bin");
                }
            }
        });

        binding.rlvEpc.setAdapter(adapter);
        binding.rlvEpc.setLayoutManager(
                new LinearLayoutManager(PalletTrackingActivity.this));

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTagData().postValue(null);
                adapter.notifyDataSetChanged();
                db.deleteTagMaster();
                startInventory();
            }
        });

        binding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTagData().postValue(null);
                adapter.notifyDataSetChanged();
                stopInventory();
                getTotalCounts().set(0);//changed by Abhijit
            }
        });
        getTagData().observe(this, new Observer<TagBean>() {
            @Override
            public void onChanged(TagBean bean) {
                if (bean != null) {
                    setListData(bean);
                }
            }
        });

    }

    private void checkValidationsAndCallAPI(){
        //List<TagBean> allTagsOffline = db.getAllTagDataForBatch()
        if(!isWorkInProgress){

            Log.e("INWORK","2");
            long tagCount = db.getTagMasterCount();
            boolean isPalletTagPresent = db.isPalletTagPresent();
            if(tagCount>0 && !isReadingOn){
                //TODO CALL API
                Log.e("INWORK","3");
                if(isPalletTagPresent){
                List<TagBean> allTags = db.getAllTagData();
                List<WorkOrderUploadTagBean> allTagsOffline1 = new ArrayList<>();
                String batchId= APIConstants.getSystemDateTimeForBatchId();
                for(int i=0;i<allTags.size();i++){
                    TagBean bean= allTags.get(i);
                    bean.setBatchId(batchId);
                    String workOrderNumber = "";
                    String workOrderType = "";
                    WorkOrderUploadTagBean workOrderUploadTagBean = new WorkOrderUploadTagBean(bean, workOrderNumber);
                    allTagsOffline1.add(workOrderUploadTagBean);
                }
                db.storeOfflineTagMaster(allTagsOffline1);
                }
                db.deleteTagMaster();
                adapter.setList(db.getAllTagData());
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void doOfflineAPICall() {
        try {
            String topBatchId= db.getTopBatchId();
            if(topBatchId==null){
                return;
            }
            WorkOrderUploadTagBean bean= db.getPalletTagForBatchId(topBatchId);
            Log.e("BEFORE4",topBatchId);
            if(bean==null){
                db.deleteOfflineTagMasterForBatch(topBatchId);
                Log.e("INWORK","4");
                return;
            }
            String palletTagId = bean.getEpcId();
            String palletTagRssi = ""+bean.getRssi();
            String palletTagCount = ""+getTotalCounts().incrementAndGet();
            String TransID = UUID.randomUUID().toString();
            Log.e("COUNT",palletTagCount);
            String palletTagAntenaId = ""+bean.getAntenna();
            //String date_time = APIConstants.getSystemDateTimeInFormatt();
            String date_time = ""+bean.getAddedDateTime();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.DEVICE_ID,androidID);
            jsonObject.put(APIConstants.ANTENA_ID, "1");
            jsonObject.put(APIConstants.TRANS_ID, TransID);
            jsonObject.put(APIConstants.RSSI, palletTagRssi);
            jsonObject.put(APIConstants.TRANSACTION_DATE_TIME, date_time);
            jsonObject.put(APIConstants.COUNT, palletTagCount);
            jsonObject.put(APIConstants.PALLET_TAG_ID, palletTagId);
            jsonObject.put(APIConstants.ANTENA_ID, palletTagAntenaId);
            jsonObject.put(APIConstants.SUB_TAG_CATEGORY_ID,getCategoryID(palletTagId));

            jsonObject.put(APIConstants.TOUCH_POINT_TYPE, "T");
            JSONArray tagDetailsArray = new JSONArray();
            List<WorkOrderUploadTagBean> allTags = db.getAllTagDataForBatch(topBatchId);
            for(int i=0;i<allTags.size();i++){
                JSONObject obj = new JSONObject();
                WorkOrderUploadTagBean tagBean = allTags.get(i);
                obj.put(APIConstants.SUB_TRANS_ID,TransID);
                obj.put(APIConstants.SUB_TAG_ID,tagBean.getEpcId());
                obj.put(APIConstants.COUNT,""+getTotalCounts().incrementAndGet());
                obj.put(APIConstants.RSSI,""+tagBean.getRssi());
                obj.put(APIConstants.SUB_TAG_CATEGORY_ID,getCategoryID(tagBean.getEpcId()));
                obj.put(APIConstants.SUB_TAG_TYPE,""+tagBean.getTagType());
                obj.put(APIConstants.TRANSACTION_DATE_TIME,""+tagBean.getAddedDateTime());
                if(!tagBean.getTagType().equalsIgnoreCase(typePallet)){
                    tagDetailsArray.put(obj);
                }
            }
            jsonObject.put(APIConstants.SUB_TAG_DETAILS,tagDetailsArray);
            //jsonObject.put(APIConstants.K_ASSET_SERIAL_NUMBER,serialnumber);
            Log.e("OFFLINEDATA",jsonObject.toString());
            postInventoryData(topBatchId,jsonObject);

        } catch (JSONException e) {
            Log.e("INWORK","5");
            Log.e("Exception", e.getMessage());
        }
    }

    public void postInventoryData(String batchId,final JSONObject loginRequestObject) {

        Log.e("INWORK","APICALL");
        isWorkInProgress = true;
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
                        Log.e("result",result.toString());

                        isWorkInProgress = false;
                        try {
                            if(result.getString("status").equalsIgnoreCase("true")){
                                db.deleteOfflineTagMasterForBatch(batchId);
                            }
                        } catch (JSONException e) {
                           // throw new RuntimeException(e);
                        }

                    }

                    @Override
                    public void onError(ANError anError) {

                        Log.e("error",anError.getErrorDetail());
                        Log.e("errorcode",""+anError.getErrorCode());

                        isWorkInProgress = false;
                    }
                });
        Log.e("URL", ""+Url);
        Log.e("URL", loginRequestObject.toString());
    }
    private void dummyAPICall(){
        if(!isWorkInProgress) {
            long tagCount = db.getTagMasterCount();
            Log.e("Dummy", "DummyCALL");
            if (tagCount > 0) {
                //TODO CALL API
                try {
                    JSONObject jsonObject1 = new JSONObject();
                    JSONArray tagDetailsArray1 = new JSONArray();
                    List<TagBean> allTags1 = db.getAllTagData();
                    for(int i=0;i<allTags1.size();i++){
                        JSONObject obj1 = new JSONObject();
                        TagBean tagBean1 = allTags1.get(i);
                        //String TransID = doOfflineAPICall(TransID);
                        //obj1.put(APIConstants.DUMMY_TRANS_ID,TransID);
                        obj1.put(APIConstants.DUMMY_TAG_ID,tagBean1.getEpcId());
                        obj1.put(APIConstants.COUNT,""+getTotalCounts().incrementAndGet());
                        obj1.put(APIConstants.RSSI,""+tagBean1.getRssi());
                        obj1.put(APIConstants.TRANSACTION_DATE_TIME,""+tagBean1.getAddedDateTime());
                        tagDetailsArray1.put(obj1);
                    }
                    jsonObject1.put(APIConstants.DUMMY_TAG_DETAILS,tagDetailsArray1);
                    postData(jsonObject1);

                } catch (Exception ex){
                    Log.e("Exception", ex.getMessage());
                }
            }
        }
    }
    public void postData(final JSONObject dummyObject) {

        Log.e("DUMMY","APICALL");
        isWorkInProgress = true;
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        String Url = APIConstants.M_HOST_URL + APIConstants.M_DUMMY_POST_INVENTORY;

        AndroidNetworking.post(APIConstants.M_HOST_URL + APIConstants.M_DUMMY_POST_INVENTORY).addJSONObjectBody(dummyObject)
                .setTag("test")
                //.addHeaders("Authorization",SharedPreferencesManager.getAccessToken(context))

                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.e("result1",result.toString());

                        isWorkInProgress = false;
                    }

                    @Override
                    public void onError(ANError anError) {

                        Log.e("error1",anError.getErrorDetail());
                        Log.e("errorcode1",""+anError.getErrorCode());

                        isWorkInProgress = false;
                    }
                });
        Log.e("URL", ""+Url);
        Log.e("URL", dummyObject.toString());
    }
    private void setListData(TagBean bean ) {
        if (bean != null) {
            isReadingOn = true;
            //setTagListData(bean);
           //check here from database
            //if already present then update it accordingly else add it according to logic
            //if pallet tag then if already present then check for time diff if time diff is < x sec then update it else delete all data and check for new pallet tag

            List<TagBean> lst= new ArrayList<>();
            lst.add(bean);

           if(bean.getTagType().equalsIgnoreCase(typePallet)){
               if (db.isPalletTagPresent()) {
                   //check time of pallet tag
                   //if time > config time then delete all data else update pallet tag data
                   String currentDate = bean.getAddedDateTime();
                   String previousDate = db.getAddedDateTimeForPalletEPC();

                   if(!previousDate.equalsIgnoreCase("")){
                       SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                       try {
                           Date date1 = simpleDateFormat.parse(previousDate);
                           Date date2 = simpleDateFormat.parse(currentDate);

                           long different = date1.getTime() - date2.getTime();
                           long secondsInMilli = 1000;
                           long elapsedSeconds = different / secondsInMilli;
                           if(elapsedSeconds<0){
                               elapsedSeconds = elapsedSeconds*-1;
                           }

                           if(elapsedSeconds > DataStoreUtils.getINSTANCE(context).getPalletTagConfigTime()){
                               if(db.isEpcPresent(bean.getEpcId())){
                                   Log.e("Time", "Greater 1");
                                   db.deleteTagMaster();
                                   db.storeTagMaster(lst);
                               }else{
                                   Log.e("Time", "Greater 2");
                                   //db.deleteTagMaster();
                                   long tagCount = db.getTagMasterCount();
                                   if(tagCount>0){
                                       //TODO CALL API
                                       Log.e("INWORK","3");
                                       List<TagBean> allTags = db.getAllTagData();
                                       List<WorkOrderUploadTagBean> allTagsOffline1 = new ArrayList<>();
                                       String batchId= APIConstants.getSystemDateTimeForBatchId();
                                       for(int i=0;i<allTags.size();i++){
                                           TagBean bean1= allTags.get(i);
                                           bean1.setBatchId(batchId);

                                           String workOrderNumber = "";
                                           String workOrderType = "";
                                           WorkOrderUploadTagBean workOrderUploadTagBean = new WorkOrderUploadTagBean(bean1, workOrderNumber);
                                           allTagsOffline1.add(workOrderUploadTagBean);

                                           //allTagsOffline1.add(bean1);
                                       }
                                       db.storeOfflineTagMaster(allTagsOffline1);
                                       db.deleteTagMaster();
                                       adapter.setList(db.getAllTagData());
                                       adapter.notifyDataSetChanged();
                                   }
                                   db.storeTagMaster(lst);
                               }


                           }else{
                               if(db.isEpcPresent(bean.getEpcId())){
                                   Log.e("Time", "Greater 3");
                                   // db.deleteTagMaster();
                                   db.storeTagMaster(lst);
                               }else{
                                   //Now here need to check RSSI, give rssi high preference
                                   Log.e("Time", "Greater 4");
                                   try {
                                       String oldRSSIString = db.getRSSIPalletEPC();
                                       int oldRSSI = Integer.parseInt(oldRSSIString);
                                       int newRSSI = bean.getRssi();
                                       if(oldRSSI<0){
                                           oldRSSI = (-1)*oldRSSI;
                                       }

                                       if(newRSSI<0){
                                           newRSSI = (-1)*newRSSI;
                                       }
                                       if(newRSSI<oldRSSI){
                                           //replace only new pallet tag with old pallet tag as he has greater RSSI and keep child tags as it is in transaction
                                           db.deletePalletTag(bean.getEpcId());
                                           db.storeTagMaster(lst);
                                       }
                                   }catch (Exception ex){
                                       Log.e("RSSIEXC",""+ ex.getMessage());
                                   }
                               }
                           }
                           adapter.setList(db.getAllTagData());
                           adapter.notifyDataSetChanged();

                       } catch (ParseException e) {
                           Log.e("EXC",""+e.getMessage());
                           e.printStackTrace();
                       }
                   }else{
                       //TODO
                   }

               }else{
                  //add
                   db.storeTagMaster(lst);
                   adapter.setList(db.getAllTagData());
                   adapter.notifyDataSetChanged();
               }
           }else{
               if (db.isPalletTagPresent()) {
                   db.storeTagMaster(lst);
                   adapter.setList(db.getAllTagData());
                   adapter.notifyDataSetChanged();

               }else{
                   if(isUnload){
                       db.storeTagMaster(lst);
                       adapter.setList(db.getAllTagData());
                       adapter.notifyDataSetChanged();
                   }
                   //ignore tags
               }

           }

            boolean dataSubmitted = false;
            if(!dataSubmitted){
                dummyAPICall();
                dataSubmitted = true;
            } else if(dataSubmitted){
                Log.e("SubmitDummy", "Submitted");
            }


        }
        //isReadingOn = false;
    }

}