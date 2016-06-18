package com.sunline.ccs.ui.client.commons;
///**
// * 
// */
//package com.sunline.ccs.ui.client.pub;
//
//import com.smartgwt.client.widgets.grid.CellFormatter;
//import com.smartgwt.client.widgets.grid.ListGridRecord;
//
///**
// * 银联8583报文第7域的交易传输日期格式转换 MMDDhhmmss -> MM-DD hh:mm:ss
//* @author fanghj
// *
// */
//public class TransmissionTimestampCellFormatter implements CellFormatter {
//
//	/* (non-Javadoc)
//	 * @see com.smartgwt.client.widgets.grid.CellFormatter#format(java.lang.Object, com.smartgwt.client.widgets.grid.ListGridRecord, int, int)
//	 */
//	@Override
//	public String format(Object value,
//			ListGridRecord record, int rowNum, int colNum) {
//		if(null == value) {
//			return null;
//		}
//		
//		String valueStr = value.toString().trim();
//		if(valueStr.length() != 10) {
//			return value.toString();
//		}
//		
//		//format: MMDDhhmmss -> MM-DD hh:mm:ss
//		return valueStr.substring(0, 2)+"-"
//				+valueStr.substring(2, 4)+" "
//				+valueStr.substring(4, 6)+":"
//				+valueStr.substring(6, 8)+":"
//				+valueStr.substring(8, 10);
//	}
//
//}
