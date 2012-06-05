package com.easyinsight.analysis;

import com.easyinsight.core.XMLMetadata;
import nu.xom.Attribute;
import nu.xom.Element;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.List;

/**
 * User: James Boe
 * Date: Mar 28, 2009
 * Time: 5:23:55 PM
 */
@Entity
@PrimaryKeyJoinColumn(name="report_state_id")
@Table(name="list_report")
public class ListDefinitionState extends AnalysisDefinitionState {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="list_report_id")
    private long definitionID;

    @OneToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="list_limits_metadata_id")
    private ListLimitsMetadata listLimitsMetadata;

    @Column(name="show_row_numbers")
    private boolean showRowNumbers = false;

    @Column(name="summarize_all")
    private boolean summarizeAll = false;

    @Override
    public Element toXML(XMLMetadata xmlMetadata) {
        Element element = new Element("listDefinitionState");
        element.addAttribute(new Attribute("showRowNumbers", String.valueOf(showRowNumbers)));
        element.addAttribute(new Attribute("summarizeAll", String.valueOf(summarizeAll)));
        return element;
    }

    public long getDefinitionID() {
        return definitionID;
    }

    public void setDefinitionID(long definitionID) {
        this.definitionID = definitionID;
    }

    public boolean isSummarizeAll() {
        return summarizeAll;
    }

    public void setSummarizeAll(boolean summarizeAll) {
        this.summarizeAll = summarizeAll;
    }

    public ListLimitsMetadata getListLimitsMetadata() {
        return listLimitsMetadata;
    }

    public void setListLimitsMetadata(ListLimitsMetadata listLimitsMetadata) {
        this.listLimitsMetadata = listLimitsMetadata;
    }

    public boolean isShowRowNumbers() {
        return showRowNumbers;
    }

    public void setShowRowNumbers(boolean showRowNumbers) {
        this.showRowNumbers = showRowNumbers;
    }

    public void beforeSave(Session session) {
        if (listLimitsMetadata != null) {
            listLimitsMetadata.beforeSave(session);
        }
    }

    public WSAnalysisDefinition createWSDefinition() {
        WSListDefinition listDefinition = new WSListDefinition();
        listDefinition.setListLimitsMetadata(listLimitsMetadata);
        listDefinition.setShowLineNumbers(showRowNumbers);
        listDefinition.setReportType(WSAnalysisDefinition.LIST);
        listDefinition.setListDefinitionID(definitionID);
        listDefinition.setSummaryTotal(summarizeAll);
        return listDefinition;
    }

    public ListDefinitionState clone(List<AnalysisItem> allFields) throws CloneNotSupportedException {
        ListDefinitionState listDefinition = (ListDefinitionState) super.clone(allFields);
        listDefinition.setDefinitionID(0);
        if (listLimitsMetadata != null) {
            listDefinition.listLimitsMetadata = listLimitsMetadata.clone();
        }
        return listDefinition;
    }

    public void updateIDs(ReplacementMap replacementMap) throws CloneNotSupportedException {
        super.updateIDs(replacementMap);
        if (listLimitsMetadata != null) {
            listLimitsMetadata.updateIDs(replacementMap);
        }
    }

    @Override
    public void afterLoad() {
        super.afterLoad();
        if (getListLimitsMetadata() != null) {
            getListLimitsMetadata().afterLoad();
        }
    }
}
