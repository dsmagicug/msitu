package com.dsmagic.kibira.utils

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

            //if "", then user didn't change units, thus ft is the default.
            if (userUnits == "") {
                val r = userInput.toDouble()
                rawUserInput = (r / METRES_FROM_FEET)
                userInputInMeters = rawUserInput

            }

            when (userUnits) {
                " Meters" -> {
                    rawUserInput = userInput.toDouble()
                    userInputInMeters = rawUserInput

                }
                " Ft" -> {
                    val r = rawUserInput
                    userInputInMeters = (r / METRES_FROM_FEET)

                }
                " Miles" -> {
                    val r = rawUserInput
                    userInputInMeters = (r * METRES_FROM_MILES)


                }
                " Acres" -> {
                    val r = rawUserInput
                    userInputInMeters = (r * METRES_FROM_ACRES)

                }
                " Inches" -> {
                    val r = rawUserInput
                    userInputInMeters = (r * METRES_FROM_INCHES)

                }
            }
            return userInputInMeters
        }
    }
}