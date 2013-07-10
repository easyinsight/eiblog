package com.easyinsight.userupload;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.scheduler.FileProcessCreateScheduledTask;

import com.easyinsight.scheduler.FileProcessOptimizedCreateScheduledTask;
import com.easyinsight.security.SecurityUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * User: jamesboe
 * Date: Mar 27, 2010
 * Time: 3:21:36 PM
 */
public class FlatFileUploadContext extends UploadContext {
    private String uploadKey;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUploadKey() {
        return uploadKey;
    }

    public void setUploadKey(String uploadKey) {
        this.uploadKey = uploadKey;
    }

    private transient UploadFormat uploadFormat;

    @Override
    public String validateUpload(EIConnection conn) throws SQLException {

        switch (type) {
            case 1:
                uploadFormat = new CsvFileUploadFormat();
                break;
            case 2:
                uploadFormat = new ExcelUploadFormat();
                break;
            case 3:
                uploadFormat = new XSSFExcelUploadFormat();
                break;
            default:
                uploadFormat = null;
                break;
        }

        if (uploadFormat == null) {
            return "Sorry, we couldn't figure out what type of file you tried to upload. Supported types are Excel 1997-2008 and delimited text files.";
        } else {
            return null;
        }
    }

    private Map<Key, Set<String>> sampleMap;

    @Override
    public List<AnalysisItem> guessFields(EIConnection conn, byte[] bytes) throws Exception {
        UserUploadAnalysis userUploadAnalysis = uploadFormat.analyze(bytes);
        sampleMap = userUploadAnalysis.getSampleMap();
        return userUploadAnalysis.getFields();
    }

    public long createDataSource(String name, List<AnalysisItem> analysisItems, EIConnection conn, boolean accountVisible, byte[] bytes) throws Exception {
        UploadFormat uploadFormat = new UploadFormatTester().determineFormat(bytes);
        if (uploadFormat instanceof CsvFileUploadFormat) {
            FileProcessOptimizedCreateScheduledTask task = new FileProcessOptimizedCreateScheduledTask();
            task.setName(name);
            task.setUserID(SecurityUtil.getUserID());
            task.setAccountID(SecurityUtil.getAccountID());
            task.createFeed(conn, bytes, uploadFormat, analysisItems, accountVisible);
            return task.getFeedID();
        } else {
            FileProcessCreateScheduledTask task = new FileProcessCreateScheduledTask();
            task.setName(name);
            task.setUserID(SecurityUtil.getUserID());
            task.setAccountID(SecurityUtil.getAccountID());
            task.createFeed(conn, bytes, uploadFormat, analysisItems, accountVisible);
            return task.getFeedID();
        }
    }

    @Override
    public List<String> getSampleValues(Key key) {
        return new ArrayList<String>(sampleMap.get(key));
    }
}
