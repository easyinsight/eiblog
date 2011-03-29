package com.easyinsight.datafeeds.highrise;

import com.easyinsight.analysis.AnalysisDimension;
import com.easyinsight.analysis.AnalysisText;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.DataSourceMigration;
import com.easyinsight.datafeeds.FeedDefinition;

import java.util.Map;

/**
 * User: jamesboe
 * Date: Jun 24, 2010
 * Time: 10:08:53 AM
 */
public class HighRiseEmail1To2 extends DataSourceMigration {
    public HighRiseEmail1To2(FeedDefinition dataSource) {
        super(dataSource);
    }

    @Override
    public void migrate(Map<String, Key> keys, EIConnection conn) throws Exception {
        addAnalysisItem(new AnalysisDimension(new NamedKey(HighRiseEmailSource.EMAIL_CASE_ID)));
        addAnalysisItem(new AnalysisDimension(new NamedKey(HighRiseEmailSource.EMAIL_COMPANY_ID)));
        addAnalysisItem(new AnalysisDimension(new NamedKey(HighRiseEmailSource.EMAIL_DEAL_ID)));
        addAnalysisItem(new AnalysisDimension(new NamedKey(HighRiseEmailSource.EMAIL_ID)));
    }

    @Override
    public int fromVersion() {
        return 1;
    }

    @Override
    public int toVersion() {
        return 2;
    }
}