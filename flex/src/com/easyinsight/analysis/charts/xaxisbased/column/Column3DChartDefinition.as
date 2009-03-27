package com.easyinsight.analysis.charts.xaxisbased.column {
import com.easyinsight.analysis.charts.ChartTypes;
import com.easyinsight.analysis.charts.xaxisbased.XAxisDefinition;
[Bindable]
[RemoteClass(alias="com.easyinsight.analysis.definitions.WS3DColumnChartDefinition")]
public class Column3DChartDefinition extends XAxisDefinition{
    public function Column3DChartDefinition() {
        super();
    }

    override public function getLabel():String {
        return "3D Column";
    }

    override public function get controller():String {
        return "com.easyinsight.analysis.charts.xaxisbased.column.Column3DChartController";
    }

    override public function getChartType():int {
        return ChartTypes.COLUMN_3D;
    }

    override public function getChartFamily():int {
        return ChartTypes.COLUMN_FAMILY;
    }
}
}