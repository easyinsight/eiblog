package com.easyinsight.solutions
{
import flash.utils.ByteArray;

import mx.collections.ArrayCollection;

[Bindable]
	[RemoteClass(alias="com.easyinsight.solutions.Solution")]
	public class Solution
	{
        public static const SAAS:int = 1;
        public static const CLIENT:int = 2;
        public static const LEARNING:int = 3;
        public static const BIZ:int = 4;
        public static const HIDDEN:int = 5;

		public var solutionID:int;
		public var name:String;
		public var industry:String;
		public var copyData:Boolean;
		public var solutionArchiveName:String;
        public var installable:Boolean;
        public var solutionTier:int;
        public var image:ByteArray;
        public var accessible:Boolean;
        public var tags:ArrayCollection;
        public var category:int;
        public var logoLink:String;
        public var dataSourceType:int;

		public function Solution()
		{
		}

	}
}