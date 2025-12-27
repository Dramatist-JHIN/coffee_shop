import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 需引入 path 模块

export default defineConfig({
  plugins: [vue()],
  resolve: {
    // 配置路径别名
    alias: {
      '@': path.resolve(__dirname, 'src') // 关键：把 @ 映射到项目根目录下的 src 文件夹
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
