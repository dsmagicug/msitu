package com.dsmagic.kibira

import com.google.android.gms.maps.LocationSource

class RtkLocationSource : LocationSource {

    override fun activate(p0: LocationSource.OnLocationChangedListener) {
       NmeaReader.listener = p0
    }

    override fun deactivate() {
       // TODO("Not yet implemented")
    }

}