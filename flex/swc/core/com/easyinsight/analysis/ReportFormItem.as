package com.easyinsight.analysis {
import mx.containers.FormItem;
import mx.controls.CheckBox;

public class ReportFormItem extends FormItem {

    public var property:String;
    public var report:Object;
    public var value:Object;
    public var enabledProperty:String;
    public var reloadOnChange:Boolean;

    public function ReportFormItem(label:String = null, property:String = null, value:Object = null, report:Object = null, enabledProperty:String = null, reloadOnChange:Boolean = false) {
        super();
        this.label = label;
        this.value = value;
        this.report = report;
        this.property = property;
        this.enabledProperty = enabledProperty;
        this.reloadOnChange = reloadOnChange;
        direction = "horizontal";
    }

    override protected function createChildren():void {
        super.createChildren();
        if (enabledProperty != null) {
            checkBox = new CheckBox();
            checkBox.selected = Boolean(report[enabledProperty]);
            addChild(checkBox);
        }
    }

    private var checkBox:CheckBox;

    public function validate():Boolean {
        return true;
    }

    protected function getValue():Object {
        return null;
    }

    public function save():void {
        report[property] = getValue();
        if (enabledProperty != null) {
            report[enabledProperty] = checkBox.selected;
        }
    }
}
}