package com.example.locationapplication


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

fun EditText.requestFocusWithKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (!hasFocus()) {
        requestFocus()
    }

    post { imm.showSoftInput(this, InputMethodManager.SHOW_FORCED) }
}

fun hideKeyboard(context: Context, view: View) {
    val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.hideSoftInputFromWindow(view.windowToken, 0)
}

fun vectorToBitmap(resources: Resources, @DrawableRes id: Int): BitmapDescriptor {
    val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
    val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun showReminderInMap(context: Context,
                      map: GoogleMap,
                      reminder: Reminder) {
    if (reminder.latLng != null) {
        val latLng = reminder.latLng as LatLng
       // val vectorToBitmap = vectorToBitmap(context.resources, R.drawable.ic_twotone_location_on_48px)
        val marker = map.addMarker(MarkerOptions().position(latLng))
        marker.tag = reminder.id
        if (reminder.radius != null) {
            val radius = reminder.radius as Double
            map.addCircle(
                CircleOptions()
                    .center(reminder.latLng)
                    .radius(radius)
                    .strokeColor(ContextCompat.getColor(context, R.color.colorAccent)))
                    //.fillColor(ContextCompat.getColor(context, R.color.colorReminderFill)))
        }
    }
}

private val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, message: String, latLng: LatLng) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }

    val intent = MapsActivity.newIntent(context.applicationContext, latLng)

    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(MainActivity::class.java)
        .addNextIntent(intent)
    val notificationPendingIntent = stackBuilder
        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(message)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())