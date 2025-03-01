package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInfo
import app.simple.inure.apk.utils.PackageUtils.isAppHidden
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.isSystemApp
import app.simple.inure.apk.utils.PackageUtils.isUpdateInstalled
import app.simple.inure.apk.utils.PackageUtils.isUserApp
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.TrackerUtils.getTrackerSignatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInfoMenuViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val menuItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadMetaOptions()
        }
    }

    private val menuOptions: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadActionOptions()
        }
    }

    private val miscellaneousItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadMiscellaneousItems()
        }
    }

    private val trackers: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            loadTrackers()
        }
    }

    fun getComponentsOptions(): LiveData<List<Pair<Int, Int>>> {
        return menuItems
    }

    fun getActionsOptions(): LiveData<List<Pair<Int, Int>>> {
        return menuOptions
    }

    fun getMiscellaneousItems(): LiveData<List<Pair<Int, Int>>> {
        return miscellaneousItems
    }

    fun getTrackers(): LiveData<Int> {
        return trackers
    }

    fun loadActionOptions() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<Pair<Int, Int>>()

            if (packageManager.isPackageInstalled(packageInfo.packageName)) {
                warning.postValue(context.getString(R.string.app_not_installed, packageInfo.packageName))

                if (ConfigurationPreferences.isUsingRoot()) {
                    list.rootMenu()
                } else if (ConfigurationPreferences.isUsingShizuku()) {
                    list.shizukuMenu()
                } else {
                    list.normalMenu()
                }
            } else {
                if (packageInfo.applicationInfo.sourceDir.toFile().exists()) {
                    list.add(Pair(R.drawable.ic_send, R.string.send))

                    if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0) {
                        list.add(Pair(R.drawable.ic_publish, R.string.install))
                    } else {
                        if (ConfigurationPreferences.isUsingShizuku() || ConfigurationPreferences.isUsingRoot()) {
                            list.add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
                        }
                    }
                }
            }

            if (isNotThisApp().invert()) {
                list.add(Pair(R.drawable.ic_change_history, R.string.change_logs))
                list.add(Pair(R.drawable.ic_credits, R.string.credits))
                list.add(Pair(R.drawable.ic_translate, R.string.translate))
            }

            // Check if app has settings activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.queryIntentActivities(Intent().apply {
                    action = Intent.ACTION_APPLICATION_PREFERENCES
                }, 0).let { resolveInfos ->
                    resolveInfos.forEach {
                        if (it.activityInfo.packageName == packageInfo.packageName) {
                            list.add(Pair(R.drawable.ic_settings, R.string.preferences))
                        }
                    }
                }
            }

            menuOptions.postValue(list)
        }
    }

    fun loadMetaOptions() {
        viewModelScope.launch(Dispatchers.Default) {

            val isInstalled = packageManager.isPackageInstalled(packageInfo.packageName)

            val list = mutableListOf<Pair<Int, Int>>()

            list.add(Pair(R.drawable.ic_permission, R.string.permissions))
            list.add(Pair(R.drawable.ic_activities, R.string.activities))
            list.add(Pair(R.drawable.ic_services, R.string.services))
            list.add(Pair(R.drawable.ic_certificate, R.string.certificate))
            list.add(Pair(R.drawable.ic_resources, R.string.resources))
            list.add(Pair(R.drawable.ic_receivers, R.string.receivers))
            list.add(Pair(R.drawable.ic_provider, R.string.providers))
            list.add(Pair(R.drawable.ic_android, R.string.manifest))
            list.add(Pair(R.drawable.ic_anchor, R.string.uses_feature))
            list.add(Pair(R.drawable.ic_graphics, R.string.graphics))
            list.add(Pair(R.drawable.ic_extras, R.string.extras))
            list.add(Pair(R.drawable.ic_shared_libs, R.string.shared_libs))
            if (isInstalled) {
                list.add(Pair(R.drawable.ic_code, R.string.dex_classes))
            }
            list.add(Pair(R.drawable.ic_radiation_nuclear, R.string.trackers))

            if (isInstalled) {
                if (ConfigurationPreferences.isUsingRoot()) {
                    list.add(Pair(R.drawable.ic_power_off, R.string.boot))
                }
            }

            if (ConfigurationPreferences.isUsingRoot() && isInstalled) {
                list.add(1, Pair(R.drawable.ic_rocket_launch, R.string.operations))
                list.add(Pair(R.drawable.sc_preferences, R.string.shared_prefs))
            }

            menuItems.postValue(list)
        }
    }

    fun loadMiscellaneousItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<Pair<Int, Int>>()

            list.add(Pair(R.drawable.ic_downloading, R.string.extract))
            list.add(Pair(R.drawable.ic_play_store, R.string.play_store))
            list.add(Pair(R.drawable.ic_fdroid, R.string.fdroid))
            list.add(Pair(R.drawable.ic_amazon, R.string.amazon))
            list.add(Pair(R.drawable.ic_galaxy_appstore, R.string.galaxy_store))

            miscellaneousItems.postValue(list)
        }
    }

    private fun ArrayList<Pair<Int, Int>>.rootMenu() {
        if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
            add(Pair(R.drawable.ic_launch, R.string.launch))
        }

        add(Pair(R.drawable.ic_send, R.string.send))

        if (isNotThisApp()) {
            add(Pair(R.drawable.ic_delete, R.string.uninstall))

            if (packageInfo.isUserApp()) {
                add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
            }

            if (packageInfo.isSystemApp()) {
                if (packageInfo.isUpdateInstalled()) {
                    add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                }
            }

            if (packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                add(Pair(R.drawable.ic_disable, R.string.disable))
            } else {
                add(Pair(R.drawable.ic_check, R.string.enable))
            }

            if (DevelopmentPreferences.get(DevelopmentPreferences.enableHiddenApps)) {
                if (packageManager.isAppHidden(packageInfo.packageName)) {
                    add(Pair(R.drawable.ic_visibility, R.string.visible))
                } else {
                    add(Pair(R.drawable.ic_visibility_off, R.string.hidden))
                }
            }

            add(Pair(R.drawable.ic_close, R.string.force_stop))
            add(Pair(R.drawable.ic_delete_sweep, R.string.clear_data))
        }

        add(Pair(R.drawable.ic_broom, R.string.clear_cache))
        add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
    }

    private fun ArrayList<Pair<Int, Int>>.shizukuMenu() {
        if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
            add(Pair(R.drawable.ic_launch, R.string.launch))
        }

        add(Pair(R.drawable.ic_send, R.string.send))

        if (isNotThisApp()) {
            add(Pair(R.drawable.ic_delete, R.string.uninstall))

            if (packageInfo.isUserApp()) {
                add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
            }

            if (packageInfo.isSystemApp()) {
                if (packageInfo.isUpdateInstalled()) {
                    add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                }
            }

            if (packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                add(Pair(R.drawable.ic_disable, R.string.disable))
            } else {
                add(Pair(R.drawable.ic_check, R.string.enable))
            }

            if (DevelopmentPreferences.get(DevelopmentPreferences.enableHiddenApps)) {
                if (packageManager.isAppHidden(packageInfo.packageName)) {
                    add(Pair(R.drawable.ic_visibility, R.string.visible))
                } else {
                    add(Pair(R.drawable.ic_visibility_off, R.string.hidden))
                }
            }

            add(Pair(R.drawable.ic_close, R.string.force_stop))
            add(Pair(R.drawable.ic_delete_sweep, R.string.clear_data))
        }

        add(Pair(R.drawable.ic_broom, R.string.clear_cache))
        add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
    }

    private fun ArrayList<Pair<Int, Int>>.normalMenu() {
        if (packageInfo.isUserApp()) {
            if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
                add(Pair(R.drawable.ic_launch, R.string.launch))
            }

            add(Pair(R.drawable.ic_send, R.string.send))

            if (isNotThisApp()) {
                add(Pair(R.drawable.ic_delete, R.string.uninstall))
            }
        } else {
            if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName)) {
                add(Pair(R.drawable.ic_launch, R.string.launch))
            }

            add(Pair(R.drawable.ic_send, R.string.send))

            if (isNotThisApp()) {
                if (packageInfo.isUpdateInstalled()) {
                    add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                }
            }
        }

        add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
    }

    private fun loadTrackers() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val packageInfo: PackageInfo = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.MATCH_DISABLED_COMPONENTS)
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.GET_DISABLED_COMPONENTS)
                    }
                } catch (e: NameNotFoundException) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.MATCH_DISABLED_COMPONENTS)!!
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.GET_DISABLED_COMPONENTS)!!
                    }
                }

                val trackers = application.getTrackerSignatures()
                var count = 0

                if (packageInfo.activities != null) {
                    for (activity in packageInfo.activities) {
                        for (tracker in trackers) {
                            if (activity.name.lowercase().contains(tracker.lowercase())) {
                                count++
                                break
                            }
                        }
                    }
                }

                if (packageInfo.services != null) {
                    for (service in packageInfo.services) {
                        for (tracker in trackers) {
                            if (service.name.lowercase().contains(tracker.lowercase())) {
                                count++
                                break
                            }
                        }
                    }
                }

                if (packageInfo.receivers != null) {
                    for (receiver in packageInfo.receivers) {
                        for (tracker in trackers) {
                            if (receiver.name.lowercase().contains(tracker.lowercase())) {
                                count++
                                break
                            }
                        }
                    }
                }

                this@AppInfoMenuViewModel.trackers.postValue(count)
            }.getOrElse {
                this@AppInfoMenuViewModel.trackers.postValue(0)
            }
        }
    }

    private fun isNotThisApp(): Boolean {
        return packageInfo.packageName != application.packageName
    }

    fun unsetUpdateFlag() {
        FlagUtils.unsetFlag(packageInfo.applicationInfo.flags, ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
    }
}