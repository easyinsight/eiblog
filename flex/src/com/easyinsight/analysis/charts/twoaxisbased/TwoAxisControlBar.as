package com.easyinsight.analysis.charts.twoaxisbased {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItemUpdateEvent;
import com.easyinsight.analysis.CustomChangeEvent;
import com.easyinsight.analysis.DataServiceEvent;
import com.easyinsight.analysis.DimensionDropArea;
import com.easyinsight.analysis.IReportControlBar;
import com.easyinsight.analysis.ListDropAreaGrouping;
import com.easyinsight.analysis.MeasureDropArea;
import com.easyinsight.analysis.ReportDataEvent;
import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.containers.HBox;
import mx.controls.Label;
import mx.events.FlexEvent;
public class TwoAxisControlBar extends HBox implements IReportControlBar {
    private var xAxisGrouping:ListDropAreaGrouping;
    private var yAxisGrouping:ListDropAreaGrouping;
    private var measureGrouping:ListDropAreaGrouping;

    private var xAxisDefinition:TwoAxisDefinition;

    public function TwoAxisControlBar() {
        xAxisGrouping = new ListDropAreaGrouping();
        xAxisGrouping.maxElements = 1;
        xAxisGrouping.dropAreaType = DimensionDropArea;
        xAxisGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        yAxisGrouping = new ListDropAreaGrouping();
        yAxisGrouping.maxElements = 1;
        yAxisGrouping.dropAreaType = DimensionDropArea;
        yAxisGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        measureGrouping = new ListDropAreaGrouping();
        measureGrouping.maxElements = 1;
        measureGrouping.dropAreaType = MeasureDropArea;
        measureGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        setStyle("verticalAlign", "middle");
    }

    override protected function createChildren():void {
        super.createChildren();
        var groupingLabel:Label = new Label();
        groupingLabel.text = "Y Axis Grouping:";
        groupingLabel.setStyle("fontSize", 14);
        addChild(groupingLabel);
        addChild(yAxisGrouping);
        var xAxisGroupingLabel:Label = new Label();
        xAxisGroupingLabel.text = "X Axis Grouping:";
        xAxisGroupingLabel.setStyle("fontSize", 14);
        addChild(xAxisGroupingLabel);
        addChild(xAxisGrouping);
        var measureGroupingLabel:Label = new Label();
        measureGroupingLabel.text = "Measure:";
        measureGroupingLabel.setStyle("fontSize", 14);
        addChild(measureGroupingLabel);
        addChild(measureGrouping);
         if (xAxisDefinition.xaxis != null) {
            xAxisGrouping.addAnalysisItem(xAxisDefinition.xaxis);
        }
        if (xAxisDefinition.yaxis != null) {
            yAxisGrouping.addAnalysisItem(xAxisDefinition.yaxis);
        }
        if (xAxisDefinition.measure != null) {
            measureGrouping.addAnalysisItem(xAxisDefinition.measure);
        }
        var limitLabel:Label = new Label();
        BindingUtils.bindProperty(limitLabel, "text", this, "limitText");
        addChild(limitLabel);
    }

    private var _limitText:String;

    [Bindable]
    public function get limitText():String {
        return _limitText;
    }

    public function set limitText(val:String):void {
        _limitText = val;
        dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
    }

    public function onDataReceipt(event:DataServiceEvent):void {
        if (event.limitedResults) {
            limitText = "Showing " + event.limitResults + " of " + event.maxResults + " results";
        } else {
            limitText = "";
        }
    }

    private function requestListData(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function set analysisDefinition(analysisDefinition:AnalysisDefinition):void {
        xAxisDefinition = analysisDefinition as TwoAxisDefinition;
    }

    public function isDataValid():Boolean {
        return (xAxisGrouping.getListColumns().length > 0 && measureGrouping.getListColumns().length > 0 &&
                yAxisGrouping.getListColumns().length > 0);
    }

    public function createAnalysisDefinition():AnalysisDefinition {
        xAxisDefinition.xaxis = xAxisGrouping.getListColumns()[0];
        xAxisDefinition.yaxis = yAxisGrouping.getListColumns()[0];
        xAxisDefinition.measure = measureGrouping.getListColumns()[0];
        return xAxisDefinition;
    }

    public function set analysisItems(analysisItems:ArrayCollection):void {
        xAxisGrouping.analysisItems = analysisItems;
        measureGrouping.analysisItems = analysisItems;
    }

    public function addItem(analysisItem:com.easyinsight.analysis.AnalysisItem):void {
    }

    public function onCustomChangeEvent(event:CustomChangeEvent):void {
    }
}
}