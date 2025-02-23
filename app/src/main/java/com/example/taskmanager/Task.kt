package com.example.taskmanager

import android.os.Parcel
import android.os.Parcelable

enum class TaskStatus {
    PENDING,
    COMPLETED,
    OVERDUE,
    IN_PROGRESS
}

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val status: TaskStatus = TaskStatus.PENDING
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        TaskStatus.values()[parcel.readInt()]
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeInt(status.ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task = Task(parcel)
        override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
    }
} 