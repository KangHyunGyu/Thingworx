package com.hhi.beans;

import java.sql.Timestamp;

public class BunkeringData{
		
		String foName;
		String eventType;
		Timestamp time;
		double property_value;
		
		public BunkeringData(String foName,String eventType,Timestamp time,double property_value){
			this.foName = foName;
			this.eventType = eventType;
			this.time = time;
			this.property_value = property_value;
		}

		public String getFoName() {
			return foName;
		}

		public void setFoName(String foName) {
			this.foName = foName;
		}

		public String getEventType() {
			return eventType;
		}

		public void setEventType(String eventType) {
			this.eventType = eventType;
		}

		public Timestamp getTime() {
			return time;
		}

		public void setTime(Timestamp time) {
			this.time = time;
		}

		public double getProperty_value() {
			return property_value;
		}

		public void setProperty_value(double property_value) {
			this.property_value = property_value;
		}
		
	}