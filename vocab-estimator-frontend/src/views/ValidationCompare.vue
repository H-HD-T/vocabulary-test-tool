<template>
  <div class="validation">
    <el-card>
      <template #header>
        <span>算法验证(与TestYourVocab对比)</span>
      </template>

      <el-alert
        title="导入testyourvocab.com验证数据集,与本算法结果进行对比分析。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-form>
        <el-form-item label="验证数据">
          <el-input
            v-model="jsonData"
            type="textarea"
            :rows="10"
            placeholder='JSON格式: [{"knownWords":["word1","word2"],"unknownWords":["word3"],"standardEstimate":8000}]'
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="runValidation" :loading="loading">运行验证</el-button>
          <el-upload
            action="#"
            accept=".json,.txt"
            :show-file-list="false"
            :before-upload="handleFileUpload"
            style="display: inline-block; margin-left: 10px;"
          >
            <el-button>上传JSON文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px;" v-if="validationResult">
      <template #header><span>验证结果</span></template>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 28px; font-weight: bold; color: #409EFF;">{{ validationResult.meanError?.toFixed(2) }}</div>
              <div style="font-size: 12px; color: #909399;">平均绝对误差</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 28px; font-weight: bold; color: #67C23A;">{{ validationResult.meanBias?.toFixed(2) }}</div>
              <div style="font-size: 12px; color: #909399;">平均偏差</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 28px; font-weight: bold; color: #E6A23C;">{{ validationResult.correlation?.toFixed(4) }}</div>
              <div style="font-size: 12px; color: #909399;">相关系数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 28px; font-weight: bold; color: #303133;">{{ validationResult.sampleCount }}</div>
              <div style="font-size: 12px; color: #909399;">样本数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div ref="chartRef" style="height: 350px; margin-top: 20px;"></div>

      <el-table :data="validationResult.items" border stripe size="small" max-height="400" style="margin-top: 20px;">
        <el-table-column label="标准值(Ci)" width="120">
          <template #default="{ row }">{{ row.standardEstimate }}</template>
        </el-table-column>
        <el-table-column label="算法值(Di)" width="130">
          <template #default="{ row }">{{ row.algorithmEstimate }}</template>
        </el-table-column>
        <el-table-column label="差值" width="100">
          <template #default="{ row }">
            <el-tag :type="Math.abs(row.diff) < 1000 ? 'success' : Math.abs(row.diff) < 2000 ? 'warning' : 'danger'">
              {{ row.diff >= 0 ? '+' : '' }}{{ row.diff }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认识单词" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.knownWords?.join(', ') }}</template>
        </el-table-column>
        <el-table-column label="不认识单词" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.unknownWords?.join(', ') }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { importValidationData, uploadValidationFile } from '@/api/validationApi'

const jsonData = ref('')
const loading = ref(false)
const validationResult = ref(null)
const chartRef = ref(null)
let chart = null

async function runValidation() {
  if (!jsonData.value.trim()) {
    ElMessage.warning('请输入验证数据')
    return
  }
  loading.value = true
  try {
    const res = await importValidationData(jsonData.value)
    if (res.code === 200) {
      validationResult.value = res.data
      ElMessage.success('验证完成')
      await nextTick()
      renderChart()
    }
  } catch (e) {
    ElMessage.error('验证失败')
  } finally {
    loading.value = false
  }
}

function handleFileUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    jsonData.value = e.target.result
    ElMessage.success('文件已加载: ' + file.name)
  }
  reader.readAsText(file)
  return false
}

function renderChart() {
  if (!chartRef.value || !validationResult.value) return
  if (chart) chart.dispose()
  chart = echarts.init(chartRef.value)

  const items = validationResult.value.items?.slice(0, 20) || []
  const categories = items.map((_, i) => '样本 ' + (i + 1))

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['标准值(Ci)', '算法值(Di)', '差值'] },
    xAxis: { type: 'category', data: categories },
    yAxis: [
      { type: 'value', name: '词汇量' },
      { type: 'value', name: '差值' }
    ],
    series: [
      {
        name: '标准值(Ci)',
        type: 'bar',
        data: items.map(i => i.standardEstimate),
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '算法值(Di)',
        type: 'bar',
        data: items.map(i => i.algorithmEstimate),
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '差值',
        type: 'line',
        yAxisIndex: 1,
        data: items.map(i => i.diff),
        itemStyle: { color: '#F56C6C' },
        lineStyle: { type: 'dashed' }
      }
    ]
  })
}
</script>