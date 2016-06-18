package com.sunline.ccs.param.def.consts;

import com.sunline.ccs.param.def.enums.AuthReason;

public class AuthReasonGroups {

	/**
	 * 锁定码配置中可以使用的授权原因码列表
	 * 目前可选所有的授权原因码，包括系统原因码
	 */
	public static final AuthReason[] BLOCKCODE_REASONS = AuthReason.values();
	
	/**
	 * 授权通用检查原因码列表
	 * 用于卡产品授权参数配置
	 */
	public static final AuthReason[] COMMON_REASONS = 
			new AuthReason[]{AuthReason.A000, AuthReason.A001, AuthReason.A100,
							AuthReason.A101, AuthReason.A102, AuthReason.A103,
							AuthReason.A104, AuthReason.A109, AuthReason.B001,
							AuthReason.B002, AuthReason.B003, AuthReason.B004,
							AuthReason.B005, AuthReason.B006, AuthReason.B007,
							AuthReason.B008, AuthReason.B009, AuthReason.B010,AuthReason.B011,
							AuthReason.F001, AuthReason.F002, AuthReason.F003,
							AuthReason.F004, AuthReason.F005, AuthReason.F006,
							AuthReason.F007, AuthReason.F008, AuthReason.F009,
							AuthReason.F010, AuthReason.F011, AuthReason.F012,
							AuthReason.F013, AuthReason.F014, AuthReason.F015,
							AuthReason.F016, AuthReason.F017, AuthReason.F018,
							AuthReason.F019, AuthReason.F020, AuthReason.F021,
							AuthReason.F022, AuthReason.F023, AuthReason.F024,
							AuthReason.F025, AuthReason.F026, AuthReason.F027,
							AuthReason.F028, AuthReason.F029, AuthReason.F030,
							AuthReason.F031, AuthReason.F032, AuthReason.F033,
							AuthReason.F034, AuthReason.F035, AuthReason.F036,
							AuthReason.F037, AuthReason.F038, AuthReason.F039,
							AuthReason.F040, AuthReason.F041, AuthReason.F042,
							AuthReason.F043, AuthReason.F044, AuthReason.I001, AuthReason.I002,
							AuthReason.I005, AuthReason.I006, AuthReason.I007,
							AuthReason.I008, AuthReason.I009, AuthReason.I010, AuthReason.I011,
							AuthReason.M000, AuthReason.M001, AuthReason.R001,
							AuthReason.R002, AuthReason.R003, AuthReason.R004,
							AuthReason.R005, AuthReason.R006, AuthReason.R007,
							AuthReason.R008, AuthReason.R009, AuthReason.R010,
							AuthReason.R011, AuthReason.R012, AuthReason.R013,
							AuthReason.R014, AuthReason.R015, AuthReason.R016,
							AuthReason.R017, AuthReason.R018,
							AuthReason.TC01, AuthReason.TC02, AuthReason.TC03,
							AuthReason.TF01, AuthReason.TF02, AuthReason.TF03,
							AuthReason.TF04, AuthReason.TF05, AuthReason.TI01,
							AuthReason.TI02, AuthReason.TL01, AuthReason.TL51, 
							AuthReason.TL52, AuthReason.TL53, AuthReason.TL54, 
							AuthReason.TL55, AuthReason.TL56, AuthReason.TL57, 
							AuthReason.TL58, AuthReason.TM01, AuthReason.TM02, 
							AuthReason.TM03, AuthReason.TM04,
							AuthReason.TMC1, AuthReason.TMC2, AuthReason.TMC3,
							AuthReason.TO01, AuthReason.TO02, AuthReason.TS01,
							AuthReason.TS02, AuthReason.TS03, AuthReason.TT01,
							AuthReason.TT02, AuthReason.TT03, AuthReason.TT04, 
							AuthReason.TT05, AuthReason.TT06, AuthReason.TT07,
							AuthReason.TU01, AuthReason.TU02, AuthReason.TU03,
							AuthReason.V001, AuthReason.V002, AuthReason.V003,
							AuthReason.V004, AuthReason.V005, AuthReason.V006,
							AuthReason.V007, AuthReason.V008, AuthReason.V009,
							AuthReason.V010, AuthReason.V011, AuthReason.V012,
							AuthReason.V013, AuthReason.V014, AuthReason.V015,
							AuthReason.V016, AuthReason.V017, AuthReason.V018,
							AuthReason.V019, AuthReason.V020, AuthReason.V021,
							AuthReason.V022, AuthReason.V023, AuthReason.V024,
							AuthReason.V025, AuthReason.V026, AuthReason.V027,
							AuthReason.V028, AuthReason.V029, AuthReason.V030,
							AuthReason.V031, AuthReason.V101, AuthReason.V102,
							AuthReason.V203, AuthReason.V304, AuthReason.V033,
							AuthReason.V034, AuthReason.V035, AuthReason.V036,
							AuthReason.V037, AuthReason.V038, AuthReason.V039,
							AuthReason.V040, AuthReason.V041, AuthReason.V042,
							AuthReason.V043, AuthReason.V044, AuthReason.V045,
							AuthReason.V046, AuthReason.V047, AuthReason.V048,
							AuthReason.V049, AuthReason.MC01, AuthReason.MC02,
							AuthReason.VC01, AuthReason.VC02
							};
	
	/**
	 * 系统原因码列表
	 */
	public static final AuthReason[] SYSTEM_REASONS = 
			new AuthReason[]{AuthReason.S001, AuthReason.S002, AuthReason.S003, 
							AuthReason.S003, AuthReason.S004, AuthReason.S005,
							AuthReason.S006, AuthReason.S007, AuthReason.S008,
							AuthReason.S009, AuthReason.S010};
	
}
