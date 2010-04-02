package com.easyinsight.goals
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.easyinsight.goals.GoalTree")]
	public class GoalTree
	{
		public var rootNode:GoalTreeNode;
		public var name:String;
		public var description:String;
		public var stage:String;
		public var goalTreeID:int;
		public var milestones:ArrayCollection;
		public var trackingIntervals:ArrayCollection;
        public var administrators:ArrayCollection = new ArrayCollection();
        public var consumers:ArrayCollection = new ArrayCollection();
        public var newSolutions:ArrayCollection = new ArrayCollection();
        public var subTreeParents:ArrayCollection = new ArrayCollection();
        public var iconImage:String;
        public var urlKey:String;
		
		public function GoalTree()
		{
		}

	}
}