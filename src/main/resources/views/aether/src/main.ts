import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 导入 Bootstrap Icons 样式
import 'bootstrap-icons/font/bootstrap-icons.css'
// 确保字体文件可以被加载
import 'bootstrap-icons/font/fonts/bootstrap-icons.woff'
import 'bootstrap-icons/font/fonts/bootstrap-icons.woff2'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.mount('#app')
