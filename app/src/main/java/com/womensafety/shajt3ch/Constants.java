package com.womensafety.shajt3ch;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

public class Constants {

  public static final Intent[] AUTO_START_INTENTS = {
    new Intent().setComponent(new ComponentName("com.samsung.android.lool",
      "com.samsung.android.sm.ui.battery.BatteryActivity")),
    new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
    new Intent().setComponent(new ComponentName("com.miui.securitycenter",
      "com.miui.permcenter.autostart.AutoStartManagementActivity")), new Intent().setComponent(
    new ComponentName("com.letv.android.letvsafe",
      "com.letv.android.letvsafe.AutobootManageActivity")), new Intent().setComponent(
    new ComponentName("com.huawei.systemmanager",
      "com.huawei.systemmanager.optimize.process.ProtectActivity")), new Intent().setComponent(
    new ComponentName("com.coloros.safecenter",
      "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
    new Intent().setComponent(new ComponentName("com.coloros.safecenter",
      "com.coloros.safecenter.startupapp.StartupAppListActivity")), new Intent().setComponent(
    new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
    new Intent().setComponent(new ComponentName("com.iqoo.secure",
      "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")), new Intent().setComponent(
    new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
    new Intent().setComponent(new ComponentName("com.vivo.permissionmanager",
      "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")), new Intent().setComponent(
    new ComponentName("com.asus.mobilemanager",
      "com.asus.mobilemanager.entry.FunctionActivity")).setData(
    Uri.parse("mobilemanager://function/entry/AutoStart"))
  };


}
