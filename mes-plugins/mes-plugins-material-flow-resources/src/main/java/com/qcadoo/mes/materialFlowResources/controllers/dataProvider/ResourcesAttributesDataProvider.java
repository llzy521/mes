package com.qcadoo.mes.materialFlowResources.controllers.dataProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ResourcesAttributesDataProvider {

    public static final String PRODUCT_NUMBER = "productNumber";

    public static final String PRODUCT_NAME = "productName";

    public static final String WAREHOUSE_NUMBER = "warehouseNumber";

    public static final String PRODUCT_UNIT = "productUnit";

    public static final String VALUE = "value";

    public static final String ATTRIBUTE_NUMBER = "attributeNumber";

    public static final String ATTRIBUTE_VALUE = "attributeValue";

    public static final String NUMERIC_DATA_TYPE = "02numeric";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();
        columns.add(new ColumnDTO(ResourceFields.NUMBER,
                translationService.translate("materialFlowResources.resource.number.label", locale)));
        columns.add(new ColumnDTO(WAREHOUSE_NUMBER, translationService
                .translate("materialFlowResources.resourcesList.window.mainTab.resourceGrid.column.locationNumber", locale)));
        columns.add(new ColumnDTO(PRODUCT_NUMBER,
                translationService.translate("materialFlowResources.resource.product.label", locale)));
        columns.add(new ColumnDTO(PRODUCT_NAME,
                translationService.translate("materialFlowResources.resourceDto.productName.label", locale)));
        columns.add(new ColumnDTO(ResourceFields.AVAILABLE_QUANTITY,
                translationService.translate("materialFlowResources.resource.availableQuantity.label", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(PRODUCT_UNIT,
                translationService.translate("materialFlowResources.resourceDto.productUnit.label", locale)));
        columns.add(new ColumnDTO(ResourceFields.PRICE,
                translationService.translate("materialFlowResources.resource.price.label", locale), NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(VALUE, translationService.translate("materialFlowResources.resource.value.label", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(ResourceFields.TIME,
                translationService.translate("materialFlowResources.resource.time.label", locale)));
        columns.add(new ColumnDTO(ResourceFields.EXPIRATION_DATE,
                translationService.translate("materialFlowResources.resource.expirationDate.label", locale)));
        columns.add(new ColumnDTO(ResourceFields.PRODUCTION_DATE,
                translationService.translate("materialFlowResources.resource.productionDate.label", locale)));
        columns.add(new ColumnDTO(ResourceFields.BATCH,
                translationService.translate("materialFlowResources.resource.batch.label", locale)));
        String query = "SELECT a.number AS id, a.name, a.unit, a.valuetype AS dataType "
                + "FROM basic_attribute a WHERE a.forresource = TRUE ORDER BY a.number";
        columns.addAll(jdbcTemplate.query(query, Collections.emptyMap(), new BeanPropertyRowMapper(ColumnDTO.class)));
        return columns;
    }

    public List<Map<String, Object>> getRecords() {
        String query = "SELECT r.id, r.number, p.number AS productNumber, p.name AS productName, r.availablequantity, "
                + "l.number AS warehouseNumber, p.unit AS productUnit, r.price, (r.quantity * r.price) AS value, "
                + "to_char(r.\"time\", 'YYYY-MM-DD HH24:MI:SS') AS \"time\", "
                + "r.productiondate, r.expirationdate, r.batch, a.number AS attributeNumber, rav.value AS attributeValue "
                + "FROM materialflowresources_resource r JOIN basic_product p ON p.id = r.product_id "
                + "JOIN materialflow_location l ON l.id = r.location_id "
                + "LEFT JOIN materialflowresources_resourceattributevalue rav ON r.id = rav.resource_id "
                + "LEFT JOIN basic_attribute a ON a.id = rav.attribute_id ORDER BY r.number, a.number";
        List<Map<String, Object>> attributes = jdbcTemplate.queryForList(query, Collections.emptyMap());
        Map<Long, Map<String, Object>> results = Maps.newHashMap();
        for (Map<String, Object> attribute : attributes) {
            Long resourceId = (Long) attribute.get("id");
            Map<String, Object> row;
            if (results.containsKey(resourceId)) {
                row = results.get(resourceId);
            } else {
                row = Maps.newHashMap();
                row.put("id", resourceId);
                row.put(ResourceFields.NUMBER, attribute.get(ResourceFields.NUMBER));
                row.put(WAREHOUSE_NUMBER, attribute.get(WAREHOUSE_NUMBER));
                row.put(PRODUCT_NUMBER, attribute.get(PRODUCT_NUMBER));
                row.put(PRODUCT_NAME, attribute.get(PRODUCT_NAME));
                row.put(ResourceFields.AVAILABLE_QUANTITY, attribute.get(ResourceFields.AVAILABLE_QUANTITY));
                row.put(PRODUCT_UNIT, attribute.get(PRODUCT_UNIT));
                row.put(ResourceFields.PRICE, attribute.get(ResourceFields.PRICE));
                row.put(VALUE, attribute.get(VALUE));
                row.put(ResourceFields.TIME, attribute.get(ResourceFields.TIME));
                row.put(ResourceFields.PRODUCTION_DATE, attribute.get(ResourceFields.PRODUCTION_DATE));
                row.put(ResourceFields.EXPIRATION_DATE, attribute.get(ResourceFields.EXPIRATION_DATE));
                row.put(ResourceFields.BATCH, attribute.get(ResourceFields.BATCH));
            }
            if (!Objects.isNull(attribute.get(ATTRIBUTE_NUMBER))) {
                String attributeValue = (String) row.get(attribute.get(ATTRIBUTE_NUMBER));
                if (Objects.isNull(attributeValue)) {
                    row.put((String) attribute.get(ATTRIBUTE_NUMBER), attribute.get(ATTRIBUTE_VALUE));
                } else {
                    row.put((String) attribute.get(ATTRIBUTE_NUMBER), attributeValue + ", " + attribute.get(ATTRIBUTE_VALUE));
                }
            }
            results.put(resourceId, row);
        }
        return new ArrayList<>(results.values());
    }
}
