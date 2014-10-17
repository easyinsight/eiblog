package com.easyinsight.customupload.wizard {

[Bindable]
[RemoteClass(alias="com.easyinsight.userupload.JSONUploadContext")]
public class JSONUploadContext extends UploadContext {

    public var userName:String;
    public var httpMethod:int;
    public var url:String;
    public var password:String;
    public var jsonPath:String;
    public var dataSourceName:String;
    public var nextPagePath:String;
    public var resultsJSONPath:String;
    public var paginationMethod:int;
    public var perPageLimit:int;
    public var firstPageNumber:int;
    public var pageField:String;
    public var offsetField:String;
    public var limitField:String;

    public function JSONUploadContext() {
        super();
    }
}
}