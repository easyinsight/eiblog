package com.easyinsight.analysis.definitions;

import com.easyinsight.analysis.AnalysisTypes;
import com.easyinsight.analysis.DiagramLink;

import java.text.MessageFormat;
import java.util.List;

/**
 * User: jamesboe
 * Date: 10/5/11
 * Time: 2:00 PM
 */
public class WSDiagramDefinition extends WSKPIDefinition {

    private long diagramReportID;
    private List<DiagramLink> links;

    public List<DiagramLink> getLinks() {
        return links;
    }

    public void setLinks(List<DiagramLink> links) {
        this.links = links;
    }

    public long getDiagramReportID() {
        return diagramReportID;
    }

    public void setDiagramReportID(long diagramReportID) {
        this.diagramReportID = diagramReportID;
    }

    @Override
    public String getDataFeedType() {
        return AnalysisTypes.DIAGRAM;
    }

    @Override
    public String toHTML(String targetDiv) {
        String timezoneOffset = "timezoneOffset='+new Date().getTimezoneOffset()+'";
        String format = "$.getJSON(''/app/diagramChart?reportID={0}&{1}&''+ strParams, function(data) '{' window.drawDiagram(data, $(\"#{2}\"), ''{3}'') '}');";
        return MessageFormat.format(format, getAnalysisID(), timezoneOffset, targetDiv, getAnalysisID());
    }
}
