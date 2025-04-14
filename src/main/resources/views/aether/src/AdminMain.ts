import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Admin from './Admin.vue'
import router from './router/admin'  // 使用admin专用路由

// 导入 Bootstrap Icons 样式
import 'bootstrap-icons/font/bootstrap-icons.css'
// 确保字体文件可以被加载
import 'bootstrap-icons/font/fonts/bootstrap-icons.woff'
import 'bootstrap-icons/font/fonts/bootstrap-icons.woff2'

const app = createApp(Admin)

app.use(createPinia())
app.use(router)
app.mount('#app')
