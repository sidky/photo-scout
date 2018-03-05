package com.github.sidky.photoscout.details

import android.os.Parcel
import android.os.Parcelable

data class PhotoDetails(val id: Long,
                        val title: String,
                        val url: String) : Parcelable {

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(id)
        dest?.writeString(title)
        dest?.writeString(url)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PhotoDetails> {
        override fun createFromParcel(parcel: Parcel): PhotoDetails {
            val id = parcel.readLong()
            val title = parcel.readString()
            val url = parcel.readString()

            return PhotoDetails(id = id, title = title, url = url)
        }

        override fun newArray(size: Int): Array<PhotoDetails?> {
            return arrayOfNulls(size)
        }
    }
}