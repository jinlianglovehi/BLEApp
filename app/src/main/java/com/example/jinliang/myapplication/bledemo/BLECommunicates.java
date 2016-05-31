/* Copyright 2015 Huami Inc.  All rights reserved. */
package com.example.jinliang.myapplication.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;

import com.huami.watch.ble.Communicates;
import com.huami.watch.ble.SampleGattAttributes;
import com.huami.watch.ble.dbOp;
import com.huami.watch.ble.listener.BLETransforListener;
import com.huami.watch.ble.trasnfer.BLEDataTransfor;
import com.huami.watch.notification.TransportUri;
import com.huami.watch.notification.data.StatusBarNotificationData;
import com.huami.watch.transport.DataBundle;
import com.huami.watch.transport.TransportDataItem;
import com.huami.watch.transport.TransporterModules;
import com.huami.watch.utils.Constants;
import com.huami.watch.utils.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by xuxue on 15/09/28.
 */
public class BLECommunicates {
    private final static String TAG ="ble_tag_refacetor   tag_Communicates";
    private final static String TAG_AGPS =TAG +"  AGPS";
    ////////////////////////////
    public static byte PK_CHAR1 = 0x7e;
    public static byte PK_CHAR2 = 0x7d;
    public static byte SEP_CHAR1 = 0x6e;
    public static byte SEP_CHAR2 = 0x6d;

    /**
     * AGPS 常量
     */
    public static final byte AGPS_PACKAGE_HEAD = 0x0c;// AGPS 数据 头
    public static final byte AGPS_PACKAGE_DATA = 0x0d;// AGPS 数据 内容
    public static final byte AGPS_PACKAGE_END  = 0x0e;// AGPS 数据 尾
    public static final int  AGPS_BATCH_PACKNUM = 30 ;// AGPS 每30个包作为一批数据

    //
    public static byte mRecv[] = new byte[2048];
    public int mRecvStart = -1;
    public int mRecvEnd = -1;
    //
    public static byte mSend[] = new byte[2048];
    public int mSendStart = 0;
    public int mSendEnd = 0;
    //
    public static byte mRecv8[] = new byte[2048];
    public int mRecvStart8 = -1;
    public int mRecvEnd8 = -1;
    //
    public static byte mSend8[] = new byte[2048];
    public int mSendStart8 = 0;
    public int mSendEnd8 = 0;
    //
    private Context mCon;
    //


    public static final String CMD_CONFIG_TO_PHONE = "cf2p";

    public static final String CMD_CONFIG_TO_PHONE2 = "cf2p2";

    public static final String CMD_CONFIG_TO_WATCH = "cf2w";

    public static final String CMD_CONFIG_TO_WATCH2 = "cf2w2";
    //
    public static final String CMD_BLACKLIST_TO_PHONE = "bl_to_phone";
    public static final String CMD_BLACKLIST_TO_PHONE2 = "bl_to_phone2";
    public static final String CMD_BLACKLIST_TO_WATCH = "bl_to_watch";
    public static final String CMD_BLACKLIST_TO_WATCH2 = "bl_to_watch2";
    public static final String CMD_BLACKLIST_TO_WATCH_ALL="bl_to_watch_all";
    //
    public static final String CMD_SYNC_START = "synS";
    public static final String CMD_SYNC_END = "synE";
    public static final String CMD_NOTI_TEST = "cmd_notitest";
    public static final String CMD_CONNECTED = "cmd_connected";
    public static final String CMD_UNBIND = "cmd_unbind";
    public static final String CMD_WALL_QUERY = "wall_query";
    public static final String CMD_WALL_SET = "wall_set";
    public static final String CMD_WATCHWALL_QUERY = "watchwall_query";
    //
    public static final String CMD_USERINFO_TO_WATCH = "userinfo_to_watch";
    public static final String CMD_USERINFO_TO_WATCH2 = "userinfo_to_watch2";
    public static final String CMD_WEATHER_TO_WATCH = "weather_to_watch";
    public static final String CMD_WEATHER_TO_WATCH2 = "weather_to_watch2";
    public static final String CMD_WIFI_PASS = "wifi_pass";
    public static final String CMD_rom_upgrade = "rom_upgrade";
    public static final String CMD_rom_upgrade_notify="rom_upgrade_notify";
    public static final String CMD_wifi_input_cancel = "wifi_input_cancel";
    public static final String CMD_wifi_disconnect = "wifi_disconnect";
    public static final String CMD_sportconfig_to_watch = "sportconfig_to_watch";
    //
    public static final String CMD_sport_config="sport_config";
    public static final String CMD_health_config="health_config";
    public static final String CMD_sys_config="sys_config";
    public static final String CMD_sport_Info="sport_Info";
    public static final String CMD_health_Info="health_Info";
    public static final String CMD_sport_config_query="sport_config_query";
    public static final String CMD_health_config_query="health_config_query";
    public static final String CMD_sys_config_query="sys_config_query";
    public static final String CMD_sport_Info_query="sport_Info_query";
    public static final String CMD_health_Info_query="health_Info_query";
    public static final String HM_ACTION_BLUETOOTH_CONFIRM_PAIR = "com.huami.action.confirm_pair";

    //public static final String CMD_WIFI_PASS2="wifi_pass";
    //public static final String ACTION_WALL_RECEIVER = "android.intent.action.WallReceiver_IOS";
    //
    public boolean mWaiting2 = false;
    public boolean mWaiting8 = false;
    //public static int NOTIFY_MTU = 20;
    public static int NOTIFY_MTU = 19;
    public long mLastSynEndTime=0;
    public long mLastSynStartTime=0;


    /**
     * 解析工具处理
     *
     * @param con
     */
    private BLEDataParser bleDataParser;

    //
    public BLECommunicates(Context con) {
        mCon = con;
        bleDataParser = new BLEDataParser();
        init();
    }

    public void init() {
        mRecv[0] = PK_CHAR1;
        mRecvStart = 0;
        mRecvEnd = 1;
        //
        mRecv8[0] = PK_CHAR1;
        mRecvStart8 = 0;
        mRecvEnd8 = 1;
    }

    public void ios2mywatch(ArrayList<byte[]> cmds,BLETransforListener callBack) {
        try {
            do {
                ArrayList<String> arrs = new ArrayList<String>();
                int i, ni;
                ni = cmds.size();
                for (i = 0; i < ni; i++) {
                    String cmdname = new String(cmds.get(i), "UTF-8");
                    arrs.add(cmdname);
                }


                if(BLEDataTransfor.SEND_MESSAGE_METHOD_STATUS){
                    Intent sitIntent = new Intent(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_MYWATCH);
                    Log.i(TAG, "ios2mywatch: brocastData:" + arrs);
                    sitIntent.putStringArrayListExtra("cmds", arrs);
                    //mCon.sendBroadcast(sitIntent);
                    mCon.sendOrderedBroadcast(sitIntent, null);
                }else{
                    DataBundle dataBundle = new DataBundle();
                    dataBundle.putStringArrayList("cmds", arrs);
                    sendSimpleMessage(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_MYWATCH,
                            dataBundle ,callBack );
                }

            } while (1 < 0);
        } catch (Exception e) {
            e.printStackTrace();
            //bbreak = true;
        }
    }


    public synchronized void processing(byte[] pd, int itype, BLETransforListener bleListener) {

        Log.i(TAG, "processing: 解析数据 itype:" + itype + "pd.length:"+ pd.length);
        int i, ni;
        switch (itype) {
            case SampleGattAttributes.COMM_TYPE_2:
                ni = pd.length;
                for (i = 0; i < ni; i++) {
                    if (pd[i] == PK_CHAR1) {
                        if (mRecvEnd > mRecvStart + 1) {
                            ArrayList<byte[]> bas = DecodeDic(mRecv, mRecvStart + 1, mRecvEnd - mRecvStart - 1);
                            //do process
                            docmd(bas, bleListener);

                        }
                        mRecvStart = 0;
                        mRecvEnd = 1;
                    } else {
                        if (mRecvEnd != -1) {
                            if (mRecvEnd < mRecv.length - 1) {
                                mRecv[mRecvEnd] = pd[i];
                                mRecvEnd++;
                            } else//overflow
                            {
                                Log.d(TAG, "overflow");
                                mRecvStart = 0;
                                mRecvEnd = -1;
                            }
                        }
                    }
                }

                break;
            case SampleGattAttributes.COMM_TYPE_8:
                ni = pd.length;

                Log.d(TAG, "processing :发送的数据包 start8: " + mRecvStart8 + ", receEnd8:" + mRecvEnd8);
                for (i = 0; i < ni; i++) {

                    if (pd[i] == PK_CHAR1) {
                        if (mRecvEnd8 > mRecvStart8 + 1) {
                            Log.d(TAG, "processing :完整包处理 start8: " + mRecvStart8 + ", receEnd8:" + mRecvEnd8);
                            ArrayList<byte[]> bas = DecodeDic(mRecv8, mRecvStart8 + 1, mRecvEnd8 - mRecvStart8 - 1);
                            //do process
                            docmd(bas, bleListener);
                        }
                        mRecvStart8 = 0;
                        mRecvEnd8 = 1;
                    } else {
                        if (mRecvEnd8 != -1) {
                            if (mRecvEnd8 < mRecv8.length - 1) {
                                mRecv8[mRecvEnd8] = pd[i];
                                mRecvEnd8++;
                            } else//overflow
                            {
                                Log.d(TAG, "overflow");
                                mRecvStart8 = 0;
                                mRecvEnd8 = -1;
                            }
                        }
                    }
                }

                break;
            case SampleGattAttributes.COMM_TYPE_10:
                break;
        }

    }

    public byte[] EncodeFieldData(byte[] pdata) {
        byte sep1[] = {SEP_CHAR2, 0x02};
        byte sep2[] = {SEP_CHAR2, 0x01};
        int i, ni;
        ni = pdata.length;
        //ByteBuffer tmpd = ByteBuffer.allocate(ni*2);
        ByteArrayOutputStream tmpd = new ByteArrayOutputStream();
        for (i = 0; i < ni; i++) {
            if (pdata[i] == SEP_CHAR1) {
                tmpd.write(sep1, 0, sep1.length);
            } else if (pdata[i] == SEP_CHAR2) {
                tmpd.write(sep2, 0, sep2.length);
            } else {
                tmpd.write(pdata[i]);
            }
        }

        return tmpd.toByteArray();
    }

    public byte[] DecodeFieldData(byte[] pdata) {
        return DecodeFieldData(pdata, 0, pdata.length);
    }

    public byte[] DecodeFieldData(byte[] pdata, int istart, int ilen) {
        byte sep1[] = {SEP_CHAR1};
        byte sep2[] = {SEP_CHAR2};
        int i, ni;
        //const char* pdata=olddata.bytes;
        ni = istart + ilen;
        //ByteBuffer tmpd = ByteBuffer.allocate(ilen);
        ByteArrayOutputStream tmpd = new ByteArrayOutputStream();
        for (i = istart; i < ni; i++) {
            if (pdata[i] == SEP_CHAR2 && i < ni - 1) {
                if (pdata[i + 1] == 0x02) {
                    tmpd.write(sep1[0]);
                    i++;
                } else if (pdata[i + 1] == 0x01) {
                    tmpd.write(sep2[0]);
                    i++;
                } else {
                    tmpd.write(pdata[i]);
                    //bad place
                }

            } else {
                tmpd.write(pdata[i]);
            }
        }
        byte[] content = tmpd.toByteArray();
        return content;
    }

    public byte[] EncodePkData(byte[] pdata) {
        byte sep1[] = {PK_CHAR2, 0x02};
        byte sep2[] = {PK_CHAR2, 0x01};
        int i, ni;
        //const char* pdata=olddata.bytes;
        ni = pdata.length;
        //ByteBuffer tmpd = ByteBuffer.allocate(ni*2);
        ByteArrayOutputStream tmpd = new ByteArrayOutputStream();
        for (i = 0; i < ni; i++) {
            if (pdata[i] == PK_CHAR1) {
                tmpd.write(sep1, 0, sep1.length);
            } else if (pdata[i] == PK_CHAR2) {
                tmpd.write(sep2, 0, sep2.length);
            } else {
                tmpd.write(pdata[i]);
            }
        }
        return tmpd.toByteArray();
    }

    public byte[] DecodePkData(byte[] pdata)//to do
    {
        return DecodePkData(pdata, 0, pdata.length);
    }

    public byte[] DecodePkData(byte[] pdata, int istart, int iend)//to do
    {
        ByteArrayOutputStream tmpd = new ByteArrayOutputStream();
        byte sep1[] = {PK_CHAR1};
        byte sep2[] = {PK_CHAR2};
        int i, ni;
        tmpd.reset();
        ni = istart + iend;
        //ByteBuffer tmpd = ByteBuffer.allocate(ni);
        for (i = istart; i < ni; i++) {
            if (pdata[i] == PK_CHAR2 && i < ni - 1) {
                if (pdata[i + 1] == 0x02) {
                    tmpd.write(sep1, 0, 1);
                    i++;
                } else if (pdata[i + 1] == 0x01) {
                    //tmpd.put(sep2);
                    tmpd.write(sep2, 0, 1);
                    i++;
                } else {
                    //bad place
                    tmpd.write(pdata[i]);
                }

            } else {
                tmpd.write(pdata[i]);
            }
        }
        byte[] content = tmpd.toByteArray();
        return content;
    }

    public byte[] EncodeDic(ArrayList<byte[]> theData) {
        byte sep1[] = {SEP_CHAR1};
        byte pk1[] = {PK_CHAR1};
        //NSMutableData* tmpd = [[NSMutableData alloc] init];
        //ByteBuffer tmpd = ByteBuffer.allocate(2048);
        ByteArrayOutputStream tmpd3 = new ByteArrayOutputStream();
        //ByteBuffer tmpd3 = ByteBuffer.allocate(2048);
        //NSMutableData* tmpd3 = [[NSMutableData alloc] init];
        int i, ni;
        ni = theData.size();
        //[tmpd appendBytes:pk1 length:1];
        for (i = 0; i < ni; i++) {
            byte[] ef = EncodeFieldData(theData.get(i));
            if (i > 0) {
                tmpd3.write(sep1, 0, sep1.length);
            }
            tmpd3.write(ef, 0, ef.length);
        }
        tmpd3.write(0x0);
        byte[] content = tmpd3.toByteArray();
        tmpd3.reset();
        ////////////////////////////////////////////////////
        byte ical = content[0];
        for (i = 1; i < content.length - 1; i++) {
            ical = (byte) (ical ^ content[i]);
        }
        content[content.length - 1] = ical;

        byte[] bytes = EncodePkData(content);
        tmpd3.write(pk1, 0, pk1.length);
        tmpd3.write(bytes, 0, bytes.length);
        tmpd3.write(pk1, 0, pk1.length);
//////////////////////
        content = tmpd3.toByteArray();
//
        return content;
    }

    //NSDictionary *dataDict = [NSDictionary dictionaryWithObject:cmda forKey:@"key0"];
    public ArrayList<byte[]> DecodeDic(byte[] indata) {
        //NSDictionary *theData = [notification userInfo];
        ArrayList<byte[]> retd = new ArrayList<byte[]>();
        byte[] pb = DecodePkData(indata);
        int i, ni, j, starti;

        ni = pb.length;
        byte ical = pb[0];
        for (i = 1; i < ni - 1; i++) {
            ical = (byte) (ical ^ pb[i]);
        }
        do {
            if (ical != pb[ni - 1])
                break;
            ni--;
            //
            j = 0;
            starti = -1;
            for (i = 0; i < ni; i++) {
                if (pb[i] == SEP_CHAR1) {
                    if (i > starti + 1) {
                        byte[] fd = DecodeFieldData(pb, starti + 1, i - starti - 1);
                        retd.add(fd);
                    }
                    starti = i;
                } else {
                    if (i == ni - 1) {
                        if (i > starti) {
                            byte[] fd = DecodeFieldData(pb, starti + 1, i - starti);
                            retd.add(fd);
                        }
                    }
                }
            }
            //
        } while (1 < 0);
        return retd;

    }

    //
    public ArrayList<byte[]> DecodeDic(byte[] indata, int istart, int ilen) {
        //NSDictionary *theData = [notification userInfo];
        ArrayList<byte[]> retd = new ArrayList<byte[]>();
        byte[] pb = DecodePkData(indata, istart, ilen);


        int i, ni, j, starti;

        ni = pb.length;
        byte ical = pb[0];
        for (i = 1; i < ni - 1; i++) {
            ical = (byte) (ical ^ pb[i]);
        }
        do {
            if (ical != pb[ni - 1])
                break;
            ni--;
            //
            j = 0;
            starti = -1;
            for (i = 0; i < ni; i++) {
                if (pb[i] == SEP_CHAR1) {
                    if (i > starti + 1) {
                        byte[] fd = DecodeFieldData(pb, starti + 1, i - starti - 1);
                        retd.add(fd);
                    }
                    starti = i;
                } else {
                    if (i == ni - 1) {
                        if (i > starti) {
                            byte[] fd = DecodeFieldData(pb, starti + 1, i - starti);
                            retd.add(fd);
                        }
                    }
                }
            }
            //
        } while (1 < 0);
        return retd;

    }

    ///////////////////////////////////////
    public void onWriteData(byte[] wdata, int itype) {
        int i, ni;
        if (itype == 2) {
            if (mSend.length - mSendEnd >= wdata.length) {
                for (i = 0; i < wdata.length; i++) {
                    mSend[mSendEnd++] = wdata[i];
                }
            } else {
                if (mSendStart > 0) {
                    for (i = mSendStart; i < mSendEnd; i++) {
                        mSend[i - mSendStart] = mSend[i];
                    }
                    mSendEnd -= mSendStart;
                    mSendStart = 0;
                }
                //
                if (mSend.length - mSendEnd >= wdata.length) {
                    for (i = 0; i < wdata.length; i++) {
                        mSend[mSendEnd++] = wdata[i];
                    }
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            if (!mWaiting2) {
                if (mSendEnd > mSendStart)
                    sendData(itype);
            }
        } else if (itype == 8) {
            if (mSend8.length - mSendEnd8 >= wdata.length) {
                for (i = 0; i < wdata.length; i++) {
                    mSend8[mSendEnd8++] = wdata[i];
                }
            } else {
                if (mSendStart8 > 0) {
                    for (i = mSendStart8; i < mSendEnd8; i++) {
                        mSend8[i - mSendStart8] = mSend8[i];
                    }
                    mSendEnd8 -= mSendStart8;
                    mSendStart8 = 0;
                }
                //
                if (mSend8.length - mSendEnd8 >= wdata.length) {
                    for (i = 0; i < wdata.length; i++) {
                        mSend8[mSendEnd8++] = wdata[i];
                    }
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            if (!mWaiting8) {
                if (mSendEnd8 > mSendStart8)
                    sendData(itype);
            }
        }

    }

    //
    private void onWriteData2(byte[] wdata, int itype) {

        //////////////////////////////////////////////////////////////////////
        Log.v(TAG, "onWriteData");
        BluetoothGattCharacteristic mGattChar_to_phone = null;
        switch (itype) {
            case SampleGattAttributes.COMM_TYPE_2:
                mGattChar_to_phone = BLEDataTransfor.mBluetoothGattCharacteristic_to_phone2;
                break;
            case SampleGattAttributes.COMM_TYPE_8:
                mGattChar_to_phone = BLEDataTransfor.mBluetoothGattCharacteristic_to_phone8;
                break;
        }
        try {
            BluetoothDevice device = BLEDataTransfor.mBluetoothDevice_phone;
            if (device == null)
                device = BLEDataTransfor.mBluetoothDevice_newlywrite;
            if (BLEDataTransfor.sGattServer != null
                    && mGattChar_to_phone != null
                    && device != null) {
                mGattChar_to_phone.setValue(wdata);
                BLEDataTransfor.sGattServer.notifyCharacteristicChanged(device,
                        mGattChar_to_phone, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    void sendData(int itype) {
        int amountToSend;
        ByteArrayOutputStream tmpd3;
        switch (itype) {
            case SampleGattAttributes.COMM_TYPE_2:
                if (mSendStart >= mSendEnd) {
                    return;
                }
                amountToSend = mSendEnd - mSendStart;
                if (amountToSend > NOTIFY_MTU) amountToSend = NOTIFY_MTU;
                tmpd3 = new ByteArrayOutputStream();
                Log.v(TAG, "send data start:" + mSendStart + " len:" + amountToSend);
                //
                tmpd3.write(SampleGattAttributes.idcmd);
                //
                tmpd3.write(mSend, mSendStart, amountToSend);
                onWriteData2(tmpd3.toByteArray(), itype);
                mSendStart += amountToSend;
                if (mSendStart >= mSendEnd) {
                    return;
                } else {
                    new WaitTask(itype).execute("wait");

                }
                break;
            case SampleGattAttributes.COMM_TYPE_8:
                if (mSendStart8 >= mSendEnd8) {
                    return;
                }
                amountToSend = mSendEnd8 - mSendStart8;
                if (amountToSend > NOTIFY_MTU) amountToSend = NOTIFY_MTU;
                tmpd3 = new ByteArrayOutputStream();
                Log.v(TAG, "send data start:" + mSendStart8 + " len:" + amountToSend);
                //
                tmpd3.write(SampleGattAttributes.idcmd);
                //
                tmpd3.write(mSend8, mSendStart8, amountToSend);
                onWriteData2(tmpd3.toByteArray(), itype);
                mSendStart8 += amountToSend;
                if (mSendStart8 >= mSendEnd8) {
                    return;
                } else {
                    new WaitTask(itype).execute("wait");
                }
                break;
        }
    }

    public void BlackListSyn() {
        try {
            do {
                int i, ni;
                String strq = "select * from blacklist where _bsyn='0' ";
                //String strq="select * from blacklist ";
                //
                ArrayList<HashMap<String, String>> arrs = dbOp.getInstance(mCon).sql2array(strq);
                ni = arrs.size();
                if (ni == 0)
                    break;
                ArrayList<byte[]> cmds2 = new ArrayList<byte[]>();
                byte[] cmdb2 = null;
                cmdb2 = CMD_BLACKLIST_TO_PHONE.getBytes("UTF-8");
                cmds2.add(0, cmdb2);
                cmdb2 = ("" + arrs.size()).getBytes("UTF-8");
                cmds2.add(1, cmdb2);
//_packagename text primary key, _title text, _icon_bytes blob,_bInBlacklist text not null default('0')
                for (i = 0; i < ni; i++) {
                    HashMap<String, String> hm = arrs.get(i);
                    String tmp;
                    tmp = hm.get("_packagename");
                    cmdb2 = tmp.getBytes("UTF-8");
                    cmds2.add(i * 3 + 2, cmdb2);
                    tmp = hm.get("_title");
                    cmdb2 = tmp.getBytes("UTF-8");
                    cmds2.add(i * 3 + 3, cmdb2);
                    tmp = hm.get("_bInBlacklist");
                    cmdb2 = tmp.getBytes("UTF-8");
                    cmds2.add(i * 3 + 4, cmdb2);
                }
                byte[] wdata = EncodeDic(cmds2);
                onWriteData(wdata, 2);
            } while (1 < 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    public void ConfigSyn() {
        try {
            do {
                int i, ni;
                String strq = "select * from watchconfig where _bsyn='0' ";
                //String strq="select * from blacklist ";
                //
                ArrayList<HashMap<String, String>> arrs = dbOp.getInstance(mCon).sql2array(strq);
                ni = arrs.size();
                if (ni == 0)
                    break;
                ArrayList<byte[]> cmds2 = new ArrayList<byte[]>();
                byte[] cmdb2 = null;
                cmdb2 = CMD_CONFIG_TO_PHONE.getBytes("UTF-8");
                cmds2.add(0, cmdb2);
                cmdb2 = ("" + arrs.size()).getBytes("UTF-8");
                cmds2.add(1, cmdb2);
//_packagename text primary key, _title text, _icon_bytes blob,_bInBlacklist text not null default('0')
                for (i = 0; i < ni; i++) {
                    HashMap<String, String> hm = arrs.get(i);
                    String tmp;
                    tmp = hm.get("_name");
                    cmdb2 = tmp.getBytes("UTF-8");
                    cmds2.add(i * 2 + 2, cmdb2);
                    tmp = hm.get("_value");
                    cmdb2 = tmp.getBytes("UTF-8");
                    cmds2.add(i * 2 + 3, cmdb2);
                }
                Log.i(TAG, "ConfigSyn:   mywatch to ios data:" + cmds2.toString());
                byte[] wdata = EncodeDic(cmds2);
                onWriteData(wdata, 2);
            } while (1 < 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    public void setbluetoothmac() {
        String blueAddress = BluetoothAdapter.getDefaultAdapter()
                .getAddress();
        dbOp.getInstance(mCon).setConfigV("serviceid", blueAddress, "0");
        dbOp.getInstance(mCon).setConfigV("mac_address", blueAddress, "0");
        //dbOp.getInstance(mCon).setConfigV("serviceid_nlen",""+blueAddress.length(),"0");

    }

    //
    public void readInternalStorage() {
        //CMyTransObj to = new CMyTransObj();
        DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        //String p=decimalFomat.format(price);//format 返回的是字符串
        StatFs sf = new StatFs("/data");
        long blockSize = sf.getBlockSize();
        long totalBlocks = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
        float v1 = ((float) (availCount * blockSize) / 1024 / 1024 / 1024);
        float v2 = ((float) (totalBlocks * blockSize) / 1024 / 1024 / 1024);
        String s1 = decimalFormat.format(v1);
        String s2 = decimalFormat.format(v2);
        String storage = "" + s1 + "G/" + s2 + "G";
        dbOp.getInstance(mCon).setConfigV("vol_size", storage, "0");

    }

    //
    public void sendBatteryLevel() {
        /*CMyTransObj to = new CMyTransObj();*/

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mCon.registerReceiver(null, ifilter);
//
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra("level", 0);
        int scale = batteryStatus.getIntExtra("scale", 100);
        int ratio = (level * 100) / scale;
//
        long currentTime = System.currentTimeMillis();
        long lastTime = currentTime;
        if (isCharging) {
            SharedPreferences sharedPreferences = mCon.getSharedPreferences(
                    BLEDataTransfor.strSharedName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            //lastTime= System.currentTimeMillis();
            editor.putLong("time_long", lastTime);
            editor.apply();

        } else {
            SharedPreferences mPreferenceBlue = mCon.getSharedPreferences(BLEDataTransfor.strSharedName,
                    Context.MODE_PRIVATE);
            lastTime = mPreferenceBlue.getLong("time_long",
                    -1);
        }
        if (currentTime < lastTime) {
            currentTime = lastTime;
        }
        if(lastTime==-1)
        {
            dbOp.getInstance(mCon).setConfigV("lastchargingtime","-1","0");
        }
        else
        {
        long extraTime = lastTime % (1000 * 60 * 60 * 24);
        int ndays = (int) ((currentTime - lastTime + extraTime) / (1000 * 60 * 60 * 24));
        dbOp.getInstance(mCon).setConfigV("lastchargingtime", "" + ndays, "0");

        }
        dbOp.getInstance(mCon).setConfigV("ischarging", isCharging ? "1" : "0", "0");
        //dbOp.getInstance(mCon).setConfigV("batterylevel",""+ratio+"%","0");
        dbOp.getInstance(mCon).setConfigV("batterylevel", "" + ratio, "0");

    }

    public void getVersion() {
        String[] version = {"null", "null", "null", "null"};
        String str1 = "/proc/version";
        String str2;
        String[] arrayOfString;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            version[0] = arrayOfString[2];//KernelVersion
            localBufferedReader.close();
        } catch (IOException e) {
        }
        version[1] = Build.VERSION.RELEASE;// firmware version
        version[2] = Build.MODEL;//model
        version[3] = Build.DISPLAY;//system version
        //return version;
        dbOp.getInstance(mCon).setConfigV("rom_version", version[3], "0");
        dbOp.getInstance(mCon).setConfigV("firmware_version", version[1], "0");
        dbOp.getInstance(mCon).setConfigV("model_version", version[2], "0");
    }

    //////
    //
    public class WaitTask extends
            AsyncTask<String, Void, Integer> {
        //
        //public static final String DEFAULT_PACKAGE_NAME="loadAllApp";
        public int mType;

        //
        public WaitTask(int itype) {
            mType = itype;
        }

        //
        @SuppressWarnings("unused")
        @Override
        protected void onPostExecute(Integer ival) {
            super.onPostExecute(ival);
            //
            switch (mType) {
                case SampleGattAttributes.COMM_TYPE_2:
                    mWaiting2 = false;
                    if (mSendEnd > mSendStart)
                        sendData(mType);
                    break;
                case SampleGattAttributes.COMM_TYPE_8:
                    mWaiting8 = false;
                    if (mSendEnd8 > mSendStart8)
                        sendData(mType);
                    break;
            }

            //new WaitTask().execute("wait");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //
            switch (mType) {
                case SampleGattAttributes.COMM_TYPE_2:
                    mWaiting2 = true;
                    break;
                case SampleGattAttributes.COMM_TYPE_8:
                    mWaiting8 = true;
                    break;
            }
        }

        @Override
        protected Integer doInBackground(String... params) {

            try {
                //100 LOST,200 NOT LOST
                //Thread.sleep(200);
                //Thread.sleep(100);
                //Thread.sleep(50);
                //Thread.sleep(25);
                //Thread.sleep(12);
                //Thread.sleep(6);
                //Thread.sleep(3);
                switch (mType) {
                    case SampleGattAttributes.COMM_TYPE_2:
                        Thread.sleep(10);
                        break;
                    case SampleGattAttributes.COMM_TYPE_8:
                        Thread.sleep(500);
                        break;
                }
                //Thread.sleep(500);
                //Thread.sleep(1000);
                //System.out.println("send...");
            } catch (Exception e) {
                e.printStackTrace();
                //System.out.println("thread error...");
            }
            //
            return 1;
        }
    }



    // ########################  特殊数据处理  ####################################
    /**
     * 数据解析 发送
     *
     * @param cmds
     */
    public void docmd(ArrayList<byte[]> cmds, BLETransforListener callBack) {

        /**
         * 广播形式
         */

        Log.i(TAG, "docmd: ");
        boolean bbreak = false;
        do {
            if (cmds.size() == 0)
                break;
            String strcmd = "";
            try {
                strcmd = new String(cmds.get(0), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                bbreak = true;
            }
            if (bbreak)
                break;
            Log.d(TAG, "--------------------------Communicates Receive:"+strcmd +"----------------------------------------");
            if (strcmd.equalsIgnoreCase(CMD_NOTI_TEST)) {//CMD_UNBIND
                Log.i(TAG, "--------*****   BLE  to   watch  for  cmd_noti_test    *****----------");
                // TODO: 16-5-12  测试callBack 回调的方式
                try {
                    do {
                        ArrayList<String> arrs = new ArrayList<String>();
                        int i, ni;
                        ni = cmds.size();
                        for (i = 0; i < ni; i++) {
                            String cmdname = new String(cmds.get(i), "UTF-8");
                            arrs.add(cmdname);
                        }

                            DataBundle dataBundle = new DataBundle();
                            dataBundle.putStringArrayList("cmds", arrs);
                            sendSimpleMessage(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_MYWATCH,
                                    dataBundle ,callBack );

                    } while (1 < 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    //bbreak = true;
                }
                // TODO: 16-5-12 测试完成时候 需要放开
//                ios2mywatch(cmds, callBack);
            }//CMD_WIFI_PASS2
            else if (strcmd.equalsIgnoreCase(CMD_USERINFO_TO_WATCH)) {
                Log.i(TAG, "--------*****   BLE  to   watch  for  userinfo   *****----------");
                ios2mywatch(cmds,callBack);
            }
            else if (strcmd.equalsIgnoreCase(CMD_SYNC_START)) {
                long curtime=System.currentTimeMillis();
                if(curtime<mLastSynStartTime+10000)
                {

                } else {
                    Log.i(TAG, "--------*****   BLE  to   watch  for  sync start  *****----------");
                    BLEDataTransfor.s().mSimulatorHandler.removeMessages(BLEDataTransfor.MSG_PAIRING_FINISH);
                    if(BLEDataTransfor.SEND_MESSAGE_METHOD_STATUS){
                        Intent intent2 = new Intent(HM_ACTION_BLUETOOTH_CONFIRM_PAIR);
                        mCon.sendBroadcast(intent2);
                    }else{
                        sendSimpleMessage(HM_ACTION_BLUETOOTH_CONFIRM_PAIR, null ,callBack);
                    }
                    //MSG_PAIRING_FINISH
                    BLEDataTransfor.s().mSimulatorHandler.sendEmptyMessageDelayed(BLEDataTransfor.MSG_PAIRING_FINISH,80000);
                }
                mLastSynStartTime=curtime;
            }
            else if (strcmd.equalsIgnoreCase(CMD_SYNC_END)) {
                Log.i(TAG, "--------*****   BLE  to   watch  for  sync end  *****----------");

                //
                int iDEVICE_PROVISIONED=Settings.Global.getInt(mCon.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
                if(iDEVICE_PROVISIONED<=0) {
                    SharedPreferences sharedPreferences = mCon.getSharedPreferences(
                            BLEDataTransfor.strSharedName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
//
                    editor.putLong("time_long", -1);
                    editor.apply();
                }

                //
                long curtime=System.currentTimeMillis();
                if(curtime<mLastSynEndTime+10000)
                {

                }
                else
                    BLEDataTransfor.s().mSimulatorHandler.sendEmptyMessage(BLEDataTransfor.MSG_PAIRING_FINISH);
                mLastSynEndTime=curtime;
                /*Intent intent = new Intent("com.huami.watch.companion.action.PAIRING");
                intent.putExtra("pairingFinish", true);
                mCon.sendBroadcast(intent);*/
            }
            else if (strcmd.equalsIgnoreCase(CMD_WEATHER_TO_WATCH)) {
                if(cmds.size()<2)
                    break;
                try {
                    String strs;
                    strs = new String(cmds.get(1), "UTF-8");
                    Log.i(TAG, "--------*****   BLE  to  watch  for  weather  *****--------data:"+strs.toString());
                    /*
                    strs=
                            "{"+
                                    "\"tempUnit\": \"C\","+
                                    "\"tempFormatted\": \"3ºC\","+
                                    "\"temp\": 3,"+
                                    "\"isNotification\": false,"+
                                    "\"weatherCode\": 1,"+
                                    "\"weather\": \"多云\","+
                                    "\"time\": 1455505496529,"+
                                    "\"aqiLevel\": 2,"+
                                    "\"aqi\": 53,"+
                                    "\"forecasts\": ["+
                                    "{"+
                                    "\"tempFormatted\": \"6ºC/-2ºC\","+
                                    "\"weatherTo\": \"\","+
                                    "\"weatherFrom\": \"多云\","+
                                    "\"weatherCodeFrom\": 1,"+
                                    "\"weatherCodeTo\": -1,"+
                                    "\"tempMin\": -2,"+
                                    "\"tempMax\": 6,"+
                                    "\"weather\": \"多云\","+
                                    "\"day\": 1  "+
                                    "},"+
                                    "{"+
                                    "\"tempFormatted\": \"11ºC/-2ºC\","+
                                    "\"weatherTo\": \"\","+
                                    "\"weatherFrom\": \"多云\","+
                                    "\"weatherCodeFrom\": 1,"+
                                    "\"weatherCodeTo\": -1,"+
                                    "\"tempMin\": -2,"+
                                    "\"tempMax\": 11,"+
                                    "\"weather\": \"多云\","+
                                    "\"day\": 2"+
                                    "},"+
                                    "{"+
                                    "\"tempFormatted\": \"13ºC/1ºC\","+
                                    "\"weatherTo\": \"\","+
                                    "\"weatherFrom\": \"多云\","+
                                    "\"weatherCodeFrom\": 1,"+
                                    "\"weatherCodeTo\": -1,"+
                                    "\"tempMin\": 1,"+
                                    "\"tempMax\": 13,"+
                                    "\"weather\": \"多云\","+
                                    "\"day\": 3"+
                                    "},"+
                                    "{"+
                                    "\"tempFormatted\": \"16ºC/5ºC\","+
                                    "\"weatherTo\": \"\","+
                                    "\"weatherFrom\": \"多云\","+
                                    "\"weatherCodeFrom\": 1,"+
                                    "\"weatherCodeTo\": -1,"+
                                    "\"tempMin\": 5,"+
                                    "\"tempMax\": 16,"+
                                    "\"weather\": \"多云\","+
                                    "\"day\": 4"+
                                    "},"+
                                    "{"+
                                    "\"tempFormatted\": \"15ºC/3ºC\","+
                                    "\"weatherTo\": \"\","+
                                    "\"weatherFrom\": \"多云\","+
                                    "\"weatherCodeFrom\": 1,"+
                                    "\"weatherCodeTo\": -1,"+
                                    "\"tempMin\": 3,"+
                                    "\"tempMax\": 15,"+
                                    "\"weather\": \"多云\","+
                                    "\"day\": 5"+
                                    "},"+
                                    "{"+
                                    "\"tempFormatted\": \"0ºC/0ºC\","+
                                    "\"weatherTo\": \"\","+
                                    "\"weatherFrom\": \"晴\","+
                                    "\"weatherCodeFrom\": 0,"+
                                    "\"weatherCodeTo\": -1,"+
                                    "\"tempMin\": 0,"+
                                    "\"tempMax\": 0,"+
                                    "\"weather\": \"晴\","+
                                    "\"day\": 6"+
                                    "}"+
                                    "],"+
                                    "\"alert\": {"+
                                    "\"content\": \"DummyContent\","+
                                    "\"title\": \"DummyTitle\""+
                    "}"+
                            "}";
*/
                    //strs="{\"tempUnit\":\"C\",\"tempFormatted\":\"14ºC\",\"isNotification\":true,\"weatherCode\":1,\"alert\":{\"content\":\"DummyContent\",\"title\":\"DummyTitle\"},\"weather\":\"多云\",\"forecasts\":[{\"tempFormatted\":\"19ºC/10ºC\",\"weatherTo\":\"\",\"weatherFrom\":\"多云\",\"weatherCodeFrom\":1,\"weatherCodeTo\":-1,\"tempMin\":10,\"tempMax\":19,\"weather\":\"多云\",\"day\":1},{\"tempFormatted\":\"15ºC/5ºC\",\"weatherTo\":\"\",\"weatherFrom\":\"多云\",\"weatherCodeFrom\":1,\"weatherCodeTo\":-1,\"tempMin\":5,\"tempMax\":15,\"weather\":\"多云\",\"day\":2},{\"tempFormatted\":\"14ºC/4ºC\",\"weatherTo\":\"\",\"weatherFrom\":\"多云\",\"weatherCodeFrom\":1,\"weatherCodeTo\":-1,\"tempMin\":4,\"tempMax\":14,\"weather\":\"多云\",\"day\":3},{\"tempFormatted\":\"15ºC/7ºC\",\"weatherTo\":\"\",\"weatherFrom\":\"多云\",\"weatherCodeFrom\":1,\"weatherCodeTo\":-1,\"tempMin\":7,\"tempMax\":15,\"weather\":\"多云\",\"day\":4},{\"tempFormatted\":\"17ºC/5ºC\",\"weatherTo\":\"多云\",\"weatherFrom\":\"晴\",\"weatherCodeFrom\":0,\"weatherCodeTo\":1,\"tempMin\":5,\"tempMax\":17,\"weather\":\"晴转多云\",\"day\":5},{\"tempFormatted\":\"0ºC/0ºC\",\"weatherTo\":\"\",\"weatherFrom\":\"晴\",\"weatherCodeFrom\":0,\"weatherCodeTo\":-1,\"tempMin\":0,\"tempMax\":0,\"weather\":\"晴\",\"day\":6}],\"temp\":14,\"time\":1458658863749,\"aqiLevel\":3,\"aqi\":115}";
                    Settings.System.putString(mCon.getContentResolver(), "WeatherInfo", strs);
                    if(BLEDataTransfor.SEND_MESSAGE_METHOD_STATUS){
                        Intent intent = new Intent("com.huami.watch.action.WEATHER_UPDATE");
                        mCon.sendBroadcast(intent);
                    }else{
                        sendSimpleMessage("com.huami.watch.action.WEATHER_UPDATE",null,callBack);

                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                /*
                ArrayList<byte[]> cmds2 = new ArrayList<byte[]>();
                byte[] cmdb2 = null;
                cmdb2 = Communicates.CMD_WEATHER_TO_WATCH2.getBytes();
                cmds2.add(0, cmdb2);
                byte[] wdata = EncodeDic(cmds2);
                onWriteData(wdata,8);
                */
            }
            else if (//
                    strcmd.equalsIgnoreCase(CMD_WIFI_PASS) ||
                            strcmd.equalsIgnoreCase(CMD_rom_upgrade) ||//CMD_rom_upgrade_notify
                            strcmd.equalsIgnoreCase(CMD_rom_upgrade_notify) ||
                            strcmd.equalsIgnoreCase(CMD_wifi_input_cancel) ||
                            strcmd.equalsIgnoreCase(CMD_wifi_disconnect) ||//CMD_sportconfig_to_watch
                            strcmd.equalsIgnoreCase(CMD_sportconfig_to_watch) ||
                            strcmd.equalsIgnoreCase(CMD_health_config) ||
                            strcmd.equalsIgnoreCase(CMD_sport_config) ||
                            strcmd.equalsIgnoreCase(CMD_health_Info) ||
                            strcmd.equalsIgnoreCase(CMD_sport_Info) ||
                            strcmd.equalsIgnoreCase(CMD_sys_config) ||
                            strcmd.equalsIgnoreCase(CMD_health_config_query) ||
                            strcmd.equalsIgnoreCase(CMD_sport_config_query)  ||
                            strcmd.equalsIgnoreCase(CMD_health_Info_query) ||
                            strcmd.equalsIgnoreCase(CMD_sport_Info_query)  ||
                            strcmd.equalsIgnoreCase(CMD_sys_config_query)
                    ) {
                Log.i(TAG, "--------*****   BLE  to  watch  for "+strcmd+"  *****----------");
                ios2mywatch(cmds,callBack);
            }
            else if (strcmd.equalsIgnoreCase(CMD_CONNECTED)) {
                if (cmds.size() >=2)
                {
                    try {
                        String strs = new String(cmds.get(1), "UTF-8");
                        if(strs.equalsIgnoreCase("1"))
                        {
                            BLEDataTransfor.s().m_bInScanner_time= System.currentTimeMillis();
                        }


                        BLEDataTransfor.s().ReverseConnectOutside(strs);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                BLEDataTransfor.s().sendConnectecd();
            } else if (strcmd.equalsIgnoreCase(CMD_UNBIND)) {//CMD_UNBIND
                Log.i(TAG, "--------*****   BLE  to  watch  for  unbind  *****----------");
                mCon.sendBroadcast(new Intent(BLEDataTransfor.ACTION_HUAMI_UNBIND));
                ios2mywatch(cmds,callBack);
            }
            else if (strcmd.equalsIgnoreCase(CMD_WALL_QUERY)) {//CMD_UNBIND

                Log.i(TAG, "--------*****   BLE  to  watch  for query biaopan  *****----------");
                if(BLEDataTransfor.SEND_MESSAGE_METHOD_STATUS){
                    Intent intent;
                    intent = new Intent();
                    //intent.setAction(BLEDataTransfor.ACTION_HUAMI_DATASEND);
                    intent.setAction(BLEDataTransfor.ACTION_WALL_RECEIVER);
                    //intent.putExtra("url", ACTION_WALL_RECEIVER);
                    Bundle bd = new Bundle();
                    bd.putString("cmd", "query");
                    intent.putExtra("bd", bd);
                    mCon.sendBroadcast(intent);
                }else{
                    DataBundle bd = new DataBundle();
                    bd.putString("cmd", "query");
                    DataBundle dataBundle = new DataBundle();
                    dataBundle.putBundle("bd",bd);
                    sendSimpleMessage(BLEDataTransfor.ACTION_WALL_RECEIVER,dataBundle,callBack);
                }

            }
            else if (strcmd.equalsIgnoreCase(CMD_WALL_SET)) {
                if (cmds.size() < 3)
                    break;
                String pkname = null;
                String svname = null;
                try {
                    pkname = new String(cmds.get(1), "UTF-8");
                    svname = new String(cmds.get(2), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    bbreak = true;
                }
                if (bbreak)
                    break;
                Log.i(TAG, "--------*****   BLE  to  watch  for set biaopan  *****----------");

                if(BLEDataTransfor.SEND_MESSAGE_METHOD_STATUS){
                    Intent intent;
                    intent = new Intent();
                    //intent.setAction(BLEDataTransfor.ACTION_HUAMI_DATASEND);
                    intent.setAction(BLEDataTransfor.ACTION_WALL_RECEIVER);
                    //intent.putExtra("url", ACTION_WALL_RECEIVER);
                    Bundle bd = new Bundle();
                    bd.putString("cmd", "set");
                    bd.putString("packagename", pkname);
                    bd.putString("servicename", svname);
                    intent.putExtra("bd", bd);
                    mCon.sendBroadcast(intent);
                }else{
                    DataBundle dataBundle = new DataBundle();
                    dataBundle.putString("cmd", "set");
                    dataBundle.putString("packagename", pkname);
                    dataBundle.putString("servicename", svname);
                    DataBundle parent = new DataBundle() ;
                    parent.putBundle("bd",dataBundle);
                    sendSimpleMessage(BLEDataTransfor.ACTION_WALL_RECEIVER,parent,callBack);
                }

            }
            //CMD_WATCHWALL_QUERY
            else if (strcmd.equalsIgnoreCase(CMD_WATCHWALL_QUERY)) {
                Log.i(TAG, "--------*****   BLE  to  ios  for WATCHWALL_QUERY  *****----------");
                String pkname=dbOp.getInstance(mCon).getConfigV("face_pkname");
                String svname=dbOp.getInstance(mCon).getConfigV("face_servname");
                dbOp.getInstance(mCon).setConfigV("face_pkname",pkname,"0");
                dbOp.getInstance(mCon).setConfigV("face_servname", svname, "0");
                ConfigSyn();//
                /*Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);*/
            }
            else if (strcmd.equalsIgnoreCase(CMD_BLACKLIST_TO_WATCH)) {
                int i,ni,j;
                try {
                    do{
                        if (cmds.size() < 2)
                            break;
                        String strcount=new String(cmds.get(1), "UTF-8");
                        int icount=Integer.parseInt(strcount);
                        if(cmds.size()!=icount*2+2)
                            break;
                        ni=cmds.size();
//m_db.execSQL("CREATE TABLE if not exists blacklist (_packagename text primary key, _title text, _icon_bytes blob,_bInBlacklist text not null default('0'),_bsyn text not null default('1'),_tmp1 text)");//_type: 0 normal, 1 up, 2 down, 3 up and down
                        for(i=2;i<ni;i+=2)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            String bin = new String(cmds.get(i+1), "UTF-8");
                            String strq="update blacklist set _bInBlacklist='"+bin+"', _bsyn='1' where _packagename='"+pkname+"'";
                            dbOp.getInstance(mCon).sql2array(strq);
                        }
                        ArrayList<byte[]> cmds2 = new ArrayList<byte[]>();
                        byte[] cmdb2 = null;
                        cmdb2 = Communicates.CMD_BLACKLIST_TO_WATCH2.getBytes("UTF-8");
                        cmds2.add(0, cmdb2);
                        cmdb2 = (""+icount).getBytes("UTF-8");
                        cmds2.add(1, cmdb2);
                        for(i=2,j=2;i<ni;i+=2,j++)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            cmdb2 = pkname.getBytes("UTF-8");
                            cmds2.add(j, cmdb2);
                        }
                        byte[] wdata = EncodeDic(cmds2);
                        onWriteData(wdata,2);
                        //
                    }while (1<0);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    //bbreak = true;
                }
            }
            else if (strcmd.equalsIgnoreCase(CMD_BLACKLIST_TO_WATCH_ALL)) {
                int i,ni,j;
                try {
                    do{
                        if (cmds.size() < 2)
                            break;
                        String strcount=new String(cmds.get(1), "UTF-8");
                        int icount=Integer.parseInt(strcount);
                        if(cmds.size()!=icount*3+2)
                            break;
                        ni=cmds.size();
//m_db.execSQL("CREATE TABLE if not exists blacklist (_packagename text primary key, _title text, _icon_bytes blob,_bInBlacklist text not null default('0'),_bsyn text not null default('1'),_tmp1 text)");//_type: 0 normal, 1 up, 2 down, 3 up and down
                        for(i=2;i<ni;i+=3)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            String title = new String(cmds.get(i+1), "UTF-8");
                            String bin = new String(cmds.get(i+2), "UTF-8");

                            String strq="insert or replace into blacklist(_packagename, _title,_bInBlacklist,_bsyn) values('"+pkname+"','"+title+"','"+bin+"','1')";
                            dbOp.getInstance(mCon).sql2cmd(strq);
                            //strq="update blacklist set _bInBlacklist='"+bin+"', _bsyn='1' where _packagename='"+pkname+"'";
                            //dbOp.getInstance(mCon).sql2array(strq);
                        }
                        ArrayList<byte[]> cmds2 = new ArrayList<byte[]>();
                        byte[] cmdb2 = null;
                        cmdb2 = Communicates.CMD_BLACKLIST_TO_WATCH2.getBytes("UTF-8");
                        cmds2.add(0, cmdb2);
                        cmdb2 = (""+icount).getBytes("UTF-8");
                        cmds2.add(1, cmdb2);
                        for(i=2,j=2;i<ni;i+=3,j++)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            cmdb2 = pkname.getBytes("UTF-8");
                            cmds2.add(j, cmdb2);
                        }
                        byte[] wdata = EncodeDic(cmds2);
                        onWriteData(wdata,8);
                        //
                    }while (1<0);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    //bbreak = true;
                }
            }
            else if (strcmd.equalsIgnoreCase(CMD_BLACKLIST_TO_PHONE2)) {
                int i,ni,j;
                try {
                    do{
                        if (cmds.size() < 2)
                            break;
                        String strcount=new String(cmds.get(1), "UTF-8");
                        int icount=Integer.parseInt(strcount);
                        if(cmds.size()!=icount+2)
                            break;
                        ni=cmds.size();
//m_db.execSQL("CREATE TABLE if not exists blacklist (_packagename text primary key, _title text, _icon_bytes blob,_bInBlacklist text not null default('0'),_bsyn text not null default('1'),_tmp1 text)");//_type: 0 normal, 1 up, 2 down, 3 up and down
                        for(i=2;i<ni;i++)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            //String bin = new String(cmds.get(i+1), "UTF-8");
                            String strq="update blacklist set _bsyn='1' where _packagename='"+pkname+"'";
                            dbOp.getInstance(mCon).sql2array(strq);
                        }
                    }while (1<0);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    //bbreak = true;
                }
            }
            else if (strcmd.equalsIgnoreCase(CMD_CONFIG_TO_WATCH)) {
                int i,ni,j;
                try {
                    do{
                        if (cmds.size() < 2)
                            break;
                        String strcount=new String(cmds.get(1), "UTF-8");
                        int icount=Integer.parseInt(strcount);
                        if(cmds.size()!=icount*2+2)
                            break;
                        ni=cmds.size();
//m_db.execSQL("CREATE TABLE if not exists blacklist (_packagename text primary key, _title text, _icon_bytes blob,_bInBlacklist text not null default('0'),_bsyn text not null default('1'),_tmp1 text)");//_type: 0 normal, 1 up, 2 down, 3 up and down
                        for(i=2;i<ni;i+=2)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            String bin = new String(cmds.get(i+1), "UTF-8");
                            String strq="insert or replace into watchconfig(_name,_value,_bsyn) values('"+pkname+"','"+bin+"','1')";
                            dbOp.getInstance(mCon).sql2array(strq);
                            ///////////////////////////////////////////////
                            if(pkname.equalsIgnoreCase("log_status"))
                            {
                                Log.i(TAG, "--------*****   BLE to watch  login status   *****----------:"+bin);
                                Settings.System.putInt(mCon.getContentResolver(), Constants.LOG_STATUS, bin.equalsIgnoreCase("1")?1:0);

                                Intent intent2 = new Intent(Constants.ACTION_DEVICE_CONNECTION_CHANGED_IOS);
                                mCon.sendBroadcast(intent2);
                            }
                            else if(pkname.equalsIgnoreCase("json_str"))
                            {
                                String TEST_ORDER = "{" +
                                        "\"data\": [    " +
                                        "   {" +
                                        "      \"pkg\": \"com.huami.watch.sport\"," +
                                        "     \"cls\": \"com.huami.watch.sport.ui.view.SportLauncherView\"," +
                                        "    \"srl\": \"10\"" +
                                        "}," +
                                        "{" +
                                        "   \"pkg\": \"com.huami.watch.health\"," +
                                        "  \"cls\": \"com.huami.watch.health.widget.StepLauncherView\"," +
                                        " \"srl\": \"12\"" +
                                        "}," +
                                        "{" +
                                        "   \"pkg\": \"com.huami.watch.health\"," +
                                        "  \"cls\": \"com.huami.watch.health.widget.HeartLauncherView\"," +
                                        " \"srl\": \"232\"" +
                                        "}" +
                                        "]" +
                                        "}";

                                String TEST_ORDER_2 = "{" +
                                        "\"data\": [    " +
                                        "   {" +
                                        "      \"pkg\": \"com.huami.watch.sport\"," +
                                        "     \"cls\": \"com.huami.watch.sport.ui.view.SportLauncherView\"," +
                                        "    \"srl\": \"1\"" +
                                        "}," +
                                        "{" +
                                        "   \"pkg\": \"com.huami.watch.health\"," +
                                        "  \"cls\": \"com.huami.watch.health.widget.StepLauncherView\"," +
                                        " \"srl\": \"5\"" +
                                        "}," +
                                        "{" +
                                        "   \"pkg\": \"com.huami.watch.health\"," +
                                        "  \"cls\": \"com.huami.watch.health.widget.HeartLauncherView\"," +
                                        " \"srl\": \"3\"" +
                                        "}" +
                                        "]" +
                                        "}";
                                //bin=TEST_ORDER;
                                final String id = "springboard_widget_order_in";
                                Log.i(TAG, "springboard_widget_data: "+ bin);
                                Settings.System.putString(mCon.getContentResolver(), id, bin);
                            }
                        }
                        ArrayList<byte[]> cmds2 = new ArrayList<byte[]>();
                        byte[] cmdb2 = null;
                        cmdb2 = BLECommunicates.CMD_CONFIG_TO_WATCH2.getBytes("UTF-8");
                        cmds2.add(0, cmdb2);
                        cmdb2 = (""+icount).getBytes("UTF-8");
                        cmds2.add(1, cmdb2);
                        for(i=2,j=2;i<ni;i+=2,j++)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            cmdb2 = pkname.getBytes("UTF-8");
                            cmds2.add(j, cmdb2);
                        }
                        byte[] wdata = EncodeDic(cmds2);
                        onWriteData(wdata,2);
                        //
                    }while (1<0);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    //bbreak = true;
                }
            }
            else if (strcmd.equalsIgnoreCase(CMD_CONFIG_TO_PHONE2)) {
                Log.i(TAG, "--------*****   BLE  to   db  cf2p2  *****----------");
                int i,ni,j;
                try {
                    do{
                        if (cmds.size() < 2)
                            break;
                        String strcount=new String(cmds.get(1), "UTF-8");
                        int icount=Integer.parseInt(strcount);
                        if(cmds.size()!=icount+2)
                            break;
                        ni=cmds.size();
//m_db.execSQL("CREATE TABLE if not exists blacklist (_packagename text primary key, _title text, _icon_bytes blob,_bInBlacklist text not null default('0'),_bsyn text not null default('1'),_tmp1 text)");//_type: 0 normal, 1 up, 2 down, 3 up and down
                        for(i=2;i<ni;i++)
                        {
                            String pkname = new String(cmds.get(i), "UTF-8");
                            //String bin = new String(cmds.get(i+1), "UTF-8");
                            String strq="update watchconfig set _bsyn='1' where _name='"+pkname+"'";
                            dbOp.getInstance(mCon).sql2array(strq);
                        }
                    }while (1<0);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    //bbreak = true;
                }
            }
            else {
                int i,ni;
                ni=cmds.size();

                if(BLEDataTransfor.SEND_MESSAGE_METHOD_STATUS){
                    Intent intent;
                    intent = new Intent();
                    //intent.setAction(BLEDataTransfor.ACTION_HUAMI_DATASEND);
                    intent.setAction(strcmd);
                    //intent.putExtra("url", ACTION_WALL_RECEIVER);
                    Bundle bd = new Bundle();
                    for(i=0;i<ni;i++)
                    {
                        bd.putByteArray("byte"+i,cmds.get(i));
                        //intent.putExtra("byte"+i,cmds.get(i));
                    }
                    intent.putExtra("bd",bd);
                    mCon.sendBroadcast(intent);
                }else{
                    DataBundle dataBundle = new DataBundle();

                    for(i=0;i<ni;i++)
                    {
                        dataBundle.putByteArray("byte"+i,cmds.get(i));
                        //intent.putExtra("byte"+i,cmds.get(i));
                    }

                    DataBundle parent = new DataBundle();
                    parent.putBundle("bd",dataBundle);
                    sendSimpleMessage(strcmd,dataBundle,callBack);


                }




            }

        } while (1 < 0);




//        if(bleDataParser !=null && callBack!=null){
//            // TODO: 16-5-9  数据转换
//            callBack.recevierBLEData(bleDataParser.bytesToEntity(cmds));
//            return ;
//        }else{
//            Log.i(TAG, "docmd:  unable to send Data successful ");
//        }

    }

    public void sendSimpleMessage(String action ,DataBundle dataBundle, BLETransforListener callBack) {
        sendSimpleMessage("", action, dataBundle, callBack);
    }

    /**
     * 发送 key value 特定的数组的数值
     * @param action
     * @param callBack
     */
    public void sendSimpleMessage(String moelName, String action ,DataBundle dataBundle, BLETransforListener callBack){

        if(bleDataParser!=null && callBack!=null){
            TransportDataItem dataItem  = bleDataParser.transforToDataItem(moelName, action, dataBundle);
            callBack.recevierBLEData(dataItem);
        }else{
              if(BLEDataTransfor.DEBUG){
                      Log.i(TAG, "------sendSimpleMessage--fail because: bleDataParser is null Or callBack is  null  -");
                }
        }
    }


    /**
     * @param action action value in TransportUri.ACTION_ADD or TransportUri.ACTION_DEL
     * @param pkg
     * @param uid
     * @param title
     * @param content
     * @param when
     * @return the TransportDataItem
     */
    public void sendNoticationMessage(String action, String pkg, byte[] uid, String title, String content, long when,BLETransforListener callBack) {
        TransportDataItem dataItem = new TransportDataItem(TransporterModules.MODULE_NOTIFICAION);
        dataItem.addAction(action);
        DataBundle mDataBundle = new DataBundle();
        StatusBarNotificationData statusNotiData = StatusBarNotificationData.fromIOS(pkg, uid, title, content,
                when);
        mDataBundle.putParcelable(TransportUri.KEY_DATA, statusNotiData);
        dataItem.setData(mDataBundle);

        if(callBack !=null){
            callBack.recevierBLEData(dataItem);

        }

    }


    public void sendTimeData(BluetoothGattCharacteristic characteristic ,BLETransforListener bleTransforListener){

        String month_prefix = "";
        String day_prefix = "";
        String hour_prefix = "";
        String min_prefix = "";
        String sec_prefix = "";

        int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
        //Log.d(TAG_REMOTE, "year:: " + year);

        int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 2);
        if (month < 10) {
            month_prefix = new String("0" + month);
        } else {
            month_prefix = String.valueOf(month);
        }

        int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 3);
        if (day < 10) {
            day_prefix = new String("0" + day);
        } else {
            day_prefix = String.valueOf(day);
        }
        int hour = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
        if (hour < 10) {
            hour_prefix = new String("0" + hour);
        } else {
            hour_prefix = String.valueOf(hour);
        }

        int min = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 5);
        if (min < 10) {
            min_prefix = new String("0" + min);
        } else {
            min_prefix = String.valueOf(min);
        }

        int sec = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 6);
        if (sec < 10) {
            sec_prefix = new String("0" + sec);
        } else {
            sec_prefix = String.valueOf(sec);
        }

        int date = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 7);
        ArrayList<String> arrs=new ArrayList<String>();
        arrs.add(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_MYWATCH_syntime);
        arrs.add(""+year);
        arrs.add(""+(month-1));
        arrs.add(""+day);
        arrs.add("" + hour);
        arrs.add("" + min);
//        Intent sitIntent = new Intent(BLEDataTransfor.ACTION_HUAMI_DATASEND_IOS_MYWATCH);
//        sitIntent.putStringArrayListExtra("cmds",arrs);
//        sendBroadcast(sitIntent);

    }
// ########################## handle  agps data  start  ###############################################
    private int agpsFileLength = 0; // AGPS 长度
    private int agpsPackageLocation=0 ; // AGPS 位置

    /**
     * 大数据传输通道处理
     * @param pd
     * @param bleListener
     */
    public synchronized void processing_AGPS_Data(byte[] pd, BLETransforListener bleListener){
        Log.i(TAG_AGPS, "processing_AGPS_Data: ");
        if(pd.length==0){
            return ;
        }
        byte syncCode = pd[0]; // 同步码
        byte agpsPackageType = pd[1]; // 包类型
        switch (agpsPackageType){
            case AGPS_PACKAGE_HEAD: // 包头
                Log.i(TAG_AGPS, " receiver AGPS package head  ");
                byte[] agpsLengthBytes = {pd[2],pd[3]};
                agpsFileLength = Utils.byteArrayToInt(agpsLengthBytes);// AGPS 文件包的长度
                returnIosAgpsHeaderStatusCode();
                handleAGPSData(pd , 0 , bleListener );
                break;
            case AGPS_PACKAGE_DATA: // 包内容
                Log.i(TAG_AGPS, " receiver AGPS package data  ");
                byte[] agpsPositionBytes = {pd[2],pd[3]};
                agpsPackageLocation = Utils.byteArrayToInt(agpsPositionBytes); // AGPS 数据所在package 位置
                handleAGPSData(pd, agpsPackageLocation, bleListener);
                break;
            // 包尾根据字节大小自行判断
//            case AGPS_PACKAGE_END:// 包 结束;
//                byte[] agpsPositionBytes2 = {pd[2],pd[3]};
//                agpsPackageLocation = Utils.byteArrayToInt(agpsPositionBytes2); // AGPS 数据所在package 位置
//                handleAGPSData(pd, agpsPackageLocation, bleListener);
//                break;
        }
    }

    /**
     *  AGPS 头文件接受回执
     */
    private void returnIosAgpsHeaderStatusCode(){
        Log.i(TAG_AGPS, "---- receiver AGPS head Data success , return ios head statusCode------");
        byte[]  bytes  ={} ;
        // TODO: 16-5-30  待确定 返回状态的信息
        sendToIosData(bytes);
    }

    /**
     * 发送AGPS 数据单元
     * @param bytes
     */
    private void sendToIosData(byte[] bytes){
        Log.i(TAG_AGPS, " sendToIosData ");
        BluetoothDevice device = BLEDataTransfor.mBluetoothDevice_phone;
        if (device == null)
            device = BLEDataTransfor.mBluetoothDevice_newlywrite;
        if (BLEDataTransfor.sGattServer != null
                && device != null) {
            BLEDataTransfor.s().Cha10_sendData(device,bytes);
        }
    }

    /**
     * @param pd
     * @param position
     * @param bleListener
     */
    private int currentBatchPackageNum  ; // 当前批的 第几个包
    private ByteArrayOutputStream currentBatchBytes ;
    private void handleAGPSData (byte[] pd , int position , BLETransforListener bleListener){
        Log.i(TAG_AGPS, " handleAGPSData ");

        /**
         * 分包 注意  position , AGPS_BATCH_PACKNUM 统计的分支
         */
        if(Utils.getFileLength(position,16,pd.length-4)==agpsFileLength){ // 文件接受 结束
            Log.i(TAG_AGPS, "handleAGPSData: receiver AGPS Data over  ");
            // TODO: 16-5-30  handle 文件的整体合包逻辑

        }else {
            ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
            contentBytes.write(pd, 3, pd.length - 1);
            currentBatchPackageNum++;
            Log.i(TAG_AGPS, "handleAGPSData: currentBatchPackageNum:" + currentBatchPackageNum + ", position:" + position);
            if (position % AGPS_BATCH_PACKNUM == 0) { // 创建 AGPS 批数据开始
                try {
                    Log.i(TAG_AGPS, "handleAGPSData: write AGPS Data batch status: head  ");
                    currentBatchBytes = new ByteArrayOutputStream();
                    contentBytes.writeTo(currentBatchBytes);
                } catch (IOException e) {
                    currentBatchPackageNum --;
                    Log.i(TAG, "handleAGPSData: write AGPS Data batch status: head is error ");
                    e.printStackTrace();
                }
                return;
            } else if (position % AGPS_BATCH_PACKNUM != (AGPS_BATCH_PACKNUM - 1)) {
                try {
                    Log.i(TAG_AGPS, "handleAGPSData: write AGPS Data batch status: adding  ");
                    contentBytes.writeTo(currentBatchBytes);
                } catch (IOException e) {
                    currentBatchPackageNum-- ;
                    Log.i(TAG, "handleAGPSData: write AGPS Data batch status: adding is error ");
                    e.printStackTrace();
                }
                return;
            } else if ((position % AGPS_BATCH_PACKNUM == (AGPS_BATCH_PACKNUM - 1)) && (currentBatchPackageNum == AGPS_BATCH_PACKNUM)) { // 整批数据处理
                Log.i(TAG_AGPS, "handleAGPSData: handle AGPS Data batch status: over   ");
                currentBatchPackageNum = 0;
                // TODO: 16-5-30 结束一批数据
                /**
                 * 批数据保存 将 currentBatchBytes 保存
                 */
                return;
            } else {
                currentBatchPackageNum = 0;

                int returnPostion = Utils.getAgpsBatchStartNumNear(position,AGPS_BATCH_PACKNUM);
                // TODO: 16-5-30 批数据重发机制 将 position 回退 然后 发送 ios 重发该批数据.
                /**
                 *  returnPosition 需要对接一下 ,
                 */
                return;
            }
        }

    }
    // ########################## handle  agps data  end  ###############################################
}
