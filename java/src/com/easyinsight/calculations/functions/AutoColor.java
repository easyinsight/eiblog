package com.easyinsight.calculations.functions;

import cern.jet.stat.Descriptive;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.TextValueExtension;
import com.easyinsight.calculations.*;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.Value;
import com.easyinsight.logging.LogClass;

import java.awt.*;

/**
 * User: jamesboe
 * Date: 11/11/14
 * Time: 11:52 AM
 */
public class AutoColor extends Function {

    @Override
    public Value evaluate() {
        String statName = minusBrackets(getParameterName(0));
        AnalysisItem statMeasure = findDataSourceItem(0);
        Value target;
        Value source = getParameter(0);
        int pCount;
        if (paramCount() == 4) {
            target = getParameter(1);
            pCount = 2;
        } else {
            target = source;
            pCount = 1;
        }
        String color1String = minusQuotes(getParameter(pCount)).toString();
        if (color1String.length() == 7) {
            color1String = color1String.substring(1, 7);
        }
        int color1 = Integer.parseInt(color1String, 16);
        String color2String = minusQuotes(getParameter(pCount+1)).toString();
        if (color2String.length() == 7) {
            color2String = color2String.substring(1, 7);
        }
        int color2 = Integer.parseInt(color2String, 16);
        if (statMeasure == null) {
            throw new FunctionException("Could not find the specified field " + statName);
        }
        String processName = statMeasure.qualifiedName();
        StatCalculationCache statCache = (StatCalculationCache) calculationMetadata.getCache(new StatCacheBuilder(null, statMeasure,
                d -> new Scale(Descriptive.min(d), Descriptive.max(d))), processName);

        double instanceValue = source.toDouble();
        Scale scale = (Scale) statCache.getResult();
        double range = scale.max - scale.min;
        double place = instanceValue / range;
        Color c1 = new Color(color1);
        Color c2 = new Color(color2);
        int redDelta = c2.getRed() - c1.getRed();
        int endRed = Math.min(Math.max((int) (redDelta * place + c1.getRed()), 0), 255);
        int greenDelta = c2.getGreen() - c1.getGreen();
        int endGreen = Math.min(Math.max((int) (greenDelta * place + c1.getGreen()), 0), 255);
        int blueDelta = c2.getBlue() - c1.getBlue();
        int endBlue = Math.min(Math.max((int) (blueDelta * place + c1.getBlue()), 0), 255);
        int endRGB = new Color(endRed, endGreen, endBlue).getRGB();
        TextValueExtension textValueExtension = (TextValueExtension) target.getValueExtension();
        if (textValueExtension == null) {
            textValueExtension = new TextValueExtension();
            target.setValueExtension(textValueExtension);
        }
        textValueExtension.setColor(endRGB);
        return new EmptyValue();
    }

    @Override
    public boolean onDemand() {
        return true;
    }

    private static class Scale {
        private double min;
        private double max;

        private Scale(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    @Override
    public int getParameterCount() {
        return -1;
    }
}
