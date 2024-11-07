package com.rtnmsitu.maths

import kotlin.math.*

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

/**
 * Class representing WGS84-coordinates. Based on code from stack overflow.
 * @see [Stack Overflow](https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm)
 *
 * @see [Wikipedia-entry on WGS-84](https://en.wikipedia.org/wiki/World_Geodetic_System)
 *
 * @author Rolf Rander Næss
 */
class WGS84(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) {

    constructor(utm: UTM) : this() {
        fromUTM(utm.zone, utm.letter, utm.easting, utm.northing)
    }

    override fun toString(): String {
        val ns = if (latitude < 0) 'S' else 'N'
        val ew = if (longitude < 0) 'W' else 'E'
        return String.format("%s%c %s%c", abs(latitude), ns, abs(longitude), ew)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WGS84
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 32 * result + longitude.hashCode()
        return result
    }

    private fun fromUTM(zone: Int, letter: Char, easting: Double, northing: Double) {

        val north: Double = if (letter > 'M') northing else northing - 10000000

        latitude = (north / 6366197.724 / 0.9996 + (1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996)
            .pow(2.0) - 0.006739496742 * sin(north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996) * (atan(
            cos(
                atan(
                    (exp(
                        (easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0) / 3)
                    ) - exp(
                        -(easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0) / 3)
                    )) / 2 / cos(
                        (north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) / 2) + (0.006739496742 * 3 / 4).pow(2.0) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 - (0.006739496742 * 3 / 4).pow(
                            3.0
                        ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996)
                            .pow(2.0)) / 4 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) * cos(north / 6366197.724 / 0.9996).pow(2.0) * cos(north / 6366197.724 / 0.9996)
                            .pow(2.0)) / 3)) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0)) + north / 6366197.724 / 0.9996
                    )
                )
            ) * tan(
                (north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + (0.006739496742 * 3 / 4).pow(2.0) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 - (0.006739496742 * 3 / 4).pow(
                    3.0
                ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) * cos(north / 6366197.724 / 0.9996).pow(2.0) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 3)) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0)) + north / 6366197.724 / 0.9996
            )
        ) - north / 6366197.724 / 0.9996) * 3 / 2) * (atan(
            cos(
                atan(
                    (exp(
                        (easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0) / 3)
                    ) - exp(
                        -(easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0) / 3)
                    )) / 2 / cos(
                        (north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) / 2) + (0.006739496742 * 3 / 4).pow(2.0) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 - (0.006739496742 * 3 / 4).pow(
                            3.0
                        ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 + sin(
                            2 * north / 6366197.724 / 0.9996
                        ) * cos(north / 6366197.724 / 0.9996).pow(2.0) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 3)) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                            1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                        ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0)) + north / 6366197.724 / 0.9996
                    )
                )
            ) * tan(
                (north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + (0.006739496742 * 3 / 4).pow(2.0) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 - (0.006739496742 * 3 / 4).pow(
                    3.0
                ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) * cos(north / 6366197.724 / 0.9996).pow(2.0) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 3)) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0)) + north / 6366197.724 / 0.9996
            )
        ) - north / 6366197.724 / 0.9996)) * 180 / Math.PI

        latitude = (latitude * 10000000).roundToLong().toDouble()

        latitude /= 10000000

        longitude = atan(
            (exp(
                (easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0) / 3)
            ) - exp(
                -(easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0) / 3)
            )) / 2 / cos(
                (north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + (0.006739496742 * 3 / 4).pow(2.0) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 - (0.006739496742 * 3 / 4).pow(
                    3.0
                ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) / 2) + sin(2 * north / 6366197.724 / 0.9996) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 4 + sin(
                    2 * north / 6366197.724 / 0.9996
                ) * cos(north / 6366197.724 / 0.9996).pow(2.0) * cos(north / 6366197.724 / 0.9996).pow(2.0)) / 3)) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                )) * (1 - 0.006739496742 * ((easting - 500000) / (0.9996 * 6399593.625 / sqrt(
                    1 + 0.006739496742 * cos(north / 6366197.724 / 0.9996).pow(2.0)
                ))).pow(2.0) / 2 * cos(north / 6366197.724 / 0.9996).pow(2.0)) + north / 6366197.724 / 0.9996
            )
        ) * 180 / Math.PI + zone * 6 - 183

        longitude = (longitude * 10000000).roundToLong().toDouble()

        longitude /= 10000000
    }
}