<template>
  <div class="model-selector" v-click-outside="closeDropdown">
    <!-- 选择器头部 -->

    <div class="selector-header" @click="toggleDropdown">
      <div class="selected-model">
        <span class="model-name">{{ selectedModelName || '请选择模型' }}</span>
      </div>
      <i class="bi bi-chevron-down" :class="{ 'rotated': isOpen }"></i>
    </div>

    <!-- 下拉列表 -->
    <transition name="dropdown">
      <div class="dropdown-menu" v-show="isOpen">
        <div v-if="loading" class="loading-state">
          <i class="bi bi-arrow-repeat spinning"></i>
          加载中...
        </div>
        <template v-else>
          <!-- 按系列分组显示模型 -->
          <div v-for="series in groupedModels" :key="series.name" class="model-series">
            <div class="series-name">{{ series.name }}</div>
            <div class="model-item" 
                 v-for="model in series.models" 
                 :key="model.modelCode"
                 :class="{ 'active': selected === model.modelCode }"
                 @click="selectModel(model)">
              {{ model.modelName }}
            </div>
          </div>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, inject } from 'vue'
import http from '@/commons/Http'
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 数据模型
interface ModelData {
  modelCode: string // 模型代码(不展示)
  modelName: string // 模型名称(展示)
  series: string    // 模型系列(分组)
}

const props = defineProps<{
  selected: string //当前选择的模型代码
}>()

const emits = defineEmits<{
  (e: "select-model", modelCode: string): void //选择模型时触发
}>()

// 状态定义
const isOpen = ref(false)
const loading = ref(false)
const data = ref<ModelData[]>([])

// 监听外部selected变化
watch(() => props.selected, (newValue) => {
  if (newValue && data.value.length > 0) {
    const exists = data.value.some(model => model.modelCode === newValue)
    if (!exists) {
      loadModelList()
    }
  }
})

// 计算当前选中模型的名称
const selectedModelName = computed(() => {
  const model = data.value.find(m => m.modelCode === props.selected)
  return model ? model.modelName : ''
})

// 按系列分组的模型列表
const groupedModels = computed(() => {
  const groups = new Map<string, ModelData[]>()
  
  data.value.forEach(model => {
    if (!groups.has(model.series)) {
      groups.set(model.series, [])
    }
    groups.get(model.series)?.push(model)
  })
  
  return Array.from(groups.entries()).map(([name, models]) => ({
    name,
    models
  }))
})

// 方法定义
const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const closeDropdown = () => {
  isOpen.value = false
}

const selectModel = (model: ModelData) => {
  emits('select-model', model.modelCode)
  closeDropdown()
}

// 重新加载模型列表
const loadModelList = async () => {
  loading.value = true
  try {
    const modelList = await http.postEntity<ModelData[]>('/model/series/getModelSeries', {})
    data.value = modelList
    
    // 确保在有模型数据时自动选择
    if (data.value.length > 0) {
      // 如果当前未选择模型或选择的模型不在列表中，自动选择第一个
      const exists = data.value.some(model => model.modelCode === props.selected)
      if (!props.selected || !exists) {
        emits('select-model', data.value[0].modelCode)
      }
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  loadModelList()
})

// 指令定义
const vClickOutside = {
  mounted(el: any, binding: any) {
    el._clickOutside = (event: any) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el: any) {
    document.removeEventListener('click', el._clickOutside)
  }
}

</script>

<style scoped>
.model-selector {
  position: relative;
  width: 100%;
  user-select: none;
  margin-bottom: 2px;
}

.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background: v-bind('theme.boxColor');
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.selected-model {
  flex: 1;
  min-width: 0;
}

.model-name {
  color: v-bind('theme.boxTextColor');
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.2px;
}

.bi-chevron-down {
  font-size: 14px;
  color: v-bind('theme.boxBorderColor');
  transition: transform 0.3s ease, color 0.3s ease;
  margin-left: 5px;
}

.bi-chevron-down.rotated {
  transform: rotate(180deg);
  color: v-bind('theme.boxTextColor');
}

.selector-header:hover .bi-chevron-down {
  color: v-bind('theme.boxTextColor');
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 2px);
  left: 0;
  right: 0;
  background: v-bind('theme.boxSecondColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  max-height: 250px;
  overflow-y: auto;
  z-index: 1000;
  backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.loading-state {
  padding: 12px;
  text-align: center;
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 14px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.model-series {
  padding: 5px 0;
}

.model-series:not(:last-child) {
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
}

.series-name {
  padding: 3px 10px;
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.model-item {
  padding: 6px 10px;
  color: v-bind('theme.boxTextColor');
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 0.2px;
}

.model-item:hover {
  background: v-bind('theme.boxSecondColorHover');
  color: v-bind('theme.boxTextColor');
}

.model-item.active {
  background: v-bind('theme.mainColor');
  color: v-bind('theme.boxTextColor');
  font-weight: 500;
}

/* 自定义滚动条样式 */
.dropdown-menu::-webkit-scrollbar {
  width: 3px;
}

.dropdown-menu::-webkit-scrollbar-track {
  background: transparent;
}

.dropdown-menu::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
  border-radius: 1.5px;
}

.dropdown-menu::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColorHover');
}

/* 下拉动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: top;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: scaleY(0.8) translateY(-5px);
}
</style>