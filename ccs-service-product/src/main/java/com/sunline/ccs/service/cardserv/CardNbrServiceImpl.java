package com.sunline.ccs.service.cardserv;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsCardnbrGrt;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcCardNbrService;

/**
 * 生成卡号
 * 
 */
@Service
public class CardNbrServiceImpl implements CcCardNbrService {

	private static final Logger logger = LoggerFactory.getLogger(CardNbrServiceImpl.class);
	
	private static final String org = "000000000001";
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	private Map<String, Long> currMap = new ConcurrentHashMap<String, Long>();
	private Map<String, Long> maxMap = new ConcurrentHashMap<String, Long>();
	
	@Value("#{env['step']?:1000}")
	private Long step;
	
	/**
	 * 卡号
	 * @param productCd
	 * @return
	 */
	@Override
	@Transactional
    public synchronized long getCardNbr(String productCd) {
    	if(currMap.get(productCd)==null)
    		updateMap(productCd);
    	return getNextSeq(productCd);
    }
    

	/**
	 * 初始化map
	 */
	@PostConstruct
	@SuppressWarnings(value="all")
	@Transactional
	private void initMap(){
		Query query = em.createQuery("SELECT t FROM CcsCardnbrGrt t");
		 
		List<CcsCardnbrGrt> result = (List<CcsCardnbrGrt>)query.getResultList();
		
		for(CcsCardnbrGrt cardnbrGrt : result){
			geneNextGroup(cardnbrGrt.getProductCd());
		}
		
	}
	
	
	/**
	 * 更新map
	 * @param productCd
	 */
	private void updateMap(String productCd) {
		Query query = em.createQuery("SELECT t FROM CcsCardnbrGrt t where t.productCd=:productCd");
		query.setParameter("productCd", productCd);
		CcsCardnbrGrt cardnbrGrt = null;
		try{
			cardnbrGrt = (CcsCardnbrGrt)query.getSingleResult();
		}catch(EmptyResultDataAccessException e){
			OrganizationContextHolder.setCurrentOrg(org);
			Product product = unifiedParameterFacility.loadParameter(productCd, Product.class);
			cardnbrGrt = new CcsCardnbrGrt();
			cardnbrGrt.setOrg(org);
			cardnbrGrt.setProductCd(productCd);
			cardnbrGrt.setCurrValue(Long.parseLong(product.cardnoRangeFlr));
			em.persist(cardnbrGrt);
		}
		
		geneNextGroup(productCd);
	}
	
    /**
     * 获取下一个seq
     * @param productCd
     * @return
     */
	private long getNextSeq(String productCd)
    {
		if(currMap.get(productCd) < maxMap.get(productCd)){
			return currMap.put(productCd, currMap.get(productCd) + 1);
		}else{
			geneNextGroup(productCd);
			return currMap.put(productCd, currMap.get(productCd) + 1);
		}
    }
    
	/**
	 * 获取下一个seqgroup
	 * @param productCd
	 */
	private void geneNextGroup(String productCd)
    {
		if(logger.isDebugEnabled())
			logger.debug("取下一组数" + productCd);
		Query query = em.createQuery("update CcsCardnbrGrt t SET t.currValue = t.currValue+:step where t.productCd=:productCd");
		query.setParameter("step", step);
		query.setParameter("productCd", productCd);
		query.setLockMode(LockModeType.PESSIMISTIC_FORCE_INCREMENT);
		query.executeUpdate();
		query = em.createQuery("SELECT t.currValue FROM CcsCardnbrGrt t where t.productCd=:productCd");
		query.setParameter("productCd", productCd);
		Object obj = query.getSingleResult();
		long maxValue = Long.parseLong(obj.toString());
		long currVal = maxValue - step;
		maxMap.put(productCd, maxValue);
		currMap.put(productCd, currVal);
    }
	
}
