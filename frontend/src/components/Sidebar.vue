<template>
  <aside class="sidebar">
    <ul class="menu">
      <li v-if="role === 'member'" class="menu-item"><a href="#" @click.prevent="goToHome">首页</a></li>
      <li v-if="role === 'member'" class="menu-item"><a href="#" @click.prevent="goToProfile">个人信息</a></li>
      <li v-if="role === 'member'" class="menu-item"><a href="#" @click.prevent="goToCoursePurchase">课程购买</a></li>
      <li v-if="role === 'member'" class="menu-item"><a href="#" @click.prevent="goToCourseReserve">课程预约</a></li>
      <li v-if="role === 'member'" class="menu-item"><a href="#" @click.prevent="goToMySchedule">我的预约</a></li>
      <li v-if="role === 'member'" class="menu-item"><a href="#" @click.prevent="goToClockIn">健身打卡</a></li>

      <li v-if="role === 'coach'" class="menu-item"><a href="#" @click.prevent="goToHome">首页</a></li>
      <li v-if="role === 'coach'" class="menu-item"><a href="#" @click.prevent="goToProfile">个人信息</a></li>
      <li v-if="role === 'coach'" class="menu-item"><a href="#" @click.prevent="goToCoachCourse">课程管理</a></li>
      <li v-if="role === 'coach'" class="menu-item"><a href="#" @click.prevent="goToCoachOrder">预约查看</a></li>
      <li v-if="role === 'coach'" class="menu-item"><a href="#" @click.prevent="goToCoachAttendance">课程考勤</a></li>

      <li v-if="role === 'admin'" class="menu-item"><a href="#" @click.prevent="goToHome">首页</a></li>
      <li v-if="role === 'admin'" class="menu-item"><a href="#" @click.prevent="goToProfile">个人信息</a></li>
      <li v-if="role === 'admin'" class="menu-item"><a href="#" @click.prevent="goToMemberManage">会员管理</a></li>
      <li v-if="role === 'admin'" class="menu-item"><a href="#" @click.prevent="goToCoachManage">教练管理</a></li>
      <li v-if="role === 'admin'" class="menu-item"><a href="#" @click.prevent="goToCourseManage">课程管理</a></li>
      <li v-if="role === 'admin'" class="menu-item"><a href="#" @click.prevent="goToOrderManage">订单管理</a></li>
    </ul>
  </aside>
</template>

<script>
export default {
  name: 'Sidebar',
  data() {
    return { role: localStorage.getItem('role') }
  },
  methods: {
    goToHome() {
      const roleHomeMap = { member: '/member', coach: '/coach', admin: '/admin' }
      this.$router.push(roleHomeMap[this.role] || '/login')
    },
    goToProfile() { this.$router.push(`/${this.role}/profile`) },
    goToCoursePurchase() { this.$router.push('/member/course') },
    goToCourseReserve() { this.$router.push('/member/reserve') },
    goToMySchedule() { this.$router.push('/member/schedule') },
    goToClockIn() { this.$router.push('/member/clockin') },
    goToCoachCourse() { this.$router.push('/coach/course') },
    goToCoachOrder() { this.$router.push('/coach/order') },
    goToCoachAttendance() { this.$router.push('/coach/attendance') },
    goToMemberManage() { this.$router.push('/admin/member') },
    goToCoachManage() { this.$router.push('/admin/coach') },
    goToCourseManage() { this.$router.push('/admin/course') },
    goToOrderManage() { this.$router.push('/admin/order') }
  }
}
</script>

<style scoped>
.sidebar { position: fixed; left: 0; top: 60px; width: 200px; height: calc(100vh - 60px); background-color: #eceff3; border-right: 1px solid #d8dde5; z-index: 999; }
.menu { list-style: none; margin: 0; padding: 14px 10px; }
.menu-item { margin: 0 0 6px; }
.menu-item a { display: block; padding: 12px 14px; border-radius: 6px; color: #374151; text-decoration: none; font-size: 15px; font-weight: 700; transition: background-color 0.2s, color 0.2s, transform 0.2s; }
.menu-item a:hover { background-color: #ffffff; color: #111827; transform: translateX(2px); }
.menu-item a:active { background-color: #dde3ea; }
@media (max-width: 768px) {
  .sidebar { width: 100%; height: auto; position: relative; top: 0; border-right: none; border-bottom: 1px solid #d8dde5; }
  .menu { display: flex; flex-direction: row; overflow-x: auto; padding: 8px; }
  .menu-item { margin: 0 6px 0 0; white-space: nowrap; }
  .menu-item a { padding: 9px 13px; }
}
</style>
