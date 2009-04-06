package com.easyinsight.api;

import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.IRow;
import com.easyinsight.storage.IWhere;
import com.easyinsight.core.*;

import java.util.List;
import java.util.ArrayList;

/**
 * User: James Boe
 * Date: Jan 15, 2009
 * Time: 2:34:49 PM
 */
public abstract class PublishService {
    protected final DataSet toDataSet(Row row) {
        DataSet dataSet = new DataSet();
        dataSet.addRow(toRow(row));
        return dataSet;
    }

    protected final DataSet toDataSet(Row[] rows) {
        DataSet dataSet = new DataSet();
        for (Row row : rows) {
            dataSet.addRow(toRow(row));
        }
        return dataSet;
    }

    private IRow toRow(Row row) {
        IRow transformedRow = new com.easyinsight.analysis.Row();
        StringPair[] stringPairs = row.getStringPairs();
        if (stringPairs != null) {
            for (StringPair stringPair : stringPairs) {
                transformedRow.addValue(stringPair.getKey(), stringPair.getValue() == null ? new EmptyValue() : new StringValue(stringPair.getValue()));
            }
        }
        NumberPair[] numberPairs = row.getNumberPairs();
        if (numberPairs != null) {
            for (NumberPair numberPair : numberPairs) {
                transformedRow.addValue(numberPair.getKey(), new NumericValue(numberPair.getValue()));
            }
        }
        DatePair[] datePairs = row.getDatePairs();
        if (datePairs != null) {
            for (DatePair datePair : datePairs) {
                transformedRow.addValue(datePair.getKey(), datePair.getValue() == null ? new EmptyValue() : new DateValue(datePair.getValue()));
            }
        }
        return transformedRow;
    }

    protected final List<IWhere> createWheres(Where where) {
        List<IWhere> wheres = new ArrayList<IWhere>();
        StringWhere[] stringWheres = where.getStringWheres();
        if (stringWheres != null) {
            for (StringWhere stringWhere : stringWheres) {
                wheres.add(new com.easyinsight.storage.StringWhere(new NamedKey(stringWhere.getKey()), stringWhere.getValue()));
            }
        }
        NumberWhere[] numberWheres = where.getNumberWheres();
        if (numberWheres != null) {
            for (NumberWhere numberWhere : numberWheres) {
                wheres.add(new com.easyinsight.storage.NumericWhere(new NamedKey(numberWhere.getKey()), numberWhere.getValue(), numberWhere.getComparison().createStorageComparison()));
            }
        }
        DateWhere[] dateWheres = where.getDateWheres();
        if (dateWheres != null) {
            for (DateWhere dateWhere : dateWheres) {
                wheres.add(new com.easyinsight.storage.DateWhere(new NamedKey(dateWhere.getKey()), dateWhere.getValue(), dateWhere.getComparison().createStorageComparison()));
            }
        }
        DayWhere[] dayWheres = where.getDayWheres();
        if (dayWheres != null) {
            for (DayWhere dayWhere : dayWheres) {
                System.out.println("Updating day " + dayWhere.getDayOfYear() + " - " + dayWhere.getYear());
                wheres.add(new com.easyinsight.storage.DayWhere(new NamedKey(dayWhere.getKey()), dayWhere.getYear(), dayWhere.getDayOfYear()));
            }
        }
        return wheres;
    }
}
