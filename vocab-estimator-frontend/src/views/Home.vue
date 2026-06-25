<template>
  <div class="home">
    <el-card class="welcome-card">
      <div class="welcome-content">
        <h1>英文词汇量估算工具</h1>
        <p>基于词频加权与分层校准算法,支持在线测试、批量词表估算、语料分析与四六级成绩相关性统计。</p>
        <el-divider />
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in features" :key="item.title">
            <el-card shadow="hover" class="feature-card" @click="$router.push(item.route)">
              <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header><span>快速开始</span></template>
      <div style="text-align: center; padding: 10px 0;">
        <div v-if="currentUser">
          <el-tag type="success" size="large" style="margin-bottom: 15px;">
            当前用户: {{ currentUser.studentCode }}
          </el-tag>
          <br/>
          <el-button type="primary" size="large" @click="$router.push('/online-test')" style="width: 200px;">
            开始词汇测试
          </el-button>
        </div>
        <div v-else>
          <el-button type="primary" size="large" @click="autoLogin" :loading="loggingIn">
            进入测试
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header><span>系统概览</span></template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="s in stats" :key="s.label">
          <el-statistic :title="s.label" :value="s.value" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOverviewStats } from '@/api/statsApi'
import { registerUser } from '@/api/userApi'
import { ElMessage } from 'element-plus'

const currentUser = ref(null)
const loggingIn = ref(false)
const stats = ref([
  { label: '总用户数', value: '--' },
  { label: '总测试数', value: '--' },
  { label: '平均词汇量', value: '--' },
  { label: '四级相关性', value: '--' }
])

const features = [
  { title: '在线测试', desc: '随机词汇测试,即时估算词汇量', icon: 'Edit', route: '/online-test', color: '#409EFF' },
  { title: '批处理', desc: '上传词表进行批量估算', icon: 'Upload', route: '/batch-manage', color: '#67C23A' },
  { title: '语料分析', desc: '分析C/F/P/K四类学员语料', icon: 'DataAnalysis', route: '/corpus-analysis', color: '#E6A23C' },
  { title: '统计报表', desc: '四六级成绩与词汇量相关性', icon: 'TrendCharts', route: '/stats', color: '#F56C6C' }
]

async function autoLogin() {
  loggingIn.value = true
  try {
    const code = 'Guest' + Date.now().toString().slice(-6)
    const res = await registerUser({ studentCode: code, nameAlias: '' })
    if (res.code === 200) {
      currentUser.value = res.data
      localStorage.setItem('currentUser', JSON.stringify(res.data))
    }
  } catch (e) {
    ElMessage.error('自动登录失败,请检查后端是否启动')
  } finally {
    loggingIn.value = false
  }
}

onMounted(async () => {
  const saved = localStorage.getItem('currentUser')
  if (saved) {
    currentUser.value = JSON.parse(saved)
  } else {
    await autoLogin()
  }
  try {
    const res = await getOverviewStats()
    if (res.code === 200) {
      stats.value = [
        { label: '总用户数', value: res.data.userCount },
        { label: '总测试数', value: res.data.testCount },
        { label: '平均词汇量', value: res.data.avgVocab },
        { label: '四级相关性', value: res.data.cet4Correlation }
      ]
    }
  } catch (e) { console.log('Stats unavailable') }
})
</script>

<style scoped>
.welcome-card { text-align: center; }
.welcome-content h1 { font-size: 28px; color: #303133; margin-bottom: 10px; }
.welcome-content p { color: #606266; font-size: 14px; max-width: 700px; margin: 0 auto 20px; line-height: 1.8; }
.feature-card { cursor: pointer; text-align: center; min-height: 150px; transition: transform 0.2s; }
.feature-card:hover { transform: translateY(-5px); }
.feature-card h3 { margin: 10px 0 5px; font-size: 16px; }
.feature-card p { font-size: 12px; color: #909399; }
</style>