package com.easyinsight.calculations.functions;

import cern.jet.stat.Descriptive;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.TextValueExtension;
import com.easyinsight.analysis.WSCrosstabDefinition;
import com.easyinsight.calculations.Function;
import com.easyinsight.calculations.FunctionException;
import com.easyinsight.calculations.StatCacheBuilder;
import com.easyinsight.calculations.StatCalculationCache;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.Value;
import com.easyinsight.logging.LogClass;

import java.awt.*;

/**
 * User: jamesboe
 * Date: 11/11/14
 * Time: 11:52 AM
 */
public class AutoBackgroundColor extends Function {

    @Override
    public Value evaluate() {
        AnalysisItem statMeasure;
        int color1;
        int color2;

        if (paramCount() == 0) {
            if (calculationMetadata.getReport() instanceof WSCrosstabDefinition) {
                WSCrosstabDefinition crosstabDefinition = (WSCrosstabDefinition) calculationMetadata.getReport();
                statMeasure = crosstabDefinition.getMeasures().get(0);
                color2 = crosstabDefinition.getSummaryBackgroundColor();
                color1 = 0xFFFFFF;
            } else {
                return new EmptyValue();
            }
        } else if (paramCount() == 1) {
            if (calculationMetadata.getReport() instanceof WSCrosstabDefinition) {
                WSCrosstabDefinition crosstabDefinition = (WSCrosstabDefinition) calculationMetadata.getReport();
                statMeasure = crosstabDefinition.getMeasures().get(0);
                String color2String = minusQuotes(getParameter(2)).toString();
                if (color2String.length() == 7) {
                    color2String = color2String.substring(1, 7);
                }
                color2 = Integer.parseInt(color2String, 16);
                color1 = 0xFFFFFF;
            } else {
                return new EmptyValue();
            }
        } else {
            String statName = minusBrackets(getParameterName(0));

            statMeasure = findDataSourceItem(0);
            String color1String = minusQuotes(getParameter(1)).toString();
            if (color1String.length() == 7) {
                color1String = color1String.substring(1, 7);
            }

            color1 = Integer.parseInt(color1String, 16);
            String color2String = minusQuotes(getParameter(2)).toString();
            if (color2String.length() == 7) {
                color2String = color2String.substring(1, 7);
            }

            color2 = Integer.parseInt(color2String, 16);
            if (statMeasure == null) {
                throw new FunctionException("Could not find the specified field " + statName);
            }
        }
        String processName = statMeasure.qualifiedName();
        StatCalculationCache statCache = (StatCalculationCache) calculationMetadata.getCache(new StatCacheBuilder(null, statMeasure,
                d -> new Scale(Descriptive.min(d), Math.log(Descriptive.max(d)))), processName);
        Value target = getRow().getValue(statMeasure);
        //Value target = getParameter(0);
        double instanceValue = target.toDouble();
        System.out.println("instance value = " + instanceValue);
        Scale scale = (Scale) statCache.getResult();
        if (scale.min < 0) {
            scale.min = -(Math.log(Math.abs(scale.min)));
        }
        double range = scale.max - scale.min;
        double place = (instanceValue > 0 ? Math.log(instanceValue) : 0) / range;
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
        textValueExtension.setBackgroundColor(endRGB);
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
