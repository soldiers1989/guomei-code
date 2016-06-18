package com.sunline.ccs.batch.rpt.cca000;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ark.batch.KeyBasedStreamReader;


public class RA009YGStatInfo extends KeyBasedStreamReader<Integer, Integer>{

	@Override
	protected List<Integer> loadKeys() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		return list;
	}

	@Override
	protected Integer loadItemByKey(Integer key) {
		
		return key;
	}

}
