package top.niunaijun.blackboxa.view.fake;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import top.niunaijun.blackbox.entity.location.BLocation;
import top.niunaijun.blackboxa.databinding.ActivityOsmdroidBinding;

import top.niunaijun.blackboxa.util.ToastEx;

public class FollowMyLocationOverlay extends AppCompatActivity {
    private static final String TAG = "FollowMyLocationOverlay";

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private ActivityOsmdroidBinding binding;

    private GeoPoint startPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        binding = ActivityOsmdroidBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BLocation location = getIntent().getParcelableExtra("location");
        if (location == null) {
            startPoint = new GeoPoint(30.2736, 120.1563);
        } else {
            startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        }

        final Marker startMarker = new Marker(binding.map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        binding.map.getOverlays().add(startMarker);

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                startPoint = p;
                startMarker.setPosition(p);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                binding.map.getOverlays().add(startMarker);
                Toast.makeText(FollowMyLocationOverlay.this, p.getLatitude() + " - " + p.getLongitude(), Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        binding.map.getOverlays().add(new MapEventsOverlay(mReceive));

        IMapController mapController = binding.map.getController();
        mapController.setZoom(12.5);
        mapController.setCenter(startPoint);
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishWithResult(startPoint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    private void finishWithResult(GeoPoint geoPoint) {
        getIntent().putExtra("latitude", geoPoint.getLatitude());
        getIntent().putExtra("longitude", geoPoint.getLongitude());
        setResult(Activity.RESULT_OK, getIntent());
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View decor = getWindow().peekDecorView();
        if (decor != null && imm != null) {
            imm.hideSoftInputFromWindow(decor.getWindowToken(), 0);
        }
        finish();
    }
}
