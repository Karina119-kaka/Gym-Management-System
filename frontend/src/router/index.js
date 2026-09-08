import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Welcome from '../views/Welcome.vue'
import MemberHome from '../views/member/Home.vue'
import MemberProfile from '../views/member/Profile.vue'
import CoursePurchase from '../views/member/CoursePurchase.vue'
import MemberCourseDetail from '../views/member/CourseDetail.vue'
import MemberClockIn from '../views/member/ClockIn.vue'
import MemberCourseReserve from '../views/member/CourseReserve.vue'
import MemberMySchedule from '../views/member/MySchedule.vue'
import CoachHome from '../views/coach/Home.vue'
import CoachProfile from '../views/coach/Profile.vue'
import CoachCoursePublish from '../views/coach/CoursePublish.vue'
import CoachOrderReview from '../views/coach/OrderReview.vue'
import CoachAttendance from '../views/coach/Attendance.vue'
import AdminHome from '../views/admin/Home.vue'
import AdminProfile from '../views/admin/Profile.vue'
import MemberManage from '../views/admin/MemberManage.vue'
import CoachManage from '../views/admin/CoachManage.vue'
import CourseManage from '../views/admin/CourseManage.vue'
import AdminOrderManage from '../views/admin/OrderManage.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/welcome',
    name: 'Welcome',
    component: Welcome,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/login'
  },
  {
    path: '/member',
    name: 'MemberHome',
    component: MemberHome,
    meta: {
      requiresAuth: true,
      role: 'member'
    }
  },
  {
    path: '/member/profile',
    name: 'MemberProfile',
    component: MemberProfile,
    meta: {
      requiresAuth: true,
      role: 'member'
    }
  },
  {
    path: '/member/course',
    name: 'CoursePurchase',
    component: CoursePurchase,
    meta: {
      requiresAuth: true,
      role: 'member'
    }
  },
  {
    path: '/member/course/:id',
    name: 'MemberCourseDetail',
    component: MemberCourseDetail,
    meta: {
      requiresAuth: true,
      role: 'member'
    }
  },
  {
    path: '/member/clockin',
    name: 'MemberClockIn',
    component: MemberClockIn,
    meta: {
      requiresAuth: true,
      role: 'member'
    }
  },
  {
    path: '/member/reserve',
    name: 'MemberCourseReserve',
    component: MemberCourseReserve,
    meta: {
      requiresAuth: true,
      role: 'member'
    }
  },
  {
    path: '/member/schedule',
    name: 'MemberMySchedule',
    component: MemberMySchedule,
    meta: {
      requiresAuth: true,
      role: 'member'
    }
  },
  {
    path: '/coach',
    name: 'CoachHome',
    component: CoachHome,
    meta: {
      requiresAuth: true,
      role: 'coach'
    }
  },
  {
    path: '/coach/profile',
    name: 'CoachProfile',
    component: CoachProfile,
    meta: {
      requiresAuth: true,
      role: 'coach'
    }
  },
  {
    path: '/coach/course',
    name: 'CoachCoursePublish',
    component: CoachCoursePublish,
    meta: {
      requiresAuth: true,
      role: 'coach'
    }
  },
  {
    path: '/coach/order',
    name: 'CoachOrderReview',
    component: CoachOrderReview,
    meta: {
      requiresAuth: true,
      role: 'coach'
    }
  },
  {
    path: '/coach/attendance',
    name: 'CoachAttendance',
    component: CoachAttendance,
    meta: {
      requiresAuth: true,
      role: 'coach'
    }
  },
  {
    path: '/admin',
    name: 'AdminHome',
    component: AdminHome,
    meta: {
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/profile',
    name: 'AdminProfile',
    component: AdminProfile,
    meta: {
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/member',
    name: 'MemberManage',
    component: MemberManage,
    meta: {
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/coach',
    name: 'CoachManage',
    component: CoachManage,
    meta: {
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/course',
    name: 'CourseManage',
    component: CourseManage,
    meta: {
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/order',
    name: 'AdminOrderManage',
    component: AdminOrderManage,
    meta: {
      requiresAuth: true,
      role: 'admin'
    }
  },
  
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const role = localStorage.getItem('role')
    if (!role) {
      next('/login')
    } else if (to.meta.role && role !== to.meta.role) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
