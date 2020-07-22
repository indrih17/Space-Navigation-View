package com.luseen.spacelib

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

/**
 * Информация об элементе space nav view. Не используется для центральной кнопки.
 */
@Parcelize
data class SpaceItem(
    val name: String,
    @DrawableRes val defaultIcon: Int,
    @DrawableRes val activeIcon: Int
) : Parcelable
