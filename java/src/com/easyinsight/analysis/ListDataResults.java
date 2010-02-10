package com.easyinsight.analysis;

import java.util.Map;

import java.io.Serializable;

public class ListDataResults extends DataResults implements Serializable {
    private AnalysisItem[] headers;
    private Map<String, String> groupings;
    private AnalysisItemResultMetadata[] headerMetadata;
    private ListRow[] rows;
    private boolean limitedResults;
    private int limitResults;
    private int maxResults;

    public Map<String, String> getGroupings() {
        return groupings;
    }

    public void setGroupings(Map<String, String> groupings) {
        this.groupings = groupings;
    }

    public boolean isLimitedResults() {
        return limitedResults;
    }

    public void setLimitedResults(boolean limitedResults) {
        this.limitedResults = limitedResults;
    }

    public int getLimitResults() {
        return limitResults;
    }

    public void setLimitResults(int limitResults) {
        this.limitResults = limitResults;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public AnalysisItemResultMetadata[] getHeaderMetadata() {
        return headerMetadata;
    }

    public void setHeaderMetadata(AnalysisItemResultMetadata[] headerMetadata) {
        this.headerMetadata = headerMetadata;
    }

    public AnalysisItem[] getHeaders() {
        return headers;
    }

    public void setHeaders(AnalysisItem[] headers) {
        this.headers = headers;
    }

    public ListRow[] getRows() {
        return rows;
    }

    public void setRows(ListRow[] rows) {
        this.rows = rows;
    }

    public void toBlah() {
        String[][] grid = new String[headers.length][rows.length + 1];
        for (int x = 0; x < headers.length; x++) {
            grid[x][0] = headers[x].getKey().toDisplayName();
            for (int y = 0; y < rows.length; y++) {
                ListRow listRow = rows[y];
                grid[x][y + 1] = listRow.getValues()[x].toString(); 
            }
        }
    }

    @Override
    public EmbeddedResults toEmbeddedResults() {
        EmbeddedDataResults embeddedDataResults = new EmbeddedDataResults();
        embeddedDataResults.setHeaders(headers);
        embeddedDataResults.setRows(rows);
        embeddedDataResults.setAdditionalProperties(getAdditionalProperties());
        return embeddedDataResults;
    }
}
