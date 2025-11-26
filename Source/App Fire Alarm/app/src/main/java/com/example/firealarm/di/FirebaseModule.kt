package com.example.firealarm.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance();

    @Provides
    @Singleton
    @Named("sensorRef")
    fun provideSensorRef(db: FirebaseDatabase): DatabaseReference = db.getReference("sensor/data")

    @Provides
    @Singleton
    @Named("statusRef")
    fun provideStatusRef(db: FirebaseDatabase): DatabaseReference = db.getReference("status/data")
}