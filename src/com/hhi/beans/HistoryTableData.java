package com.hhi.beans;


public class HistoryTableData{
		
		String source_id;
		String property_name;
		double value;
		String value_text;
		String etc;
		int type;
		boolean quality = true;
		int qc;
		boolean isResearch = false;
		/**
		 * 
		 * @param source_id
		 * @param property_name
		 */
		public HistoryTableData(String source_id,String property_name){
			this.source_id = source_id;
			this.property_name = property_name;
		}

		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public String getSource_id() {
			return source_id;
		}
		public void setSource_id(String source_id) {
			this.source_id = source_id;
		}
		public String getProperty_name() {
			return property_name;
		}
		public void setProperty_name(String property_name) {
			this.property_name = property_name;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}
		public String getValue_text() {
			return value_text;
		}
		public void setValue_text(String value_text) {
			this.value_text = value_text;
		}
		public String getEtc() {
			return etc;
		}
		public void setEtc(String etc) {
			this.etc = etc;
		}

		public boolean isQuality() {
			return quality;
		}

		public void setQuality(boolean quality) {
			this.quality = quality;
		}

		public int getQc() {
			return qc;
		}

		public void setQc(int qc) {
			this.qc = qc;
		}
		
		public boolean isResearch() {
			return isResearch;
		}

		public void setResearch(boolean isResearch) {
			this.isResearch = isResearch;
		}
	}