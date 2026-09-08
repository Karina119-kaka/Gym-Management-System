<template>
  <div class="navbar">
    <div class="navbar-left">
      <h1>健身房管理系统</h1>
    </div>
    <div class="navbar-right">
      <div class="user-dropdown" @click="toggleDropdown">
        <div class="user-circle">{{ displayName }}</div>
        <div class="dropdown-menu" v-if="showDropdown">
          <a href="#" @click.prevent="goToProfile">个人信息</a>
          <a href="#" @click.prevent="logout">退出登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { postForm } from '../utils/api'

export default {
  name: 'Navbar',
  data() {
    return {
      role: localStorage.getItem('role'),
      displayName: '',
      showDropdown: false,
      documentClickHandler: null
    }
  },
  computed: {
    roleLabel() {
      const roleMap = {
        member: '会员',
        coach: '教练',
        admin: '管理员'
      }
      return roleMap[this.role] || '用户'
    }
  },
  methods: {
    refreshDisplayName() {
      const userData = JSON.parse(localStorage.getItem('user')) || {}
      this.displayName = userData.memberName || userData.coachName || userData.adminName || this.roleLabel
    },
    async logout() {
      try {
        await postForm('/api/login/logout')
      } finally {
        localStorage.clear()
        this.$router.push('/login')
      }
    },
    goToProfile() {
      this.$router.push(`/${this.role}/profile`)
      this.showDropdown = false
    },
    toggleDropdown() {
      this.showDropdown = !this.showDropdown
    }
  },
  mounted() {
    this.refreshDisplayName()
    window.addEventListener('user-updated', this.refreshDisplayName)
    this.documentClickHandler = (e) => {
      if (!this.$el.contains(e.target)) {
        this.showDropdown = false
      }
    }
    document.addEventListener('click', this.documentClickHandler)
  },
  beforeUnmount() {
    window.removeEventListener('user-updated', this.refreshDisplayName)
    if (this.documentClickHandler) {
      document.removeEventListener('click', this.documentClickHandler)
      this.documentClickHandler = null
    }
  }
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: #3f4652;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.16);
  z-index: 1000;
}
.navbar-left h1 {
  font-size: 20px;
  font-weight: 800;
  margin: 0;
}
.navbar-right {
  position: relative;
}
.user-dropdown {
  position: relative;
  cursor: pointer;
}
.user-circle {
  min-width: 58px;
  height: 36px;
  padding: 0 12px;
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.12);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.24);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 13px;
  font-weight: 700;
}
.dropdown-menu {
  position: absolute;
  top: 46px;
  right: 0;
  width: 132px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 14px 32px rgba(31, 41, 55, 0.18);
  overflow: hidden;
}
.dropdown-menu a {
  display: block;
  padding: 11px 14px;
  color: #374151;
  text-decoration: none;
  font-size: 14px;
}
.dropdown-menu a:hover {
  background-color: #f3f4f6;
  color: #111827;
}
.dropdown-menu a:first-child {
  border-bottom: 1px solid #f1f5f9;
}
</style>
