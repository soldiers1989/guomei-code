package com.sunline.ccs.batch.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 检查job.xml文件中任务是否前后连续
 * @author houxh
 *
 */
public class CheckJobXml {
	@SuppressWarnings("unchecked")
	public static void main(String[] args)throws Exception {
		SAXReader reader=new SAXReader();
		Document doc = reader.read(CheckJobXml.class.getClassLoader().getResourceAsStream("jobs.xml"));
		
		Map<String, Element> m = new HashMap<String, Element>(); 
		List<Element> l = doc.getRootElement().elements();
		Element job = null;
		for(Element t:l){
			if("ccsJob".equals(t.attributeValue("id"))){
				job = t;
				break;
			}
		}
		l = job.elements();
		for(Element e : l){
			if(e.attributeValue("id")!=null){
				m.put(e.attributeValue("id"), e);
			}
		}
		String start = "before-clean-table";
		Element s = m.get(start);
		m.remove(start);
		while(s!=null){
			System.out.println(s.attributeValue("id")+"->"+s.attributeValue("next"));
			String key = s.attributeValue("next");
			s = m.get(key);
			m.remove(key);
		}
		
		if(m.keySet().size()!=0){
			for(String t : m.keySet()){
				System.err.println(t);
			}
			throw new Exception("有缺少上下文的作业步");
		}
		
	}
}

