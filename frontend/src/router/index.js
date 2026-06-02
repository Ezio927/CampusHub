import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
  { path: '/demands/publish', name: 'publish', component: () => import('@/views/PublishDemandView.vue'), meta: { auth: true } },
  { path: '/demands/:id', name: 'demand-detail', component: () => import('@/views/DemandDetailView.vue') },
  { path: '/orders', name: 'orders', component: () => import('@/views/MyOrdersView.vue'), meta: { auth: true } },
  { path: '/orders/:id', name: 'order-detail', component: () => import('@/views/OrderDetailView.vue'), meta: { auth: true } },
  { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { auth: true } },
  { path: '/reviews/:userId', name: 'reviews', component: () => import('@/views/ReviewsView.vue') },
  { path: '/messages', name: 'messages', component: () => import('@/views/MessagesView.vue'), meta: { auth: true } },
  { path: '/chat/:id', name: 'chat', component: () => import('@/views/ChatView.vue'), meta: { auth: true } },
  { path: '/admin', name: 'admin', component: () => import('@/views/admin/DashboardView.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/users', name: 'admin-users', component: () => import('@/views/admin/UserManageView.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/orders', name: 'admin-orders', component: () => import('@/views/admin/OrderManageView.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/certifications', name: 'admin-certs', component: () => import('@/views/admin/CertReviewView.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/arbitrations', name: 'admin-arbs', component: () => import('@/views/admin/ArbitrationView.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/message-reports', name: 'admin-message-reports', component: () => import('@/views/admin/MessageReportView.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/credit-appeals', name: 'admin-credit-appeals', component: () => import('@/views/admin/CreditAppealView.vue'), meta: { auth: true, admin: true } },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) return '/login'
})

export default router
