package com.example.jinliang.myapplication.bledemo;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.huami.watch.ble.BLEService;
import com.huami.watch.ble.Communicates;
import com.huami.watch.ble.dbOp;
import com.huami.watch.ble.trasnfer.BLEDataTransfor;
import com.huami.watch.transport.DataBundle;
import com.huami.watch.transport.TransportDataItem;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by jinliang on 16-5-9.
 * Ble 数据解析工具
 */
public class BLEDataParser {
    // TransportDataItem 权限

     private final static String TAG_LOG = BLEDataParser.class.getName();
    public static final String  authority = "com.huami.watch.ble" ;
    // ####################  蓝牙数据 由底层 To 消息中心发送  解析工具类
    public static int amsFlag =0;
    public TransportDataItem bytesToEntity(ArrayList<byte[]> bytes) {
        // TODO: 16-5-9

        return new TransportDataItem();
    }

    /**
     * 数据类型的包装
     * @param action  消息类型
     * @param dataBundle  数据携带
     * @return
     */
    public TransportDataItem transforToDataItem(String modelName, String action, DataBundle dataBundle) {
        if (modelName == null) {
            throw new NullPointerException("modelName can not be null.");
        }
        // TODO: 16-5-9  设置 action
        TransportDataItem dataItem = new TransportDataItem(modelName);
        if (action != null) {
            dataItem.addAction(action);
        }
        if(dataBundle==null){
            dataBundle = new DataBundle();
        }
        dataItem.setData(dataBundle);
        return dataItem;
    }

    /**
     *  数据类型转换
     * @param dataItem from  消息分发service DataTransportService
     */
    public void dispathMessageReceived (TransportDataItem dataItem){
         String action  = dataItem.getAction() ;
         DataBundle dataBundle = dataItem.getData();
         if(action==null){
             return ;
         }
            //Log.v(TAG_LOG, action);
            if (action.equalsIgnoreCase(BLEService.ACTION_HUAMI_DATASEND_IOS_FORWATCH)) {
           handleMessageAction_ACTION_WALL_RECEIVER(dataBundle);
            } else if (action.equalsIgnoreCase(BLEService.ACTION_HUAMI_DATASEND_IOS_FINDPHONE)) {
                       handleMessageAction_FindPhone();
            } else if (
                    action.equalsIgnoreCase(BLEService.ACTION_HUAMI_DATASEND_IOS_DIRECT)
                            ||
                            action.equalsIgnoreCase(BLEService.ACTION_HUAMI_DATASEND_IOS_DIRECT8)
                    ) {
                handlerMessageAction_ACTION_HUAMI_DATASEND_IOS_DIRECT(dataBundle,action);
                //
            }
            //ACTION_HUAMI_IOS_ANCS_BLOCK
            else if (action.equalsIgnoreCase(BLEService.ACTION_HUAMI_IOS_ANCS_BLOCK)) {

                handlerMessageAction_ACTION_HUAMI_IOS_ANCS_BLOCK(dataBundle);

            }//
            else if (action.equalsIgnoreCase(BLEService.ACTION_HUAMI_IOS_ANCS_DEL_TO_PHONE) || action.equalsIgnoreCase(BLEService.ACTION_HUAMI_IOS_ANCS_DEL_FROM_CARD)) {
                do {
                    if (BLEDataTransfor.s().mRemoteGatt == null)
                        break;
                    byte _action_id = 0x01;
                    byte[] uid=dataBundle.getByteArray("uid");
                    byte[] get_notification_attribute = {
                            (byte)0x02,
                            //UID
                            uid[0], uid[1], uid[2], uid[3],
                            //action
                            _action_id
                    };
                    BLEDataTransfor.s().writegatt(BLEDataTransfor.s().mRemoteGatt, get_notification_attribute);
                }while (1<0);
            }
            else if (action.equalsIgnoreCase(BLEService.ACTION_HUAMI_IOS_ANCS_APPLY)) {
                do {
                    if (BLEDataTransfor.s().mRemoteGatt == null)
                        break;
                    byte _action_id = 0x01;
                    byte[] uid=dataBundle.getByteArray("uid");
                    String apply=dataBundle.getString("apply");
                    if(apply.equalsIgnoreCase("1"))
                    {
                        _action_id = 0x00;
                        byte[] get_notification_attribute = {
                                (byte)0x02,
                                //UID
                                uid[0], uid[1], uid[2], uid[3],
                                //action
                                _action_id
                        };
                        BLEDataTransfor.s().writegatt(BLEDataTransfor.s().mRemoteGatt, get_notification_attribute);
                    }
                    else if(apply.equalsIgnoreCase("0"))
                    {
                        //_action_id = 0x00;
                        byte[] get_notification_attribute = {
                                (byte)0x02,
                                //UID
                                uid[0], uid[1], uid[2], uid[3],
                                //action
                                _action_id
                        };
                        BLEDataTransfor.s().writegatt(BLEDataTransfor.s().mRemoteGatt, get_notification_attribute);
                    }

                }while (1<0);
            }//BLEService.ACTION_HUAMI_BOOT_COMPLETED
            else if (action.equalsIgnoreCase(BLEService.ACTION_HUAMI_BOOT_COMPLETED)) {
                //BLEService.s().startAdver0(3000);
            }
            else if (action.equalsIgnoreCase(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = dataBundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
                int itype=device.getType();
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                    {
                        Log.d(TAG_LOG, "正在配对......type:"+itype);
                        if(BLEDataTransfor.s().mBluetoothDevice_newlywrite!=null &&

                                (itype!=device.DEVICE_TYPE_CLASSIC)
                                )
                        {
                            byte[] bys = new byte[]{'2'};
                            BLEDataTransfor.s().Cha9_sendData(device, bys);
                        }
                        else
                        {
                            Log.d(TAG_LOG, "not in ble mode");
                        }
                    }
                    break;
                    case BluetoothDevice.BOND_BONDED:
                    {
                        Log.d(TAG_LOG, "完成配对......type:"+itype);
                        if(BLEDataTransfor.s().mBluetoothDevice_newlywrite!=null &&
                                (itype!=device.DEVICE_TYPE_CLASSIC)
                                )
                        {
                            byte[] bys = new byte[]{'3'};
                            BLEDataTransfor.s().Cha9_sendData(device, bys);
                            BLEDataTransfor.s().connectAncsOutside(device);
                        }
                        else
                        {
                            Log.d(TAG_LOG, "not in ble mode");
                        }
                    }
                    break;
                    case BluetoothDevice.BOND_NONE:
                    {
                        Log.d(TAG_LOG, "取消配对......type:"+itype);
                        if(BLEDataTransfor.s().mBluetoothDevice_newlywrite!=null &&
                                (itype!=device.DEVICE_TYPE_CLASSIC)
                                )
                        {
                            BLEDataTransfor.s().Cha9_setCancelStatus("1");
                            BLEDataTransfor.s().mSimulatorHandler.sendEmptyMessageDelayed(BLEService.MSG_SET_BOND_CANCEL_STATUS, 3000);

                        }
                        else
                        {
                            Log.d(TAG_LOG, "not in ble mode");
                        }
                    }
                    default:
                        break;
                }
            }
            //


    }



    //#################### 处理方法单元 ###########################

private void handleMessageAction_ACTION_WALL_RECEIVER (DataBundle dataBundle){

    String url = dataBundle.getString("url");
    DataBundle bd = dataBundle.getBundle("bd");
    if (url == null)
        return ;
    if (url.equalsIgnoreCase(BLEDataTransfor.ACTION_WALL_RECEIVER)) {
        String cmd = bd.getString("cmd");
        do {
            if (cmd == null)
                break;
            //set_reply
            if (cmd.equalsIgnoreCase("set_reply")) {
                String pkname = null;
                String svname = null;
                if (bd.containsKey("packagename" ))
                    pkname = bd.getString("packagename");
                if (bd.containsKey("servicename" ))
                    svname = bd.getString("servicename" );
                if(pkname!=null && svname!=null)
                {
                    dbOp.getInstance(BLEDataTransfor.s().mContex).setConfigV("face_pkname",pkname,"0");
                    dbOp.getInstance(BLEDataTransfor.s().mContex).setConfigV("face_servname",svname,"0");
                    BLEDataTransfor.s().bleCommunicates.ConfigSyn();
                }

            } else if (cmd.equalsIgnoreCase("query_reply")) {
                ArrayList<byte[]> cmds = new ArrayList<byte[]>();
                if (!bd.containsKey("ncount"))
                    break;
                int i;
                int ncount = bd.getInt("ncount");
                //
                if (bd.containsKey("selectedpackagename" ) && bd.containsKey("selectedservicename" ))
                {
                    String pkname = bd.getString("selectedpackagename");
                    String svname = bd.getString("selectedservicename" );
                    dbOp.getInstance(BLEDataTransfor.s().mContex).setConfigV("face_pkname",pkname,"0");
                    dbOp.getInstance(BLEDataTransfor.s().mContex).setConfigV("face_servname",svname,"0");
                    BLEDataTransfor.s().bleCommunicates.ConfigSyn();
                }
                //
                try {
                    cmds.add(String.valueOf(ncount).getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (cmds.size() == 0)
                    break;

                for (i = 0; i < ncount; i++) {
                    String pkname = null;
                    String svname = null;
                    String title = null;
                    byte[] icon_bytes=null;
                    //
                    if (bd.containsKey("packagename" + i))
                        pkname = bd.getString("packagename" + i);
                    if (bd.containsKey("servicename" + i))
                        svname = bd.getString("servicename" + i);
                    if (bd.containsKey("title" + i))
                        title = bd.getString("title" + i);
                    //export face image
                                    /*if(bd.containsKey("icon_bytes"+i))
                                        icon_bytes=bd.getByteArray("icon_bytes" + i);
                                    if (pkname != null && svname != null && title != null && icon_bytes!=null) {
                                        //Bitmap bm= BitmapFactory.decodeByteArray(icon_bytes, 0, icon_bytes.length);
                                        try {
                                            String filepath= Environment.getExternalStorageDirectory()+
                                            pkname+"_"+svname+"_"+title+".png";
                                            Log.d(TAG_LOG, filepath);
                                            File file = new File(Environment.getExternalStorageDirectory(),
                                                    pkname+"_"+svname+"_"+title+".png");
                                            FileOutputStream fos = new FileOutputStream(file);
                                            //String info = "I am a chinanese!";
                                            fos.write(icon_bytes);
                                            fos.close();
                                            //System.out.println("写入成功：");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }*/
                    //end for export
                    if (pkname != null && svname != null && title != null) {
                        try {
                            cmds.add(pkname.getBytes("UTF-8"));
                            cmds.add(svname.getBytes("UTF-8"));
                            cmds.add(title.getBytes("UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (cmds.size() == (ncount * 3 + 1)) {
                    String cmd2 = Communicates.CMD_WALL_QUERY + "2";
                    byte[] cmdb = null;
                    try {
                        cmdb = cmd2.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    cmds.add(0, cmdb);
                    byte[] wdata = BLEDataTransfor.s().bleCommunicates.EncodeDic(cmds);
                    BLEDataTransfor.s().bleCommunicates.onWriteData(wdata,2);
                    //onWriteData(wdata);
                                    /*
                                    mOperator.saveAll(nail);
                                    //
                                    Intent intent2 = new Intent(ACTION_WALL_RECEIVER_REPLY);
                                    intent2.putExtra("bd", bd);
                                    context.sendBroadcast(intent2);
                                    */
                }

            }
        } while (1 < 0);
    }
}

    // 发现手机的数据转换
    private void handleMessageAction_FindPhone(){
        do {
            if (BLEDataTransfor.s().mRemoteGatt == null)
                break;
            //byte[] bytes=new byte[]{(byte) 0x01};
            amsFlag=16;
            byte[] bytes=new byte[]{(byte) 0x0};
            //byte[] bytes=new byte[]{(byte) 0x05};
            BLEDataTransfor.s().writegatt_ams(BLEDataTransfor.s().mRemoteGatt, bytes);

        }while (1<0);
    }

    private void handlerMessageAction_ACTION_HUAMI_DATASEND_IOS_DIRECT(DataBundle dataBundle,String action ){
        ArrayList<byte[]> cmds = new ArrayList<byte[]>();
        int i, ni;
        for(i=0;1>0;i++)
        {
            String bytei="byte"+i;
            if(dataBundle.containsKey(bytei))
            {
                cmds.add(dataBundle.getByteArray(bytei));
            }
            else
            {
                break;
            }
        }
        byte[] wdata = BLEDataTransfor.s().bleCommunicates.EncodeDic(cmds);

        if (action.equalsIgnoreCase(BLEService.ACTION_HUAMI_DATASEND_IOS_DIRECT))
            BLEDataTransfor.s().bleCommunicates.onWriteData(wdata,2);
        else
            BLEDataTransfor.s().bleCommunicates.onWriteData(wdata,8);
    }


    private void handlerMessageAction_ACTION_HUAMI_IOS_ANCS_BLOCK(DataBundle databundle){

        do {
            String pkgname=databundle.getString("pkgname");
            if(pkgname==null)
                break;
            String strq="update blacklist set _bInBlacklist='1',_bsyn='0' where _packagename='"+pkgname+"'";
            dbOp.getInstance(BLEDataTransfor.s().mContex).sql2array(strq);
            BLEDataTransfor.s().bleCommunicates.BlackListSyn();

        }while(1<0);



    }
}
