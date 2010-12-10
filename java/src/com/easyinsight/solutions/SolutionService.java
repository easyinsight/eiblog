package com.easyinsight.solutions;

import com.easyinsight.core.EIDescriptor;
import com.easyinsight.dashboard.Dashboard;
import com.easyinsight.dashboard.DashboardDescriptor;
import com.easyinsight.dashboard.DashboardStorage;
import com.easyinsight.datafeeds.*;
import com.easyinsight.analysis.*;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.export.ExportService;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.security.AuthorizationManager;
import com.easyinsight.security.AuthorizationRequirement;
import com.easyinsight.goals.*;
import com.easyinsight.core.InsightDescriptor;
import com.easyinsight.core.Key;
import com.easyinsight.core.DataSourceDescriptor;
import com.easyinsight.users.Account;
import com.easyinsight.exchange.SolutionReportExchangeItem;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.io.ByteArrayInputStream;

import org.hibernate.Session;

/**
 * User: James Boe
 * Date: Aug 28, 2008
 * Time: 12:32:59 AM
 */
public class SolutionService {

    public void addKPIData(SolutionKPIData solutionKPIData) {
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(solutionKPIData.getDataSourceID(), conn);
            dataSource.setAccountVisible(true);
            new FeedStorage().updateDataFeedConfiguration(dataSource, conn);
            if (solutionKPIData.getActivity() != null) {
                PreparedStatement queryStmt = conn.prepareStatement("SELECT SCHEDULED_DATA_SOURCE_REFRESH.SCHEDULED_DATA_SOURCE_REFRESH_ID FROM SCHEDULED_DATA_SOURCE_REFRESH, DATA_FEED, SCHEDULED_ACCOUNT_ACTIVITY WHERE " +
                        "SCHEDULED_DATA_SOURCE_REFRESH.data_source_id = data_feed.data_feed_id and data_feed.feed_type = ? AND " +
                        "scheduled_data_source_refresh.scheduled_account_activity_id = scheduled_account_activity.scheduled_account_activity_id and " +
                        "scheduled_account_activity.account_id = ?");
                queryStmt.setInt(1, dataSource.getFeedType().getType());
                queryStmt.setLong(2, SecurityUtil.getAccountID());
                ResultSet rs = queryStmt.executeQuery();
                if (!rs.next()) {
                    new ExportService().addOrUpdateSchedule(solutionKPIData.getActivity(), solutionKPIData.getUtcOffset(), conn);
                }
            }
            conn.commit();
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public List<Long> getInstalledConnections() {
        EIConnection conn = Database.instance().getConnection();
        try {
            Set<Long> connectionIDs = new HashSet<Long>();
            PreparedStatement connUserStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.SOLUTION_ID FROM SOLUTION_INSTALL, UPLOAD_POLICY_USERS, DATA_FEED WHERE " +
                    "UPLOAD_POLICY_USERS.USER_ID = ? AND UPLOAD_POLICY_USERS.feed_id = SOLUTION_INSTALL.installed_data_source_id AND " +
                    "SOLUTION_INSTALL.installed_data_source_id = DATA_FEED.DATA_FEED_ID AND DATA_FEED.VISIBLE = ?");
            connUserStmt.setLong(1, SecurityUtil.getUserID());
            connUserStmt.setBoolean(2, true);
            ResultSet userRS = connUserStmt.executeQuery();
            while (userRS.next()) {
                long connectionID = userRS.getLong(1);
                connectionIDs.add(connectionID);
            }
            PreparedStatement connGroupStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.SOLUTION_ID FROM SOLUTION_INSTALL, UPLOAD_POLICY_GROUPS," +
                    "GROUP_TO_USER_JOIN, DATA_FEED WHERE GROUP_TO_USER_JOIN.USER_ID = ? AND GROUP_TO_USER_JOIN.group_id = UPLOAD_POLICY_GROUPS.group_id AND " +
                    "UPLOAD_POLICY_GROUPS.feed_id = SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID AND SOLUTION_INSTALL.installed_data_source_id = DATA_FEED.DATA_FEED_ID AND DATA_FEED.VISIBLE = ?");
            connGroupStmt.setLong(1, SecurityUtil.getUserID());
            connGroupStmt.setBoolean(2, true);
            ResultSet groupRS = connGroupStmt.executeQuery();
            while (groupRS.next()) {
                long connectionID = groupRS.getLong(1);
                connectionIDs.add(connectionID);
            }
            PreparedStatement connAccountStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.SOLUTION_ID FROM SOLUTION_INSTALL, UPLOAD_POLICY_USERS, DATA_FEED, USER WHERE " +
                    "UPLOAD_POLICY_USERS.USER_ID = USER.USER_ID AND USER.ACCOUNT_ID = ? AND UPLOAD_POLICY_USERS.feed_id = SOLUTION_INSTALL.installed_data_source_id AND " +
                    "SOLUTION_INSTALL.installed_data_source_id = DATA_FEED.DATA_FEED_ID AND DATA_FEED.VISIBLE = ? AND DATA_FEED.ACCOUNT_VISIBLE = ?");
            connAccountStmt.setLong(1, SecurityUtil.getAccountID());
            connAccountStmt.setBoolean(2, true);
            connAccountStmt.setBoolean(3, true);
            ResultSet acountRS = connAccountStmt.executeQuery();
            while (acountRS.next()) {
                long connectionID = acountRS.getLong(1);
                connectionIDs.add(connectionID);
            }
            return new ArrayList<Long>(connectionIDs);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public boolean alreadyHasConnection(long connectionID) {
        List<DataSourceDescriptor> descriptors = new ArrayList<DataSourceDescriptor>();
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement dsQueryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.ORIGINAL_DATA_SOURCE_ID FROM " +
                    "SOLUTION_INSTALL WHERE SOLUTION_INSTALL.solution_id = ?");
            dsQueryStmt.setLong(1, connectionID);
            ResultSet dsRS = dsQueryStmt.executeQuery();
            if (dsRS.next()) {
                long originalDataSourceID = dsRS.getLong(1);
                PreparedStatement queryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.installed_data_source_id, DATA_FEED.FEED_NAME, DATA_FEED.FEED_TYPE FROM " +
                    "SOLUTION_INSTALL, DATA_FEED, UPLOAD_POLICY_USERS WHERE " +
                    "SOLUTION_INSTALL.original_data_source_id = ? AND " +
                    "SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID = DATA_FEED.DATA_FEED_ID AND " +
                    "UPLOAD_POLICY_USERS.USER_ID = ? AND DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_USERS.FEED_ID AND DATA_FEED.visible = ?");
                queryStmt.setLong(1, originalDataSourceID);
                queryStmt.setLong(2, SecurityUtil.getUserID());
                queryStmt.setBoolean(3, true);
                ResultSet rs = queryStmt.executeQuery();
                while (rs.next()) {
                    long id = rs.getLong(1);
                    String name = rs.getString(2);
                    int feedType = rs.getInt(3);
                    descriptors.add(new DataSourceDescriptor(name, id, feedType));
                }
                if (descriptors.isEmpty()) {
                    PreparedStatement groupQueryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID, DATA_FEED.FEED_NAME, DATA_FEED.FEED_TYPE FROM " +
                            "SOLUTION_INSTALL, DATA_FEED, UPLOAD_POLICY_GROUPS, GROUP_TO_USER_JOIN WHERE " +
                            "SOLUTION_INSTALL.ORIGINAL_DATA_SOURCE_ID = ? AND " +
                            "SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID = DATA_FEED.DATA_FEED_ID AND " +
                            "DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_GROUPS.FEED_ID AND UPLOAD_POLICY_GROUPS.group_id = group_to_user_join.group_id AND " +
                            "group_to_user_join.user_id = ? AND DATA_FEED.VISIBLE = ?");
                    groupQueryStmt.setLong(1, originalDataSourceID);
                    groupQueryStmt.setLong(2, SecurityUtil.getUserID());
                    groupQueryStmt.setBoolean(3, true);
                    rs = queryStmt.executeQuery();
                    while (rs.next()) {
                        long id = rs.getLong(1);
                        String name = rs.getString(2);
                        int feedType = rs.getInt(3);
                        descriptors.add(new DataSourceDescriptor(name, id, feedType));
                    }
                }
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return !descriptors.isEmpty();
    }

    public Solution retrieveSolution(long solutionID) {
        try {
            return getSolution(solutionID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void deleteSolution(long solutionID) {
        SecurityUtil.authorizeAccountTier(Account.ADMINISTRATOR);
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM SOLUTION WHERE SOLUTION_ID = ?");
            stmt.setLong(1, solutionID);
            stmt.executeUpdate();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public byte[] getSolutionArchive(long solutionID) {
        byte[] bytes;
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT ARCHIVE, SOLUTION_ARCHIVE_NAME FROM SOLUTION WHERE SOLUTION_ID = ?");
            queryStmt.setLong(1, solutionID);
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                bytes = rs.getBytes(1);
            } else {
                throw new RuntimeException("No data found for that file ID.");
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return bytes;
    }

    public void addSolutionArchive(byte[] archive, long solutionID, String solutionArchiveName) {
        SecurityUtil.authorizeAccountTier(Account.ADMINISTRATOR);
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement updateArchiveStmt = conn.prepareStatement("UPDATE SOLUTION SET ARCHIVE = ?, SOLUTION_ARCHIVE_NAME = ? WHERE SOLUTION_ID = ?");
            ByteArrayInputStream bais = new ByteArrayInputStream(archive);

            updateArchiveStmt.setBinaryStream(1, bais, archive.length);
            //updateArchiveStmt.setBytes(1, archive);
            updateArchiveStmt.setString(2, solutionArchiveName);
            updateArchiveStmt.setLong(3, solutionID);
            updateArchiveStmt.executeUpdate();
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public long addSolution(Solution solution, List<Integer> feedIDs) {
        long userID = SecurityUtil.getUserID();
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement insertSolutionStmt = conn.prepareStatement("INSERT INTO SOLUTION (NAME, DESCRIPTION, INDUSTRY, AUTHOR, COPY_DATA, goal_tree_id, " +
                    "SOLUTION_TIER, CATEGORY, screencast_directory, screencast_mp4_name, footer_text, logo_link, detail_page_class) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            insertSolutionStmt.setString(1, solution.getName());
            insertSolutionStmt.setString(2, solution.getDescription());
            insertSolutionStmt.setString(3, solution.getIndustry());
            insertSolutionStmt.setString(4, solution.getAuthor());
            insertSolutionStmt.setBoolean(5, solution.isCopyData());
            if (solution.getGoalTreeID() == 0) {
                insertSolutionStmt.setNull(6, Types.BIGINT);
            } else {
                insertSolutionStmt.setLong(6, solution.getGoalTreeID());
            }
            insertSolutionStmt.setInt(7, solution.getSolutionTier());
            insertSolutionStmt.setInt(8, solution.getCategory());
            insertSolutionStmt.setString(9, solution.getScreencastDirectory());
            insertSolutionStmt.setString(10, solution.getScreencastName());
            insertSolutionStmt.setString(11, solution.getFooterText());
            insertSolutionStmt.setString(12, solution.getLogoLink());
            insertSolutionStmt.setString(13, solution.getDetailPageClass());
            insertSolutionStmt.execute();
            long solutionID = Database.instance().getAutoGenKey(insertSolutionStmt);
            PreparedStatement addRoleStmt = conn.prepareStatement("INSERT INTO USER_TO_SOLUTION (USER_ID, SOLUTION_ID, USER_ROLE) VALUES (?, ?, ?)");
            addRoleStmt.setLong(1, userID);
            addRoleStmt.setLong(2, solutionID);
            addRoleStmt.setInt(3, SolutionRoles.OWNER);
            addRoleStmt.execute();
            PreparedStatement addFeedStmt = conn.prepareStatement("INSERT INTO SOLUTION_TO_FEED (FEED_ID, SOLUTION_ID) VALUES (?, ?)");
            for (Integer feedID : feedIDs) {
                addFeedStmt.setLong(1, feedID);
                addFeedStmt.setLong(2, solutionID);
                addFeedStmt.execute();
            }
            conn.commit();
            return solutionID;
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(false);
            Database.closeConnection(conn);
        }
    }

    public void updateSolution(Solution solution, List<Integer> feedIDs) {
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement updateSolutionStmt = conn.prepareStatement("UPDATE SOLUTION SET NAME = ?, DESCRIPTION = ?, INDUSTRY = ?, AUTHOR = ?, " +
                    "COPY_DATA = ?, GOAL_TREE_ID = ?, SOLUTION_TIER = ?, CATEGORY = ?, screencast_directory = ?, screencast_mp4_name = ?, footer_text = ?," +
                    "logo_link = ?, DETAIL_PAGE_CLASS = ? WHERE SOLUTION_ID = ?",
                    Statement.RETURN_GENERATED_KEYS);
            updateSolutionStmt.setString(1, solution.getName());
            updateSolutionStmt.setString(2, solution.getDescription());
            updateSolutionStmt.setString(3, solution.getIndustry());
            updateSolutionStmt.setString(4, solution.getAuthor());
            updateSolutionStmt.setBoolean(5, solution.isCopyData());
            if (solution.getGoalTreeID() == 0) {
                updateSolutionStmt.setNull(6, Types.BIGINT);
            } else {
                updateSolutionStmt.setLong(6, solution.getGoalTreeID());
            }
            updateSolutionStmt.setInt(7, solution.getSolutionTier());
            updateSolutionStmt.setInt(8, solution.getCategory());
            updateSolutionStmt.setString(9, solution.getScreencastDirectory());
            updateSolutionStmt.setString(10, solution.getScreencastName());
            updateSolutionStmt.setString(11, solution.getFooterText());
            updateSolutionStmt.setString(12, solution.getLogoLink());
            updateSolutionStmt.setString(13, solution.getDetailPageClass());
            updateSolutionStmt.setLong(14, solution.getSolutionID());
            updateSolutionStmt.executeUpdate();
            PreparedStatement deleteFeedsStmt = conn.prepareStatement("DELETE FROM SOLUTION_TO_FEED WHERE SOLUTION_ID = ?");
            deleteFeedsStmt.setLong(1, solution.getSolutionID());
            deleteFeedsStmt.executeUpdate();
            PreparedStatement addFeedStmt = conn.prepareStatement("INSERT INTO SOLUTION_TO_FEED (FEED_ID, SOLUTION_ID) VALUES (?, ?)");
            for (Integer feedID : feedIDs) {
                addFeedStmt.setLong(1, feedID);
                addFeedStmt.setLong(2, solution.getSolutionID());
                addFeedStmt.execute();
            }
            conn.commit();
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public AuthorizationRequirement determineAuthorizationRequirements(int feedType, long solutionID) {
        FeedType type = new FeedType(feedType);
        return new AuthorizationManager().authorize(type, solutionID);        
    }

    public boolean connectionInstalled(long solutionID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.installed_data_source_id FROM " +
                    "SOLUTION_INSTALL, DATA_FEED, UPLOAD_POLICY_USERS WHERE " +
                    "SOLUTION_INSTALL.solution_id = ? AND " +
                    "SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID = DATA_FEED.DATA_FEED_ID AND " +
                    "UPLOAD_POLICY_USERS.USER_ID = ? AND DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_USERS.FEED_ID AND " +
                    "DATA_FEED.VISIBLE = ?");
            queryStmt.setLong(1, solutionID);
            queryStmt.setLong(2, SecurityUtil.getUserID(false));
            queryStmt.setBoolean(3, true);
            ResultSet rs = queryStmt.executeQuery();
            boolean valid = rs.next();
            if (!valid) {
                PreparedStatement groupQueryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID, DATA_FEED.FEED_NAME FROM " +
                            "SOLUTION_INSTALL, DATA_FEED, UPLOAD_POLICY_GROUPS, GROUP_TO_USER_JOIN WHERE " +
                            "SOLUTION_INSTALL.SOLUTION_ID = ? AND " +
                            "SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID = DATA_FEED.DATA_FEED_ID AND " +
                            "DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_GROUPS.FEED_ID AND UPLOAD_POLICY_GROUPS.group_id = group_to_user_join.group_id AND " +
                            "group_to_user_join.user_id = ? AND DATA_FEED.VISIBLE = ?");
                groupQueryStmt.setLong(1, solutionID);
                groupQueryStmt.setLong(2, SecurityUtil.getUserID(false));
                groupQueryStmt.setBoolean(3, true);
                rs = queryStmt.executeQuery();
                valid = rs.next();
            }
            return valid;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public List<DataSourceDescriptor> determineDataSourceForEntity(EIDescriptor descriptor) {
        EIConnection conn = Database.instance().getConnection();
        try {
            if (descriptor.getType() == EIDescriptor.REPORT) {
                return determineDataSourceForReport(descriptor.getId(), conn).descriptors;
            } else if (descriptor.getType() == EIDescriptor.DASHBOARD) {
                return determineDataSourceForDashboard(descriptor.getId(), conn).descriptors;
            } else {
                throw new UnsupportedOperationException("Unsupported exchange type " + descriptor.getType());
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    private Blah determineDataSourceForDashboard(long dashboardID, EIConnection conn) throws SQLException {
        List<DataSourceDescriptor> descriptors = new ArrayList<DataSourceDescriptor>();
        List<ExtraDataSourceInfo> extraInfos = new ArrayList<ExtraDataSourceInfo>();
        PreparedStatement dashboardQuery = conn.prepareStatement("SELECT DASHBOARD.data_source_id, DASHBOARD.dashboard_name," +
                "DASHBOARD.CREATION_DATE, DASHBOARD.DESCRIPTION, DASHBOARD.AUTHOR_NAME FROM DASHBOARD WHERE DASHBOARD_ID = ?");
        dashboardQuery.setLong(1, dashboardID);
        ResultSet dashboardRS = dashboardQuery.executeQuery();
        if (dashboardRS.next()) {
            long dataSourceID = dashboardRS.getLong(1);
            PreparedStatement dsQueryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.ORIGINAL_DATA_SOURCE_ID, SOLUTION_INSTALL.solution_id, SOLUTION.name FROM " +
                "SOLUTION_INSTALL, SOLUTION WHERE SOLUTION_INSTALL.installed_data_source_id = ? and solution_install.solution_id = solution.solution_id");
            dsQueryStmt.setLong(1, dataSourceID);
            ResultSet dsRS = dsQueryStmt.executeQuery();
            if (dsRS.next()) {
                long originalDataSourceID = dsRS.getLong(1);
                long connectionID = dsRS.getLong(2);
                String connectionName = dsRS.getString(3);

                determineDataSources(descriptors, extraInfos, conn, originalDataSourceID);
                if (descriptors.size() > 1) {
                    describe(descriptors, extraInfos, conn);
                }
                return new Blah(descriptors, connectionID, connectionName);
            }

        }
        return null;
    }

    private static class Blah {
        private List<DataSourceDescriptor> descriptors;
        private long connectionID;
        private String connectionName;

        private Blah(List<DataSourceDescriptor> descriptors, long connectionID, String connectionName) {
            this.descriptors = descriptors;
            this.connectionID = connectionID;
            this.connectionName = connectionName;
        }
    }

    public ReportTemplateInfo determineDataSourceForURLKey(String urlKey) {
        EIConnection conn = Database.instance().getConnection();
        try {
            ReportTemplateInfo reportTemplateInfo = new ReportTemplateInfo();
            SolutionReportExchangeItem srei;
            PreparedStatement reportQueryStmt = conn.prepareStatement("SELECT ANALYSIS_ID, DATA_FEED_ID, TITLE, CREATE_DATE, AUTHOR_NAME, DESCRIPTION " +
                    "FROM ANALYSIS WHERE ANALYSIS.solution_visible = ? AND ANALYSIS.url_key = ?");
            reportQueryStmt.setBoolean(1, true);
            reportQueryStmt.setString(2, urlKey);
            Blah blah;
            ResultSet rs = reportQueryStmt.executeQuery();
            if (rs.next()) {
                long reportID = rs.getLong(1);
                long dataSourceID = rs.getLong(2);
                String reportName = rs.getString(3);
                Date dateCreated = new Date(rs.getTimestamp(4).getTime());
                String authorName = rs.getString(5);
                String description = rs.getString(6);
                EIDescriptor descriptor = new InsightDescriptor(reportID, null, dataSourceID, 0, null);
                PreparedStatement ratingStmt = conn.prepareStatement("SELECT AVG(RATING), COUNT(RATING) FROM USER_REPORT_RATING WHERE REPORT_ID = ?");
                ratingStmt.setLong(1, reportID);
                ResultSet ratingRS = ratingStmt.executeQuery();
                ratingRS.next();
                double ratingAverage = ratingRS.getDouble(1);
                int ratingCount = ratingRS.getInt(2);
                blah = determineDataSourceForReport(reportID, conn);
                srei = new SolutionReportExchangeItem(reportName, reportID, ratingAverage, ratingCount, dateCreated, description,
                        authorName, descriptor, blah.connectionID, blah.connectionName);
            } else {
                PreparedStatement dashboardQueryStmt = conn.prepareStatement("SELECT DASHBOARD_ID, URL_KEY, DASHBOARD_NAME, CREATION_DATE," +
                        "AUTHOR_NAME, DESCRIPTION FROM DASHBOARD WHERE DASHBOARD.exchange_visible = ? and " +
                        "DASHBOARD.url_key = ?");
                dashboardQueryStmt.setBoolean(1, true);
                dashboardQueryStmt.setString(2, urlKey);
                ResultSet dashboardRS = dashboardQueryStmt.executeQuery();
                if (dashboardRS.next()) {
                    long dashboardID = dashboardRS.getLong(1);
                    String dashboardURLKey = dashboardRS.getString(2);
                    String dashboardName = dashboardRS.getString(3);
                    Date dateCreated = new Date(dashboardRS.getTimestamp(4).getTime());
                    String authorName = dashboardRS.getString(5);
                    String description = dashboardRS.getString(6);
                    EIDescriptor descriptor = new DashboardDescriptor(null, dashboardID, dashboardURLKey);
                    blah = determineDataSourceForDashboard(dashboardID, conn);
                    PreparedStatement ratingStmt = conn.prepareStatement("SELECT AVG(RATING), COUNT(RATING) FROM DASHBOARD_USER_RATING WHERE DASHBOARD_ID = ?");
                    ratingStmt.setLong(1, dashboardID);
                    ResultSet ratingRS = ratingStmt.executeQuery();
                    ratingRS.next();
                    double ratingAverage = ratingRS.getDouble(1);
                    int ratingCount = ratingRS.getInt(2);
                    srei = new SolutionReportExchangeItem(dashboardName, dashboardID, ratingAverage, ratingCount, dateCreated, description,
                            authorName, descriptor, blah.connectionID, blah.connectionName);
                } else {
                    return null;
                }
            }
            reportTemplateInfo.setExchangeData(srei);
            reportTemplateInfo.setDataSources(blah.descriptors);
            return reportTemplateInfo;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    private Blah determineDataSourceForReport(long reportID, EIConnection conn) throws SQLException {
        List<DataSourceDescriptor> descriptors = new ArrayList<DataSourceDescriptor>();
        List<ExtraDataSourceInfo> extraInfos = new ArrayList<ExtraDataSourceInfo>();
        PreparedStatement reportQuery = conn.prepareStatement("SELECT ANALYSIS.data_feed_id, ANALYSIS.url_key " +
                "FROM ANALYSIS WHERE ANALYSIS_ID = ?");
        reportQuery.setLong(1, reportID);
        ResultSet reportRS = reportQuery.executeQuery();
        if (reportRS.next()) {
            long dataSourceID = reportRS.getLong(1);
            PreparedStatement dsQueryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.ORIGINAL_DATA_SOURCE_ID, SOLUTION_INSTALL.solution_id," +
                    "SOLUTION.NAME FROM " +
                    "SOLUTION_INSTALL, SOLUTION WHERE SOLUTION_INSTALL.installed_data_source_id = ? and SOLUTION_INSTALL.solution_id = solution.solution_id");
            dsQueryStmt.setLong(1, dataSourceID);
            ResultSet dsRS = dsQueryStmt.executeQuery();
            long connectionID;
            String connectionName;
            if (dsRS.next()) {
                long originalDataSourceID = dsRS.getLong(1);
                connectionID = dsRS.getLong(2);
                connectionName = dsRS.getString(3);
                determineDataSources(descriptors, extraInfos, conn, originalDataSourceID);
                if (descriptors.size() > 1) {
                    describe(descriptors, extraInfos, conn);
                }
                return new Blah(descriptors, connectionID, connectionName);
            }
        }
        return null;
    }

    private void determineDataSources(List<DataSourceDescriptor> descriptors, List<ExtraDataSourceInfo> extraInfos, EIConnection conn, long originalDataSourceID) throws SQLException {
        PreparedStatement queryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.installed_data_source_id, DATA_FEED.FEED_NAME, DATA_FEED.FEED_TYPE, DATA_FEED.create_date FROM " +
            "SOLUTION_INSTALL, DATA_FEED, UPLOAD_POLICY_USERS WHERE " +
            "SOLUTION_INSTALL.original_data_source_id = ? AND " +
            "SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID = DATA_FEED.DATA_FEED_ID AND " +
            "UPLOAD_POLICY_USERS.USER_ID = ? AND DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_USERS.FEED_ID AND DATA_FEED.VISIBLE = ?");
        queryStmt.setLong(1, originalDataSourceID);
        queryStmt.setLong(2, SecurityUtil.getUserID());
        queryStmt.setBoolean(3, true);
        ResultSet rs = queryStmt.executeQuery();
        while (rs.next()) {
            long id = rs.getLong(1);
            String name = rs.getString(2);
            int feedType = rs.getInt(3);
            descriptors.add(new DataSourceDescriptor(name, id, feedType));
            extraInfos.add(new ExtraDataSourceInfo(rs.getTimestamp(4), feedType));
        }
        if (descriptors.isEmpty()) {
            PreparedStatement groupQueryStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID, DATA_FEED.FEED_NAME, DATA_FEED.FEED_TYPE," +
                    "DATA_FEED.create_date FROM " +
                    "SOLUTION_INSTALL, DATA_FEED, UPLOAD_POLICY_GROUPS, GROUP_TO_USER_JOIN WHERE " +
                    "SOLUTION_INSTALL.ORIGINAL_DATA_SOURCE_ID = ? AND " +
                    "SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID = DATA_FEED.DATA_FEED_ID AND " +
                    "DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_GROUPS.FEED_ID AND UPLOAD_POLICY_GROUPS.group_id = group_to_user_join.group_id AND " +
                    "group_to_user_join.user_id = ? AND DATA_FEED.VISIBLE = ?");
            groupQueryStmt.setLong(1, originalDataSourceID);
            groupQueryStmt.setLong(2, SecurityUtil.getUserID());
            groupQueryStmt.setBoolean(3, true);
            rs = queryStmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                String name = rs.getString(2);
                int feedType = rs.getInt(3);
                descriptors.add(new DataSourceDescriptor(name, id, feedType));
                extraInfos.add(new ExtraDataSourceInfo(rs.getTimestamp(4), feedType));
            }
        }
        if (descriptors.isEmpty()) {
            PreparedStatement queryAccountStmt = conn.prepareStatement("SELECT SOLUTION_INSTALL.installed_data_source_id, DATA_FEED.FEED_NAME, DATA_FEED.FEED_TYPE, DATA_FEED.create_date FROM " +
                "SOLUTION_INSTALL, DATA_FEED, UPLOAD_POLICY_USERS, USER WHERE " +
                "SOLUTION_INSTALL.original_data_source_id = ? AND " +
                "SOLUTION_INSTALL.INSTALLED_DATA_SOURCE_ID = DATA_FEED.DATA_FEED_ID AND " +
                "UPLOAD_POLICY_USERS.USER_ID = USER.USER_ID AND USER.ACCOUNT_ID = ? AND " +
                    "DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_USERS.FEED_ID AND DATA_FEED.VISIBLE = ? AND DATA_FEED.ACCOUNT_VISIBLE = ?");
            queryAccountStmt.setLong(1, originalDataSourceID);
            queryAccountStmt.setLong(2, SecurityUtil.getAccountID());
            queryAccountStmt.setBoolean(3, true);
            queryAccountStmt.setBoolean(4, true);
            ResultSet accountRS = queryAccountStmt.executeQuery();
            while (accountRS.next()) {
                long id = accountRS.getLong(1);
                String name = accountRS.getString(2);
                int feedType = accountRS.getInt(3);
                descriptors.add(new DataSourceDescriptor(name, id, feedType));
                extraInfos.add(new ExtraDataSourceInfo(accountRS.getTimestamp(4), feedType));
            }
        }
    }

    private static class ExtraDataSourceInfo {
        private Date creationDate;
        private int feedType;

        private ExtraDataSourceInfo(Date creationDate, int feedType) {
            this.creationDate = creationDate;
            this.feedType = feedType;
        }
    }

    private void describe(List<DataSourceDescriptor> descriptors, List<ExtraDataSourceInfo> extraInfos, EIConnection conn) throws SQLException {
        PreparedStatement queryReportCountStmt = conn.prepareStatement("SELECT COUNT(ANALYSIS_ID) FROM ANALYSIS WHERE " +
                "ANALYSIS.data_feed_id = ? AND ANALYSIS.temporary_report = ?");
        for (int i = 0; i < descriptors.size(); i++) {
            DataSourceDescriptor descriptor = descriptors.get(i); 
            ExtraDataSourceInfo extraInfo = extraInfos.get(i);
            String description;
            queryReportCountStmt.setLong(1, descriptor.getId());
            queryReportCountStmt.setBoolean(2, false);
            ResultSet rs = queryReportCountStmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            String dateString = SimpleDateFormat.getDateInstance().format(extraInfo.creationDate);
            if (count == 0) {
                description = "this data source was created on " + dateString + " and has no reports.";
            } else if (count == 1) {
                description = "this data source was created on " + dateString + " and has 1 report.";
            } else {
                description = "this data source was created on " + dateString + " and has " + count + " reports.";
            }
            descriptor.setDescription(description);
        }
    }

    public EIDescriptor installEntity(EIDescriptor descriptor, long dataSourceID) {
        if (descriptor.getType() == EIDescriptor.DASHBOARD) {
            return installDashboard(descriptor.getId(), dataSourceID);
        } else if (descriptor.getType() == EIDescriptor.REPORT) {
            return installReport(descriptor.getId(), dataSourceID);
        } else {
            throw new RuntimeException("Unknown descriptor type " + descriptor.getType());
        }
    }

    public DashboardDescriptor installDashboard(long dashboardID, long dataSourceID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            Session session = Database.instance().createSession(conn);
            DashboardStorage dashboardStorage = new DashboardStorage();
            FeedStorage feedStorage = new FeedStorage();
            Dashboard dashboard = dashboardStorage.getDashboard(dashboardID, conn);
            FeedDefinition sourceDataSource = feedStorage.getFeedDefinitionData(dashboard.getDataSourceID(), conn);
            Set<Long> reportIDs = dashboard.containedReports();
            List<AnalysisDefinition> reports = new ArrayList<AnalysisDefinition>();

            for (Long containedReportID : reportIDs) {
                reports.add(new AnalysisStorage().getPersistableReport(containedReportID, session));
            }

            Map<Long, AnalysisDefinition> reportReplacementMap = new HashMap<Long, AnalysisDefinition>();

            FeedDefinition targetDataSource = feedStorage.getFeedDefinitionData(dataSourceID, conn);
            Map<Key, Key> keyReplacementMap = createKeyReplacementMap(targetDataSource, sourceDataSource);
            
            List<AnalysisDefinition> reportList = new ArrayList<AnalysisDefinition>();
            for (AnalysisDefinition child : reports) {

                AnalysisDefinition copyReport = copyReportToDataSource(targetDataSource, child, keyReplacementMap);
                reportReplacementMap.put(child.getAnalysisID(), copyReport);
                reportList.add(copyReport);
            }

            for (AnalysisDefinition copiedReport : reportList) {
                new AnalysisStorage().saveAnalysis(copiedReport, session);
            }

            for (AnalysisDefinition copiedReport : reportReplacementMap.values()) {
                copiedReport.updateReportIDs(reportReplacementMap);
            }

            for (AnalysisDefinition copiedReport : reportList) {
                new AnalysisStorage().saveAnalysis(copiedReport, session);
            }

            Dashboard copiedDashboard = dashboard.cloneDashboard(reportReplacementMap, true, sourceDataSource.getFields(), keyReplacementMap);

            copiedDashboard.setDataSourceID(dataSourceID);

            dashboardStorage.saveDashboard(copiedDashboard, conn);

            DashboardDescriptor dashboardDescriptor = new DashboardDescriptor(copiedDashboard.getName(), copiedDashboard.getId(), copiedDashboard.getUrlKey());

            session.flush();

            conn.commit();
            session.close();

            return dashboardDescriptor;
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public InsightDescriptor installReport(long reportID, long dataSourceID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FeedStorage feedStorage = new FeedStorage();


            Session session = Database.instance().createSession(conn);
            AnalysisDefinition originalBaseReport = new AnalysisStorage().getPersistableReport(reportID, session);
            FeedDefinition sourceDataSource = feedStorage.getFeedDefinitionData(originalBaseReport.getDataFeedID(), conn);
            // okay, we might have multiple reports here...
            // find all the other reports in the dependancy graph here
            List<AnalysisDefinition> reports = originalBaseReport.containedReports(session);
            Set<Long> containedReportIDs = originalBaseReport.containedReportIDs();
            for (Long containedReportID : containedReportIDs) {
                reports.add(new AnalysisStorage().getPersistableReport(containedReportID, session));
            }
            reports.add(originalBaseReport);
            Map<Long, AnalysisDefinition> reportReplacementMap = new HashMap<Long, AnalysisDefinition>();
            List<AnalysisDefinition> reportList = new ArrayList<AnalysisDefinition>();
            for (AnalysisDefinition child : reports) {
                FeedDefinition targetDataSource = feedStorage.getFeedDefinitionData(dataSourceID, conn);
                Map<Key, Key> keyReplacementMap = createKeyReplacementMap(targetDataSource, sourceDataSource);
                AnalysisDefinition copyReport = copyReportToDataSource(targetDataSource, child, keyReplacementMap);
                reportReplacementMap.put(child.getAnalysisID(), copyReport);
                reportList.add(copyReport);
            }

            for (AnalysisDefinition copiedReport : reportList) {
                new AnalysisStorage().saveAnalysis(copiedReport, session);
            }

            for (AnalysisDefinition copiedReport : reportReplacementMap.values()) {
                copiedReport.updateReportIDs(reportReplacementMap);
            }

            for (AnalysisDefinition copiedReport : reportList) {
                new AnalysisStorage().saveAnalysis(copiedReport, session);
            }

            session.flush();

            AnalysisDefinition copiedBaseReport = reportReplacementMap.get(reportID);
            // TODO: Add urlKey
            InsightDescriptor insightDescriptor = new InsightDescriptor(copiedBaseReport.getAnalysisID(), copiedBaseReport.getTitle(),
                    copiedBaseReport.getDataFeedID(), copiedBaseReport.getReportType(),null);

            conn.commit();
            session.close();
            return insightDescriptor;
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    private Map<Key, Key> createKeyReplacementMap(FeedDefinition localDefinition, FeedDefinition sourceDefinition) {
        Map<Key, Key> keys = new HashMap<Key, Key>();
        for (AnalysisItem sourceField : sourceDefinition.getFields()) {
            for (AnalysisItem targetField : localDefinition.getFields()) {
                if (sourceField.toDisplay().equals(targetField.toDisplay())) {
                    keys.put(sourceField.getKey(), targetField.getKey());
                    break;
                }
            }
        }
        return keys;
    }

    private AnalysisDefinition copyReportToDataSource(FeedDefinition localDefinition, AnalysisDefinition report,
                                                        Map<Key, Key> keyReplacementMap) throws CloneNotSupportedException {
        AnalysisDefinition clonedReport = report.clone(keyReplacementMap, localDefinition.getFields(), true);
        clonedReport.setSolutionVisible(false);
        clonedReport.setAnalysisPolicy(AnalysisPolicy.PRIVATE);
        clonedReport.setDataFeedID(localDefinition.getDataFeedID());

        // what to do here...

        clonedReport.setUserBindings(Arrays.asList(new UserToAnalysisBinding(SecurityUtil.getUserID(), UserPermission.OWNER)));
        clonedReport.setTemporaryReport(true);
        return clonedReport;
    }

    public List<SolutionReportExchangeItem> getSolutionReports() {
        List<SolutionReportExchangeItem> reports = new ArrayList<SolutionReportExchangeItem>();
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement analysisQueryStmt = conn.prepareStatement("SELECT ANALYSIS.ANALYSIS_ID, ANALYSIS.TITLE, ANALYSIS.DATA_FEED_ID, ANALYSIS.REPORT_TYPE, " +
                    "analysis.create_date, ANALYSIS.DESCRIPTION, DATA_FEED.FEED_NAME, ANALYSIS.AUTHOR_NAME," +
                    "DATA_FEED.PUBLICLY_VISIBLE, SOLUTION.NAME, SOLUTION.SOLUTION_ID, ANALYSIS.url_key FROM DATA_FEED, SOLUTION_INSTALL, SOLUTION, ANALYSIS " +
                    " WHERE ANALYSIS.DATA_FEED_ID = DATA_FEED.DATA_FEED_ID AND " +
                    "ANALYSIS.DATA_FEED_ID = SOLUTION_INSTALL.installed_data_source_id AND ANALYSIS.SOLUTION_VISIBLE = ? " +
                    "AND solution_install.solution_id = solution.solution_id GROUP BY ANALYSIS.ANALYSIS_ID");

            PreparedStatement getReportRatingStmt = conn.prepareStatement("SELECT avg(user_report_rating.rating), count(user_report_rating.rating) from " +
                    "user_report_rating where report_id = ?");

            analysisQueryStmt.setBoolean(1, true);
            ResultSet analysisRS = analysisQueryStmt.executeQuery();
            while (analysisRS.next()) {
                long analysisID = analysisRS.getLong(1);
                if (analysisID == 0) {
                    continue;
                }
                String title = analysisRS.getString(2);
                long dataSourceID = analysisRS.getLong(3);
                int reportType = analysisRS.getInt(4);

                Date created = null;
                java.sql.Date date = analysisRS.getDate(5);
                if (date != null) {
                    created = new Date(date.getTime());
                }

                String description = analysisRS.getString(6);
                String authorName = analysisRS.getString(8);
                String connectionName = analysisRS.getString(10);
                long connectionID = analysisRS.getLong(11);
                String urlKey = analysisRS.getString(12);
                getReportRatingStmt.setLong(1, analysisID);
                ResultSet ratingRS = getReportRatingStmt.executeQuery();
                ratingRS.next();
                double ratingAverage = ratingRS.getLong(1);
                int ratingCount = ratingRS.getInt(2);
                InsightDescriptor insightDescriptor = new InsightDescriptor(analysisID, title, dataSourceID, reportType, urlKey);
                SolutionReportExchangeItem item = new SolutionReportExchangeItem(title, analysisID,
                        ratingAverage, ratingCount, created, description, authorName, insightDescriptor, connectionID, connectionName);
                reports.add(item);
            }

            PreparedStatement dashboardQueryStmt = conn.prepareStatement("SELECT DASHBOARD.DASHBOARD_ID, DASHBOARD.DASHBOARD_NAME, " +
                    "SOLUTION.NAME, SOLUTION.SOLUTION_ID, dashboard.creation_date, dashboard.description," +
                    "dashboard.author_name, dashboard.url_key FROM DATA_FEED, SOLUTION_INSTALL, SOLUTION, dashboard " +
                    " WHERE dashboard.data_source_id = DATA_FEED.DATA_FEED_ID AND " +
                    "dashboard.data_source_id = SOLUTION_INSTALL.installed_data_source_id AND dashboard.exchange_visible = ? " +
                    "AND solution_install.solution_id = solution.solution_id AND dashboard.temporary_dashboard = ?");
            PreparedStatement getRatingDashboardStmt = conn.prepareStatement("SELECT avg(DASHBOARD_USER_RATING.rating), count(dashboard_user_rating.rating) from " +
                    "dashboard_user_rating where dashboard_id = ?");
            dashboardQueryStmt.setBoolean(1, true);
            dashboardQueryStmt.setBoolean(2, false);

            ResultSet dashboardRS = dashboardQueryStmt.executeQuery();
            while (dashboardRS.next()) {
                long dashboardID = dashboardRS.getLong(1);
                String dashboardName = dashboardRS.getString(2);
                String connectionName = dashboardRS.getString(3);
                long connectionID = dashboardRS.getLong(4);
                Date createdDate = new Date(dashboardRS.getTimestamp(5).getTime());
                String description = dashboardRS.getString(6);
                String authorName = dashboardRS.getString(7);
                String urlKey = dashboardRS.getString(8);

                getRatingDashboardStmt.setLong(1, dashboardID);
                ResultSet ratingRS = getRatingDashboardStmt.executeQuery();
                ratingRS.next();

                double ratingAverage = ratingRS.getDouble(1);
                int ratingCount = ratingRS.getInt(2);
                DashboardDescriptor dashboardDescriptor = new DashboardDescriptor(dashboardName, dashboardID, urlKey);
                SolutionReportExchangeItem item = new SolutionReportExchangeItem(dashboardName, dashboardID, ratingAverage,
                        ratingCount, createdDate, description, authorName, dashboardDescriptor, connectionID, connectionName);
                reports.add(item);
            }
            Collections.sort(reports, new Comparator<SolutionReportExchangeItem>() {

                public int compare(SolutionReportExchangeItem solutionReportExchangeItem, SolutionReportExchangeItem solutionReportExchangeItem1) {
                    return ((Double)solutionReportExchangeItem1.getRatingAverage()).compareTo(solutionReportExchangeItem.getRatingAverage());
                }
            });
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return reports;
    }

    public List<SolutionInstallInfo> installSolution(long solutionID) {
        // establish the connection from the account/user to the solution
        // retrieve the feeds for this solution
        // retrieve the insights matching that feed
        // clone the feed/insights
        Solution solution = getSolution(solutionID);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            InstallationSystem installationSystem = new InstallationSystem(conn);
            installationSystem.installSolution(solution);
            conn.commit();
            return installationSystem.getAllSolutions();
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public List<FeedDescriptor> getAvailableFeeds() {
        SecurityUtil.authorizeAccountTier(Account.ADMINISTRATOR);
        List<FeedDescriptor> feedDescriptors = new ArrayList<FeedDescriptor>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT DATA_FEED.DATA_FEED_ID, DATA_FEED.FEED_NAME FROM DATA_FEED, UPLOAD_POLICY_USERS WHERE " +
                    "UPLOAD_POLICY_USERS.USER_ID = ? AND DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_USERS.FEED_ID AND DATA_FEED.VISIBLE = ?");
            queryStmt.setLong(1, SecurityUtil.getUserID());
            queryStmt.setBoolean(2, true);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                long feedID = rs.getLong(1);
                String name = rs.getString(2);
                FeedDescriptor feedDescriptor = new FeedDescriptor();
                feedDescriptor.setId(feedID);
                feedDescriptor.setName(name);
                feedDescriptors.add(feedDescriptor);
            }
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
        return feedDescriptors;
    }

    public List<FeedDescriptor> getDescriptorsForSolution(long solutionID) {
        List<FeedDescriptor> feedDescriptors = new ArrayList<FeedDescriptor>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT DATA_FEED.DATA_FEED_ID, DATA_FEED.FEED_NAME FROM SOLUTION_TO_FEED, DATA_FEED " +
                    "WHERE SOLUTION_ID = ? AND DATA_FEED.DATA_FEED_ID = SOLUTION_TO_FEED.FEED_ID");
            queryStmt.setLong(1, solutionID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                long feedID = rs.getLong(1);
                String name = rs.getString(2);
                FeedDescriptor feedDescriptor = new FeedDescriptor();
                feedDescriptor.setId(feedID);
                feedDescriptor.setName(name);
                feedDescriptors.add(feedDescriptor);
            }
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
        return feedDescriptors;
    }

    private Solution getSolution(long solutionID) {
        Connection conn = Database.instance().getConnection();
        try {
            return getSolution(solutionID, conn);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public Solution getSolution(long solutionID, Connection conn) throws SQLException {
        int accountType = 0;
        if (SecurityUtil.getUserID(false) > 0) {
            accountType = SecurityUtil.getAccountTier();
        }
        PreparedStatement getSolutionsStmt = conn.prepareStatement("SELECT SOLUTION_ID, NAME, DESCRIPTION, INDUSTRY, AUTHOR, COPY_DATA, SOLUTION_ARCHIVE_NAME, goal_tree_id," +
                "solution_image, screencast_directory, screencast_mp4_name, solution_tier, footer_text, logo_link, detail_page_class FROM SOLUTION WHERE SOLUTION_ID = ?");
        getSolutionsStmt.setLong(1, solutionID);
        PreparedStatement dataSourceCountStmt = conn.prepareStatement("SELECT COUNT(*) FROM solution_to_feed WHERE solution_id = ?");
        PreparedStatement goalTreeCountStmt = conn.prepareStatement("SELECT COUNT(*) FROM solution_to_goal_tree WHERE solution_id = ?");
        ResultSet rs = getSolutionsStmt.executeQuery();
        if (rs.next()) {
            String name = rs.getString(2);
            String description = rs.getString(3);
            String industry = rs.getString(4);
            String author = rs.getString(5);
            boolean copyData = rs.getBoolean(6);
            String solutionArchiveName = rs.getString(7);
            long goalTreeID = rs.getLong(8);
            Solution solution = new Solution();
            solution.setName(name);
            solution.setDescription(description);
            solution.setSolutionID(solutionID);
            solution.setIndustry(industry);
            solution.setAuthor(author);
            solution.setCopyData(copyData);
            solution.setSolutionArchiveName(solutionArchiveName);
            solution.setGoalTreeID(goalTreeID);
            solution.setImage(rs.getBytes(9));
            solution.setScreencastDirectory(rs.getString(10));
            solution.setScreencastName(rs.getString(11));
            solution.setSolutionTier(rs.getInt(12));
            solution.setFooterText(rs.getString(13));
            solution.setLogoLink(rs.getString(14));
            solution.setDetailPageClass(rs.getString(15));
            solution.setAccessible(solution.getSolutionTier() <= accountType);
            dataSourceCountStmt.setLong(1, solutionID);
            ResultSet dataSourceRS = dataSourceCountStmt.executeQuery();
            dataSourceRS.next();
            goalTreeCountStmt.setLong(1, solutionID);
            ResultSet goalTreeRS = goalTreeCountStmt.executeQuery();
            goalTreeRS.next();
            solution.setInstallable(dataSourceRS.getInt(1) > 0 || goalTreeRS.getInt(1) > 0);
            return solution;
        } else {
            throw new RuntimeException();
        }
    }

    public List<SolutionGoalTreeDescriptor> getTreesFromSolutions() {
        int solutionTier = SecurityUtil.getAccountTier();
        List<SolutionGoalTreeDescriptor> descriptors = new ArrayList<SolutionGoalTreeDescriptor>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement treeStmt = conn.prepareStatement("SELECT SOLUTION_ID, SOLUTION.NAME, GOAL_TREE.GOAL_TREE_ID, GOAL_TREE.NAME, GOAL_TREE.goal_tree_icon FROM SOLUTION, GOAL_TREE WHERE " +
                    "SOLUTION.GOAL_TREE_ID = GOAL_TREE.GOAL_TREE_ID AND SOLUTION.SOLUTION_TIER <= ?");
            treeStmt.setInt(1, solutionTier);
            ResultSet rs = treeStmt.executeQuery();
            while (rs.next()) {
                long solutionID = rs.getLong(1);
                String solutionName = rs.getString(2);
                long goalTreeID = rs.getLong(3);
                String goalTreeName = rs.getString(4);
                descriptors.add(new SolutionGoalTreeDescriptor(goalTreeID, goalTreeName, 0, solutionID, solutionName, rs.getString(5)));
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return descriptors;
    }

    public List<Solution> getSolutions() {
        int accountType = 0;
        if (SecurityUtil.getUserID(false) > 0) {
            accountType = SecurityUtil.getAccountTier();
        }
        List<Solution> solutions = new ArrayList<Solution>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement getSolutionsStmt = conn.prepareStatement("SELECT SOLUTION_ID, NAME, DESCRIPTION, INDUSTRY, AUTHOR, COPY_DATA, SOLUTION_ARCHIVE_NAME, GOAL_TREE_ID, SOLUTION_TIER," +
                    "solution_image, CATEGORY, screencast_directory, screencast_mp4_name, footer_text, logo_link, detail_page_class FROM SOLUTION WHERE SOLUTION_TIER <= ?");
            getSolutionsStmt.setInt(1, Account.ADMINISTRATOR);
            PreparedStatement dataSourceCountStmt = conn.prepareStatement("SELECT COUNT(*) FROM solution_to_feed WHERE solution_id = ?");
            PreparedStatement goalTreeCountStmt = conn.prepareStatement("SELECT COUNT(*) FROM solution_to_goal_tree WHERE solution_id = ?");
            ResultSet rs = getSolutionsStmt.executeQuery();
            while (rs.next()) {
                long solutionID = rs.getLong(1);
                String name = rs.getString(2);
                String description = rs.getString(3);
                String industry = rs.getString(4);
                String author = rs.getString(5);
                boolean copyData = rs.getBoolean(6);
                String solutionArchiveName = rs.getString(7);
                long goalTreeID = rs.getLong(8);
                int solutionTier = rs.getInt(9);
                byte[] solutionImage = rs.getBytes(10);
                Solution solution = new Solution();
                solution.setName(name);
                solution.setAccessible(solutionTier <= accountType);
                solution.setDescription(description);
                solution.setSolutionID(solutionID);
                solution.setIndustry(industry);
                solution.setAuthor(author);
                solution.setCopyData(copyData);
                solution.setSolutionArchiveName(solutionArchiveName);
                solution.setGoalTreeID(goalTreeID);
                solution.setSolutionTier(solutionTier);
                solution.setImage(solutionImage);
                solution.setCategory(rs.getInt(11));
                solution.setScreencastDirectory(rs.getString(12));
                solution.setScreencastName(rs.getString(13));
                solution.setFooterText(rs.getString(14));
                solution.setLogoLink(rs.getString(15));
                solution.setDetailPageClass(rs.getString(16));
                dataSourceCountStmt.setLong(1, solutionID);
                ResultSet dataSourceRS = dataSourceCountStmt.executeQuery();
                dataSourceRS.next();
                goalTreeCountStmt.setLong(1, solutionID);
                ResultSet goalTreeRS = goalTreeCountStmt.executeQuery();
                goalTreeRS.next();
                solution.setInstallable(dataSourceRS.getInt(1) > 0 || goalTreeRS.getInt(1) > 0);
                solutions.add(solution);
            }
            Collections.sort(solutions, new Comparator<Solution>() {

                public int compare(Solution o1, Solution o2) {
                    if (o1.isAccessible() && !o2.isAccessible()) {
                        return -1;
                    } else if (!o1.isAccessible() && o2.isAccessible()) {
                        return 1;
                    } else {
                        return o1.getName().compareTo(o2.getName());
                    }
                }
            });
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return solutions;
    }

    public void uninstallSolution(long solutionID, boolean deleteFeeds) {
        long userID = SecurityUtil.getUserID();
        Connection conn = Database.instance().getConnection();
        try {
            if (deleteFeeds) {

            }
            PreparedStatement uninstallStmt = conn.prepareStatement("DELETE FROM USER_TO_SOLUTION WHERE SOLUTION_ID = ? AND USER_ID = ?");
            uninstallStmt.setLong(1, solutionID);
            uninstallStmt.setLong(2, userID);
            uninstallStmt.executeUpdate();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public List<Solution> getInstalledSolutions() {
        long userID = SecurityUtil.getUserID();
        List<Solution> solutions = new ArrayList<Solution>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement getSolutionsStmt = conn.prepareStatement("SELECT SOLUTION.SOLUTION_ID, NAME, DESCRIPTION, INDUSTRY, AUTHOR, COPY_DATA, SOLUTION_ARCHIVE_NAME FROM " +
                    "SOLUTION, USER_TO_SOLUTION WHERE SOLUTION.SOLUTION_ID = USER_TO_SOLUTION.SOLUTION_ID AND USER_TO_SOLUTION.USER_ID = ? AND " +
                    "USER_TO_SOLUTION.USER_ROLE = ?");
            getSolutionsStmt.setLong(1, userID);
            getSolutionsStmt.setInt(2, SolutionRoles.INSTALLER);
            ResultSet rs = getSolutionsStmt.executeQuery();
            while (rs.next()) {
                long solutionID = rs.getLong(1);
                String name = rs.getString(2);
                String description = rs.getString(3);
                String industry = rs.getString(4);
                String author = rs.getString(5);
                boolean copyData = rs.getBoolean(6);
                String solutionArchiveName = rs.getString(7);
                Solution solution = new Solution();
                solution.setName(name);
                solution.setDescription(description);
                solution.setSolutionID(solutionID);
                solution.setIndustry(industry);
                solution.setAuthor(author);
                solution.setCopyData(copyData);
                solution.setSolutionArchiveName(solutionArchiveName);
                solutions.add(solution);
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return solutions;
    }

    public byte[] getArchive(long solutionID) {
        Connection conn = Database.instance().getConnection();
        byte[] bytes;
        try {
            Statement getArchiveStmt = conn.createStatement();
            ResultSet rs = getArchiveStmt.executeQuery("SELECT ARCHIVE FROM SOLUTION WHERE SOLUTION_ID = " + solutionID);
            if (rs.next()) {
                bytes = rs.getBytes(1);
            } else {
                throw new RuntimeException("Couldn't find archive for solution " + solutionID);
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return bytes;
    }

    public List<Solution> getSolutionsWithTags(List<Tag> tags) {
        List<Solution> solutions = new ArrayList<Solution>();
        Connection conn = Database.instance().getConnection();
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT SOLUTION.SOLUTION_ID FROM SOLUTION, SOLUTION_TAG WHERE SOLUTION_TAG.TAG_NAME IN (");
            Iterator<Tag> tagIter = tags.iterator();
            while (tagIter.hasNext()) {
                tagIter.next();
                queryBuilder.append("?");
                if (tagIter.hasNext()) {
                    queryBuilder.append(",");
                }
            }
            queryBuilder.append(") AND SOLUTION_TAG.SOLUTION_ID = SOLUTION.SOLUTION_ID");
            PreparedStatement querySolutionsStmt = conn.prepareStatement(queryBuilder.toString());
            int i = 1;
            for (Tag tag : tags) {
                querySolutionsStmt.setString(i++, tag.getTagName());
            }
            ResultSet solutionIDRS = querySolutionsStmt.executeQuery();
            while (solutionIDRS.next()) {
                solutions.add(getSolution(solutionIDRS.getLong(1)));
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return solutions;
    }

    public void addSolutionImage(byte[] bytes, long solutionID) {
        SecurityUtil.authorizeAccountTier(Account.ADMINISTRATOR);
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement updateArchiveStmt = conn.prepareStatement("UPDATE SOLUTION SET SOLUTION_IMAGE = ? WHERE SOLUTION_ID = ?");
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            updateArchiveStmt.setBinaryStream(1, bais, bytes.length);
            updateArchiveStmt.setLong(2, solutionID);
            updateArchiveStmt.executeUpdate();
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }
}
