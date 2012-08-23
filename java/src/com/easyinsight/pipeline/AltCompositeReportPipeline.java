package com.easyinsight.pipeline;

import com.easyinsight.analysis.*;
import com.easyinsight.calculations.CalcGraph;
import com.easyinsight.datafeeds.FeedService;
import com.easyinsight.etl.LookupTable;

import java.util.*;

/**
 * User: jamesboe
 * Date: 3/13/11
 * Time: 12:16 PM
 */
public class AltCompositeReportPipeline extends Pipeline {
    private Collection<AnalysisItem> joinItems;

    public AltCompositeReportPipeline(Collection<AnalysisItem> joinItems) {
        this.joinItems = joinItems;
    }

    @Override
    protected List<IComponent> generatePipelineCommands(Set<AnalysisItem> allNeededAnalysisItems, Set<AnalysisItem> reportItems, Collection<FilterDefinition> filters, WSAnalysisDefinition report, List<AnalysisItem> allItems, InsightRequestMetadata insightRequestMetadata) {
        List<IComponent> components = new ArrayList<IComponent>();
        for (AnalysisItem analysisItem : allNeededAnalysisItems) {
            if (analysisItem.getLookupTableID() != null && analysisItem.getLookupTableID() > 0) {
                LookupTable lookupTable = new FeedService().getLookupTable(analysisItem.getLookupTableID());
                if (lookupTable.getSourceField().hasType(AnalysisItemTypes.LISTING)) {
                    AnalysisList analysisList = (AnalysisList) lookupTable.getSourceField();
                    if (analysisList.isMultipleTransform()) components.add(new TagTransformComponent(analysisList));
                } else if (lookupTable.getSourceField().hasType(AnalysisItemTypes.DERIVED_DIMENSION)) {
                    Set<AnalysisItem> analysisItems = new HashSet<AnalysisItem>();
                    analysisItems.add(lookupTable.getSourceField());
                    components.addAll(new CalcGraph().doFunGraphStuff(analysisItems, allItems, reportItems, Pipeline.BEFORE, new AnalysisItemRetrievalStructure(Pipeline.BEFORE)));
                }
                components.add(new LookupTableComponent(lookupTable));
            }
        }
        for (AnalysisItem range : items(AnalysisItemTypes.RANGE_DIMENSION, allNeededAnalysisItems)) {
            components.add(new RangeComponent((AnalysisRangeDimension) range));
        }
        components.addAll(new CalcGraph().doFunGraphStuff(new HashSet<AnalysisItem>(joinItems), allItems, reportItems, Pipeline.BEFORE, new AnalysisItemRetrievalStructure(Pipeline.BEFORE)));
        for (AnalysisItem item : joinItems) {
            for (AnalysisItem tag : items(AnalysisItemTypes.LISTING, joinItems)) {
                AnalysisList analysisList = (AnalysisList) tag;
                if (analysisList.isMultipleTransform()) components.add(new TagTransformComponent(analysisList));
            }
            components.add(new DateTransformComponent(item));
        }
        /*components.add(new NormalizationComponent());
        components.add(new AggregationComponent());*/
        return components;
    }
}
