/* Copyright 2015 Huami Inc.  All rights reserved. */
package com.example.jinliang.myapplication.bledemo;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import com.huami.watch.ble.BLEService;
import com.huami.watch.ble.Communicates;
import com.huami.watch.ble.IconImageManager;
import com.huami.watch.ble.MessageReceiver;
import com.huami.watch.ble.PacketProcessor;
import com.huami.watch.ble.SampleGattAttributes;
import com.huami.watch.ble.dbOp;
import com.huami.watch.ble.outerBroadCast;
import com.huami.watch.ble.pkgBroadCast;
import com.huami.watch.ble.listener.BLETransforListener;
import com.huami.watch.ble.listener.IBLETransactor;
import com.huami.watch.ble.parse.BLECommunicates;
import com.huami.watch.ble.parse.BLEDataParser;
import com.huami.watch.ble.receiver.BLEMessageReceiver;
import com.huami.watch.ble.utils.BLERefactorCommit;
import com.huami.watch.notification.NotificationPoster;
import com.huami.watch.notification.TransportUri;
import com.huami.watch.transport.DataBundle;
import com.huami.watch.transport.TransportDataItem;
import com.huami.watch.transport.TransporterModules;
import com.huami.watch.utils.Constants;
import com.huami.watch.utils.Utils;
import com.huami.watch.watchapp.WatchApp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

//import android.support.v4.app.NotificationManagerCompat;

/**
 * 重构蓝牙 模块
 */
public class BLEDataTransfor implements IBLETransactor {
    //

    /**
     * 方案的切换: true 代表:广播形式, false 代表CallBack 形式
     */
    public final static boolean SEND_MESSAGE_METHOD_STATUS = true;
    public static final boolean DEBUG = BLERefactorCommit.DEBUG && true;
    private final static String TAG = "ble_tag_refacetor";

    /**
     * Tag 管理
     * TAG_SELF : 代表周边服务是手表
     * TGA_OTHER: 代表周边服务是 手机
     */


    public static BLECommunicates bleCommunicates;

    public static final String mSendFlag = "_huami";
    //
    //action.huami.ble.notitest//android.huami.ble.fake.BOOT_COMPLETED
    public static final String ACTION_HUAMI_BLE_fake_BOOT_COMPLETED = "android.huami.ble.fake.BOOT_COMPLETED";
    public static final String ACTION_HUAMI_BLE_NOTI_TEST = "action.huami.ble.notitest";
    public static final String ACTION_HUAMI_BLE_START = "action.huami.ble.start";
    public static final String ACTION_HUAMI_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_HUAMI_BLE_STOP = "android.huami.ble.stop_service";
    public static final String ACTION_HUAMI_BLE_SET_STATUS = "action.huami.ble.set_status";
    public static final String KEY_HUAMI_BLE_SET_STATUS = "key_ble_set_status";
    public static final int VALUE_HUAMI_BLE_STATUS_START = 1;
    public static final int VALUE_HUAMI_BLE_STATUS_STOP = 0;
    /*public static final String ACTION_HUAMI_BLE_QUERY_CONSTR="action.huami.ble.query.constr";
    public static final String ACTION_HUAMI_BLE_GET_CONSTR="action.huami.ble.get.constr";*/
    public static final String ACTION_HUAMI_UNBIND = "com.huami.watch.action.huami_unbind";
    //action_huami_ios_ancs_apply
    public static final String ACTION_HUAMI_IOS_ANCS_APPLY = "action_huami_ios_ancs_apply";
    public static final String ACTION_HUAMI_IOS_ANCS_DEL_TO_WATCH = "action_huami_ios_ancs_del_to_watch";
    public static final String ACTION_HUAMI_IOS_ANCS_DEL_TO_PHONE = "action_huami_ios_ancs_del_to_phone";
    public static final String ACTION_HUAMI_IOS_ANCS_DEL_FROM_CARD = "action_huami_ios_ancs_del";
    public static final String ACTION_HUAMI_IOS_ANCS_BLOCK = "action_huami_ios_ancs_block";
    public static final String ACTION_HUAMI_CARD = "android.intent.action.huami_card";
    //
    public static final String ACTION_HUAMI_IOS_ANCS_SHOW = "action_huami_ios_ancs_show";
    //public static final String MOBILEPHONE_PKGNAME = "com.mi.watch";
    public static final String PKGNAME_MOBILEPHONE = "com.apple.mobilephone";
    public static final String PKGNAME_MIWATCH = "com.huami.watch";
    //public static final String ACTION_HUAMI_DATASEND_IOS_FORWATCH = "com.huami.watch.datasend_ios_forwatch";
    public static final String ACTION_HUAMI_DATASEND_IOS_FORWATCH = "com.huami.watch.datasend";
    public static final String ACTION_HUAMI_DATASEND_IOS_DIRECT = "com.huami.watch.datasend_ios_direct";
    public static final String ACTION_HUAMI_DATASEND_IOS_DIRECT8 = "com.huami.watch.datasend_ios_direct8";
    //public static final String ACTION_HUAMI_DATASEND_IOS_NOTITEST = "com.huami.watch.datasend_ios_notitest";
    public static final String ACTION_HUAMI_DATASEND_IOS_MYWATCH = "com.huami.watch.datasend_ios_mywatch";
    public static final String ACTION_HUAMI_DATASEND_IOS_MYWATCH_syntime = "syntime";
    public static final String ACTION_HUAMI_DATASEND_IOS_MYWATCH_unbind = "unbind";
    public static final String ACTION_HUAMI_DATASEND_IOS_MYWATCH_scanmode = "scanmode";
    //public static final String ACTION_HUAMI_DATASEND_IOS_MYWATCH_wifi_pass2 = "wifi_pass2";
    public static final String ACTION_HUAMI_DATASEND_IOS_FINDPHONE = "com.huami.watch.datasend_ios_findphone";
    //public static final String ACTION_HUAMI_DATASEND_IOS_MOBILEPHONE = "com.huami.watch.datasend_ios_mobilephone";
    //public static final String ACTION_HUAMI_DATASEND_IOS_UNBIND = "com.huami.watch.datasend_ios_unbind";
    //public static final String ACTION_WALL_RECEIVER = "android.intent.action.WallReceiver";
    //public static final String ACTION_WALL_RECEIVER = "android.intent.action.WallReceiver_IOS";
    public static final String ACTION_WALL_RECEIVER = "android.intent.action.WallReceiver";
    public static final String strSharedName = "bleconnect";

    private static final String SYSTEM_TIME = "SystemTime";
    private static final String SYSTEM_TIME_ZONE = "SystemTimeZone";
    public static final String ACTION_SYNC_TIME = TransporterModules.MOUDLE_COMPANION + ".transport.SyncTime";

    //
    private static final String TAG_SELF = TAG   + "   bt-TAG_SELF";
    private static final String TAG_REMOTE = TAG + "   bt-TAG_REMOTE";
    private static final String TAG_TIME = TAG + "   synctime";
    //huami service
    //private static final String HUAMI_DEVICE_NAME= "HuamiWatch";
    //
    //private int is_subscribed_characteristics = 0;
    // !ANCS constants
    public final static int MSG_CHECK_ANCS_CONN = 2;
    public final static int MSG_SET_BOND_CANCEL_STATUS = 3;
    public final static int MSG_ReSTART_ADVERTISING = 4;
    public final static int MSG_SHUTDOWN_ADVERTISING = 5;
    public final static int MSG_PROCESS_ANCS_DATA_SOURCE = 6;
    public final static int MSG_PAIRING_BACKUPSTART = 7;
    public final static int MSG_PAIRING_FINISH = 8;
    public final static int MSG_DELNOTI_FROM_WATCH= 9;
    public final static int MSG_RESET_RemoteGattReuse= 10;
    public final static int MSG_REMOVE_PHONECALL= 11;
    public final static int MSG_ADD_NOTIFICATION = 100;
    public final static int MSG_DO_NOTIFICATION = 101;
    public final static int MSG_RESET = 102;
    public final static int MSG_ERR = 103;
    public final static int MSG_CHECK_TIME = 104;
    public final static int MSG_FINISH = 105;
    public final static int FINISH_DELAY = 100;// 100 ms
    public final static int TIMEOUT = 5 * 1000;

    private Vibrator vib;
    private long pattern[] = {200, 100, 200, 100};

    //private int notification_id = 0;
    public int mPhoneCallNoti_id = -1;
    public int mPhoneCallNoti_times = 0;

    private PowerManager.WakeLock wake_lock;

    private PacketProcessor packet_processor;

    private BLEMessageReceiver message_receiver;// = new MessageReceiver();BroadcastReceiver
    private pkgBroadCast mPkgBC;// = new pkgBroadCast();
    private outerBroadCast mBroadCast;
    NotificationPoster mNotiPoster;

    //private byte[] uid = null;
    //public ArrayList<byte[]> mNotiArray = new ArrayList<byte[]>();
    //public HashMap<String,byte[]> mNotiArray = new HashMap<String,byte[]>();
    private ArrayList<Integer> mNotiArray = new ArrayList(20);
    public ArrayList<Integer> mDelNotiArray = new ArrayList(20);
    //public HashMap<String,BluetoothDevice> mDevices = new HashMap<String,BluetoothDevice>();
    public HashMap<String, BluetoothDevice> mDevices = new HashMap<String, BluetoothDevice>();
    public HashMap<String, Long> mDevices_time = new HashMap<String, Long>();


    ///////////////////////////////////////////////////////////
    HashMap<Integer, String> errmap = new HashMap<Integer, String>();
    //public static int mDelay = 0;
    //public static int mSleeping = 0;
    private static boolean mActive = false;

    public static final boolean isActive() {
        return mActive;
    }

    public static boolean mAncsConnected = false;
    public static boolean mCtrlConnected = false;
    //public static boolean mCmdStop = false;
    public static Context mContex;
    public static int mADVERTISE_TX_POWER = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
    public static int mADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
//
    //private final static String SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb";
    //private final static String CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb";

    //
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    public static BluetoothGattServer sGattServer = null;
    private BluetoothLeAdvertiser sAdvertiser = null;
    public static BluetoothDevice mBluetoothDevice_phone = null;
    public static BluetoothDevice mBluetoothDevice_newlywrite = null;
    public static BluetoothGattCharacteristic mBluetoothGattCharacteristic_to_phone2 = null;
    public static BluetoothGattCharacteristic mBluetoothGattCharacteristic_to_phone8 = null;
    public static BluetoothGattCharacteristic mBluetoothGattCharacteristic_to_phone9 = null;
    public static BluetoothGattCharacteristic mBluetoothGattCharacteristic_to_phone10 = null;
    //public static BluetoothGattCharacteristic mGattChar_to_phone = null;
    public BluetoothGatt mRemoteGatt = null;
    public BluetoothGatt mRemoteGattReuse = null;
    public String mRemoteGattReAddress = "";
    public long m_reverse_time = 0;
    public static long m_bInScanner_time = 0;
    public boolean mResetAdver = true;
    public long mSubsNotiTime=0;
    public long mLastNotiTime=0;
    //private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;


    //private int mHeartRate = 62;
    private static final long screen_time_out = 1000;
    private IconImageManager icon_image_manager = new IconImageManager();
    private int notification_id = 0;
    //
    AdvertiseCallback myAdvertiseCallback = new MyAdvertiseCallback();

    /**
     * 回调
     */
    private BLETransforListener bleListener;

    /**
     * 单例实例
     */

    private static BLEDataTransfor instance;

    public static BLEDataTransfor getInstance(Context context) {

        if (instance == null) {
//            synchronized (BLEDataTransfor.class) {
//                if (instance == null) {
                    instance = new BLEDataTransfor(context.getApplicationContext());
//                }
//            }
        }
        return instance;
    }

    //设置 监听

    public void setBLEListener(BLETransforListener bleListener) {
        this.bleListener = bleListener;
    }

    public BLEDataTransfor(Context context) {

        if(DEBUG){
            Log.i(TAG, "----M:BLEDataTransfor--init--");
        }
        errmap.put(0, "CONNECTION_PRIORITY_BALANCED");
        errmap.put(1, "CONNECTION_PRIORITY_HIGH");
        errmap.put(2, "CONNECTION_PRIORITY_LOW_POWER");
        errmap.put(143, "GATT_CONNECTION_CONGESTED");
        errmap.put(257, "GATT_FAILURE");
        errmap.put(5, "GATT_INSUFFICIENT_AUTHENTICATION");
        errmap.put(15, "GATT_INSUFFICIENT_ENCRYPTION");
        errmap.put(13, "GATT_INVALID_ATTRIBUTE_LENGTH");
        errmap.put(7, "GATT_INVALID_OFFSET");
        errmap.put(2, "GATT_READ_NOT_PERMITTED");
        errmap.put(6, "GATT_REQUEST_NOT_SUPPORTED");
        errmap.put(0, "GATT_SUCCESS");
        errmap.put(3, "GATT_WRITE_NOT_PERMITTED");
        //
        errmap.put(132, "GATT_BUSY");
        mContex = context;
        packet_processor = new PacketProcessor();
        InitSetting();
    }

    public void delNotiFromWatch(Intent intent)
    {
        do {
            byte[] uid=intent.getByteArrayExtra("uid");
            int iuid=uid_byte2int(uid);
            synchronized (mDelNotiArray)
            {
                mDelNotiArray.add(iuid);
            }
            mSimulatorHandler.removeMessages(MSG_DELNOTI_FROM_WATCH);
            mSimulatorHandler.sendEmptyMessageDelayed(MSG_DELNOTI_FROM_WATCH,3000);

        }while (1<0);
    }
    public void delNotiFromWatch2()
    {
        do {
            int iuid=-1;
            synchronized (mDelNotiArray)
            {
                if(mDelNotiArray.size()>0)
                    iuid=mDelNotiArray.remove(0);
            }
            if(iuid==-1)
                break;
            if (mRemoteGatt == null)
                break;
            byte[] uid=uid_int2byte(iuid);
            byte _action_id = 0x01;

            byte[] get_notification_attribute = {
                    (byte)0x02,
                    //UID
                    uid[0], uid[1], uid[2], uid[3],
                    //action
                    _action_id
            };
            writegatt(mRemoteGatt, get_notification_attribute);
            int icount=0;
            synchronized (mDelNotiArray)
            {
                icount=mDelNotiArray.size();
            }
            if(icount>0)
            {
                mSimulatorHandler.sendEmptyMessageDelayed(MSG_DELNOTI_FROM_WATCH,3000);
            }
        }while (1<0);
    }

    private class MyAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.v(TAG_SELF, "Advertise:onStartSuccess:"+settingsInEffect.toString());
            return;
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.v(TAG_SELF, "Advertise:onStartFailure");
            boolean D = true;
            String TAG = TAG_SELF;
            if (D) Log.e(TAG, "onStartFailure errorCode" + errorCode);

            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                if (D) {
                    //Toast.makeText(mContext, R.string.advertise_failed_data_too_large, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
                }
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                if (D) {
                    //Toast.makeText(mContext, R.string.advertise_failed_too_many_advertises, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to start advertising because no advertising instance is available.");
                }
            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                if (D) {
                    //Toast.makeText(mContext, R.string.advertise_failed_already_started, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to start advertising as the advertising is already started");
                }
            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                if (D) {
                    //Toast.makeText(mContext, R.string.advertise_failed_internal_error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Operation failed due to an internal error");
                }
            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                if (D) {
                    //Toast.makeText(mContext, R.string.advertise_failed_feature_unsupported, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "This feature is not supported on this platform");
                }
            } else {
                Log.e(TAG, "unknown error with errorCode:" + errorCode);
            }


            return;
        }


    }


    public Handler mSimulatorHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            int what = msg.what;
            if (MSG_CHECK_TIME == what) {
                Log.e(TAG_REMOTE, "msg timeout !");
                packet_processor.init(-1);
                mSimulatorHandler.sendEmptyMessage(MSG_DO_NOTIFICATION);
            } else if (MSG_SET_BOND_CANCEL_STATUS == what) {
                Cha9_setCancelStatus("0");
            } else if (MSG_ReSTART_ADVERTISING == what) {
                mSimulatorHandler.removeMessages(MSG_ReSTART_ADVERTISING);
                mSimulatorHandler.removeMessages(MSG_SHUTDOWN_ADVERTISING);
                //stopAdver();
                startAdver();
            } else if (MSG_SHUTDOWN_ADVERTISING == what) {
                mSimulatorHandler.removeMessages(MSG_ReSTART_ADVERTISING);
                mSimulatorHandler.removeMessages(MSG_SHUTDOWN_ADVERTISING);
                Log.e(TAG_SELF, "advertise timeout ! ancs:" + mAncsConnected);
                if (mAncsConnected) {
                    /*stopAdver();
                    setScanMode(BluetoothAdapter.SCAN_MODE_NONE);*/
                } else {
                    //stopAdver();
                    //startAdver();
                }
            }
            else if (MSG_DELNOTI_FROM_WATCH == what)//MSG_DELNOTI_FROM_WATCH
            {
                delNotiFromWatch2();
            }//
            else if (MSG_RESET_RemoteGattReuse == what)//MSG_DELNOTI_FROM_WATCH
            {
                try
                {
                    if(mRemoteGattReuse!=null)
                    {
                        mRemoteGattReuse.close();
                        mRemoteGattReuse=null;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }//MSG_REMOVE_PHONECALL
            else if (MSG_REMOVE_PHONECALL == what)//MSG_DELNOTI_FROM_WATCH
            {
                if(mPhoneCallNoti_times>0)
                {
//                    Intent intent1 = new Intent();
//                    intent1.setAction(ACTION_HUAMI_IOS_ANCS_DEL_TO_WATCH);
//                    intent1.putExtra("uid", uid_int2byte(mPhoneCallNoti_id));
//                    mContex.sendBroadcast(intent1);
                    bleCommunicates.sendNoticationMessage(TransportUri.ACTION_DEL ,null, uid_int2byte(mPhoneCallNoti_id), null, null,
                            System.currentTimeMillis(),bleListener);
                    mSimulatorHandler.sendEmptyMessageDelayed(MSG_REMOVE_PHONECALL, 2000);
                    mPhoneCallNoti_times--;
                }
                
            }
            //MSG_PROCESS_ANCS_DATA_SOURCE
            else if (MSG_PROCESS_ANCS_DATA_SOURCE == what) {
                byte[] get_data = (byte[]) msg.obj;
                Log.w(TAG_REMOTE, "UUID_TO_WATCH value length:: " + get_data.length);
                Log.w(TAG_REMOTE, "UUID_TO_WATCH value length:: " + byte2str(get_data));
                try {

                    //packet_processor.bout.write(get_data);
                    packet_processor.pushdata(get_data);
                    mSimulatorHandler.removeMessages(MSG_FINISH);
                    mSimulatorHandler.sendEmptyMessageDelayed(MSG_FINISH, FINISH_DELAY);
                } catch (Exception e) {
                    Log.w(TAG_REMOTE, e.toString());
                }
            }
            /*else if (MSG_PAIRING_BACKUPSTART == what) {
                mSimulatorHandler.removeMessages(MSG_PAIRING_BACKUPSTART);
                mSimulatorHandler.removeMessages(MSG_PAIRING_FINISH);
                Intent intent = new Intent("com.huami.watch.companion.action.PAIRING");
                intent.putExtra("pairingRestoreBackupStart", true);
                sendBroadcast(intent);
                mSimulatorHandler.sendEmptyMessageDelayed(MSG_PAIRING_FINISH,10000);
            }*/
            else if (MSG_PAIRING_FINISH == what) {
                mSimulatorHandler.removeMessages(MSG_PAIRING_BACKUPSTART);

                // 方案一
                if (SEND_MESSAGE_METHOD_STATUS) {
                    Intent intent = new Intent("com.huami.watch.companion.action.PAIRING");
                    intent.putExtra("pairingFinish", true);
                    mContex.sendBroadcast(intent);
                } else {
                    DataBundle dataBundle = new DataBundle();
                    dataBundle.putBoolean("pairingFinish", true);
                    bleCommunicates.sendSimpleMessage("com.huami.watch.companion.action.PAIRING",
                            dataBundle, bleListener
                    );
                }

            } else if (MSG_DO_NOTIFICATION == what) {
                askforNoti();
            } else if (MSG_RESET == what) {
                mSimulatorHandler.removeMessages(MSG_ADD_NOTIFICATION);
                mSimulatorHandler.removeMessages(MSG_DO_NOTIFICATION);
                mSimulatorHandler.removeMessages(MSG_RESET);
                mSimulatorHandler.removeMessages(MSG_ERR);
                packet_processor.init(-1);
                Log.e(TAG_REMOTE, "ANCSHandler reseted");
            } else if (MSG_ERR == what) {
                Log.e(TAG_REMOTE, "error, skip cur data");
                packet_processor.init(-1);
                mSimulatorHandler.sendEmptyMessage(MSG_DO_NOTIFICATION);
            } else if (MSG_FINISH == what) {
                //IOSNotification.log("msg  data.finish()");
                mSimulatorHandler.removeMessages(MSG_CHECK_TIME);
                //
                boolean bret=packet_processor.finish();
                if(bret)
                {
                    String ds_app_id=packet_processor.get_ds_app_id();
                    String ds_title=packet_processor.get_ds_title();
                    String ds_message=packet_processor.get_ds_message();
                    String ds_date=packet_processor.get_ds_date();
                    byte[] uid = packet_processor.get_uid();
                    //
                    String ds_title_old=ds_title;
                    if(ds_app_id.equalsIgnoreCase("com.apple.MobileSMS"))
                        ds_title = "短信";
                    else if(ds_app_id.equalsIgnoreCase("com.tencent.xin"))
                        ds_title = "微信";
                    //
                    /*
                    Log.w(TAG_REMOTE, "uid:" + byte2str(uid));
                    Log.w(TAG_REMOTE, "app_id:" + ds_app_id);
                    Log.w(TAG_REMOTE, "title:" + ds_title);
                    Log.w(TAG_REMOTE, "content:" + ds_message);
                    Log.w(TAG_REMOTE, "date:" + ds_date);*/
///////////////////////////////////////////////////////////////////////
                    if(ds_app_id.equalsIgnoreCase(PKGNAME_MIWATCH) && ds_message.contains("连接中"))
                    {
                        //setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                        if(mResetAdver) {
                            //stopAdver();
                            mResetAdver = false;
                            //startAdver();
                        }
                    }
                    else
                    {
                        boolean bnoti=true;
                        if(!ds_app_id.equalsIgnoreCase(PKGNAME_MOBILEPHONE))
                        {
                            String strq="select * from blacklist where _packagename='"+ds_app_id+"'";
                            ArrayList<HashMap<String,String>> arrs=dbOp.getInstance(mContex).sql2array(strq);
                            if(arrs.size()==0)
                            {
                                int iret=1;
                                strq="insert into blacklist(_packagename, _title,_bInBlacklist,_bsyn) values('"+ds_app_id+"','"+ds_title+"','0','0')";
                                iret=dbOp.getInstance(mContex).sql2cmd(strq);
                                if(iret==0)
                                    bnoti=false;
                                if(bnoti)
                                {
                                    ArrayList<byte[]> cmds = new ArrayList<byte[]>();
                                    byte[] cmdb = null;
                                    try {
                                        cmdb = Communicates.CMD_BLACKLIST_TO_PHONE.getBytes("UTF-8");
                                        cmds.add(0, cmdb);
                                        cmdb = "1".getBytes("UTF-8");
                                        cmds.add(1, cmdb);
                                        cmdb = ds_app_id.getBytes("UTF-8");
                                        cmds.add(2, cmdb);
                                        //
                                        cmdb = ds_title.getBytes("UTF-8");
                                        //
                                        cmds.add(3, cmdb);
                                        cmdb = "0".getBytes("UTF-8");
                                        cmds.add(4, cmdb);
                                        //
                                        byte[] wdata = bleCommunicates.EncodeDic(cmds);
                                        bleCommunicates.onWriteData(wdata,2);
                                        //
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            else
                            {
                                HashMap<String,String> hm=arrs.get(0);
                                String bin=hm.get("_bInBlacklist");
                                if(bin.equalsIgnoreCase("1"))
                                {
                                    bnoti=false;
                                }
                            }
                        }
///////////////////////////////////////////////////////////////////
                        long days=0;
                        long hours=0;
                        long minutes=0;
                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
                        try
                        {
                            ds_date=ds_date.replace('T',' ');
                            Date d1 = df.parse(ds_date);
                            Date d2 = new Date(System.currentTimeMillis());
                            long diff = d1.getTime() - d2.getTime();//这样得到的差值是微秒级别
                            if(diff<0)
                                diff=-diff;
                            days = diff / (1000 * 60 * 60 * 24);
                            hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
                            minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
                            //
                            Log.w(TAG_REMOTE, "" + days + "天" + hours + "小时" + minutes + "分");
                            //System.out.println(""+days+"天"+hours+"小时"+minutes+"分");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if(days>=2)
                        //else
                        {
                            if (SEND_MESSAGE_METHOD_STATUS) {
                                Intent intent1=new Intent(BLEService.ACTION_HUAMI_IOS_ANCS_DEL_TO_PHONE);
                                intent1.putExtra("uid", uid);
                                mContex.sendBroadcast(intent1);
                            } else {
                                DataBundle dataBundle = new DataBundle();
                                dataBundle.putByteArray("uid", uid);
                                bleCommunicates.sendSimpleMessage(ACTION_HUAMI_IOS_ANCS_DEL_TO_PHONE,
                                        dataBundle,
                                        bleListener
                                );
                            }
                        }
                        else
                        {
                            String strnoti=dbOp.getInstance(mContex).getConfigV("noti_push");
                            int iDEVICE_PROVISIONED=Settings.Global.getInt(mContex.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
                            if(iDEVICE_PROVISIONED>0 && bnoti && !strnoti.equalsIgnoreCase("0") && Settings.System.getInt(mContex.getContentResolver(), Constants.LOG_STATUS,1)>0 )
                            {
                                if(ds_app_id.equalsIgnoreCase(PKGNAME_MOBILEPHONE))
                                {
                                    //
                                    //方案一
                                    if (SEND_MESSAGE_METHOD_STATUS) {
                                        //ds_app_id, uid, ds_title, ds_message
                                        Intent _intent_positive = new Intent();
                                        _intent_positive.setAction(ACTION_HUAMI_IOS_ANCS_SHOW);
                                        _intent_positive.putExtra("uid", uid);
                                        _intent_positive.putExtra("pkgname",ds_app_id);
                                        _intent_positive.putExtra("title",ds_title_old);
                                        _intent_positive.putExtra("content",ds_message);
                                        mContex.sendBroadcast(_intent_positive);
                                    } else {
                                        DataBundle dataBundle = new DataBundle();
                                        dataBundle.putByteArray("uid", uid);
                                        dataBundle.putString("pkgname", ds_app_id);
                                        dataBundle.putString("title", ds_title_old);
                                        dataBundle.putString("content", ds_message);
                                        bleCommunicates.sendSimpleMessage(ACTION_HUAMI_IOS_ANCS_SHOW, dataBundle, bleListener);
                                    }
                                }
                                else
                                {

                                    if(days==0 && hours==0 && minutes<3) {
                                        if (mNotiPoster != null) {
                                            Log.i(TAG, "--------*****   app notification  *****---- pkgname:" + ds_app_id + " uid:" + uid + "ds_title:"+ds_title+",ds_message:" +ds_message);
                                            bleCommunicates.sendNoticationMessage(TransportUri.ACTION_ADD ,ds_app_id, uid, ds_title_old, ds_message,
                                                    System.currentTimeMillis(),bleListener);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                packet_processor.init(-1);
                mSimulatorHandler.removeMessages(MSG_DO_NOTIFICATION);
                mSimulatorHandler.sendEmptyMessageDelayed(MSG_DO_NOTIFICATION, 250);
                //mSimulatorHandler.sendEmptyMessage(MSG_DO_NOTIFICATION);
            } else if (msg.what == MSG_CHECK_ANCS_CONN) {
                if (!mAncsConnected) {
                    long curtime = System.currentTimeMillis();
                    Iterator iter = mDevices_time.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String) entry.getKey();
                        long val = (long) entry.getValue();
                        //if(curtime-val>20000)
                        if (curtime - val > 20000) {
                            /*mDevices_time.put(key, curtime);
                            BluetoothDevice device=mDevices.get(key);
                            if(device.getBondState()==BluetoothDevice.BOND_BONDED) {
                                String ibond = Cha9_getBondStatus();
                                Log.w(TAG_SELF, "start reverse connect 3......key:" + key + " ibond:" + ibond);
                                if (ibond.equalsIgnoreCase("1")) {
                                    if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                                        device.connectGatt(mContex, false, mReverseCallback);
                                }
                                m_reverse_time = System.currentTimeMillis();
                                break;
                            }*/
                        }
                    }
                }
                //if (!isDestroyed())
                {
                    Message msg2 = new Message();
                    msg2.what = MSG_CHECK_ANCS_CONN;
                    //handler.sendMessage(msg);
                    mSimulatorHandler.sendMessageDelayed(msg2, 10000);
                    //mSimulatorHandler.sendMessageDelayed(msg2, 5000);
                }
            }
        }
    };


    //
    public static BLEDataTransfor s() {
        return getInstance(mContex);
    }

    ///////////////////////////////////////////////////////////////////////////////////////


    private byte[] transToBytes(int num) {
        byte[] vals = new byte[3];

        vals[1] = (byte) num;

        return vals;
    }

    //
    private final BluetoothGattCallback mReverseCallback = new BluetoothGattCallback() {
        //
        //int mmindex=3;
        //
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.d(TAG_REMOTE, "onConnectionStateChange: " + status + " -> " + newState);
            //
            //mRemoteGatt=null;
            //
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                */
                Log.i(TAG_REMOTE, "Connected to GATT server.");
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            
                /*
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                */
                if (mRemoteGatt == gatt) {
                    synchronized(mNotiArray)
                    {
                        mNotiArray.clear();
                    }
                    synchronized(mDelNotiArray)
                    {
                        mDelNotiArray.clear();
                    }
                    Log.i(TAG_REMOTE, "Disconnected from right GATT server. mRemoteGatt:" + mRemoteGatt);
                    mResetAdver = true;
                    mRemoteGatt = null;
                    mAncsConnected = false;
                    onAncsAvailable(mAncsConnected);
                    //
                    mSimulatorHandler.removeMessages(MSG_ReSTART_ADVERTISING);
                    mSimulatorHandler.removeMessages(MSG_SHUTDOWN_ADVERTISING);

                    /*if(!mCmdStop)
                        mSimulatorHandler.sendEmptyMessageDelayed(MSG_ReSTART_ADVERTISING,0);*/
                    //onCtrlAvailable(mCtrlConnected);
                } else {
                    Log.i(TAG_REMOTE, "Disconnected from other GATT server:" + gatt);
                }
                //gatt.disconnect();
                //gatt.close();//newhubnewhub
                //broadcastUpdate(intentAction);
            }
        }

        public void SubscribeDataSource(BluetoothGatt gatt) {
            Log.d(TAG_REMOTE, "subscribeDataSource");
            BluetoothGattService service = gatt.getService(UUID.fromString(SampleGattAttributes.service_ancs));
            if (service == null) {
                Log.d(TAG_REMOTE, "cant find service:" + SampleGattAttributes.lookup(SampleGattAttributes.service_ancs, "service_ancs"));
                gatt.disconnect();
                //gatt.close();
                //
            } else {
                gatt.requestConnectionPriority(gatt.CONNECTION_PRIORITY_LOW_POWER);
                Log.d(TAG_REMOTE, "find service:" + SampleGattAttributes.lookup(SampleGattAttributes.service_ancs, "service_ancs"));
                //Log.d(TAG_LOG, String.valueOf(bluetooth_gatt.getServices()));

                // subscribe data source characteristic
                BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(UUID.fromString(SampleGattAttributes.ancs_data_source));

                if (data_characteristic == null) {
                    Log.d(TAG_REMOTE, "cant find data source chara");
                } else {
                    Log.d(TAG_REMOTE, "find data source chara ");
                    //Log.d(TAG_LOG, "set notify:: " + data_characteristic.getUuid());
                    gatt.setCharacteristicNotification(data_characteristic, true);
                    BluetoothGattDescriptor descriptor = data_characteristic.getDescriptor(
                            UUID.fromString(SampleGattAttributes.UUID_Descriptor));
                    if (descriptor == null) {
                        Log.d(TAG_REMOTE, " ** cant find data source desc :: ");
                    } else {
                        Log.d(TAG_REMOTE, " ** find data source desc :: ");
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                    }
                }
            }
        }

        public void SubscribeAms(BluetoothGatt gatt) {
            Log.d(TAG_REMOTE, "subscribeAms");
            BluetoothGattService service = gatt.getService(UUID.fromString(SampleGattAttributes.service_ams));
            if (service == null) {
                Log.d(TAG_REMOTE, "cant find service:" + SampleGattAttributes.lookup(SampleGattAttributes.service_ams, "service_ams"));
                //synctime(gatt);
            } else {
                Log.d(TAG_REMOTE, "find service:" + SampleGattAttributes.lookup(SampleGattAttributes.service_ams, "service_ams"));
                //Log.d(TAG_LOG, String.valueOf(bluetooth_gatt.getServices()));

                // subscribe data source characteristic
                BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(UUID.fromString(SampleGattAttributes.ams_entity_update));

                if (data_characteristic == null) {
                    Log.d(TAG_REMOTE, "cant find ams_entity_update chara");
                } else {
                    Log.d(TAG_REMOTE, "find ams_entity_update chara ");
                    //Log.d(TAG_LOG, "set notify:: " + data_characteristic.getUuid());
                    gatt.setCharacteristicNotification(data_characteristic, true);
                    BluetoothGattDescriptor descriptor = data_characteristic.getDescriptor(
                            UUID.fromString(SampleGattAttributes.UUID_Descriptor));
                    if (descriptor == null) {
                        Log.d(TAG_REMOTE, " ** cant find ams_entity_update desc :: ");
                    } else {
                        Log.d(TAG_REMOTE, " ** find ams_entity_update desc :: ");
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                    }
                }
            }
        }

        /**
         * 设置时区
         * @param characteristic
         */
        public void setTimeZone(BluetoothGattCharacteristic characteristic){
            Log.i(TAG_TIME, "setTimeZone...");
            final int  timeZoneKey = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8,0);
            Log.i(TAG_TIME, "setTimeZone: key = "+ timeZoneKey );
            if (timeZoneKey >= -48 || timeZoneKey <= 56) {
                // Save TimeZoneId
                String  timeZoneStr = Utils.getTimeZoneId(timeZoneKey);
                Utils.saveBLeConfigValue(mContex, Utils.BLE_PREFS_KEY_TIMEZONE_CONFIG, timeZoneStr);
                Log.i(TAG_TIME, "setTimeZone: save timezone to PREFS.");
            }
        }

        public void setTime(BluetoothGattCharacteristic characteristic) {

            Log.d(TAG_TIME, "setTime...");
            int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
            int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 2);
            int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 3);
            int hour = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
            int min = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 5);
            int sec = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 6);

            String timeZoneId = Utils.getBLeConfigValue(mContex, Utils.BLE_PREFS_KEY_TIMEZONE_CONFIG);
            if (timeZoneId == null) {
                Log.d(TAG_TIME, "setTime timeZoneId is null, get default.");
                timeZoneId = TimeZone.getDefault().getID();
            }
            Log.d(TAG_TIME, "setTime timeZoneId is " + timeZoneId);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
            c.set(year, month - 1, day, hour, min, sec);
            Log.d(TAG_TIME, "setTime currenttime is " + c.getTimeInMillis());
            DataBundle dataBundle = new DataBundle();
            dataBundle.putLong(SYSTEM_TIME, c.getTimeInMillis());
            dataBundle.putString(SYSTEM_TIME_ZONE, timeZoneId);
            bleCommunicates.sendSimpleMessage(TransporterModules.MOUDLE_COMPANION, ACTION_SYNC_TIME, dataBundle, bleListener);
            Log.d(TAG_TIME, "setTime done.");

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //Log.d(TAG_REMOTE,  "=======onServicesDiscovered received: " + status);

            Log.w(TAG_REMOTE, " onServicesDiscovered:: " + errmap.get(status) + ":" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "----onServicesDiscovered----");
                List<BluetoothGattService> servs = gatt.getServices();
                for (BluetoothGattService gattService : servs) {
                    //HashMap<String, String> currentServiceData = new HashMap<String, String>();
                    String uuid = gattService.getUuid().toString();
                    Log.w(TAG_REMOTE, " onServicesDiscovered:: " + uuid);
                }

                Log.d(TAG_TIME, "onServicesDiscovered done. syncTimeZone.");
                syncTimeZone(gatt);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            String uuid = characteristic.getUuid().toString();
            Log.w(TAG_REMOTE, " onCharacteristicRead:: " + SampleGattAttributes.lookup(uuid, "unknown character"));
            Log.w(TAG_REMOTE, " onCharacteristicRead:: " + errmap.get(status) + ":" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (uuid.equalsIgnoreCase(SampleGattAttributes.characteristics_current_time)) {
                    //set current time
                    setTime(characteristic);
                    SubscribeDataSource(gatt);
                }else if (SampleGattAttributes.characteristics_current_timezone.equalsIgnoreCase(uuid)){
                    //set current TimeZone
                    setTimeZone(characteristic);
                    synctime(gatt);
                }
            } else {
                if (uuid.equalsIgnoreCase(SampleGattAttributes.characteristics_current_timezone) || uuid.equalsIgnoreCase(SampleGattAttributes.characteristics_current_time) ) {
                    gatt.disconnect();
                    if (gatt == mRemoteGattReuse) {
                        Log.w(TAG_REMOTE, " onCharacteristicRead:: gatt == mRemoteGattReuse.");
                        mRemoteGattReuse = null;
                        mRemoteGattReAddress = null;
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            String uuid = characteristic.getUuid().toString();
            Log.w(TAG_REMOTE, " onCharacteristicWrite:: " + SampleGattAttributes.lookup(uuid, "unknown character"));
            Log.w(TAG_REMOTE, " onCharacteristicWrite:: " + errmap.get(status) + ":" + status);
            if (uuid.equalsIgnoreCase(SampleGattAttributes.ams_remote_command)) {
                if (MessageReceiver.amsFlag > 0) {
                    MessageReceiver.amsFlag--;
                    Log.i(TAG_REMOTE, "--------*****   find Phone   *****----------");
                    do {
                        if (BLEDataTransfor.s().mRemoteGatt == null)
                            break;
                        //byte[] bytes=new byte[]{(byte) 0x01};
                        byte[] bytes = new byte[]{(byte) 0x05};
                        BLEDataTransfor.s().writegatt_ams(BLEDataTransfor.s().mRemoteGatt, bytes);
                    } while (1 < 0);
                }
            } else if (uuid.equalsIgnoreCase(SampleGattAttributes.ams_entity_update)) {
                //synctime(gatt);
            }
            //Log.d(TAG,  "=======onCharacteristicWrite : " + status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.d(TAG_REMOTE, "=======onReliableWriteCompleted : " + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            String uuid = characteristic.getUuid().toString();
            Log.w(TAG_REMOTE, " onCharacteristicChanged:: " + SampleGattAttributes.lookup(uuid, "unknown character"));
            //
            mRemoteGatt = gatt;

            if (SampleGattAttributes.ancs_data_source.toString().equalsIgnoreCase(uuid)) {
                Log.i(TAG_REMOTE, "--------*****   BLE  to   watch  with app notification  *****----------");
                byte[] get_data = characteristic.getValue();
                Message msg = new Message();
                msg.what = MSG_PROCESS_ANCS_DATA_SOURCE;
                if (get_data != null) {
                    msg.obj=get_data.clone();
                } else {
                    Log.i(TAG_REMOTE, "error the ancs data is null!");
                }
                mSimulatorHandler.sendMessage(msg);
                /*

                Log.w(TAG_REMOTE, "UUID_TO_WATCH value length:: " + get_data.length);
                try {

                    //packet_processor.bout.write(get_data);
                    packet_processor.pushdata(get_data);
                    mSimulatorHandler.removeMessages(MSG_FINISH);
                    mSimulatorHandler.sendEmptyMessageDelayed(MSG_FINISH, FINISH_DELAY);
                } catch (Exception e) {
                    Log.w(TAG_REMOTE, e.toString());
                }
*/


            }
            //notify from characteristic notification characteristic
            else if (SampleGattAttributes.ancs_notification_source.toString().equals(uuid)) {
                //Log.d(TAG_LOG, "get notify from notification source chara");
                Log.i(TAG_REMOTE, "----ancs_notification_source-----");

                byte[] data = characteristic.getValue();
                int iDEVICE_PROVISIONED = Settings.Global.getInt(mContex.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
                if (iDEVICE_PROVISIONED > 0 && data != null && data.length > 0) {
                    byte[] uid2 = new byte[4];
                    uid2[0] = data[4];
                    uid2[1] = data[5];
                    uid2[2] = data[6];
                    uid2[3] = data[7];
                    String struid2 = byte2str(uid2);
                    int iuid = uid_byte2int(uid2);

                    int eventId = data[0] & 255;
                    int eventFlags = data[1] & 255;
                    int categoryId = data[2] & 255;
                    int categoryCount = data[3] & 255;
                    String delimitor = new String("-");
                    String spaceString = new String("");
                    String dataString = spaceString + eventId + delimitor + eventFlags + delimitor + categoryId + delimitor + categoryCount + delimitor + struid2;
                    //Log.w(TAG_REMOTE, "notify:"+struid2+" data:" + dataString);
                    Log.w(TAG_REMOTE, "notify data:" + dataString);

                    if (String.format("%02X", data[0]).equals("00")) {

                        Log.w(TAG_REMOTE, "get notify:" + struid2 + "  iuid = " + iuid);
                        boolean bforget=false;
                        long curtime=System.currentTimeMillis();
                        if(curtime<mSubsNotiTime+30000 && curtime>=mSubsNotiTime)//close to subNoti
                        {
                            if(curtime<mLastNotiTime+100 && curtime>=mLastNotiTime)
                            {
                                Log.w(TAG_REMOTE, "noti is too close to subNoti, so forget it");
                                bforget=true;
                            }
                        }
                        mLastNotiTime=curtime;
                        if(categoryId!=2 && bforget!=true)
                        {
                            int badd=0;
                            synchronized(mNotiArray)
                            {
                                if (!mNotiArray.contains(iuid)) {
                                    if(categoryId==1)
                                        mNotiArray.add(0,iuid);
                                    else
                                        mNotiArray.add(iuid);
                                    badd=1;
                                }
                            }
                            if(badd>0)
                            {
                                mSimulatorHandler.removeMessages(MSG_DO_NOTIFICATION);
                                long idelay = 100;
                                mSimulatorHandler.sendEmptyMessageDelayed(MSG_DO_NOTIFICATION, idelay);
                            }
                            else
                            {
                                    Log.w(TAG_REMOTE, "multi notify:" + struid2);
                                }

                            }

                    } else if (String.format("%02X", data[0]).equals("01")) {
                        Log.w(TAG_REMOTE, "modify notification" + struid2 + "  iuid = " + iuid);
                    } else if (String.format("%02X", data[0]).equals("02")) {
                        Log.w(TAG_REMOTE, "delete notification" + struid2 + "  iuid = " + iuid);
                        int bremove=0;
                        synchronized(mNotiArray)
                        {
                            if(mNotiArray.contains(iuid)) {
                                int ipos=mNotiArray.indexOf(iuid);
                                if(ipos>=0)
                                    mNotiArray.remove(ipos);
                                bremove=1;
                            }
                        }
                        if(bremove>0)
                        {
                            Log.w(TAG_REMOTE, "remove newly built notify:" + struid2);
                        }
                        else
                        {
//                            Intent intent1 = new Intent();
//                            intent1.setAction(ACTION_HUAMI_IOS_ANCS_DEL_TO_WATCH);
//                            intent1.putExtra("uid", uid2);
//                            mContex.sendBroadcast(intent1);
                            bleCommunicates.sendNoticationMessage(TransportUri.ACTION_DEL ,null, uid2, null, null,
                                    System.currentTimeMillis(),bleListener);
                            if(categoryId==1)
                            {
                            mPhoneCallNoti_id = iuid;
                            mPhoneCallNoti_times = 5;
                            mSimulatorHandler.sendEmptyMessageDelayed(MSG_REMOVE_PHONECALL, 2000);
                            }

                        }
                    }
                    /*
                    try {


                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.w(TAG_REMOTE, "error");
                        e.printStackTrace();
                    }*/
                }
            }
            else if (uuid.equalsIgnoreCase(SampleGattAttributes.characteristics_current_time)) {
                Log.i(TAG_REMOTE, "--------*****   ancs current time    *****----------");
                syncTimeZone(gatt);
            }
        }

        /////////////////////////////////////////////////////
        /**
         * 同步时区
         * @param gatt
         */
        public void syncTimeZone(BluetoothGatt gatt){
            Log.d(TAG_TIME, "syncTimeZone");
            BluetoothGattService _service = gatt.getService(UUID.fromString(SampleGattAttributes.service_cts));
            if (_service == null) {
                Log.d(TAG_TIME, "cant find service service_cts");
                gatt.disconnect();
            } else {
                Log.d(TAG_TIME, "find service cts timezone");
                //Log.d(TAG_REMOTE, String.valueOf(bluetooth_gatt.getServices()));

                // subscribe data source characteristic
                BluetoothGattCharacteristic data_characteristic = _service.getCharacteristic(UUID.fromString(SampleGattAttributes.characteristics_current_timezone));

                if (data_characteristic == null) {
                    Log.d(TAG_TIME, "cant find data characteristics_timeZone chara");
                } else {
                    Log.d(TAG_TIME, "find characteristics_timeZone :: " + data_characteristic.getUuid());
                    //first read
                    gatt.readCharacteristic(data_characteristic);
                }

            }
        }
        /**
         * 同步时间
         * @param gatt
         */
        public void synctime(BluetoothGatt gatt) {
            Log.d(TAG_TIME, "synctime");
            BluetoothGattService _service = gatt.getService(UUID.fromString(SampleGattAttributes.service_cts));
            if (_service == null) {
                Log.d(TAG_TIME, "cant find service service_cts");
                gatt.disconnect();
            } else {
                Log.d(TAG_TIME, "find service cts");
                //Log.d(TAG_REMOTE, String.valueOf(bluetooth_gatt.getServices()));

                // subscribe data source characteristic
                BluetoothGattCharacteristic data_characteristic = _service.getCharacteristic(UUID.fromString(SampleGattAttributes.characteristics_current_time));

                if (data_characteristic == null) {
                    Log.d(TAG_TIME, "cant find data characteristics_current_time chara");
                } else {
                    Log.d(TAG_TIME, "find characteristics_current_time :: " + data_characteristic.getUuid());
                    //first read
                    gatt.readCharacteristic(data_characteristic);
                }

            }
        }

        public void subscribetime(BluetoothGatt gatt) {
            Log.d(TAG_TIME, "subscribetime");
            BluetoothGattService _service = gatt.getService(UUID.fromString(SampleGattAttributes.service_cts));
            if (_service == null) {
                Log.d(TAG_TIME, "cant find service service_cts");
            } else {
                Log.d(TAG_TIME, "find service cts");
                //Log.d(TAG_REMOTE, String.valueOf(bluetooth_gatt.getServices()));

                // subscribe data source characteristic
                BluetoothGattCharacteristic data_characteristic = _service.getCharacteristic(UUID.fromString(SampleGattAttributes.characteristics_current_time));

                if (data_characteristic == null) {
                    Log.d(TAG_TIME, "cant find data characteristics_current_time chara");
                } else {
                    Log.d(TAG_TIME, "find characteristics_current_time :: " + data_characteristic.getUuid());
                    //first read
                    //gatt.readCharacteristic(data_characteristic);
                    //then subscribe
                    gatt.setCharacteristicNotification(data_characteristic, true);
                    BluetoothGattDescriptor descriptor = data_characteristic.getDescriptor(
                            UUID.fromString(SampleGattAttributes.UUID_Descriptor));
                    if (descriptor == null) {
                        Log.d(TAG_TIME, " ** cant find cts desc :: ");
                    } else {
                        Log.d(TAG_TIME, " ** find cts desc :: ");
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                    }
                }

            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            String uuid = descriptor.getCharacteristic().getUuid().toString();
            Log.w(TAG_REMOTE, " onDescriptorWrite:: " + SampleGattAttributes.lookup(uuid, "unknown character"));
            Log.w(TAG_REMOTE, " onDescriptorWrite:: " + errmap.get(status) + ":" + status);
            // Notification source
            //synctime(gatt);
            //
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //
                Cha9_setBondStatus();
                //if(uuid.equalsIgnoreCase(SampleGattAttributes.characteristics_data_source))
                //{
                //if (is_subscribed_characteristics==0) {
                if (uuid.equalsIgnoreCase(SampleGattAttributes.ancs_data_source)) {
                    //subscribe characteristic notification characteristic
                    BluetoothGattService service = gatt.getService(UUID.fromString(SampleGattAttributes.service_ancs));
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(SampleGattAttributes.ancs_notification_source));
//
                    if (characteristic == null) {
                        Log.d(TAG_REMOTE, " cant find noti chara");
                    } else {
                        Log.d(TAG_REMOTE, " ** find noti chara :: ");
                        if (SampleGattAttributes.ancs_notification_source.equalsIgnoreCase(characteristic.getUuid().toString())) {
                            Log.d(TAG_REMOTE, " set notify for noti char");
                            gatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor notify_descriptor = characteristic.getDescriptor(
                                    UUID.fromString(SampleGattAttributes.UUID_Descriptor));
                            if (notify_descriptor == null) {
                                Log.d(TAG_REMOTE, " ** not find noti desc :: ");
                            } else {
                                Log.d(TAG_REMOTE, " ** find noti desc :: ");
                                notify_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(notify_descriptor);
                                //is_subscribed_characteristics = 1;
                            }
                        }
                    }
                } else if (uuid.equalsIgnoreCase(SampleGattAttributes.ancs_notification_source)) {
                    mSubsNotiTime=System.currentTimeMillis();
                    //
                    mAncsConnected = true;
                    onAncsAvailable(mAncsConnected);
                    //setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
                    //
                    mSimulatorHandler.removeMessages(MSG_ReSTART_ADVERTISING);
                    mSimulatorHandler.removeMessages(MSG_SHUTDOWN_ADVERTISING);
                    //mSimulatorHandler.sendEmptyMessageDelayed(MSG_ReSTART_ADVERTISING,2000);
                    //
                    //BluetoothAdapter.getDefaultAdapter().setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
                    //onCtrlAvailable(mCtrlConnected);
                    //
                    //mBluetoothDevice_phone =gatt.getDevice();// device;
                    //
                    mRemoteGatt = gatt;
                    //
                    // get current time
                    //Log.d(TAG_REMOTE, ":get time+_=-=_=-+-+-+-=_=_=_+-=-=-=-=");

                    //synctime(gatt);
                    subscribetime(gatt);
                } else if (uuid.equalsIgnoreCase(SampleGattAttributes.characteristics_current_time)) {
                    SubscribeAms(gatt);
                } else if (uuid.equalsIgnoreCase(SampleGattAttributes.ams_entity_update)) {
                    //find music controll service
                    //Log.d(TAG_REMOTE, "*+*+*+*+*+*+*+*+*+*+ find music control");
                    BluetoothGattService service = gatt.getService(UUID.fromString(SampleGattAttributes.service_ams));
                    if (service != null) {
                        BluetoothGattCharacteristic chara = service.getCharacteristic(UUID.fromString(SampleGattAttributes.ams_entity_update));
                        if (chara != null) {
                            chara.setValue(new byte[]{(byte) 0x02, (byte) 0x00, (byte) 0x02});
                            gatt.writeCharacteristic(chara);
                        }
                    }
                }
            }
            /*else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
                Log.d(TAG_REMOTE, "status: write not permitted remove authrization");
                removeBond(gatt);
            }*/
            else if (status != gatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if(
                        uuid.equalsIgnoreCase(SampleGattAttributes.ancs_data_source)
                        ||
                        uuid.equalsIgnoreCase(SampleGattAttributes.ancs_notification_source)
                        )
                gatt.disconnect();
                mSimulatorHandler.sendEmptyMessage(MSG_RESET_RemoteGattReuse);
            }
            /*else if (status == 132) {//GATT_BUSY
                //Log.d(TAG_REMOTE, "status: write not permitted");
                if (uuid.equalsIgnoreCase(SampleGattAttributes.ancs_data_source)) {
                    //SubscribeDataSource(gatt);
                }

            }*/
        }
    };

    public void removeBond(BluetoothGatt gatt) {
        if (gatt == null) {
            gatt = mRemoteGatt;
        }
        if (gatt != null) {
            Log.d(TAG_REMOTE, "remove bond!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //remove authrization
            Method method = null;
            try {
                method = gatt.getDevice().getClass().getMethod("removeBond", (Class[]) null);
                method.invoke(gatt.getDevice(), (Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            gatt.disconnect();
        }
    }

    private class MyGattServerCallback extends BluetoothGattServerCallback {

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
//
            //device.getType()
            String struuid = characteristic.getUuid().toString();
            String strname = SampleGattAttributes.lookup(struuid, "unknown chara");
            //
            Log.v(TAG_SELF, "onCharacteristicReadRequest test2 device: " + device
                    + " requestId: " + requestId + " offset: " + offset
                    + " chara: " + strname);

            String valStr = "75";
            byte[] value = valStr.getBytes();


            //dumpCharacteristic(characteristic);
            /*int num = mHeartRate;
            byte[] value = transToBytes(num);
            */
            //byte[] value={0x37};
            sGattServer.sendResponse(device, requestId,
                    BluetoothGatt.GATT_SUCCESS, offset, value);

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId, BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded, int offset,
                                                 byte[] value) {
            //
            String struuid = characteristic.getUuid().toString();
            String strname = SampleGattAttributes.lookup(struuid, "unknown chara");
            //
            //String myValue = new String(value);

            /*Log.v(TAG_SELF, "onCharacteristicWriteRequest char: " + strname
                    + " requestId: " + requestId + " offset" + offset
                    + " preparedWrite:"
                    + preparedWrite + " responseNeeded:" + responseNeeded
                    + " value: " + byte2str(value));*/
            //
            Log.v(TAG_SELF, "struuid:" + struuid);
            if (struuid.equalsIgnoreCase(SampleGattAttributes.UUID_HUAMI_COMM2)) {
                //mGattChar_to_phone=mBluetoothGattCharacteristic_to_phone2;
                do {
                    if (value.length == 0)
                        break;
                    if (value[0] != SampleGattAttributes.idcmd) {
                        Log.v(TAG_SELF, "received bad char cmd: " + value[0] + " length:" + value.length);
                        break;
                    }
                    //
                    mBluetoothDevice_newlywrite = device;
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        mBluetoothDevice_phone = device;
                        if (!mCtrlConnected) {
                            mCtrlConnected = true;
                            onCtrlAvailable(mCtrlConnected);
                        }
                    }
                    //
                    ByteArrayOutputStream tmpd = new ByteArrayOutputStream();
                    tmpd.write(value, 1, value.length - 1);
                    bleCommunicates.processing(tmpd.toByteArray(), 2, bleListener);

                } while (1 < 0);
            }
            if (struuid.equalsIgnoreCase(SampleGattAttributes.UUID_HUAMI_COMM8)) {
                //mGattChar_to_phone=mBluetoothGattCharacteristic_to_phone2;
                do {
                    if (value.length == 0)
                        break;
                    if (value[0] != SampleGattAttributes.idcmd) {
                        Log.v(TAG_SELF, "received bad char cmd: " + value[0] + " length:" + value.length);
                        break;
                    }
                    //
                    //mBluetoothDevice_newlywrite=device;
                    //
                    ByteArrayOutputStream tmpd = new ByteArrayOutputStream();
                    tmpd.write(value, 1, value.length - 1);
                    bleCommunicates.processing(tmpd.toByteArray(), 8, bleListener);

                } while (1 < 0);
            } else if (struuid.equalsIgnoreCase(SampleGattAttributes.UUID_HUAMI_COMM9)) {
                Cha9_sendStatus(device);
            } else if (SampleGattAttributes.UUID_HUAMI_COMM10.equalsIgnoreCase(struuid)){
                bleCommunicates.processing_AGPS_Data(value,bleListener);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status,
                                            int newState) {
            String straddr = device.getAddress();
            Log.i(TAG_SELF, "onConnectionStateChange device 1: " + device
                    + " status: " + status + " newState: " + newState + " bonded:" + device.getBondState());
            //mConnectionState = newState;
            int bondstate = device.getBondState();
            String strbond = Cha9_getBondStatus();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                ///////////////
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.w(TAG_SELF, "start reverse connect 1......addr:" + straddr + " initbond:" + strbond + " bondstate:" + bondstate);
                    if (mRemoteGattReuse != null && mRemoteGattReAddress.equalsIgnoreCase(device.getAddress())) {
                        Log.w(TAG_SELF, "mRemoteGattReuse connect old.");
                        mRemoteGattReuse.connect();
                    } else {
                        Log.w(TAG_SELF, "mRemoteGattReuse connect new.");
                        mRemoteGattReuse = device.connectGatt(mContex, false, mReverseCallback);
                        mRemoteGattReAddress = device.getAddress();
                    }
                    m_reverse_time = System.currentTimeMillis();
                    Log.w(TAG_SELF, "mRemoteGattReuse set m_reverse_time = " + m_reverse_time);
                }

                mDevices.put(straddr, device);
                mDevices_time.put(straddr, System.currentTimeMillis());


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mDevices.containsKey(straddr))
                    mDevices.remove(straddr);
                if (mDevices_time.containsKey(straddr))
                    mDevices_time.remove(straddr);
                if (mBluetoothDevice_phone != null && mBluetoothDevice_phone.getAddress().equalsIgnoreCase(straddr)) {
                    mBluetoothDevice_phone = null;
                    mCtrlConnected = false;
                    //onAncsAvailable(mAncsConnected);
                    onCtrlAvailable(mCtrlConnected);
                    Log.i(TAG_SELF, "onConnectionStateChange disconnect right device!");
                    //
                    //mConnected=false;//
                } else {
                    Log.i(TAG_SELF, "onConnectionStateChange disconnect normal device.");
                }
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device,
                                            int requestId, int offset, BluetoothGattDescriptor descriptor) {
            //
            String struuid = descriptor.getCharacteristic().getUuid().toString();
            String strname = SampleGattAttributes.lookup(struuid, "unknown chara");
            //
            Log.v(TAG_SELF, "onDescriptorReadRequest char: " + strname
                    + " requestId: " + requestId + " offset" + offset
                    + " descriptor: " + descriptor);
            sGattServer.sendResponse(device, requestId,
                    BluetoothGatt.GATT_SUCCESS, offset,
                    (byte[]) transToBytes(49));// just for test
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device,
                                             int requestId, BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded, int offset,
                                             byte[] value) {
            String struuid = descriptor.getCharacteristic().getUuid().toString();
            String strname = SampleGattAttributes.lookup(struuid, "unknown chara");
            Log.v(TAG_SELF, "onDescriptorWriteRequest chara: " + strname
                    + " requestId: " + requestId + " offset" + offset
                    + " descriptor: " + descriptor + " preparedWrite: "
                    + preparedWrite + " responseNeeded: " + responseNeeded
                    + " value: " + byte2str(value));
            //////////////////////////////
            if (responseNeeded) {
                Log.v(TAG_SELF, "write response SUCCESS");
                sGattServer.sendResponse(device, requestId,
                        BluetoothGatt.GATT_SUCCESS, offset, value);
            }
            //////////////////////////////
            if (struuid.equalsIgnoreCase(SampleGattAttributes.UUID_HUAMI_COMM2)) {
                //mGattChar_to_phone=mBluetoothGattCharacteristic_to_phone2;
                if (value.length == 2) {
                    if (value[0] == 0 && value[1] == 0) {
                        if (mBluetoothDevice_phone != null && mBluetoothDevice_phone.getAddress().equalsIgnoreCase(device.getAddress())) {
                            mBluetoothDevice_phone = null;
                            mCtrlConnected = false;
                            //onAncsAvailable(mAncsConnected);
                            onCtrlAvailable(mCtrlConnected);
                            Log.i(TAG_SELF, "onDescriptorWriteRequest disconnect right device!");
                            //
                            //mConnected=false;//
                        }
                    } else if (value[0] == 1 && value[1] == 0) {

                    }

                }
            } else if (struuid.equalsIgnoreCase(SampleGattAttributes.UUID_HUAMI_COMM9)) {
                if (value.length == 2) {
                    if (value[0] == 1 && value[1] == 0) {
                        Cha9_sendStatus(device);
                    }
                }
            }

        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId,
                                   boolean execute) {

            Log.v(TAG_SELF, "onExecuteWrite device: " + device + " requestId: "
                    + requestId + " execute: " + execute);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d(TAG_SELF, "======onServiceAdded my status: " + status
                    + " service: " + service);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // sGattServer.startAdvertising();
                //AdvertiseCallback myAdvertiseCallback = new MyAdvertiseCallback();
                //startAdver0(10000);
                //startAdver0(30000);
                //startAdver0(0);
                startAdver();
                //startAdver();
            }
        }

    }

    /*public void startAdver0(long delayMillis)
    {
        mSimulatorHandler.removeMessages(MSG_START_ADVERTISING);
        mSimulatorHandler.sendEmptyMessageDelayed(MSG_START_ADVERTISING,delayMillis);
    }*/
    public void connectAncsOutside(BluetoothDevice device)//after bonded
    {
        String strbond = Cha9_getBondStatus();
        int bondstate = device.getBondState();
        if (!mAncsConnected) {
            if (System.currentTimeMillis() - m_reverse_time > 20000) {

                Log.w(TAG_SELF, "connectAncsOutside: start reverse connect new 20......addr:" + device.getAddress() + " initbond:" + strbond + " bonded:" + bondstate);
                mRemoteGattReuse = device.connectGatt(mContex, false, mReverseCallback);
                mRemoteGattReAddress = device.getAddress();
                m_reverse_time = System.currentTimeMillis();
                Log.w(TAG_SELF, "mRemoteGattReuse set m_reverse_time = " + m_reverse_time);
            } else {
                Log.v(TAG_SELF, "connectAncsOutside: can not start reverse connect 20.........");
            }
        }
    }

    public void ReverseConnectOutside(String strInScanner) {
        BluetoothDevice device = mBluetoothDevice_newlywrite;
        String strbond = Cha9_getBondStatus();
        int bondstate = device.getBondState();
        String straddr = device.getAddress();
        Log.i(TAG_SELF, "ReverseConnectOutside addr:" + straddr + "initbond:" + strbond + " bondstate:" + bondstate);
        if (strbond.equalsIgnoreCase("1")) {
            if (bondstate == BluetoothDevice.BOND_BONDED)
            //if(ibond==BluetoothDevice.BOND_BONDED || ibond==BluetoothDevice.BOND_NONE )
            {
                /*if(mCommunicates!=null) {
                    mCommunicates.mSendStart = 0;
                    mCommunicates.mSendEnd = 0;
                }*/ //it should be put at other place.//NEWHUB
                //
                mBluetoothDevice_phone = device;
                //if(!mCtrlConnected)
                {
                    mCtrlConnected = true;
                    onCtrlAvailable(mCtrlConnected);
                }
                mCtrlConnected = true;
                //
                if (!mAncsConnected) {
                    if (System.currentTimeMillis() - m_reverse_time > 20000) {
                        Log.w(TAG_SELF, "start reverse connect 21......addr:" + device.getAddress() + " initbond:" + strbond + " bonded:" + bondstate);
                        if (mRemoteGattReuse != null && mRemoteGattReAddress.equalsIgnoreCase(device.getAddress())) {
                            int status = mBluetoothManager.getConnectionState(device, BluetoothGatt.GATT_SERVER);
                            Log.w(TAG_SELF, "mRemoteGattReuse connect old status = " + status);
                            switch (status) {
                                case BluetoothProfile.STATE_CONNECTED:
                                    Log.i(TAG_REMOTE, "GATT server already Connected.");
                                    mRemoteGattReuse.disconnect();
                                    mRemoteGattReuse = null;
                                    mRemoteGattReAddress = null;
                                    break;
                                case BluetoothProfile.STATE_DISCONNECTED:
                                    Log.i(TAG_REMOTE, "GATT server is disconnected.");
                                    mRemoteGattReuse.connect();
                                    break;
                            }
                        } else {
                            Log.w(TAG_SELF, "mRemoteGattReuse connect new");
                            mRemoteGattReuse = device.connectGatt(mContex, false, mReverseCallback);
                            mRemoteGattReAddress = device.getAddress();
                        }
                        m_reverse_time = System.currentTimeMillis();
                        Log.w(TAG_SELF, "mRemoteGattReuse set m_reverse_time = " + m_reverse_time);
                    } else {
                        Log.v(TAG_SELF, "ReverseConnectOutside: can not start reverse connect 21.........");
                    }
                }
            } else if (bondstate == BluetoothDevice.BOND_NONE) {
                Log.w(TAG_SELF, "create bond 1.........");
                device.createBond();
            } else if (bondstate == BluetoothDevice.BOND_BONDING) {

            }
        } else {
            if (strInScanner.equalsIgnoreCase("1")) {
                /*if(mCommunicates!=null) {
                    mCommunicates.mSendStart = 0;
                    mCommunicates.mSendEnd = 0;
                }*/ //it should be put at other place.//NEWHUB
                do {
                    if (bondstate == BluetoothDevice.BOND_BONDING)
                        break;
                    if (bondstate == BluetoothDevice.BOND_BONDED)
                    //if(ibond==BluetoothDevice.BOND_BONDED || ibond==BluetoothDevice.BOND_NONE)
                    {
                        mBluetoothDevice_phone = device;
                        //if(!mCtrlConnected)
                        {
                            mCtrlConnected = true;
                            onCtrlAvailable(mCtrlConnected);
                        }
                        mCtrlConnected = true;
                        //

                        //
                        if (!mAncsConnected) {
                            if (System.currentTimeMillis() - m_reverse_time > 20000) {
                                Log.w(TAG_SELF, "start reverse connect 22......addr:" + device.getAddress() + " initbond:" + strbond + " bonded:" + bondstate);
                                //if(strbond.equalsIgnoreCase("1"))
                                if (mRemoteGattReuse != null && mRemoteGattReAddress.equalsIgnoreCase(device.getAddress())) {
                                    int status = mBluetoothManager.getConnectionState(device, BluetoothGatt.GATT_SERVER);
                                    Log.w(TAG_SELF, "mRemoteGattReuse connect old status = " + status);
                                    switch (status) {
                                        case BluetoothProfile.STATE_CONNECTED:
                                            Log.i(TAG_REMOTE, "GATT server already Connected.");
                                            mRemoteGattReuse.disconnect();
                                            mRemoteGattReuse = null;
                                            mRemoteGattReAddress = null;
                                            break;
                                        case BluetoothProfile.STATE_DISCONNECTED:
                                            Log.i(TAG_REMOTE, "GATT server is disconnected.");
                                            mRemoteGattReuse.connect();
                                            break;
                                    }
                                } else {
                                    Log.w(TAG_SELF, "mRemoteGattReuse connect new");
                                    mRemoteGattReuse = device.connectGatt(mContex, false, mReverseCallback);
                                    mRemoteGattReAddress = device.getAddress();
                                }
                                m_reverse_time = System.currentTimeMillis();
                                Log.w(TAG_SELF, "mRemoteGattReuse set m_reverse_time = " + m_reverse_time);
                            } else {
                                Log.v(TAG_SELF, "ReverseConnectOutside: can not start reverse connect 22.........");
                            }
                        }
                    } else {
                        Log.w(TAG_SELF, "create bond 2.........");
                        device.createBond();
                    }

                } while (false);
            }
        }
    }

    public String Cha9_getBondStatus() {
        SharedPreferences mPreferenceBlue = mContex.getSharedPreferences(BLEDataTransfor.strSharedName,
                Context.MODE_PRIVATE);
        String ibond = mPreferenceBlue.getString("ibond",
                "0");
        return ibond;
        //return "0";
    }

    public void Cha9_setBondStatus() {
        SharedPreferences mPreferenceBlue = mContex.getSharedPreferences(BLEDataTransfor.strSharedName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mPreferenceBlue.edit();
        edit.putString("ibond", "1");
        edit.commit();
    }

    public String Cha9_getCancelStatus() {
        SharedPreferences mPreferenceBlue = mContex.getSharedPreferences(BLEDataTransfor.strSharedName,
                Context.MODE_PRIVATE);
        String ibond = mPreferenceBlue.getString("icancel",
                "0");
        return ibond;
        //return "0";
    }

    public void Cha9_setCancelStatus(String strcancel) {
        SharedPreferences mPreferenceBlue = mContex.getSharedPreferences(BLEDataTransfor.strSharedName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mPreferenceBlue.edit();
        edit.putString("icancel", strcancel);
        edit.commit();
    }

    public void Cha9_sendStatus(BluetoothDevice device) {
        String strb = Cha9_getBondStatus();
        String strc = Cha9_getCancelStatus();
        strb += strc;
        try {
            byte[] bytes = strb.getBytes("UTF-8");
            Cha9_sendData(device, bytes);
            /*
            mBluetoothGattCharacteristic_to_phone9.setValue(bytes);
            //gatt.writeCharacteristic(characteristic);
            sGattServer.notifyCharacteristicChanged(device,
                    mBluetoothGattCharacteristic_to_phone9, false);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cha9_setCancelStatus("0");
        mSimulatorHandler.removeMessages(MSG_SET_BOND_CANCEL_STATUS);
    }

    public void Cha9_sendData(BluetoothDevice device, byte[] bytes) {
        try {
            if (mBluetoothGattCharacteristic_to_phone9 != null) {
                mBluetoothGattCharacteristic_to_phone9.setValue(bytes);
                //gatt.writeCharacteristic(characteristic);
                sGattServer.notifyCharacteristicChanged(device,
                        mBluetoothGattCharacteristic_to_phone9, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Cha8_sendData(BluetoothDevice device, byte[] bytes) {
        try {
            if (mBluetoothGattCharacteristic_to_phone8 != null) {
                mBluetoothGattCharacteristic_to_phone8.setValue(bytes);
                //gatt.writeCharacteristic(characteristic);
                sGattServer.notifyCharacteristicChanged(device,
                        mBluetoothGattCharacteristic_to_phone8, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * AGS 数据返回逻辑
     * @param device
     * @param bytes
     *
     */
    public void Cha10_sendData(BluetoothDevice device, byte[] bytes) {
        try {
            if (mBluetoothGattCharacteristic_to_phone10 != null) {
                mBluetoothGattCharacteristic_to_phone10.setValue(bytes);
                //gatt.writeCharacteristic(characteristic);
                sGattServer.notifyCharacteristicChanged(device,
                        mBluetoothGattCharacteristic_to_phone10, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    ///////////////////////////////////////////////////////////////////////////////////////
    public static int uid_byte2int(byte[] uid3) {
        int uid = ((0xff & uid3[3]) << 24) | ((0xff & uid3[2]) << 16)            // 1234 是通知UID
                | ((0xff & uid3[1]) << 8) | ((0xff & uid3[0]));
        return uid;
    }

    public static byte[] uid_int2byte(int uid) {
        byte[] bys = new byte[4];
        bys[0] = (byte) (uid & 0xff);
        bys[1] = (byte) ((uid >> 8) & 0xff);
        bys[2] = (byte) ((uid >> 16) & 0xff);
        bys[3] = (byte) ((uid >> 24) & 0xff);
        return bys;
        /*int uid = ((0xff&uid3[3]) << 24) | ((0xff &uid3[2]) << 16)			// 1234 是通知UID
                | ((0xff & uid3[1]) << 8) | ((0xff &uid3[0]));
        return uid;*/
    }

    public static String byte2str(byte[] bys) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte byteChar : bys) {
            stringBuilder.append(String.format("%02X", byteChar));
        }
        return stringBuilder.toString();
    }

    public static int str2byte2(int iascii) {
        int iret = 0;
        if (iascii >= 65)
            iret = iascii - 65 + 10;
        else
            iret = iascii - 48;
        return iret;
    }

    public static byte[] str2byte(String bys) {
        byte[] by1 = bys.getBytes();
        int i, j, ni;
        ni = by1.length;
        byte[] by2 = new byte[ni / 2];
        for (i = 0, j = 0; i < ni; i += 2, j++) {
            by2[j] = (byte) (str2byte2((int) by1[i]) * 16 + str2byte2((int) by1[i + 1]));
        }
        Log.w(TAG_SELF, bys + " vs " + byte2str(by2));
        return by2;
    }

    public void writegatt(BluetoothGatt gatt, byte[] bytes) {
        BluetoothGattService service = gatt.getService(UUID.fromString(SampleGattAttributes.service_ancs));
        if (service == null) {
            Log.w(TAG_REMOTE, "cant find ancs service");
        } else {
            Log.w(TAG_REMOTE, "find ancs service");
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(SampleGattAttributes.ancs_control_point));
            if (characteristic == null) {
                Log.w(TAG_REMOTE, "cant find control chara");
            } else {
                Log.w(TAG_REMOTE, "find control chara");
                characteristic.setValue(bytes);
                gatt.writeCharacteristic(characteristic);
            }
        }
    }

    public void writegatt_ams(BluetoothGatt gatt, byte[] bytes) {
        BluetoothGattService service = gatt.getService(UUID.fromString(SampleGattAttributes.service_ams));
        if (service == null) {
            Log.w(TAG_REMOTE, "cant find ams service");
        } else {
            Log.w(TAG_REMOTE, "find ams service");
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(SampleGattAttributes.ams_remote_command));
            if (characteristic == null) {
                Log.w(TAG_REMOTE, "cant find ams remote chara");
            } else {
                Log.w(TAG_REMOTE, "find ams remote chara");
                characteristic.setValue(bytes);
                gatt.writeCharacteristic(characteristic);
            }
        }
    }

    void askforNoti() {
        Log.i(TAG_REMOTE, "--------*****  watch  to   phone  for app notification  *****----------");
        //if (mDelay <= 0) {
        int iuid=-1;
        synchronized(mNotiArray)
        {
            if (mNotiArray.size() >0) {
                iuid = mNotiArray.remove(0);
                Log.i(TAG_REMOTE, "take iuid from mNotiArray " + iuid);
            }
        }
        do {
            if(iuid==-1)
                break;
            byte[] uid3=uid_int2byte(iuid);
            //
            if(PacketProcessor.muid!=-1)
                break;
            if(mRemoteGatt==null)
            {
                Log.d(TAG_REMOTE, "askforNoti() send MSG_DO_NOTIFICATION delay 5000ms.");
                mSimulatorHandler.removeMessages(MSG_DO_NOTIFICATION);
                mSimulatorHandler.sendEmptyMessageDelayed(MSG_DO_NOTIFICATION, TIMEOUT);
                break;
            }
            mSimulatorHandler.removeMessages(MSG_DO_NOTIFICATION);
            //
            Log.w(TAG_REMOTE,"ask for noti:"+byte2str(uid3));
            byte[] get_notification_attribute = {
                    (byte) 0x00,
                    //UID
                    uid3[0], uid3[1], uid3[2], uid3[3],
                    //app id
                    (byte) PacketProcessor.NotificationAttributeIDAppIdentifier,
                    //app date
                    (byte) PacketProcessor.NotificationAttributeIDDate,
                    //title
                    (byte) PacketProcessor.NotificationAttributeIDTitle, (byte) 50, (byte) 0,
                    //message
                    (byte) PacketProcessor.NotificationAttributeIDMessage, (byte) 250, (byte) 0
                        /*
                        //title
                        (byte) PacketProcessor.NotificationAttributeIDTitle, (byte) 0xff, (byte) 0xff,
                        //message
                        (byte) PacketProcessor.NotificationAttributeIDMessage, (byte) 0xff, (byte) 0xff
                        */
            };
            int uid = iuid;
                /*int uid = ((0xff&uid3[3]) << 24) | ((0xff &uid3[2]) << 16)            // 1234 是通知UID
                        | ((0xff & uid3[1]) << 8) | ((0xff &uid3[0]));*/
            packet_processor.init(uid);
            writegatt(mRemoteGatt, get_notification_attribute);
            //
            mSimulatorHandler.removeMessages(MSG_CHECK_TIME);
            mSimulatorHandler.sendEmptyMessageDelayed(MSG_CHECK_TIME, TIMEOUT);

        }while(false);

    }

    void stopAdver() {
        mSimulatorHandler.removeMessages(MSG_ReSTART_ADVERTISING);
        mSimulatorHandler.removeMessages(MSG_SHUTDOWN_ADVERTISING);
        //
        Log.w(TAG_SELF, "stop adver mBluetoothAdapter:" + mBluetoothAdapter.isEnabled() + " sAdvertiser:" + (sAdvertiser != null));
        if (mBluetoothAdapter.isEnabled() && sAdvertiser != null) {
            try {
                sAdvertiser.stopAdvertising(myAdvertiseCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sAdvertiser = null;
    }

    void startAdver() {
        mSimulatorHandler.removeMessages(MSG_ReSTART_ADVERTISING);
        mSimulatorHandler.removeMessages(MSG_SHUTDOWN_ADVERTISING);
        //
        do {
            if (sAdvertiser != null) {
                Log.w(TAG_SELF, "startAdver already has");
                //break;
            }
            //BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            sAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            if (sAdvertiser == null) {
                Log.v(TAG_SELF, "can not find sAdvertiser");
                break;
            }
            //Log.w(TAG_SELF,"stop adver");
            //sAdvertiser.stopAdvertising(myAdvertiseCallback);
            Log.w(TAG_SELF, "start adver tx:" + mADVERTISE_TX_POWER + " mode:" + mADVERTISE_MODE);
            //sAdvertiser = null;
            AdvertiseSettings.Builder SettingBuilder = new AdvertiseSettings.Builder();
            SettingBuilder.setTxPowerLevel(mADVERTISE_TX_POWER);
            SettingBuilder.setAdvertiseMode(mADVERTISE_MODE);
            SettingBuilder.setTimeout(0);
            SettingBuilder.setConnectable(true);

            AdvertiseData.Builder advBuilder = new AdvertiseData.Builder();
            AdvertiseData.Builder responseBuilder = new AdvertiseData.Builder();

            //DataBuilder.setIncludeDeviceName(true);
            advBuilder.addServiceUuid(ParcelUuid.fromString(SampleGattAttributes.UUID_HUAMI_SERVICE));
            //responseBuilder.addServiceUuid(ParcelUuid.fromString(SampleGattAttributes.UUID_HUAMI_SERVICE));
/*
            AdvertiseData.Builder DataBuilder = new AdvertiseData.Builder();
            DataBuilder.setIncludeDeviceName(true);
            DataBuilder.addServiceUuid(ParcelUuid.fromString(SampleGattAttributes.UUID_HUAMI_SERVICE));
*/
            try {
                String oldblueAddress = BluetoothAdapter.getDefaultAdapter()
                        .getAddress();
                String blueAddress = oldblueAddress.replace(":", "");
                if (blueAddress.length() == 12) {
                    byte[] bys = str2byte(blueAddress);
                    advBuilder.addServiceData(ParcelUuid.fromString(SampleGattAttributes.UUID_HUAMI_SERVICE), bys);
                    //DataBuilder.addServiceData(ParcelUuid.fromString(SampleGattAttributes.UUID_HUAMI_SERVICE),bys);
                }
                String brname = "mi-" + blueAddress.substring(blueAddress.length() - 4);
                advBuilder.addManufacturerData('\uffff', brname.getBytes());
                //responseBuilder.addManufacturerData('\uffff', brname.getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }

            /*sAdvertiser.startAdvertising(SettingBuilder.build(),
                    DataBuilder.build(), myAdvertiseCallback);*/
            /*sAdvertiser.startAdvertising(SettingBuilder.build(),
                    advBuilder.build(), myAdvertiseCallback);*/
            sAdvertiser.startAdvertising(SettingBuilder.build(), advBuilder.build(),
                    responseBuilder.build(), myAdvertiseCallback);
            //mSimulatorHandler.sendEmptyMessageDelayed(MSG_SHUTDOWN_ADVERTISING, 180000);
            //mSimulatorHandler.sendEmptyMessageDelayed(MSG_SHUTDOWN_ADVERTISING, 60000);

        } while (false);
    }

    private BLEDataParser bleDataParser ;
    @Override
    public void sendData(TransportDataItem dataItem) {
        if(bleDataParser==null){
            bleDataParser = new BLEDataParser();
        }

        bleDataParser.dispathMessageReceived(dataItem);
    }



    @Override
    public void start() {

        if (mActive) {
            Log.i(TAG, "--------*****   the BLE already started. *****----------");
            return;
        }

        Log.i(TAG, "--------*****   start BLE   *****----------");
        mBluetoothManager = (BluetoothManager) mContex.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mNotiPoster == null)
            mNotiPoster = NotificationPoster.getManager(mContex);


        // TODO: 16-5-10 jinliang 逻辑未处理
//            if(intent!=null) {
//                String action = intent.getAction();
//                Log.e(TAG_SELF, "onStartCommand:" + action);
//                if (action != null) {
//                    if (action.equalsIgnoreCase(ACTION_HUAMI_BLE_NOTI_TEST)) {
//                    /*if(mNotiPoster==null)
//                        mNotiPoster = NotificationPoster.getManager(this);*/
//                        byte[] uid = new byte[4];
//                        uid[0] = 1;
//                        uid[1] = 2;
//                        uid[2] = 3;
//                        uid[3] = (byte) (new Random().nextInt() % 10);
//                        mNotiPoster.addNotification("IIIOOOSSS", uid, "OOOOSSSSIIIII", "iiiissssoooooo",
//                                System.currentTimeMillis());
//                    } else {
//
//                    }
//                    break;
//                }
//            }
        //
        vib = (Vibrator) mContex.getSystemService(mContex.VIBRATOR_SERVICE);
        //

        //
        bleCommunicates = new BLECommunicates(mContex);
        //

        BluetoothGattServerCallback callback = new MyGattServerCallback();
        if (sGattServer != null) {
            Log.e(TAG_SELF, "There is a gatt server has not closed yet.");
            sGattServer.close();
            sGattServer = null;
        }
        Log.e(TAG_SELF, "The status of BluetoothAdapter is " + mBluetoothManager.getAdapter().getState());
        sGattServer = mBluetoothManager.openGattServer(mContex, callback);

        if (sGattServer == null) {
            Log.e(TAG_SELF, "There is no gatt server.");
            //finish();
        }
        //
        BluetoothGattService bs = new BluetoothGattService(
                UUID.fromString(SampleGattAttributes.UUID_HUAMI_SERVICE),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //服务特征 一
        addCharacteristic_UUID_HUAMICOMM(bs);
        addCharacteristic_UUID_HUAMI_COMM8(bs);
        addCharacteristic_UUID_HUAMI_COMM9(bs);
        addCharacteristic_UUID_HUAMI_COMM10(bs);

        if (sGattServer != null) {
            sGattServer.addService(bs);
        }
        Message msg2 = new Message();
        msg2.what = MSG_CHECK_ANCS_CONN;

        mSimulatorHandler.sendMessageDelayed(msg2, 5000);

//        if (message_receiver == null) {
//            message_receiver = new BLEMessageReceiver();
//            IntentFilter intent_filter = new IntentFilter();
//            //intent_filter.addAction(ACTION_HUAMI_CARD + mSendFlag);//action_huami_watch_datasend
//            intent_filter.addAction(ACTION_HUAMI_DATASEND_IOS_FORWATCH);
//            intent_filter.addAction(ACTION_HUAMI_DATASEND_IOS_DIRECT);
//            intent_filter.addAction(ACTION_HUAMI_DATASEND_IOS_DIRECT8);
//            intent_filter.addAction(ACTION_HUAMI_IOS_ANCS_BLOCK);
//            intent_filter.addAction(ACTION_HUAMI_IOS_ANCS_DEL_TO_PHONE);//ACTION_HUAMI_IOS_ANCS_DEL_FROM_CARD
//            intent_filter.addAction(ACTION_HUAMI_IOS_ANCS_DEL_FROM_CARD);
//            intent_filter.addAction(ACTION_HUAMI_IOS_ANCS_APPLY);
//            intent_filter.addAction(ACTION_HUAMI_DATASEND_IOS_FINDPHONE);
//            //BluetoothDevice.ACTION_BOND_STATE_CHANGED//BLEService.ACTION_HUAMI_BOOT_COMPLETED
//            intent_filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//            // TODO: 16-4-22  jinliang 注释掉
////                intent_filter.addAction(BLEService.ACTION_HUAMI_BOOT_COMPLETED);
//        /*intent_filter.addAction(action_negative);
//        intent_filter.addAction(action_delete);*/
//            //intent_filter.addAction(action_renotify);
//            mContex.registerReceiver(message_receiver, intent_filter);
//        }
        initBrocastReceive();

        /////////////////////////////////////////////////////////////
        mActive = true;
        //

        //
    }

    @Override
    public void stop() {

        if (!mActive) {
            Log.i(TAG, "--------*****   the BLE already stopped. *****----------");
            return;
        }

        Log.i(TAG, "--------*****   stop BLE   *****----------");
        //MSG_REMOVE_PHONECALL
        mSimulatorHandler.removeMessages(MSG_REMOVE_PHONECALL);
        mSimulatorHandler.removeMessages(MSG_RESET_RemoteGattReuse);
        mSimulatorHandler.removeMessages(MSG_DELNOTI_FROM_WATCH);
        mSimulatorHandler.removeMessages(MSG_PAIRING_BACKUPSTART);
        mSimulatorHandler.removeMessages(MSG_PAIRING_FINISH);
        mSimulatorHandler.removeMessages(MSG_PROCESS_ANCS_DATA_SOURCE);
        mSimulatorHandler.removeMessages(MSG_ReSTART_ADVERTISING);
        mSimulatorHandler.removeMessages(MSG_SHUTDOWN_ADVERTISING);

        mSimulatorHandler.removeMessages(MSG_CHECK_ANCS_CONN);
        mSimulatorHandler.removeMessages(MSG_CHECK_TIME);
        mSimulatorHandler.removeMessages(MSG_FINISH);
        mSimulatorHandler.removeMessages(MSG_DO_NOTIFICATION);
        //
        mSimulatorHandler.removeMessages(MSG_ADD_NOTIFICATION);
        mSimulatorHandler.removeMessages(MSG_RESET);
        mSimulatorHandler.removeMessages(MSG_ERR);
        //
        mCtrlConnected = false;
        mAncsConnected = false;
        onAncsAvailable(mAncsConnected);
        onCtrlAvailable(mCtrlConnected);
        //mConnectionState
        unRegisterReceiver();
        //unregisterReceiver(message_receiver);//registerReceiver();
        Log.v(TAG_SELF, "onStop sGattServer: " + sGattServer);


        /*if(mGattChar_to_phone!=null)
        {
            ;//mGattChar_to_phone.
        }*/
        //
        if (mRemoteGatt != null) {
            mRemoteGatt.disconnect();
        }
        mRemoteGatt = null;
        //
        if (mBluetoothDevice_phone != null && sGattServer != null) {
            sGattServer.cancelConnection(mBluetoothDevice_phone);
        }
        mBluetoothDevice_phone = null;
        //
        if (sGattServer != null) {
            // sGattServer.stopAdvertising();
            sGattServer.close();
            sGattServer = null;
        }
        //
        stopAdver();
        // If stopAdvertising() gets called before close() a null
        // pointer exception is raised.
        //

        mActive = false;
        //

    }

    @Override
    public void changeBLEStatus(int bleStatu) {

    }


    // ######################################################################


    public void sendConnectecd() {
        if (mAncsConnected && mCtrlConnected) {
            ArrayList<byte[]> cmds = new ArrayList<byte[]>();
            byte[] cmdb = null;
            try {
                cmdb = Communicates.CMD_CONNECTED.getBytes("UTF-8");
                cmds.add(0, cmdb);
                //
                byte[] wdata = bleCommunicates.EncodeDic(cmds);
                bleCommunicates.onWriteData(wdata, 2);
                //
                System.out.println("send connected...");
                if (SEND_MESSAGE_METHOD_STATUS) {
                    Intent mIntent = new Intent();
                    //remove qrcode in test
                    mIntent.setAction(Constants.ACTION_CHANNEL_IOS_AVAILABLE);
                    mIntent.putExtra("avaiable", true);
                    mContex.sendBroadcast(mIntent);
                } else {
                    DataBundle dataBundle = new DataBundle();
                    dataBundle.putBoolean("avaiable", true);
                    bleCommunicates.sendSimpleMessage(Constants.ACTION_CHANNEL_IOS_AVAILABLE,
                            dataBundle,
                            bleListener);
                }
                //remove qrcode in setupwizard
                //mSimulatorHandler.sendEmptyMessageDelayed(MSG_PAIRING_BACKUPSTART,0);
                ///////////////////////////////////////////////////////////////////////////////////////////
                bleCommunicates.readInternalStorage();
                bleCommunicates.setbluetoothmac();
                bleCommunicates.sendBatteryLevel();
                bleCommunicates.getVersion();
                //
                bleCommunicates.ConfigSyn();
                //
                bleCommunicates.BlackListSyn();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void setScanMode(int imode) {
        // TODO Auto-generated method stub
        ArrayList<String> arrs = new ArrayList<String>();
        arrs.add(ACTION_HUAMI_DATASEND_IOS_MYWATCH_scanmode);
        arrs.add("" + imode);

        if(SEND_MESSAGE_METHOD_STATUS){
            Intent sitIntent = new Intent(ACTION_HUAMI_DATASEND_IOS_MYWATCH);
            sitIntent.putStringArrayListExtra("cmds",arrs);
            mContex.sendBroadcast(sitIntent);
        }else{
            DataBundle dataBundle = new DataBundle();
            dataBundle.putStringArrayList("cmds",arrs);
            bleCommunicates.sendSimpleMessage(ACTION_HUAMI_DATASEND_IOS_MYWATCH,dataBundle,bleListener);
        }

        //
    }

    public void onAncsAvailable(boolean arg0) {
        // TODO Auto-generated method stub

        if(DEBUG){
            Log.i(TAG, "----BLE  To  Service  ----CONN_ANCS--:"+arg0);
        }

        Settings.System.putInt(mContex.getContentResolver(), Constants.CONN_ANCS, arg0 ? 1 : 0);
        //
        if (SEND_MESSAGE_METHOD_STATUS) {
            Intent intent2 = new Intent(Constants.ACTION_DEVICE_CONNECTION_CHANGED_IOS);
            intent2.putExtra(Constants.CONN_ANCS, arg0);
            mContex.sendBroadcast(intent2);
        } else {
            DataBundle dataBundle = new DataBundle();
            dataBundle.putBoolean(Constants.CONN_ANCS,arg0);
            bleCommunicates.sendSimpleMessage(Constants.ACTION_DEVICE_CONNECTION_CHANGED_IOS, dataBundle, bleListener);
            sendConnectecd();
        }


        //
    }

    public void onCtrlAvailable(boolean arg0) {
        // TODO Auto-generated method stub
        if(DEBUG){
            Log.i(TAG, "----BLE  To  Service  ----CONN_BLE_CTRL--:"+arg0);
        }
        // Set connect type
        if (arg0)
            Settings.System.putString(mContex.getContentResolver(), Constants.CONN_TYPE, "BLE");
        Settings.System.putInt(mContex.getContentResolver(), Constants.CONN_BLE_CTRL, arg0 ? 1 : 0);
        //
        if(SEND_MESSAGE_METHOD_STATUS){
            Intent intent2 = new Intent(Constants.ACTION_DEVICE_CONNECTION_CHANGED_IOS);
            intent2.putExtra(Constants.CONN_BLE_CTRL, arg0);
            mContex.sendBroadcast(intent2);
        }else{
            DataBundle dataBundle = new DataBundle();
            dataBundle.putBoolean(Constants.CONN_BLE_CTRL,arg0);
            bleCommunicates.sendSimpleMessage(Constants.ACTION_DEVICE_CONNECTION_CHANGED_IOS,
                  dataBundle,
                    bleListener
            );
        }

        //
        sendConnectecd();
    }

    //######################### 添加蓝牙服务特种  ###################################

    /**
     * 添加 huami_comm 特征
     *
     * @return
     */
    private void addCharacteristic_UUID_HUAMICOMM(BluetoothGattService bs) {
        final int properties2 = BluetoothGattCharacteristic.PROPERTY_BROADCAST
                | BluetoothGattCharacteristic.PROPERTY_READ
                | BluetoothGattCharacteristic.PROPERTY_NOTIFY
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                | BluetoothGattCharacteristic.PROPERTY_WRITE;
        final int permissions2 = BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE;
        final int descPermissions2 = BluetoothGattDescriptor.PERMISSION_READ
                //for password
                //| BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED
                //
                | BluetoothGattDescriptor.PERMISSION_WRITE;
        String descStr2 = new String("communicate demo, can read,WRITE");
        //
        BluetoothGattCharacteristic gattChar2 = new BluetoothGattCharacteristic(
                UUID.fromString(SampleGattAttributes.UUID_HUAMI_COMM2), properties2, permissions2);
        BluetoothGattDescriptor gattDesc2 = new BluetoothGattDescriptor(
                UUID.fromString(SampleGattAttributes.UUID_Descriptor),
                descPermissions2);
        gattDesc2.setValue(descStr2.getBytes());
        gattChar2.addDescriptor(gattDesc2);
        bs.addCharacteristic(gattChar2);
        mBluetoothGattCharacteristic_to_phone2 = gattChar2;
        if (DEBUG) {
            Log.i(TAG, "------start---" + "  addCharacteristic_UUID_HUAMICOMM");
        }
    }


    // 修改 uuid_huami_comm8 特征
    private void addCharacteristic_UUID_HUAMI_COMM8(BluetoothGattService bs) {

        final int properties8 = BluetoothGattCharacteristic.PROPERTY_BROADCAST
                | BluetoothGattCharacteristic.PROPERTY_READ
                | BluetoothGattCharacteristic.PROPERTY_NOTIFY
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                | BluetoothGattCharacteristic.PROPERTY_WRITE;
        final int permissions8 = BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE;
        final int descPermissions8 = BluetoothGattDescriptor.PERMISSION_READ
                //for password
                //| BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED
                //
                | BluetoothGattDescriptor.PERMISSION_WRITE;
        String descStr8 = new String("communicate demo, can read,WRITE");
        //
        BluetoothGattCharacteristic gattChar8 = new BluetoothGattCharacteristic(
                UUID.fromString(SampleGattAttributes.UUID_HUAMI_COMM8), properties8, permissions8);
        BluetoothGattDescriptor gattDesc8 = new BluetoothGattDescriptor(
                UUID.fromString(SampleGattAttributes.UUID_Descriptor),
                descPermissions8);
        gattDesc8.setValue(descStr8.getBytes());
        gattChar8.addDescriptor(gattDesc8);
        bs.addCharacteristic(gattChar8);
        mBluetoothGattCharacteristic_to_phone8 = gattChar8;
        if (DEBUG) {
            Log.i(TAG, "------addCharacteristic_UUID_HUAMI_COMM8---" + "addCharacteristic_UUID_HUAMI_COMM8");
        }

    }

    // 添加 huami_comm9 的特征
    private void addCharacteristic_UUID_HUAMI_COMM9(BluetoothGattService bs) {
        final int properties9 = //BluetoothGattCharacteristic.PROPERTY_BROADCAST
                //|
                BluetoothGattCharacteristic.PROPERTY_READ
                        | BluetoothGattCharacteristic.PROPERTY_NOTIFY
                        | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                        | BluetoothGattCharacteristic.PROPERTY_WRITE;
        final int permissions9 = BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE;

        final int descPermissions9 = BluetoothGattDescriptor.PERMISSION_READ
                //for password
                //| BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED
                //
                | BluetoothGattDescriptor.PERMISSION_WRITE;
        String descStr9 = new String("communicate demo, can read,WRITE");

        BluetoothGattCharacteristic gattChar9 = new BluetoothGattCharacteristic(
                UUID.fromString(SampleGattAttributes.UUID_HUAMI_COMM9), properties9, permissions9);
        BluetoothGattDescriptor gattDesc9 = new BluetoothGattDescriptor(
                UUID.fromString(SampleGattAttributes.UUID_Descriptor),
                descPermissions9);
        gattDesc9.setValue(descStr9.getBytes());
        gattChar9.addDescriptor(gattDesc9);
        bs.addCharacteristic(gattChar9);
        mBluetoothGattCharacteristic_to_phone9 = gattChar9;
        if (DEBUG) {
            Log.i(TAG, "------addCharacteristic_UUID_HUAMI_COMM9---" + "addCharacteristic_UUID_HUAMI_COMM9");
        }
    }

    // 修改 uuid_huami_comm10 特征
    private void addCharacteristic_UUID_HUAMI_COMM10(BluetoothGattService bs) {
        final int properties10 = BluetoothGattCharacteristic.PROPERTY_BROADCAST
                | BluetoothGattCharacteristic.PROPERTY_READ
                | BluetoothGattCharacteristic.PROPERTY_NOTIFY
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                | BluetoothGattCharacteristic.PROPERTY_WRITE;
        final int permissions10 = BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE;
        final int descPermissions10 = BluetoothGattDescriptor.PERMISSION_READ
                | BluetoothGattDescriptor.PERMISSION_WRITE;
        String descStr10 = new String("Large file characteristic channel, can read,WRITE");
        //
        BluetoothGattCharacteristic gattChar10 = new BluetoothGattCharacteristic(
                UUID.fromString(SampleGattAttributes.UUID_HUAMI_COMM10), properties10, permissions10);
        BluetoothGattDescriptor gattDesc10 = new BluetoothGattDescriptor(
                UUID.fromString(SampleGattAttributes.UUID_Descriptor),
                descPermissions10);
        gattDesc10.setValue(descStr10.getBytes());
        gattChar10.addDescriptor(gattDesc10);
        bs.addCharacteristic(gattChar10);
        mBluetoothGattCharacteristic_to_phone10 = gattChar10;
        if (DEBUG) {
            Log.i(TAG, "------addCharacteristic_UUID_HUAMI_COMM10---" + "addCharacteristic_UUID_HUAMI_COMM10");
        }
    }

    private  void initBrocastReceive() {
        if (message_receiver == null) {
            message_receiver = new BLEMessageReceiver(this, mContex);
            IntentFilter intent_filter = new IntentFilter();
            //intent_filter.addAction(ACTION_HUAMI_CARD + mSendFlag);//action_huami_watch_datasend
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_FORWATCH);
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_DIRECT);
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_DIRECT8);
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_IOS_ANCS_BLOCK);
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_IOS_ANCS_DEL_TO_PHONE);//ACTION_HUAMI_IOS_ANCS_DEL_FROM_CARD
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_IOS_ANCS_DEL_FROM_CARD);
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_IOS_ANCS_APPLY);
            intent_filter.addAction(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_FINDPHONE);
            //BluetoothDevice.ACTION_BOND_STATE_CHANGED//BLEService.ACTION_HUAMI_BOOT_COMPLETED
            //蓝牙绑定状态的监控
            intent_filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            // TODO: 16-4-22  jinliang 注释掉
//                intent_filter.addAction(BLEService.ACTION_HUAMI_BOOT_COMPLETED);
        /*intent_filter.addAction(action_negative);
        intent_filter.addAction(action_delete);*/
            //intent_filter.addAction(action_renotify);
            mContex.registerReceiver(message_receiver, intent_filter);
        }
        if (mPkgBC == null) {
            mPkgBC = new pkgBroadCast();
            IntentFilter mIntentFilter = new IntentFilter();
            //
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            mIntentFilter.addDataScheme("package");
            //
            mContex.registerReceiver(mPkgBC, mIntentFilter);
        }
        if (mBroadCast == null) {
            mBroadCast = new outerBroadCast();
            IntentFilter mIntentFilter = new IntentFilter();
            //mIntentFilter.addAction(WatchfaceItemsFragment.ACTION_WALL_RECEIVER);
            mIntentFilter.addAction(WatchApp.ACTION_APPS_RECEIVER);
            //mIntentFilter.addAction(VoiceRec.ACTION_VOICE_COMMUNICATE);
            mContex.registerReceiver(mBroadCast, mIntentFilter);
        }
    }

    private void unRegisterReceiver() {
        if (message_receiver != null) {
            mContex.unregisterReceiver(message_receiver);
            message_receiver = null;
        }
        if (mPkgBC != null) {
            mContex.unregisterReceiver(mPkgBC);
            mPkgBC = null;
        }
        if (mBroadCast != null) {
            mContex.unregisterReceiver(mBroadCast);
            mBroadCast = null;
        }
    }

    private void InitSetting()
    {
        SharedPreferences mPreferenceBlue = mContex.getSharedPreferences(strSharedName,
                Context.MODE_PRIVATE);
        if ("openbluetooth".equals(mPreferenceBlue.getString("address",
                "openbluetooth"))) {
            String blueAddress = BluetoothAdapter.getDefaultAdapter()
                    .getAddress();
            SharedPreferences.Editor edit = mPreferenceBlue.edit();
            edit.putString("address", blueAddress);
            edit.commit();
        }
        /*
        if("00000000".equals(mPreferenceBlue.getString("serviceid","00000000")))
        {
            Random random = new Random();
            //
            SharedPreferences.Editor edit = mPreferenceBlue.edit();
            String serviceid=String.valueOf(random.nextInt() % 100000000);
            if(serviceid.startsWith("-"))
                serviceid=serviceid.substring(1);
            while(serviceid.length()<8)
                serviceid="0"+serviceid;
            edit.putString("serviceid", serviceid);
            edit.commit();
        }
        */
    }
}
