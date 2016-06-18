
/***********************性能测试优化sql start, add by lizz 20151017***********************/
 
--生成卡号用序列
DROP SEQUENCE CCS_CARDNBR_GRT_SEQ;
CREATE SEQUENCE CCS_CARDNBR_GRT_SEQ INCREMENT BY 1 START WITH ?;--根据CCS_CARDNBR_GRT 最大值设置

--drop 索引
drop INDEX IDX_CCS_ACCT_001;
drop INDEX IDX_CCS_ACCT_002;
drop index idx_ccs_acct_003;
drop index idx_ccs_acct_004;
drop INDEX IDX_CCS_ACCT_O_002;
drop index idx_ccs_order_001;
drop index idx_ccs_order_002;
drop index idx_ccs_card_002;
drop index idx_ccs_authmemo_o_001;
drop INDEX IDX_CCS_AUTHMEMO_O_002;
drop index idx_ccs_repay_schedule_001;
drop index idx_ccs_repay_schedule_002;
drop index idx_ccs_repay_schedule_003;
drop index idx_ccs_loan_reg_001;
drop index idx_ccs_loan_reg_002;
drop index idx_ccs_loan_reg_003;
drop index idx_ccs_loan_reg_hist_001;
drop index idx_ccs_posting_tmp_001;
drop INDEX IDX_CCS_LOAN_001;
drop INDEX IDX_CCS_LOAN_002;
drop index idx_ccs_loan_003;
drop index idx_ccs_loan_004;
drop index idx_ccs_loan_005;
drop index idx_ccs_loan_006;
drop INDEX IDX_CCS_PLAN_001;
drop index idx_ccs_plan_002;
drop index idx_ccs_txn_hst_001;
drop INDEX IDX_CCS_CUSTOMER_001;
drop INDEX IDX_CCS_ADDRESS_001;
drop INDEX IDX_CCS_LINKMAN_001;
drop INDEX IDX_CCS_EMPLOYEE_001;
drop INDEX IDX_CCS_TXN_UNSTATEMENT_001;
drop INDEX IDX_CCS_STATEMENT_001;
drop INDEX IDX_CCS_STATEMENT_002;

CREATE INDEX IDX_CCS_ACCT_001 ON CCS_ACCT (CUST_ID ASC);
CREATE INDEX IDX_CCS_ACCT_002 ON CCS_ACCT (GUARANTY_ID ASC);
create unique index idx_ccs_acct_003 on ccs_acct (contr_nbr);
create index idx_ccs_acct_004 on ccs_acct (acct_nbr,acct_type,org);
CREATE INDEX IDX_CCS_ACCT_O_002 ON CCS_ACCT_O (CUST_LMT_ID ASC);
CREATE INDEX IDX_CCS_ORDER_001 ON CCS_ORDER (ACCT_NBR ASC, ACCT_TYPE ASC);
create index idx_ccs_order_002 on ccs_order (servicesn,acq_id);
create index idx_ccs_card_002 on ccs_card (acct_nbr);
create unique index idx_ccs_authmemo_o_001 on ccs_authmemo_o (acct_nbr,acct_type,AUTH_TXN_STATUS,LOG_BIZ_DATE,FINAL_ACTION,TXN_DIRECTION,LOG_KV);
CREATE INDEX IDX_CCS_AUTHMEMO_O_002 ON CCS_AUTHMEMO_O (ACCT_NBR ASC, ACCT_TYPE ASC);
create index idx_ccs_repay_schedule_001 on ccs_repay_schedule (acct_nbr,acct_type);
create index idx_ccs_repay_schedule_002 on ccs_repay_schedule (loan_id,LOAN_PMT_DUE_DATE);
create index idx_ccs_repay_schedule_003 on ccs_repay_schedule (CURR_TERM);
CREATE INDEX IDX_CCS_LOAN_REG_001 ON CCS_LOAN_REG(GUARANTY_ID ASC);
create unique index idx_ccs_loan_reg_002 on ccs_loan_reg (LOGIC_CARD_NBR,REF_NBR);
create index idx_ccs_loan_reg_003 on ccs_loan_reg (DUE_BILL_NO,LOAN_ACTION);
create index idx_ccs_loan_reg_hist_001 on ccs_loan_reg_hst (DUE_BILL_NO,LOAN_ACTION);
create index idx_ccs_posting_tmp_001 on ccs_posting_tmp (acct_nbr,acct_type);
CREATE INDEX IDX_CCS_LOAN_001 ON CCS_LOAN (ACCT_NBR ASC, ACCT_TYPE ASC);
CREATE INDEX IDX_CCS_LOAN_002 ON CCS_LOAN(GUARANTY_ID ASC);
create unique index idx_ccs_loan_003 on ccs_loan (loan_id,ref_nbr);
create index idx_ccs_loan_004 on ccs_loan (card_nbr,ORIG_AUTH_CODE,ORIG_TXN_AMT);
create index idx_ccs_loan_005 on ccs_loan extract(ORIG_TRANS_DATE);
create index idx_ccs_loan_006 on ccs_loan (LOGIC_CARD_NBR,REF_NBR);
CREATE INDEX IDX_CCS_PLAN_001 ON CCS_PLAN (ACCT_NBR ASC, ACCT_TYPE ASC);
create index idx_ccs_plan_002 on ccs_plan (ref_nbr,plan_type,term);
create index idx_ccs_txn_hst_001 on ccs_txn_hst (acct_nbr,acct_type,STMT_DATE);
CREATE INDEX IDX_CCS_CUSTOMER_001 ON CCS_CUSTOMER (ID_NO ASC, ID_TYPE ASC);
CREATE INDEX IDX_CCS_ADDRESS_001 ON CCS_ADDRESS (CUST_ID ASC);
CREATE INDEX IDX_CCS_LINKMAN_001 ON CCS_LINKMAN (CUST_ID ASC);
CREATE INDEX IDX_CCS_EMPLOYEE_001 ON CCS_EMPLOYEE (CUST_ID ASC);
CREATE INDEX IDX_CCS_TXN_UNSTATEMENT_001 ON CCS_TXN_UNSTATEMENT (ACCT_NBR ASC, ACCT_TYPE ASC);
CREATE INDEX IDX_CCS_STATEMENT_001 ON CCS_STATEMENT (ACCT_NBR ASC, ACCT_TYPE ASC);
CREATE INDEX IDX_CCS_STATEMENT_002 ON ccs_statement (STMT_DATE ASC);




/***********************性能测试优化sql end, add by lizz 20151017***********************/
--add by lizz
alter table CCS_AUTHMEMO_O modify(REJ_REASON varchar2(100));
--add by lizz 20151021
alter table CCS_AUTHMEMO_HST modify(REJ_REASON varchar2(100));
--add by zhangqx
alter table CCS_ADDRESS modify(COUNTRY_CODE null);
alter table CCS_ADDRESS modify(PHONE null);
alter table CCS_ADDRESS modify(ADDRESS null);