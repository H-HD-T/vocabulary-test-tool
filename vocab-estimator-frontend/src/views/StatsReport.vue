<template>
  <div class="stats">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>四六级成绩与词汇量相关性</span></template>
          <div ref="scatterRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>概览统计</span></template>
          <div v-if="overview">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="总用户数">{{ overview.userCount }}</el-descriptions-item>
              <el-descriptions-item label="总测试数">{{ overview.testCount }}</el-descriptions-item>
              <el-descriptions-item label="平均词汇量">{{ overview.avgVocab }}</el-descriptions-item>
              <el-descriptions-item label="平均置信度">{{ overview.avgConfidence }}%</el-descriptions-item>
              <el-descriptions-item label="四级相关系数">
                <el-tag :type="overview.cet4Correlation > 0.5 ? 'success' : 'warning'">
                  {{ overview.cet4Correlation }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else style="text-align: center; padding: 40px; color: #909399;">加载统计中...</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>详细数据</span>
        <el-button size="small" style="float: right;" @click="exportReport">导出报表</el-button>
      </template>
      <el-table :data="correlationItems" border stripe size="small" max-height="400">
        <el-table-column prop="studentCode" label="学生" width="120" />
        <el-table-column prop="cet4Score" label="四级" width="80" />
        <el-table-column prop="cet6Score" label="六级" width="80" />
        <el-table-column prop="estimateVocab" label="估算词汇量" width="130" />
        <el-table-column prop="avgConfidence" label="平均置信度" width="130">
          <template #default="{ row }">{{ row.avgConfidence?.toFixed(1) }}%</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { getCorrelationStats, getOverviewStats } from '@/api/statsApi'

const scatterRef = ref(null)
const overview = ref(null)
const correlationItems = ref([])
let scatterChart = null

onMounted(async () => {
  try {
    const [corrRes, overviewRes] = await Promise.all([
      getCorrelationStats(),
      getOverviewStats()
    ])

    if (overviewRes.code === 200) {
      overview.value = overviewRes.data
    }

    if (corrRes.code === 200) {
      correlationItems.value = corrRes.data.correlationItems || []
      await nextTick()
      renderScatter()
    }
  } catch (e) {
    console.log('Stats not available')
  }
})

function renderScatter() {
  if (!scatterRef.value || correlationItems.value.length === 0) return
  if (scatterChart) scatterChart.dispose()
  scatterChart = echarts.init(scatterRef.value)

  const hasCET4 = correlationItems.value.some(i => i.cet4Score != null)
  const hasCET6 = correlationItems.value.some(i => i.cet6Score != null)

  const series = []
  if (hasCET4) {
    series.push({
      name: '四级',
      type: 'scatter',
      data: correlationItems.value
        .filter(i => i.cet4Score != null)
        .map(i => [i.cet4Score, i.estimateVocab]),
      itemStyle: { color: '#409EFF' }
    })
  }
  if (hasCET6) {
    series.push({
      name: '六级',
      type: 'scatter',
      data: correlationItems.value
        .filter(i => i.cet6Score != null)
        .map(i => [i.cet6Score, i.estimateVocab]),
      itemStyle: { color: '#F56C6C' }
    })
  }

  scatterChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: function(p) {
        return p.seriesName + '<br/>分数: ' + p.value[0] + '<br/>词汇量: ' + p.value[1]
      }
    },
    xAxis: { type: 'value', name: '四六级分数' },
    yAxis: { type: 'value', name: '估算词汇量' },
    series: series,
    legend: { data: ['四级', '六级'] }
  })
}

function exportReport() {
  const data = correlationItems.value.map(i => ({
    学生: i.studentCode,
    CET4: i.cet4Score,
    CET6: i.cet6Score,
    VocabEstimate: i.estimateVocab,
    AvgConfidence: (i.avgConfidence?.toFixed(1) || '') + '%'
  }))
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '统计报表')
  XLSX.writeFile(wb, 'vocab_stats_report.xlsx')
  ElMessage.success('报表已导出')
}
</script>