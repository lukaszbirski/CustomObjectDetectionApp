package pl.birski.customobjectdetectionapp

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object PermissionUtil {
    fun hasMessagesPermission(context: Context) = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.CAMERA
    )

    fun returnPermissionsArray() = arrayOf(
        Manifest.permission.CAMERA
    )
}
