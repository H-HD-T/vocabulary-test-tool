<template>
  <div class="online-test">
    <!-- Start screen -->
    <el-card v-if="!testing && !testResult">
      <div class="start-screen">
        <div class="start-icon">
          <el-icon :size="80" color="#409EFF"><EditPen /></el-icon>
        </div>
        <h2>词汇量测试</h2>
        <p class="start-desc">共 40 题,从 K/P/F/C 四个难度等级随机抽取<br/>选择单词对应的正确释义,测出你的词汇量</p>
        <div class="start-info">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-card">
                <div class="num">40</div>
                <div class="label">题数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-card">
                <div class="num">K/P/F/C</div>
                <div class="label">四级难度</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-card">
                <div class="num">--</div>
                <div class="label">你的词汇量</div>
              </div>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" size="large" class="start-btn" @click="startTest" :loading="loading">
          开始测词汇
        </el-button>
      </div>
    </el-card>

    <!-- Test screen -->
    <el-card v-if="testing" class="test-card">
      <div class="test-header">
        <div class="progress-text">{{ answeredCount }} / {{ totalWords }}</div>
        <div class="progress-bar-wrapper">
          <el-progress :percentage="progressPercent" :show-text="false" :stroke-width="6" />
        </div>
        <div class="level-tag" v-if="currentWord">
          <el-tag :type="levelTagType" size="small">{{ currentWord.difficulty }}</el-tag>
        </div>
      </div>
      <div class="word-section">
        <div class="word-number">第 {{ currentIndex + 1 }} 题</div>
        <div class="word-text">{{ currentWord?.word }}</div>
      </div>
      <div class="options-grid">
        <div
          v-for="(opt, idx) in options"
          :key="idx"
          class="option-item"
          :class="{
            correct: selectedOption !== null && opt.isCorrect,
            wrong: selectedOption === idx && !opt.isCorrect,
            disabled: selectedOption !== null
          }"
          @click="selectOption(idx)"
        >
          <span class="option-label">{{ optionLabels[idx] }}</span>
          <span class="option-text">{{ opt.label }}</span>
          <span class="option-icon" v-if="selectedOption !== null && opt.isCorrect">
            <el-icon color="#67C23A"><Check /></el-icon>
          </span>
          <span class="option-icon" v-if="selectedOption === idx && !opt.isCorrect">
            <el-icon color="#F56C6C"><Close /></el-icon>
          </span>
        </div>
      </div>
    </el-card>

    <!-- Result screen -->
    <el-card v-if="testResult && !testing" class="result-card">
      <div class="result-section">
        <div class="result-badge" :class="resultLevel">
          <el-icon :size="48"><Medal /></el-icon>
        </div>
        <h2>测试完成!</h2>
        <div class="result-score">
          <div class="known-count">{{ testResult.knownCount }}<span class="unit">/{{ testResult.totalWords }}</span></div>
          <div class="score-label">答对题数</div>
        </div>
        <el-divider />
        <div class="estimate-section">
          <div class="estimate-item">
            <div class="estimate-value">{{ testResult.estimate }}</div>
            <div class="estimate-label">估算词汇量</div>
          </div>
          <div class="estimate-item">
            <div class="estimate-value">{{ testResult.minRange }} - {{ testResult.maxRange }}</div>
            <div class="estimate-label">估算范围</div>
          </div>
          <div class="estimate-item">
            <div class="estimate-value">{{ testResult.confidence?.toFixed(1) }}%</div>
            <div class="estimate-label">置信度</div>
          </div>
        </div>
        <div class="result-actions">
          <el-button @click="resetTest">再测一次</el-button>
          <el-button type="primary" @click="showChart = !showChart">查看历史</el-button>
        </div>
      </div>
    </el-card>

    <!-- History chart -->
    <el-card style="margin-top: 20px;" v-if="historyRecords.length > 0">
      <template #header>
        <span>历史测试记录</span>
        <el-button size="small" style="float: right;" @click="showChart = !showChart">
          {{ showChart ? '隐藏' : '显示' }}图表
        </el-button>
      </template>
      <div ref="chartRef" style="height: 280px;" v-show="showChart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { generatePaper, submitTest, getTestHistory } from '@/api/testApi'
import * as echarts from 'echarts'

const currentUser = ref(null)
const testing = ref(false)
const loading = ref(false)
const testResult = ref(null)
const words = ref([])
const currentIndex = ref(0)
const options = ref([])
const selectedOption = ref(null)
const optionLabels = ['A', 'B', 'C', 'D']
const historyRecords = ref([])
const chartRef = ref(null)
const showChart = ref(false)
let chartInstance = null

// Track answers: { wordId, word, known (true=答对), answered (true=已作答) }
const answerRecords = ref([])

const currentWord = computed(() => words.value[currentIndex.value])
const totalWords = computed(() => words.value.length)
const answeredCount = computed(() => {
  // Count of words that have been answered (regardless of correct/wrong)
  let answered = 0
  for (const r of answerRecords.value) {
    if (r.answered) answered++
  }
  return answered
})

const progressPercent = computed(() => {
  if (totalWords.value === 0) return 0
  return Math.round((answeredCount.value / totalWords.value) * 100)
})

const resultLevel = computed(() => {
  if (!testResult.value) return ''
  const e = testResult.value.estimate
  if (e >= 6000) return 'level-c'
  if (e >= 4000) return 'level-f'
  if (e >= 2000) return 'level-p'
  return 'level-k'
})

const levelTagType = computed(() => {
  const map = { K: 'info', P: 'primary', F: 'warning', C: 'danger' }
  return map[currentWord.value?.difficulty] || 'info'
})

async function startTest() {
  if (!currentUser.value) {
    ElMessage.warning('请先进入首页自动登录')
    return
  }
  loading.value = true
  testResult.value = null
  words.value = []
  currentIndex.value = 0
  answerRecords.value = []
  try {
    const res = await generatePaper(currentUser.value.id, 40)
    if (res.code === 200) {
      words.value = res.data.words || []
      if (words.value.length === 0) {
        ElMessage.error('题库为空,无法生成试卷')
        loading.value = false
        return
      }
      // Initialize answer records for all words (all un-answered initially)
      answerRecords.value = words.value.map(w => ({
        wordId: w.id,
        word: w.word,
        known: false,
        answered: false
      }))
      currentIndex.value = 0
      selectedOption.value = null
      testing.value = true
      buildOptions()
    } else {
      ElMessage.error('生成试卷失败: ' + (res.message || '未知错误'))
    }
  } catch (e) {
    ElMessage.error('生成试卷失败,请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

function buildOptions() {
  const word = currentWord.value
  if (!word) return
  const correct = { label: word.definition || word.word, isCorrect: true }
  const distractors = getDistractors(word)
  const allOptions = [correct, ...distractors]
  for (let i = allOptions.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[allOptions[i], allOptions[j]] = [allOptions[j], allOptions[i]]
  }
  options.value = allOptions
  selectedOption.value = null
}

function getDistractors(correctWord) {
  const pool = words.value.filter(w => w.id !== correctWord.id && w.definition)
  const shuffled = [...pool].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, 3).map(w => ({ label: w.definition, isCorrect: false }))
}

function selectOption(idx) {
  if (selectedOption.value !== null) return
  selectedOption.value = idx
  const isCorrect = options.value[idx].isCorrect

  // Record the answer for the current word
  if (answerRecords.value[currentIndex.value]) {
    answerRecords.value[currentIndex.value].known = isCorrect
    answerRecords.value[currentIndex.value].answered = true
  }

  if (currentIndex.value < words.value.length - 1) {
    // Next word
    setTimeout(() => {
      currentIndex.value++
      buildOptions()
    }, isCorrect ? 500 : 1000)
  } else {
    // Last word answered, submit
    setTimeout(async () => {
      await doSubmit()
    }, isCorrect ? 500 : 1000)
  }
}

async function doSubmit() {
  try {
    // Only submit words that were actually answered
    const answeredWords = answerRecords.value.filter(r => r.answered)
    if (answeredWords.length === 0) {
      ElMessage.warning('没有回答任何题目')
      testing.value = false
      return
    }
    const submitData = {
      answers: answeredWords.map(r => ({
        wordId: r.wordId,
        word: r.word,
        known: r.known
      })),
      testType: 'GUI'
    }
    const res = await submitTest(currentUser.value.id, submitData)
    if (res.code === 200) {
      testResult.value = res.data
      testing.value = false
      await loadHistory()
      await nextTick()
      renderChart()
    } else {
      ElMessage.error('提交失败: ' + (res.message || '未知错误'))
    }
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

function resetTest() {
  testResult.value = null
  words.value = []
  currentIndex.value = 0
  options.value = []
  selectedOption.value = null
  answerRecords.value = []
  startTest()
}

async function loadHistory() {
  if (!currentUser.value) return
  try {
    const res = await getTestHistory(currentUser.value.id)
    if (res.code === 200) {
      historyRecords.value = (res.data || []).slice().reverse()
    }
  } catch (e) {
    console.log(e)
  }
}

function renderChart() {
  if (!chartRef.value || historyRecords.value.length === 0) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: historyRecords.value.map((_, i) => '第' + (i + 1) + '次') },
    yAxis: { type: 'value', name: '词汇量' },
    series: [{
      name: '词汇量', type: 'line', data: historyRecords.value.map(r => r.estimateVocab),
      markLine: { data: [{ type: 'average', name: '均值' }] },
      itemStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64,158,255,0.1)' }
    }]
  })
}

onMounted(() => {
  const saved = localStorage.getItem('currentUser')
  if (saved) {
    currentUser.value = JSON.parse(saved)
    loadHistory()
  }
})
</script>

<style scoped>
.start-screen { text-align: center; padding: 40px 20px; }
.start-icon { margin-bottom: 15px; }
.start-screen h2 { font-size: 26px; margin-bottom: 10px; color: #303133; }
.start-desc { color: #909399; font-size: 14px; line-height: 1.8; margin-bottom: 25px; }
.start-info { max-width: 500px; margin: 0 auto 30px; }
.info-card { background: #f5f7fa; border-radius: 10px; padding: 15px 10px; }
.info-card .num { font-size: 22px; font-weight: bold; color: #409EFF; }
.info-card .label { font-size: 12px; color: #909399; margin-top: 5px; }
.start-btn { width: 220px; height: 50px; font-size: 18px; border-radius: 25px; }
.test-card { min-height: 450px; }
.test-header { display: flex; align-items: center; gap: 12px; margin-bottom: 30px; }
.progress-text { font-size: 14px; color: #909399; white-space: nowrap; min-width: 45px; }
.progress-bar-wrapper { flex: 1; }
.level-tag { flex-shrink: 0; }
.word-section { text-align: center; padding: 30px 0; }
.word-number { font-size: 13px; color: #909399; margin-bottom: 15px; }
.word-text { font-size: 48px; font-weight: bold; color: #303133; letter-spacing: 2px; font-family: 'Times New Roman', serif; }
.options-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; max-width: 560px; margin: 0 auto; padding: 0 10px 20px; }
.option-item {
  display: flex; align-items: center; gap: 10px;
  padding: 16px 18px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: #fff;
}
.option-item:hover:not(.disabled) { border-color: #409EFF; background: #f0f7ff; }
.option-item.correct { border-color: #67C23A; background: #f0f9eb; }
.option-item.wrong { border-color: #F56C6C; background: #fef0f0; }
.option-item.disabled { pointer-events: none; opacity: 0.85; }
.option-label { font-weight: bold; color: #409EFF; font-size: 14px; min-width: 18px; }
.option-text { font-size: 15px; color: #303133; flex: 1; }
.option-icon { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); }
.result-card { text-align: center; padding: 20px; }
.result-badge { margin-bottom: 10px; }
.result-badge.level-c { color: #F56C6C; }
.result-badge.level-f { color: #E6A23C; }
.result-badge.level-p { color: #409EFF; }
.result-badge.level-k { color: #909399; }
.result-card h2 { font-size: 24px; margin-bottom: 20px; color: #303133; }
.result-score { margin-bottom: 15px; }
.known-count { font-size: 48px; font-weight: bold; color: #67C23A; }
.known-count .unit { font-size: 20px; color: #909399; }
.score-label { font-size: 14px; color: #909399; margin-top: 5px; }
.estimate-section { display: flex; justify-content: center; gap: 40px; margin: 20px 0; }
.estimate-item { text-align: center; }
.estimate-value { font-size: 22px; font-weight: bold; color: #303133; }
.estimate-label { font-size: 12px; color: #909399; margin-top: 5px; }
.result-actions { margin-top: 25px; display: flex; gap: 15px; justify-content: center; }
</style>