<template>
  <div class="batch-manage">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>词表输入</span></template>
          <el-input
            v-model="textInput"
            type="textarea"
            :rows="12"
            placeholder="每行一个单词,格式: 单词, 认识(或 不认识)&#10;例如:&#10;apple, known&#10;philosophy, unknown&#10;abandon, recognized"
          />
          <div style="margin-top: 10px;">
            <el-upload
              action="#"
              accept=".txt"
              :show-file-list="false"
              :before-upload="handleFileUpload"
            >
              <el-button>上传TXT文件</el-button>
            </el-upload>
            <el-button type="primary" style="margin-left: 10px;" @click="processBatch" :loading="loading">
              批量计算
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header><span>批量估算结果</span></template>

          <!-- 整体估算结果卡片 -->
          <div v-if="batchResult" style="text-align: center; padding: 10px 0;">
            <el-row :gutter="20">
              <el-col :span="8">
                <div style="background: #f0f9eb; border-radius: 8px; padding: 15px 5px;">
                  <div style="font-size: 28px; font-weight: bold; color: #67C23A;">{{ batchResult.estimate }}</div>
                  <div style="font-size: 12px; color: #909399; margin-top: 5px;">估算词汇量</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="background: #f0f7ff; border-radius: 8px; padding: 15px 5px;">
                  <div style="font-size: 20px; font-weight: bold; color: #409EFF;">{{ batchResult.minRange }} - {{ batchResult.maxRange }}</div>
                  <div style="font-size: 12px; color: #909399; margin-top: 5px;">估算范围</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="background: #fdf6ec; border-radius: 8px; padding: 15px 5px;">
                  <div style="font-size: 28px; font-weight: bold; color: #E6A23C;">{{ batchResult.confidence.toFixed(1) }}%</div>
                  <div style="font-size: 12px; color: #909399; margin-top: 5px;">置信度</div>
                </div>
              </el-col>
            </el-row>
            <el-tag type="success" style="margin-top: 10px;">
              认识: {{ batchResult.knownCount }} / {{ batchResult.knownCount + batchResult.unknownCount }} 词
            </el-tag>
          </div>

          <div v-else-if="parsedWords.length > 0" style="padding: 20px; text-align: center; color: #909399;">
            请点击"批量计算"进行分析
          </div>
          <div v-else style="padding: 20px; text-align: center; color: #909399;">
            请在左侧输入词表后点击批量计算
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 单词明细表 -->
    <el-card style="margin-top: 20px;" v-if="parsedWords.length > 0">
      <template #header>
        <span>单词明细({{ parsedWords.length }}词)</span>
        <el-button size="small" style="float: right;" @click="exportExcel" v-if="batchResult">
          导出Excel
        </el-button>
      </template>
      <el-table :data="parsedWords" max-height="350" border stripe size="small">
        <el-table-column prop="word" label="单词" width="140" />
        <el-table-column prop="known" label="标记" width="100">
          <template #default="{ row }">
            <el-tag :type="row.known ? 'success' : 'danger'" size="small">
              {{ row.known ? '认识' : '不认识' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" />
        <el-table-column prop="definition" label="释义" min-width="180" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 采样测试 -->
    <el-card style="margin-top: 20px;">
      <template #header><span>采样测试(稳定性验证)</span></template>
      <el-form :model="samplingForm" inline>
        <el-form-item label="采样长度">
          <el-select v-model="samplingForm.sampleLength">
            <el-option label="200 词" :value="200" />
            <el-option label="300 词" :value="300" />
            <el-option label="400 词" :value="400" />
          </el-select>
        </el-form-item>
        <el-form-item label="认识比例(%)">
          <el-slider v-model="samplingForm.knowRatio" :min="10" :max="90" style="width: 200px;" show-input />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="runSampling" :loading="samplingLoading">运行900组采样</el-button>
        </el-form-item>
      </el-form>
      <div v-if="samplingResult">
        <el-alert
          :title="'均值: ' + (samplingResult.meanEstimate || '-') + ' | 方差: ' + (samplingResult.variance || '-') + ' | 样本数: ' + (samplingResult.sampleCount || '-')"
          type="success"
          show-icon
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadBatchText, uploadBatchFile, runSamplingTest } from '@/api/batchApi'
import * as XLSX from 'xlsx'

const textInput = ref('')
const parsedWords = ref([])
const batchResult = ref(null)
const loading = ref(false)
const samplingLoading = ref(false)
const samplingResult = ref(null)
const samplingForm = reactive({
  sampleLength: 200,
  knowRatio: 50
})

async function processBatch() {
  if (!textInput.value.trim()) {
    ElMessage.warning('请先输入词表')
    return
  }
  loading.value = true
  batchResult.value = null
  parsedWords.value = []
  try {
    const res = await uploadBatchText({ textContent: textInput.value })
    if (res.code === 200) {
      // 后端返回 data.results = [每条词表的估算结果]
      const results = res.data.results || []

      // 提取每个词表的单词信息
      const words = []
      let totalKnown = 0
      let totalUnknown = 0
      let sumEstimate = 0
      let sumMin = 0
      let sumMax = 0
      let sumConfidence = 0
      let validCount = 0

      for (const r of results) {
        // 解析 wordLine: "apple, known"
        const parts = (r.wordLine || '').split(',')
        const word = parts[0]?.trim() || ''
        const known = (parts[1]?.trim() || '').toLowerCase() === 'known' || (parts[1]?.trim() || '').toLowerCase() === '认识'

        words.push({
          word: word,
          known: known,
          difficulty: r.estimate?.difficulty || '',
          definition: r.estimate?.definition || ''
        })

        if (known) totalKnown++
        else totalUnknown++

        if (r.estimate) {
          sumEstimate += r.estimate.estimate || 0
          sumMin += r.estimate.minRange || 0
          sumMax += r.estimate.maxRange || 0
          sumConfidence += r.estimate.confidence || 0
          validCount++
        }
      }

      parsedWords.value = words

      // 计算整体估算值 (均值)
      if (validCount > 0) {
        batchResult.value = {
          estimate: Math.round(sumEstimate / validCount),
          minRange: Math.round(sumMin / validCount),
          maxRange: Math.round(sumMax / validCount),
          confidence: sumConfidence / validCount,
          knownCount: totalKnown,
          unknownCount: totalUnknown
        }
      } else {
        ElMessage.warning('无法解析词表')
      }
    }
  } catch (e) {
    ElMessage.error('批量计算失败')
  } finally {
    loading.value = false
  }
}

function handleFileUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    textInput.value = e.target.result
    ElMessage.success('文件已加载: ' + file.name)
  }
  reader.readAsText(file)
  return false
}

async function runSampling() {
  samplingLoading.value = true
  try {
    const params = {
      sampleLength: samplingForm.sampleLength,
      knowRatio: samplingForm.knowRatio
    }
    const res = await runSamplingTest(params)
    if (res.code === 200) {
      samplingResult.value = res.data
      ElMessage.success('采样测试完成')
    }
  } catch (e) {
    ElMessage.error('采样测试失败')
  } finally {
    samplingLoading.value = false
  }
}

function exportExcel() {
  const data = parsedWords.value.map(r => ({
    单词: r.word,
    标记: r.known ? '认识' : '不认识',
    难度: r.difficulty || '',
    释义: r.definition || ''
  }))
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '批量结果')
  XLSX.writeFile(wb, 'batch_results.xlsx')
  ElMessage.success('导出成功')
}
</script>