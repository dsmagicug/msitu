package com.dsmagic.kibira.roomDatabase.sharing;
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

import java.util.List;

public class ProjectDTO {
    private String name;
    private double gapsize;
    private double lineLength;
    private String meshType;
    private  String gapsizeunits;
    private String lineLengthUnits;

    private String plantingDirection;

    public List<?> getBasePoints() {
        return basePoints;
    }

    public void setBasePoints(List<?> basePoints) {
        this.basePoints = basePoints;
    }

    private List<?> basePoints;

    public List<?> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<?> coordinates) {
        this.coordinates = coordinates;
    }

    private List<?> coordinates;


    public String getMeshType() {
        return meshType;
    }

    public void setMeshType(String meshType) {
        this.meshType = meshType;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGapsize() {
        return gapsize;
    }

    public void setGapsize(double gapsize) {
        this.gapsize = gapsize;
    }

    public double getLineLength() {
        return lineLength;
    }

    public void setLineLength(double lineLength) {
        this.lineLength = lineLength;
    }


    public String getGapsizeunits() {
        return gapsizeunits;
    }

    public void setGapsizeunits(String gapsizeunits) {
        this.gapsizeunits = gapsizeunits;
    }

    public String getLineLengthUnits() {
        return lineLengthUnits;
    }

    public void setLineLengthUnits(String lineLengthUnits) {
        this.lineLengthUnits = lineLengthUnits;
    }

    public String getPlantingDirection() {
        return plantingDirection;
    }

    public void setPlantingDirection(String plantingDirection) {
        this.plantingDirection = plantingDirection;
    }


}
