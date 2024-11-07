package com.rtnmsitu.maths
/*

 *  This file is part of Msitu.

 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions

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

import kotlin.math.*

/**
 * Class representing UTM-coordinates. Based on code from stack overflow.
 * @see [Stack Overflow](https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm)
 *
 * @see [Wikipedia-entry on UTM](https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system)
 *
 * @author Rolf Rander Næss
 */
class UTM(
    var zone: Int = 0,
    var letter: Char = 0.toChar().uppercaseChar(),
    var easting: Double = 0.0,
    var northing: Double = 0.0
) {

    override fun toString(): String =
        String.format("%s %c %s %s", zone, letter, easting, northing)

    /**
     * Tests the exact representation. There might be more representations for
     * the same geographical point with different letters or zones, but that is
     * not taken into account.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UTM
        if (easting != other.easting) return false
        if (northing != other.northing) return false
        if (zone != other.zone) return false
        if (letter != other.letter) return false
        return true
    }

    override fun hashCode(): Int {
        var result = easting.hashCode()
        result = 32 * result + northing.hashCode()
        result = 32 * result + zone
        result = 32 * result + letter.hashCode()
        return result
    }

    constructor(utm: String) : this() {
        val parts = utm.split(" ").toTypedArray()
        zone = parts[0].toInt()
        letter = parts[1].uppercase()[0]
        easting = parts[2].toDouble()
        northing = parts[3].toDouble()
    }

    constructor(wgs: WGS84) : this() {
        fromWGS84(wgs.latitude, wgs.longitude)
    }

    private fun fromWGS84(latitude: Double, longitude: Double) {

        zone = floor(longitude / 6 + 31).toInt()

        letter = when {
            latitude < -72 -> 'C'
            latitude < -64 -> 'D'
            latitude < -56 -> 'E'
            latitude < -48 -> 'F'
            latitude < -40 -> 'G'
            latitude < -32 -> 'H'
            latitude < -24 -> 'J'
            latitude < -16 -> 'K'
            latitude < -8 -> 'L'
            latitude < 0 -> 'M'
            latitude < 8 -> 'N'
            latitude < 16 -> 'P'
            latitude < 24 -> 'Q'
            latitude < 32 -> 'R'
            latitude < 40 -> 'S'
            latitude < 48 -> 'T'
            latitude < 56 -> 'U'
            latitude < 64 -> 'V'
            latitude < 72 -> 'W'
            else -> 'X'
        }

        easting = 0.5 * ln(
            (1 + cos(latitude * Math.PI / 180) * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - cos(
                latitude * Math.PI / 180
            ) * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))
        ) * 0.9996 * 6399593.62 / (1 + 0.0820944379.pow(2.0) * cos(latitude * Math.PI / 180).pow(2.0)).pow(0.5) * (1 + 0.0820944379.pow(
            2.0
        ) / 2 * (0.5 * ln(
            (1 + cos(latitude * Math.PI / 180) * sin(
                longitude * Math.PI / 180 - (6 * zone - 183)
                        * Math.PI / 180
            )) / (1 - cos(latitude * Math.PI / 180)
                    * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))
        )).pow(2.0) * cos(latitude * Math.PI / 180).pow(2.0) / 3) + 500000

        easting = (easting * 100).roundToLong() * 0.01

        northing = (atan(
            tan(latitude * Math.PI / 180) / cos(
                longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180
            )
        ) - latitude * Math.PI / 180) * 0.9996 * 6399593.625 / sqrt(
            1 + 0.006739496742 * cos(latitude * Math.PI / 180).pow(2.0)
        ) * (1 + 0.006739496742 / 2 * (0.5 * ln(
            (1 + cos(latitude * Math.PI / 180) * sin(
                longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180
            )) / (1 - cos(latitude * Math.PI / 180) * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))
        )).pow(2.0) * cos(latitude * Math.PI / 180).pow(2.0)) + 0.9996 * 6399593.625 * (latitude * Math.PI / 180 - 0.005054622556 * (latitude * Math.PI / 180 + sin(
            2 * latitude * Math.PI / 180
        ) / 2) + 4.258201531e-05 * (3 * (latitude * Math.PI / 180 + sin(
            2 * latitude * Math.PI / 180
        ) / 2) + sin(2 * latitude * Math.PI / 180) * cos(latitude * Math.PI / 180).pow(2.0)) / 4 - 1.674057895e-07 * (5 * (3 * (latitude * Math.PI / 180 + sin(
            2 * latitude * Math.PI / 180
        ) / 2) + sin(2 * latitude * Math.PI / 180) * cos(latitude * Math.PI / 180).pow(2.0)) / 4 + sin(2 * latitude * Math.PI / 180) * cos(
            latitude * Math.PI / 180
        )
            .pow(2.0) * cos(latitude * Math.PI / 180).pow(2.0)) / 3)

        if (letter < 'N') northing += 10000000

        northing = (northing * 100).roundToLong() * 0.01
    }
}
