<template>
  <div class="color-picker-container">
    <GlowDiv class="color-box" @click="toggleColorPanel" :class="{ 'glow-border': showPanel }">
      <div class="color-display" :style="{ backgroundColor: modelValue }"></div>
    </GlowDiv>
    
    <transition name="panel-transition">
      <GlowDiv v-if="showPanel" class="color-panel" @mousedown.stop>
        <div class="color-area">
          <div 
            class="color-gradient"
            ref="colorArea"
            @mousedown="startPickingColor"
            :style="{ backgroundColor: `hsl(${hue}, 100%, 50%)` }"
          >
            <div class="white-gradient"></div>
            <div class="black-gradient"></div>
            <div 
              class="color-pointer"
              :style="{
                left: `${saturation}%`,
                top: `${100 - value}%`
              }"
            ></div>
          </div>
        </div>
        
        <div class="slider-area">
          <div class="slider-container">
            <div class="slider-label">色相</div>
            <input 
              type="range" 
              min="0" 
              max="360" 
              v-model="hue" 
              class="hue-slider"
              @mousedown.stop
            />
          </div>
          
          <div class="slider-container">
            <div class="slider-label">透明度</div>
            <div class="alpha-slider-bg">
              <input 
                type="range" 
                min="0" 
                max="100" 
                v-model="alpha" 
                class="alpha-slider"
                @mousedown.stop
              />
            </div>
          </div>
        </div>
        
        <div class="preview-area">
          <GlowDiv class="color-preview" border="none">
            <div class="preview-box" :style="{ backgroundColor: modelValue }"></div>
          </GlowDiv>
          <div class="color-value">{{ modelValue }}</div>
        </div>
        
        <div class="button-area">
          <GlowButton @click="confirmColor" :corners="['top-right']">确认</GlowButton>
          <GlowButton @click="cancelSelection" theme="danger" :corners="['top-left']">取消</GlowButton>
        </div>
      </GlowDiv>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { inject } from 'vue'
import { GLOW_THEME_INJECTION_KEY, type GlowThemeColors, defaultTheme } from './GlowTheme'
import GlowDiv from './GlowDiv.vue'
import GlowButton from './GlowButton.vue'

const props = defineProps<{
  color: string // rgba
}>()

const emit = defineEmits<{
  (e: "on-color-selected", color: string): void
}>()

const theme = inject(GLOW_THEME_INJECTION_KEY, defaultTheme) as GlowThemeColors

// 颜色面板显示状态
const showPanel = ref(false)

// 颜色参数
const hue = ref(0)
const saturation = ref(100)
const value = ref(100)
const alpha = ref(100)

// 初始颜色和当前选择的颜色
const initialColor = ref(props.color)
const modelValue = ref(props.color)

// 颜色区域引用
const colorArea = ref<HTMLElement | null>(null)

// 添加拖动状态控制
const isDragging = ref(false)
const isPickingColor = ref(false)

// 解析初始RGBA颜色
const parseRgba = (rgba: string) => {
  const regex = /rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([0-9.]+))?\)/
  const matches = rgba.match(regex)
  
  if (matches) {
    const r = parseInt(matches[1])
    const g = parseInt(matches[2])
    const b = parseInt(matches[3])
    const a = matches[4] ? parseFloat(matches[4]) : 1
    
    // 转换为HSV
    const rgbToHsv = (r: number, g: number, b: number) => {
      r /= 255
      g /= 255
      b /= 255
      
      const max = Math.max(r, g, b)
      const min = Math.min(r, g, b)
      const d = max - min
      let h = 0
      const s = max === 0 ? 0 : d / max
      const v = max
      
      if (max !== min) {
        switch (max) {
          case r: h = (g - b) / d + (g < b ? 6 : 0); break
          case g: h = (b - r) / d + 2; break
          case b: h = (r - g) / d + 4; break
        }
        h /= 6
      }
      
      return {
        h: Math.round(h * 360),
        s: Math.round(s * 100),
        v: Math.round(v * 100)
      }
    }
    
    const hsv = rgbToHsv(r, g, b)
    hue.value = hsv.h
    saturation.value = hsv.s
    value.value = hsv.v
    alpha.value = Math.round(a * 100)
  }
}

// 计算当前RGBA颜色
const calculateRgba = () => {
  const hsvToRgb = (h: number, s: number, v: number) => {
    h /= 360
    s /= 100
    v /= 100
    
    let r, g, b
    
    const i = Math.floor(h * 6)
    const f = h * 6 - i
    const p = v * (1 - s)
    const q = v * (1 - f * s)
    const t = v * (1 - (1 - f) * s)
    
    switch (i % 6) {
      case 0: r = v; g = t; b = p; break
      case 1: r = q; g = v; b = p; break
      case 2: r = p; g = v; b = t; break
      case 3: r = p; g = q; b = v; break
      case 4: r = t; g = p; b = v; break
      case 5: r = v; g = p; b = q; break
      default: r = 0; g = 0; b = 0
    }
    
    return {
      r: Math.round(r * 255),
      g: Math.round(g * 255),
      b: Math.round(b * 255)
    }
  }
  
  const rgb = hsvToRgb(hue.value, saturation.value, value.value)
  const a = alpha.value / 100
  
  return `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${a})`
}

// 监听颜色参数变化，更新颜色值
watch([hue, saturation, value, alpha], () => {
  modelValue.value = calculateRgba()
})

// 监听props.color变化
watch(() => props.color, (newColor) => {
  if (newColor !== modelValue.value) {
    modelValue.value = newColor
    parseRgba(newColor)
  }
})

// 组件挂载时添加事件监听
onMounted(() => {
  // 解析初始颜色
  parseRgba(props.color);
  initialColor.value = props.color;
  
  // 添加全局点击事件监听
  document.addEventListener('mousedown', handleGlobalMouseDown, true);
  
  // 为所有输入元素添加点击事件阻止冒泡
  const inputs = document.querySelectorAll('.color-picker-container input');
  inputs.forEach(input => {
    input.addEventListener('click', (e) => e.stopPropagation());
  });
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('mousedown', handleGlobalMouseDown, true)
})

// 全局鼠标按下事件处理
const handleGlobalMouseDown = (event: MouseEvent) => {
  // 如果面板未显示，无需处理
  if (!showPanel.value) return;

  // 检查点击是否在颜色选择器容器外部
  const container = document.querySelector('.color-picker-container') as HTMLElement;
  if (!container) return;
  
  // 使用事件路径检查更准确
  const path = event.composedPath();
  const isOutside = !path.includes(container);
  
  // 只有在点击外部且不在拾取颜色状态时才关闭面板
  if (isOutside && !isPickingColor.value) {
    showPanel.value = false;
  }
}

// 开始拾取颜色
const startPickingColor = (event: MouseEvent) => {
  // 阻止事件冒泡，避免触发全局点击事件
  event.preventDefault();
  event.stopPropagation();
  
  // 设置拖动和拾取状态
  isDragging.value = true;
  isPickingColor.value = true;
  
  // 立即更新颜色
  updateColorFromPosition(event);
  
  // 处理鼠标移动
  const moveHandler = (e: MouseEvent) => {
    if (isDragging.value) {
      e.preventDefault();
      updateColorFromPosition(e);
    }
  };
  
  // 处理鼠标释放
  const upHandler = () => {
    isDragging.value = false;
    
    // 延迟重置拾取状态，防止面板立即关闭
    setTimeout(() => {
      isPickingColor.value = false;
    }, 100);
    
    // 移除临时事件监听
    document.removeEventListener('mousemove', moveHandler);
    document.removeEventListener('mouseup', upHandler);
  };
  
  // 添加临时事件监听
  document.addEventListener('mousemove', moveHandler);
  document.addEventListener('mouseup', upHandler);
}

// 切换颜色面板显示
const toggleColorPanel = (event: MouseEvent) => {
  event.preventDefault()
  event.stopPropagation()
  
  if (!isDragging.value) {
    showPanel.value = !showPanel.value
  }
}

// 确认颜色选择
const confirmColor = (event: MouseEvent) => {
  if (event) {
    event.preventDefault();
    event.stopPropagation();
  }
  showPanel.value = false;
  isPickingColor.value = false;
  emit('on-color-selected', modelValue.value);
}

// 取消选择
const cancelSelection = (event: MouseEvent) => {
  if (event) {
    event.preventDefault();
    event.stopPropagation();
  }
  showPanel.value = false;
  isPickingColor.value = false;
  modelValue.value = initialColor.value;
  parseRgba(initialColor.value);
}

// 从鼠标位置更新颜色
const updateColorFromPosition = (event: MouseEvent) => {
  if (!colorArea.value) return
  
  const rect = colorArea.value.getBoundingClientRect()
  let x = event.clientX - rect.left
  let y = event.clientY - rect.top
  
  // 限制在区域内
  x = Math.max(0, Math.min(rect.width, x))
  y = Math.max(0, Math.min(rect.height, y))
  
  // 计算饱和度和明度（使用HSV空间）
  saturation.value = Math.round((x / rect.width) * 100)
  value.value = Math.round(100 - (y / rect.height) * 100)
}
</script>

<style scoped>
.color-picker-container {
  position: relative;
  display: inline-block;
}

.color-box {
  width: 30px;
  height: 30px;
  cursor: pointer;
  transition: all 0.3s;
}

.color-display {
  width: 100%;
  height: 100%;
  border-radius: 0;
}

.color-panel {
  position: absolute;
  top: 40px;
  left: 0;
  width: 240px;
  padding: 12px;
  z-index: 1000;
}

.color-area {
  margin-bottom: 10px;
}

.color-gradient {
  position: relative;
  width: 100%;
  height: 120px;
  border-radius: 0;
  overflow: hidden;
}

.white-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, rgba(255, 255, 255, 1), rgba(255, 255, 255, 0));
  pointer-events: none;
}

.black-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0), rgba(0, 0, 0, 1));
  pointer-events: none;
}

.color-pointer {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 0;
  border: 1px solid white;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.8);
  pointer-events: none;
}

.slider-area {
  margin-bottom: 10px;
  padding: 0;
}

.slider-container {
  margin-bottom: 8px;
  padding: 0;
}

.slider-label {
  font-size: 12px;
  margin-bottom: 4px;
}

.hue-slider {
  width: 100%;
  height: 16px;
  -webkit-appearance: none;
  background: linear-gradient(
    to right,
    #ff0000, #ffff00, #00ff00, #00ffff, #0000ff, #ff00ff, #ff0000
  );
  border-radius: 0;
  outline: none;
}

.alpha-slider-bg {
  position: relative;
  width: 100%;
  height: 16px;
  background-image: linear-gradient(45deg, #ccc 25%, transparent 25%),
                    linear-gradient(-45deg, #ccc 25%, transparent 25%),
                    linear-gradient(45deg, transparent 75%, #ccc 75%),
                    linear-gradient(-45deg, transparent 75%, #ccc 75%);
  background-size: 8px 8px;
  background-position: 0 0, 0 4px, 4px -4px, -4px 0px;
  border-radius: 0;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.alpha-slider-bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, transparent, v-bind('modelValue'));
  z-index: 1;
  pointer-events: none;
}

.alpha-slider {
  position: relative;
  width: 100%;
  height: 16px;
  -webkit-appearance: none;
  background: transparent;
  z-index: 2;
  margin: 0;
  padding: 0;
  display: block;
}

.preview-area {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.color-preview {
  width: 30px;
  height: 30px;
  padding: 2px;
}

.preview-box {
  width: 100%;
  height: 100%;
  border-radius: 0;
}

.color-value {
  flex: 1;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.button-area {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

input[type="range"] {
  -webkit-appearance: none;
  border-radius: 0;
  outline: none;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 12px;
  height: 16px;
  border-radius: 0;
  background: white;
  border: 1px solid #888;
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
  cursor: pointer;
}

/* 添加过渡动画效果 */
.panel-transition-enter-active,
.panel-transition-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
  transform-origin: top left;
}

.panel-transition-enter-from,
.panel-transition-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}

.panel-transition-enter-to,
.panel-transition-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

/* 添加颜色选择器的点击效果 */
.color-box {
  transition: transform 0.2s ease;
}

.color-box:active {
  transform: scale(0.95);
}

/* 为各种交互元素添加过渡效果 */
.color-gradient,
.slider-container input,
.preview-box {
  transition: all 0.2s ease;
}

.color-pointer {
  transition: box-shadow 0.2s ease;
}

.color-pointer:hover {
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}
</style>