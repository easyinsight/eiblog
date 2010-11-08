package com.easyinsight.datafeeds.constantcontact;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.storage.DataStorage;
import nu.xom.*;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.*;

/**
 * User: jamesboe
 * Date: Nov 7, 2010
 * Time: 10:57:24 AM
 */
public class CCCampaignResultsSource extends ConstantContactBaseSource {

    public static final String CAMPAIGN_ID = "Source Campaign ID";
    public static final String CONTACT_ID = "Source Contact ID";
    public static final String SENT_COUNT = "Sent Count";
    public static final String OPEN_COUNT = "Open Count";
    public static final String CLICK_COUNT = "Click Count";
    public static final String BOUNCE_COUNT = "Bounce Count";
    public static final String FORWARD_COUNT = "Forward Count";
    public static final String OPT_OUT_COUNT = "Opt Out Count";
    public static final String SPAM_REPORT_COUNT = "Spam Report Count";
    public static final String EVENT_DATE = "Event Date";

    public CCCampaignResultsSource() {
        setFeedName("Campaign Results");
    }

    @NotNull
    @Override
    protected List<String> getKeys() {
        return Arrays.asList(CAMPAIGN_ID, CONTACT_ID, SENT_COUNT, CLICK_COUNT, OPEN_COUNT,
                BOUNCE_COUNT, FORWARD_COUNT, OPT_OUT_COUNT, SPAM_REPORT_COUNT, EVENT_DATE);
    }

    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, DataSet dataSet, Connection conn) {
        List<AnalysisItem> items = new ArrayList<AnalysisItem>();
        items.add(new AnalysisDimension(keys.get(CONTACT_ID), true));
        items.add(new AnalysisDimension(keys.get(CAMPAIGN_ID), true));
        items.add(new AnalysisDateDimension(keys.get(EVENT_DATE), true, AnalysisDateDimension.DAY_LEVEL));
        items.add(new AnalysisMeasure(keys.get(SENT_COUNT), AggregationTypes.SUM));
        items.add(new AnalysisMeasure(keys.get(OPEN_COUNT), AggregationTypes.SUM));
        items.add(new AnalysisMeasure(keys.get(CLICK_COUNT), AggregationTypes.SUM));
        items.add(new AnalysisMeasure(keys.get(BOUNCE_COUNT), AggregationTypes.SUM));
        items.add(new AnalysisMeasure(keys.get(FORWARD_COUNT), AggregationTypes.SUM));
        items.add(new AnalysisMeasure(keys.get(OPT_OUT_COUNT), AggregationTypes.SUM));
        items.add(new AnalysisMeasure(keys.get(SPAM_REPORT_COUNT), AggregationTypes.SUM));
        return items;
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.CONSTANT_CONTACT_CAMPAIGN_RESULTS;
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, DataStorage dataStorage, EIConnection conn) throws ReportException {
        try {
            ConstantContactCompositeSource ccSource = (ConstantContactCompositeSource) parentDefinition;
            DataSet dataSet = new DataSet();
            Document doc = query("https://api.constantcontact.com/ws/customers/" + ccSource.getCcUserName() + "/campaigns", ccSource.getTokenKey(), ccSource.getTokenSecret());
            boolean hasMoreCampaigns;
            do {
                hasMoreCampaigns = false;
                Nodes nodes = doc.query("/feed/entry");
                for (int i = 0; i < nodes.size(); i++) {
                    Node node = nodes.get(i);
                    String idString = node.query("id/text()").get(0).getValue();
                    String id = idString.split("/")[7];
                    String eventsURL = "http://api.constantcontact.com/ws/customers/" + ccSource.getCcUserName() + "/campaigns/" + id + "/events/";
                    Document eventsDoc = query(eventsURL, ccSource.getTokenKey(), ccSource.getTokenSecret());
                    boolean hasMoreEvents;
                    do {
                        hasMoreEvents = false;
                        Nodes eventNodes = eventsDoc.query("/service/workspace/collection");
                        for (int j = 0; j < eventNodes.size(); j++) {
                            Element eventElement = (Element) eventNodes.get(j);
                            Attribute attribute = eventElement.getAttribute("href");
                            Document eventDetailDoc = query("https://api.constantcontact.com" + attribute.getValue(), ccSource.getTokenKey(), ccSource.getTokenSecret());
                            Nodes sendNodes = eventDetailDoc.query("/feed/entry/content/SentEvent");
                            for (int k = 0; k < sendNodes.size(); k++) {
                                Node sendNode = sendNodes.get(k);
                                Element contact = (Element) sendNode.query("Contact").get(0);
                                String contactIDString = contact.getAttribute("id").getValue();
                                IRow row = dataSet.createRow();
                                String contactID = contactIDString.split("/")[7];
                                row.addValue(CONTACT_ID, contactID);
                                row.addValue(CAMPAIGN_ID, id);
                                row.addValue(SENT_COUNT, 1);
                                row.addValue(EVENT_DATE, sendNode.query("EventTime/text()").get(0).getValue());
                            }
                            Nodes bounceNodes = eventDetailDoc.query("/feed/entry/content/BounceEvent");
                            for (int k = 0; k < bounceNodes.size(); k++) {
                                Node sendNode = bounceNodes.get(k);
                                Element contact = (Element) sendNode.query("Contact").get(0);
                                String contactIDString = contact.getAttribute("id").getValue();
                                IRow row = dataSet.createRow();
                                String contactID = contactIDString.split("/")[7];
                                row.addValue(CONTACT_ID, contactID);
                                row.addValue(CAMPAIGN_ID, id);
                                row.addValue(BOUNCE_COUNT, 1);
                                row.addValue(EVENT_DATE, sendNode.query("EventTime/text()").get(0).getValue());
                            }
                            Nodes openNodes = eventDetailDoc.query("/feed/entry/content/OpenEvent");
                            for (int k = 0; k < openNodes.size(); k++) {
                                Node sendNode = openNodes.get(k);
                                Element contact = (Element) sendNode.query("Contact").get(0);
                                String contactIDString = contact.getAttribute("id").getValue();
                                IRow row = dataSet.createRow();
                                String contactID = contactIDString.split("/")[7];
                                row.addValue(CONTACT_ID, contactID);
                                row.addValue(CAMPAIGN_ID, id);
                                row.addValue(OPEN_COUNT, 1);
                                row.addValue(EVENT_DATE, sendNode.query("EventTime/text()").get(0).getValue());
                            }
                            Nodes clickNodes = eventDetailDoc.query("/feed/entry/content/ClickEvent");
                            for (int k = 0; k < clickNodes.size(); k++) {
                                Node sendNode = clickNodes.get(k);
                                Element contact = (Element) sendNode.query("Contact").get(0);
                                String contactIDString = contact.getAttribute("id").getValue();
                                IRow row = dataSet.createRow();
                                String contactID = contactIDString.split("/")[7];
                                row.addValue(CONTACT_ID, contactID);
                                row.addValue(CAMPAIGN_ID, id);
                                row.addValue(CLICK_COUNT, 1);
                                row.addValue(EVENT_DATE, sendNode.query("EventTime/text()").get(0).getValue());
                            }
                            Nodes forwardNodes = eventDetailDoc.query("/feed/entry/content/ForwardEvent");
                            for (int k = 0; k < forwardNodes.size(); k++) {
                                Node sendNode = forwardNodes.get(k);
                                Element contact = (Element) sendNode.query("Contact").get(0);
                                String contactIDString = contact.getAttribute("id").getValue();
                                IRow row = dataSet.createRow();
                                String contactID = contactIDString.split("/")[7];
                                row.addValue(CONTACT_ID, contactID);
                                row.addValue(CAMPAIGN_ID, id);
                                row.addValue(FORWARD_COUNT, 1);
                                row.addValue(EVENT_DATE, sendNode.query("EventTime/text()").get(0).getValue());
                            }
                            Nodes optOutNodes = eventDetailDoc.query("/feed/entry/content/OptOutEvent");
                            for (int k = 0; k < optOutNodes.size(); k++) {
                                Node sendNode = optOutNodes.get(k);
                                Element contact = (Element) sendNode.query("Contact").get(0);
                                String contactIDString = contact.getAttribute("id").getValue();
                                IRow row = dataSet.createRow();
                                String contactID = contactIDString.split("/")[7];
                                row.addValue(CONTACT_ID, contactID);
                                row.addValue(CAMPAIGN_ID, id);
                                row.addValue(OPT_OUT_COUNT, 1);
                                row.addValue(EVENT_DATE, sendNode.query("EventTime/text()").get(0).getValue());
                            }
                        }

                        Nodes links = doc.query("/feed/link");

                        for (int j = 0; j < links.size(); j++) {
                            Element link = (Element) links.get(j);
                            Attribute attribute = link.getAttribute("rel");
                            if (attribute != null && "next".equals(attribute.getValue())) {
                                String linkURL = link.getAttribute("href").getValue();
                                hasMoreEvents = true;
                                doc = query("https://api.constantcontact.com" + linkURL, ccSource.getTokenKey(), ccSource.getTokenSecret());
                                break;
                            }
                        }

                    } while (hasMoreEvents);
                }
                Nodes links = doc.query("/feed/link");

                for (int i = 0; i < links.size(); i++) {
                    Element link = (Element) links.get(i);
                    Attribute attribute = link.getAttribute("rel");
                    if (attribute != null && "next".equals(attribute.getValue())) {
                        String linkURL = link.getAttribute("href").getValue();
                        hasMoreCampaigns = true;
                        doc = query("https://api.constantcontact.com" + linkURL, ccSource.getTokenKey(), ccSource.getTokenSecret());
                        break;
                    }
                }
            } while (hasMoreCampaigns);
            return dataSet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
