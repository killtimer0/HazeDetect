package com.example.hazedetect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hazedetect.databinding.ActivityPositioningBinding;

public class PositioningActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private ActivityPositioningBinding binding;

    private void ensureLocationPermissionsAndGet() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d("Location", "ensureLocationPermissions: isProviderEnabled -- " + gpsEnabled);
        if (!gpsEnabled) {
            onAcquireLocationFail("请打开定位开关来获取位置信息");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean locGranted = PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            Log.d("Location", "ensureLocationPermissions: checkSelfPermission -- " + locGranted);
            if (locGranted) {
                tryGetLocation();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void tryGetLocation() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (null != location) {
            WeatherDataManager.getInstance(null).queryLocation(location.getLongitude(), location.getLatitude(), result -> {
                if (result.ok) {
                    onAcquireLocationOk(result.data);
                } else {
                    onAcquireLocationFail(result.error.getLocalizedMessage());
                }
            });
        } else {
            onAcquireLocationFail("无法获取定位");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (REQUEST_LOCATION_PERMISSION == requestCode) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[1]) {
//                Log.d("Location", "onRequestPermissionsResult: grantResults -- GRANTED");
                tryGetLocation();
            } else {
//                Log.d("Location", "onRequestPermissionsResult: grantResults -- DENIED");
                onAcquireLocationFail("无法获取定位权限，请在设置中手动开启");
            }
        }
    }

    private void onAcquireLocationFail(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void onAcquireLocationOk(LocationInfo locationInfo) {
        setResult(RESULT_OK, getIntent().putExtra("location", locationInfo));
//        Toast.makeText(this, locationInfo.toString(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private CityListAdapter mCityListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPositioningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 获取实时定位
        binding.fab.setOnClickListener(view -> ensureLocationPermissionsAndGet());

        // 手动输入城市
        Handler handler = new Handler();
        Runnable doQuery = () -> {
            Editable editable = binding.content.editCity.getText();
            if (null != editable) {
                String query = editable.toString().strip();
                if (!query.isEmpty()) {
                    WeatherDataManager.getInstance(this).queryCities(query, result -> {
                        if (result.ok && result.data.length > 0) {
                            binding.content.textViewEmpty.setVisibility(View.GONE);
                            binding.content.listCity.setVisibility(View.VISIBLE);
                            mCityListAdapter.updateCityList(result.data);
                        } else {
                            binding.content.textViewEmpty.setVisibility(View.VISIBLE);
                            binding.content.listCity.setVisibility(View.GONE);
                            binding.content.textViewEmpty.setText(result.ok ? "未查询到相关数据" : result.error.getLocalizedMessage());
                            mCityListAdapter.updateCityList(null);
                        }
                    });
                }
            }
        };
        binding.content.editCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(doQuery);
                handler.postDelayed(doQuery, 500);
            }
        });
        mCityListAdapter = new CityListAdapter();
        binding.content.listCity.setAdapter(mCityListAdapter);
        mCityListAdapter.setLocationSelectedListener(this::onAcquireLocationOk);
    }
}