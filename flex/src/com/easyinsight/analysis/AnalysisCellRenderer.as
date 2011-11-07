package com.easyinsight.analysis
{


import com.easyinsight.analysis.list.ListDefinition;
import com.easyinsight.filtering.FilterValueDefinition;
import com.easyinsight.pseudocontext.PseudoContextWindow;
import com.easyinsight.pseudocontext.StandardContextWindow;
import com.easyinsight.report.ReportNavigationEvent;
import com.easyinsight.solutions.InsightDescriptor;

import flash.events.Event;
import flash.events.MouseEvent;
import flash.net.URLRequest;
import flash.net.navigateToURL;

import mx.collections.ArrayCollection;

import mx.controls.Alert;

import mx.controls.listClasses.IListItemRenderer;
import mx.core.UITextField;
import mx.core.UITextFormat;
import mx.events.FlexEvent;
import mx.formatters.Formatter;
import mx.managers.CursorManager;
import mx.managers.PopUpManager;


public class AnalysisCellRenderer extends UITextField implements IListItemRenderer
{
    private var _data:Object;
    private var _analysisItem:AnalysisItem;
    private var _selectionEnabled:Boolean;
    private var _report:AnalysisDefinition;
    private var _rolloverIcon:Class;

    private var hyperlinked:Boolean;

    public function AnalysisCellRenderer() {
        super();
        addEventListener(MouseEvent.ROLL_OVER, onRollOver);
        addEventListener(MouseEvent.ROLL_OUT, onRollOut);
        addEventListener(MouseEvent.CLICK, onClick);
        this.percentWidth = 100;
    }

    public function set report(value:AnalysisDefinition):void {
        _report = value;
    }

    public function set rolloverIcon(value:Class):void {
        _rolloverIcon = value;
    }

    private function onClick(event:MouseEvent):void {
        if (defaultLink != null) {
            if (defaultLink is URLLink) {
                var urlLink:URLLink = defaultLink as URLLink;
                var url:String = data[urlLink.label + "_link"];
                navigateToURL(new URLRequest(url), "_blank");
            } else if (defaultLink is DrillThrough) {
                var drillThrough:DrillThrough = defaultLink as DrillThrough;
                var executor:DrillThroughExecutor = new DrillThroughExecutor(drillThrough);
                executor.addEventListener(DrillThroughEvent.DRILL_THROUGH, onDrill);
                executor.send();
            }
        }
    }

    private function onDrill(event:DrillThroughEvent):void {
        var filters:ArrayCollection;
        if (analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
            var filterDefinition:FilterValueDefinition = new FilterValueDefinition();
            filterDefinition.field = analysisItem;
            filterDefinition.singleValue = true;
            filterDefinition.filteredValues = new ArrayCollection([data[analysisItem.qualifiedName()]]);
            filterDefinition.enabled = true;
            filterDefinition.inclusive = true;
            filters = new ArrayCollection([ filterDefinition ]);
        }
        if (event.drillThrough.miniWindow) {
            dispatchEvent(new ReportWindowEvent(event.report.id, 0, 0, filters, InsightDescriptor(event.report).dataFeedID, InsightDescriptor(event.report).reportType));
        } else {
            dispatchEvent(new ReportNavigationEvent(ReportNavigationEvent.TO_REPORT, event.report, filters));
        }
    }

    private function onRollOver(event:MouseEvent):void {
        if (_rolloverIcon && !_selectionEnabled) {
            CursorManager.setCursor(_rolloverIcon);
        }
        if (hyperlinked) {
            if (utf != null) {
                var tf:UITextFormat = new UITextFormat(this.systemManager, utf.font, utf.size, utf.color, null, null, true);
                tf.align = utf.align;
                setTextFormat(tf);
                invalidateProperties();
            }
        }
    }

    private var utf:UITextFormat;

    private function onRollOut(event:MouseEvent):void {
        if (_rolloverIcon && !_selectionEnabled) {
            CursorManager.removeAllCursors();
        }
        if (hyperlinked) {
            if (utf != null) {
                var tf:UITextFormat = new UITextFormat(this.systemManager, utf.font, utf.size, utf.color, null, null, false);
                tf.align = utf.align;
                setTextFormat(tf);
                invalidateProperties();
            }
        }
    }

    private function passThrough(event:Event):void {
        dispatchEvent(event);
    }

    public function get analysisItem():AnalysisItem {
        return _analysisItem;
    }

    private var defaultLink:Link;

    public function set analysisItem(val:AnalysisItem):void {
        _analysisItem = val;
        if (_analysisItem != null) {
            toolTip = _analysisItem.tooltip;
            if (_analysisItem.links != null) {
                for each (var link:Link in _analysisItem.links) {
                    if (link.defaultLink) {
                        defaultLink = link;
                        break;
                    }
                }
            }
        }
    }

    public function validateProperties():void {
        validateNow();
    }

    public function validateSize(recursive:Boolean = false):void {
        validateNow();
    }

    public function validateDisplayList():void {
        validateNow();
    }

    public function set data(value:Object):void {
        _data = value;
        var color:uint = 0;
        var text:String;
        if (value != null) {
            var field:String = analysisItem.qualifiedName();
            var formatter:Formatter = analysisItem.getFormatter();
            if (value[field] is Value) {
                var objVal:Value = value[field];
                if (objVal == null) {
                    text = "";
                } else {
                    text = formatter.format(objVal.getValue());
                }
                if (_report is ListDefinition) {
                    var listDefinition:ListDefinition = _report as ListDefinition;
                    if (objVal.summary) {
                        color = listDefinition.summaryRowTextColor;
                    } else {
                        if (objVal.valueExtension != null) {
                            var ext:TextValueExtension = objVal.valueExtension as TextValueExtension;
                            color = ext.color;
                        } else {
                            color = listDefinition.textColor;
                        }
                    }
                }
                if (defaultLink != null && objVal != null && objVal.type() != Value.EMPTY) {
                    hyperlinked = true;
                }
            } else {
                hyperlinked = false;
                if (value[field] != null) {
                    text = formatter.format(value[field]);
                } else {
                    text = "";
                }

            }
        } else {
            text = "";
        }

        setText(text);

        var rext:TextReportFieldExtension = analysisItem.reportFieldExtension as TextReportFieldExtension;
        var align:String = "left";
        if (rext != null && rext.align != null) {
            align = rext.align.toLowerCase();
        }
        utf = new UITextFormat(this.systemManager, _report.getFont(), _report.fontSize, color);
        utf.align = align;
        setTextFormat(utf);
        new StandardContextWindow(analysisItem, passThrough, this, value);
        invalidateProperties();
        dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
    }

    protected function setText(text:String):void {
        this.text = text;
    }

    public function get data():Object {
        return _data;
    }

}
}