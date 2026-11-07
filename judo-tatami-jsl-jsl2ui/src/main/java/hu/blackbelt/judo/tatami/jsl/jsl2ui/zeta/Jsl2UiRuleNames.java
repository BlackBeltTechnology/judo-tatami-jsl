package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

/**
 * Constants for all JSL-to-UI transformation rule names.
 * Organized by ETL module source file.
 */
public final class Jsl2UiRuleNames {

    private Jsl2UiRuleNames() {}

    // =========================================================================
    // application/actorDeclaration.etl
    // =========================================================================
    public static final String ACTOR = "Actor";
    public static final String AUTHENTICATION = "Authentication";

    // =========================================================================
    // application/actorGroupDeclaration.etl
    // =========================================================================
    public static final String MENU_ITEM_GROUP = "MenuItemGroup";

    // =========================================================================
    // application/modifiable.etl
    // =========================================================================
    public static final String ICON_MODIFIER_ICON = "IconModifierIcon";
    public static final String CLAIM_MODIFIER = "ClaimModifier";

    // =========================================================================
    // structure/transferDeclaration.etl
    // =========================================================================
    public static final String CLASS_TYPE = "ClassType";

    // =========================================================================
    // structure/transferActionDeclaration.etl
    // =========================================================================
    public static final String OPERATION_TYPE = "OperationType";
    public static final String OPERATION_OUTPUT_PARAMETER_TYPE = "OperationOutputParameterType";
    public static final String OPERATION_INPUT_PARAMETER_TYPE = "OperationInputParameterType";
    public static final String OPERATION_FAULT_PARAMETER_TYPE = "OperationFaultParameterType";

    // =========================================================================
    // structure/transferFieldDeclaration.etl
    // =========================================================================
    public static final String CREATE_TRANSIENT_TRANSFER_ATTRIBUTE = "CreateTransientTransferAttribute";
    public static final String CREATE_DERIVED_TRANSFER_ATTRIBUTE = "CreateDerivedTransferAttribute";
    public static final String CREATE_MAPPED_TRANSFER_ATTRIBUTE = "CreateMappedTransferAttribute";

    // =========================================================================
    // structure/transferRelationDeclaration.etl
    // =========================================================================
    public static final String RELATION_TYPE = "RelationType";
    public static final String CLONE_RELATION_TYPE = "CloneRelationType";

    // =========================================================================
    // type/type.etl
    // =========================================================================
    public static final String CREATE_NUMERIC_TYPE = "CreateNumericType";
    public static final String CREATE_DATE_TYPE = "CreateDateType";
    public static final String CREATE_TIME_TYPE = "CreateTimeType";
    public static final String CREATE_TIMESTAMP_TYPE = "CreateTimestampType";
    public static final String CREATE_ENUMERATION_TYPE = "CreateEnumerationType";
    public static final String CREATE_ENUMERATION_MEMBER = "CreateEnumerationMember";
    public static final String CREATE_BOOLEAN_TYPE = "CreateBooleanType";
    public static final String CREATE_STRING_TYPE = "CreateStringType";
    public static final String MIME_TYPE = "MimeType";
    public static final String CREATE_BINARY_TYPE = "CreateBinaryType";

    // =========================================================================
    // type/dataTypeOperation.etl
    // =========================================================================
    public static final String BOOLEAN_OPERATION = "BooleanOperation";
    public static final String BOOLEAN_OPERATION_EQUALS = "BooleanOperationEquals";
    public static final String NUMERIC_OPERATION = "NumericOperation";
    public static final String NUMERIC_OPERATION_LESS_THAN = "NumericOperationLessThan";
    public static final String NUMERIC_OPERATION_GREATER_THAN = "NumericOperationGreaterThan";
    public static final String NUMERIC_OPERATION_LESS_OR_EQUAL = "NumericOperationLessOrEqual";
    public static final String NUMERIC_OPERATION_GREATER_OR_EQUAL = "NumericOperationGreaterOrEqual";
    public static final String NUMERIC_OPERATION_EQUAL = "NumericOperationEqual";
    public static final String NUMERIC_OPERATION_NOT_EQUAL = "NumericOperationNotEqual";
    public static final String ENUMERATION_OPERATION = "EnumerationOperation";
    public static final String ENUMERATION_OPERATION_EQUALS = "EnumerationOperationEquals";
    public static final String ENUMERATION_OPERATION_NOT_EQUALS = "EnumerationOperationNotEquals";
    public static final String STRING_OPERATION = "StringOperation";
    public static final String STRING_OPERATION_LESS_THAN = "StringOperationLessThan";
    public static final String STRING_OPERATION_GREATER_THAN = "StringOperationGreaterThan";
    public static final String STRING_OPERATION_LESS_OR_EQUAL = "StringOperationLessOrEqual";
    public static final String STRING_OPERATION_GREATER_OR_EQUAL = "StringOperationGreaterOrEqual";
    public static final String STRING_OPERATION_EQUAL = "StringOperationEqual";
    public static final String STRING_OPERATION_NOT_EQUAL = "StringOperationNotEqual";
    public static final String STRING_OPERATION_MATCHES = "StringOperationMatches";
    public static final String STRING_OPERATION_LIKE = "StringOperationLike";

    // =========================================================================
    // view/frontendDeclaration.etl
    // =========================================================================
    public static final String APPLICATION = "Application";
    public static final String THEME = "Theme";
    public static final String NAVIGATION_CONTROLLER = "NavigationController";
    public static final String EMPTY_DASHBOARD_PAGE_DEFINITION = "EmptyDashboardPageDefinition";
    public static final String EMPTY_DASHBOARD_PAGE_CONTAINER = "EmptyDashboardPageContainer";

    // =========================================================================
    // view/viewDeclaration.etl
    // =========================================================================
    public static final String VIEW_PAGE_CONTAINER = "ViewPageContainer";
    public static final String VIEW_PAGE_CONTAINER_VISUAL_ELEMENT = "ViewPageContainerVisualElement";
    public static final String VIEW_PAGE_CONTAINER_BUTTON_GROUP = "ViewPageContainerButtonGroup";
    public static final String VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION = "ViewPageContainerBackActionDefinition";
    public static final String VIEW_PAGE_CONTAINER_BACK_BUTTON_ICON = "ViewPageContainerBackButtonIcon";
    public static final String VIEW_PAGE_CONTAINER_BACK_BUTTON = "ViewPageContainerBackButton";
    public static final String VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION = "ViewPageContainerRefreshActionDefinition";
    public static final String VIEW_PAGE_CONTAINER_REFRESH_BUTTON_ICON = "ViewPageContainerRefreshButtonIcon";
    public static final String VIEW_PAGE_CONTAINER_REFRESH_BUTTON = "ViewPageContainerRefreshButton";
    public static final String VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION = "ViewPageContainerDeleteActionDefinition";
    public static final String VIEW_PAGE_CONTAINER_DELETE_BUTTON_ICON = "ViewPageContainerDeleteButtonIcon";
    public static final String VIEW_PAGE_CONTAINER_DELETE_BUTTON = "ViewPageContainerDeleteButton";
    public static final String VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION = "ViewPageContainerUpdateActionDefinition";
    public static final String VIEW_PAGE_CONTAINER_UPDATE_BUTTON_ICON = "ViewPageContainerUpdateButtonIcon";
    public static final String VIEW_PAGE_CONTAINER_UPDATE_BUTTON = "ViewPageContainerUpdateButton";

    // =========================================================================
    // view/viewDeclarationForm.etl
    // =========================================================================
    public static final String FORM_PAGE_CONTAINER = "FormPageContainer";
    public static final String FORM_PAGE_CONTAINER_VISUAL_ELEMENT = "FormPageContainerVisualElement";
    public static final String FORM_PAGE_CONTAINER_BUTTON_GROUP = "FormPageContainerButtonGroup";
    public static final String FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION = "FormPageContainerBackActionDefinition";
    public static final String FORM_PAGE_CONTAINER_BACK_BUTTON_ICON = "FormPageContainerBackButtonIcon";
    public static final String FORM_PAGE_CONTAINER_BACK_BUTTON = "FormPageContainerBackButton";
    public static final String FORM_PAGE_CONTAINER_CREATE_BUTTON_ICON = "FormPageContainerCreateButtonIcon";
    public static final String FORM_PAGE_CONTAINER_CREATE_BUTTON = "FormPageContainerCreateButton";
    public static final String FORM_PAGE_CONTAINER_CREATE_ACTION_DEFINITION = "FormPageContainerCreateActionDefinition";
    public static final String FORM_PAGE_CONTAINER_GET_TEMPLATE_ACTION_DEFINITION = "FormPageContainerGetTemplateActionDefinition";

    // =========================================================================
    // view/viewGroupDeclaration.etl
    // =========================================================================
    public static final String GROUP_VISUAL_ELEMENT = "GroupVisualElement";
    public static final String GROUP_FRAME = "GroupFrame";
    public static final String GROUP_ICON = "GroupIcon";
    public static final String TAB_GROUP = "TabGroup";

    // =========================================================================
    // view/viewTabsDeclaration.etl
    // =========================================================================
    public static final String TAB_BAR_VISUAL_ELEMENT = "TabBarVisualElement";
    public static final String SUB_TAB = "SubTab";
    public static final String TABS_ICON = "TabsIcon";

    // =========================================================================
    // view/actionGroupDeclaration.etl
    // =========================================================================
    public static final String ACTION_GROUP_VISUAL_ELEMENT = "ActionGroupVisualElement";

    // =========================================================================
    // view/viewWidgetDeclaration.etl
    // =========================================================================
    public static final String VIEW_WIDGET_ICON = "ViewWidgetIcon";
    public static final String BINARY_TYPE_INPUT = "BinaryTypeInput";
    public static final String BOOLEAN_TYPE_TRINARY_LOGIC_COMBO = "BooleanTypeTrinaryLogicCombo";
    public static final String BOOLEAN_TYPE_CHECKBOX = "BooleanTypeCheckbox";
    public static final String DATE_TYPE_INPUT = "DateTypeInput";
    public static final String TIMESTAMP_TYPE_DATE_TIME_INPUT = "TimestampTypeDateTimeInput";
    public static final String TIME_TYPE_TIME_INPUT = "TimeTypeTypeTimeInput";
    public static final String ENUMERATION_TYPE_RADIO = "EnumerationTypeRadio";
    public static final String ENUMERATION_TYPE_COMBO = "EnumerationTypeCombo";
    public static final String NUMERIC_TYPE_VISUAL_INPUT = "NumericTypeVisualInput";
    public static final String STRING_TYPE_TEXT_INPUT = "StringTypeTextInput";
    public static final String STRING_TYPE_TEXT_AREA = "StringTypeTextArea";
    public static final String STRING_TYPE_FORMATTED = "StringTypeFormatted";

    // =========================================================================
    // view/viewActionDeclaration.etl
    // =========================================================================
    public static final String VIEW_ACTION_DECLARATION_BUTTON = "ViewActionDeclarationButton";
    public static final String VIEW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION = "ViewActionDeclarationCallOperationActionDefinition";
    public static final String VIEW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION = "ViewActionDeclarationOpenSelectorActionDefinition";
    public static final String VIEW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION = "ViewActionDeclarationOpenFormActionDefinition";
    public static final String VIEW_ACTION = "ViewAction";
    public static final String OPERATION_INPUT_FORM_PAGE_DEFINITION = "OperationInputFormPageDefinition";
    public static final String OPERATION_INPUT_FORM_BACK_ACTION = "OperationInputFormBackAction";
    public static final String OPERATION_INPUT_FORM_GET_TEMPLATE_ACTION = "OperationInputFormGetTemplateAction";
    public static final String OPERATION_INPUT_FORM_CALL_ACTION_BUTTON = "OperationInputFormCallActionButton";
    public static final String OPERATION_INPUT_FORM_CALL_ACTION_BUTTON_ICON = "OperationInputFormCallActionButtonIcon";
    public static final String OPERATION_INPUT_FORM_CALL_ACTION_DEFINITION = "OperationInputFormCallActionDefinition";
    public static final String OPERATION_INPUT_SELECTOR_PAGE_DEFINITION = "OperationInputSelectorPageDefinition";
    public static final String OPERATION_INPUT_SELECTOR_CALL_ACTION_BUTTON = "OperationInputSelectorCallActionButton";
    public static final String OPERATION_INPUT_SELECTOR_CALL_ACTION_DEFINITION = "OperationInputSelectorCallActionDefinition";
    public static final String OPERATION_INPUT_SELECTOR_BACK_ACTION = "OperationInputSelectorBackAction";
    public static final String OPERATION_INPUT_SELECTOR_CALL_ACTION = "OperationInputSelectorCallAction";
    public static final String OPERATION_INPUT_SELECTOR_RANGE_ACTION = "OperationInputSelectorRangeAction";
    public static final String OPERATION_INPUT_SELECTOR_FILTER_ACTION = "OperationInputSelectorFilterAction";
    public static final String OPERATION_INPUT_FORM_CALL_ACTION = "OperationInputFormCallAction";
    public static final String OPERATION_OUTPUT_PAGE_DEFINITION = "OperationOutputPageDefinition";
    public static final String OPERATION_OUTPUT_BACK_ACTION = "OperationOutputBackAction";
    public static final String OPERATION_OUTPUT_REFRESH_ACTION = "OperationOutputRefreshAction";
    public static final String OPERATION_OUTPUT_UPDATE_ACTION = "OperationOutputUpdateAction";
    public static final String OPERATION_OUTPUT_DELETE_ACTION = "OperationOutputDeleteAction";

    // =========================================================================
    // view/viewLinkDeclaration.etl
    // =========================================================================
    public static final String LINK_ICON = "LinkIcon";
    public static final String INLINE_VIEW_LINK = "InlineViewLink";
    public static final String INLINE_VIEW_BUTTON = "InlineViewButton";
    public static final String INLINE_VIEW_BUTTON_OPEN_PAGE_ACTION_DEFINITION = "InlineViewButtonOpenPageActionDefinition";
    public static final String INLINE_VIEW_BUTTON_PRE_FETCH_ACTION_DEFINITION = "InlineViewButtonPreFetchActionDefinition";
    public static final String VIEW_LINK_DECLARATION_REFRESH_ACTION_DEFINITION = "ViewLinkDeclarationRefreshActionDefinition";
    public static final String INLINE_VIEW_LINK_BUTTON_GROUP = "InlineViewLinkButtonGroup";
    public static final String VIEW_LINK_DECLARATION_REPRESENTATION_COLUMN = "ViewLinkDeclarationRepresentationColumn";
    public static final String VIEW_LINK_DECLARATION_OPEN_PAGE_BUTTON = "ViewLinkDeclarationOpenPageButton";
    public static final String VIEW_LINK_DECLARATION_OPEN_PAGE_BUTTON_ICON = "ViewLinkDeclarationOpenPageButtonIcon";
    public static final String VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION_DEFINITION = "ViewLinkDeclarationOpenPageActionDefinition";
    public static final String VIEW_LINK_DECLARATION_OPEN_FORM_BUTTON = "ViewLinkDeclarationOpenFormButton";
    public static final String VIEW_LINK_DECLARATION_OPEN_FORM_BUTTON_ICON = "ViewLinkDeclarationOpenFormButtonIcon";
    public static final String VIEW_LINK_DECLARATION_OPEN_CREATE_FORM_ACTION_DEFINITION = "ViewLinkDeclarationOpenCreateFormActionDefinition";
    public static final String VIEW_LINK_DECLARATION_DELETE_BUTTON = "ViewLinkDeclarationDeleteButton";
    public static final String VIEW_LINK_DECLARATION_DELETE_BUTTON_ICON = "ViewLinkDeclarationDeleteButtonIcon";
    public static final String VIEW_LINK_DECLARATION_ROW_DELETE_ACTION_DEFINITION = "ViewLinkDeclarationRowDeleteActionDefinition";
    public static final String VIEW_LINK_DECLARATION_UNSET_BUTTON = "ViewLinkDeclarationUnsetButton";
    public static final String VIEW_LINK_DECLARATION_UNSET_BUTTON_ICON = "ViewLinkDeclarationUnsetButtonIcon";
    public static final String VIEW_LINK_DECLARATION_UNSET_ACTION_DEFINITION = "ViewLinkDeclarationUnsetActionDefinition";
    public static final String VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_BUTTON = "ViewLinkDeclarationOpenSetSelectorButton";
    public static final String VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_BUTTON_ICON = "ViewLinkDeclarationOpenSetSelectorButtonIcon";
    public static final String VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_ACTION_DEFINITION = "ViewLinkDeclarationOpenSetSelectorActionDefinition";
    public static final String VIEW_LINK_DECLARATION_AUTOCOMPLETE_RANGE_ACTION = "ViewLinkDeclarationAutocompleteRangeAction";
    public static final String VIEW_LINK_DECLARATION_AUTOCOMPLETE_RANGE_ACTION_DEFINITION = "ViewLinkDeclarationAutocompleteRangeActionDefinition";
    public static final String VIEW_LINK_DECLARATION_AUTOCOMPLETE_SET_ACTION = "ViewLinkDeclarationAutocompleteSetAction";
    public static final String VIEW_LINK_DECLARATION_AUTOCOMPLETE_SET_ACTION_DEFINITION = "ViewLinkDeclarationAutocompleteSetActionDefinition";

    // =========================================================================
    // view/viewLinkDeclarationFormPage.etl
    // =========================================================================
    public static final String VIEW_LINK_CREATE_FORM_PAGE_DEFINITION = "ViewLinkCreateFormPageDefinition";
    public static final String VIEW_LINK_CREATE_FORM_CREATE_ACTION = "ViewLinkCreateFormCreateAction";
    public static final String VIEW_LINK_CREATE_FORM_BACK_ACTION = "ViewLinkCreateFormBackAction";

    // =========================================================================
    // view/viewLinkDeclarationSetSelectorPage.etl
    // =========================================================================
    public static final String VIEW_LINK_DECLARATION_SET_SELECTOR_PAGE_DEFINITION = "ViewLinkDeclarationSetSelectorPageDefinition";
    public static final String VIEW_LINK_DECLARATION_SET_SELECTOR_SET_ACTION = "ViewLinkDeclarationSetSelectorSetAction";
    public static final String VIEW_LINK_DECLARATION_SET_SELECTOR_BACK_ACTION = "ViewLinkDeclarationSetSelectorBackAction";
    public static final String VIEW_LINK_DECLARATION_SET_SELECTOR_TABLE_FILTER_ACTION = "ViewLinkDeclarationSetSelectorTableFilterAction";
    public static final String VIEW_LINK_DECLARATION_SET_SELECTOR_TABLE_RANGE_ACTION = "ViewLinkDeclarationSetSelectorTableRangeAction";

    // =========================================================================
    // view/viewLinkDeclarationViewPage.etl
    // =========================================================================
    public static final String VIEW_LINK_PAGE_DEFINITION = "ViewLinkPageDefinition";
    public static final String VIEW_LINK_PAGE_DEFINITION_REFRESH_ACTION = "ViewLinkPageDefinitionRefreshAction";
    public static final String VIEW_LINK_PAGE_DEFINITION_BACK_ACTION = "ViewLinkPageDefinitionBackAction";
    public static final String VIEW_LINK_PAGE_DEFINITION_UPDATE_ACTION = "ViewLinkPageDefinitionUpdateAction";
    public static final String VIEW_LINK_PAGE_DEFINITION_DELETE_ACTION = "ViewLinkPageDefinitionDeleteAction";
    public static final String VIEW_LINK_DECLARATION_REFRESH_ACTION = "ViewLinkDeclarationRefreshAction";
    public static final String VIEW_LINK_DECLARATION_PRE_FETCH_ACTION = "ViewLinkDeclarationPreFetchAction";
    public static final String VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION = "ViewLinkDeclarationOpenPageAction";
    public static final String VIEW_LINK_DECLARATION_OPEN_FORM_ACTION = "ViewLinkDeclarationOpenFormAction";
    public static final String VIEW_LINK_DECLARATION_ROW_DELETE_ACTION = "ViewLinkDeclarationRowDeleteAction";
    public static final String VIEW_LINK_DECLARATION_UNSET_ACTION = "ViewLinkDeclarationUnsetAction";
    public static final String VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_DIALOG_ACTION = "ViewLinkDeclarationOpenSetSelectorDialogAction";

    // =========================================================================
    // view/viewTableDeclaration.etl
    // =========================================================================
    public static final String TABLE_ICON = "TableIcon";
    public static final String INLINE_VIEW_CARDS = "InlineViewCards";
    public static final String INLINE_VIEW_CARDS_BUTTON_GROUP = "InlineViewCardsButtonGroup";
    public static final String INLINE_VIEW_CARDS_FILTER_BUTTON = "InlineViewCardsFilterButton";
    public static final String INLINE_VIEW_CARDS_FILTER_BUTTON_ICON = "InlineViewCardsFilterButtonIcon";
    public static final String INLINE_VIEW_CARDS_FILTER_BUTTON_ACTION_DEFINITION = "InlineViewCardsFilterButtonActionDefinition";
    public static final String INLINE_VIEW_CARDS_REFRESH_BUTTON = "InlineViewCardsRefreshButton";
    public static final String INLINE_VIEW_CARDS_REFRESH_BUTTON_ICON = "InlineViewCardsRefreshButtonIcon";
    public static final String INLINE_VIEW_CARDS_REFRESH_ACTION_DEFINITION = "InlineViewCardsRefreshActionDefinition";
    public static final String INLINE_VIEW_CARDS_ROW_BUTTON_GROUP = "InlineViewCardsRowButtonGroup";
    public static final String INLINE_VIEW_CARDS_OPEN_PAGE_BUTTON = "InlineViewCardsOpenPageButton";
    public static final String INLINE_VIEW_CARDS_OPEN_PAGE_BUTTON_ICON = "InlineViewCardsOpenPageButtonIcon";
    public static final String INLINE_VIEW_CARDS_OPEN_PAGE_ACTION_DEFINITION = "InlineViewCardsOpenPageActionDefinition";
    public static final String INLINE_VIEW_TAGS = "InlineViewTags";
    public static final String VIEW_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION_DEFINITION = "ViewTagsDeclarationAutocompleteRangeActionDefinition";
    public static final String VIEW_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION_DEFINITION = "ViewTagsDeclarationAutocompleteAddActionDefinition";
    public static final String INLINE_VIEW_TAGS_BUTTON_GROUP = "InlineViewTagsButtonGroup";
    public static final String INLINE_VIEW_TAGS_FILTER_BUTTON = "InlineViewTagsFilterButton";
    public static final String INLINE_VIEW_TAGS_FILTER_BUTTON_ICON = "InlineViewTagsFilterButtonIcon";
    public static final String INLINE_VIEW_TAGS_FILTER_ACTION_DEFINITION = "InlineViewTagsFilterActionDefinition";
    public static final String INLINE_VIEW_TAGS_REFRESH_BUTTON = "InlineViewTagsRefreshButton";
    public static final String INLINE_VIEW_TAGS_REFRESH_BUTTON_ICON = "InlineViewTagsRefreshButtonIcon";
    public static final String INLINE_VIEW_TAGS_REFRESH_ACTION_DEFINITION = "InlineViewTagsRefreshActionDefinition";
    public static final String INLINE_VIEW_TAGS_ROW_BUTTON_GROUP = "InlineViewTagsRowButtonGroup";
    public static final String INLINE_VIEW_TAGS_OPEN_PAGE_BUTTON = "InlineViewTagsOpenPageButton";
    public static final String INLINE_VIEW_TAGS_OPEN_PAGE_BUTTON_ICON = "InlineViewTagsOpenPageButtonIcon";
    public static final String INLINE_VIEW_TAGS_OPEN_PAGE_ACTION_DEFINITION = "InlineViewTagsOpenPageActionDefinition";
    public static final String INLINE_VIEW_TABLE = "InlineViewTable";
    public static final String TABLE_PRIMITIVE_COLUMN = "TablePrimitiveColumn";
    public static final String COLUMN_ICON = "ColumnIcon";
    public static final String TABLE_PRIMITIVE_COLUMN_FILTER = "TablePrimitiveColumnFilter";
    public static final String INLINE_VIEW_TABLE_BUTTON_GROUP = "InlineViewTableButtonGroup";
    public static final String INLINE_VIEW_TABLE_ROW_BUTTON_GROUP = "InlineViewTableRowButtonGroup";
    public static final String VIEW_TABLE_DECLARATION_FILTER_BUTTON = "ViewTableDeclarationFilterButton";
    public static final String VIEW_TABLE_DECLARATION_FILTER_BUTTON_ICON = "ViewTableDeclarationFilterButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_FILTER_ACTION_DEFINITION = "ViewTableDeclarationFilterActionDefinition";
    public static final String VIEW_TABLE_DECLARATION_REFRESH_BUTTON = "ViewTableDeclarationRefreshButton";
    public static final String VIEW_TABLE_DECLARATION_REFRESH_BUTTON_ICON = "ViewTableDeclarationRefreshButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_REFRESH_ACTION_DEFINITION = "ViewTableDeclarationRefreshActionDefinition";
    public static final String VIEW_TABLE_DECLARATION_OPEN_CREATE_BUTTON = "ViewTableDeclarationOpenCreateButton";
    public static final String VIEW_TABLE_DECLARATION_OPEN_CREATE_BUTTON_ICON = "ViewTableDeclarationOpenCreateButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_OPEN_CREATE_ACTION_DEFINITION = "ViewTableDeclarationOpenCreateActionDefinition";
    public static final String VIEW_TABLE_DECLARATION_OPEN_PAGE_BUTTON = "ViewTableDeclarationOpenPageButton";
    public static final String VIEW_TABLE_DECLARATION_OPEN_PAGE_BUTTON_ICON = "ViewTableDeclarationOpenPageButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION_DEFINITION = "ViewTableDeclarationOpenPageActionDefinition";
    public static final String VIEW_TABLE_DECLARATION_ROW_DELETE_BUTTON = "ViewTableDeclarationRowDeleteButton";
    public static final String VIEW_TABLE_DECLARATION_ROW_DELETE_BUTTON_ICON = "ViewTableDeclarationRowDeleteButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_ROW_DELETE_ACTION_DEFINITION = "ViewTableDeclarationRowDeleteActionDefinition";
    public static final String VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_BUTTON = "ViewTableDeclarationOpenAddSelectorButton";
    public static final String VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_BUTTON_ICON = "ViewTableDeclarationOpenAddSelectorButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION_DEFINITION = "ViewTableDeclarationOpenAddSelectorActionDefinition";
    public static final String VIEW_TABLE_DECLARATION_BULK_REMOVE_BUTTON = "ViewTableDeclarationBulkRemoveButton";
    public static final String VIEW_TABLE_DECLARATION_BULK_REMOVE_BUTTON_ICON = "ViewTableDeclarationBulkRemoveButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION_DEFINITION = "ViewTableDeclarationBulkRemoveActionDefinition";
    public static final String VIEW_TABLE_DECLARATION_CLEAR_BUTTON = "ViewTableDeclarationClearButton";
    public static final String VIEW_TABLE_DECLARATION_CLEAR_BUTTON_ICON = "ViewTableDeclarationClearButtonIcon";
    public static final String VIEW_TABLE_DECLARATION_CLEAR_ACTION_DEFINITION = "ViewTableDeclarationClearActionDefinition";
    public static final String VIEW_TABLE_ACTION_DECLARATION_BUTTON = "ViewTableActionDeclarationButton";
    public static final String VIEW_TABLE_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION = "ViewTableActionDeclarationCallOperationActionDefinition";
    public static final String VIEW_TABLE_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION = "ViewTableActionDeclarationOpenSelectorActionDefinition";
    public static final String VIEW_TABLE_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION = "ViewTableActionDeclarationOpenFormActionDefinition";

    // =========================================================================
    // view/viewTableDeclarationAddSelectorPage.etl
    // =========================================================================
    public static final String VIEW_TABLE_DECLARATION_ADD_SELECTOR_PAGE_DEFINITION = "ViewTableDeclarationAddSelectorPageDefinition";
    public static final String VIEW_TABLE_DECLARATION_ADD_SELECTOR_ADD_ACTION = "ViewTableDeclarationAddSelectorAddAction";
    public static final String VIEW_TABLE_DECLARATION_ADD_SELECTOR_BACK_ACTION = "ViewTableDeclarationAddSelectorBackAction";
    public static final String VIEW_TABLE_DECLARATION_ADD_SELECTOR_TABLE_FILTER_ACTION = "ViewTableDeclarationAddSelectorTableFilterAction";
    public static final String VIEW_TABLE_DECLARATION_ADD_SELECTOR_TABLE_RANGE_ACTION = "ViewTableDeclarationAddSelectorTableRangeAction";

    // =========================================================================
    // view/viewTableDeclarationFormPage.etl
    // =========================================================================
    public static final String VIEW_TABLE_CREATE_FORM_PAGE_DEFINITION = "ViewTableCreateFormPageDefinition";
    public static final String VIEW_TABLE_CREATE_FORM_CREATE_ACTION = "ViewTableCreateFormCreateAction";
    public static final String VIEW_TABLE_CREATE_FORM_BACK_ACTION = "ViewTableCreateFormBackAction";

    // =========================================================================
    // view/viewTableDeclarationViewPage.etl
    // =========================================================================
    public static final String VIEW_TABLE_VIEW_PAGE_DEFINITION = "ViewTableViewPageDefinition";
    public static final String VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION = "ViewTableTagsDeclarationAutocompleteRangeAction";
    public static final String VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION = "ViewTableTagsDeclarationAutocompleteAddAction";
    public static final String VIEW_TABLE_VIEW_PAGE_DEFINITION_REFRESH_ACTION = "ViewTableViewPageDefinitionRefreshAction";
    public static final String VIEW_TABLE_VIEW_PAGE_DEFINITION_BACK_ACTION = "ViewTableViewPageDefinitionBackAction";
    public static final String VIEW_TABLE_VIEW_PAGE_DEFINITION_UPDATE_ACTION = "ViewTableViewPageDefinitionUpdateAction";
    public static final String VIEW_TABLE_VIEW_PAGE_DEFINITION_DELETE_ACTION = "ViewTableViewPageDefinitionDeleteAction";
    public static final String VIEW_TABLE_DECLARATION_FILTER_ACTION = "ViewTableDeclarationFilterAction";
    public static final String VIEW_TABLE_DECLARATION_REFRESH_ACTION = "ViewTableDeclarationRefreshAction";
    public static final String VIEW_TABLE_DECLARATION_OPEN_CREATE_ACTION = "ViewTableDeclarationOpenCreateAction";
    public static final String VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION = "ViewTableDeclarationOpenPageAction";
    public static final String VIEW_TABLE_DECLARATION_ROW_DELETE_ACTION = "ViewTableDeclarationRowDeleteAction";
    public static final String VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION = "ViewTableDeclarationOpenAddSelectorAction";
    public static final String VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION = "ViewTableDeclarationBulkRemoveAction";
    public static final String VIEW_TABLE_DECLARATION_CLEAR_ACTION = "ViewTableDeclarationClearAction";

    // =========================================================================
    // view/menuTableDeclaration.etl
    // =========================================================================
    public static final String MENU_TABLE_NAVIGATION_ITEM = "MenuTableNavigationItem";

    // =========================================================================
    // view/menuTableDeclarationTablePage.etl
    // =========================================================================
    public static final String ACCESS_TABLE_PAGE_DEFINITION = "AccessTablePageDefinition";
    public static final String ACCESS_TABLE_BACK_ACTION = "AccessTableBackAction";
    public static final String ACCESS_TABLE_TABLE_OPEN_CREATE_ACTION = "AccessTableTableOpenCreateAction";
    public static final String ACCESS_TABLE_TABLE_REFRESH_ACTION = "AccessTableTableRefreshAction";
    public static final String ACCESS_TABLE_TABLE_FILTER_ACTION = "AccessTableTableFilterAction";
    public static final String ACCESS_TABLE_OPEN_PAGE_ACTION = "AccessTableOpenPageAction";
    public static final String ACCESS_TABLE_ROW_DELETE_ACTION = "AccessTableRowDeleteAction";
    public static final String ACCESS_TABLE_TABLE_BULK_REMOVE_ACTION = "AccessTableTableBulkRemoveAction";
    public static final String ACCESS_TABLE_TABLE_CLEAR_ACTION = "AccessTableTableClearAction";

    // =========================================================================
    // view/menuTableDeclarationCardsPage.etl
    // =========================================================================
    public static final String ACCESS_CARDS_PAGE_DEFINITION = "AccessCardsPageDefinition";
    public static final String ACCESS_CARDS_BACK_ACTION = "AccessCardsBackAction";
    public static final String ACCESS_CARDS_TABLE_REFRESH_ACTION = "AccessCardsTableRefreshAction";
    public static final String ACCESS_CARDS_TABLE_FILTER_ACTION = "AccessCardsTableFilterAction";
    public static final String ACCESS_CARDS_OPEN_PAGE_ACTION = "AccessCardsOpenPageAction";

    // =========================================================================
    // view/menuTableDeclarationTagsPage.etl
    // =========================================================================
    public static final String ACCESS_TAGS_PAGE_DEFINITION = "AccessTagsPageDefinition";
    public static final String ACCESS_TAGS_BACK_ACTION = "AccessTagsBackAction";
    public static final String ACCESS_TAGS_TABLE_REFRESH_ACTION = "AccessTagsTableRefreshAction";
    public static final String ACCESS_TAGS_TABLE_FILTER_ACTION = "AccessTagsTableFilterAction";
    public static final String ACCESS_TAGS_OPEN_PAGE_ACTION = "AccessTagsOpenPageAction";

    // =========================================================================
    // view/menuTableDeclarationFormPage.etl
    // =========================================================================
    public static final String ACCESS_TABLE_CREATE_FORM_PAGE_DEFINITION = "AccessTableCreateFormPageDefinition";
    public static final String ACCESS_TABLE_CREATE_FORM_BACK_ACTION = "AccessTableCreateFormBackAction";
    public static final String ACCESS_TABLE_CREATE_FORM_GET_TEMPLATE_ACTION = "AccessTableCreateFormGetTemplateAction";
    public static final String ACCESS_TABLE_CREATE_FORM_CREATE_ACTION = "AccessTableCreateFormCreateAction";

    // =========================================================================
    // view/menuTableDeclarationAddSelectorPage.etl
    // =========================================================================
    public static final String ACCESS_TABLE_TABLE_ADD_SELECTOR_PAGE_DEFINITION = "AccessTableTableAddSelectorPageDefinition";
    public static final String ACCESS_TABLE_TABLE_ADD_SELECTOR_ADD_ACTION = "AccessTableTableAddSelectorAddAction";
    public static final String ACCESS_TABLE_TABLE_ADD_SELECTOR_BACK_ACTION = "AccessTableTableAddSelectorBackAction";
    public static final String ACCESS_TABLE_TABLE_ADD_SELECTOR_TABLE_FILTER_ACTION = "AccessTableTableAddSelectorTableFilterAction";
    public static final String ACCESS_TABLE_TABLE_ADD_SELECTOR_TABLE_RANGE_ACTION = "AccessTableTableAddSelectorTableRangeAction";

    // =========================================================================
    // view/menuTableDeclarationViewPage.etl
    // =========================================================================
    public static final String ACCESS_TABLE_VIEW_PAGE_DEFINITION = "AccessTableViewPageDefinition";
    public static final String ACCESS_TABLE_VIEW_BACK_ACTION = "AccessTableViewBackAction";
    public static final String ACCESS_TABLE_VIEW_REFRESH_ACTION = "AccessTableViewRefreshAction";
    public static final String ACCESS_TABLE_VIEW_UPDATE_ACTION = "AccessTableViewUpdateAction";
    public static final String ACCESS_TABLE_VIEW_DELETE_ACTION = "AccessTableViewDeleteAction";

    // =========================================================================
    // view/menuLinkDeclaration.etl
    // =========================================================================
    public static final String MENU_LINK_NAVIGATION_ITEM = "MenuLinkNavigationItem";

    // =========================================================================
    // view/menuLinkDeclarationFormPage.etl
    // =========================================================================
    public static final String ACCESS_LINK_CREATE_FORM_PAGE_DEFINITION = "AccessLinkCreateFormPageDefinition";
    public static final String ACCESS_LINK_CREATE_FORM_BACK_ACTION = "AccessLinkCreateFormBackAction";
    public static final String ACCESS_LINK_CREATE_FORM_GET_TEMPLATE_ACTION = "AccessLinkCreateFormGetTemplateAction";
    public static final String ACCESS_LINK_CREATE_FORM_CREATE_ACTION = "AccessLinkCreateFormCreateAction";

    // =========================================================================
    // view/menuLinkDeclarationViewPage.etl
    // =========================================================================
    public static final String ACCESS_VIEW_PAGE_DEFINITION = "AccessViewPageDefinition";
    public static final String ACCESS_VIEW_BACK_ACTION = "AccessViewBackAction";
    public static final String ACCESS_VIEW_REFRESH_ACTION = "AccessViewRefreshAction";
    public static final String ACCESS_VIEW_UPDATE_ACTION = "AccessViewUpdateAction";
    public static final String ACCESS_VIEW_DELETE_ACTION = "AccessViewDeleteAction";

    // =========================================================================
    // view/rowDeclaration.etl
    // =========================================================================
    public static final String TABLE_PAGE_CONTAINER = "TablePageContainer";
    public static final String TABLE_PAGE_CONTAINER_BUTTON_GROUP = "TablePageContainerButtonGroup";
    public static final String TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_ACTION_DEFINITION = "TablePageContainerSetSelectorSetActionDefinition";
    public static final String TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_BUTTON_ICON = "TablePageContainerSetSelectorSetButtonIcon";
    public static final String TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_BUTTON = "TablePageContainerSetSelectorSetButton";
    public static final String TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_ACTION_DEFINITION = "TablePageContainerAddSelectorAddActionDefinition";
    public static final String TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_BUTTON_ICON = "TablePageContainerAddSelectorAddButtonIcon";
    public static final String TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_BUTTON = "TablePageContainerAddSelectorAddButton";
    public static final String TABLE_PAGE_CONTAINER_BACK_BUTTON_ICON = "TablePageContainerBackButtonIcon";
    public static final String TABLE_PAGE_CONTAINER_BACK_ACTION_DEFINITION = "TablePageContainerBackActionDefinition";
    public static final String TABLE_PAGE_CONTAINER_BACK_BUTTON = "TablePageContainerBackButton";
    public static final String TABLE_PAGE_CONTAINER_VISUAL_ELEMENT = "TablePageContainerVisualElement";
    public static final String TABLE_FRAME = "TableFrame";
    public static final String TABLE_TABLE = "TableTable";
    public static final String TABLE_TABLE_BUTTON_GROUP = "TableTableButtonGroup";
    public static final String TABLE_ROW_BUTTON_GROUP = "TableRowButtonGroup";
    public static final String TABLE_TABLE_FILTER_BUTTON = "TableTableFilterButton";
    public static final String TABLE_TABLE_FILTER_BUTTON_ICON = "TableTableFilterButtonIcon";
    public static final String TABLE_TABLE_FILTER_ACTION_DEFINITION = "TableTableFilterActionDefinition";
    public static final String TABLE_TABLE_REFRESH_BUTTON = "TableTableRefreshButton";
    public static final String TABLE_TABLE_REFRESH_BUTTON_ICON = "TableTableRefreshButtonIcon";
    public static final String TABLE_TABLE_REFRESH_ACTION_DEFINITION = "TableTableRefreshActionDefinition";
    public static final String TABLE_TABLE_OPEN_CREATE_BUTTON = "TableTableOpenCreateButton";
    public static final String TABLE_TABLE_OPEN_CREATE_BUTTON_ICON = "TableTableOpenCreateButtonIcon";
    public static final String TABLE_TABLE_OPEN_CREATE_ACTION_DEFINITION = "TableTableOpenCreateActionDefinition";
    public static final String TABLE_OPEN_PAGE_BUTTON = "TableOpenPageButton";
    public static final String TABLE_OPEN_PAGE_BUTTON_ICON = "TableOpenPageButtonIcon";
    public static final String TABLE_OPEN_PAGE_ACTION_DEFINITION = "TableOpenPageActionDefinition";
    public static final String TABLE_ROW_DELETE_BUTTON = "TableRowDeleteButton";
    public static final String TABLE_ROW_DELETE_BUTTON_ICON = "TableRowDeleteButtonIcon";
    public static final String TABLE_ROW_DELETE_ACTION_DEFINITION = "TableRowDeleteActionDefinition";
    public static final String TABLE_TABLE_OPEN_ADD_SELECTOR_BUTTON = "TableTableOpenAddSelectorButton";
    public static final String TABLE_TABLE_OPEN_ADD_SELECTOR_BUTTON_ICON = "TableTableOpenAddSelectorButtonIcon";
    public static final String TABLE_TABLE_OPEN_ADD_SELECTOR_ACTION_DEFINITION = "TableTableOpenAddSelectorActionDefinition";
    public static final String TABLE_TABLE_BULK_REMOVE_BUTTON = "TableTableBulkRemoveButton";
    public static final String TABLE_TABLE_BULK_REMOVE_BUTTON_ICON = "TableTableBulkRemoveButtonIcon";
    public static final String TABLE_TABLE_BULK_REMOVE_ACTION_DEFINITION = "TableTableBulkRemoveActionDefinition";
    public static final String TABLE_TABLE_CLEAR_BUTTON = "TableTableClearButton";
    public static final String TABLE_TABLE_CLEAR_BUTTON_ICON = "TableTableClearButtonIcon";
    public static final String TABLE_TABLE_CLEAR_ACTION_DEFINITION = "TableTableClearActionDefinition";

    // =========================================================================
    // view/rowActionDeclaration.etl
    // =========================================================================
    public static final String ROW_ACTION_DECLARATION_BUTTON = "RowActionDeclarationButton";
    public static final String ROW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION = "RowActionDeclarationCallOperationActionDefinition";
    public static final String ROW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION = "RowActionDeclarationOpenSelectorActionDefinition";
    public static final String ROW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION = "RowActionDeclarationOpenFormActionDefinition";
    public static final String ROW_ACTION = "RowAction";

    // =========================================================================
    // view/rowColumnDeclaration.etl
    // =========================================================================
    public static final String ROW_COLUMN_DECLARATION_PRIMITIVE_COLUMN = "RowColumnDeclarationPrimitiveColumn";
    public static final String ROW_COLUMN_DECLARATION_COLUMN_ICON = "RowColumnDeclarationColumnIcon";
    public static final String ROW_COLUMN_DECLARATION_PRIMITIVE_COLUMN_FILTER = "RowColumnDeclarationPrimitiveColumnFilter";

    // =========================================================================
    // view/cardDeclaration.etl
    // =========================================================================
    public static final String CARD_PAGE_CONTAINER = "CardPageContainer";
    public static final String CARD_PAGE_CONTAINER_BUTTON_GROUP = "CardPageContainerButtonGroup";
    public static final String CARD_PAGE_CONTAINER_BACK_BUTTON_ICON = "CardPageContainerBackButtonIcon";
    public static final String CARD_PAGE_CONTAINER_BACK_ACTION_DEFINITION = "CardPageContainerBackActionDefinition";
    public static final String CARD_PAGE_CONTAINER_BACK_BUTTON = "CardPageContainerBackButton";
    public static final String CARD_PAGE_CONTAINER_VISUAL_ELEMENT = "CardPageContainerVisualElement";
    public static final String CARD_TABLE = "CardTable";
    public static final String CARD_TABLE_BUTTON_GROUP = "CardTableButtonGroup";
    public static final String CARD_TABLE_ROW_BUTTON_GROUP = "CardTableRowButtonGroup";
    public static final String CARD_TABLE_FILTER_BUTTON = "CardTableFilterButton";
    public static final String CARD_TABLE_FILTER_BUTTON_ICON = "CardTableFilterButtonIcon";
    public static final String CARD_TABLE_FILTER_ACTION_DEFINITION = "CardTableFilterActionDefinition";
    public static final String CARD_TABLE_REFRESH_BUTTON = "CardTableRefreshButton";
    public static final String CARD_TABLE_REFRESH_BUTTON_ICON = "CardTableRefreshButtonIcon";
    public static final String CARD_TABLE_REFRESH_ACTION_DEFINITION = "CardTableRefreshActionDefinition";
    public static final String CARD_OPEN_PAGE_BUTTON = "CardOpenPageButton";
    public static final String CARD_OPEN_PAGE_BUTTON_ICON = "CardOpenPageButtonIcon";
    public static final String CARD_OPEN_PAGE_ACTION_DEFINITION = "CardOpenPageActionDefinition";
    public static final String CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN = "CardWidgetDeclarationPrimitiveColumn";
    public static final String CARD_COLUMN_DECLARATION_COLUMN_ICON = "CardColumnDeclarationColumnIcon";
    public static final String CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER = "CardWidgetDeclarationPrimitiveColumnFilter";

    // =========================================================================
    // view/tagDeclaration.etl
    // =========================================================================
    public static final String TAG_PAGE_CONTAINER = "TagPageContainer";
    public static final String TAG_PAGE_CONTAINER_BUTTON_GROUP = "TagPageContainerButtonGroup";
    public static final String TAG_PAGE_CONTAINER_BACK_BUTTON_ICON = "TagPageContainerBackButtonIcon";
    public static final String TAG_PAGE_CONTAINER_BACK_ACTION_DEFINITION = "TagPageContainerBackActionDefinition";
    public static final String TAG_PAGE_CONTAINER_BACK_BUTTON = "TagPageContainerBackButton";
    public static final String TAG_PAGE_CONTAINER_VISUAL_ELEMENT = "TagPageContainerVisualElement";
    public static final String TAG_TABLE = "TagTable";
    public static final String TAG_TABLE_BUTTON_GROUP = "TagTableButtonGroup";
    public static final String TAG_TABLE_ROW_BUTTON_GROUP = "TagTableRowButtonGroup";
    public static final String TAG_TABLE_FILTER_BUTTON = "TagTableFilterButton";
    public static final String TAG_TABLE_FILTER_BUTTON_ICON = "TagTableFilterButtonIcon";
    public static final String TAG_TABLE_FILTER_ACTION_DEFINITION = "TagTableFilterActionDefinition";
    public static final String TAG_TABLE_REFRESH_BUTTON = "TagTableRefreshButton";
    public static final String TAG_TABLE_REFRESH_ACTION_DEFINITION = "TagTableRefreshActionDefinition";
    public static final String TAG_OPEN_PAGE_BUTTON = "TagOpenPageButton";
    public static final String TAG_OPEN_PAGE_ACTION_DEFINITION = "TagOpenPageActionDefinition";
    public static final String TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN = "TagWidgetDeclarationPrimitiveColumn";
    public static final String TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER = "TagWidgetDeclarationPrimitiveColumnFilter";

    // =========================================================================
    // view/enumLiteral.etl
    // =========================================================================
    public static final String ENUMERATION_MEMBER_OPTION = "EnumerationMemberOption";
}
