package com.easyinsight.genredata {
import com.easyinsight.dashboard.DashboardDescriptor;
import com.easyinsight.framework.PerspectiveInfo;
import com.easyinsight.listing.ListingChangeEvent;
import com.easyinsight.quicksearch.EIDescriptor;
import com.easyinsight.report.ReportAnalyzeSource;
import com.easyinsight.solutions.DataSourceDescriptor;
import com.easyinsight.solutions.InsightDescriptor;
import com.easyinsight.util.PopUpUtil;
import com.easyinsight.util.ProgressAlert;

import flash.display.DisplayObject;
import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;
import mx.controls.Alert;
import mx.core.Application;
import mx.managers.PopUpManager;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class DelayedReportTemplate extends EventDispatcher {

    private var urlKey:String;
    private var info:ReportTemplateInfo;

    private var solutionService:RemoteObject;

    public function DelayedReportTemplate(urlKey:String) {
        this.urlKey = urlKey;
        solutionService = new RemoteObject();
        solutionService.destination = "solutionService";
        solutionService.determineDataSourceForURLKey.addEventListener(ResultEvent.RESULT, onDetermine);
        solutionService.installEntity.addEventListener(ResultEvent.RESULT, installedReport);
    }

    public function execute():void {
        solutionService.determineDataSourceForURLKey.send(urlKey);
    }

    private function onDetermine(event:ResultEvent):void {
        info = solutionService.determineDataSourceForURLKey.lastResult as ReportTemplateInfo;
        if (info == null) {
            Alert.show("The requested report template URL was not found.");
            return;
        }
        var dataSources:ArrayCollection = info.dataSources;
        var validDataSources:ArrayCollection = new ArrayCollection();
        for each (var ds:DataSourceDescriptor in dataSources) {
            if (ds.prebuilts == null || ds.prebuilts.length == 0) {
                validDataSources.addItem(ds);
            }
        }
        if (validDataSources.length == 0 && dataSources.length == 1) {
            Alert.show("You've already installed this prebuilt onto your data source.");
        } else if (validDataSources.length == 0 && dataSources.length > 1) {
            Alert.show("You've already installed this prebuilt onto all of your data sources.");
        } else if (dataSources.length == 0) {
            var window:NoSolutionInstalledWindow = new NoSolutionInstalledWindow();
            window.solution = info.exchangeData.solutionID;
            window.addEventListener(ListingChangeEvent.LISTING_CHANGE, onListingEvent);
            PopUpManager.addPopUp(window, Application.application as DisplayObject, true);
            PopUpUtil.centerPopUp(window);
        } else if (validDataSources.length == 1) {
            var dsd:DataSourceDescriptor = dataSources.getItemAt(0) as DataSourceDescriptor;
            ProgressAlert.alert(Application.application as DisplayObject, "Installing...", null, solutionService.installEntity);
            solutionService.installEntity.send(info.exchangeData.descriptor, dsd.id);
        } else {
            var dsWindow:DataSourceChoiceWindow = new DataSourceChoiceWindow();
            dsWindow.sources = validDataSources;
            dsWindow.addEventListener(DataSourceSelectionEvent.DATA_SOURCE_SELECTION, dataSourceChoice, false, 0, true);
            PopUpManager.addPopUp(dsWindow, Application.application as DisplayObject, true);
            PopUpUtil.centerPopUp(dsWindow);
        }
    }

    private function onListingEvent(event:ListingChangeEvent):void {
        dispatchEvent(event);
    }

    private function dataSourceChoice(event:DataSourceSelectionEvent):void {
        ProgressAlert.alert(Application.application as DisplayObject, "Installing...", null, solutionService.installEntity);
        solutionService.installEntity.send(info.exchangeData.descriptor, event.dataSource.id);
    }

    private function installedReport(event:ResultEvent):void {
        
        var descriptor:EIDescriptor = solutionService.installEntity.lastResult as EIDescriptor;
        if (descriptor is InsightDescriptor) {
            var insightDescriptor:InsightDescriptor = descriptor as InsightDescriptor;
            dispatchEvent(new AnalyzeEvent(new ReportAnalyzeSource(insightDescriptor)));
        } else if (descriptor is DashboardDescriptor ){
            dispatchEvent(new AnalyzeEvent(new PerspectiveInfo(PerspectiveInfo.DASHBOARD_VIEW, {dashboardID: descriptor.id, exchangeItem: info.exchangeData})));
        }
    }
}
}