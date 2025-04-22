package com.ssquad.gps.camera.geotag.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.ssquad.gps.camera.geotag.domain.MapLocationRepository
import com.ssquad.gps.camera.geotag.utils.LocationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class MapLocationRepositoryImpl(private val context: Context) : MapLocationRepository {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override suspend fun getCurrentLocation(): LocationResult =
       withContext(Dispatchers.IO){
           suspendCancellableCoroutine { continuation ->
               try {
                   val locationRequest = LocationRequest.Builder(
                       Priority.PRIORITY_HIGH_ACCURACY,
                       0L
                     )
                       .setMinUpdateIntervalMillis(0L)
                       .setMaxUpdateDelayMillis(0L)
                       .setMaxUpdates(1)
                       .build()
                   val locationCallback = object : LocationCallback() {
                       override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                           val location = locationResult.lastLocation
                           fusedLocationClient.removeLocationUpdates(this)
                           if (location != null) {
                               continuation.resume(LocationResult.Success(location))
                           } else {
                               continuation.resume(LocationResult.Error("Không tìm thấy vị trí"))
                           }
                       }
                   }
                   fusedLocationClient.requestLocationUpdates(
                       locationRequest,
                       locationCallback,
                       Looper.getMainLooper()
                   )
                   continuation.invokeOnCancellation {
                       fusedLocationClient.removeLocationUpdates(locationCallback)
                   }
               } catch (e: SecurityException) {
                   continuation.resume(LocationResult.Error("Không có quyền truy cập vị trí:${e.message}"))
               } catch (e: Exception) {
                   continuation.resume(LocationResult.Error("Lỗi lấy vị trí:${e.message}"))
               }
           }
       }

    override suspend fun getAddressFromLocation(location: Location): LocationResult =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val geoCoder by lazy {
                    Geocoder(context, Locale.getDefault())
                }
                try {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        geoCoder.getFromLocation(location.latitude, location.longitude, 1){addresses->
                            if(addresses.isNotEmpty()){
                                val address = addresses[0]
                                val addressText = getFormatAddress(address)
                                continuation.resume(LocationResult.Address(addressText))
                            }else{
                                continuation.resume(LocationResult.Address("Không rõ địa chỉ"))
                            }
                        }
                    }else{
                        val addresses= geoCoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        if (addresses?.isNotEmpty() == true) {
                            val address = addresses[0]
                            val addressText = getFormatAddress(address)
                            continuation.resume(LocationResult.Address(addressText))
                        } else {
                            continuation.resume(LocationResult.Address("Không rõ địa chỉ"))
                        }
                    }
                }catch (e: Exception){
                    continuation.resume(LocationResult.Error("Lỗi lấy địa chỉ:${e.message}"))
                }
            }
        }
    private fun getFormatAddress(address: Address): String{
        val locality = address.locality ?: address.subAdminArea?:""
        val country = address.countryName?:""
        return when{
            locality.isNotEmpty() && country.isNotEmpty() -> "$locality, $country"
            locality.isNotEmpty() -> locality
            country.isNotEmpty() -> country
            else -> "Không tìm thấy địa chỉ"
        }
    }
}