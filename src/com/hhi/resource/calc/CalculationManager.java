package com.hhi.resource.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.hhi.beans.HistoryTableData;
import com.thingworx.entities.RootEntity;
import com.thingworx.entities.utils.EntityUtilities;
import com.thingworx.entities.utils.ThingUtilities;
import com.thingworx.networks.Network;
import com.thingworx.relationships.RelationshipTypes.ThingworxRelationshipTypes;
import com.thingworx.resources.Resource;
import com.thingworx.things.Thing;
import com.thingworx.things.bindings.collections.LocalPropertyBindingCollection;
import com.thingworx.thingshape.ThingShape;
import com.thingworx.thingtemplates.ThingTemplate;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.collections.ValueCollectionList;
import com.thingworx.types.primitives.InfoTablePrimitive;

public class CalculationManager {
	
	
	List<Calculation> calcs = new ArrayList<Calculation>();
	Map<String,Thing> thingMap = new HashMap<String,Thing>();
	Map<String,List<Thing>> entityMap = new HashMap<String,List<Thing>>();
	Map<String,Resource> resourceMap = new HashMap<String,Resource>();
	List<String> attrList;
	Map<String, HistoryTableData> valueMap;
	Map<String, LocalPropertyBindingCollection> bindingMap = new HashMap<String,LocalPropertyBindingCollection>();
	
	List<String> meThingNameList;
	List<String> geThingNameList;
	List<String> deThingNameList;
	List<String> sbThingNameList;
	
	//FSRU ADD 2020.03.03
	List<String> rbThingNameList;
	//FSRU END
	
	List<String> shaftThingNameList;
	List<Thing> allFlowMeterList;
	
	DateTime time;
	double rate = 10000; // 10 sec
	
	Network flowMeterNetwork;
	
	String[] boilerTypes = {"Aux","Composite","Donkey","Regas"};
	BoilerTempData[] boilerInfo = null;
	
	public boolean thingworxSave = true;

	public CalculationManager() throws Exception{
		time = new DateTime();
	}
	
	public void setValueMap(Map<String, HistoryTableData> valueMap){
		this.valueMap = valueMap;
	}
	
	public List<String> getAttrList(){
		return attrList;
	}
	
	public void setAttrList(List<String> attrList){
		this.attrList = attrList;
	}
	
	public void addAttr(String key){
		if(attrList!=null && !attrList.contains(key)){
			attrList.add(key);
		}
	}
	
	public void setRate(double rate){
		this.rate = rate;
	}
	
	public void add(String source_id, String property_name) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class<?> thing = Class.forName("com.hhi.resouce.calc."+source_id+"_"+property_name);
		Calculation calc = (Calculation)thing.newInstance();
		calc.setManager(this);

		calcs.add(calc);
	}
	
	public void setTime(DateTime time){
		this.time = time;
	}
	
	public DateTime getTime(){
		return time;
	}
	
	public void setThingworxSave(boolean save){
		this.thingworxSave = save;
	}
	
	public void run(){
		
		List<Calculation> sortList = new ArrayList<Calculation>();
		for(Calculation calc : calcs){
			int thisOrder = calc.getOrder();
			boolean flag = false;
			int size = sortList.size();
			for(int i=0; i< size; i++){
				int order = sortList.get(i).getOrder();
				if(order > thisOrder){
					sortList.add(i, calc);
					flag = true;
					break;
				}
			}
			if(!flag){
				sortList.add(calc);
			}
		}
		
		//System.out.println("================  Calculation Manager Start =============== ");;
		long tstart = System.currentTimeMillis();
		
		for(Calculation calc : sortList){
			try {
				
				//System.out.print("start : " + calc.getClass().getName());;
				//long start = System.currentTimeMillis();
				calc.calculate();
				//System.out.println(" - " + (System.currentTimeMillis()-start)*0.001 + " sec");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//System.out.println("================  Calculation Manager End (" + (System.currentTimeMillis()-tstart)*0.001 + " sec)=============== ");;
	}

	public Thing getThing(String thingName){
		Thing thing = thingMap.get(thingName);
		if(thing==null){
			thing = ThingUtilities.findThing(thingName);
			thingMap.put(thingName, thing);
		}
		return thing;
	}

	public List<Thing> getEntity(String entityName, ThingworxRelationshipTypes type) throws Exception{
		
		List<Thing> list = entityMap.get(entityName);
		
		if(list==null){
			
			list = new ArrayList<Thing>();

			RootEntity entity = EntityUtilities.findEntity(entityName, type);
			InfoTable things = null;
			if(entity  instanceof ThingShape){
				things = ((ThingShape)entity).GetImplementingThings();
			}
			else if(entity  instanceof ThingTemplate){
				things = ((ThingTemplate)entity).GetImplementingThings();
			} 
			
			if(things!=null){
				for(int j = 0; things != null && j < things.getRowCount(); j++){
					String thingName = things.getRow(j).getStringValue("name");
					list.add(getThing(thingName));
				}
			}
		}
		return list;
	}
	
	public List<Thing> getFlowMeterChildThings(String flowMeterName) throws Exception {

		List<Thing> list = entityMap.get(flowMeterName);
		
		if(list==null){
			
			list = new ArrayList<Thing>();
			InfoTable infotable = getFlowMeterNetwork().GetChildConnections(flowMeterName);
			ValueCollectionList values = infotable.getRows();
			for(int i=0; i< values.size(); i++){
				
				ValueCollection value = values.get(i);
				String to = value.getStringValue("to");
				list.add(getThing(to));
			}
		}
		return list;
	}
	
	public String getLastNetworkThingName(String thingName) throws Exception {
		InfoTable childThings = getFlowMeterNetwork().GetChildConnections(thingName);

		if(childThings.getRowCount() > 0) {
			return getLastNetworkThingName(childThings.getRow(0).getStringValue("to"));
		} else {
			return thingName;
		}
	}
	
	public String getParentNetworkThingName(String thingName) throws Exception {
		String parentThingName = getFlowMeterNetwork().GetParentName(thingName);
		return parentThingName;
	}
	
	public String getFlowMeterParentThingName(String flowMeterName) throws Exception  {
		return getFlowMeterNetwork().GetParentName(flowMeterName);
	}
	
	public Network getFlowMeterNetwork(){
		if(flowMeterNetwork==null){
			flowMeterNetwork = (Network)EntityUtilities.findEntity("FlowMeterNetwork", ThingworxRelationshipTypes.Network);
		}
		
		return flowMeterNetwork;
	}

	public Resource getResource(String resourceName) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Resource resource = resourceMap.get(resourceName);
		if(resource==null){
			Class<?> t = Class.forName("com.hhi.resouce."+resourceName);
			resource = (Resource) t.newInstance();
			resourceMap.put(resourceName, resource);
		}
		return resource;
	}
	

	public List<String> getMEList() throws Exception{
		if(meThingNameList==null){
			meThingNameList = getLastNetworkThings("METotalThing");
		}
		return meThingNameList;
	}
	public List<String> getGEList() throws Exception{
		
		if(geThingNameList==null){
			geThingNameList = new ArrayList<String>();
			int gecount = getDECount();
			for(int i=1; i<= gecount; i++){
				geThingNameList.add("DG"+i+"Thing");
			}
		}
		return geThingNameList;
	}
	public List<String> getDEList() throws Exception{
		
		if(deThingNameList==null){
			deThingNameList = getLastNetworkThings("DETotalThing");
		}
		return deThingNameList;
	}
	public List<String> getSBList() throws Exception{
	
		if(sbThingNameList==null){
			sbThingNameList = getLastNetworkThings("SBTotalThing");
		}
		return sbThingNameList;
	}
	
	//FSRU ADD 2020.03.03
	public List<String> getRBList() throws Exception{
		
		if(rbThingNameList==null){
			rbThingNameList = getLastNetworkThings("RegasBoilerTotalThing");
		}
		return rbThingNameList;
	}
	//FSRU END
	
	public List<String> getShaftList() throws Exception{
		
		if(shaftThingNameList==null){
			shaftThingNameList = new ArrayList<String>();
			int sbcount = getMECount();
			for(int i=1; i<= sbcount; i++){
				shaftThingNameList.add("Shaft"+i+"Thing");
			}
		}
		return shaftThingNameList;
	}
	
	private  int getMECount() throws Exception{
		return getMEList().size();
	}
	
	private  int getDECount() throws Exception{
		return getDEList().size();
	}
	
	private  List<String> getLastNetworkThings(String thingName) throws Exception{
		
		List<String> thingNameList = new ArrayList<String>();
		InfoTable infotable = getFlowMeterNetwork().GetChildConnections(thingName);
		if(infotable.getRowCount()>0){
			getLastNetworkThings(thingName,thingNameList);	
		}
		return thingNameList;
	}
	
	private  void getLastNetworkThings(String thingName,List<String> thingNameList) throws Exception{
		InfoTable infotable = getFlowMeterNetwork().GetChildConnections(thingName);
		if(infotable.getRowCount()>0){
			ValueCollectionList values = infotable.getRows();
			for(int i=0; i< values.size(); i++){
				ValueCollection value = values.get(i);
				getLastNetworkThings(value.getStringValue("to"), thingNameList);
			}
		}else{
			thingNameList.add(thingName);
		}
	}
	
	public List<Thing> getFlowMeters() throws Exception{
		if(allFlowMeterList == null){
			allFlowMeterList = new ArrayList<Thing>();
			allFlowMeterList.addAll(getFlowMeterChildThings("METotalThing"));
			allFlowMeterList.addAll(getFlowMeterChildThings("DETotalThing"));
			allFlowMeterList.addAll(getFlowMeterChildThings("SBTotalThing"));
			
			//FSRU ADD 2020.03.03
			allFlowMeterList.addAll(getFlowMeterChildThings("RegasBoilerTotalThing"));
			//FSRU END
		}
		return allFlowMeterList;
	}
	
	public double getDoubleValue(String thingName, String propertyName){
		HistoryTableData data = valueMap.get(thingName + "_" + propertyName);
		double result = 0;
		if(data==null){
			try {
				result = getThing(thingName).GetNumberPropertyValue(propertyName);
			} catch (Exception e) {
				try {
					result = getThing(thingName).GetIntegerPropertyValue(propertyName);
				} catch (Exception e1) {
					System.out.println("Thing Name : " + thingName + " - Property Name :" + propertyName);
					e1.printStackTrace();
				}
			}
		}else{
			result = data.getValue();
		}
		return result;
	}
	
	public String getStringValue(String thingName, String propertyName){
		HistoryTableData data = valueMap.get(thingName + "_" + propertyName);
		String result = null;
		if(data==null){
			try {
				result = getThing(thingName).GetStringPropertyValue(propertyName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			result = data.getValue_text();
		}
		return result;
	}
	
	public boolean getBooleanValue(String thingName, String propertyName){
		HistoryTableData data = valueMap.get(thingName + "_" + propertyName);
		boolean result = false;
		if(data==null){
			try {
				result = getThing(thingName).GetBooleanPropertyValue(propertyName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String valueText = data.getValue_text();
			result = Boolean.parseBoolean(valueText);
		}
		return result;
	}
	
	public boolean isLocalBinding(String thingName,String propertyName){
		LocalPropertyBindingCollection binding = bindingMap.get(thingName);
		
		if(binding==null){
			Thing thing = getThing(thingName);
			binding = thing.getEffectiveLocalPropertyBindings();
			bindingMap.put(thingName, binding);
		}
		
		return binding.containsKey(propertyName);
	}
	

	
	public BoilerTempData[] getBoilerTable() throws Exception{
		
		if(boilerInfo==null){
			List<String> sblist = getSBList();
			boilerInfo = new BoilerTempData[sblist.size()];
			Thing vessel = getThing("ST.Vessel.Thing");
			int sbcount = 1; 

			for(int i=0; i< boilerTypes.length; i++){
				for(int j=1; j <= 2; j++){
					String property_name = boilerTypes[i]+j+"Boiler";
					System.out.println("Use Boiler "+property_name);
					InfoTablePrimitive table = (InfoTablePrimitive)vessel.getPropertyValue(property_name);

					if(table!=null){
						InfoTable info = table.getValue();
						System.out.println("table!=null "+property_name);
						if(info!=null && info.getRowCount()>2 && sbcount<=boilerInfo.length){
							System.out.println("sbcount-1 "+(sbcount-1));
							BoilerTempData data = new BoilerTempData();
							data.type = boilerTypes[i];
							data.infotable = info;
							data.typeCount = j;
							data.thingName = "MainEngine1Thing";
							boilerInfo[sbcount-1] = data;
							sbcount++;
						}
					}
				}
			}
		}
		return boilerInfo;
	}
	
	class BoilerTempData{
		public String type = "";
		public int typeCount = 0;
		public String thingName = "";
		public InfoTable infotable = null;
	}

}
