import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    },
    server: {
        port: 5173,
        proxy: {
            // 开发期将 /api 代理到本地后端（默认 8080）
            '/api': 'http://localhost:8080'
        }
    }
});
