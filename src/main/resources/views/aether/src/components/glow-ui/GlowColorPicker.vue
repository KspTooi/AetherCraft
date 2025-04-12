<template>
  <div class="color-picker-container">
    <div 
      class="color-box" 
      @click="toggleColorPanel"
      @touchstart="handleTouchStart"
      :style="{ backgroundColor: modelValue }"
      ref="colorBox"
    />

    <Teleport to="body">
      <div v-show="showPanel" class="color-panel-overlay" @click.stop>
        <GlowDiv 
          class="color-select-panel" 
          borderGlow="top" 
          @click.stop
          :style="panelStyle"
        >
          <div class="color-area">
            <div 
              class="color-gradient"
              ref="colorArea"
              @mousedown="startPickingColor"
              @touchstart.prevent="startTouchPickingColor"
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
                />
              </div>
            </div>
          </div>
          
          <div class="preview-area">
            <div class="color-preview">
              <div class="preview-box" :style="{ backgroundColor: modelValue }"></div>
            </div>
            <input 
              class="color-value" 
              v-model="colorInputValue"
              @blur="validateColorInput"
              @keydown.enter="validateColorInput"
            />
          </div>
          
          <div class="button-area">
            <GlowButton @click="confirmColor"
                        @touchend="handleConfirmTouch"
                        style="height: 30px;min-height: 30px">
              确认
            </GlowButton>
            <GlowButton @click="closeSelectPanel"
                        @touchend="handleCancelTouch"
                        style="height: 30px;min-height: 30px">
              取消
            </GlowButton>
          </div>
        </GlowDiv>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick, inject } from 'vue'
import { GLOW_THEME_INJECTION_KEY, type GlowThemeColors, defaultTheme } from './GlowTheme'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { Teleport } from 'vue';
import GlowButton from "@/components/glow-ui/GlowButton.vue";

const theme = inject(GLOW_THEME_INJECTION_KEY, defaultTheme) as GlowThemeColors

const props = defineProps<{
  color: string //RGBA格式的颜色串
}>()

const emit = defineEmits<{
  (e: "update:color", color: string): void
}>()

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

// 用于输入框显示的颜色值
const colorInputValue = ref(props.color || 'rgba(0, 0, 0, 1)')

// 颜色选择器位置样式
const panelStyle = ref({
  position: 'fixed' as const,
  top: '0px',
  left: '0px',
  transform: 'none'
})

// 颜色选择器参考元素
const colorBox = ref<HTMLElement | null>(null)

// 判断字符串是否为空
const isBlank = (str: string | null | undefined): boolean => {
  return str === null || str === undefined || str.trim() === ''
}

// 解析初始RGBA颜色
const parseRgba = (rgba: string) => {
  if (isBlank(rgba)) {
    return
  }
  
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
    return
  }
  
  // 如果不是有效的RGBA颜色，设置默认值
  hue.value = 0
  saturation.value = 100
  value.value = 100
  alpha.value = 100
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

// 验证并解析用户输入的颜色
const validateColorInput = () => {
  // 尝试解析用户输入的颜色
  const regex = /rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([0-9.]+))?\)/
  const matches = colorInputValue.value.match(regex)
  
  if (matches) {
    const r = parseInt(matches[1])
    const g = parseInt(matches[2])
    const b = parseInt(matches[3])
    const a = matches[4] ? parseFloat(matches[4]) : 1
    
    // 验证RGB值是否在0-255范围内
    if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255 && a >= 0 && a <= 1) {
      // 有效的颜色值,更新模型
      modelValue.value = colorInputValue.value
      parseRgba(colorInputValue.value)
      return
    }
  }
  
  // 无效的颜色值,恢复为最后一次有效的值
  colorInputValue.value = modelValue.value
}

// 监听modelValue变化,更新输入框显示值
watch(() => modelValue.value, (newValue) => {
  if (isBlank(newValue)) {
    colorInputValue.value = 'rgba(0, 0, 0, 1)'
    return
  }
  colorInputValue.value = newValue
})

// 计算可用空间和选择器位置
const calculatePanelPosition = (clickEvent?: MouseEvent) => {
  // 检查是否为移动设备
  const isMobile = window.matchMedia('(hover: none) and (pointer: coarse)').matches;
  
  // 如果是移动设备，直接居中显示
  if (isMobile) {
    panelStyle.value = {
      position: 'fixed' as const,
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)'
    }
    return;
  }
  
  const PANEL_WIDTH = 240
  const PANEL_HEIGHT = 300 // 估算值，可以根据实际情况调整
  const MARGIN = 10 // 边距
  
  let x = 0
  let y = 0
  
  if (clickEvent) {
    // 使用点击事件的坐标
    x = clickEvent.clientX
    y = clickEvent.clientY
  } else if (colorBox.value) {
    // 使用颜色框的位置
    const rect = colorBox.value.getBoundingClientRect()
    x = rect.left
    y = rect.bottom + MARGIN
  }
  
  // 获取视口尺寸
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  
  // 调整X坐标，确保面板不超出视口左侧和右侧
  if (x < MARGIN) {
    // 左侧空间不足
    x = MARGIN
  } else if (x + PANEL_WIDTH + MARGIN > viewportWidth) {
    // 右侧空间不足
    x = viewportWidth - PANEL_WIDTH - MARGIN
  }
  
  // 检查底部空间
  if (y + PANEL_HEIGHT + MARGIN > viewportHeight) {
    // 底部空间不足，尝试在元素上方显示
    if (colorBox.value) {
      const rect = colorBox.value.getBoundingClientRect()
      if (rect.top > PANEL_HEIGHT + MARGIN) {
        // 上方空间足够
        y = rect.top - PANEL_HEIGHT - MARGIN
      } else {
        // 上方空间也不足，居中显示
        y = Math.max(MARGIN, (viewportHeight - PANEL_HEIGHT) / 2)
      }
    } else {
      // 无参考元素，调整为不超出底部
      y = Math.max(MARGIN, viewportHeight - PANEL_HEIGHT - MARGIN)
    }
  }
  
  // 设置位置样式
  panelStyle.value = {
    position: 'fixed' as const,
    top: `${y}px`,
    left: `${x}px`,
    transform: 'none'
  }
}

//打开/关闭颜色选择框
const toggleColorPanel = (event?: MouseEvent) => {
  // 如果要显示面板，先计算位置
  if (!showPanel.value) {
    initialColor.value = props.color || 'rgba(0, 0, 0, 1)'
    parseRgba(props.color)
    
    // 先计算面板位置
    if (event) {
      calculatePanelPosition(event)
    } else {
      calculatePanelPosition()
    }
    
    // 再显示面板
    showPanel.value = true
    return
  }
  
  // 关闭面板
  showPanel.value = false
}

//关闭颜色选择框
const closeSelectPanel = () => {
  showPanel.value = false
  modelValue.value = initialColor.value
}

// 确认颜色选择
const confirmColor = () => {
  showPanel.value = false
  emit('update:color', modelValue.value)
}

// 处理鼠标移动
const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value || !colorArea.value) {
    return
  }
  
  const rect = colorArea.value.getBoundingClientRect()
  let x = e.clientX - rect.left
  let y = e.clientY - rect.top
  
  // 限制在区域内
  x = Math.max(0, Math.min(rect.width, x))
  y = Math.max(0, Math.min(rect.height, y))
  
  // 计算饱和度和明度（使用HSV空间）
  saturation.value = Math.round((x / rect.width) * 100)
  value.value = Math.round(100 - (y / rect.height) * 100)
}

// 处理鼠标释放
const handleMouseUp = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

// 开始拾取颜色
const startPickingColor = (event: MouseEvent) => {
  isDragging.value = true
  
  // 立即更新颜色
  handleMouseMove(event)
  
  // 添加临时事件监听
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 开始触摸拾取颜色
const startTouchPickingColor = (event: TouchEvent) => {
  if (!event.touches[0]) {
    return
  }
  
  isDragging.value = true
  
  // 立即更新颜色
  handleTouchMove(event)
  
  // 添加临时事件监听
  document.addEventListener('touchmove', handleTouchMove, { passive: false })
  document.addEventListener('touchend', handleTouchEnd, { passive: false })
  document.addEventListener('touchcancel', handleTouchEnd, { passive: false })
}

// 处理触摸移动
const handleTouchMove = (e: TouchEvent) => {
  if (!isDragging.value || !colorArea.value || !e.touches[0]) {
    return
  }
  
  // 阻止默认行为，防止页面滚动
  e.preventDefault()
  
  const touch = e.touches[0]
  const rect = colorArea.value.getBoundingClientRect()
  let x = touch.clientX - rect.left
  let y = touch.clientY - rect.top
  
  // 限制在区域内
  x = Math.max(0, Math.min(rect.width, x))
  y = Math.max(0, Math.min(rect.height, y))
  
  // 计算饱和度和明度（使用HSV空间）
  saturation.value = Math.round((x / rect.width) * 100)
  value.value = Math.round(100 - (y / rect.height) * 100)
}

// 处理触摸结束
const handleTouchEnd = (e: TouchEvent) => {
  isDragging.value = false
  
  // 移除临时事件监听
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
  document.removeEventListener('touchcancel', handleTouchEnd)
}

// 处理触摸开始事件
const handleTouchStart = (event: TouchEvent) => {
  event.preventDefault()
  if (event.touches && event.touches[0]) {
    // 创建一个模拟的MouseEvent对象
    const touch = event.touches[0]
    const mouseEvent = {
      clientX: touch.clientX,
      clientY: touch.clientY,
      preventDefault: () => {},
      stopPropagation: () => {}
    } as MouseEvent
    
    toggleColorPanel(mouseEvent)
  } else {
    // 如果没有触摸点，直接调用toggleColorPanel不带参数
    toggleColorPanel()
  }
}

// 处理确认按钮触摸事件
const handleConfirmTouch = (event: TouchEvent) => {
  event.preventDefault()
  confirmColor()
}

// 处理取消按钮触摸事件
const handleCancelTouch = (event: TouchEvent) => {
  event.preventDefault()
  closeSelectPanel()
}

// 监听窗口大小变化，更新面板位置
onMounted(() => {
  // 解析初始颜色
  parseRgba(props.color)
  initialColor.value = props.color || 'rgba(0, 0, 0, 1)'
  
  // 确保colorInputValue有初始值
  if (isBlank(colorInputValue.value)) {
    colorInputValue.value = calculateRgba()
  }
  
  // 初始化时就计算好面板位置
  nextTick(() => {
    calculatePanelPosition()
  })
  
  // 添加窗口大小改变事件监听
  const handleResize = () => {
    if (showPanel.value) {
      calculatePanelPosition()
    }
  }
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
  document.removeEventListener('touchcancel', handleTouchEnd)
  
  // 移除resize事件监听
  const handleResize = () => {
    if (showPanel.value) {
      calculatePanelPosition()
    }
  }
  window.removeEventListener('resize', handleResize)
})
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
  border: 1px solid #777777;
  transition: border 0.3s ease, box-shadow 0.3s ease, transform 0.2s ease;
}

.color-box:hover{
  border: 1px dashed v-bind('theme.boxGlowColor');
  box-shadow: 0 0 3px v-bind('theme.boxGlowColor');
}

.color-panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 9999;
  display: block; /* 修改为block以支持绝对定位 */
}

.color-select-panel {
  width: 240px;
  padding: 12px;
  z-index: 10000;
  transform: none; /* 移除transform使面板精确定位 */
}

.color-area {
  margin-bottom: 10px;
}

.color-gradient {
  position: relative;
  width: 100%;
  height: 120px;
  cursor: crosshair;
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
}

.slider-container {
  margin-bottom: 8px;
  display: flex;
  flex-direction: column;
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
  border: none;
  margin: 0;
  padding: 0;
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
  border: none;
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
  border: 1px solid #ddd;
}

.preview-box {
  width: 100%;
  height: 100%;
}

.color-value {
  flex: 1;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border: none;
  outline: none;
  background: transparent;
  padding: 0;
  color: inherit;
  font-family: inherit;
}

.button-area {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-container {
  width: 100%;
  padding-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.confirm-btn, .cancel-btn {
  padding: 4px 12px;
  border: none;
  border-radius: 0;
  cursor: pointer;
  font-size: 12px;
}

.confirm-btn {
  background: #1a73e8;
  color: white;
}

.cancel-btn {
  background: #f1f3f4;
  color: #3c4043;
}

input[type="range"] {
  -webkit-appearance: none;
  outline: none;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 12px;
  height: 16px;
  border-radius: 0;
  background: white;
  border: none;
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
  cursor: pointer;
}

@media (hover: none) and (pointer: coarse) {
  /* 移动设备上的样式调整 */
  .color-select-panel {
    width: 280px; /* 移动设备上稍微宽一点 */
  }
  
  .color-pointer {
    width: 12px;
    height: 12px;
  }
  
  input[type="range"]::-webkit-slider-thumb {
    width: 20px; /* 移动设备上滑块更大，易于触控 */
    height: 20px;
  }
  
  .button-area {
    padding-top: 8px;
  }
}
</style>