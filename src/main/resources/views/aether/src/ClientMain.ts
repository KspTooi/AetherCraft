import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ClientRoot from './ClientRoot.vue'
import router from './router/client'  // 使用client专用路由

// 导入 Bootstrap Icons 样式
import 'bootstrap-icons/font/bootstrap-icons.css'
// 确保字体文件可以被加载
import 'bootstrap-icons/font/fonts/bootstrap-icons.woff'
import 'bootstrap-icons/font/fonts/bootstrap-icons.woff2'

const app = createApp(ClientRoot)

app.use(createPinia())
app.use(router)
app.mount('#app')
