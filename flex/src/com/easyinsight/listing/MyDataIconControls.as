package com.easyinsight.listing
{
import com.easyinsight.administration.feed.CredentialsResponse;
import com.easyinsight.administration.feed.ServerDataSourceDefinition;
import com.easyinsight.analysis.CredentialsDefinition;
import com.easyinsight.customupload.FileFeedUpdateWindow;
import com.easyinsight.customupload.RefreshWindow;
import com.easyinsight.customupload.UploadConfigEvent;
import com.easyinsight.framework.GenericFaultHandler;
import com.easyinsight.genredata.ModuleAnalyzeEvent;

    import com.easyinsight.solutions.InsightDescriptor;
    import flash.events.MouseEvent;
	
	import mx.containers.HBox;
import mx.controls.Alert;
import mx.controls.Button;
	import mx.managers.PopUpManager;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class MyDataIconControls extends HBox
	{
		private var obj:Object;
		
		[Embed(source="../../../../assets/refresh.png")]
        public var refreshIcon:Class;
                
        [Embed(source="../../../../assets/businessman_edit.png")]
        public var adminIcon:Class;
        
        [Embed(source="../../../../assets/media_play_green.png")]
        public var playIcon:Class;

        [Embed(source="../../../../assets/navigate_cross.png")]
        public var deleteIcon:Class;

        private var userUploadSource:RemoteObject
        
        private var refreshButton:Button;
        private var adminButton:Button;
        private var analyzeButton:Button;
        private var deleteButton:Button;

		public function MyDataIconControls()
		{
			super();
			analyzeButton = new Button();
			analyzeButton.setStyle("icon", playIcon);
			analyzeButton.toolTip = "Analyze...";
			analyzeButton.addEventListener(MouseEvent.CLICK, analyzeCalled);
			addChild(analyzeButton);			
			refreshButton = new Button();
			refreshButton.setStyle("icon", refreshIcon);
			refreshButton.toolTip = "Refresh...";
			refreshButton.addEventListener(MouseEvent.CLICK, refreshCalled);
			addChild(refreshButton);
			adminButton = new Button();
			adminButton.setStyle("icon", adminIcon);
			adminButton.toolTip = "Administer...";
			adminButton.addEventListener(MouseEvent.CLICK, adminCalled);
			addChild(adminButton);
			deleteButton = new Button();
            deleteButton.setStyle("icon", deleteIcon);
            deleteButton.addEventListener(MouseEvent.CLICK, deleteCalled);

            addChild(deleteButton);
			
			this.setStyle("paddingLeft", 5);
			this.setStyle("paddingRight", 5);
		}
		
		private function refreshCalled(event:MouseEvent):void {
			if (obj is DataFeedDescriptor) {
				var feedDescriptor:DataFeedDescriptor = obj as DataFeedDescriptor;
				switch (feedDescriptor.feedType) {
					case DataFeedDescriptor.STATIC:
					case DataFeedDescriptor.EMPTY:
						fileData(feedDescriptor);
						break;
					default:
						refreshData(feedDescriptor);
						break;
				}
			}
		}

        private function deleteCalled(event:MouseEvent):void {
            dispatchEvent(new DeleteDataSourceEvent(obj));                        
        }
		
		private function analyzeCalled(event:MouseEvent):void {
			if (obj is DataFeedDescriptor) {
				var descriptor:DataFeedDescriptor = obj as DataFeedDescriptor;
				dispatchEvent(new ModuleAnalyzeEvent(new DescriptorAnalyzeSource(descriptor)));
			} else {
				var analysisDefinition:InsightDescriptor = obj as InsightDescriptor;
                dispatchEvent(new ModuleAnalyzeEvent(new AnalysisDefinitionAnalyzeSource(analysisDefinition)));
			}
		}

		private function refreshData(feedDescriptor:DataFeedDescriptor):void {
            if(feedDescriptor.hasSavedCredentials) {
                userUploadSource = new RemoteObject();
                userUploadSource.destination = "userUpload";
                userUploadSource.refreshData.addEventListener(ResultEvent.RESULT, completedRefresh);
                userUploadSource.refreshData.addEventListener(FaultEvent.FAULT, GenericFaultHandler.genericFault);
                userUploadSource.refreshData.send(feedDescriptor.dataFeedID, null, false);
                return;
            }

			var refreshWindow:RefreshWindow = RefreshWindow(PopUpManager.createPopUp(this.parent.parent.parent, RefreshWindow, true));
			refreshWindow.feedID = feedDescriptor.dataFeedID;
            refreshWindow.addEventListener(UploadConfigEvent.UPLOAD_CONFIG_COMPLETE, passEvent);
			PopUpManager.centerPopUp(refreshWindow);
		}

    private function passEvent(event:UploadConfigEvent):void {
        dispatchEvent(event);
    }



    private function completedRefresh(event:ResultEvent):void {
        var credentialsResponse:CredentialsResponse = userUploadSource.refreshData.lastResult as CredentialsResponse;
        if(!credentialsResponse.successful)
            Alert.show(credentialsResponse.failureMessage, "Error");
        dispatchEvent(new UploadConfigEvent(UploadConfigEvent.UPLOAD_CONFIG_COMPLETE));
    }
		
		private function fileData(feedDescriptor:DataFeedDescriptor):void {
			var feedUpdateWindow:FileFeedUpdateWindow = FileFeedUpdateWindow(PopUpManager.createPopUp(this.parent.parent.parent, FileFeedUpdateWindow, true));
			feedUpdateWindow.feedID = feedDescriptor.dataFeedID;
			PopUpManager.centerPopUp(feedUpdateWindow);
				
		}
		
		private function adminCalled(event:MouseEvent):void {
			if (obj is DataFeedDescriptor) {
				var descriptor:DataFeedDescriptor = obj as DataFeedDescriptor;
				dispatchEvent(new ModuleAnalyzeEvent(new FeedAdminAnalyzeSource(descriptor.dataFeedID)));
			}
		}
		
		override public function set data(value:Object):void {
			this.obj = value;
			if (value is DataFeedDescriptor) {
				var descriptor:DataFeedDescriptor = value as DataFeedDescriptor;
				refreshButton.setVisible(true);
				if (descriptor.role == DataFeedDescriptor.OWNER) {
					adminButton.setVisible(true);
				} else {
					adminButton.setVisible(false);
				}
			} else {
				refreshButton.setVisible(false);
				adminButton.setVisible(false);
			}			
		}
		
		override public function get data():Object {
			return this.obj;
		}
	}
}