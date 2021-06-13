package com.udacity.project4.locationreminders.geofence

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.udacity.project4.R
import java.util.concurrent.TimeUnit

/**
 * Created by Daniel
 * */
internal object GeoFenceConstants{
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val ACTION_GEOFENCE_EVENT =
            "ACTION_GEOFENCE_EVENT"
   val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
}



/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}