import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // Vite代理会将/api代理到后端
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 在这里可以添加token等认证信息
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    const message = error.response?.data?.message || '请求失败';
    console.error('API Error:', message);
    return Promise.reject(new Error(message));
  }
);

export default api;