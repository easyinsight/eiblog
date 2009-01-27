package com.easyinsight.analysis.charts
{
	import com.easyinsight.analysis.AnalysisDateDimension;
	import com.easyinsight.analysis.AnalysisDimension;
	import com.easyinsight.analysis.AnalysisItem;
	import com.easyinsight.analysis.AnalysisItemTypes;
	import com.easyinsight.analysis.AnalysisMeasure;
	
	import mx.charts.CategoryAxis;
	import mx.charts.DateTimeAxis;
	import mx.charts.Legend;
	import mx.charts.LineChart;
	import mx.charts.chartClasses.IAxis;
	import mx.charts.series.LineSeries;
	import mx.collections.ArrayCollection;
	
	public class LineChartAdapter extends ChartAdapter
	{
		[Bindable]
		private var chartData:ArrayCollection;
		
		private var lineChart:LineChart;
		
		private var legend:Legend;
		
		public function LineChartAdapter()
		{
			super();
			this.percentHeight = 100;
			this.percentWidth = 100;
		}
		
		override protected function createChildren():void {
			super.createChildren();
			if (lineChart == null) {
				lineChart = new LineChart();
				lineChart.percentHeight = 100;
				lineChart.percentWidth = 100;
				//lineChart.dataProvider = chartData;
				lineChart.selectionMode = "multiple";				
			}
			addChild(lineChart);
			if (legend == null) {
				legend = new Legend();
				legend.direction = "vertical";
				legend.percentHeight = 100;				
			}
			addChild(legend);
		}
		
		override public function get chartType():int {
			return ChartTypes.LINE_2D;
		}
		
		override public function dataChange(dataSet:ArrayCollection, dimensions:Array, measures:Array):void {
			
			this.chartData = dataSet;
			
			if (measures.length >= 2 || dimensions.length == 2) {
				
				removeChild(lineChart);
				
				lineChart = new LineChart();
				lineChart.percentHeight = 100;
				lineChart.percentWidth = 100;
				lineChart.dataProvider = chartData;
				lineChart.selectionMode = "multiple";		
			
				var xAxisDimension:AnalysisItem;
				if (measures.length >= 2) {
					xAxisDimension = dimensions[0] as AnalysisItem;
				} else {
					xAxisDimension = dimensions[1] as AnalysisItem;
				}
				
				var xAxis:IAxis;
				
				if (xAxisDimension.hasType(AnalysisItemTypes.DATE)) {
					var dateDimension:AnalysisDateDimension = xAxisDimension as AnalysisDateDimension;
					var dateAxis:DateTimeAxis = new DateTimeAxis();
					switch (dateDimension.dateLevel) {
						case AnalysisItemTypes.YEAR_LEVEL:
							dateAxis.dataUnits = "years";
							break;
						case AnalysisItemTypes.MONTH_LEVEL:
							dateAxis.dataUnits = "months";
							break;
						case AnalysisItemTypes.DAY_LEVEL:
							dateAxis.dataUnits = "days";
							break;
					}					
					dateAxis.displayName = dateDimension.display;
					xAxis = dateAxis;
				} else {
					var categoryAxis:CategoryAxis = new CategoryAxis();
					categoryAxis.categoryField = xAxisDimension.key.createString();
					categoryAxis.displayName = xAxisDimension.display;
					categoryAxis.dataProvider = chartData;
					xAxis = categoryAxis;
				}
				
				/*var xAxisRenderer:AxisRenderer = new AxisRenderer();
		        xAxisRenderer.axis = xAxis;
		        xAxisRenderer.setStyle("color", "#FFFFFF");
		        xAxisRenderer.placement = "bottom";*/
				
				lineChart.horizontalAxis = xAxis;
				
				//lineChart.horizontalAxisRenderers = [ xAxisRenderer ];
				
				/*var xAxisRenderer:AxisRenderer = new AxisRenderer();
				xAxisRenderer.setStyle("color", "#FFFFFF");
				xAxisRenderer.axis = xAxis;
				lineChart.horizontalAxisRenderers = [ xAxisRenderer ];*/
				
				/*var yCategoryAxis:CategoryAxis = new CategoryAxis();
				lineChart.verticalAxis = yCategoryAxis;									
				
				var yAxisRenderer:AxisRenderer = new AxisRenderer();
				yAxisRenderer.setStyle("color", "#FFFFFF");
				yAxisRenderer.axis = yCategoryAxis;  			
				
				lineChart.horizontalAxis = xAxis;*/
				
				var mySeries:Array = new Array();
				
				if (measures.length > 1) {
					// we'll render multiple measures
					var measureSeries:Array = [];
					
					for each (var measureItem:AnalysisItem in measures) {
						var lineSeries:LineSeries = new LineSeries();
						lineSeries.yField = measureItem.key.createString();
						lineSeries.xField = xAxisDimension.key.createString();
						//lineSeries.setStyle("form", "curve");
						lineSeries.displayName = measureItem.display;					
						measureSeries.push(lineSeries);
					}
					
					lineChart.series = measureSeries;
					var measureCategoryAxis:CategoryAxis = new CategoryAxis();
					 
					lineChart.verticalAxis = measureCategoryAxis;
					
				} else {
					var analysisMeasure:AnalysisMeasure = measures[0];
					var dimension:AnalysisDimension = dimensions[0];
					var uniques:ArrayCollection = new ArrayCollection();
					
					var allItems:ArrayCollection = new ArrayCollection();					
					
					var seriesData:Object = new Object();
					for (var i:int = 0; i < dataSet.length; i++) {
						var object:Object = dataSet.getItemAt(i);
						var dimensionValue:String = object[dimensions[0].key.createString()];
						var newSeriesData:ArrayCollection = seriesData[dimensionValue];
						if (newSeriesData == null) {
							newSeriesData = new ArrayCollection();
							seriesData[dimensionValue] = newSeriesData;
						}
						var newObject:Object = new Object();
						newObject[dimensions[1].key.createString()] = object[dimensions[1].key.createString()];
						newObject[dimensionValue] = object[measures[0].key.createString()];
						newSeriesData.addItem(newObject);
						//allItems.addItem(newObject);
						if (!uniques.contains(dimensionValue)) {
							uniques.addItem(dimensionValue);
						}
					}
					for (i = 0; i < uniques.length; i++) {
						var key:String = uniques.getItemAt(i) as String;
						var uniqueLineSeries:LineSeries = new LineSeries();
						uniqueLineSeries.xField = dimensions[1].key.createString();
						uniqueLineSeries.yField = key;
						uniqueLineSeries.displayName = key;
						uniqueLineSeries.dataProvider = seriesData[key];
						//uniqueLineSeries.labelFunction = renderChartLabel; 
						mySeries.push(uniqueLineSeries);	
					}
					
					
					/* var categoryAxis:CategoryAxis = new CategoryAxis();
					var yAxisDimension:AnalysisItem = dimensions[1] as AnalysisItem;
					categoryAxis.categoryField = yAxisDimension.qualifiedName();
					lineChart.verticalAxis = categoryAxis;*/
					//lineChart.dataProvider = seriesData[uniques.getItemAt(0)] as ArrayCollection;
				}
				lineChart.series = mySeries;
				//lineChart.dataProvider = this.chartData;
				
				legend.dataProvider = lineChart;
				legend.direction = "vertical";
				addChildAt(lineChart, 0);
			}
		}

		override public function getMaxMeasures():int {
			return 1;
		}
		
		override public function getMaxDimensions():int {
			return 2;
		}		
	}
}