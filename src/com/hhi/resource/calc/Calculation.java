package com.hhi.resource.calc;

import org.joda.time.DateTime;

import com.hhi.resource.calc.CalculationManager;

public abstract class Calculation {

String filterThingName = "ISS.FilterParameter.Thing";
	
	int defaultOrder = 10;
	
	CalculationManager manager;
	
	public static final double K2T = 1000;
	public static final double K2M = 0.5144;
	public static final double C2J = 4.184;
	public static final double K2G = 1000;
	public static final double D2M = 3600;
	public static final double NM = 1852;
	public static final double M2S = 60;
	
	public static String DEFAULT_BASELINE = "TEST";
	
	public void setManager(CalculationManager manager){
		this.manager = manager;
	}
	
	public abstract void calculate() throws Exception;
	
	public DateTime getDateTime(){
		return manager.getTime();
	}
	
	public int getOrder(){
		return defaultOrder;
	}
	
	public void println(Object obj){
		//System.out.println(obj);
	}

	public void println(){
		//System.out.println();
	}

	public void print(Object obj){
		//System.out.print(obj);
	}
	
}
