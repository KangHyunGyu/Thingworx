package com.hhi.beans;

import java.sql.Timestamp;

public class FuelChangeData{
		
		String foName;
		String source_id;
		Timestamp time;
		double startConsumption;
		double consumption;
		double rob;
		
		public FuelChangeData(Timestamp time,String source_id,String foName){
			this.source_id = source_id;
			this.time = time;
			this.foName = foName;
		}

		public String getFoName() {
			return foName;
		}

		public void setFoName(String foName) {
			this.foName = foName;
		}

		public String getSource_id() {
			return source_id;
		}

		public void setSource_id(String source_id) {
			this.source_id = source_id;
		}

		public Timestamp getTime() {
			return time;
		}

		public void setTime(Timestamp time) {
			this.time = time;
		}

		public double getConsumption() {
			return consumption;
		}

		public void setConsumption(double consumption) {
			this.consumption = consumption;
		}
		
		public double getStartConsumption() {
			return startConsumption;
		}

		public void setStartConsumption(double startConsumption) {
			this.startConsumption = startConsumption;
		}

		public double getRob() {
			return rob;
		}

		public void setRob(double rob) {
			this.rob = rob;
		}
	}