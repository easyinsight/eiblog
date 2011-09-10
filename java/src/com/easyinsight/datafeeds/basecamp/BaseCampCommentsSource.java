package com.easyinsight.datafeeds.basecamp;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.storage.IDataStorage;
import com.easyinsight.users.Token;
import com.easyinsight.users.TokenStorage;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import org.apache.commons.httpclient.HttpClient;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.text.DateFormat;
import java.util.*;

/**
 * User: James Boe
 * Date: Jun 16, 2009
 * Time: 9:36:57 PM
 */
public class BaseCampCommentsSource extends BaseCampBaseSource {

    public static final String COMMENT_AUTHOR = "Comment Author";
    public static final String MILESTONE_ID = "Milestone ID";
    public static final String COMMENT_BODY = "Comment Body";
    public static final String COMMENT_ID = "Comment ID";
    public static final String COMMENT_CREATED_ON = "Comment Created On";

    public static final String COUNT = "Count";

    public BaseCampCommentsSource() {
        setFeedName("Comments");
    }

    @NotNull
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return Arrays.asList(COUNT, COMMENT_AUTHOR, MILESTONE_ID, COMMENT_BODY, COMMENT_ID, COMMENT_CREATED_ON);
    }

    public FeedType getFeedType() {
        return FeedType.BASECAMP_COMMENTS;
    }

    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();
        analysisItems.add(new AnalysisDimension(keys.get(COMMENT_AUTHOR), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMMENT_ID), true));
        analysisItems.add(new AnalysisDimension(keys.get(MILESTONE_ID), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMMENT_BODY), true));
        AnalysisDateDimension commentCreatedOnDim = new AnalysisDateDimension(keys.get(COMMENT_CREATED_ON), true, AnalysisDateDimension.DAY_LEVEL);
        analysisItems.add(commentCreatedOnDim);              
        analysisItems.add(new AnalysisMeasure(keys.get(COUNT), AggregationTypes.SUM));
        return analysisItems;
    }

    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) {
        DataSet ds = new DataSet();
        BaseCampCompositeSource source = (BaseCampCompositeSource) parentDefinition;
        if (source.isIncludeMilestoneComments()) {

        String url = source.getUrl();
        DateFormat df = new XmlSchemaDateFormat();


        Token token = new TokenStorage().getToken(SecurityUtil.getUserID(), TokenStorage.BASECAMP_TOKEN, parentDefinition.getDataFeedID(), false, conn);
        HttpClient client = getHttpClient(token.getTokenValue(), "");

        Builder builder = new Builder();
        try {
            BaseCampCache basecampCache = source.getOrCreateCache(client);
            Document projects = runRestRequest("/projects.xml", client, builder, url, null, true, parentDefinition, false);
            Nodes projectNodes = projects.query("/projects/project");
            for(int i = 0;i < projectNodes.size();i++) {
                Node curProject = projectNodes.get(i);
                String projectName = queryField(curProject, "name/text()");                                
                loadingProgress(i, projectNodes.size(), "Synchronizing with comments of " + projectName + "...", callDataID);
                String projectStatus = queryField(curProject, "status/text()");
                if (!source.isIncludeArchived() && "archived".equals(projectStatus)) {
                    continue;
                }
                if (!source.isIncludeInactive() && "inactive".equals(projectStatus)) {
                    continue;
                }
                String projectIdToRetrieve = queryField(curProject, "id/text()");

                Document milestoneList = runRestRequest("/projects/" + projectIdToRetrieve + "/milestones/list", client, builder, url, null, false, parentDefinition, false);

                Nodes milestoneCacheNodes = milestoneList.query("/milestones/milestone");
                for (int milestoneIndex = 0; milestoneIndex < milestoneCacheNodes.size(); milestoneIndex++) {
                    Node milestoneNode = milestoneCacheNodes.get(milestoneIndex);
                    String milestoneID = queryField(milestoneNode, "id/text()");
                    Document comments = runRestRequest("/milestones/" + milestoneID + "/comments.xml", client, builder, url, null, false, parentDefinition, false);
                    Nodes commentNodes = comments.query("/comments/comment");
                    for (int j = 0; j < commentNodes.size(); j++) {
                        Node commentNode = commentNodes.get(j);
                        String commentID = queryField(commentNode, "id/text()");
                        String authorName = basecampCache.getUserName(queryField(commentNode, "author-id"));
                        String body = queryField(commentNode, "body/text()");
                        String createdDateString = queryField(commentNode, "created-at/text()");
                        Date createdDate = null;
                        if(createdDateString != null ) {
                            createdDate = df.parse(createdDateString);
                        }
                        IRow row = ds.createRow();
                        row.addValue(keys.get(COMMENT_ID), commentID);
                        row.addValue(keys.get(MILESTONE_ID), milestoneID);
                        row.addValue(keys.get(COMMENT_AUTHOR), authorName);
                        row.addValue(keys.get(COMMENT_BODY), body);
                        row.addValue(keys.get(COMMENT_CREATED_ON), createdDate);
                        row.addValue(keys.get(COUNT), 1);
                    }
                }
                IDataStorage.insertData(ds);
                ds = new DataSet();
            }
        } catch (ReportException re) {
            throw re;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        }
        return null;
    }





    @Override
    public int getVersion() {
        return 1;
    }
}