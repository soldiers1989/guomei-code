package com.sunline.ccs.param.ui.client.authproduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.CheckTypeDomainClient;
import com.sunline.ccs.param.def.enums.CheckType;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

/**
 * 授权验证点控制
 * 
 * @author  lindh
 * @version  [版本号, Jul 13, 2015]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Singleton
public class AuthProductCheckEnabledLayout
{
    @Inject
    private AuthProductConstants authProductConstants;

    @Inject
    private CheckTypeDomainClient checkTypeDomainClient;

    private KylinForm checkEnabledForm;

    private Map<CheckType, BooleanColumnHelper> items;

    public String getLayoutTitle()
    {

	return authProductConstants.authProductCheckEnabled();
    }

    public Widget createCanvas()
    {

	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("100%");
	panel.setHeight("100%");

	LinkedHashMap<String, String> checkMap = checkTypeDomainClient.asLinkedHashMap(false);

	items = new HashMap<CheckType, BooleanColumnHelper>();

	List<BooleanColumnHelper> formItems = new ArrayList<BooleanColumnHelper>();

	for (CheckType type : CheckType.values())
	{
	    BooleanColumnHelper item =
		    new BooleanColumnHelper(type.toString(), checkMap.get(type.toString())).asCheckBoxItem();

	    items.put(type, item);
	    formItems.add(item);
	}
	checkEnabledForm = new KylinForm();
	checkEnabledForm.setCol(3);
	checkEnabledForm.setWidth("100%");

	checkEnabledForm.setField(formItems.toArray(new BooleanColumnHelper[formItems.size()]));
	checkEnabledForm.getSetting().labelWidth(230).labelAlign("right");

	panel.add(checkEnabledForm);
	return panel;
    }

    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
	checkEnabledForm.getUi().clear();

	Map<String, Boolean> checkEnabledMap = new HashMap<String, Boolean>();

	if (data.asMapData().getData("checkEnabled") != null)
	{
	    checkEnabledMap = (Map<String, Boolean>)data.asMapData().getData("checkEnabled").asMapData().toMap();
	}

	if (checkEnabledMap != null && checkEnabledMap.size() > 0)
	{
	    for (CheckType type : CheckType.values())
	    {
		BooleanColumnHelper item = items.get(type);
		Boolean value = checkEnabledMap.get(type.name());

		if (value != null)
		{
		    checkEnabledForm.setFieldValue(item.getName(), value);
		} else
		{
		    checkEnabledForm.setFieldValue(item.getName(), false);
		}
	    }
	}
    }

    public boolean validForm()
    {
        return checkEnabledForm.valid();
    }

    public MapData getFormValues()
    {

	MapData submitData = new MapData();
	submitData.put("checkEnabled", checkEnabledForm.getSubmitData().asListData());

	return submitData;
    }

    public KylinForm getForms()
    {

	return checkEnabledForm;
    }
}
