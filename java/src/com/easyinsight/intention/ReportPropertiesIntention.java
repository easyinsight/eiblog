package com.easyinsight.intention;

/**
 * User: jamesboe
 * Date: 9/18/11
 * Time: 2:28 PM
 */
public class ReportPropertiesIntention extends Intention {
    private boolean fullJoins;
    private boolean summaryRow;
    private boolean trendSetup;

    public boolean isTrendSetup() {
        return trendSetup;
    }

    public void setTrendSetup(boolean trendSetup) {
        this.trendSetup = trendSetup;
    }

    public boolean isSummaryRow() {
        return summaryRow;
    }

    public void setSummaryRow(boolean summaryRow) {
        this.summaryRow = summaryRow;
    }

    public boolean isFullJoins() {
        return fullJoins;
    }

    public void setFullJoins(boolean fullJoins) {
        this.fullJoins = fullJoins;
    }
}
