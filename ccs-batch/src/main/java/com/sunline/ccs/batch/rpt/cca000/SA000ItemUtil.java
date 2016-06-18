package com.sunline.ccs.batch.rpt.cca000;

import com.sunline.ppy.dictionary.enums.IdType;

public class SA000ItemUtil {
	
	public static String getYgIdType(IdType type){
		if(type == null){
			return "";
		}
		String value;
		switch (type) {
		case I:   value="I1"; break;
		case T:   value="I1"; break;
		case S:   value="I3"; break;
		case P:   value="I2"; break;
		case R:   value="I6"; break;
		case H:   value="I5"; break;
		case W:   value="I9"; break;
		case F:   value="Ia"; break;
		case C:   value="I4"; break;
		default:  value="I8"; break;
		}
		return value;
	}

}
