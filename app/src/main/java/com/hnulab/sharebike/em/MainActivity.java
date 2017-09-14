package com.hnulab.sharebike.em;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.google.gson.Gson;
import com.hnulab.sharebike.em.activity.DestinationActivity;
import com.hnulab.sharebike.em.activity.LoginActivity;
import com.hnulab.sharebike.em.activity.MyMessageActivity;
import com.hnulab.sharebike.em.activity.MyTripActivity;
import com.hnulab.sharebike.em.activity.MyWalletActivity;
import com.hnulab.sharebike.em.activity.PersonalInformationActivity;
import com.hnulab.sharebike.em.activity.UserKnowActivity;
import com.hnulab.sharebike.em.base.EnvData;
import com.hnulab.sharebike.em.broadcast.BluetoothReceiver;
import com.hnulab.sharebike.em.databinding.ActivityMainBinding;
import com.hnulab.sharebike.em.dialog.LoadDialog;
import com.hnulab.sharebike.em.lib.LocationTask;
import com.hnulab.sharebike.em.lib.OnLocationGetListener;
import com.hnulab.sharebike.em.lib.PositionEntity;
import com.hnulab.sharebike.em.lib.RegeocodeTask;
import com.hnulab.sharebike.em.lib.RouteTask;
import com.hnulab.sharebike.em.lib.Sha1;
import com.hnulab.sharebike.em.lib.Utils;
import com.hnulab.sharebike.em.overlay.WalkRouteOverlay;
import com.hnulab.sharebike.em.util.AMapUtil;
import com.hnulab.sharebike.em.util.BluetoothAutoConnectUtils;
import com.hnulab.sharebike.em.util.CommonUtils;
import com.hnulab.sharebike.em.util.ToastUtil;
import com.hnulab.sharebike.em.view.statusbar.StatusBarUtil;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//import com.hnulab.sharebike.em.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements AMap.OnCameraChangeListener,
        AMap.OnMapLoadedListener, OnLocationGetListener, View.OnClickListener, RouteTask.OnRouteCalculateListener,
        AMap.OnMapTouchListener, RouteSearch.OnRouteSearchListener, AMap.OnMapClickListener, AMap.InfoWindowAdapter {
    public static final String TAG = "MainActivity";
    public static final int REQUEST_CODE = 1;
    //地图view
    MapView mMapView = null;
    //初始化地图控制器对象
    AMap aMap;
    //刷新定位
    ImageView iv_refresh, iv_scan_code;

    //定位
    private LocationTask mLocationTask;
    //逆地理编码功能
    private RegeocodeTask mRegeocodeTask;
    //绘制点标记
    private Marker mPositionMark, mInitialMark, tempMark;//可移动、圆点、点击
    //初始坐标、移动记录坐标
    private LatLng mStartPosition, mRecordPositon;
    //默认添加一次
    private boolean mIsFirst = true;
    //就第一次显示位置
    private boolean mIsFirstShow = true;

    private LatLng initLocation;

    // 一定需要对应的bean
    private ActivityMainBinding mBinding;

    private NavigationView navView;
    private DrawerLayout drawerLayout;
    private FrameLayout llTitleMenu;
    private Toolbar toolbar;

    private ValueAnimator animator = null;//坐标动画
    private BitmapDescriptor initBitmap, moveBitmap, smallIdentificationBitmap, bigIdentificationBitmap, bigredpacageBitmap;//定位圆点、可移动、所有标识（车）
    private RouteSearch mRouteSearch;

    private WalkRouteResult mWalkRouteResult;
    private LatLonPoint mStartPoint = null;//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = null;//终点，116.481288,39.995576
    private final int ROUTE_TYPE_WALK = 3;
    private boolean isClickIdentification = false;
    WalkRouteOverlay walkRouteOverlay;//路线
    private String[] time;
    private String distance;

    //蓝牙广播
    //广播action
    private String ACTION_UPDATEUI = "com.hnulab.sharebike.update";
    //设备mac地址key
    public static String EXTRA_DEVICE_ADDRESS = "address";
    //广播接收者
    BroadcastReceiver broadcastReceiver;
    //获取本地蓝牙适配器，即蓝牙设备
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    //用来保存存储的文件名
    public String filename = "";
    //蓝牙设备
    BluetoothDevice _device = null;
    //蓝牙通信socket
    BluetoothSocket _socket = null;
    boolean bRun = true;
    boolean bThread = false;
    //宏定义查询设备句柄
    private final static int REQUEST_CONNECT_DEVICE = 1;
    //SPP服务UUID号
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    //输入流，用来接收蓝牙数据
    private InputStream is;
    //显示用数据缓存
    private String smsg = "";
    //保存用数据缓存
    private String fmsg = "";
    public boolean flag = true;
    String message = "";
    int nn = 1;

    public String key = "";
    //当前二氧化碳浓度
    private String co2_data;
    //蓝牙权限
    private int MY_PERMISSION_REQUEST_CONSTANT = 1;


    //时间获取格式
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");

    private List<EnvData> envDatas = new ArrayList<>();
    private boolean isUpload = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化xutils3.5
        x.Ext.init(getApplication());
        x.Ext.setDebug(org.xutils.BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        x.view().inject(this);

        mBinding = DataBindingUtil.setContentView(this, com.hnulab.sharebike.em.R.layout.activity_main);
        initId();
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, drawerLayout, CommonUtils.getColor(com.hnulab.sharebike.em.R.color.colorTheme));
        initToolbar();
        initDrawerLayout();
        //获取地图控件引用
        mMapView = (MapView) findViewById(com.hnulab.sharebike.em.R.id.map);
        iv_refresh = (ImageView) findViewById(com.hnulab.sharebike.em.R.id.iv_refresh);
        iv_refresh.setOnClickListener(this);
        iv_scan_code = (ImageView) findViewById(com.hnulab.sharebike.em.R.id.iv_scan_code);
        iv_scan_code.setOnClickListener(this);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initBitmap();
        initAMap();
        initLocation();
        RouteTask.getInstance(getApplicationContext())
                .addRouteCalculateListener(this);
        Log.e(TAG, "sha1" + Sha1.sHA1(this));
        //Android 6.0 蓝牙权限问题
        if (Build.VERSION.SDK_INT >= 6.0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CONSTANT);
        }
        //蓝牙连接功能
        initbroadcast();
        if (_bluetooth == null) {
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // 设置设备可以被搜索
        new Thread() {
            public void run() {
                if (_bluetooth.isEnabled() == false) {
                    _bluetooth.enable();
                }
            }
        }.start();

    }

    private void initBitmap() {
        initBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.location_marker);
        moveBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.icon_loaction_start);
        smallIdentificationBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.stable_cluster_marker_one_normal);
        bigIdentificationBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.stable_cluster_marker_one_select);
        bigredpacageBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_red_package_big);
    }

    private void initId() {
        drawerLayout = mBinding.drawerLayout;
        navView = mBinding.navView;
        toolbar = mBinding.include.toolbar;
        llTitleMenu = mBinding.include.llTitleMenu;
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initDrawerLayout() {
        navView.inflateHeaderView(com.hnulab.sharebike.em.R.layout.nav_header_main);
        View headerView = navView.getHeaderView(0);
        RelativeLayout rl_header_bg = (RelativeLayout) headerView.findViewById(R.id.rl_header_bg);
        LinearLayout ll_nav_trip = (LinearLayout) headerView.findViewById(R.id.ll_nav_trip);
        LinearLayout ll_nav_money = (LinearLayout) headerView.findViewById(R.id.ll_nav_money);
        LinearLayout ll_nav_message = (LinearLayout) headerView.findViewById(R.id.ll_nav_message);
        LinearLayout ll_nav_guide = (LinearLayout) headerView.findViewById(R.id.ll_nav_guide);
        LinearLayout ll_nav_setting = (LinearLayout) headerView.findViewById(R.id.ll_nav_setting);
        rl_header_bg.setOnClickListener(this);
        ll_nav_trip.setOnClickListener(this);
        ll_nav_money.setOnClickListener(this);
        ll_nav_message.setOnClickListener(this);
        ll_nav_guide.setOnClickListener(this);
        ll_nav_setting.setOnClickListener(this);
        llTitleMenu.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, DestinationActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 初始化地图控制器对象
     */
    private void initAMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mRouteSearch = new RouteSearch(this);
            mRouteSearch.setRouteSearchListener(this);
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.getUiSettings().setGestureScaleByMapCenter(true);
//            aMap.getUiSettings().setScrollGesturesEnabled(false);
            aMap.setOnMapTouchListener(this);
            aMap.setOnMapLoadedListener(this);
            aMap.setOnCameraChangeListener(this);
            aMap.setOnMapClickListener(this);
            // 绑定 Marker 被点击事件
            aMap.setOnMarkerClickListener(markerClickListener);
            aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        }
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        mLocationTask = LocationTask.getInstance(getApplicationContext());
        mLocationTask.setOnLocationGetListener(this);
        mRegeocodeTask = new RegeocodeTask(getApplicationContext());
    }

    // 定义 Marker 点击事件监听
    AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {

        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        @Override
        public boolean onMarkerClick(final Marker marker) {
            Log.e(TAG, "点击的Marker");
            Log.e(TAG, marker.getPosition() + "");
            isClickIdentification = true;
            if (tempMark != null) {
                tempMark.setIcon(smallIdentificationBitmap);//点击时的图标
                walkRouteOverlay.removeFromMap();
                tempMark = null;
            }
            startAnim(marker);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                        tempMark = marker;
                        Log.e(TAG, mPositionMark.getPosition().latitude + "===" + mPositionMark.getPosition().longitude);
                        mStartPoint = new LatLonPoint(mRecordPositon.latitude, mRecordPositon.longitude);
                        mPositionMark.setPosition(mRecordPositon);
                        mEndPoint = new LatLonPoint(marker.getPosition().latitude, marker.getPosition().longitude);
//                        ArrayList<BitmapDescriptor> icons = marker.getIcons();

                        if (Utils.bitmapDescriptor.equals(BitmapDescriptorFactory
                                .fromResource(R.drawable.stable_cluster_marker_one_normal))) {

                            marker.setIcon(bigIdentificationBitmap);
                        } else if (Utils.bitmapDescriptor.equals(BitmapDescriptorFactory
                                .fromResource(R.drawable.marker_red_package))) {
                            marker.setIcon(bigredpacageBitmap);
                        }
                        marker.setPosition(marker.getPosition());
                        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);//出行路线规划
//                        Intent intent = new Intent(MainActivity.this, RouteActivity.class);
//                        intent.putExtra("start_lat", mPositionMark.getPosition().latitude);
//                        intent.putExtra("start_lng", mPositionMark.getPosition().longitude);
//                        intent.putExtra("end_lat", marker.getPosition().latitude);
//                        intent.putExtra("end_lng", marker.getPosition().longitude);
//                        startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return true;
        }
    };

    private void startAnim(Marker marker) {
        ScaleAnimation anim = new ScaleAnimation(1.0f, 1.3f, 1.0f, 1.3f);
        anim.setDuration(300);
        marker.setAnimation(anim);
        marker.startAnimation();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除蓝牙绑定
        try {
            BluetoothAutoConnectUtils.removeBond(_device);
            Log.e("removeBond", "onDestroy-->removeBond");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        Utils.removeMarkers();
        mMapView.onDestroy();
        mLocationTask.onDestroy();
        RouteTask.getInstance(getApplicationContext()).removeRouteCalculateListener(this);
        //解除蓝牙绑定

        //关闭蓝牙连接
        if (_socket != null)  //关闭连接socket
            try {
                _socket.close();
            } catch (IOException e) {
            }
//                    	_bluetooth.disable();  //关闭蓝牙服务

        // 注销广播
        unregisterReceiver(broadcastReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        if (mInitialMark != null) {
            mInitialMark.setToTop();
        }
        if (mPositionMark != null) {
            mPositionMark.setToTop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_title_menu:// 开启菜单
                drawerLayout.openDrawer(GravityCompat.START);
                // 关闭
//                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.rl_header_bg:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PersonalInformationActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_trip:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyTripActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_money:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyWalletActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_message:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyMessageActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_guide:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UserKnowActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_setting:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoginActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.iv_refresh:
                clickRefresh();
                break;
            case R.id.iv_scan_code:
                //TODO 点击扫码

//                                        BluetoothReceiver.BLUETOOTH_NAME="HC-05";
//                                        BluetoothReceiver.BLUETOOTH_PIN="1234";
//                BluetoothConnect();
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * description:二维码扫描回调
         * auther：luojie
         * time：2017/9/12 13:40
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);//取到扫描结果
                    String[] datas = result.split("&");
                    if (datas.length < 1) {
                        Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
                    } else {
                        // TODO: 2017/9/12 蓝牙解析回调
                        String ping = datas[0].split("=")[1];
                        String name = datas[1].split("=")[1];
                        BluetoothReceiver.BLUETOOTH_NAME = name;
                        BluetoothReceiver.BLUETOOTH_PIN = ping;
//                                                            Toast.makeText(this, "ping："+ping+"\nname："+name, Toast.LENGTH_LONG).show();
                        //蓝牙连接
                        BluetoothConnect();
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * description:通知设备进行蓝牙连接
     * auther：luojie
     * time：2017/9/12 16:35
     */
    private void BluetoothConnect() {
        if (_bluetooth.isEnabled() == false) {  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            _bluetooth.enable();
            return;
        }
        if (_socket == null) {
            //触发系统广播ACTION_FOUND
            _bluetooth.startDiscovery();
        } else {
            //关闭连接socket
            try {
                is.close();
                _socket.close();
                _socket = null;
                bRun = false;
//                                        mButton.setText("连接");
                BluetoothAutoConnectUtils.removeBond(_device);
                Log.e("removeBond", "removeBond");
            } catch (IOException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    //注册蓝牙连接反馈的广播
    private void initbroadcast() {
        // 动态注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATEUI);
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    /**
     * description:蓝牙连接成功，进行数据更新
     * auther：luojie
     * time：2017/9/13 10:46
     */
    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 响应返回结果
            // MAC地址，由DeviceListActivity设置返回
            String address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
            // 得到蓝牙设备句柄
            _device = _bluetooth.getRemoteDevice(address);

            // 用服务号得到socket
            try {
                _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
            }
            try {
                _socket.connect();
                Toast.makeText(context, "连接" + _device.getName() + "成功！", Toast.LENGTH_SHORT).show();
//                                        mButton.setText("断开");
            } catch (IOException e) {
                try {
                    Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
                    _socket.close();
                    _socket = null;
                } catch (IOException ee) {
                    Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            //打开接收线程
            try {
                is = _socket.getInputStream();   //得到蓝牙数据输入流
            } catch (IOException e) {
                Toast.makeText(context, "接收数据失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (bThread == false) {
                ReadThread.start();
                bThread = true;
            } else {
                bRun = true;
            }
        }

    }


    //蓝牙数据接收线程
    Thread ReadThread = new Thread() {

        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            String message1 = "";
            //接收线程
            while (true) {
                try {
                    num = is.read(buffer);//读入数据
                    n = 0;
                    String s0 = new String(buffer, 0, num);
                    fmsg += s0;    //保存收到数据
                    for (i = 0; i < num; i++) {
                        if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
                            buffer_new[n] = 0x0a;
                            i++;
                        } else {
                            buffer_new[n] = buffer[i];
                        }
                        n++;
                    }
                    // TODO: 2017/9/13 CO2 数据获取
                    co2_data = new String(buffer_new, 0, n - 1);

                    Log.i("Co2", "浓度：-->" + co2_data);
                    //延迟1s
                    Thread.sleep(1000);
//                                                  String[] split = s.split("\n");
//                                                  if (split!=null) {
//                                                            System.out.println("Co2浓度：-->"+split[0]);
//                                                  }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.e(TAG, "onCameraChange" + cameraPosition.target);
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.e(TAG, "onCameraChangeFinish" + cameraPosition.target);
        if (!isClickIdentification) {
            mRecordPositon = cameraPosition.target;
        }
        mStartPosition = cameraPosition.target;
        mRegeocodeTask.setOnLocationGetListener(this);
        mRegeocodeTask
                .search(mStartPosition.latitude, mStartPosition.longitude);
//        Utils.removeMarkers();
        if (mIsFirst) {
            // TODO: 2017/9/14 模拟加车  模拟加红包
            Utils.addEmulateData(aMap, mStartPosition);
            iv_refresh.setVisibility(View.VISIBLE);
            iv_scan_code.setVisibility(View.VISIBLE);
            createInitialPosition(cameraPosition.target.latitude, cameraPosition.target.longitude);
            createMovingPosition();
            mIsFirst = false;
        }
        if (mInitialMark != null) {
            mInitialMark.setToTop();
        }
        if (mPositionMark != null) {
            mPositionMark.setToTop();
            if (!isClickIdentification) {
                animMarker();
            }
        }
    }


    /**
     * 地图加载完成
     */
    @Override
    public void onMapLoaded() {
        mLocationTask.startLocate();
    }

    /**
     * 创建初始位置图标
     */
    private void createInitialPosition(double lat, double lng) {
        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.setFlat(true);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(new LatLng(lat, lng));
        markerOptions.icon(initBitmap);
        mInitialMark = aMap.addMarker(markerOptions);
        mInitialMark.setClickable(false);
    }

    /**
     * 创建移动位置图标
     */
    private void createMovingPosition() {
        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.setFlat(true);
//        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(new LatLng(0, 0));
        markerOptions.icon(moveBitmap);
        mPositionMark = aMap.addMarker(markerOptions);
        mPositionMark.setPositionByPixels(mMapView.getWidth() / 2,
                mMapView.getHeight() / 2);
        mPositionMark.setClickable(false);
    }

    //一秒定位一次，获取到所有位置信息
    @Override
    public void onLocationGet(PositionEntity entity) {
        // todo 这里在网络定位时可以减少一个逆地理编码
        Log.e("onLocationGet", "onLocationGet" + entity.address);
        RouteTask.getInstance(getApplicationContext()).setStartPoint(entity);
        mStartPosition = new LatLng(entity.latitue, entity.longitude);
        if (mIsFirstShow) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    mStartPosition, 17);
            aMap.animateCamera(cameraUpate);
            mIsFirstShow = false;
        }
        mInitialMark.setPosition(mStartPosition);
        initLocation = mStartPosition;
        Log.e("onLocationGet", "onLocationGet" + mStartPosition);
        //如果环境监测数据不为空，则开始填充数据
        if (co2_data != null) {
            //封装环境数据信息
            EnvData envData = new EnvData();
            //获取当前时间
            Date curDate = new Date(System.currentTimeMillis());
            envData.setE_latitfude(entity.latitue + "");
            envData.setE_longitude(entity.longitude + "");
            envData.setE_city(entity.city);
            envData.setE_address(entity.address);
            envData.setE_co2(Integer.parseInt(co2_data));
            envData.setE_time(formatter.format(curDate));
            System.out.println(envData.getE_latitfude());
            System.out.println(envData.getE_longitude());
            System.out.println(envData.getE_time());
            System.out.println(envData.getE_co2());
            System.out.println(envData.getE_city());
            System.out.println(envData.getE_address());
            //填充数据到集合
            envDatas.add(envData);
        }
        //如果缓存数据已经有100条
        if (envDatas.size() == 100 && isUpload == false) {
            isUpload = true;
            /**
             * description:传数据到服务器
             * auther：xuewenliao
             * time：2017/9/13 21:07
             */
            Log.i("server", "come");
            Thread loginThread = new Thread(new SendDataThread());
            loginThread.start();
            Log.i("server", "start");

        }

    }

    class SendDataThread implements Runnable {

        @Override
        public void run() {
            Log.i("server", "run");
            Gson gson = new Gson();
            String sendData = gson.toJson(envDatas);
            RequestParams params = new RequestParams("http://39.108.151.208:9030/sharebike/evn_data/");
            params.addHeader("Content-type", "application/json");
            params.setCharset("UTF-8");
            params.setAsJsonContent(true);
            params.setBodyContent(sendData);

            Log.i("server", "run_SUCCESS");
            x.http().post(params, callback);
        }
    }

    private Callback.CommonCallback<String> callback = new Callback.CommonCallback<String>() {
        @Override
        public void onSuccess(String result) {
            Log.i("server", "REGISTER_SUCCESS");
            envDatas.clear();
            Log.i("server", "clear");
            isUpload = false;
            System.out.print(1);

            //接收数据
//            String jsonBack = result;
//            EnvData data = new Gson().fromJson(jsonBack,EnvData.class);
//            Log.i("data",data.toString());

        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            Log.i("server", "CONNECT_FAIL");
        }

        @Override
        public void onCancelled(CancelledException cex) {
            Log.i("server", "onCancelled");
        }

        @Override
        public void onFinished() {
            Log.i("server", "onFinished");
        }
    };


    @Override
    public void onRegecodeGet(PositionEntity entity) {
        Log.e(TAG, "onRegecodeGet" + entity.address);
        entity.latitue = mStartPosition.latitude;
        entity.longitude = mStartPosition.longitude;
        RouteTask.getInstance(getApplicationContext()).setStartPoint(entity);
        RouteTask.getInstance(getApplicationContext()).search();
        Log.e(TAG, "onRegecodeGet" + mStartPosition);
    }

    @Override
    public void onRouteCalculate(float cost, float distance, int duration) {
        Log.e(TAG, "cost" + cost + "---" + "distance" + distance + "---" + "duration" + duration);
        PositionEntity endPoint = RouteTask.getInstance(getApplicationContext()).getEndPoint();
        mRecordPositon = new LatLng(endPoint.latitue, endPoint.longitude);
        clickMap();
        RouteTask.getInstance(getApplicationContext()).setEndPoint(null);
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (motionEvent.getPointerCount() >= 2) {
            aMap.getUiSettings().setScrollGesturesEnabled(false);
        } else {
            aMap.getUiSettings().setScrollGesturesEnabled(true);
        }
    }

    private void animMarker() {
        if (animator != null) {
            animator.start();
            return;
        }
        animator = ValueAnimator.ofFloat(mMapView.getHeight() / 2, mMapView.getHeight() / 2 - 30);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(150);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mPositionMark.setPositionByPixels(mMapView.getWidth() / 2, Math.round(value));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPositionMark.setIcon(moveBitmap);
            }
        });
        animator.start();
    }

    private void endAnim() {
        if (animator != null && animator.isRunning())
            animator.end();
    }


    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        LoadDialog.getInstance().dismiss();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    time = AMapUtil.getFriendlyTimeArray(dur);
                    distance = AMapUtil.getFriendlyLength(dis);
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    tempMark.setTitle(des);
                    tempMark.showInfoWindow();
                    Log.e(TAG, des);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(this, R.string.no_result);
                }
            } else {
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(this, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(this, "终点未设置");
        }
        showDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }

    private void showDialog() {
        LoadDialog loadDialog = LoadDialog.getInstance();
        loadDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.load_dialog);
        LoadDialog.getInstance().show(getSupportFragmentManager(), "");
    }

    @Override
    public void onMapClick(LatLng latLng) {
        clickMap();
    }

    private void clickRefresh() {
        clickInitInfo();
        if (initLocation != null) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    initLocation, 17f);
            aMap.animateCamera(cameraUpate);
        }
    }

    private void clickMap() {
        clickInitInfo();
        if (mRecordPositon != null) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    mRecordPositon, 17f);
            aMap.animateCamera(cameraUpate);
        }
    }

    private void clickInitInfo() {
        isClickIdentification = false;
        if (null != tempMark) {
            tempMark.setIcon(smallIdentificationBitmap);
            tempMark.hideInfoWindow();
            tempMark = null;
        }
        if (null != walkRouteOverlay) {
            walkRouteOverlay.removeFromMap();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Log.e(TAG, "getInfoWindow");
        View infoWindow = getLayoutInflater().inflate(
                R.layout.info_window, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        TextView tv_time_info = (TextView) view.findViewById(R.id.tv_time_info);
        TextView tv_distance = (TextView) view.findViewById(R.id.tv_distance);
        tv_time.setText(time[0]);
        tv_time_info.setText(time[1]);
        tv_distance.setText(distance);
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.e(TAG, "getInfoContents");
        return null;
    }


}