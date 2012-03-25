package com.easyinsight.calculations;

import com.easyinsight.datafeeds.IJoin;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jamesboe
 * Date: 3/24/12
 * Time: 6:32 PM
 */
public class CompositeCalculationMetadata extends CalculationMetadata {
    private List<IJoin> joins = new ArrayList<IJoin>();

    public List<IJoin> getJoins() {
        return joins;
    }

    public void setJoins(List<IJoin> joins) {
        this.joins = joins;
    }
}
