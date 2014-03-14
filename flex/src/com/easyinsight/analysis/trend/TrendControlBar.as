/**
 * Created by IntelliJ IDEA.
 * User: jamesboe
 * Date: 9/26/11
 * Time: 12:34 PM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.analysis.trend {
import com.easyinsight.analysis.AnalysisChangedEvent;
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.AnalysisItemUpdateEvent;
import com.easyinsight.analysis.CustomChangeEvent;
import com.easyinsight.analysis.DataServiceEvent;
import com.easyinsight.analysis.IReportControlBar;
import com.easyinsight.analysis.ListDropAreaGrouping;
import com.easyinsight.analysis.ReportControlBar;
import com.easyinsight.analysis.ReportDataEvent;
import com.easyinsight.analysis.TrendDropArea;

import mx.collections.ArrayCollection;

import mx.controls.Label;

public class TrendControlBar extends ReportControlBar implements IReportControlBar {

    private var listViewGrouping:ListDropAreaGrouping;
    private var listDefinition:TrendDefinition;

    public function TrendControlBar() {
        super();
        listViewGrouping = new ListDropAreaGrouping();
        listViewGrouping.unlimited = true;
        listViewGrouping.dropAreaType = TrendDropArea;
        listViewGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        setStyle("verticalAlign", "middle");
    }

    override protected function createChildren():void {
        super.createChildren();
        var measureLabel:Label = new Label();
        measureLabel.text = "Measures:";
        measureLabel.setStyle("fontSize", 14);
        addChild(measureLabel);
        listViewGrouping.report = listDefinition;
        addDropAreaGrouping(listViewGrouping);
        var columns:ArrayCollection = listDefinition.measures;
        if (columns != null) {
            for (var i:int = 0; i < columns.length; i++) {
                var column:AnalysisItem = columns.getItemAt(i) as AnalysisItem;
                listViewGrouping.addAnalysisItem(column);
            }
        }
    }

    public function onDataReceipt(event:DataServiceEvent):void {
    }

    private function requestListData(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function set analysisDefinition(analysisDefinition:AnalysisDefinition):void {
        listDefinition = analysisDefinition as TrendDefinition;
    }

    public function isDataValid():Boolean {
        return listViewGrouping.getListColumns().length > 0;
    }

    public function createAnalysisDefinition():AnalysisDefinition {
        listDefinition.measures = new ArrayCollection(listViewGrouping.getListColumns());
        return listDefinition;
    }

    public function addItem(analysisItem:AnalysisItem):void {
        if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
            listViewGrouping.addAnalysisItem(analysisItem);
            dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
            dispatchEvent(new AnalysisChangedEvent(false));
        }
    }

    public function onCustomChangeEvent(event:CustomChangeEvent):void {
    }
}
}
