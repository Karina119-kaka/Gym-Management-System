<template>
  <div class="welcome-container">
    <div class="welcome-card">
      <h1>欢迎使用健身房管理系统</h1>
      <p class="welcome-message">
        尊敬的 {{ roleText }}，欢迎登录系统！
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Welcome',
  data() {
    return {
      role: localStorage.getItem('role')
    }
  },
  computed: {
    roleText() {
      return this.role === 'member' ? '会员' : this.role === 'coach' ? '教练' : '管理员'
    }
  }
}
</script>

<style scoped>
.welcome-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.welcome-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 30px;
  width: 100%;
  max-width: 800px;
  text-align: center;
}

.welcome-card h1 {
  color: #4CAF50;
  margin-bottom: 20px;
  font-size: 28px;
}

.welcome-message {
  font-size: 18px;
  color: #666;
}
</style>