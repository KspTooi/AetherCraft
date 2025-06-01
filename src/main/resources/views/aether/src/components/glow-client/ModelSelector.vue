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
              <span class="model-name-text">{{ model.modelName }}</span>
              <div class="model-tags">
                <span v-if="model.thinking === 1" class="tag thinking-tag">
                  思考
                </span>
<!--                <span class="tag size-tag" :class="getSizeClass(model.size)">
                  {{ getSizeDisplay(model.size) }}
                </span>-->
                <span class="tag speed-tag" :class="getSpeedClass(model.speed)">
                  {{ model.speed }}
                </span>
                <span class="tag intelligence-tag" :class="getIntelligenceClass(model.intelligence)">
                  {{ model.intelligence }}
                </span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, inject } from 'vue'
import ModelVariantApi, { type ClientModelVariant } from '@/commons/api/ModelVariantApi'
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 数据模型（为了兼容现有模板，保持原有字段名）
interface ModelData {
  modelCode: string // 模型代码(不展示)
  modelName: string // 模型名称(展示)
  series: string    // 模型系列(分组)
  size: string      // 模型规模
  speed: string     // 模型速度
  intelligence: string // 智能程度
  thinking: number  // 思考能力
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

// 数据转换函数
const convertApiDataToModelData = (apiData: ClientModelVariant[]): ModelData[] => {
  return apiData.map(item => ({
    modelCode: item.code,
    modelName: item.name,
    series: item.series,
    size: getSizeDisplay(item.scale),
    speed: getSpeedDisplay(item.speed),
    intelligence: getIntelligenceDisplay(item.intelligence),
    thinking: item.thinking
  }))
}

// 将数字转换为显示文本
const getSizeDisplay = (scale: number): string => {
  const sizeMap: Record<number, string> = {
    0: '小型',
    1: '中型', 
    2: '大型'
  }
  return sizeMap[scale] || '中型'
}

const getSpeedDisplay = (speed: number): string => {
  const speedMap: Record<number, string> = {
    0: '慢速',
    1: '中速',
    2: '快速',
    3: '极快'
  }
  return speedMap[speed] || '中速'
}

const getIntelligenceDisplay = (intelligence: number): string => {
  const intelligenceMap: Record<number, string> = {
    0: '木质',
    1: '石质',
    2: '铁质',
    3: '钻石',
    4: '纳米',
    5: '量子'
  }
  return intelligenceMap[intelligence] || '石质'
}

// 重新加载模型列表
const loadModelList = async () => {
  loading.value = true
  try {
    const apiData = await ModelVariantApi.getModelVariantList()
    data.value = convertApiDataToModelData(apiData)
    
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

// 获取模型大小样式类
const getSizeClass = (size: string): string => {
  const sizeMap: Record<string, string> = {
    '大型': 'size-l',
    '中型': 'size-m',
    '小型': 'size-s'
  }
  return sizeMap[size] || 'size-m'
}

// 获取模型速度样式类
const getSpeedClass = (speed: string): string => {
  const speedMap: Record<string, string> = {
    '极快': 'speed-fastest',
    '快速': 'speed-fast',
    '中速': 'speed-medium',
    '慢速': 'speed-slow'
  }
  return speedMap[speed] || 'speed-medium'
}

// 获取智能程度样式类
const getIntelligenceClass = (intelligence: string): string => {
  const intelligenceMap: Record<string, string> = {
    '量子': 'intelligence-elite',
    '纳米': 'intelligence-diamond',
    '钻石': 'intelligence-platinum',
    '铁质': 'intelligence-silver',
    '石质': 'intelligence-bronze',
    '木质': 'intelligence-wood'
  }
  return intelligenceMap[intelligence] || 'intelligence-bronze'
}

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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
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

.model-name-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-tags {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 6px;
  border-radius: 0; /* 直角风格 */
  font-size: 10px;
  font-weight: 600;
  line-height: 1;
  min-width: 18px;
  height: 16px;
  text-align: center;
  border: 1px solid;
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  transition: all 0.2s ease;
  position: relative;
}

/* 大小标签样式 */
.size-tag.size-l {
  background: v-bind('theme.dangerColor');
  border-color: v-bind('theme.dangerBorderColor');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px v-bind('theme.dangerBorderColor');
}

.size-tag.size-m,
.size-tag.size-s {
  background: v-bind('theme.boxAccentColor');
  border-color: v-bind('theme.boxBorderColor');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px v-bind('theme.boxBorderColor');
}

/* 速度标签样式 */
.speed-tag.speed-fastest {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.7);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px rgba(34, 197, 94, 0.5);
  font-weight: 600;
}

.speed-tag.speed-fast {
  background: rgba(22, 163, 74, 0.2);
  border-color: rgba(22, 163, 74, 0.7);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px rgba(22, 163, 74, 0.5);
  font-weight: 600;
}

.speed-tag.speed-medium {
  background: rgba(21, 128, 61, 0.2);
  border-color: rgba(21, 128, 61, 0.7);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px rgba(21, 128, 61, 0.5);
  font-weight: 600;
}

.speed-tag.speed-slow {
  background: rgba(20, 83, 45, 0.2);
  border-color: rgba(20, 83, 45, 0.7);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px rgba(20, 83, 45, 0.5);
}

.speed-tag.speed-slowest {
  background: rgba(14, 59, 32, 0.2);
  border-color: rgba(14, 59, 32, 0.7);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px rgba(14, 59, 32, 0.5);
}

/* 智能程度标签样式 */
.intelligence-tag.intelligence-elite {
  background: rgba(52, 14, 115, 0.5);
  border-color: rgba(112, 40, 230, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 10px rgba(92, 25, 191, 0.8);
}

.intelligence-tag.intelligence-diamond {
  background: rgba(159, 58, 3, 0.6);
  border-color: rgb(159, 58, 3);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 10px rgba(159, 58, 3, 0.8);
}

.intelligence-tag.intelligence-platinum {
  background: rgba(158, 97, 1, 0.6);
  border-color: rgba(158, 97, 1, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 10px rgba(158, 97, 1, 0.8);
}

.intelligence-tag.intelligence-gold,
.intelligence-tag.intelligence-silver,
.intelligence-tag.intelligence-bronze,
.intelligence-tag.intelligence-wood {
  background: v-bind('theme.boxAccentColor');
  border-color: v-bind('theme.boxBorderColor');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 4px v-bind('theme.boxBorderColor');
}

/* 思考标签样式 */
.thinking-tag {
  background: v-bind('theme.boxGlowColor');
  border-color: v-bind('theme.boxGlowColor');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 6px v-bind('theme.boxGlowColor');
  padding: 2px 6px;
  min-width: 24px;
  font-size: 10px;
  font-weight: 600;
}

/* 激活状态下的标签样式调整 */
.model-item.active .size-tag.size-l {
  background: v-bind('theme.dangerColorActive');
  border-color: v-bind('theme.dangerBorderColorActive');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px v-bind('theme.dangerBorderColorActive');
}

.model-item.active .size-tag.size-m,
.model-item.active .size-tag.size-s {
  background: v-bind('theme.boxColorActive');
  border-color: v-bind('theme.boxBorderColorHover');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px v-bind('theme.boxBorderColorHover');
}

.model-item.active .speed-tag.speed-fastest {
  background: rgba(34, 197, 94, 0.4);
  border-color: rgba(34, 197, 94, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.7);
}

.model-item.active .speed-tag.speed-fast {
  background: rgba(22, 163, 74, 0.4);
  border-color: rgba(22, 163, 74, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px rgba(22, 163, 74, 0.7);
}

.model-item.active .speed-tag.speed-medium {
  background: rgba(21, 128, 61, 0.4);
  border-color: rgba(21, 128, 61, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px rgba(21, 128, 61, 0.7);
}

.model-item.active .speed-tag.speed-slow {
  background: rgba(20, 83, 45, 0.4);
  border-color: rgba(20, 83, 45, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px rgba(20, 83, 45, 0.7);
}

.model-item.active .speed-tag.speed-slowest {
  background: rgba(14, 59, 32, 0.4);
  border-color: rgba(14, 59, 32, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px rgba(14, 59, 32, 0.7);
}

.model-item.active .intelligence-tag.intelligence-elite {
  background: rgba(52, 14, 115, 0.5);
  border-color: rgba(112, 40, 230, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 10px rgba(92, 25, 191, 0.8);
}

.model-item.active .intelligence-tag.intelligence-diamond {
  background: rgba(159, 58, 3, 0.6);
  border-color: rgb(159, 58, 3);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 10px rgba(159, 58, 3, 0.8);
}

.model-item.active .intelligence-tag.intelligence-platinum {
  background: rgba(158, 97, 1, 0.6);
  border-color: rgba(158, 97, 1, 1);
  color: rgb(255, 255, 255);
  box-shadow: 0 0 10px rgba(158, 97, 1, 0.8);
}

.model-item.active .intelligence-tag.intelligence-gold,
.model-item.active .intelligence-tag.intelligence-silver,
.model-item.active .intelligence-tag.intelligence-bronze,
.model-item.active .intelligence-tag.intelligence-wood {
  background: v-bind('theme.boxColorActive');
  border-color: v-bind('theme.boxBorderColorHover');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 8px v-bind('theme.boxBorderColorHover');
}

.model-item.active .thinking-tag {
  background: v-bind('theme.boxGlowColor');
  border-color: v-bind('theme.boxGlowColor');
  color: rgb(255, 255, 255);
  box-shadow: 0 0 10px v-bind('theme.boxGlowColor');
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