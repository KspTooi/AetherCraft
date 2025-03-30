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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'

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
}

.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.selector-header:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.selected-model {
  flex: 1;
  min-width: 0;
}

.model-name {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bi-chevron-down {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  transition: transform 0.3s ease;
}

.bi-chevron-down.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 1000;
  backdrop-filter: blur(10px);
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
  padding: 8px 0;
}

.model-series:not(:last-child) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.series-name {
  padding: 4px 12px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  font-weight: 500;
}

.model-item {
  padding: 8px 12px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.model-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.model-item.active {
  background: rgba(79, 172, 254, 0.2);
  color: #4facfe;
}

/* 自定义滚动条样式 */
.dropdown-menu::-webkit-scrollbar {
  width: 4px;
}

.dropdown-menu::-webkit-scrollbar-track {
  background: transparent;
}

.dropdown-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.dropdown-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style> 