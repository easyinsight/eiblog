package com.easyinsight.datafeeds.harvest;

import com.easyinsight.analysis.AnalysisDimension;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.DataSourceMigration;
import com.easyinsight.datafeeds.FeedDefinition;

import java.util.Map;

/**
 * User: jamesboe
 * Date: 2/21/12
 * Time: 4:26 PM
 */
public class HarvestProject1To2 extends DataSourceMigration {
    public HarvestProject1To2(FeedDefinition dataSource) {
        super(dataSource);
    }

    @Override
    public void migrate(Map<String, Key> keys, EIConnection conn) throws Exception {
        addAnalysisItem(new AnalysisDimension(new NamedKey(HarvestProjectSource.BASECAMP_ID), true));
        addAnalysisItem(new AnalysisDimension(new NamedKey(HarvestProjectSource.HIGHRISE_ID), true));
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
