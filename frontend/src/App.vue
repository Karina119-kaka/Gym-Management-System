<template>
  <div class="app">
    <template v-if="!isLoginPage">
      <Navbar v-if="isLoggedIn" />
      <div class="main-container">
        <Sidebar v-if="isLoggedIn" />
        <div class="content">
          <router-view />
        </div>
      </div>
    </template>
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script>
import Navbar from './components/Navbar.vue'
import Sidebar from './components/Sidebar.vue'

export default {
  name: 'App',
  components: {
    Navbar,
    Sidebar
  },
  computed: {
    isLoggedIn() {
      return localStorage.getItem('role') !== null
    },
    isLoginPage() {
      return this.$route.path === '/login'
    }
  },
  mounted() {
    // 检查是否需要认证
    if (this.$route.meta.requiresAuth && !this.isLoggedIn) {
      this.$router.push('/login')
    }
  },
  watch: {
    '$route'() {
      // 当路由变化时，检查是否需要认证
      if (this.$route.meta.requiresAuth && !this.isLoggedIn) {
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
  background-color: #f5f5f5;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-container {
  display: flex;
  flex: 1;
  margin-top: 60px;
}

.content {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
  margin-left: 200px;
}

.purchase-confirm-dialog {
  border-radius: 10px !important;
  padding: 6px 8px 4px !important;
}

.purchase-confirm-dialog .el-message-box__header {
  padding: 14px 16px 8px !important;
}

.purchase-confirm-dialog .el-message-box__title {
  font-size: 22px !important;
  font-weight: 700 !important;
  color: #111827 !important;
}

.purchase-confirm-dialog .el-message-box__content {
  padding: 10px 16px 8px !important;
}

.purchase-confirm-dialog .el-message-box__message p {
  font-size: 18px !important;
  line-height: 1.4 !important;
  color: #374151 !important;
}

.purchase-confirm-dialog .el-message-box__btns {
  padding: 12px 16px 14px !important;
}

.purchase-confirm-dialog .el-button {
  min-width: 96px;
  border-radius: 8px !important;
  font-weight: 700 !important;
}

.purchase-confirm-dialog .el-button--primary {
  background: #2f3542 !important;
  border-color: #2f3542 !important;
}

@media (max-width: 768px) {
  .main-container {
    flex-direction: column;
  }
  
  .content {
    margin-left: 0;
  }
}
</style>
