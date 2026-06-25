<template>
  <div class="corpus-analysis">
    <el-card>
      <template #header><span>语料输入</span></template>
      <el-input
        v-model="corpusText"
        type="textarea"
        :rows="10"
        placeholder="在此粘贴英文文本, 例如:&#10;I enjoy reading books and learning new words every day."
      />
      <div style="margin-top: 10px;">
        <el-upload
          action="#"
          accept=".txt"
          :show-file-list="false"
          :before-upload="handleFileUpload"
          style="display: inline-block;"
        >
          <el-button>上传TXT文件</el-button>
        </el-upload>
        <el-button type="primary" style="margin-left: 10px;" @click="analyzeText" :loading="analyzing">
          分析词汇量
        </el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header><span>分析结果</span></template>

      <div v-if="analysisResult" style="padding: 10px 0;">
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="8">
            <div style="background: #f0f9eb; border-radius: 8px; padding: 20px 5px; text-align: center;">
              <div style="font-size: 32px; font-weight: bold; color: #67C23A;">{{ analysisResult.estimate }}</div>
              <div style="font-size: 12px; color: #909399; margin-top: 5px;">估算词汇量</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div style="background: #f0f7ff; border-radius: 8px; padding: 20px 5px; text-align: center;">
              <div style="font-size: 22px; font-weight: bold; color: #409EFF;">{{ analysisResult.minRange }} - {{ analysisResult.maxRange }}</div>
              <div style="font-size: 12px; color: #909399; margin-top: 5px;">估算范围</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div style="background: #fdf6ec; border-radius: 8px; padding: 20px 5px; text-align: center;">
              <div style="font-size: 32px; font-weight: bold; color: #E6A23C;">{{ (analysisResult.confidence || 0).toFixed(1) }}%</div>
              <div style="font-size: 12px; color: #909399; margin-top: 5px;">置信度</div>
            </div>
          </el-col>
        </el-row>

        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="总词数">{{ analysisResult.totalWords }}</el-descriptions-item>
          <el-descriptions-item label="不重复词数">{{ extractedWords.length }}</el-descriptions-item>
          <el-descriptions-item label="有效样本">{{ analysisResult.validSamples }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-else-if="!analyzing" style="text-align: center; padding: 40px; color: #909399;">
        <el-icon :size="40"><DataAnalysis /></el-icon>
        <p>输入或上传英文文本后点击"分析词汇量"</p>
      </div>
      <div v-else style="text-align: center; padding: 40px; color: #909399;">
        <el-icon :size="40" class="is-loading"><Loading /></el-icon>
        <p>分析中...</p>
      </div>
    </el-card>

    <!-- 提取的单词展示 -->
    <el-card style="margin-top: 20px;" v-if="extractedWords.length > 0">
      <template #header><span>提取的单词({{ extractedWords.length }}个)</span></template>
      <div style="max-height: 350px; overflow-y: auto;">
        <el-tag
          v-for="(w, idx) in extractedWords"
          :key="idx"
          size="small"
          style="margin: 3px;"
        >
          {{ w }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { importCorpusText, analyzeAllCorpuses } from '@/api/corpusApi'

const corpusText = ref('')
const analysisResult = ref(null)
const extractedWords = ref([])
const analyzing = ref(false)

function handleFileUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    corpusText.value = e.target.result
    ElMessage.success('文件已加载: ' + file.name)
  }
  reader.readAsText(file)
  return false
}

async function analyzeText() {
  if (!corpusText.value.trim()) {
    ElMessage.warning('请输入或上传英文文本')
    return
  }
  analyzing.value = true
  analysisResult.value = null
  extractedWords.value = []

  try {
    const importRes = await importCorpusText('C', corpusText.value)
    if (importRes.code !== 200) {
      ElMessage.error('导入失败')
      analyzing.value = false
      return
    }

    if (importRes.data && importRes.data.extractedWords) {
      try {
        extractedWords.value = JSON.parse(importRes.data.extractedWords)
      } catch (e) {}
    }

    const analyzeRes = await analyzeAllCorpuses()
    if (analyzeRes.code === 200) {
      const results = analyzeRes.data || []
      if (results.length > 0) {
        const match = results.find(r => r.corpusType === 'C') || results[0]
        analysisResult.value = {
          estimate: match.estimate?.estimate,
          minRange: match.estimate?.minRange,
          maxRange: match.estimate?.maxRange,
          confidence: match.estimate?.confidence,
          totalWords: match.totalWords,
          validSamples: (match.estimate?.knownCount || 0) + (match.estimate?.unknownCount || 0)
        }
      } else {
        ElMessage.warning('分析没有返回结果')
      }
    }
  } catch (e) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}
</script>