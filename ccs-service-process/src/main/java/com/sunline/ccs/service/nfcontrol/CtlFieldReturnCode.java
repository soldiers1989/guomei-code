package com.sunline.ccs.service.nfcontrol;

import com.sunline.ccs.service.api.Constants;

public enum CtlFieldReturnCode {

	BEYOND_EXPIRE_DATE

	{
		@Override
		String getReturnCode() {
			return Constants.ERRNEXP_CODE;
		}
	},
	NonHavePPin

	{
		@Override
		String getReturnCode() {
			return Constants.ERRNPIN_CODE;
		}
	},
	NonHaveQPin {
		@Override
		String getReturnCode() {
			return Constants.ERRNQIN_CODE;
		}
	},
	NON_ACTIVE {
		@Override
		String getReturnCode() {
			return Constants.ERRNOAC_CODE;
		}
	},
	NON_SUPP_CARD {
		@Override
		String getReturnCode() {
			return Constants.ERRSUPP_CODE;
		}
	},
	P_PIN_LOCK {
		@Override
		String getReturnCode() {
			return Constants.ERRLOCP_CODE;
		}
	},
	Q_PIN_LOCK {
		@Override
		String getReturnCode() {
			return Constants.ERRLOIC_CODE;
		}
	},
	A {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCA_CODE;
		}
	},
	B {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCB_CODE;
		}
	},
	C {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCC_CODE;
		}
	},
	D {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCD_CODE;
		}
	},
	E {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCE_CODE;
		}
	},
	F {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCF_CODE;
		}
	},
	G {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCG_CODE;
		}
	},
	H {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCH_CODE;
		}
	},
	I {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCI_CODE;
		}
	},
	J {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCJ_CODE;
		}
	},
	K {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCK_CODE;
		}
	},
	L {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCL_CODE;
		}
	},
	M {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCM_CODE;
		}
	},
	N {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCN_CODE;
		}
	},
	O {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCO_CODE;
		}
	},
	P {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCP_CODE;
		}
	},
	Q {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCQ_CODE;
		}
	},
	R {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCR_CODE;
		}
	},
	S {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCS_CODE;
		}
	},
	T {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCT_CODE;
		}
	},
	U {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCU_CODE;
		}
	},
	V {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCV_CODE;
		}
	},
	W {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCW_CODE;
		}
	},
	X {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCX_CODE;
		}
	},
	Y {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCY_CODE;
		}
	},
	Z {
		@Override
		String getReturnCode() {
			return Constants.ERRBLCZ_CODE;
		}
	},
	ZREO{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC0_CODE;
		}
	},
	ONE{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC1_CODE;
		}
	},
	TWO{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC2_CODE;
		}
	},
	THREE{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC3_CODE;
		}
	},
	FOUR{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC4_CODE;
		}
	},
	FIVE{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC5_CODE;
		}
	},
	SIX{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC6_CODE;
		}
	},
	SEVEN{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC7_CODE;
		}
	},
	EIGHT{
		String getReturnCode(){
			return Constants.ERRBLC8_CODE;
		}
	},
	NINE{
		@Override
		String getReturnCode(){
			return Constants.ERRBLC9_CODE;
		}
	};

	abstract String getReturnCode();
}
