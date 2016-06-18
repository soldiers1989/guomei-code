package com.sunline.ccs.ui.client.pages.lockingcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.param.def.enums.BlockLevel;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.PublicConstants;
import com.sunline.ccs.ui.client.commons.UIUtil;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 锁定码维护
 * 
 * @author linxc 2015年6月22日
 *
 */
@Singleton
public class LockingCodeMaintain extends Page {

    public static final String PAGE_ID = "tran-3404";

    @Inject
    private LockingCodeMaintainConstants constants;

    @Inject
    private PublicConstants publicConstants;
    //
    // @Inject
    // private T3404InterAsync server;

    @Inject
    private UIUtil uiUtil;

    @Inject
    private UCcsAcct uCcsAcct;

    private TextColumnHelper cardItem;

    private KylinForm formSearch;

    private EnumColumnHelper accountTypeItem;

    private String cardNo;

    private String acctType;

    private String acctBlockCode;// 账户层锁定码

    //private String cardBlockCode;// 卡片层锁定码

    //private String bothBlockCode;// 共用层锁定码

    private Map<String, Map> allBLMap;// 所有锁定码模板参数信息

    //private String submitBothBlockCode;// 更新时共用层提交的锁定码

    private String submitAcctBlockCode;// 更新时账户层提交的锁定码

    //private String submitCardBlockCode;// 更新时卡片层提交的锁定码

    private KylinForm acctBLForm;// 账户层表单

    //private KylinForm cardBLForm;// 卡片层表单

    //private KylinForm bothBLForm;

    private HorizontalPanel submitLayout;// 最底部提交按钮层

    private KylinButton buttonSubmit;// 共用锁定码form中的提交按钮
    /* 账户级别section-
     * 
     *  不使用(lsy-20150912)
     */
    //private StackPanel sectionBoth;// 共用级别section

    private StackPanel sectionAcct;// 账户级别section
    /* 卡片级别section-
     * 
     *  不使用(lsy-20150912)
     */
    
    //private StackPanel sectionCard;

    @Override
    public IsWidget createPage() {
	VerticalPanel page = new VerticalPanel();
	page.setWidth("100%");
	page.setHeight("100%");
	page.setSpacing(0);
	page.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	page.add(createSearchForm());
//	sectionBoth = new StackPanel();
//	sectionBoth.setWidth("98%");
//	sectionBoth.setHeight("100px");
	sectionAcct = new StackPanel();
	sectionAcct.setWidth("98%");
	sectionAcct.setHeight("100px");
//	sectionCard = new StackPanel();
//	sectionCard.setWidth("98%");
//	sectionCard.setHeight("100px");
	// 共用锁定码部分
//	createBothDynamicForm();
	// 账户层锁定码部分
	createAcctDynamicForm();
	// 卡片层锁定码部分
//	createCardDynamicForm();
	page.add(sectionAcct);
//	page.add(sectionBoth);
//	page.add(sectionCard);
	page.add(createSubmitBtnLayout());
	return page;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sunline.kylin.web.core.client.res.Page#refresh()
     */
    @Override
    public void refresh() {
	formSearch.getUi().clear();
	// TODO
	JavaScriptObject jsoAcctBLForm = acctBLForm.getUi().getJsElement();
	if (jsoAcctBLForm != null) {
	    acctBLForm.getUi().clear();
	}
//	JavaScriptObject jsoCardBLForm = cardBLForm.getUi().getJsElement();
//	if (jsoCardBLForm != null) {
//	    cardBLForm.getUi().clear();
//	}
//	JavaScriptObject jsoBothBLForm = bothBLForm.getUi().getJsElement();
//	if (jsoBothBLForm != null) {
//	    bothBLForm.getUi().clear();
//	}
    }

    /**
     * 创建卡号输入框
     * */
    private HorizontalPanel createSearchForm() {
	// 搜索框
	formSearch = new KylinForm();
	formSearch.setCol(2);
	formSearch.setWidth("98%");
	cardItem = uiUtil.createCardNoItem();
	accountTypeItem = uCcsAcct.AcctType().required(true).asSelectItem(SelectType.KEY_LABLE);
	formSearch.setField(cardItem, accountTypeItem);
	KylinButton searchButton = ClientUtils.createSearchButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		if (!formSearch.valid()) {
		    Dialog.alertWarn(publicConstants.msgSearchConditionInvalid(), "提示");
		    return;
		}
		cardNo = UIUtil.getCardNo(formSearch);
		acctType = formSearch.getFieldValue(CcsAcct.P_AcctType);
		// 清除缓存
		// acctBLForm.getUi().clear();
		// cardBLForm.getUi().clear();
		// bothBLForm.getUi().clear();
		// 设置所有可选
		setDisableTrue(acctBLForm);//, cardBLForm, bothBLForm);
		// 加载共用锁定码
		//loadBlockeCode(cardNo, AccountType.valueOf(acctType), BlockLevel.All);
		// 加载账户层锁定码
		loadBlockeCode(cardNo, AccountType.valueOf(acctType), BlockLevel.ACCT);
		// 加载卡片层锁定码
		//loadBlockeCode(cardNo, AccountType.valueOf(acctType), BlockLevel.CARD);
	    }
	});
	return CommonUiUtils.lineLayoutForm(formSearch, searchButton, "100%","100px");
    }

    /**
     * 创建共用锁定码form
     * 
     * @return
     */
    /*
    private KylinForm createBothDynamicForm() {
	bothBLForm = new KylinForm();
	RPC.ajax("rpc/lockingCodeMaintainServer/getBlockCode", new RpcCallback<Data>(){
	    @SuppressWarnings({"unchecked", "rawtypes"})
	    @Override
	    public void onSuccess(Data result) {
		// 复制返回的锁定码模板信息
		allBLMap = new LinkedHashMap<String, Map>();
		allBLMap.putAll((Map<String, Map>)result.asMapData().toMap());
		bothBlockCode = getStringByMap(allBLMap);
		createKylinForm(allBLMap, bothBLForm);
		// bothBLForm = createKylinForm(allBLMap);
		VerticalPanel bothlayout = new VerticalPanel();
		bothlayout.setWidth("98%");
		bothlayout.add(bothBLForm);
		bothlayout.setHeight("100px");
		sectionBoth.add(bothlayout, constants.allBlockCodeTitle());
	    }
	}, BlockLevel.All.toString());
	return bothBLForm;
    }
    */

    /**
     * 创建账户层锁定码
     * 
     * @return
     */
    private KylinForm createAcctDynamicForm() {
	acctBLForm = new KylinForm();
	RPC.ajax("rpc/lockingCodeMaintainServer/getBlockCode", new RpcCallback<Data>(){
	    @SuppressWarnings({"all"})
	    @Override
	    public void onSuccess(Data result) {
		// 复制返回的锁定码模板信息
		allBLMap = new LinkedHashMap<String, Map>();
		allBLMap.putAll((Map<String, Map>)result.asMapData().toMap());
		acctBlockCode = getStringByMap(allBLMap);
		createKylinForm(allBLMap, acctBLForm);
		VerticalPanel bothlayout = new VerticalPanel();
		// bothlayout.setOverflow(Overflow.AUTO);
		bothlayout.setWidth("98%");
		bothlayout.add(acctBLForm);
		bothlayout.setHeight("100px");
		sectionAcct.add(bothlayout, constants.acctBlockCodeTitle());
	    }
	}, BlockLevel.ACCT.toString());
	return acctBLForm;
    }

    /**
     * 创建卡片层锁定码
     * 
     * @return
     */
    /*
    public KylinForm createCardDynamicForm() {
	cardBLForm = new KylinForm();
	RPC.ajax("rpc/lockingCodeMaintainServer/getBlockCode", new RpcCallback<Data>(){

	    @SuppressWarnings({"unchecked", "rawtypes"})
	    @Override
	    public void onSuccess(Data result) {
		// 复制返回的锁定码模板信息
		allBLMap = new LinkedHashMap<String, Map>();
		allBLMap.putAll((Map<String, Map>)result.asMapData().toMap());
		cardBlockCode = getStringByMap(allBLMap);
		VerticalPanel cardlayout = new VerticalPanel();
		createKylinForm(allBLMap, cardBLForm);
		cardlayout.setWidth("98%");
		cardlayout.add(cardBLForm);
		cardlayout.setHeight("100px");
//		sectionCard.add(cardlayout, constants.cardBlockCodeTitle());
	    }
	}, BlockLevel.CARD.toString());
	return cardBLForm;
    }
	*/
    
    
    /**
     * 点击查询按钮时，显示该卡片和对应账户的锁定码
     * 
     * @param cardNo
     * @param accountType
     * @param bl
     */
    public void loadBlockeCode(final String cardNo, final AccountType accountType, final BlockLevel bl) {
	RPC.ajax("rpc/lockingCodeMaintainServer/loadBlockCode", new RpcCallback<Data>(){

	    @Override
	    public void onSuccess(Data result) {
		if (result == null || "".equals(result.toString())) {
		    return;
		}
		switch (bl) {
		//case All:
		//    fillFormValue(bothBLForm, bothBlockCode, result.toString());
		//    break;
		case ACCT:
		    fillFormValue(acctBLForm, acctBlockCode, result.toString());
		    break;
		//case CARD:
		//    fillFormValue(cardBLForm, cardBlockCode, result.toString());
		//    break;
		default:
		    break;
		}
	    }
	}, cardNo, accountType, bl);
    }

    /**
     * 创建最底部提交按钮层
     * 
     * @return
     */
    public HorizontalPanel createSubmitBtnLayout() {
	submitLayout = new HorizontalPanel();
	submitLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	KylinButton submitButton = createSubmitButton();
	submitLayout.add(submitButton);
	return submitLayout;
    }

    /**
     * 根据锁定码级别创建不同层级对应的提交按钮
     * 
     * @param bl
     * @return
     */
    
    public KylinButton createSubmitButton() {
	buttonSubmit = new KylinButton(constants.submitTitle(), "skins/icons/ok.gif");
	buttonSubmit.addClickEventListener(new IClickEventListener(){

	    @Override
	    public void onClick() {
		if (StringUtils.isBlank(cardNo)) {
		    Dialog.alertWarn(constants.unSearchWarning(), "警告");
		    return;
		}
		// 更新所有层锁定码
		updateBlockCode(acctBLForm);//bothBLForm, acctBLForm, cardBLForm);
	    }
	});
	return buttonSubmit;
    }
    

    public void updateBlockCode(KylinForm acctForm){//KylinForm bothForm, final KylinForm acctForm, final KylinForm cardForm) {
	// 共用层提交的锁定码
	//submitBothBlockCode = getSubmitBlockCodeByForm(bothForm);
	// 账户层提交的锁定码
	submitAcctBlockCode = getSubmitBlockCodeByForm(acctForm);
	// 卡片层提交的锁定码
	//submitCardBlockCode = getSubmitBlockCodeByForm(cardForm);

	RPC.ajax("rpc/lockingCodeMaintainServer/updateBlockCode", new RpcCallback<Data>(){

	    @Override
	    public void onSuccess(Data result) {
		Dialog.tipNotice("修改成功");
	    }

	}, cardNo,acctType,submitAcctBlockCode);// submitBothBlockCode, submitAcctBlockCode, submitCardBlockCode);
    }

    /**
     * 根据form得到提交的锁定码
     * 
     * @param form
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getSubmitBlockCodeByForm(final KylinForm form) {
	String submitCode = "";
	Map<String, Boolean> map = (Map<String, Boolean>)form.getSubmitData().asMapData().toMap();
	Map<String, Boolean> tempMap = new HashMap<String, Boolean>();
	tempMap.putAll(map);
	submitCode = conventFormValueToString(tempMap);
	return submitCode;
    }

    // 创建显示checkBox的form表单
    public KylinForm createKylinForm(Map<String, Map> map, KylinForm form) {
	form.setCol(4);
	// 根据锁定码创建Checkbox
	// TODO
	final BooleanColumnHelper[] items = new BooleanColumnHelper[map.size()];
	int i = 0;
	for (Entry<String, Map> entry : map.entrySet()) {
	    BooleanColumnHelper checkBoxItem =
		    new BooleanColumnHelper(entry.getKey(), entry.getValue().get("description").toString())
			    .asCheckBoxItem();
	    items[i] = checkBoxItem;
	    i++;
	}
	form.setField(items);
	form.getSetting().labelWidth(200);
	return form;
    }

    // 当搜索以后所有checkItem可选
    private void setDisableTrue(KylinForm... forms) {
	for (KylinForm form : forms) {
	    List<Field> items = form.getFields();
	    for (Field item : items) {
		form.setFieldVisible(item.getKey(), true);
	    }
	}
    }

    /**
     * 持有的锁定码就在checkBox选中
     * 
     * @param form
     * @param value
     *            账户或者卡片对应的锁定码
     * @param allBlockCode
     *            所有模板锁定码信息
     * 
     * */
    public void fillFormValue(KylinForm form, String value, String allBlockCode) {
	for (char c : allBlockCode.toCharArray()) {
	    if (value.contains(String.valueOf(c))) {
		form.setFieldValue(String.valueOf(c), true);
	    }
	}
    }

    /**
     * 得到map中的所有key值，返回String
     * 
     * @param map
     * @return
     */
    public String getStringByMap(Map<String, Map> map) {
	List<String> list = new ArrayList<String>(map.keySet());
	Collections.sort(list);
	if (list.size() == 0) {
	    return null;
	}
	StringBuffer sb = new StringBuffer();
	for (String s : list) {
	    sb.append(s);
	}
	return sb.toString();
    }

    // 获取form中所有选中的锁定码
    public String conventFormValueToString(Map<String, Boolean> map) {
	StringBuffer buffer = new StringBuffer();
	for (String key : map.keySet()) {
	    if (map.get(key)) {
		buffer.append(key);
	    }
	}
	return buffer.toString();
    }
    //
    //
    // @Override
    // protected void createCanvas() {
    //
    // gridHeader = clientUtils.createGridHeader();
    // {
    // gridHeader.createSearchArea(createSearchForm());
    // gridHeader.getSearchButton().select();
    // gridHeader.getSearchLayout().setVisible(true);
    // }
    //
    // addMember(gridHeader);
    //
    // final SectionStack transSectionStack = new SectionStack();
    // {
    // transSectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
    // transSectionStack.setWidth100();
    // transSectionStack.setHeight100();
    // transSectionStack.setCanResizeSections(false);
    // transSectionStack.setOverflow(Overflow.AUTO);
    // }
    // sectionBoth = new SectionStackSection(constants.allBlockCodeTitle());
    // sectionAcct = new SectionStackSection(constants.acctBlockCodeTitle());
    // sectionCard = new SectionStackSection(constants.cardBlockCodeTitle());
    // //共用锁定码部分
    // createBothDynamicForm();
    // //账户层锁定码部分
    // createAcctDynamicForm();
    // //卡片层锁定码部分
    // createCardDynamicForm();
    //
    // transSectionStack.setSections(sectionBoth,sectionAcct,sectionCard);
    // addMember(transSectionStack);
    // addMember(createSubmitBtnLayout());
    // }
    //
    // /**
    // * 创建卡号输入框
    // * */
    // private YakDynamicForm createSearchForm() {
    // //搜索框
    // formSearch = new YakDynamicForm();
    // {
    // formSearch.setNumCols(4);
    // formSearch.setWidth100();
    // cardItem=uiUtil.createCardNoItem();
    // accountTypeItem = uTmAccount.AcctType().required().createSelectItem();
    // formSearch.setItems(cardItem,accountTypeItem);
    //
    // formSearch.addSubmitValuesHandler(new SubmitValuesHandler() {
    // @Override
    // public void onSubmitValues(SubmitValuesEvent event) {
    // if(!formSearch.validate()) {
    // clientUtils.showWarning(publicConstants.msgSearchConditionInvalid());
    // return;
    // }
    // cardNo = UIUtil.getCardNo(cardItem);
    // acctType = accountTypeItem.getValueAsString();
    // //清除缓存
    // acctBLForm.clearValues();
    // cardBLForm.clearValues();
    // bothBLForm.clearValues();
    // //设置所有可选
    // setDisableTrue(acctBLForm,cardBLForm,bothBLForm);
    // //加载共用锁定码
    // loadBlockeCode(cardNo, AccountType.valueOf(acctType), BlockLevel.All);
    // //加载账户层锁定码
    // loadBlockeCode(cardNo, AccountType.valueOf(acctType), BlockLevel.ACCT);
    // //加载卡片层锁定码
    // loadBlockeCode(cardNo, AccountType.valueOf(acctType), BlockLevel.CARD);
    // }
    // });
    // }
    // return formSearch;
    // }
    //
    // /**
    // * 创建共用锁定码form
    // * @return
    // */
    // private void createBothDynamicForm(){
    // RPCTemplate.call(new RPCExecutor<Map<String, BlockCode>>() {
    //
    // @Override
    // public void execute(AsyncCallback<Map<String, BlockCode>> callback) {
    // server.getBlockCode(BlockLevel.All,callback);
    // }
    //
    // @Override
    // public void onSuccess(Map<String,BlockCode> map){
    // // 复制返回的锁定码模板信息
    // allBLMap = new LinkedHashMap<String, BlockCode>();
    // allBLMap.putAll(map);
    // bothBlockCode = getStringByMap(allBLMap);
    // bothBLForm = createYakDynamicForm(allBLMap);
    //
    // VLayout bothlayout = new VLayout();
    // {
    // bothlayout.setOverflow(Overflow.AUTO);
    // bothlayout.addMembers(bothBLForm);
    // bothlayout.setHeight(250);
    // }
    //
    // sectionBoth.addItem(bothlayout);
    // sectionBoth.setExpanded(true);
    //
    // }
    //
    // });
    // }
    //
    // /**
    // * 创建账户层锁定码
    // * @return
    // */
    // private YakDynamicForm createAcctDynamicForm(){
    // // acctBLForm = new YakDynamicForm();
    // RPCTemplate.call(new RPCExecutor<Map<String, BlockCode>>() {
    //
    // @Override
    // public void execute(AsyncCallback<Map<String, BlockCode>> callback) {
    // server.getBlockCode(BlockLevel.ACCT, callback);
    // }
    //
    // @Override
    // public void onSuccess(Map<String, BlockCode> map) {
    // // 复制返回的锁定码模板信息
    // allBLMap = new LinkedHashMap<String, BlockCode>();
    // allBLMap.putAll(map);
    // acctBlockCode = getStringByMap(allBLMap);
    // acctBLForm = createYakDynamicForm(allBLMap);
    //
    // VLayout acctlayout = new VLayout();
    // {
    // acctlayout.setOverflow(Overflow.AUTO);
    // acctlayout.addMembers(acctBLForm);
    // acctlayout.setHeight(250);
    // }
    //
    // sectionAcct.addItem(acctlayout);
    // sectionAcct.setExpanded(true);
    // }
    //
    // });
    // return acctBLForm;
    // }
    //
    // /**
    // * 创建卡片层锁定码
    // * @return
    // */
    // public YakDynamicForm createCardDynamicForm(){
    // // cardBLForm = new YakDynamicForm();
    // RPCTemplate.call(new RPCExecutor<Map<String, BlockCode>>() {
    //
    // @Override
    // public void execute(AsyncCallback<Map<String, BlockCode>> callback) {
    // server.getBlockCode(BlockLevel.CARD, callback);
    // }
    //
    // @Override
    // public void onSuccess(Map<String, BlockCode> map){
    // // 复制返回的锁定码模板信息
    // allBLMap = new LinkedHashMap<String, BlockCode>();
    // allBLMap.putAll(map);
    // cardBlockCode = getStringByMap(allBLMap);
    // cardBLForm = createYakDynamicForm(allBLMap);
    //
    // VLayout cardlayout = new VLayout();
    // {
    // cardlayout.setOverflow(Overflow.AUTO);
    // cardlayout.addMembers(cardBLForm);
    // cardlayout.setHeight(250);
    // }
    //
    // sectionCard.addItem(cardlayout);
    // sectionCard.setExpanded(true);
    // }
    // });
    //
    // return cardBLForm;
    // }
    //
    // /**
    // * 点击查询按钮时，显示该卡片和对应账户的锁定码
    // * @param cardNo
    // * @param accountType
    // * @param bl
    // */
    // public void loadBlockeCode(final String cardNo, final AccountType
    // accountType, final BlockLevel bl){
    // RPCTemplate.call(new RPCExecutor<String>() {
    //
    // @Override
    // public void execute(AsyncCallback<String> callback) {
    // server.loadBlockCode(cardNo, accountType, bl, callback);
    // }
    // @Override
    // public void onSuccess(String resultBl){
    // if (resultBl == null) {
    // return;
    // }
    // switch (bl) {
    // case All:
    // fillFormValue(bothBLForm, bothBlockCode, resultBl);
    // break;
    // case ACCT:
    // fillFormValue(acctBLForm, acctBlockCode, resultBl);
    // break;
    // case CARD:
    // fillFormValue(cardBLForm, cardBlockCode, resultBl);
    // break;
    //
    // default:
    // break;
    // }
    // }
    //
    // });
    // }
    //
    // /**
    // * 创建最底部提交按钮层
    // * @return
    // */
    // public HLayout createSubmitBtnLayout(){
    // submitLayout = new HLayout();{
    // submitLayout.setAlign(Alignment.CENTER);
    // }
    // IButton submitBtn = createSubmitButton();
    // submitLayout.addMember(submitBtn);
    // return submitLayout;
    // }
    //
    // /**
    // * 根据锁定码级别创建不同层级对应的提交按钮
    // *
    // * @param bl
    // * @return
    // */
    // public IButton createSubmitButton(){
    //
    // buttonSubmit = new IButton(constants.submitTitle());
    // buttonSubmit.addClickHandler(new ClickHandler() {
    //
    // @Override
    // public void onClick(ClickEvent event) {
    // if (StringUtils.isBlank(cardNo)) {
    // clientUtils.showWarning(constants.unSearchWarning());
    // return;
    // }
    // // 更新所有层锁定码
    // updateBlockCode(bothBLForm, acctBLForm, cardBLForm);
    // }
    // });
    //
    // return buttonSubmit;
    // }
    //
    // public void updateBlockCode(final YakDynamicForm bothForm, final
    // YakDynamicForm acctForm, final YakDynamicForm cardForm){
    // //共用层提交的锁定码
    // submitBothBlockCode = getSubmitBlockCodeByForm(bothForm);
    //
    // //账户层提交的锁定码
    // submitAcctBlockCode = getSubmitBlockCodeByForm(acctForm);
    //
    // //卡片层提交的锁定码
    // submitCardBlockCode = getSubmitBlockCodeByForm(cardForm);
    //
    // RPCTemplate.call(new RPCExecutor<Void>() {
    //
    // @Override
    // public void execute(AsyncCallback<Void> callback) {
    // server.updateBlockCode(cardNo,AccountType.valueOf(acctType),submitBothBlockCode,submitAcctBlockCode,
    // submitCardBlockCode, callback);
    // }
    // @Override
    // public void onSuccess(Void result){
    // clientUtils.showSuccess();
    // }
    //
    // },buttonSubmit);
    // }
    //
    // /**
    // * 根据form得到提交的锁定码
    // * @param form
    // * @return
    // */
    // @SuppressWarnings("unchecked")
    // private String getSubmitBlockCodeByForm(final YakDynamicForm form){
    // String submitCode = "";
    // Map<String,Boolean> map = form.getValues();
    // Map<String,Boolean> tempMap = new HashMap<String,Boolean> ();
    // tempMap.putAll(map);
    // submitCode = conventFormValueToString(tempMap);
    // return submitCode;
    // }
    // @Override
    // public void updateView(ParamMapToken token) {
    // }
    //
    // @Override
    // public String getTitle() {
    // return constants.pageTitle();
    // }
    //
    // @Override
    // public ImageResource getIcon() {
    // return null;
    // }
    //
    // // 创建显示checkBox的form表单
    // public YakDynamicForm createYakDynamicForm(Map<String, BlockCode> map) {
    // final YakDynamicForm form = new YakDynamicForm();
    // {
    // form.setNumCols(8);
    // form.setWidth(1000);
    // form.setTitleWidth(120);
    //
    // // 根据锁定码创建Checkbox
    // final FormItem[] items = new CheckboxItem[map.size()];
    // int i = 0;
    // for (Entry<String, BlockCode> entry : map.entrySet()) {
    // CheckboxItem checkBoxItem = new CheckboxItem(entry.getKey(),
    // entry.getValue().description);
    // items[i] = checkBoxItem;
    // i++;
    // }
    // form.setFields(items);
    // }
    // return form;
    // }
    //
    // // 当搜索以后所有checkItem可选
    // private void setDisableTrue(YakDynamicForm... forms) {
    // for (YakDynamicForm form : forms) {
    // FormItem[] items = form.getFields();
    // for (FormItem item : items) {
    // item.setDisabled(false);
    // }
    // }
    //
    // }
    //
    // /**
    // * 持有的锁定码就在checkBox选中
    // *
    // * @param form
    // * @param value
    // * 账户或者卡片对应的锁定码
    // * @param allBlockCode
    // * 所有模板锁定码信息
    // *
    // * */
    // public void fillFormValue(YakDynamicForm form, String value, String
    // allBlockCode) {
    // for (char c : allBlockCode.toCharArray()) {
    // if (value.contains(String.valueOf(c))) {
    // form.setValue(String.valueOf(c), true);
    //
    // }
    // }
    // }
    //
    // /**
    // * 得到map中的所有key值，返回String
    // * @param map
    // * @return
    // */
    // public String getStringByMap(Map<String, BlockCode> map){
    // List<String> list = new ArrayList<String>(map.keySet());
    // Collections.sort(list);
    // if(list.size() == 0){
    // return null;
    // }
    // StringBuffer sb = new StringBuffer();
    // for(String s : list){
    // sb.append(s);
    // }
    // return sb.toString();
    // }
    //
    // //获取form中所有选中的锁定码
    // public String conventFormValueToString(Map<String,Boolean> map){
    // StringBuffer buffer = new StringBuffer();
    // for(String key : map.keySet()){
    // if(map.get(key)){
    // buffer.append(key);
    // }
    // }
    // return buffer.toString();
    // }

}
