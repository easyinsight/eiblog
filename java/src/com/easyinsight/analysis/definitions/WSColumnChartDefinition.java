package com.easyinsight.analysis.definitions;

import com.easyinsight.analysis.*;
import com.easyinsight.core.*;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.dataset.LimitsResults;
import com.easyinsight.export.ExportService;
import com.easyinsight.security.SecurityUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: James Boe
 * Date: Mar 20, 2009
 * Time: 7:23:14 PM
 */
public class WSColumnChartDefinition extends WSXAxisDefinition {

    private int chartColor;
    private int gradientColor;
    private boolean useChartColor;
    private String columnSort;
    private String axisType = "Linear";
    private String labelPosition = "none";
    private int labelFontSize;
    private int labelInsideFontColor;
    private int labelOutsideFontColor;
    private boolean useInsideLabelFontColor;
    private boolean useOutsideLabelFontColor;
    private String labelFontWeight;
    private List<MultiColor> multiColors = new ArrayList<MultiColor>();

    public List<MultiColor> getMultiColors() {
        return multiColors;
    }

    public void setMultiColors(List<MultiColor> multiColors) {
        this.multiColors = multiColors;
    }

    public int getLabelFontSize() {
        return labelFontSize;
    }

    public void setLabelFontSize(int labelFontSize) {
        this.labelFontSize = labelFontSize;
    }

    public String getLabelFontWeight() {
        return labelFontWeight;
    }

    public void setLabelFontWeight(String labelFontWeight) {
        this.labelFontWeight = labelFontWeight;
    }

    public String getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(String labelPosition) {
        this.labelPosition = labelPosition;
    }

    public String getAxisType() {
        return axisType;
    }

    public void setAxisType(String axisType) {
        this.axisType = axisType;
    }

    public int getGradientColor() {
        return gradientColor;
    }

    public void setGradientColor(int gradientColor) {
        this.gradientColor = gradientColor;
    }

    public int getChartType() {
        return ChartDefinitionState.COLUMN_2D;
    }

    public int getChartFamily() {
        return ChartDefinitionState.COLUMN_FAMILY;
    }

    public int getLabelInsideFontColor() {
        return labelInsideFontColor;
    }

    public void setLabelInsideFontColor(int labelInsideFontColor) {
        this.labelInsideFontColor = labelInsideFontColor;
    }

    public int getLabelOutsideFontColor() {
        return labelOutsideFontColor;
    }

    public void setLabelOutsideFontColor(int labelOutsideFontColor) {
        this.labelOutsideFontColor = labelOutsideFontColor;
    }

    public boolean isUseInsideLabelFontColor() {
        return useInsideLabelFontColor;
    }

    public void setUseInsideLabelFontColor(boolean useInsideLabelFontColor) {
        this.useInsideLabelFontColor = useInsideLabelFontColor;
    }

    public boolean isUseOutsideLabelFontColor() {
        return useOutsideLabelFontColor;
    }

    public void setUseOutsideLabelFontColor(boolean useOutsideLabelFontColor) {
        this.useOutsideLabelFontColor = useOutsideLabelFontColor;
    }

    public String getColumnSort() {
        return columnSort;
    }

    public void setColumnSort(String columnSort) {
        this.columnSort = columnSort;
    }

    public int getChartColor() {
        return chartColor;
    }

    public void setChartColor(int chartColor) {
        this.chartColor = chartColor;
    }

    public boolean isUseChartColor() {
        return useChartColor;
    }

    public void setUseChartColor(boolean useChartColor) {
        this.useChartColor = useChartColor;
    }

    @Override
    public void populateProperties(List<ReportProperty> properties) {
        super.populateProperties(properties);
        chartColor = (int) findNumberProperty(properties, "chartColor", 0);
        gradientColor = (int) findNumberProperty(properties, "gradientColor", 0);
        useChartColor = findBooleanProperty(properties, "useChartColor", false);
        columnSort = findStringProperty(properties, "columnSort", "Unsorted");
        axisType = findStringProperty(properties, "axisType", "Linear");
        labelPosition = findStringProperty(properties, "labelPosition", "none");
        labelFontWeight = findStringProperty(properties, "labelFontWeight", "none");
        labelFontSize = (int) findNumberProperty(properties, "labelFontSize", 12);
        labelInsideFontColor = (int) findNumberProperty(properties, "labelInsideFontColor", 0);
        multiColors = multiColorProperty(properties, "multiColors");
        labelOutsideFontColor = (int) findNumberProperty(properties, "labelOutsideFontColor", 0);
        useInsideLabelFontColor = findBooleanProperty(properties, "useInsideLabelFontColor", false);
        useOutsideLabelFontColor = findBooleanProperty(properties, "useOutsideLabelFontColor", false);
    }

    @Override
    public List<ReportProperty> createProperties() {
        List<ReportProperty> properties = super.createProperties();
        properties.add(new ReportNumericProperty("chartColor", chartColor));
        properties.add(new ReportNumericProperty("gradientColor", gradientColor));
        properties.add(new ReportBooleanProperty("useChartColor", useChartColor));
        properties.add(new ReportStringProperty("columnSort", columnSort));
        properties.add(new ReportStringProperty("labelPosition", labelPosition));
        properties.add(new ReportNumericProperty("labelFontSize", labelFontSize));
        properties.add(new ReportStringProperty("labelFontWeight", labelFontWeight));
        properties.add(new ReportBooleanProperty("useInsideLabelFontColor", useInsideLabelFontColor));
        properties.add(new ReportBooleanProperty("useOutsideLabelFontColor", useOutsideLabelFontColor));
        properties.add(new ReportNumericProperty("labelInsideFontColor", labelInsideFontColor));
        properties.add(new ReportNumericProperty("labelOutsideFontColor", labelOutsideFontColor));
        properties.add(ReportMultiColorProperty.fromColors(multiColors, "multiColors"));
        return properties;
    }

    @Override
    public List<String> javaScriptIncludes() {
        List<String> includes = super.javaScriptIncludes();
//        includes.add("/js/plugins/jqplot.gradientBarRenderer.js");
//        includes.add("/js/plugins/jqplot.categoryAxisRenderer.js");
//        includes.add("/js/plugins/jqplot.canvasTextRenderer.min.js");
//        includes.add("/js/plugins/jqplot.canvasAxisLabelRenderer.min.js");
//        includes.add("/js/plugins/jqplot.canvasAxisTickRenderer.min.js");
//        includes.add("/js/plugins/jqplot.pointLabels.js");
//        includes.add("/js/visualizations/chart.js");
//        includes.add("/js/visualizations/util.js");
        return includes;
    }

    @Override
    public String rootHTML() {
        return "<div id=\"chartpseudotooltip\"></div>";
    }

    @Override
    public String toHTML(String targetDiv, HTMLReportMetadata htmlReportMetadata) {
        JSONObject fullObject = getJsonObject(htmlReportMetadata);


        String argh = fullObject.toString();
        argh = argh.replaceAll("\"", "");
        String timezoneOffset = "&timezoneOffset='+new Date().getTimezoneOffset()+'";
        /*AnalysisItem xAxis = getXaxis();
        DrillThrough drillThrough = null;
        if (xAxis.getLinks() != null) {
            for (Link link : xAxis.getLinks()) {
                if (link instanceof DrillThrough && link.isDefaultLink()) {
                    drillThrough = (DrillThrough) link;
                }
            }
        }

        String drillString = "";
        if (drillThrough != null) {
            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append("reportID=").append(getUrlKey()).append("&drillthroughID=").append(drillThrough.getLinkID()).append("&").append("sourceField=").append(xAxis.getAnalysisItemID());
            drillString = paramBuilder.toString();
        }*/
        String styleProps = htmlReportMetadata.createStyleProperties().toString();

        String xyz = "$.getJSON('/app/columnChart?reportID=" + getUrlKey() + timezoneOffset + "&'+ strParams, Chart.getColumnChartCallback('" + targetDiv + "', " + argh + "," + styleProps + "))";
        return xyz;
    }

    @Override
    public JSONObject toJSON(HTMLReportMetadata htmlReportMetadata, List<FilterDefinition> parentDefinitions) throws JSONException {

        JSONObject areaChart = super.toJSON(htmlReportMetadata, parentDefinitions);
        areaChart.put("type", "column");
        areaChart.put("key", getUrlKey());
        areaChart.put("url", "/app/columnChart");
        areaChart.put("parameters", getJsonObject(htmlReportMetadata));
        areaChart.put("styles", htmlReportMetadata.createStyleProperties());
        return areaChart;
    }

    private JSONObject getJsonObject(HTMLReportMetadata htmlReportMetadata) {
        String color;
        String color2;
        if (useChartColor) {
            color = String.format("'#%06X'", (0xFFFFFF & chartColor));
            color2 = getMeasures().size() > 1 ? color : String.format("'#%06X'", (0xFFFFFF & gradientColor));
        } else {
            color = "'#FF0000'";
            color2 = "'#990000'";
        }

        JSONObject params;
        JSONObject fullObject = new JSONObject();
        try {
            Map<String, Object> jsonParams = new LinkedHashMap<String, Object>();
            JSONObject seriesDefaults = new JSONObject();
            seriesDefaults.put("renderer", "$.jqplot.GradientBarRenderer");
                JSONArray colorObj = new JSONArray();

                JSONObject colorStop = new JSONObject();
                colorStop.put("point", 0);
                colorStop.put("color", color);
                colorObj.put(colorStop);

                colorStop = new JSONObject();
                colorStop.put("point", .15);
                colorStop.put("color", color2);
                colorObj.put(colorStop);

                colorStop = new JSONObject();
                colorStop.put("point", .5);
                colorStop.put("color", color);
                colorObj.put(colorStop);

                colorStop = new JSONObject();
                colorStop.put("point", .9);
                colorStop.put("color", color);
                colorObj.put(colorStop);

                colorStop = new JSONObject();
                colorStop.put("point", 1);
                colorStop.put("color", color2);
                colorObj.put(colorStop);

//                colorObj.put("first", "'" + color + "'");
//                colorObj.put("second", "'" + color2 + "'");
                jsonParams.put("seriesColors", new JSONArray(Arrays.asList(colorObj)));



            JSONObject rendererOptions = new JSONObject();
            rendererOptions.put("fillToZero", "true");
            rendererOptions.put("varyBarColor", "true");
            rendererOptions.put("shadowDepth", 2);
            rendererOptions.put("barMargin", 10);
            rendererOptions.put("barPadding", 0);
            seriesDefaults.put("rendererOptions", rendererOptions);
            /*seriesDefaults.put("shadow", true);
            seriesDefaults.put("shadowOffset", 1537573);
            seriesDefaults.put("shadowDepth", 1);*/
            jsonParams.put("seriesDefaults", seriesDefaults);
            JSONObject grid = getGrid();
            /*grid.put("borderColor", "'#000000'");
            grid.put("borderWidth", 1);*/
            grid.put("drawGridLines", false);
            jsonParams.put("grid", grid);

            jsonParams.put("axes", getAxes());
            params = new JSONObject(jsonParams);
            fullObject.put("jqplotOptions", params);
            JSONObject drillthroughOptions = new JSONObject();
            drillthroughOptions.put("embedded", htmlReportMetadata.isEmbedded());
            fullObject.put("drillthrough", drillthroughOptions);

            if ("auto".equals(getLabelPosition())) {
                JSONObject labels = new JSONObject();
                labels.put("location", "'n'");
                labels.put("show", "true");
                labels.put("edgetolerance", -15);
                seriesDefaults.put("pointLabels", labels);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return fullObject;
    }

    @Override
    public JSONObject getAxes() throws JSONException {
        JSONObject axes = new JSONObject();
        JSONObject xAxis = getGroupingAxis(getXaxis());
        axes.put("xaxis", xAxis);

        axes.put("yaxis", getMeasureAxis(getMeasures().get(0)));
        return axes;
    }

    @Override
    protected List<MultiColor> configuredMultiColors() {
        return multiColors;
    }
}
