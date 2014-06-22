package com.easyinsight.analysis.service {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.DataServiceEvent;
import com.easyinsight.analysis.IReportDataService;
import com.easyinsight.analysis.ListDataResults;
import com.easyinsight.analysis.RequestParams;
import com.easyinsight.analysis.Value;
import com.easyinsight.filtering.AnalysisItemFilterDefinition;
import com.easyinsight.filtering.FilterDefinition;
import com.easyinsight.framework.DataServiceLoadingEvent;
import com.easyinsight.framework.GenericFaultHandler;
import com.easyinsight.framework.InsightRequestMetadata;
import com.easyinsight.framework.InvalidFieldsEvent;

import flash.events.EventDispatcher;



import mx.collections.ArrayCollection;

import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class HTMLDataService extends EventDispatcher implements IReportDataService {

    private var dataRemoteSource:RemoteObject;



    public function HTMLDataService() {
        super();
        dataRemoteSource = new RemoteObject();
        dataRemoteSource.destination = "data";
        dataRemoteSource.list.addEventListener(ResultEvent.RESULT, processListData);
        dataRemoteSource.moreResults.addEventListener(ResultEvent.RESULT, processMoreListData);
        dataRemoteSource.list.addEventListener(FaultEvent.FAULT, GenericFaultHandler.genericFault);
    }

    private var _preserveValues:Boolean = true;

    public function set preserveValues(value:Boolean):void {
        _preserveValues = value;
    }

    public function translate(listData:ListDataResults, analysisDefinition:AnalysisDefinition):ServiceData {
        var headers:ArrayCollection = new ArrayCollection(listData.headers);
        var rows:ArrayCollection = new ArrayCollection(listData.rows);
        var data:ArrayCollection = new ArrayCollection();
        var map:Object = new Object();
        if (analysisDefinition.filterDefinitions != null) {
            for each (var filter:FilterDefinition in analysisDefinition.filterDefinitions) {
                if (filter.getType() == FilterDefinition.ANALYSIS_ITEM) {
                    var aFilter:AnalysisItemFilterDefinition = filter as AnalysisItemFilterDefinition;
                    map[aFilter.targetItem.qualifiedName()] = aFilter.field;
                }
            }
        }
        for (var i:int = 0; i < rows.length; i++) {
            var row:Object = rows.getItemAt(i);
            var values:Array = row.values as Array;
            var endObject:Object = new Object();
            //endObject["drillThroughField"] = row.drillThroughData;
            for (var j:int = 0; j < headers.length; j++) {
                var headerDimension:AnalysisItem = headers[j];


                var value:Value = values[j];
                var key:String = headerDimension.qualifiedName();
                if (_preserveValues) {
                    endObject[key] = value;
                } else {
                    endObject[key] = value.getValue();
                }
                if (value.links != null) {
                    for (var linkKey:String in value.links) {
                        var str:String = linkKey + "_link";
                        endObject[str] = value.links[linkKey];
                    }
                }
                /*if (value.drillThroughValue != null) {
                    value.drillThroughValue = "blah";
                }*/

                var aliasItem:AnalysisItem = map[headerDimension.qualifiedName()] as AnalysisItem;
                if (aliasItem != null) {
                    if (_preserveValues) {
                        endObject[aliasItem.qualifiedName()] = value;
                    } else {
                        endObject[aliasItem.qualifiedName()] = value.getValue();
                    }
                }
            }
            data.addItem(endObject);
        }
        return new ServiceData(data);
    }

    private function processListData(event:ResultEvent):void {
        var listData:ListDataResults = dataRemoteSource.list.lastResult as ListDataResults;
        if (listData.invalidAnalysisItemIDs != null && listData.invalidAnalysisItemIDs.length > 0) {
            dispatchEvent(new InvalidFieldsEvent(listData.invalidAnalysisItemIDs, listData.feedMetadata));
        }
        var serviceData:ServiceData = translate(listData, report);
        listData.additionalProperties["iframeKey"] = listData.cacheForHTMLKey;
        dispatchEvent(new DataServiceEvent(DataServiceEvent.DATA_RETURNED, serviceData.data, 
                listData.dataSourceInfo, listData.additionalProperties, listData.auditMessages, listData.reportFault,
                listData.limitedResults, listData.maxResults, listData.limitResults, listData.suggestions, serviceData.data.length > 0, listData.report, listData.fieldEvents,
        listData.filterEvents));
        dispatchEvent(new DataServiceLoadingEvent(DataServiceLoadingEvent.LOADING_STOPPED));
    }

    private function processMoreListData(event:ResultEvent):void {
        var listData:ListDataResults = dataRemoteSource.moreResults.lastResult as ListDataResults;
        if (listData.invalidAnalysisItemIDs != null && listData.invalidAnalysisItemIDs.length > 0) {
            dispatchEvent(new InvalidFieldsEvent(listData.invalidAnalysisItemIDs, listData.feedMetadata));
        }
        var serviceData:ServiceData = translate(listData, report);
        dispatchEvent(new DataServiceEvent(DataServiceEvent.DATA_RETURNED, serviceData.data,
                listData.dataSourceInfo, listData.additionalProperties, listData.auditMessages, listData.reportFault,
                listData.limitedResults, listData.maxResults, listData.limitResults, listData.suggestions, serviceData.data.length > 0, listData.report, listData.fieldEvents,
        listData.filterEvents));
        dispatchEvent(new DataServiceLoadingEvent(DataServiceLoadingEvent.LOADING_STOPPED));
    }

    private var report:AnalysisDefinition;

    private function retrieve():void {
        
    }

    public function retrieveData(definition:AnalysisDefinition, refreshAllSources:Boolean, requestParams:RequestParams):void {

        this.report = definition;
        dispatchEvent(new DataServiceLoadingEvent(DataServiceLoadingEvent.LOADING_STARTED));
        var metadata:InsightRequestMetadata = new InsightRequestMetadata();
        metadata.cacheForHTML = true;
        metadata.utcOffset = new Date().getTimezoneOffset();
        metadata.refreshAllSources = refreshAllSources;
        if (requestParams.uid == null) {
            dataRemoteSource.list.send(definition, metadata, requestParams != null && requestParams.showAll);
        } else {
            dataRemoteSource.moreResults.send(definition, metadata, requestParams.uid);
        }

    }
}
}