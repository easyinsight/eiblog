package com.easyinsight.storage;

import com.easyinsight.core.Key;
import com.easyinsight.core.Value;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * User: James Boe
* Date: Nov 10, 2008
* Time: 6:27:32 PM
*/
public class NumericWhere implements IWhere {
    private Key key;
    private double value;
    private Comparison comparison;

    public NumericWhere(Key key, double value, Comparison comparison) {
        this.key = key;
        this.value = value;
        this.comparison = comparison;
    }

    public String createWhereSQL() {
        String comparator = comparison.createComparison();
        return "k" + key.getKeyID() + " " + comparator + " ?";
    }

    public int setValue(PreparedStatement preparedStatement, int position) throws SQLException {
        preparedStatement.setDouble(position, value);
        return position + 1;
    }

    public List<String> getExtraTables() {
        return new ArrayList<String>();    
    }

    public Key getKey() {
        return key;
    }

    public boolean hasConcreteValue() {
        return false;
    }

    public Value getConcreteValue() {
        throw new UnsupportedOperationException();
    }
}
