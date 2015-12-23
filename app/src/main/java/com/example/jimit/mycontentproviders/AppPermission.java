package com.example.jimit.mycontentproviders;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jimit on 23-12-2015.
 */
public class AppPermission {
    public static boolean isMarshmallowPlusDevice() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isPermissionRequestRequired(Activity activity, @NonNull String[] permissions, int requestCode) {
        if (isMarshmallowPlusDevice() && permissions.length > 0) {
            List<String> newPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (PackageManager.PERMISSION_GRANTED != activity.checkSelfPermission(permission)) {
                    newPermissionList.add(permission);
                }
            }
            if (newPermissionList.size() > 0) {
                activity.requestPermissions(newPermissionList.toArray(new String[newPermissionList.size()]), requestCode);
                return true;
            }
        }
        return false;
    }
}
