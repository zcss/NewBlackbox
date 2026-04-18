package top.niunaijun.blackboxa.view.setting;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.app.AppManager;
import top.niunaijun.blackboxa.view.gms.GmsManagerActivity;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.setting, rootKey);

        initGms();

        invalidHideState(() -> {
            Preference p = findPreference("root_hide");
            boolean hideRoot = AppManager.getMBlackBoxLoader().hideRoot();
            if (p != null) p.setDefaultValue(hideRoot);
            return p;
        });

        invalidHideState(() -> {
            Preference p = findPreference("daemon_enable");
            boolean v = AppManager.getMBlackBoxLoader().daemonEnable();
            if (p != null) p.setDefaultValue(v);
            return p;
        });

        invalidHideState(() -> {
            Preference p = findPreference("use_vpn_network");
            boolean v = AppManager.getMBlackBoxLoader().useVpnNetwork();
            if (p != null) p.setDefaultValue(v);
            return p;
        });

        invalidHideState(() -> {
            Preference p = findPreference("disable_flag_secure");
            boolean v = AppManager.getMBlackBoxLoader().disableFlagSecure();
            if (p != null) p.setDefaultValue(v);
            return p;
        });

        initSendLogs();
    }

    private void initGms() {
        Preference gms = findPreference("gms_manager");
        if (gms == null) return;
        if (BlackBoxCore.get().isSupportGms()) {
            gms.setOnPreferenceClickListener(preference -> {
                GmsManagerActivity.start(requireContext());
                return true;
            });
        } else {
            gms.setSummary(getString(R.string.no_gms));
            gms.setEnabled(false);
        }
    }

    private interface PrefBlock { Preference get(); }

    private void invalidHideState(PrefBlock block) {
        Preference pref = block.get();
        if (pref == null) return;
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean tmpHide = Boolean.TRUE.equals(newValue);
            switch (preference.getKey()) {
                case "root_hide":
                    AppManager.getMBlackBoxLoader().invalidHideRoot(tmpHide);
                    break;
                case "daemon_enable":
                    AppManager.getMBlackBoxLoader().invalidDaemonEnable(tmpHide);
                    break;
                case "use_vpn_network":
                    AppManager.getMBlackBoxLoader().invalidUseVpnNetwork(tmpHide);
                    break;
                case "disable_flag_secure":
                    AppManager.getMBlackBoxLoader().invalidDisableFlagSecure(tmpHide);
                    break;
            }
            Toast.makeText(requireContext(),R.string.restart_module,Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void initSendLogs() {
        Preference sendLogs = findPreference("send_logs");
        if (sendLogs == null) return;
        sendLogs.setOnPreferenceClickListener(pref -> {
            pref.setEnabled(false);
            BlackBoxCore.get().sendLogs(
                    "Manual Log Upload from Settings",
                    true,
                    new BlackBoxCore.LogSendListener() {
                        @Override
                        public void onSuccess() {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> pref.setEnabled(true));
                        }
                        @Override
                        public void onFailure(@Nullable String error) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> pref.setEnabled(true));
                        }
                    }
            );
            Toast.makeText(requireContext(),"正在发送日志...（请查看通知了解状态）",Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
