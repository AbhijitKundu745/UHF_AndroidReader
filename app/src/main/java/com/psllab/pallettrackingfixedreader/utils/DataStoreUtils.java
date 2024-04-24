package com.psllab.pallettrackingfixedreader.utils;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class DataStoreUtils {
    private static final String TAG = DataStoreUtils.class.getSimpleName();
    private static volatile DataStoreUtils INSTANCE;
    RxDataStore<Preferences> dataStore;
    private Context context;

    Preferences.Key<Boolean> isSearchingKey = PreferencesKeys.booleanKey("isSearching");
    Preferences.Key<Integer> powerKey = PreferencesKeys.intKey("power");
    Preferences.Key<Integer> pollingTimerKey = PreferencesKeys.intKey("pollingtimer");
    Preferences.Key<Integer> rssiKey = PreferencesKeys.intKey("rssi");
    Preferences.Key<Integer> regionKey = PreferencesKeys.intKey("region");
    Preferences.Key<Integer> sessionKey = PreferencesKeys.intKey("session");
    Preferences.Key<Integer> qKey = PreferencesKeys.intKey("Q");
    Preferences.Key<Integer> palletTagConfigTime = PreferencesKeys.intKey("palletTagConfigTime");
    Preferences.Key<Integer> profileKey = PreferencesKeys.intKey("profile");
    Preferences.Key<Integer> targetKey = PreferencesKeys.intKey("target");
    // 附加数据
    Preferences.Key<Boolean> enableAdditionalDataKey = PreferencesKeys.booleanKey("enableAdditionalData");
    // 存储区 起始地址 长度 访问密码
    Preferences.Key<Integer> bankKey = PreferencesKeys.intKey("bank");
    Preferences.Key<String> startAddressKey = PreferencesKeys.stringKey("startAddress");
    Preferences.Key<String> lengthKey = PreferencesKeys.stringKey("length");
    Preferences.Key<String> passwordKey = PreferencesKeys.stringKey("password");
    // 过滤
    Preferences.Key<String> filterStartAddressKey = PreferencesKeys.stringKey("filterStartAddress");
    Preferences.Key<Integer> filterBankKey = PreferencesKeys.intKey("filterBank");
    Preferences.Key<Boolean> filterSuitKey = PreferencesKeys.booleanKey("filterSuit");
    Preferences.Key<String> filterDataKey = PreferencesKeys.stringKey("filterData");
    Preferences.Key<Boolean> filterNullStrKey = PreferencesKeys.booleanKey("filterNullStr");
    // 天线配置
    Preferences.Key<Boolean> ant1Key = PreferencesKeys.booleanKey("ant1");
    Preferences.Key<Boolean> ant2Key = PreferencesKeys.booleanKey("ant2");
    Preferences.Key<Boolean> ant3Key = PreferencesKeys.booleanKey("ant3");
    Preferences.Key<Boolean> ant4Key = PreferencesKeys.booleanKey("ant4");
    Preferences.Key<Boolean> ant5Key = PreferencesKeys.booleanKey("ant5");
    Preferences.Key<Boolean> ant6Key = PreferencesKeys.booleanKey("ant6");
    Preferences.Key<Boolean> ant7Key = PreferencesKeys.booleanKey("ant7");
    Preferences.Key<Boolean> ant8Key = PreferencesKeys.booleanKey("ant8");
    // gpio
    Preferences.Key<Boolean> gpo1Key = PreferencesKeys.booleanKey("gpo1");
    Preferences.Key<Boolean> gpo2Key = PreferencesKeys.booleanKey("gpo2");
    Preferences.Key<Boolean> gpo3Key = PreferencesKeys.booleanKey("gpo3");
    Preferences.Key<Boolean> gpo4Key = PreferencesKeys.booleanKey("gpo4");
    // 蜂鸣器
    Preferences.Key<Boolean> buzzerKey = PreferencesKeys.booleanKey("buzzer");


    private DataStoreUtils(Context context) {
        this.context = context;
        dataStore = new RxPreferenceDataStoreBuilder(context, "datastore_sc").build();
    }

    public static DataStoreUtils getINSTANCE(Context context) {
        if (INSTANCE == null) {
            synchronized (DataStoreUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataStoreUtils(context);
                }
            }
        }
        return INSTANCE;
    }

    public Boolean getIsSearching() {
        Flowable<Boolean> aBoolean = null;
        try {
            aBoolean = getBoolean(isSearchingKey);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public DataStoreUtils setIsSearching(Boolean isSearching) {
        putBoolean(isSearchingKey, isSearching);
        return this;
    }

    public Boolean getEnableAdditionalData() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(enableAdditionalDataKey);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public DataStoreUtils setEnableAdditionalData(Boolean enable) {
        putBoolean(enableAdditionalDataKey, enable);
        return this;
    }

    public Boolean getEnableBuzzer() {
        // TODO: 2022/3/23 开机后只使用了first的值 
/*        try {
            Flowable<Boolean> aBoolean = getBoolean(buzzerKey);
            return aBoolean.first(true).blockingGet();
        } catch (Exception e) {
            return true;
        }*/
        SharedPreferencesUtils sharedPreferencesUtils = new SharedPreferencesUtils(context);
        return sharedPreferencesUtils.getEnableBuzzer();
    }

    public DataStoreUtils setEnableBuzzer(Boolean enable) {
//        putBoolean(buzzerKey, enable);
        SharedPreferencesUtils sharedPreferencesUtils = new SharedPreferencesUtils(context);
        sharedPreferencesUtils.setEnableBuzzer(enable);
        return this;
    }

    public Integer getPower() {
        try {
            Flowable<Integer> integer = getInteger(powerKey);
            return integer.first(33).blockingGet();
        } catch (Exception e) {
            return 33;
        }
    }

    public Integer getPollingTimer() {
        try {
            Flowable<Integer> integer = getInteger(pollingTimerKey);
            return integer.first(10000).blockingGet();
        } catch (Exception e) {
            return 10000;
        }
    }

    public Integer getRssi() {
        try {
            Flowable<Integer> integer = getInteger(rssiKey);
            return integer.first(50).blockingGet();
        } catch (Exception e) {
            return 50;
        }
    }
    public DataStoreUtils setPower(Integer power) {
        putInteger(powerKey, power);
        return this;
    }

    public DataStoreUtils setPollingTimer(Integer power) {
        putInteger(pollingTimerKey, power);
        return this;
    }

    public DataStoreUtils setRssi(Integer power) {
        putInteger(rssiKey, power);
        return this;
    }

    public Integer getRegion() {
        try {
            Flowable<Integer> integer = getInteger(regionKey);
            return integer.first(0).blockingGet();
        } catch (Exception e) {
            return 0;
        }
    }

    public DataStoreUtils setRegion(Integer integer) {
        putInteger(regionKey, integer);
        return this;
    }

    public Integer getSession() {
        try {
            Flowable<Integer> integer = getInteger(sessionKey);
            return integer.first(0).blockingGet();
        } catch (Exception e) {
            return 0;
        }
    }

    public DataStoreUtils setSession(Integer integer) {
        putInteger(sessionKey, integer);
        return this;
    }

    public Integer getPalletTagConfigTime() {
        try {
            Flowable<Integer> integer = getInteger(palletTagConfigTime);
            return integer.first(300).blockingGet();
        } catch (Exception e) {
            return 300;
        }
    }

    public DataStoreUtils setPalletTagConfigTime(Integer integer) {
        putInteger(palletTagConfigTime, integer);
        return this;
    }


    public Integer getQ() {
        try {
            Flowable<Integer> integer = getInteger(qKey);
            return integer.first(5).blockingGet();
        } catch (Exception e) {
            return 5;
        }
    }

    public DataStoreUtils setQ(Integer integer) {
        putInteger(qKey, integer);
        return this;
    }

    public Integer getProfile() {
        try {
            Flowable<Integer> integer = getInteger(profileKey);
            return integer.first(0).blockingGet();
        } catch (Exception e) {
            return 0;
        }
    }

    public DataStoreUtils setProfile(Integer integer) {
        putInteger(profileKey, integer);
        return this;
    }

    public Integer getTarget() {
        try {
            Flowable<Integer> integer = getInteger(targetKey);
            return integer.first(0).blockingGet();
        } catch (Exception e) {
            return 0;
        }
    }

    public DataStoreUtils setTarget(Integer integer) {
        putInteger(targetKey, integer);
        return this;
    }

    public Integer getBank() {
        try {
            Flowable<Integer> integer = getInteger(bankKey);
            return integer.first(0).blockingGet();
        } catch (Exception e) {
            return 0;
        }
    }

    public DataStoreUtils setBank(Integer integer) {
        putInteger(bankKey, integer);
        return this;
    }

    public String getStartAddress() {
        try {
            Flowable<String> string = getString(startAddressKey);
            return string.first("0").blockingGet();
        } catch (Exception e) {
            return "0";
        }
    }

    public DataStoreUtils setStartAddress(String str) {
        putString(startAddressKey, str);
        return this;
    }

    public String getLength() {
        try {
            Flowable<String> string = getString(lengthKey);
            return string.first("12").blockingGet();
        } catch (Exception e) {
            return "12";
        }
    }

    public DataStoreUtils setLength(String str) {
        putString(lengthKey, str);
        return this;
    }

    public String getPassword() {
        try {
            Flowable<String> string = getString(passwordKey);
            return string.first("00000000").blockingGet();
        } catch (Exception e) {
            return "00000000";
        }
    }

    public DataStoreUtils setPassword(String str) {
        putString(passwordKey, str);
        return this;
    }

    public String getFilterStartAddress() {
        try {
            Flowable<String> string = getString(filterStartAddressKey);
            return string.first("").blockingGet();
        } catch (Exception e) {
            return "";
        }
    }

    public DataStoreUtils setFilterStartAddress(String str) {
        putString(filterStartAddressKey, str);
        return this;
    }

    public Integer getFilterBank() {
        try {
            Flowable<Integer> integer = getInteger(filterBankKey);
            return integer.first(1).blockingGet();
        } catch (Exception e) {
            return 1;
        }
    }

    public Boolean getFilterSuit() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(filterSuitKey);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public DataStoreUtils setFilterSuit(Boolean enable) {
        putBoolean(filterSuitKey, enable);
        return this;
    }

    public DataStoreUtils setFilterBank(Integer integer) {
        putInteger(filterBankKey, integer);
        return this;
    }

    public String getFilterData() {
        try {
            Flowable<String> string = getString(filterDataKey);
            return string.first("").blockingGet();
        } catch (Exception e) {
            return "";
        }
    }

    public DataStoreUtils setFilterData(String str) {
        putString(filterDataKey, str);
        return this;
    }

    public Boolean getFilterNull() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(filterNullStrKey);
            return aBoolean.first(true).blockingGet();
        } catch (Exception e) {
            return true;
        }
    }

    public DataStoreUtils setFilterNull(Boolean enable) {
        putBoolean(filterNullStrKey, enable);
        return this;
    }

    public Boolean getAnt1() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant1Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getAnt2() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant2Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getAnt3() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant3Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getAnt4() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant4Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getAnt5() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant5Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getAnt6() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant6Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getAnt7() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant7Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getAnt8() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(ant8Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public DataStoreUtils setAnt1(Boolean enable) {
        putBoolean(ant1Key, enable);
        return this;
    }

    public DataStoreUtils setAnt2(Boolean enable) {
        putBoolean(ant2Key, enable);
        return this;
    }

    public DataStoreUtils setAnt3(Boolean enable) {
        putBoolean(ant3Key, enable);
        return this;
    }

    public DataStoreUtils setAnt4(Boolean enable) {
        putBoolean(ant4Key, enable);
        return this;
    }

    public DataStoreUtils setAnt5(Boolean enable) {
        putBoolean(ant5Key, enable);
        return this;
    }

    public DataStoreUtils setAnt6(Boolean enable) {
        putBoolean(ant6Key, enable);
        return this;
    }

    public DataStoreUtils setAnt7(Boolean enable) {
        putBoolean(ant7Key, enable);
        return this;
    }

    public DataStoreUtils setAnt8(Boolean enable) {
        putBoolean(ant8Key, enable);
        return this;
    }

    public Boolean getGpo1() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(gpo1Key);
            return aBoolean.first(true).blockingGet();
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean getGpo2() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(gpo2Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getGpo3() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(gpo3Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getGpo4() {
        try {
            Flowable<Boolean> aBoolean = getBoolean(gpo4Key);
            return aBoolean.first(false).blockingGet();
        } catch (Exception e) {
            return false;
        }
    }

    public DataStoreUtils setGpo1(Boolean enable) {
        putBoolean(gpo1Key, enable);
        return this;
    }

    public DataStoreUtils setGpo2(Boolean enable) {
        putBoolean(gpo2Key, enable);
        return this;
    }

    public DataStoreUtils setGpo3(Boolean enable) {
        putBoolean(gpo3Key, enable);
        return this;
    }

    public DataStoreUtils setGpo4(Boolean enable) {
        putBoolean(gpo4Key, enable);
        return this;
    }


    private void putString(Preferences.Key<String> key, String value) {
        dataStore.updateDataAsync(new Function<Preferences, Single<Preferences>>() {
            @Override
            public Single<Preferences> apply(Preferences preferences) throws Throwable {
                MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                mutablePreferences.set(key, value);
                return Single.just(mutablePreferences);
            }
        });
    }

    private void putInteger(Preferences.Key<Integer> key, Integer value) {
        dataStore.updateDataAsync(new Function<Preferences, Single<Preferences>>() {
            @Override
            public Single<Preferences> apply(Preferences preferences) throws Throwable {
                MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                mutablePreferences.set(key, value);
                return Single.just(mutablePreferences);
            }
        });
    }

    private void putBoolean(Preferences.Key<Boolean> key, Boolean value) {
        dataStore.updateDataAsync(new Function<Preferences, Single<Preferences>>() {
            @Override
            public Single<Preferences> apply(Preferences preferences) throws Throwable {
                MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                mutablePreferences.set(key, value);
                return Single.just(mutablePreferences);
            }
        });
    }

    private Flowable<String> getString(Preferences.Key<String> key) {
        Flowable<String> example = dataStore.data().map(new Function<Preferences, String>() {
            @Override
            public String apply(Preferences preferences) {
                return preferences.get(key);
            }
        });
        return example;
    }

    private Flowable<Integer> getInteger(Preferences.Key<Integer> key) {
        Flowable<Integer> example = dataStore.data().map(new Function<Preferences, Integer>() {
            @Override
            public Integer apply(Preferences preferences) {
                return preferences.get(key);
            }
        });
        return example;
    }

    private Flowable<Boolean> getBoolean(Preferences.Key<Boolean> key) {
        Flowable<Boolean> example = dataStore.data().map(new Function<Preferences, Boolean>() {
            @Override
            public Boolean apply(Preferences preferences) {
                return preferences.get(key);
            }
        });
        return example;
    }
}
