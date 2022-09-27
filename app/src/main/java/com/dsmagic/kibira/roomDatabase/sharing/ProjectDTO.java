package com.dsmagic.kibira.roomDatabase.sharing;


import java.util.ArrayList;
import java.util.List;

public class ProjectDTO {
    private String name;
    private double gapsize;
    private double lineLength;
    private String meshType;
    private  String gapsizeunits;
    private String lineLengthUnits;

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


}
