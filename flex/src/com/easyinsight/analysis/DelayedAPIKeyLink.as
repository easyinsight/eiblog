package com.easyinsight.analysis
{
	import com.easyinsight.framework.LoginEvent;
import com.easyinsight.genredata.AnalyzeEvent;
import com.easyinsight.listing.DescriptorAnalyzeSource;

	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	public class DelayedAPIKeyLink extends EventDispatcher
	{
		private var apiKey:String;
		private var feedService:RemoteObject;
		
		public function DelayedAPIKeyLink(apiKey:String)
		{
			this.apiKey = apiKey;
			this.feedService = new RemoteObject();
			feedService.destination = "feeds";
			feedService.openFeedByAPIKey.addEventListener(ResultEvent.RESULT, gotFeed);
			feedService.openFeedByAPIKey.addEventListener(FaultEvent.FAULT, fault);
		}
		
		private function fault(event:FaultEvent):void {
			Alert.show(event.fault.message);
		}
		
		public function execute():void {
			feedService.openFeedByAPIKey.send(apiKey);
		}

		private function gotFeed(event:ResultEvent):void {
        	var feedResponse:FeedResponse = feedService.openFeedByAPIKey.lastResult as FeedResponse;
        	if (feedResponse.status == FeedResponse.SUCCESS) {
        		dispatchEvent(new AnalyzeEvent(new DescriptorAnalyzeSource(feedResponse.feedDescriptor.dataFeedID, feedResponse.feedDescriptor.name)));
            } else {
        		// tried to access a data source they don't have rights to, silently fail
        	}
        }  
        
        private function delayedFeed(event:LoginEvent):void {
        	feedService.openFeedByAPIKey.send(apiKey);
        }
	}
}