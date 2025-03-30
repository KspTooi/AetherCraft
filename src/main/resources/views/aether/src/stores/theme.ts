import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  // 品牌颜色
  const brandColor = ref('rgba(75, 227, 238, 0.62)')
  
  // 主要主题颜色
  const primaryColor = ref('rgba(135, 206, 250, 0.7)')
  
  // 消息悬浮背景色 - 用户
  const messageHoverUser = ref('rgba(61, 138, 168, 0.12)')

  // 消息悬浮背景色 - 模型
  const messageHoverModel = ref('rgba(61, 138, 168, 0.12)')
  
  // 激活状态的颜色
  const activeColor = ref('rgba(94, 203, 245, 0.85)')
  
  // hover状态的颜色
  const primaryHover = ref('rgba(135, 206, 250, 0.2)')
  
  // 按钮背景颜色
  const primaryButton = ref('rgba(61, 138, 168, 0.24)')
  
  // 按钮边框颜色
  const primaryButtonBorder = ref('rgba(135, 206, 250, 0.7)')
  
  // textarea背景颜色
  const textareaColor = ref('rgba(2, 98, 136, 0.1)')
  
  // textarea激活颜色
  const textareaActive = ref('rgba(61, 138, 168, 0.18)')
  
  // textarea边框颜色
  const textareaBorder = ref('rgba(135, 206, 250, 0.22)')
  
  // 导航栏模糊程度
  const navBlur = ref('15px')
  
  // 主要内容模糊程度
  const mainBlur = ref('15px')
  
  // 次要内容模糊程度
  const sideBlur = ref('15px')
  
  // 模态窗口背景颜色
  const modalColor = ref('rgba(2, 98, 136, 0.1)')
  
  // 模态窗口模糊程度
  const modalBlur = ref('10px')
  
  // 模态窗口激活颜色
  const modalActive = ref('rgba(0, 180, 240, 0.85)')
  
  // 三点菜单背景颜色
  const menuColor = ref('rgba(22, 56, 66, 0.51)')
  
  // 三点菜单激活颜色
  const menuActiveColor = ref('rgb(51, 168, 184)')
  
  // 三点菜单模糊程度
  const menuBlur = ref('6px')
  
  // 选择器背景颜色
  const selectorColor = ref('rgba(25, 35, 60, 0.5)')
  
  // 选择器激活颜色
  const selectorActiveColor = ref('rgba(23, 140, 194, 0.5)')
  
  // 选择器边框颜色
  const selectorBorderColor = ref('rgba(79, 172, 254, 0.15)')


  



  
  // 获取品牌颜色
  function getBrandColor() {
    return brandColor.value
  }
  
  // 获取主题颜色
  function getPrimaryColor() {
    return primaryColor.value
  }
  
  // 获取激活颜色
  function getActiveColor() {
    return activeColor.value
  }
  
  // 获取hover颜色
  function getPrimaryHover() {
    return primaryHover.value
  }
  
  // 获取按钮背景颜色
  function getPrimaryButton() {
    return primaryButton.value
  }
  
  // 获取按钮边框颜色
  function getPrimaryButtonBorder() {
    return primaryButtonBorder.value
  }
  
  // 获取textarea背景颜色
  function getTextareaColor() {
    return textareaColor.value
  }
  
  // 获取textarea激活颜色
  function getTextareaActive() {
    return textareaActive.value
  }
  
  // 获取textarea边框颜色
  function getTextareaBorder() {
    return textareaBorder.value
  }
  
  // 获取导航栏模糊程度
  function getNavBlur() {
    return navBlur.value
  }
  
  // 获取主要内容模糊程度
  function getMainBlur() {
    return mainBlur.value
  }
  
  // 获取次要内容模糊程度
  function getSideBlur() {
    return sideBlur.value
  }
  
  // 获取模态窗口背景颜色
  function getModalColor() {
    return modalColor.value
  }
  
  // 获取模态窗口模糊程度
  function getModalBlur() {
    return modalBlur.value
  }
  
  // 获取模态窗口激活颜色
  function getModalActive() {
    return modalActive.value
  }
  
  // 获取三点菜单背景颜色
  function getMenuColor() {
    return menuColor.value
  }
  
  // 获取三点菜单激活颜色
  function getMenuActiveColor() {
    return menuActiveColor.value
  }
  
  // 获取三点菜单模糊程度
  function getMenuBlur() {
    return menuBlur.value
  }
  
  // 获取选择器背景颜色
  function getSelectorColor() {
    return selectorColor.value
  }
  
  // 获取选择器激活颜色
  function getSelectorActiveColor() {
    return selectorActiveColor.value
  }
  
  // 获取选择器边框颜色
  function getSelectorBorderColor() {
    return selectorBorderColor.value
  }
  
  // 获取用户消息悬浮背景色
  function getMessageHoverUser() {
    return messageHoverUser.value
  }
  
  // 获取模型消息悬浮背景色
  function getMessageHoverModel() {
    return messageHoverModel.value
  }
  
  // 设置品牌颜色
  function setBrandColor(color: string) {
    brandColor.value = color
  }
  
  // 设置主题颜色
  function setPrimaryColor(color: string) {
    primaryColor.value = color
  }
  
  // 设置激活颜色
  function setActiveColor(color: string) {
    activeColor.value = color
  }
  
  // 设置hover颜色
  function setPrimaryHover(color: string) {
    primaryHover.value = color
  }
  
  // 设置按钮背景颜色
  function setPrimaryButton(color: string) {
    primaryButton.value = color
  }
  
  // 设置按钮边框颜色
  function setPrimaryButtonBorder(color: string) {
    primaryButtonBorder.value = color
  }
  
  // 设置textarea背景颜色
  function setTextareaColor(color: string) {
    textareaColor.value = color
  }
  
  // 设置textarea激活颜色
  function setTextareaActive(color: string) {
    textareaActive.value = color
  }
  
  // 设置textarea边框颜色
  function setTextareaBorder(color: string) {
    textareaBorder.value = color
  }
  
  // 设置导航栏模糊程度
  function setNavBlur(blur: string) {
    navBlur.value = blur
  }
  
  // 设置主要内容模糊程度
  function setMainBlur(blur: string) {
    mainBlur.value = blur
  }
  
  // 设置次要内容模糊程度
  function setSideBlur(blur: string) {
    sideBlur.value = blur
  }
  
  // 设置模态窗口背景颜色
  function setModalColor(color: string) {
    modalColor.value = color
  }
  
  // 设置模态窗口模糊程度
  function setModalBlur(blur: string) {
    modalBlur.value = blur
  }
  
  // 设置模态窗口激活颜色
  function setModalActive(color: string) {
    modalActive.value = color
  }
  
  // 设置三点菜单背景颜色
  function setMenuColor(color: string) {
    menuColor.value = color
  }
  
  // 设置三点菜单激活颜色
  function setMenuActiveColor(color: string) {
    menuActiveColor.value = color
  }
  
  // 设置三点菜单模糊程度
  function setMenuBlur(blur: string) {
    menuBlur.value = blur
  }
  
  // 设置选择器背景颜色
  function setSelectorColor(color: string) {
    selectorColor.value = color
  }
  
  // 设置选择器激活颜色
  function setSelectorActiveColor(color: string) {
    selectorActiveColor.value = color
  }
  
  // 设置选择器边框颜色
  function setSelectorBorderColor(color: string) {
    selectorBorderColor.value = color
  }

  // 设置用户消息悬浮背景色
  function setMessageHoverUser(color: string) {
    messageHoverUser.value = color
  }

  // 设置模型消息悬浮背景色
  function setMessageHoverModel(color: string) {
    messageHoverModel.value = color
  }

  return { 
    brandColor,
    primaryColor, 
    activeColor, 
    primaryHover,
    primaryButton,
    primaryButtonBorder,
    textareaColor,
    textareaActive,
    textareaBorder,
    navBlur,
    mainBlur,
    sideBlur,
    modalColor,
    modalBlur,
    modalActive,
    menuColor,
    menuActiveColor,
    menuBlur,
    selectorColor,
    selectorActiveColor,
    selectorBorderColor,
    messageHoverUser,
    messageHoverModel,
    getBrandColor,
    getPrimaryColor, 
    getActiveColor, 
    getPrimaryHover,
    getPrimaryButton,
    getPrimaryButtonBorder,
    getTextareaColor,
    getTextareaActive,
    getTextareaBorder,
    getNavBlur,
    getMainBlur,
    getSideBlur,
    getModalColor,
    getModalBlur,
    getModalActive,
    getMenuColor,
    getMenuActiveColor,
    getMenuBlur,
    getSelectorColor,
    getSelectorActiveColor,
    getSelectorBorderColor,
    getMessageHoverUser,
    getMessageHoverModel,
    setBrandColor,
    setPrimaryColor, 
    setActiveColor,
    setPrimaryHover,
    setPrimaryButton,
    setPrimaryButtonBorder,
    setTextareaColor,
    setTextareaActive,
    setTextareaBorder,
    setNavBlur,
    setMainBlur,
    setSideBlur,
    setModalColor,
    setModalBlur,
    setModalActive,
    setMenuColor,
    setMenuActiveColor,
    setMenuBlur,
    setSelectorColor,
    setSelectorActiveColor,
    setSelectorBorderColor,
    setMessageHoverUser,
    setMessageHoverModel
  }
}) 