import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import CustomerView from '../views/CustomerView.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/customer',
    name: 'Customer',
    component: CustomerView // 直接使用导入的组件
    //component: () => import('../views/CustomerView.vue'),
    //meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/barista',
    name: 'Barista',
    component: () => import('../views/BaristaView.vue'),
    meta: { requiresAuth: true, role: 'BARISTA' }
  },
  {
    path: '/manager',
    name: 'Manager',
    component: () => import('../views/ManagerView.vue'),
    meta: { requiresAuth: true, role: 'MANAGER' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 根据角色重定向到对应页面
  if (to.meta.role && userStore.user?.role !== to.meta.role) {
    const role = userStore.user?.role
    if (role === 'MANAGER') {
      next('/manager')
    } else if (role === 'BARISTA') {
      next('/barista')
    } else {
      next('/customer')
    }
    return
  }

  next()
})

export default router
