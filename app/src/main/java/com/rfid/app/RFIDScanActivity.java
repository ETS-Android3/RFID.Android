package com.rfid.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.goebl.david.Response;
import com.goebl.david.Webb;


import com.rscja.deviceapi.BuildConfig;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RFIDScanActivity extends Activity {
    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    private Handler handler;
    private ArrayList<Product> products;
    private ListTagView adapter;

    private TextView tv_count;

    private RadioGroup RgInventory;
    private RadioButton RbInventorySingle;
    private RadioButton RbInventoryLoop;
    private final String HOST = "http://192.168.1.5:8080";

    private Button BtClear;
    private Button BtImport;
    private Button BtInventory;
    private Button BtView;

    private ListView LvTags;
    private HashMap<String, String> map;
    private RFIDWithUHFUART mReader;

    private String fCurFilePath = "";
    private boolean fIsEmulator = false;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            setContentView(R.layout.activity_rfid_scan);
            setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);

            BtClear = (Button) findViewById(R.id.BtClear);
            BtImport = (Button) findViewById(R.id.BtImport);
            BtView = (Button) findViewById(R.id.BtView);
            tv_count = (TextView) findViewById(R.id.tv_count);
            RgInventory = (RadioGroup) findViewById(R.id.RgInventory);
            RbInventorySingle = (RadioButton) findViewById(R.id.RbInventorySingle);
            RbInventoryLoop = (RadioButton) findViewById(R.id.RbInventoryLoop);
            BtInventory = (Button) findViewById(R.id.BtInventory);
            LvTags = (ListView) findViewById(R.id.LvTags);
            products = initTag();
            adapter = new ListTagView(this, products);

            BtClear.setOnClickListener(new BtClearClickListener());
            BtImport.setOnClickListener(new BtImportClickListener());
            RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
            BtInventory.setOnClickListener(new BtInventoryClickListener());
            BtView.setOnClickListener(new BtViewClickListener());

            LvTags.setAdapter(adapter);
//            clearData();

            handler = new Handler() {
                @SuppressLint("HandlerLeak")
                @Override
                public void handleMessage(Message msg) {
                    String result = msg.obj + "";
                    String[] strs = result.split("@");
                    if (addEPCToList(strs[0], strs[1]))
                        UIHelper.playSoundSuccess();
                }
            };

            fIsEmulator = UIHelper.isEmulator();
            UIHelper.initSound(RFIDScanActivity.this);
            initUHF();
        } catch (Exception ex) {
            UIHelper.showExceptionError(RFIDScanActivity.this, ex);
        }
    }

    public ArrayList<Product> initTag() {
        Webb webb = Webb.create();
        webb.setBaseUri(HOST);

        Response<JSONObject> response = webb
                .get("/products")
                .ensureSuccess()
                .asJsonObject();
        JSONObject object = response.getBody();
        ArrayList<Product> result = new ArrayList<>();

        try {
            JSONArray data = object.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {

                JSONObject product = data.getJSONObject(i);
                JSONArray tagArray = product.getJSONArray("tags");
                for (int j = 0; j < tagArray.length(); j++) {
                    JSONObject tag = tagArray.getJSONObject(j);
                    Product item = new Product();
                    item.setProductId(product.getString("productId"));
                    item.setTagId(tag.getString("tagId"));
                    item.setName(product.getString("name"));
                    item.setStock(product.getInt("stock"));
                    item.setColor(tag.getString("color"));
                    item.setCount(0);
                    result.add(item);
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void initUHF() {
        // temporary check this, on emulator device mReader InitTask cause crash application
        if (!fIsEmulator) {
            if (mReader == null) {
                try {
                    mReader = RFIDWithUHFUART.getInstance();
                } catch (Exception ex) {
                    UIHelper.showExceptionError(RFIDScanActivity.this, ex);
                    return;
                }

                if (mReader != null) {
                    new InitTask().execute();
                }
            }
        }
    }

    /**
     * @author liuruifeng
     */
    private class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                return mReader.init();
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
                Toast.makeText(RFIDScanActivity.this, "init fail", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            try {
                super.onPreExecute();

                mypDialog = new ProgressDialog(RFIDScanActivity.this);
                mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mypDialog.setMessage("init...");
                mypDialog.setCanceledOnTouchOutside(false);
                mypDialog.show();

            } catch (Exception ex) {
                UIHelper.showExceptionError(RFIDScanActivity.this, ex);
                return;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopInventory();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (event.getRepeatCount() == 0) {
                readTag();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * @param epc
     */
    private boolean addEPCToList(String epc, String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            int index = checkIsExist(epc);

            if (index == -1) {
                UIHelper.ToastMessage(RFIDScanActivity.this, "can not find product id");
            } else {
                int tagcount = products.get(index).getCount() + 1;

                products.get(index).setCount(tagcount);
            }

            adapter.notifyDataSetChanged();
            if (index >= 0)
                return false;

            return true;
        }
        return false;
    }

    private class BtClearClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
        }
    }

    private class BtImportClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (BtInventory.getText().equals(getString(R.string.btInventory))) {
                if (products.size() == 0) {
                    UIHelper.ToastMessage(RFIDScanActivity.this, "No data");
                    return;
                }

                // save to SQL
//                 boolean re = FileImport.SaveSQL(tagList, RFIDScanActivity.this);
                try {

                    JSONArray jsonArray = new JSONArray();
                    products.stream().forEach(t -> {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("tagId", t.getTagId());
                            jsonObject.put("count", t.getCount());
                            jsonObject.put("rssi", "none");

                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    Webb webb = Webb.create();
                    webb.setBaseUri(HOST);

                    Response<JSONObject> response = webb
                            .post("/products/tags")
                            .body(jsonArray)
                            .ensureSuccess()
                            .asJsonObject();
//
                    // save excel file

//                    if (response.getBody().has("data")) {
                    fCurFilePath = FileImport.FilePathTxt;
                    UIHelper.ToastMessage(RFIDScanActivity.this, getString(R.string.uhf_msg_inventory_save_success));
                    tv_count.setText("0");
                    products.clear();
                    adapter.notifyDataSetChanged();
//                    }
                } catch (Exception ex) {
                    UIHelper.showExceptionError(RFIDScanActivity.this, ex);
                }
            } else {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_save_wanrning);
            }
        }
    }

    private class BtViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (BtInventory.getText().equals(getString(R.string.btInventory))) {
//                if (UIHelper.isNullOrEmpty(fCurFilePath)) {
//                    UIHelper.ToastMessage(RFIDScanActivity.this, "No file!");
//                    return;
//                }
                Intent in = new Intent(RFIDScanActivity.this, RFIDViewActivity.class);
                in.putExtra("IntentObject", fCurFilePath);
                startActivity(in);
            } else {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_save_wanrning);
            }
        }
    }

    private void clearData() {
        tv_count.setText("0");
        products = initTag();
        adapter = new ListTagView(this, products);
        adapter.notifyDataSetChanged();
        LvTags.setAdapter(adapter);
    }

    public class RgInventoryCheckedListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == RbInventorySingle.getId()) {
                inventoryFlag = 0;
            } else if (checkedId == RbInventoryLoop.getId()) {
                inventoryFlag = 1;
            }
        }
    }

    public class BtInventoryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(getString(R.string.btInventory))) {
            if (mReader == null) {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_sdk_open_fail);
                return;
            }

            switch (inventoryFlag) {
                case 0: {
                    UHFTAGInfo strUII = mReader.inventorySingleTag();
                    if (strUII != null) {
                        String strEPC = strUII.getEPC();
                        addEPCToList(strEPC, strUII.getRssi());
                        UIHelper.playSoundSuccess();
                        tv_count.setText("" + adapter.getCount());
                    } else {
                        UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_fail);
                    }
                }
                break;
                case 1://  .startInventoryTag((byte) 0, (byte) 0))
                {
                    if (mReader.startInventoryTag()) {
                        BtInventory.setText(getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        setViewEnabled(false);
                        new TagThread().start();
                    } else {
                        mReader.stopInventory();
                        UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_open_fail);
                    }
                }
                break;
                default:
                    break;
            }
        } else {
            stopInventory();
        }
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mReader.stopInventory()) {
                BtInventory.setText(getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    /**
     * @param strEPC
     * @return
     */
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (strEPC == null || strEPC.length() == 0) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < products.size(); i++) {
            Product temp = new Product();
            temp = products.get(i);
            if (strEPC.equals(temp.getTagId())) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    private class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            UHFTAGInfo res = null;
            while (loopFlag) {
                res = mReader.readTagFromBuffer();
                if (res != null) {
                    strTid = res.getTid();
                    if (strTid.length() != 0 && !strTid.equals("0000000" + "000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }

                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + res.getEPC() + "@" + res.getRssi();

                    handler.sendMessage(msg);
                }
            }
        }
    }

}
