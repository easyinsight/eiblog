package com.easyinsight.analysis.charts.twoaxisbased.area {
import com.easyinsight.analysis.charts.ChartTypes;
import com.easyinsight.analysis.charts.twoaxisbased.TwoAxisDefinition;
import com.easyinsight.analysis.AnalysisDefinition;
[Bindable]
[RemoteClass(alias="com.easyinsight.analysis.definitions.WSAreaChartDefinition")]
public class AreaChartDefinition extends TwoAxisDefinition{
    public function AreaChartDefinition() {
        super();
    }

    override public function get type():int {
        return AnalysisDefinition.AREA;
    }

    override public function getChartType():int {
        return ChartTypes.AREA_2D;
    }

    override public function getChartFamily():int {
        return ChartTypes.AREA_FAMILY;
    }
}
}