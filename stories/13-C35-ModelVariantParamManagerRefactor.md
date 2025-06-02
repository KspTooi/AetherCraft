# 13-C35: 模型变体参数管理器页面 (ModelVariantParamManager.vue) 重构计划

## 1. 目标

重构 `ModelVariantParamManager.vue` 页面，使其能够一次性列出所有可用的模型变体。用户可以从列表中选择一个模型变体，并通过模态框管理其参数。模态框将复用现有的 `ModelVariantParamsModal.vue` 组件。

## 2. 总体步骤

- [x] **后端 API 调整**
- [x] **前端 API 定义更新**
- [x] **前端 Vue 模板更新**

## 3. 详细开发步骤

### 3.1. 后端 API 调整 (Java)

-   [x] **更新 `GetAdminModelVariantListVo.java`**:
    -   [x] 目的：添加 `paramCount` 字段，表示模型变体的全局参数数量。
-   [x] **调整 `ModelVariantRepository.java`**:
    -   [x] 目的：修改 `getAdminModelVariantList` 方法的 JPQL 查询，使其能够返回每个模型变体的 `paramCount`（该模型变体的**全局参数总数量**，即 `user_id` 和 `player_id` 均为空的参数记录总数）。确保查询结果中也包含 `series` 字段以供前端展示。
    -   [x] **BUG修复/改进**：调整 `getAdminModelVariantList` 方法中 `GetAdminModelVariantListVo` 构造函数的 JPQL 投影格式，进行适当换行以提高可读性。
    -   [x] **BUG修复**：修正 `getAdminModelVariantList` 方法中 JPQL 投影时，`paramCount` 子查询的 `COUNT()` 结果类型与 `GetAdminModelVariantListVo` 构造函数参数类型不匹配的问题（将 `COUNT()` 结果强制转换为 `Integer`）。
-   [x] **调整 `ModelVariantService.java`**:
    -   [x] 目的：修改 `getModelVariantList` 方法，确保它能够正确获取并处理包含 `series` 和 `paramCount` 的模型变体列表。
-   [x] **检查 `ModelVariantController.java`**:
    -   [x] 目的：确保 `/admin/model/variant/getModelVariantList` 接口能够正确返回更新后的VO结构。

### 3.2. 前端 API 定义更新 (TypeScript)

-   [x] **定位并更新 `AdminModelVariantApi.ts`**:
    -   [x] 目的：更新 `GetAdminModelVariantListVo` 类型/接口，确保包含 `paramCount` 字段，并确保 `getModelVariantList` 函数的定义与后端新结构一致。
    -   [x] **BUG修复/改进**：将 `AdminModelVariantApi.ts` 中 `GetAdminModelSeriesListDto`、`SaveAdminModelSeriesDto`、`GetAdminModelSeriesListVo` 和 `GetAdminModelSeriesDetailsVo` 更名为 `GetAdminModelVariantListDto`、`SaveAdminModelVariantDto`、`GetAdminModelVariantListVo` 和 `GetAdminModelVariantDetailsVo`，以保持命名一致性。

### 3.3. 前端 Vue 模板更新 (`ModelVariantParamManager.vue`)

-   [x] **修改 `ModelVariantParamManager.vue`**:
    -   [x] 目的：移除旧的UI和逻辑，替换为显示所有模型变体的表格。
    -   [x] 目的：表格应展示模型名称、模型系列、参数数量、创建时间。
    -   [x] 目的：为每个模型变体添加 "管理参数" 按钮，点击后弹出 `ModelVariantParamsModal.vue` 模态框，并传递正确的 `modelVariantId` 和 `modelVariantName`。
    -   [x] 目的：更新页面查询和分页逻辑以适应新的列表展示方式。
    -   [x] 目的：清理不再需要的状态变量、计算属性和方法。
    -   [ ] **修订**：将列表的默认分页大小设置为 10。

## 4. 预期交付物

1.  更新后的 Java 后端代码 (`ModelVariantService.java`, `ModelVariantRepository.java`, `GetAdminModelVariantListVo.java`)。
2.  更新后的 TypeScript API 定义 (`AdminModelVariantApi.ts` 或相关文件)。
3.  重构后的 `ModelVariantParamManager.vue` Vue 组件文件。
4.  此开发计划文档（在后续步骤中持续更新完成状态）。 