package com.niu.csie.edu.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;

import java.util.ArrayList;

public class Activity10_ChooseLocation extends AppCompatActivity {

    /**Element*/
    private MapView mapView;

    /**Variable*/
    private ArrayList<Double> Location = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化地圖庫配置
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity10_choose_location);
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // 設定地圖來源，這裡使用 OpenStreetMap Mapnik 風格

        // 設定初始地圖中心點和縮放級別
        mapView.getController().setZoom(17.9);
        GeoPoint startPoint = new GeoPoint(24.746313, 121.74599); // 初始位置
        mapView.getController().setCenter(startPoint);

        // 可選：啟用地圖縮放和滑動
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        showMessage(getResources().getString(R.string.Tips_Choose_Location));

        // 新增地圖點擊事件監聽器
        mapView.getOverlays().add(new MapEventsOverlay(this, new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                // 處理地圖點擊事件
                // 在這裡處理選擇位置的邏輯，新增標記
                showMessage(getResources().getString(R.string.Latitude) + String.valueOf(geoPoint).split(",")[0] + "\n" + getResources().getString(R.string.Longitude) + String.valueOf(geoPoint).split(",")[1]);
                // 跳轉至課程
                Location.add(Double.valueOf(String.valueOf(geoPoint).split(",")[0]));
                Location.add(Double.valueOf(String.valueOf(geoPoint).split(",")[1]));
                Intent page = new Intent(Activity10_ChooseLocation.this, Activity10_Zuvio.class);
                page.putExtra("Location_Info", Location);
                startActivity(page);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showMessage(getResources().getString(R.string.Cancel_Choose));
        Intent page = new Intent(Activity10_ChooseLocation.this, Activity10_Zuvio.class);
        Location.add(24.7454820); //緯度 //格致大樓預設
        Location.add(121.7450088); //經度 //格致大樓預設
        page.putExtra("Location_Info", Location);
        startActivity(page);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume(); // 處理地圖生命週期
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause(); // 處理地圖生命週期
    }

    private void showMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}