import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
  { path: '/demands/:id', name: 'demand-detail', component: () => import('@/views/DemandDetailView.vue') },
  { path: '/demands/publish', name: 'publish-demand', component: () => import('@/views/PublishDemandView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'orders', component: () => import('@/views/MyOrdersView.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:id', name: 'order-detail', component: () => import('@/views/OrderDetailView.vue'), meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },
  { path: '/reviews/user/:userId', name: 'reviews', component: () => import('@/views/ReviewsView.vue') },
  { path: '/chat/:id', name: 'chat', component: () => import('@/views/ChatView.vue'), meta: { requiresAuth: true } },
  { path: '/admin', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', name: 'admin-users', component: () => import('@/views/admin/UserManageView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/certifications', name: 'admin-certs', component: () => import('@/views/admin/CertReviewView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/arbitrations', name: 'admin-arbs', component: () => import('@/views/admin/ArbitrationView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }
  next()
})

export default router
