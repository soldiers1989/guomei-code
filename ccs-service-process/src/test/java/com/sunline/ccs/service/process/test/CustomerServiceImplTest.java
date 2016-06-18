package com.sunline.ccs.service.process.test;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.service.process.CustomerServiceImpl;
import com.sunline.ccs.service.process.test.util.DataFixtures;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ppy.dictionary.exception.ProcessException;


/** 
* @author fanghj
* @version 创建时间：2012-8-25 下午15:15
* CustomerServiceImpl测试类
*/ 

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class CustomerServiceImplTest {
	
	@Autowired
	private RCcsAddress rCcsAddress;
	@Autowired
	private RCcsLinkman rCcsLinkman;
	
	@Autowired
	private CustomerServiceImpl cpsCustomerServiceImpl;

	private Integer cust_Id = Integer.valueOf(DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_CustId)).toString());
	private String org = DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_Org)).toString();

	private String id_No = DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdNo)).toString();
	private String id_Type = DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdType)).toString();
	
	private String card_No = DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_CardNbr)).toString();
	

	/**
	 * 
	 */
	@Before
	public void setupDatabase() {
		OrganizationContextHolder.setCurrentOrg(org);
		OrganizationContextHolder.setUsername("dfd");
	}
	


	/**
	 * 
	 */
	@Test
	public void testNF0000(){
		try {
			String addType = DataFixtures.getColunmValue(0, CcsAddress.TABLE_NAME,
					DataFixtures.getColumnName(CcsAddress.class, CcsAddress.P_AddrType)).toString();
			Map<String,Serializable> ccsCustomerMap = cpsCustomerServiceImpl.NF0000(card_No, AddressType.valueOf(addType));
			Assert.assertNotNull(ccsCustomerMap);
		} catch (ProcessException e) {			
			e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	@Test
	public void testNF1101() {
		try {
			Map<String, Serializable> map = cpsCustomerServiceImpl.NF1101(card_No);
			Assert.assertNotNull(map);
			Assert.assertEquals((String) map.get(CcsCustomer.P_CustId).toString(), this.cust_Id.toString());
		} catch (ProcessException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF1102() {

		try {
			Map<String, Serializable> map = cpsCustomerServiceImpl.NF1102(IdType.valueOf(this.id_Type), this.id_No);
			Assert.assertNotNull(map);
			Assert.assertEquals((String) map.get(CcsCustomer.P_CustId).toString(), this.cust_Id.toString());
		} catch (ProcessException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF1103() {
		try {
			Map<String, Serializable> customermap = cpsCustomerServiceImpl.NF1103(cust_Id);
			Assert.assertNotNull(customermap);
			String cust_id = customermap.get(CcsCustomer.P_CustId).toString();
			Assert.assertEquals(cust_Id.toString(), cust_id);
		} catch (ProcessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void testNF1105() {
		try {
			List<Map<String, Serializable>> address = cpsCustomerServiceImpl.NF1105(cust_Id);
			for(Map<String, Serializable> tmaddressmap :address){
				Assert.assertEquals(tmaddressmap.get(CcsAddress.P_CustId), cust_Id);
			}
		} catch (ProcessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void testNF1107() {
		try {
			List<Map<String, Serializable>> tmContact = cpsCustomerServiceImpl.NF1107(cust_Id);
			for(Map<String, Serializable> tmcontactmap :tmContact){
				Assert.assertEquals(tmcontactmap.get(CcsLinkman.P_CustId), cust_Id);
			}
		} catch (ProcessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF1201() {
		Map<String, Serializable> customermap;
		try {
			customermap = cpsCustomerServiceImpl.NF1103(cust_Id);
			customermap.put(CcsCustomer.P_IdNo, id_No);
			cpsCustomerServiceImpl.NF1201(customermap);
			Assert.assertEquals(cpsCustomerServiceImpl.NF1103(cust_Id).get(CcsCustomer.P_IdNo), id_No);
		} catch (ProcessException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF1202() {
		try {
		Iterator<Map<String, Serializable>> addrlist = cpsCustomerServiceImpl.NF1105(this.cust_Id).iterator();
		Assert.assertTrue(addrlist.hasNext());
		Map<String, Serializable> addrmap = addrlist.next();
		Long address_id = (Long) addrmap.get(CcsAddress.P_AddrId);
		String str_city = "beijing";
		addrmap.put(CcsAddress.P_City, str_city);		
			//cpsCustomerServiceImpl.NF1202(this.cust_Id, addrmap);
		Assert.assertEquals(str_city, rCcsAddress.findOne(address_id).getCity());
		} catch (ProcessException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF1203() {
		try {
			Iterator<Map<String, Serializable>> contactlist = cpsCustomerServiceImpl.NF1107(this.cust_Id).iterator();
			Assert.assertTrue(contactlist.hasNext());
			Map<String, Serializable> contactmap = contactlist.next();
			Long contact_id = (Long) contactmap.get(CcsLinkman.P_LinkmanId);
			String str_mobile_no = "1351234568";
			contactmap.put(CcsLinkman.P_MobileNo, str_mobile_no);		
				//cpsCustomerServiceImpl.NF1203(this.cust_Id, contactmap,);
				CcsLinkman rt = rCcsLinkman.findOne(contact_id);
			Assert.assertEquals(str_mobile_no, rt.getMobileNo());
			} catch (ProcessException e) {
				
				e.printStackTrace();
			}
	}

}
