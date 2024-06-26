package com.conexentools.target_app_helpers

import androidx.test.uiautomator.UiDevice
import com.conexentools.DeviceManager
import com.conexentools.Utils

abstract class TargetAppHelper(
  val device: UiDevice,
  val name: String,

  val packageName: String,
  testedVersionCode: String,
  testedVersionName: String,
) {

  protected val dm: DeviceManager = DeviceManager(device)
  val installedVersion = Utils.getPackageVersion(packageName)
  val testedVersion = Pair(testedVersionCode.toLong(), testedVersionName)

  open fun launch(clearOutPreviousInstances: Boolean = true) = dm.launchPackage(
    packageName,
    clearOutPreviousInstances = clearOutPreviousInstances
  )

  fun printVersionInfo() = Utils.printPackageVersionInfo(
    name,
    installedVersion,
    testedVersion
  )

  fun checkVersionCompatibility() {
    if (installedVersion != null && installedVersion != testedVersion) {
      Utils.toast("Usted está usando una versión de $name diferente", waitForToastToHide = true)
      Utils.toast("a la usada para crear la prueba automatizada", waitForToastToHide = true)
    }
  }

  fun throwExceptionIfNotInstalled() {
    if (installedVersion != null)
      return
    Utils.toast("Por favor instale $name", vibrate = true, waitForToastToHide = true)
    assert(false)
  }
}