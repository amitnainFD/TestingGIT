package com.freshdirect.fdstore.content.browse.sorter;

import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.FilteringProductItem;

public class PrimaryDepartmentComparator extends OptionalObjectComparator<FilteringProductItem, String> {

	@Override
	protected String getValue(FilteringProductItem obj) {
		CategoryModel category = obj.getProductModel().getPrimaryHome();
		if (category==null){
			return null;
		}
		DepartmentModel department = category.getDepartment();
		return department == null ? null : department.getFullName();
	}

}
