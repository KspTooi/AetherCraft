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
                 :class="{ 'active': modelValue === model.modelCode }"
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
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import { useThemeStore } from '../stores/theme'

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const selectorColor = computed(() => themeStore.selectorColor)
const selectorActiveColor = computed(() => themeStore.selectorActiveColor)
const selectorBorderColor = computed(() => themeStore.selectorBorderColor)
const mainBlur = computed(() => themeStore.mainBlur)

// 定义组件的props和emit
const props = defineProps<{
  modelValue: string // v-model绑定值
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// 状态定义
const isOpen = ref(false)
const loading = ref(false)
const models = ref<any[]>([])

// 计算当前选中模型的名称
const selectedModelName = computed(() => {
  const model = models.value.find(m => m.modelCode === props.modelValue)
  return model ? model.modelName : ''
})

// 按系列分组的模型列表
const groupedModels = computed(() => {
  const groups = new Map<string, any[]>()
  
  models.value.forEach(model => {
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

const selectModel = (model: any) => {
  emit('update:modelValue', model.modelCode)
  closeDropdown()
}

// 加载模型列表
const loadModels = async () => {
  loading.value = true
  try {
    const response = await axios.post('/model/series/getModelSeries')
    if (response.data.code === 0) {
      models.value = response.data.data
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  loadModels()
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
  margin-bottom: 2px; /* 为了留出底部过渡的空间 */
}

.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background: v-bind(selectorColor);
  border: 1px solid v-bind(selectorBorderColor);
  border-radius: 0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative; /* 为after伪元素定位 */
}

/* 移除底部边框，用渐变阴影替代 */
.selector-header::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  background: linear-gradient(to bottom, v-bind(selectorBorderColor), transparent);
  pointer-events: none;
  transition: opacity 0.3s ease;
  opacity: 0.7;
}

.selector-header:hover {
  background: v-bind(selectorActiveColor);
  border-color: rgba(255, 255, 255, 0.3);
}

.selector-header:hover::after {
  opacity: 1;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.3), transparent);
}

.selected-model {
  flex: 1;
  min-width: 0;
}

.model-name {
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.2px; /* 增加字母间距 */
}

.bi-chevron-down {
  font-size: 14px;
  color: v-bind(selectorBorderColor);
  transition: transform 0.3s ease, color 0.3s ease;
  margin-left: 5px;
}

.bi-chevron-down.rotated {
  transform: rotate(180deg);
  color: rgba(255, 255, 255, 0.85);
}

.selector-header:hover .model-name {
  color: rgba(255, 255, 255, 1);
}

.selector-header:hover .bi-chevron-down {
  color: rgba(255, 255, 255, 0.85);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 2px);
  left: 0;
  right: 0;
  background: rgba(20, 30, 55, 0.8);
  border: 1px solid v-bind(selectorBorderColor);
  border-radius: 0;
  max-height: 250px;
  overflow-y: auto;
  z-index: 1000;
  backdrop-filter: blur(v-bind(mainBlur));
  -webkit-backdrop-filter: blur(v-bind(mainBlur));
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3), 0 0 1px v-bind(selectorBorderColor);
}

.loading-state {
  padding: 12px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
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
  border-bottom: 1px solid v-bind(selectorBorderColor);
}

.series-name {
  padding: 3px 10px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.model-item {
  padding: 6px 10px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 0.2px;
}

.model-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.model-item.active {
  background: v-bind(selectorActiveColor);
  color: rgba(255, 255, 255, 0.8);
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
  background: v-bind(selectorBorderColor);
  border-radius: 1.5px;
}

.dropdown-menu::-webkit-scrollbar-thumb:hover {
  background: v-bind(primaryColor);
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