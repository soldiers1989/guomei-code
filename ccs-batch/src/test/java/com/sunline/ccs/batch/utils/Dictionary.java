package com.sunline.ccs.batch.utils;

import java.util.ArrayList;
import java.util.List;



/**
 * 数据字典
 * 
 * @author yuyang
 * 
 */
public class Dictionary {
	
	public static List<String> idTypeList = new ArrayList<String>();
	public static List<String> empPositionAttrTypeList = new ArrayList<String>();
	public static List<String> occupationTypeList = new ArrayList<String>();
	public static List<String> maritalStatusList = new ArrayList<String>();
	public static List<String> educationTypeList = new ArrayList<String>();
	public static List<String> houseOwnershipList = new ArrayList<String>();
	public static List<String> houseTypeList = new ArrayList<String>();
	public static List<String> liquidAssetList = new ArrayList<String>();
	public static List<String> relationshipList = new ArrayList<String>();
	public static List<String> addrTypeList = new ArrayList<String>();
	public static List<String> telTypeList = new ArrayList<String>();
	public static List<String> productCodeList = new ArrayList<String>();
	public static List<String> branchIdList = new ArrayList<String>();
	

	static{
		
		idTypeList.add("I");
		idTypeList.add("T");
		idTypeList.add("S");
		idTypeList.add("P");
		idTypeList.add("L");
		idTypeList.add("O");
		idTypeList.add("R");
		idTypeList.add("H");
		idTypeList.add("W");
		idTypeList.add("F");
		idTypeList.add("C");
		
		empPositionAttrTypeList.add("A");
		empPositionAttrTypeList.add("B");
		empPositionAttrTypeList.add("C");
		empPositionAttrTypeList.add("D");
		empPositionAttrTypeList.add("E");
		empPositionAttrTypeList.add("F");
		empPositionAttrTypeList.add("G");
		empPositionAttrTypeList.add("H");
		empPositionAttrTypeList.add("I");
		empPositionAttrTypeList.add("J");
		empPositionAttrTypeList.add("K");
		empPositionAttrTypeList.add("L");
		empPositionAttrTypeList.add("M");
		empPositionAttrTypeList.add("N");
		empPositionAttrTypeList.add("O");
		empPositionAttrTypeList.add("P");
		empPositionAttrTypeList.add("Q");
		empPositionAttrTypeList.add("R");
		empPositionAttrTypeList.add("S");
		empPositionAttrTypeList.add("Z");
		
		occupationTypeList.add("A");
		occupationTypeList.add("B");
		occupationTypeList.add("C");
		occupationTypeList.add("D");
		occupationTypeList.add("E");
		occupationTypeList.add("F");
		occupationTypeList.add("G");
		occupationTypeList.add("H");
		
		maritalStatusList.add("C");
		maritalStatusList.add("D");
		maritalStatusList.add("M");
		maritalStatusList.add("S");
		maritalStatusList.add("W");
		
		educationTypeList.add("A");
		educationTypeList.add("B");
		educationTypeList.add("C");
		educationTypeList.add("D");
		educationTypeList.add("E");
		educationTypeList.add("F");
		educationTypeList.add("G");

		houseOwnershipList.add("A");
		houseOwnershipList.add("B");
		houseOwnershipList.add("C");
		houseOwnershipList.add("D");
		houseOwnershipList.add("E");
		houseOwnershipList.add("Z");
		
		houseTypeList.add("A");
		houseTypeList.add("B");
		houseTypeList.add("C");
		houseTypeList.add("D");
		houseTypeList.add("E");
		houseTypeList.add("F");
		houseTypeList.add("G");
		houseTypeList.add("H");
		houseTypeList.add("I");
		houseTypeList.add("Z");

		liquidAssetList.add("A");
		liquidAssetList.add("B");
		liquidAssetList.add("C");
		liquidAssetList.add("D");
		liquidAssetList.add("E");
		
		relationshipList.add("B");
		relationshipList.add("C");
		relationshipList.add("F");
		relationshipList.add("L");
		relationshipList.add("M");
		relationshipList.add("O");
		relationshipList.add("P");
		relationshipList.add("S");
		relationshipList.add("W");
		
		addrTypeList.add("C");
		addrTypeList.add("E");
		addrTypeList.add("H");
		addrTypeList.add("M");
		addrTypeList.add("N");
		addrTypeList.add("O");
		addrTypeList.add("R");
		addrTypeList.add("S");
		
		telTypeList.add("COM");
		telTypeList.add("HOME");
		telTypeList.add("MOBI");
		telTypeList.add("OTH");

		productCodeList.add("P1");
		productCodeList.add("P2");
		productCodeList.add("P3");
		
		branchIdList.add("001010101");
		branchIdList.add("001010102");
	}
}
