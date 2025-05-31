<template>

</template>

<script setup lang="ts">

import {reactive, ref} from "vue";


//模态框模式
const mode = ref<"insert" | "update">("insert")

const query = reactive<any>({
  keyword: null,
  page: 1,
  pageSize: 10
})

const list = ref<any[]>([])
const total = ref(0)

//表单数据
const details = reactive<any>({
  code: "",
  createTime: "",
  enabled: 0,
  id: "",
  intelligence: 0,
  name: "",
  scale: 0,
  series: "",
  speed: 0,
  thinking: 0,
  updateTime: ""
})


const loadModelSeriesList = async () => {

}

const resetQuery = async ()=>{

}

const resetForm = async ()=>{

}


const openInsertModal = async () => {

}

const openUpdateModal = async (row : any) => {

}

const save = () => {

}

const remove = async (row : any) => {


}



</script>

<style scoped>

</style>