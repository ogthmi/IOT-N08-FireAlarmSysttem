package com.example.firealarm.data.repository

import android.util.Log
import com.example.firealarm.data.model.SensorDto
import com.example.firealarm.domain.model.Sensor
import com.example.firealarm.domain.repository.SensorRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

class SensorRepositoryImpl @Inject constructor(
    @Named("sensorRef") private val sensorRef: DatabaseReference
) : SensorRepository{
    private var listener: ValueEventListener? = null
    private val TAG = "Sensor"
    var lastSensor: Sensor? = null
    override fun getSensorData(): Flow<Sensor> = flow{
        while (true) {
            val snapshot = sensorRef.limitToLast(1).get().await()
            snapshot.children.lastOrNull()?.getValue(SensorDto::class.java)?.let {
                emit(it.toDomain())
            }
            delay(2000) // Lấy mỗi 5 giây
        }
    }.flowOn(Dispatchers.IO)
}