<template>
  <div class="glow-check-box-wrapper" 
       @click="toggleCheck" 
       @mousedown="isActive = true" 
       @mouseup="isActive = false" 
       @mouseleave="isActive = false"
       @mouseover="isHovered = true"
       @mouseout="isHovered = false">
    <div class="glow-check-box" :class="{ 'active': isActive, 'hovered': isHovered }">
      <i v-if="modelValue" class="bi bi-x"></i>
    </div>
    <div class="content-slot">
      <slot></slot>
    </div>
    <div v-if="tip && isHovered" class="glow-tip">{{ tip }}</div>
  </div>
</template>

<script setup lang="ts">
import {inject, ref} from "vue";
import {defaultTheme, GLOW_THEME_INJECTION_KEY, type GlowThemeColors} from "@/components/glow-ui/GlowTheme.ts";

const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  tip: {
    type: String,
    default: ''
  }
})

const isActive = ref(false)
const isHovered = ref(false)

const emit = defineEmits(['update:modelValue'])

const toggleCheck = () => {
  emit('update:modelValue', !props.modelValue)
}
</script>

<style scoped>
.glow-check-box-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  position: relative;
}

.glow-check-box {
  width: 14px;
  height: 14px;
  border: 1px solid v-bind('theme.boxBorderColor');
  background-color: transparent;
  margin-right: 8px;
  position: relative;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 0 transparent;
}

.glow-check-box:hover,
.glow-check-box.hovered {
  border-color: v-bind('theme.mainBorderColor');
  box-shadow: 0 0 v-bind('theme.boxBlurHover + "px"') v-bind('theme.boxGlowColor');
}

.glow-check-box.active {
  background-color: v-bind('theme.mainColorActive');
  border-color: v-bind('theme.boxGlowColor');
}

.glow-check-box i {
  color: v-bind('theme.boxGlowColor');
  font-size: 15px;
  font-weight: bold;
  line-height: 1;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.content-slot {
  color: v-bind('theme.boxTextColor');
  font-size: 14px;
}

.glow-tip {
  position: absolute;
  top: calc(100% + 5px);
  left: 0;
  background-color: rgba(0, 0, 0, 0.726);
  color: v-bind('theme.boxTextColor');
  padding: 5px 8px;
  border-radius: 0;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  pointer-events: none;
}
</style>