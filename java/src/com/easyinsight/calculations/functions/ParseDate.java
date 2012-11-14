package com.easyinsight.calculations.functions;

import com.easyinsight.calculations.Function;
import com.easyinsight.core.DateValue;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: jamesboe
 * Date: 7/8/12
 * Time: 10:47 AM
 */
public class ParseDate extends Function {
    public Value evaluate() {
        try {
            Value start = params.get(0);
            String string = start.toString();
            String value;
            if (string.startsWith("\"")) {
                value = minusQuotes(0);
            } else {
                value = string;
            }
            String format = minusQuotes(1);
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = sdf.parse(value);
            return new DateValue(date);
        } catch (ParseException e) {
            return new EmptyValue();
        }
    }

    public int getParameterCount() {
        return 2;
    }
}
