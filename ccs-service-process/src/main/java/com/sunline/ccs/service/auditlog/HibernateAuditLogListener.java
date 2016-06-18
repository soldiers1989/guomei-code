package com.sunline.ccs.service.auditlog;

import java.util.Date;
import java.util.Map;

import org.hibernate.event.EventSource;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsOpAuditLog;

public class HibernateAuditLogListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

	private Map<String, Map<String, String>> auditEntitys;

	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String INSERT = "INSERT";

	private static final String UPDATE = "UPDATE";

	private static final String DELETE = "DELETE";

	
	/**
	 * 允许或不允许全部时，指定all即可
	 */
	public static final String ALL = "all";

	@Override
	public void onPostInsert(PostInsertEvent event) {
		if (auditEntitys.containsKey(event.getEntity().getClass().getSimpleName())) {
			// 保存 插入日志
			CcsOpAuditLog log = newOpAuditLog();
			log.setEntityName(event.getEntity().getClass().getSimpleName());
			log.setEntityId(event.getId().toString());
			log.setOptType(INSERT);
			{
				Object[] state = event.getState();
				String[] fields = event.getPersister().getPropertyNames();
				String content = "";
				if (state != null && fields != null && state.length == fields.length) {
					for (int i = 0; i < fields.length; i++) {
						if (isLog(event.getEntity(), fields[i], INSERT)) {
							content = addStr(null, state, fields, content, i);
						}
					}
				}
				log.setAuditContent("[" + content + "]");
			}
			logger.debug("插入审计日志 INSERT Tm_Audit_Log ");
			insert(event.getSession(), log);
		}
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (auditEntitys.containsKey(event.getEntity().getClass().getSimpleName())) {
			// 保存 修改日志
			CcsOpAuditLog log = newOpAuditLog();
			log.setEntityName(event.getEntity().getClass().getSimpleName());
			log.setEntityId(event.getId().toString());
			log.setOptType(UPDATE);
			{
				Object[] oldState = event.getOldState();
				Object[] newState = event.getState();
				String[] fields = event.getPersister().getPropertyNames();
				String content = "";
				if (oldState != null && newState != null && fields != null && oldState.length == newState.length && oldState.length == fields.length) {
					for (int i = 0; i < fields.length; i++) {
						if (isLog(event.getEntity(), fields[i], UPDATE)) {
							if ((newState[i] == null && oldState[i] != null) || (newState[i] != null && !newState[i].equals(oldState[i]))) {
								content = addStr(oldState, newState, fields, content, i);
							}
						}
					}
				}
				log.setAuditContent("[" + content + "]");
			}
			logger.debug("插入审计日志 UPDATE TmAuditLog ");
			insert(event.getSession(), log);
		}
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if (auditEntitys.containsKey(event.getEntity().getClass().getSimpleName())) {
			// 保存 删除日志
			CcsOpAuditLog log = newOpAuditLog();
			log.setEntityName(event.getEntity().getClass().getSimpleName());
			log.setEntityId(event.getId().toString());
			log.setOptType(UPDATE);
			{
				Object[] state = event.getDeletedState();
				String[] fields = event.getPersister().getPropertyNames();
				String content = "";
				if (state != null && fields != null && state.length == fields.length) {
					for (int i = 0; i < fields.length; i++) {
						if (isLog(event.getEntity(), fields[i], DELETE)) {
							content = addStr(null, state, fields, content, i);
						}
					}
				}
				log.setAuditContent("[" + content + "]");
			}
			logger.debug("插入审计日志 DELETE TmAuditLog ");
			insert(event.getSession(), log);
		}
	}

	/**
	 * @param log
	 * @param eventSource
	 *            记录审计日志
	 * 
	 * @Title: insert
	 * @param log
	 *            void
	 * @throws
	 */
	private void insert(EventSource eventSourcesess, CcsOpAuditLog log) {
		eventSourcesess.save(log);
	}

	/**
	 * 创建日志对象，同时设置操作人操作时间等信息
	 * 
	 * @Title: newTmAuditLog
	 * @return TmAuditLog
	 * @throws
	 */
	private CcsOpAuditLog newOpAuditLog() {
		CcsOpAuditLog log = new CcsOpAuditLog();
		log.setOptDatetime(new Date());
		log.setOpUpdateId(OrganizationContextHolder.getUsername());
		log.setOrg(OrganizationContextHolder.getCurrentOrg());
		return log;
	}

	/**
	 * 验证策略是否允许记录日志，规则如下：
	 * <ol>
	 * <li>可用的关键字有：insertAllow,insertDeny,updateAllow,updateDeny,deleteAllow,
	 * deleteDeny</li>
	 * <li>没有配置对象的策略，所有字段不记录</li>
	 * <li>allow和deny都配置的按allow验证，并忽略deny</li>
	 * <li>allow和deny都允许指定all关键字</li>
	 * <li>多个字段用英文逗号隔开</li>
	 * </ol>
	 * 
	 * @Title: isLog
	 * @param entity
	 * @param string
	 * @param string2
	 * @return boolean
	 * @throws
	 */
	private boolean isLog(Object entity, String field, String op) {
		Map<String, String> entityConfig = auditEntitys.get(entity.getClass().getSimpleName());
		if (entityConfig != null) {
			String allowFields = entityConfig.get(op.toLowerCase() + "Allow");
			if (allowFields != null) {
				if (allowFields.equals(ALL) || containsField(allowFields, field)) {
					// 配置ALL，所有允许
					return true;
				}
			} else {
				String denyFields = entityConfig.get(op.toLowerCase() + "Deny");
				if (denyFields != null) {
					if (denyFields.equals(ALL) || containsField(denyFields, field)) {
						// 配置ALL，所有不允许
						return false;
					}
				}
				return true;
			}
		} else {
		}
		// 缺省不记录
		return false;
	}

	/**
	 * 配置中是否包含当前字段
	 * 
	 * @Title: containsField
	 * @param fields
	 * @param field
	 * @return boolean
	 * @throws
	 */
	private boolean containsField(String fields, String field) {
		logger.debug("containsField...fields = {" + fields + "},field = {" + field + "}");
		String[] fs = fields.split(",");
		for (String f : fs) {
			if (f.equals(field)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 向content追加一个修改项
	 * 
	 * @Title: addStr
	 * @param oldState
	 * @param newState
	 * @param fields
	 * @param content
	 * @param i
	 * @return String
	 * @throws
	 */
	private String addStr(Object[] oldState, Object[] newState, String[] fields, String content, int i) {
		if (content.length() < 30000) {
			if (content.length() > 0) {
				content += ",";
			}
			content += "{columnName:\"" + fields[i] + "\",oldValue:\"" + (oldState == null ? "" : String.valueOf(oldState[i])) + "\",newValue:\"" + String.valueOf(newState[i]) + "\"}";
		} else {
			logger.warn("审计长度超过30000");
		}
		return content;
	}

	public void setAuditEntitys(Map<String, Map<String, String>> auditEntitys) {
		this.auditEntitys = auditEntitys;
	}

}
