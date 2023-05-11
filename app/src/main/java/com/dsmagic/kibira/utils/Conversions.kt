package com.dsmagic.kibira.utils

/*
 *  This file is part of Kibira.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Kibira is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Kibira is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Kibira. If not, see <http://www.gnu.org/licenses/>
 */

class Conversions {
    companion object {
        private const val METRES_FROM_FEET = 3.28084   //one meter = 3.28084 ft
        private const val METRES_FROM_INCHES = 0.0254
        private const val METRES_FROM_MILES = 1609.34
        private const val METRES_FROM_ACRES = 4046.856 // ONE acre = 4046.856 meters

        //const val GAP_SIZE_METRES = GAP_SIZE_FEET / METRES_FROM_FEET
        fun ftToMeters(userInput: String, userUnits: String): Double {
            var rawUserInput = 0.0
            var userInputInMeters = 0.0

            //if "", then the user didn't change units, thus ft is the default.
            if (userUnits == "") {
                val r = userInput.toDouble()
                rawUserInput = (r / METRES_FROM_FEET)
                userInputInMeters = rawUserInput

            }

            //Note: Leave as is.. " mmm"
            when (userUnits) {
                " Meters" -> {
                    rawUserInput = userInput.toDouble()
                    userInputInMeters = rawUserInput

                }
                " Ft" -> {
                    val r = userInput.toDouble()
                    userInputInMeters = (r / METRES_FROM_FEET)

                }
                " Miles" -> {
                    val r = userInput.toDouble()
                    userInputInMeters = (r * METRES_FROM_MILES)


                }
                " Acres" -> {
                    val r = userInput.toDouble()
                    userInputInMeters = (r * METRES_FROM_ACRES)

                }
                " Inches" -> {
                    val r = userInput.toDouble()
                    userInputInMeters = (r * METRES_FROM_INCHES)

                }
            }
            return userInputInMeters
        }
    }
}