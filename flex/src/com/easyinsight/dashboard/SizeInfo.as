/**
 * Created by IntelliJ IDEA.
 * User: jamesboe
 * Date: 10/15/11
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.dashboard {
public class SizeInfo {

    public var preferredWidth:int;
    public var preferredHeight:int;
    public var autoCalcHeight:Boolean;

    public function SizeInfo(preferredWidth:int = 0, preferredHeight:int = 0, autoCalcHeight:Boolean = false) {
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.autoCalcHeight = autoCalcHeight;
    }
}
}
