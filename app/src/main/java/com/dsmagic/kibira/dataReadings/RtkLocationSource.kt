package com.dsmagic.kibira.dataReadings

/*
 *  This file is part of Msitu.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>
 */

import android.location.Location
import com.google.android.gms.maps.LocationSource

interface LocationChanged {
    fun onLocationChanged(loc: Location, fix: LongLat.FixType)
}

class RtkLocationSource : LocationSource {
    var listener: LocationSource.OnLocationChangedListener? = null
    var locationChanged: LocationChanged? = null
    override fun activate(p0: LocationSource.OnLocationChangedListener) {
        listener = p0
    }

    override fun deactivate() {

    // deactivate()
    }

    fun setLocationChangedTrigger(locationChanged: LocationChanged) {
        this.locationChanged = locationChanged
    }

    fun postNewLocation(l: Location,fix: LongLat.FixType) {
        listener?.onLocationChanged(l)
        locationChanged?.onLocationChanged(l,fix) // trigger it...
    }
}