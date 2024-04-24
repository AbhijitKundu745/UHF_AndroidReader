package com.psllab.pallettrackingfixedreader.databases;

import static com.seuic.uhfandroid.ext.DataExtKt.typePallet;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.psllab.pallettrackingfixedreader.bean.TagBean;
import com.psllab.pallettrackingfixedreader.bean.WorkOrderUploadTagBean;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("Range")
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 14;
    private static final String DATABASE_NAME = "PSL_PALLET_FIXEDREADER-DB";

    private static final String TABLE_TAG_MASTER = "Tag_Master_Table";
    private static final String TABLE_OFFLINE_TAG_MASTER = "Offline_Tag_Master_Table";

    private static final String K_EPC = "K_EPC";
    private static final String K_WORK_ORDER_NUMBER = "K_WORK_ORDER_NUMBER";
    private static final String K_WORK_ORDER_TYPE = "K_WORK_ORDER_TYPE";
    private static final String K_BATCH_ID = "K_BATCH_ID";
    private static final String K_RSSI = "K_RSSI";
    private static final String K_TIMES = "K_TIMES";
    private static final String K_ANTEANA = "K_ANTEANA";
    private static final String K_ADDITIONAL_DATA = "K_ADDITIONAL_DATA";

    private static final String K_TAG_TYPE = "K_TAG_TYPE";
    private static final String K_DATE_TIME = "K_DATE_TIME";
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TAG_MASTER_TABLE = "CREATE TABLE "
                + TABLE_TAG_MASTER
                + "("
                + K_EPC + " TEXT UNIQUE,"//0
                + K_RSSI + " INTEGER,"//1
                + K_TIMES + " INTEGER,"//1
                + K_ANTEANA + " TEXT,"//1
                + K_ADDITIONAL_DATA + " TEXT,"//1
                + K_TAG_TYPE + " TEXT,"//1
                + K_DATE_TIME + " TEXT"//1
                + ")";

        String CREATE_OFFLINE_TAG_MASTER_TABLE = "CREATE TABLE "
                + TABLE_OFFLINE_TAG_MASTER
                + "("
                + K_BATCH_ID + " TEXT,"//0
                + K_WORK_ORDER_NUMBER + " TEXT,"//0
                + K_EPC + " TEXT,"//0
                + K_RSSI + " INTEGER,"//1
                + K_TIMES + " INTEGER,"//1
                + K_ANTEANA + " TEXT,"//1
                + K_ADDITIONAL_DATA + " TEXT,"//1
                + K_TAG_TYPE + " TEXT,"//1
                + K_DATE_TIME + " TEXT"//1
                + ")";

        db.execSQL(CREATE_TAG_MASTER_TABLE);
        db.execSQL(CREATE_OFFLINE_TAG_MASTER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG_MASTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OFFLINE_TAG_MASTER);
        // Create tables again
        onCreate(db);
    }

    public void deleteTagMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TAG_MASTER, null, null);
        db.close();
    }

    public void deleteOfflineTagMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OFFLINE_TAG_MASTER, null, null);
        db.close();
    }

    public void deleteOfflineTagMasterForBatch(String batchId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Specify the WHERE clause and arguments based on batchId
        String whereClause = "K_BATCH_ID = ?";
        String[] whereArgs = {batchId};

        // Delete rows with the specified condition
        db.delete(TABLE_OFFLINE_TAG_MASTER, whereClause, whereArgs);

        // Close the database
        db.close();
    }

    public void deletePalletTag(String palletEpc) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Specify the WHERE clause and arguments based on batchId
        String whereClause = "K_EPC = ?";
        String[] whereArgs = {palletEpc};

        // Delete rows with the specified condition
        db.delete(TABLE_TAG_MASTER, whereClause, whereArgs);

        // Close the database
        db.close();
    }

    public void storeTagMaster(List<TagBean> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();

        try {
            for (TagBean tag : lst) {
                ContentValues values = new ContentValues();
                values.put(K_EPC, tag.getEpcId());
                values.put(K_RSSI, tag.getRssi());
                values.put(K_TIMES, tag.getTimes());
                values.put(K_ANTEANA, tag.getAntenna());
                values.put(K_ADDITIONAL_DATA, tag.getAdditionalData());
                values.put(K_TAG_TYPE, tag.getTagType());
                values.put(K_DATE_TIME, tag.getAddedDateTime());

                db.insertWithOnConflict(TABLE_TAG_MASTER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void storeOfflineTagMaster(List<WorkOrderUploadTagBean> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();

        try {
            for (WorkOrderUploadTagBean tag : lst) {
                ContentValues values = new ContentValues();
                values.put(K_BATCH_ID, tag.getBatchId());
                values.put(K_EPC, tag.getEpcId());
                values.put(K_WORK_ORDER_NUMBER, tag.getWorkOrderNumber());
                values.put(K_RSSI, tag.getRssi());
                values.put(K_TIMES, tag.getTimes());
                values.put(K_ANTEANA, tag.getAntenna());
                values.put(K_ADDITIONAL_DATA, tag.getAdditionalData());
                values.put(K_TAG_TYPE, tag.getTagType());
                values.put(K_DATE_TIME, tag.getAddedDateTime());

                db.insertWithOnConflict(TABLE_OFFLINE_TAG_MASTER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public long getTagMasterCount() {
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase() instead of getWritableDatabase() since you are performing a read operation
        try {
            return DatabaseUtils.queryNumEntries(db, TABLE_TAG_MASTER);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean isPalletTagPresent() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_TAG_TYPE + " = ?";
            String[] selectionArgs = {typePallet};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there are any rows
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return false;
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    public TagBean getPalletTag() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        TagBean tag=null;
        try {
            // Query the database to get all rows, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    K_TAG_TYPE + "=?", // condition (use "?" as a placeholder)
                    new String[]{typePallet}, // selectionArgs (replace with the actual tag type value)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Check if there are any rows
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use the constructor of TagBean to create an object
                    tag = new TagBean(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                            //cursor.getString(cursor.getColumnIndexOrThrow(K_CATEGORY_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                    );

                    // Add the TagBean object to the list

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tag;
    }
    public WorkOrderUploadTagBean getPalletTagForBatchId(String batchId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        WorkOrderUploadTagBean tag = null;

        try {
            // Construct the query using rawQuery for better control
            String query = "SELECT * FROM " + TABLE_OFFLINE_TAG_MASTER +
                    " WHERE " + K_TAG_TYPE + "=? AND " + K_BATCH_ID + "=? " +
                    " ORDER BY " + K_TAG_TYPE + " DESC";

            cursor = db.rawQuery(query, new String[]{typePallet, batchId});

            // Move cursor to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Extract data from the cursor and instantiate the TagBean object
                tag = new WorkOrderUploadTagBean(
                        cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_NUMBER)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                );
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tag;
    }
    public TagBean getPalletTagForBatchId1(String batchId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        TagBean tag = null;
        try {
            // Query the database to get all rows, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    K_TAG_TYPE + "=? AND " + K_BATCH_ID + "=?", // condition (use "?" as a placeholder)
                    new String[]{typePallet, batchId}, // selectionArgs (replace with the actual tag type and batchId values)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Rest of your code remains unchanged
            // ...

        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tag;
    }



    public boolean isEpcPresent(String epc) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_EPC + " = ?";
            String[] selectionArgs = {epc};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there are any rows
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return false;
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public String getAddedDateTimeForEPC(String epc) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_EPC + " = ?";
            String[] selectionArgs = {epc};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    new String[]{K_DATE_TIME},
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there is a result
            if (cursor != null && cursor.moveToFirst()) {
                // Get the addedDateTime value from the cursor
                int columnIndex = cursor.getColumnIndexOrThrow(K_DATE_TIME);
                return cursor.getString(columnIndex);
            } else {
                // Return a default value or handle the case where no matching record is found
                return "";
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return "";
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }


    public String getAddedDateTimeForPalletEPC() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_TAG_TYPE + " = ?";
            String[] selectionArgs = {typePallet};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    new String[]{K_DATE_TIME},
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there is a result
            if (cursor != null && cursor.moveToFirst()) {
                // Get the addedDateTime value from the cursor
                int columnIndex = cursor.getColumnIndexOrThrow(K_DATE_TIME);
                return cursor.getString(columnIndex);
            } else {
                // Return a default value or handle the case where no matching record is found
                return "";
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return "";
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    public String getRSSIPalletEPC() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_TAG_TYPE + " = ?";
            String[] selectionArgs = {typePallet};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    new String[]{K_RSSI},
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there is a result
            if (cursor != null && cursor.moveToFirst()) {
                // Get the addedDateTime value from the cursor
                int columnIndex = cursor.getColumnIndexOrThrow(K_RSSI);
                return cursor.getString(columnIndex);
            } else {
                // Return a default value or handle the case where no matching record is found
                return "-100";
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return "-100";
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public List<TagBean> getAllTagData() {
        List<TagBean> tagList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query the database to get all rows, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    null, // condition (null means no condition)
                    null, // selectionArgs (null means no arguments)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Check if there are any rows
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use the constructor of TagBean to create an object
                    TagBean tag = new TagBean(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                    );

                    // Add the TagBean object to the list
                    tagList.add(tag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tagList;
    }

    public List<WorkOrderUploadTagBean> getAllTagDataForBatch(String batchId) {
        List<WorkOrderUploadTagBean> tagList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Specify the condition to retrieve data for a specific batchId
            String selection = K_BATCH_ID + "=?";
            String[] selectionArgs = {batchId};

            // Query the database with the specified condition, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    selection, // condition
                    selectionArgs, // selectionArgs
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Check if there are any rows
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use the constructor of TagBean to create an object
                    WorkOrderUploadTagBean tag = new WorkOrderUploadTagBean(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_NUMBER)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                    );

                    // Add the TagBean object to the list
                    tagList.add(tag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tagList;
    }


    public String getTopBatchId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String topBatchId = null;

        try {
            // Query the database to get the top row, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_MASTER,
                    new String[]{K_BATCH_ID}, // Columns to retrieve (only batch ID)
                    null, // condition (null means no condition)
                    null, // selectionArgs (null means no arguments)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC", // orderBy
                    "1" // limit to 1 row
            );

            // Check if there is a row
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the batch ID
                topBatchId = cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID));
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return topBatchId;
    }
    public long getOfflineTagMasterCount() {
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase() instead of getWritableDatabase() since you are performing a read operation
        try {
            return DatabaseUtils.queryNumEntries(db, TABLE_OFFLINE_TAG_MASTER);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


}

