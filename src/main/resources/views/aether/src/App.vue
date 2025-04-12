<template>

  <GlowTheme @onThemeUpdate="onThemeUpdate" :theme="currentTheme">
    <ClientFrame>
      <RouterView />
    </ClientFrame>
  </GlowTheme>

</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlowTheme from "@/components/glow-ui/GlowTheme.vue";
import ClientFrame from "@/components/glow-client/ClientFrame.vue";
import {onMounted, reactive} from "vue";
import {defaultTheme, type GlowThemeColors} from "@/components/glow-ui/GlowTheme.ts";
import type Result from "@/entity/Result";
import type { GetActiveThemeVo } from "@/entity/vo/GetActiveThemeVo";
import type ThemeValuesVo from "@/entity/vo/ThemeValuesVo";
import axios from "axios";

const currentTheme = reactive<GlowThemeColors>(defaultTheme)

const onThemeUpdate = ()=>{
  pullAndApplyActiveTheme()
}

const pullAndApplyActiveTheme = async () => {
  try {
    const response = await axios.post<Result<GetActiveThemeVo>>("/customize/theme/getActiveTheme");
    
    if (response.data.code === 0 && response.data.data) {
      const themeData = response.data.data;
      
      if (themeData.themeValues) {
        // 将后端返回的主题值应用到当前主题
        const themeValues = themeData.themeValues as unknown as Record<string, string>;
        Object.entries(themeValues).forEach(([key, value]) => {
          if (key in currentTheme && value) {
            // @ts-ignore 确保关键字可以被索引
            currentTheme[key] = value;
          }
        });
      }
    }
  } catch (error) {
    console.error("获取主题数据失败:", error);
  }
}

onMounted(()=>{
  pullAndApplyActiveTheme()
})

</script>

<style>

/* 重置 body 边距 */
body {
  margin: 0;
  padding: 0;
  box-sizing: border-box; /* 推荐添加，使得 padding 和 border 不会增加元素总宽度 */
}

/* 可选：重置 html 元素的边距和内边距 */
html {
  margin: 0;
  padding: 0;
}

/* 如果需要，可以把之前 #app 的一些基础样式加回来，但移除宽度/边距限制 */
#app {
  font-weight: normal;
  /* 其他需要的全局字体、颜色等样式 */
}

/* Bootstrap Icons 字体配置 */
@font-face {
  font-family: "bootstrap-icons";
  src: url("bootstrap-icons/font/fonts/bootstrap-icons.woff2") format("woff2"),
       url("bootstrap-icons/font/fonts/bootstrap-icons.woff") format("woff");
}

.bi::before {
  font-family: bootstrap-icons !important;
  font-style: normal;
  font-weight: normal !important;
  font-variant: normal;
  text-transform: none;
  line-height: 1;
  vertical-align: -0.125em;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

</style>
