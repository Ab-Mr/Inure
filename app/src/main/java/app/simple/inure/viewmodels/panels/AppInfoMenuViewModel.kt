package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
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

    fun getComponentsOptions(): LiveData<List<Pair<Int, Int>>> {
        return menuItems
    }

    fun getActionsOptions(): LiveData<List<Pair<Int, Int>>> {
        return menuOptions
    }

    fun getMiscellaneousItems(): LiveData<List<Pair<Int, Int>>> {
        return miscellaneousItems
    }

    fun loadActionOptions() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<Pair<Int, Int>>()

            if (packageManager.isPackageInstalled(packageInfo.packageName)) {
                warning.postValue(context.getString(R.string.app_not_installed, packageInfo.packageName))

                if (ConfigurationPreferences.isUsingRoot()) {
                    if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_launch, R.string.launch))
                    }

                    list.add(Pair(R.drawable.ic_send, R.string.send))

                    if (isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_delete, R.string.uninstall))

                        if (packageInfo.isUserApp()) {
                            list.add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
                        }

                        if (packageInfo.isSystemApp()) {
                            if (packageInfo.isUpdateInstalled()) {
                                list.add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                            }
                        }

                        if (packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                            list.add(Pair(R.drawable.ic_disable, R.string.disable))
                        } else {
                            list.add(Pair(R.drawable.ic_check, R.string.enable))
                        }

                        if (DevelopmentPreferences.get(DevelopmentPreferences.enableHiddenApps)) {
                            if (packageManager.isAppHidden(packageInfo.packageName)) {
                                list.add(Pair(R.drawable.ic_visibility, R.string.visible))
                            } else {
                                list.add(Pair(R.drawable.ic_visibility_off, R.string.hidden))
                            }
                        }

                        list.add(Pair(R.drawable.ic_close, R.string.force_stop))
                        list.add(Pair(R.drawable.ic_delete_sweep, R.string.clear_data))
                    }

                    list.add(Pair(R.drawable.ic_broom, R.string.clear_cache))
                    list.add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
                } else if (ConfigurationPreferences.isUsingShizuku()) {
                    if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_launch, R.string.launch))
                    }

                    list.add(Pair(R.drawable.ic_send, R.string.send))

                    if (isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_delete, R.string.uninstall))

                        if (packageInfo.isUserApp()) {
                            list.add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
                        }

                        if (packageInfo.isSystemApp()) {
                            if (packageInfo.isUpdateInstalled()) {
                                list.add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                            }
                        }

                        if (packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                            list.add(Pair(R.drawable.ic_disable, R.string.disable))
                        } else {
                            list.add(Pair(R.drawable.ic_check, R.string.enable))
                        }

                        if (DevelopmentPreferences.get(DevelopmentPreferences.enableHiddenApps)) {
                            if (packageManager.isAppHidden(packageInfo.packageName)) {
                                list.add(Pair(R.drawable.ic_visibility, R.string.visible))
                            } else {
                                list.add(Pair(R.drawable.ic_visibility_off, R.string.hidden))
                            }
                        }

                        list.add(Pair(R.drawable.ic_close, R.string.force_stop))
                        list.add(Pair(R.drawable.ic_delete_sweep, R.string.clear_data))
                    }

                    list.add(Pair(R.drawable.ic_broom, R.string.clear_cache))
                    list.add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
                } else {
                    if (packageInfo.isUserApp()) {
                        if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
                            list.add(Pair(R.drawable.ic_launch, R.string.launch))
                        }

                        list.add(Pair(R.drawable.ic_send, R.string.send))

                        if (isNotThisApp()) {
                            list.add(Pair(R.drawable.ic_delete, R.string.uninstall))
                        }
                    } else {
                        if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName)) {
                            list.add(Pair(R.drawable.ic_launch, R.string.launch))
                        }

                        list.add(Pair(R.drawable.ic_send, R.string.send))

                        if (isNotThisApp()) {
                            if (packageInfo.isUpdateInstalled()) {
                                list.add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                            }
                        }
                    }

                    list.add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
                }
            } else {
                if (packageInfo.applicationInfo.sourceDir.toFile().exists()) {
                    list.add(Pair(R.drawable.ic_send, R.string.send))

                    if (ConfigurationPreferences.isUsingShizuku() || ConfigurationPreferences.isUsingRoot()) {
                        list.add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
                    }
                }
            }

            if (isNotThisApp().invert()) {
                list.add(Pair(R.drawable.ic_change_history, R.string.change_logs))
                list.add(Pair(R.drawable.ic_credits, R.string.credits))
                list.add(Pair(R.drawable.ic_translate, R.string.translate))
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
            list.add(Pair(R.drawable.ic_code, R.string.dex_classes))
            list.add(Pair(R.drawable.ic_radiation_nuclear, R.string.trackers))
            if (isInstalled) {
                list.add(Pair(R.drawable.ic_power_off, R.string.boot))
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
            list.add(Pair(R.drawable.ic_amazon, R.string.amazon))
            list.add(Pair(R.drawable.ic_fdroid, R.string.fdroid))

            miscellaneousItems.postValue(list)
        }
    }

    private fun isNotThisApp(): Boolean {
        return packageInfo.packageName != application.packageName
    }
}