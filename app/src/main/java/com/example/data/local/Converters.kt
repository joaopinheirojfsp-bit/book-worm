package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ReadingStatus

class Converters {
    @TypeConverter
    fun fromReadingStatus(status: ReadingStatus?): String {
        return status?.name ?: ReadingStatus.QUERO_LER.name
    }

    @TypeConverter
    fun toReadingStatus(value: String?): ReadingStatus {
        return try {
            if (value != null) ReadingStatus.valueOf(value) else ReadingStatus.QUERO_LER
        } catch (e: Exception) {
            ReadingStatus.QUERO_LER
        }
    }
}
