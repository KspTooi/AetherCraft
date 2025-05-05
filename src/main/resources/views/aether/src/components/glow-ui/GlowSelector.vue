<template>
  <div class="glow-select-wrapper" v-click-outside="closeDropdown">
    <div v-if="title" class="select-title">{{ title }}</div>
    <div class="select-container">
      <!-- Selector Header -->
      <div class="selector-header" @click="toggleDropdown" :class="{ 'disabled': disabled }">
        <div class="selected-value">
          <span :class="{ 'placeholder': !selectedLabel && placeholder }">
            {{ selectedLabel || placeholder || '&nbsp;' }} 
          </span>
        </div>
        <div class="select-arrow">
           <i class="bi bi-chevron-down" :class="{ 'rotated': isOpen }"></i>
        </div>
      </div>

      <!-- Dropdown Menu -->
      <transition name="dropdown">
        <div class="dropdown-menu" v-show="isOpen && !disabled">
           <div 
             class="dropdown-item"
             v-for="(option, index) in options"
             :key="index"
             :class="{ 'active': modelValue === getValue(option) }"
             @click="selectOption(option)"
           >
             {{ getLabel(option) }}
           </div>
           <div v-if="!options || options.length === 0" class="dropdown-item disabled">
             无可用选项
           </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject } from 'vue';
import { defaultTheme, GLOW_THEME_INJECTION_KEY, type GlowThemeColors } from "@/components/glow-ui/GlowTheme.ts";

// Inject theme
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  modelValue: {
    type: [String, Number],
    default: ''
  },
  options: {
    type: Array as () => any[], // Array of objects or simple values
    default: () => []
  },
  valueKey: {
    type: String,
    default: 'value' // Key for option value if options are objects
  },
  labelKey: {
    type: String,
    default: 'label' // Key for option label if options are objects
  },
  placeholder: {
    type: String,
    default: '' // Optional placeholder text
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const isOpen = ref(false);

const toggleDropdown = () => {
  if (!props.disabled) {
    isOpen.value = !isOpen.value;
  }
};

const closeDropdown = () => {
  isOpen.value = false;
};

const selectOption = (option: any) => {
  const value = getValue(option);
  emit('update:modelValue', value);
  emit('change', value);
  closeDropdown();
};

// Compute the label of the selected value
const selectedLabel = computed(() => {
  const selectedOption = props.options.find(opt => getValue(opt) === props.modelValue);
  return selectedOption ? getLabel(selectedOption) : ''
});

// Helper function to get the value from an option
const getValue = (option: any): string | number => {
  if (typeof option === 'object' && option !== null && props.valueKey in option) {
    return option[props.valueKey];
  }
  return option; // Return the option itself if it's a simple value
};

// Helper function to get the label from an option
const getLabel = (option: any): string => {
  if (typeof option === 'object' && option !== null && props.labelKey in option) {
    return option[props.labelKey];
  }
  return String(option); // Return string representation if it's a simple value
};

// Click outside directive definition (copied from ModelSelector)
const vClickOutside = {
  mounted(el: any, binding: any) {
    el._clickOutside = (event: any) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event);
      }
    };
    document.addEventListener('click', el._clickOutside);
  },
  unmounted(el: any) {
    document.removeEventListener('click', el._clickOutside);
  }
};

</script>

<style scoped>
.glow-select-wrapper {
  position: relative;
  width: 100%;
  margin-bottom: 6px;
}

.select-title {
  font-family: 'Chakra Petch', sans-serif;
  font-size: 12px;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 4px;
  opacity: 0.8;
  font-weight: 500;
}

.select-container {
  position: relative;
  width: 100%;
}

/* --- Selector Header --- */
.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 6px 10px;
  font-size: 13px;
  background-color: v-bind('theme.boxSecondColor');
  color: v-bind('theme.boxTextColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  transition: all 0.3s ease;
  outline: none;
  box-sizing: border-box;
  cursor: pointer;
  line-height: 1.2;
}

.selector-header:not(.disabled):hover {
  background-color: v-bind('theme.boxColorActive'); /* Use active color on hover */
  border-color: v-bind('theme.boxSecondColorHover');
}

.selector-header.disabled {
  background-color: v-bind('theme.disabledColor');
  border-color: v-bind('theme.disabledBorderColor');
  color: v-bind('theme.boxTextColorNoActive');
  cursor: not-allowed;
  opacity: 0.6;
}

.selected-value {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selected-value .placeholder {
  color: v-bind('theme.boxTextColorNoActive');
  opacity: 0.7;
}

.select-arrow {
  margin-left: 5px;
  color: v-bind('theme.boxTextColorNoActive');
  transition: transform 0.3s ease, color 0.3s ease;
}

.selector-header:not(.disabled):hover .select-arrow,
.selector-header:not(.disabled).active .select-arrow { /* Assuming .active class might be added when open */
  color: v-bind('theme.boxTextColor');
}

.select-arrow i {
  font-size: 14px;
  display: block; /* Prevents small layout shifts */
}

.select-arrow i.rotated {
  transform: rotate(180deg);
}

.selector-header.disabled .select-arrow {
   color: v-bind('theme.boxTextColorNoActive');
   opacity: 0.6;
}


/* --- Dropdown Menu --- */
.dropdown-menu {
  position: absolute;
  top: calc(100% + 2px);
  left: 0;
  right: 0;
  background: #000000; /* Black background as requested */
  border: 1px solid v-bind('theme.boxBorderColor');
  max-height: 200px; /* Adjust max height as needed */
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5); /* Darker shadow */
}

.dropdown-item {
  padding: 7px 10px;
  color: #cccccc; /* Lighter gray for text on black */
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.1); /* Subtle white hover */
  color: #ffffff;
}

.dropdown-item.active {
  background: v-bind('theme.mainColor');
  color: v-bind('theme.mainTextColor');
  font-weight: 500;
}

.dropdown-item.disabled {
  color: v-bind('theme.boxTextColorNoActive');
  cursor: default;
  opacity: 0.6;
   background: transparent !important; /* Ensure no hover effect */
}

/* Custom scrollbar for dropdown */
.dropdown-menu::-webkit-scrollbar {
  width: 4px;
}

.dropdown-menu::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.dropdown-menu::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
  border-radius: 2px;
}

.dropdown-menu::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColorHover');
}

/* Dropdown animation */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.25s ease-out;
  transform-origin: top;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: scaleY(0.9) translateY(-5px);
}

</style>